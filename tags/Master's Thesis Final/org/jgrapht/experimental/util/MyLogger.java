package org.jgrapht.experimental.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Get a logger
 * 
 * @author moritzfuchs
 *
 */
public class MyLogger {
	
	static private FileHandler file;
	static private SimpleFormatter formatter;

	static public void setup() throws IOException {

		// Get the global logger to configure it
		Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
		logger.setLevel(Level.OFF);
		
		file = new FileHandler("Log.txt");
	
		// Create txt Formatter
		formatter = new SimpleFormatter();
		file.setFormatter(formatter);
		logger.addHandler(file);
		
	}
} 
