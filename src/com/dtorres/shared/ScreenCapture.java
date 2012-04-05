package com.dtorres.shared;

/**
 * Take an screen shot as pass it to the localed as JPEG, check that you can manage the quality to make it faster or better printed.
 */
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.Logger;

import com.dtorres.remoted.RemoteAction;

public class ScreenCapture implements RemoteAction {
	private static final long serialVersionUID = -2203789071548240406L;
	private static final Logger log = Logger.getLogger(ScreenCapture.class);

	@Override
	public Object execute(Robot robot) throws IOException {
		// Retrieve screen size to be used in the screen capture
		Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
		Rectangle screenRect = new Rectangle(defaultToolkit.getScreenSize());
		// Encode the screen as jpeg to send bytes through the socket
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		ImageOutputStream iOut = ImageIO.createImageOutputStream(out);

		BufferedImage bufferedImage = robot.createScreenCapture(screenRect);

		IIOImage outputImage = new IIOImage(bufferedImage, null, null);
		ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
		writer.setOutput(iOut);
		ImageWriteParam writeParam = writer.getDefaultWriteParam();
		writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		writeParam.setCompressionQuality(.75f); // Quality @ 75%

		writer.write(null, outputImage, writeParam);
		boolean stillReading = true;
		while (stillReading) {
			try {
				out.write(iOut.readByte());
			} catch (EOFException e) {
				break;
			} catch (IOException e) {
				log.error("Error processing the Image Stream", e);
				break;
			}
		}

		return out.toByteArray();
	}

	@Override
	public String toString() {
		return "ScreenCapture";
	}

}
