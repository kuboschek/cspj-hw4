package com.rubin.jsonprocessing;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ASInfo {
	int number;
	String holder;
	String block;
	
	/**
	 * 
	 * @param json The API response as a string
	 * @return An ASInfo object corresponding to the data object
	 */
	public static ASInfo deserialize(String json) {
		ASInfo info = new ASInfo();
		JsonParser parser = new JsonParser();
		
		JsonObject resp = (JsonObject)parser.parse(json);
		JsonObject data = resp.getAsJsonObject("data");
		
		
		JsonObject holder = data.getAsJsonArray("asns").get(0).getAsJsonObject();
		JsonObject block = data.getAsJsonObject("block");
		
		info.holder = holder.get("holder").getAsString();
		info.number = holder.get("asn").getAsInt();
		info.block = block.get("resource").getAsString();
		
		return info;
	}
}
