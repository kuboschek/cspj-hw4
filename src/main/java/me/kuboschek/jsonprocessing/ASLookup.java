package me.kuboschek.jsonprocessing;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;

public class ASLookup implements Callable<ASInfo> {
	private static Connection dbConn = null;
	
	private String queryIP;
	
	private static final String baseURL = "https://stat.ripe.net/data/prefix-overview/data.json";
	private static final String dbName = "ripe.db";
	
	/**
	 * Sets the IP to be looked up when task is run.
	 * @param ip The address to look up
	 */
	public ASLookup(String ip) {
		this.queryIP = ip;
		
		if(dbConn == null) {
			try {
				Class.forName("org.sqlite.JDBC");
				initDatabase();
			} catch (Exception e) {
		    	System.err.println("Could not connect to database");
		    	System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		        System.exit(0);
			}
		}
	}

	public static boolean isCached(String queryIP) {
		return getCache(queryIP) == null;
	}

	public static ASInfo getCache(String queryIP) {
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
			stmt = conn.createStatement();
			String sql = "SELECT * FROM 'as' R WHERE 'ip' = '" + queryIP + "';";
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Only get results if there are rows in the set
		try {
			if(rs != null && rs.next()) {
				// Set the number in constructor, to distinguish NULL_INFO
				ASInfo info = new ASInfo(rs.getLong("asn"));
				
				info.block = rs.getString("prefix");
				info.holder = rs.getString("holder");
				
				return info;
			}
		} catch(SQLException ex) {
			// TODO Auto catch block
			ex.printStackTrace();
		}
		
		return null;
	}

	private void initDatabase() throws SQLException {	
		// Open database connection
        dbConn = DriverManager.getConnection("jdbc:sqlite:" + dbName);

	    Statement stmt = null;
		
		// Create table if not existant
	    stmt = dbConn.createStatement();
	    String sql =  "CREATE TABLE IF NOT EXISTS 'as' (ip VARCHAR(42),"
					+ "prefix VARCHAR(46) not NULL,"
					+ "asn INTEGER not NULL,"
					+ "holder VARCHAR(255))";
	    stmt.executeUpdate(sql);
	    stmt.close();
	}
	
	private void storeInfo(String queryIP, ASInfo info) throws SQLException {
		Statement stmt = dbConn.createStatement();
		String sql = "INSERT INTO 'as' VALUES " +
					 String.format("('%s', '%s', %d, '%s');", queryIP, info.block, info.number, info.holder);
				
		stmt.executeUpdate(sql);
	}
	
	@Override
	public ASInfo call() throws Exception {
		
		// Don't run request when record is already available
		ASInfo info = getCache(queryIP);
		
		// Special NULL_INFO exists to also cache IPs without any AS.
		if(info != null) {
			return info.equals(ASInfo.NULL_INFO) ? null : info;
		}
		
		URL url = new URL(baseURL + "?resource=" + queryIP);
		URLConnection conn = url.openConnection();
		
		BufferedReader in = new BufferedReader(
								new InputStreamReader(conn.getInputStream()));
		
		StringBuffer response = new StringBuffer();
		String s;

		// Read all lines of the response into a string
		while((s = in.readLine()) != null)
			response.append(s);
			
		in.close();
		
		// Eat up the exception here, this means that flows without an AS number do not get annotated, since null is returned
		try {
			info = ASInfo.deserialize(response.toString());
		} catch (Exception e) {
		}
		
		// Cache the result even if there is no AS
		if(info != null) {
			storeInfo(queryIP, info);
		} else {
			storeInfo(queryIP, ASInfo.NULL_INFO);
		}
		
		return info;
	}
}
