package com.dtorres.shared;

/**
 * Used to specify the mouse button to be clicked, and how many times it will be clicked.
 */

import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.dtorres.remoted.RemoteAction;

public class MouseClick implements RemoteAction {

	private static final long serialVersionUID = -8092981078403403340L;
	private static final Logger log = Logger.getLogger(MouseClick.class);
	private final int mouseModifiers;
	private final int clickCount;

	public MouseClick(int mouseModifiers, int clickCount) {
		log.info("Initializing a MouseClicker");
		this.mouseModifiers = mouseModifiers;
		this.clickCount = clickCount;
		log.info("Initializing a MouseClicker");
	}

	public MouseClick(MouseEvent event) {
		this(event.getModifiers(), event.getClickCount());
		log.info("built from MouseClick overload with MouseEvent.");
	}

	/**
	 * Difference between override and overload: the overload comes inside this
	 * same class, using the same method name, but different signature (the
	 * parameters are either from different type or the amount of parameters are
	 * different).
	 */
	@Override
	public Object execute(Robot robot) throws IOException {
		for (int i = 0; i < clickCount; i++) {
			robot.mousePress(mouseModifiers);
			robot.mouseRelease(mouseModifiers);
		}
		return null;
	}

	/**
	 * Overriding from Object inherited method to assign an String value when
	 * invoking MouseClick.toString(), used while logging in RemotedActionQueue
	 * while adding tasks to the action queue
	 */
	@Override
	public String toString() {
		return "MouseClick: button: [" + mouseModifiers + "], [" + clickCount
				+ "]";
	}

}
