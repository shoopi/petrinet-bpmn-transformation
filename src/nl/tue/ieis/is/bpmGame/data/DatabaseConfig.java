package nl.tue.ieis.is.bpmGame.data;

import java.sql.*;

import nl.tue.ieis.is.bpmGame.activiti.ActivitiConfig;

public class DatabaseConfig {
	
	public static Connection getConnection()
	  {
	    Connection conn = null;
	    try {
	      Class.forName(ActivitiConfig.DriverClassName);
	      conn = DriverManager.getConnection(ActivitiConfig.DBSchema, ActivitiConfig.DBUsername, ActivitiConfig.DBPassword);
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    return conn;
	  }
}