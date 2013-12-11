package org.jgrapht.experimental.clustering.stats;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Test {

	public static void main(String[] args) {

		try {
	      Class.forName("org.sqlite.JDBC");

	      // create a database connection
	      Connection c = DriverManager.getConnection("jdbc:sqlite:db/stats.db");
	      Statement statement = c.createStatement();
	      statement.setQueryTimeout(30);  // set timeout to 30 sec
	      
	      statement.executeUpdate("INSERT INTO krv_runs (edges , nodes, timestamp) VALUES (100,1000, CURRENT_TIMESTAMP)");
	      
	      
	      ResultSet rs = statement.executeQuery("SELECT * FROM 'krv_runs'");

	      while (rs.next()) {
	    	  System.out.print(rs.getInt("edges"));
	    	  System.out.print(" - ");
	    	  System.out.print(rs.getInt("nodes"));
	    	  System.out.print(" - ");
	    	  System.out.print(rs.getLong("time"));
	    	  System.out.print(" - ");
	    	  System.out.print(rs.getString("os"));
	    	  System.out.print(" - ");
	    	  System.out.println(rs.getString("timestamp"));
	      }
	      
	      rs = statement.executeQuery("SELECT * FROM krv_iterations");
	      System.out.println("*************");
	      
	      while (rs.next()) {
	    	  System.out.print(rs.getInt("krv_run_id"));
	    	  System.out.print(" - ");
	    	  System.out.print(rs.getLong("time"));
	    	  System.out.print(" - ");
	    	  System.out.print(rs.getLong("time_max_flow"));
	    	  System.out.print(" - ");
	    	  System.out.print(rs.getDouble("potential"));
	    	  System.out.print(" - ");
	    	  System.out.print(rs.getDouble("edges_in_game"));
	    	  System.out.print(" - ");
	    	  System.out.print(rs.getString("os"));
	    	  System.out.print(" - ");
	    	  System.out.println(rs.getString("timestamp"));
	      }
	      
	      c.close();
	    } catch(SQLException e) {
	    	e.printStackTrace();
	    } catch(ClassNotFoundException e) {
	    	e.printStackTrace();
	    }
	}
}
