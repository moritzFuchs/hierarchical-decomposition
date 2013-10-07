package org.jgrapht.experimental.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

/**
 * Factory class for logger objects
 * 
 * @author moritzfuchs
 *
 */
public class LoggerFactory {

	private static final Level LEVEL = Level.FINE;
	
	private static FileHandler handler = null;
	
	/**
	 * Get a new logger
	 * 
	 * @param name : The name of the new logger
	 * @return : A new logger
	 */
	public static Logger getLogger(String name) {

		Logger l = Logger.getLogger(name);
		l.setLevel(LEVEL);
		
		if (handler == null) {
			try {
				handler = new FileHandler("Log.txt");
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// Create txt Formatter
			SimpleFormatter formatter = new SimpleFormatter();
			handler.setFormatter(formatter);
		}
		
		l.addHandler(handler);
		

		
		return l;
	}
	
}
