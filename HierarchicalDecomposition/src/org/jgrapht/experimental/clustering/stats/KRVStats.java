package org.jgrapht.experimental.clustering.stats;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import org.jgrapht.experimental.util.LoggerFactory;

public class KRVStats {

	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(KRVStats.class.getName());

	/**
	 * Singleton instance for this class
	 */
	private static KRVStats instance = null;
	
	/**
	 * Database connection
	 */
	private Connection c;
	
	
	private PreparedStatement krv_run_update = null;
	private PreparedStatement krv_iteration_insert = null;
	
	
	
	private KRVStats() {
		try {
		      Class.forName("org.sqlite.JDBC");

		      // create a database connection
		      c = DriverManager.getConnection("jdbc:sqlite:db/stats.db");
		      Statement statement = c.createStatement();
		      statement.setQueryTimeout(30);  // set timeout to 30 sec
		      
		      statement.executeUpdate("CREATE TABLE IF NOT EXISTS krv_runs ("
		      		+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
		      		+ "nodes INTEGER, "
		      		+ "edges INTEGER, "
		      		+ "time LONG, "
		      		+ "os STRING,"
		      		+ "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
		      		+ ")");
		      
		      statement.executeUpdate("CREATE TABLE IF NOT EXISTS krv_iterations ("
		      		+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
		      		+ "krv_run_id INTEGER, "
		      		+ "time LONG, "
		      		+ "time_max_flow INTEGER,"
		      		+ "iteration INTEGER,"
		      		+ "potential DOUBLE,"
		      		+ "edges_in_game INTEGER,"
		      		+ "os STRING,"
		      		+ "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
		      		+ ")");
		      
		    } catch(SQLException e) {
		    	e.printStackTrace();
		    	LOGGER.warning("SQL-Exception: " + e.getMessage());
		    } catch(ClassNotFoundException e) {
		    	e.printStackTrace();
		    	LOGGER.warning("Could not find JDBC driver for SQLite database.");
		    }
	}
	
	/**
	 * Returns singleton instance of this class
	 * 
	 * @return : Singleton KRVStats object
	 */
	public static KRVStats getInstance() {
		if (instance == null) {
			instance = new KRVStats();
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
	public synchronized Integer registerKRVRun(Integer nodes , Integer edges) {
		Integer result = -1;
		
		try {
			Statement statement = c.createStatement();
			statement.executeUpdate("INSERT INTO krv_runs ('nodes', 'edges', 'os') VALUES (" + nodes + "," + edges + ", '" + System.getProperty("os.name") + "')");
			ResultSet rs = statement.executeQuery("SELECT last_insert_rowid()");
			result = rs.getInt(1);
			statement.close();
		} catch (SQLException e) {
			LOGGER.warning("Could not insert new KRV run row! Message: " + e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * Updates a KRVRun with the time it took in total
	 * 
	 * @param id : ID of the KRVRun
	 * @param time : TIme it took to finish in ms
	 */
	public void updateKRVRun(Integer id , long time) {
		try {
			if (krv_run_update == null) {
				krv_run_update = c.prepareStatement("UPDATE krv_runs SET time = ? WHERE id = ?");
			}
			
			krv_run_update.setLong(1, time);
			krv_run_update.setInt(2, id);
			
			krv_run_update.execute();
			
		} catch (SQLException e) {
			LOGGER.warning("Could not update KRV run! Message: " + e.getMessage());
		}
	}
	
	/**
	 * Adds information about a single iteration of the KRV procedure and saves it to the DB.
	 * 
	 * @param id : ID of the KRV run
	 * @param edges_in_game : Number of edges that are still in the game
	 * @param time : Time it took to finish this iteration.
	 * @param iteration : Number of the iteration
	 * @param potential : Potential after this iteration.
	 */
	public void addIteration(Integer id, Integer edges_in_game, Long time,Long timeMaxFlow, Integer iteration, Double potential) {
		try {
			if (krv_iteration_insert == null) {
				krv_iteration_insert = c.prepareStatement("INSERT INTO krv_iterations(krv_run_id, time, time_max_flow, iteration, potential, edges_in_game, os) "
						+ "VALUES (?,?,?,?,?,?,?)");
			}

			krv_iteration_insert.setInt(1, id);
			krv_iteration_insert.setLong(2, time);
			krv_iteration_insert.setLong(3, timeMaxFlow);
			krv_iteration_insert.setInt(4, iteration);
			krv_iteration_insert.setDouble(5, potential);
			krv_iteration_insert.setInt(6, edges_in_game);
			krv_iteration_insert.setString(7, System.getProperty("os.name"));

			krv_iteration_insert.execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
			LOGGER.warning("Could not insert new iteration row! Message: " + e.getMessage());
		}
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
