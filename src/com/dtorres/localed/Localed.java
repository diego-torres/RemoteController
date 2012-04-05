package com.dtorres.localed;

/**
 * Listens when a remoted connects and read the screen to gain control over sockets
 */

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.dtorres.remoted.RemoteAction;
import com.dtorres.shared.MouseClick;
import com.dtorres.shared.MouseMove;
import com.dtorres.shared.ScreenCapture;
import com.dtorres.sync.RemoteActionQueue;

public class Localed extends JFrame {

	// Generated serial version id by eclipse Helios wizard
	private static final long serialVersionUID = -6282303754001595150L;
	private static final Logger log = Logger.getLogger(Localed.class);

	// Communication port
	public static final int PORT = 7357;
	private static final long SCREEN_COM_SYNC = 2000;
	private static final int HEIGHT = 400;
	private static final int WIDTH = 500;

	private final ObjectInputStream input;
	private final ObjectOutputStream output;
	private final String remotedName;
	private final Timer timer;
	private final Thread invoker;
	private final JLabel screenImageLabel = new JLabel();
	private final RemoteActionQueue tasks = new RemoteActionQueue();

	private volatile boolean running = true;

	public Localed(Socket socket) throws IOException, ClassNotFoundException {
		log.info("Starting localed program instance");
		output = new ObjectOutputStream(socket.getOutputStream());
		input = new ObjectInputStream(socket.getInputStream());
		remotedName = (String) input.readObject();
		init();

		createListener();
		timer = createScreenRetriever();
		invoker = createInvoker();

		configureExitEvents();
		// Stop the timer when the window is closed.
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				timer.cancel();
			}
		});

		log.info("Finished connecting to [" + socket + "]");
	}

	private void configureExitEvents() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				log.info("Shutting down the timer");
				timer.cancel();
			}
		});
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					log.info("Shutting down the output stream");
					output.close();
				} catch (IOException ex) {
					log.error("Unable to close output stream", ex);
				}
				try {
					log.info("Shutting down the input stream");
					input.close();
				} catch (IOException ex) {
					log.error("Unable to close input stream", ex);
				}
			}
		});

	}

	private Thread createInvoker() {
		Thread invoker = new Thread("Invoker") {
			public void run() {
				try {
					while (true) {
						RemoteAction action = tasks.next();
						output.writeObject(action);
						output.flush();
					}
				} catch (Exception e) {
					log.error("Fail while invoking action", e);
					setTitle("disconnected from [" + remotedName + "]");
				}
			}
		};
		invoker.start();
		return invoker;
	}

	private Timer createScreenRetriever() {
		Timer timer = new Timer();

		TimerTask retrieverTask = new TimerTask() {
			public void run() {
				tasks.add(new ScreenCapture());
			}
		};

		timer.schedule(retrieverTask, 1, SCREEN_COM_SYNC);
		return timer;
	}
	
	private void createListener() {
		Thread listener = new Thread() {
			public void run() {
				while (true) {
					try {
						byte[] imageBytes = (byte[]) input.readObject();
						log.info("Received [" + imageBytes.length
								+ "] bytes from remoted [" + remotedName + "]");
						renderImage(imageBytes);
					} catch (Exception e) {
						log.error("Fail while receiving image", e);
						invoker.interrupt();
						timer.cancel();
						running = false;
						return;
					}
				}
			}
		};
		listener.start();
	}

	protected void renderImage(byte[] imageBytes) throws IOException {
		Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("jpg");
		ImageReader reader = readers.next();
		ImageInputStream imageIs = ImageIO.createImageInputStream(imageBytes);
		reader.setInput(imageIs, true);
		ImageReadParam param = reader.getDefaultReadParam();
		
		Image image = reader.read(0, param);
		final BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				screenImageLabel.setIcon(new ImageIcon(bufferedImage));
			}
		});
	}

	private void init() {
		log.info("Init - BEGIN");
		setTitle("Connected to [" + remotedName + "]");
		// Show the remote screen and listen to mouse events
		getContentPane().add(new JScrollPane(screenImageLabel));
		screenImageLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (running) {
					tasks.add(new MouseMove(e));
					tasks.add(new MouseClick(e));
					tasks.add(new ScreenCapture());
				} else {
					// Not running, beep
					log.warn("Tryied to click a remote client with no connection.");
					Toolkit.getDefaultToolkit().beep();
				}
			}
		});
		setSize(WIDTH, HEIGHT);
		setVisible(true);
		log.info("Init - END");
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		log.info("Connection listening for client acceptance.");
		ServerSocket serverSocket = new ServerSocket(PORT);
		while(true){
			Socket socket = serverSocket.accept();
			log.info("Connection from " + socket);
			new Localed(socket);
		}
	}

}
