package org.jgrapht.experimental.clustering.stats;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.jgrapht.experimental.util.LoggerFactory;

public class SQLiteConnection {

	/**
	 * Logger for this class
	 */
	private static Logger LOGGER = LoggerFactory.getLogger(SQLiteConnection.class.getName());
	
	/**
	 * Database connection
	 */
	private static Connection c = null;
	
	/**
	 * Path to SQLite database file
	 */
	private static String path = "db/stats.db";
	
	/**
	 * Returns a singleton {@link Connection} to the SQLite database
	 * 
	 * @return Connection : The database connection
	 */
	public static Connection getConnection() {
		try {
			if (c == null) {
				Class.forName("org.sqlite.JDBC");
				
			    // create a database connection
			    c = DriverManager.getConnection("jdbc:sqlite:" + path);
			}
		} catch (ClassNotFoundException e) {
			LOGGER.warning("JDBC Driver not found!");
			e.printStackTrace();
		} catch (SQLException e) {
			LOGGER.warning("Could not connect do database!");
			e.printStackTrace();
		}
		
		return c;
	}
	
	public void close() {
		try {
			this.c.close();
		} catch (SQLException e) {
			LOGGER.warning("Could not close database connection. Message: " + e.getMessage());
		}
	}
	
}
