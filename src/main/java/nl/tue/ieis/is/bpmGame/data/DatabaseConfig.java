package main.java.nl.tue.ieis.is.bpmGame.data;

import java.sql.*;

public class DatabaseConfig {
    public final static String driverClassName = "org.h2.Driver";
	public final static String shayaDB = "jdbc:h2:~/shaya";
	
	public static Connection getConnection()
	  {
	    Connection conn = null;
	    try {
	      Class.forName(driverClassName);
	      conn = DriverManager.getConnection(shayaDB, "sa", "bpmgame");
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    return conn;
	  }
}