package mf.gui;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.transform.TransformerConfigurationException;

import mf.superpixel.SuperpixelGraph;
import mf.superpixel.SuperpixelImport;

import org.xml.sax.SAXException;

import javafx.scene.image.Image;

public class Test {

	public static void main(String[] args) throws ClassNotFoundException {
	    Class.forName("org.sqlite.JDBC");

		Connection connection = null;
	    try
	    {
	      // create a database connection
	      connection = DriverManager.getConnection("jdbc:sqlite:db/stats.db");
	      Statement statement = connection.createStatement();
	      statement.setQueryTimeout(30);  // set timeout to 30 sec.

	      statement.executeUpdate("create table person (id integer, name string) if not exists person");

	      statement.executeUpdate("insert into person values(1, 'leo')");
	      statement.executeUpdate("insert into person values(2, 'yui')");
	      ResultSet rs = statement.executeQuery("select * from person");
	      while(rs.next())
	      {
	        // read the result set
	        System.out.println("name = " + rs.getString("name"));
	        System.out.println("id = " + rs.getInt("id"));
	      }
	    }
	    catch(SQLException e) {
	      // if the error message is "out of memory", 
	      // it probably means no database file is found
	      System.err.println(e.getMessage());
	    }
	    finally
	    {
	      try
	      {
	        if(connection != null)
	          connection.close();
	      }
	      catch(SQLException e)
	      {
	        // connection close failed.
	        System.err.println(e);
	      }
	    }

	}
}
