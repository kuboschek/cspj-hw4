package me.kuboschek.jsonprocessing;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class ASLookup implements Callable<ASInfo> {
	private static Map<String, ASInfo> cache = new HashMap<>();
	private String queryIP;
	
	private static final String baseURL = "https://stat.ripe.net/data/prefix-overview/data.json";
	
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
		} else {
			cache.put(queryIP, ASInfo.NULL_INFO);
		}
		
		return info;
	}
}
