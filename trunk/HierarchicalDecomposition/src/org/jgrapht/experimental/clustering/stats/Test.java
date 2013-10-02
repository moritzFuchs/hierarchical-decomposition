package org.jgrapht.experimental.clustering.stats;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Test {

	public static void main(String[] args) {
		
		KRVStats stats = KRVStats.getInstance();
		
		Integer id = stats.registerKRVRun(13, 37);
		System.out.println("id " + id);
		stats.updateKRVRun(id, 300L);
		
		stats.addIteration(id, 20, 40L,20L, 1, 100.0);
		stats.addIteration(id, 15, 30L,15L, 2, 50.0);
		stats.addIteration(id, 14, 20L,10L, 3, 40.0);
		stats.addIteration(id, 6, 10L,5L, 4, 25.0);
		
		stats.close();
		
		try {
	      Class.forName("org.sqlite.JDBC");

	      // create a database connection
	      Connection c = DriverManager.getConnection("jdbc:sqlite:db/stats.db");
	      Statement statement = c.createStatement();
	      statement.setQueryTimeout(30);  // set timeout to 30 sec
	      
	      
	      ResultSet rs = statement.executeQuery("SELECT * FROM krv_runs");

	      while (rs.next()) {
	    	  System.out.println(rs.getInt("edges"));
	    	  System.out.println(rs.getInt("nodes"));
	    	  System.out.println(rs.getLong("time"));
	      }
	      
	      rs = statement.executeQuery("SELECT * FROM krv_iterations");
	      System.out.println("*************");
	      
	      while (rs.next()) {
	    	  System.out.println(rs.getInt("krv_run_id"));
	    	  System.out.println(rs.getLong("time"));
	    	  System.out.println(rs.getDouble("potential"));
	    	  System.out.println(rs.getDouble("edges_in_game"));
	      }
	      
	      c.close();
	    } catch(SQLException e) {
	    	e.printStackTrace();
	    } catch(ClassNotFoundException e) {
	    	e.printStackTrace();
	    }
	}
}
