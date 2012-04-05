package com.dtorres.shared;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.IOException;

import com.dtorres.remoted.RemoteAction;

public class MessagingWindow implements RemoteAction {

	private static final long serialVersionUID = 7846268853462669827L;
	private final String message;

	public MessagingWindow(String message) {
		this.message = message;
	}

	@Override
	public Object execute(Robot robot) throws IOException {
		Frame frame = new Frame();
		frame.setSize(200, 200);
		frame.add("Center", new Label(message));
		Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
		Dimension screenDimension = defaultToolkit.getScreenSize();
		frame.setLocation(screenDimension.width/3, screenDimension.height/3);
		frame.pack();
		frame.setVisible(true);
		return null;
	}
	
	@Override
	public String toString(){
		return "MessagingWindow (\"" + message + "\")";
	}

}
