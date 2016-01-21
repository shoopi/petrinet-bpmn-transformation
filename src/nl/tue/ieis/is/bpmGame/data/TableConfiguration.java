package nl.tue.ieis.is.bpmGame.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class TableConfiguration {

	static Connection conn = DatabaseConfig.getConnection();
	
	public void createTableDeploymentFilenameUser() {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			try {
				stmt.executeUpdate("DROP TABLE DEPLOYMENT_FILENAME_USER");
			} catch(java.sql.SQLException ex) {System.out.println(ex.getMessage());}
			String sql = "CREATE TABLE DEPLOYMENT_FILENAME_USER (DEPLOYMENT_ID VARCHAR(50) NOT NULL, FILENAME VARCHAR(50), USERID VARCHAR(50) NOT NULL, PRIMARY KEY (DEPLOYMENT_ID))";
			stmt.executeUpdate(sql);
			stmt.close();
			System.out.println("DEPLOYMENT_FILENAME_USER HAS BEEN CREATED.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void addDeploymentFilenameUser(String deploymentId, String filename, String userid) {
		try {
			String sql = "INSERT INTO DEPLOYMENT_FILENAME_USER(DEPLOYMENT_ID, FILENAME, USERID) VALUES (?,?,?)"; 
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, deploymentId);
			ps.setString(2, filename);
			ps.setString(3, userid);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void removeDeploymentFilenameUser(String deploymentId) {
		try {
			String sql = "DELETE FROM DEPLOYMENT_FILENAME_USER WHERE DEPLOYMENT_ID = ?"; 
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, deploymentId);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Map<String, String> loadAllDeploymentFileForUser (String userId) {
		String sql = "SELECT DEPLOYMENT_ID, FILENAME FROM DEPLOYMENT_FILENAME_USER WHERE USERID = ?";
		Map<String, String> deployment2File = new HashMap<String, String>();
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()) {
		    	  String depId = rs.getString("DEPLOYMENT_ID");
		    	  String filename = rs.getString("FILENAME");
		    	  deployment2File.put(depId, filename);
			}
			ps.close();			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return deployment2File;
	}
	
	public boolean isSameFileName (String userId, String fileName) {
		String sql = "SELECT FILENAME FROM DEPLOYMENT_FILENAME_USER WHERE USERID = ? AND FILENAME = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, fileName);
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) {
		    	 return true;
			}
			ps.close();			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String laodSingleDeploymentId(String userId, String fileName) {
		String sql = "SELECT DEPLOYMENT_ID FROM DEPLOYMENT_FILENAME_USER WHERE USERID = ? AND FILENAME = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, fileName);
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) {
		    	 return rs.getString("DEPLOYMENT_ID");
			}
			ps.close();			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
