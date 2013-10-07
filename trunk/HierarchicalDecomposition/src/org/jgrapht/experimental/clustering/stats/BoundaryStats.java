package org.jgrapht.experimental.clustering.stats;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import org.jgrapht.experimental.util.LoggerFactory;

public class BoundaryStats {

	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BoundaryStats.class.getName());

	/**
	 * Singleton instance for this class
	 */
	private static BoundaryStats instance = null;
	
	/**
	 * Database connection
	 */
	private Connection c;
	
	private PreparedStatement boundarySplitStatement;
	
	private BoundaryStats() {
		try {
			c = SQLiteConnection.getConnection();
		    Statement statement = c.createStatement();
		    statement.setQueryTimeout(30);  // set timeout to 30 sec
		      
		    statement.executeUpdate("CREATE TABLE IF NOT EXISTS boundary_splits ("
		      		+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
		      		+ "nodes INTEGER, "
		      		+ "edgesIn INTEGER, "
		      		+ "edgesOut INTEGER, "
		      		+ "os STRING,"
		      		+ "cut BOOLEAN, "
		      		+ "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
		      		+ ")");
		    
		    boundarySplitStatement = c.prepareStatement("INSERT INTO boundary_splits ('nodes', 'edgesIn', 'edgesOut', 'cut' ,'os') VALUES (?,?,?,?,?)");
		} catch(SQLException e) {
			e.printStackTrace();
		  	LOGGER.warning("SQL-Exception: " + e.getMessage());
		}

	}
	
	/**
	 * Returns singleton instance of this class
	 * 
	 * @return : Singleton KRVStats object
	 */
	public static BoundaryStats getInstance() {
		if (instance == null) {
			instance = new BoundaryStats();
		}
		
		return instance;
	}
	
	/**
	 * Inserts a new row into the database, returns id of new row
	 * 
	 * @param nodes : Number of nodes in the graph
	 * @param edges : Number of edges in the graph
	 * @return Integer : The ID of the new row  
	 */
	public synchronized Integer addRun(Integer nodes , Integer edgesIn,Integer edgesOut , Boolean cut) {
		Integer result = -1;
		
		try {
			
			boundarySplitStatement.setInt(1, nodes);
			boundarySplitStatement.setInt(2, edgesIn);
			boundarySplitStatement.setInt(3, edgesOut);
			boundarySplitStatement.setBoolean(4, cut);
			boundarySplitStatement.setString(5, System.getProperty("os.name"));
			
			boundarySplitStatement.execute();
			boundarySplitStatement.clearParameters();
		} catch (SQLException e) {
			LOGGER.warning("Could not insert new Boundary split row! Message: " + e.getMessage());
		}
		return result;
	}

	
	/**
	 * Cleanup connection when closing.
	 */
	public void close() {
		try {
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
