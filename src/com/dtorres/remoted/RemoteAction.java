package com.dtorres.remoted;
/**
 * This interface will be used by remoted machine to represent a command execution.
 */
import java.awt.Robot;
import java.io.IOException;
import java.io.Serializable;

public interface RemoteAction extends Serializable {
	Object execute(Robot robot) throws IOException;
}
