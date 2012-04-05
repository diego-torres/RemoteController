package com.dtorres.sync;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.dtorres.remoted.RemoteAction;

/**
 * Synchronizes the transfers between localed and remoted 
 * @author dtorres
 *
 */


public class RemoteActionQueue {
	private final LinkedList<RemoteAction> tasks = new LinkedList<RemoteAction>();
	private static final Logger log = Logger.getLogger(RemoteActionQueue.class);
	public RemoteAction next() throws InterruptedException{
		synchronized(tasks){
			while(tasks.isEmpty()){
				tasks.wait();
			}
			return tasks.removeFirst();
		}
	}
	
	public void add(RemoteAction task){
		synchronized(tasks){
			tasks.add(task);
			// This is where that toString override in shared objects takes effect.
			log.info("Registered task on queue: [" + tasks + "]");
			tasks.notifyAll();
		}
	}
	
}
