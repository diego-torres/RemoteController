package com.dtorres.remoted;

/**
 * This class will run in the client machine to communicate events and objects trough a result.
 */
import java.awt.AWTException;
import java.awt.Robot;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.dtorres.localed.Localed;

public class Remoted {
	
	private final static Logger log = Logger.getLogger(Remoted.class);
	
	private final ObjectOutputStream output;
	private final ObjectInputStream input;
	private final Robot robot;

	public Remoted(String serverName, String clientName) throws IOException,
			AWTException {
		log.info("Creating new instance of Remoted.");
		Socket socket = new Socket(serverName, Localed.PORT);
		log.debug("Socket object created.");
		robot = new Robot();
		output = new ObjectOutputStream(socket.getOutputStream());
		log.debug("Output stream initialized.");
		input = new ObjectInputStream(socket.getInputStream());
		log.debug("Input stream initialized.");
		output.writeObject(clientName);
		output.flush();
		log.debug("Flushed output stream.");
		log.info("new instance of Remoted created.");
	}

	public void run() throws ClassNotFoundException {
		log.info("Running remoted listener");
		try {
			// listens to action requests
			while (true) {
				RemoteAction action = (RemoteAction) input.readObject();
				// Nice use of polymorphism right? if it barks, as a dog,
				// movesTail as a dog, breaths as a dog, then it can be casted
				// to a dog object.
				Object result = action.execute(robot);
				if (result != null) {
					log.debug("Writting response to output stream");
					output.writeObject(result);
					output.flush();
					output.reset();
				}
			}
		} catch (IOException e) {
			log.error("Exception while running the remoted listener", e);
		}
		log.info("Remoted listener just stopped");
	}
	
	public static void main(String[] args) throws IOException, AWTException, ClassNotFoundException{
		Remoted remoted = new Remoted(args[0], args[1]);
		remoted.run();
	}

}
