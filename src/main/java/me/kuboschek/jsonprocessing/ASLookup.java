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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class ASLookup implements Callable<ASInfo> {
	/**
	 * While I'd like to solely rely on a database for caching, this approach was not fast enough.
	 * Therefore, a hybrid approach is used. In-memory storage of the cache is used for reads,
	 * while writes are carried through to the database as well. The in-memory cache is populated
	 * from the database on startup.
	 */
	private static Map<String, ASInfo> cache = new HashMap<>();
	private String queryIP;
	
	private static final String baseURL = "https://stat.ripe.net/data/prefix-overview/data.json";
	private static final String dbName = "ripe.db";
	
	/**
	 * Sets the IP to be looked up when task is run.
	 * @param ip The address to look up
	 */
	public ASLookup(String ip) {
		this.queryIP = ip;
	}

	public static boolean isCached(String queryIP) {
		return cache.containsKey(queryIP);
	}

	public static ASInfo getCache(String queryIP) {
		return cache.get(queryIP);
	}
	
	public static void initDatabase() throws SQLException, ClassNotFoundException {
		// Open database connection
		Class.forName("org.sqlite.JDBC");
        Connection dbConn = DriverManager.getConnection("jdbc:sqlite:" + dbName);

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
	
	public static void populateCache() throws SQLException, ClassNotFoundException {
		initDatabase();
		
		Connection db = DriverManager.getConnection("jdbc:sqlite:" + dbName);
		Statement stmt = null;
		
		
		stmt = db.createStatement();
		// TODO Save cache to db file
		String sql = "SELECT * FROM 'as';";
		
		ResultSet rs = stmt.executeQuery(sql);
		
		int count = 0;
		
		if(rs != null) {
			// Fill the cache row-by-row
			while(rs.next()) {
				ASInfo info = new ASInfo(rs.getLong("asn"));
				
				info.block = rs.getString("prefix");
				info.holder = rs.getString("holder");
				
				cache.put(rs.getString("ip"), info);
				count++;
			}
		} else {
			System.err.println("No AS info loaded from database.");
		}
		
		System.out.println(String.format("%d cached AS infos loaded from file.", count));
		
		stmt.close();
		db.close();
	}
	
	public static void saveCache(String queryIP, ASInfo info) throws SQLException, ClassNotFoundException{
		initDatabase();
		
		Connection db = DriverManager.getConnection("jdbc:sqlite:" + dbName);
		Statement stmt = null;
		
		
		stmt = db.createStatement();
		// TODO Save cache to db file
		String sql = "INSERT INTO 'as' VALUES " +
		String.format("('%s', '%s', %d, '%s');", queryIP, info.block, info.number, info.holder);
			
		stmt.executeUpdate(sql);
		stmt.close();
		
		db.close();
	}

	@Override
	public ASInfo call() throws Exception {
		
		// Don't run request when record is already available
		ASInfo info = cache.get(queryIP);
		
			// Special NULL_INFO exists to allow usage of concurrent map for cache.
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
			cache.put(queryIP, info);
			saveCache(queryIP, info);
		} else {
			cache.put(queryIP, ASInfo.NULL_INFO);
			saveCache(queryIP, ASInfo.NULL_INFO);
		}
		
		return info;
	}
}
