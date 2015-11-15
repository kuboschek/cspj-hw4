package com.rubin.jsonprocessing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ASInfoTest {
	static final String json1 = "{\"status\": \"ok\", \"server_id\": \"stat-app3\", \"status_code\": 200, \"version\": \"1.3\", \"cached\": false, \"see_also\": [], \"time\": \"2015-11-15T13:19:57.603087\", \"messages\": [[\"warning\", \"Given resource is not announced but result has been aligned to first-level less-specific (87.64.0.0/14).\"]], \"data_call_status\": \"supported - connecting to ursa\", \"process_time\": 465, \"query_id\": \"90deb39c-8b9b-11e5-a2bc-d4ae52a9c1b6\", \"data\": {\"query_time\": \"2015-11-15T08:00:00\", \"is_less_specific\": true, \"resource\": \"87.64.0.0/14\", \"actual_num_related\": 0, \"num_filtered_out\": 0, \"asns\": [{\"holder\": \"BELGACOM-SKYNET-AS Proximus NV,BE\", \"asn\": 5432}], \"block\": {\"resource\": \"87.0.0.0/8\", \"name\": \"IANA IPv4 Address Space Registry\", \"desc\": \"RIPE NCC (Status: ALLOCATED)\"}, \"related_prefixes\": [], \"type\": \"prefix\", \"announced\": true}}";
	static final String json2 = "{\"status\": \"ok\", \"server_id\": \"stat-app7\", \"status_code\": 200, \"version\": \"1.3\", \"cached\": false, \"see_also\": [], \"time\": \"2015-11-15T13:47:55.752477\", \"messages\": [[\"warning\", \"Given resource is not announced but result has been aligned to first-level less-specific (87.23.0.0/17).\"]], \"data_call_status\": \"supported - connecting to ursa\", \"process_time\": 377, \"query_id\": \"792d5d6c-8b9f-11e5-8458-f8bc1247f998\", \"data\": {\"query_time\": \"2015-11-15T08:00:00\", \"is_less_specific\": true, \"resource\": \"87.23.0.0/17\", \"actual_num_related\": 0, \"num_filtered_out\": 0, \"asns\": [{\"holder\": \"ASN-IBSNAZ Telecom Italia S.p.a.,IT\", \"asn\": 3269}], \"block\": {\"resource\": \"87.0.0.0/8\", \"name\": \"IANA IPv4 Address Space Registry\", \"desc\": \"RIPE NCC (Status: ALLOCATED)\"}, \"related_prefixes\": [], \"type\": \"prefix\", \"announced\": true}}";
	static final String json3 = "{\"status\": \"ok\", \"server_id\": \"stat-app7\", \"status_code\": 200, \"version\": \"1.3\", \"cached\": false, \"see_also\": [], \"time\": \"2015-11-15T13:52:11.330880\", \"messages\": [[\"warning\", \"Given resource is not announced but result has been aligned to first-level less-specific (123.32.0.0/12).\"]], \"data_call_status\": \"supported - connecting to ursa\", \"process_time\": 461, \"query_id\": \"1176b884-8ba0-11e5-aa49-f8bc1247f998\", \"data\": {\"query_time\": \"2015-11-15T08:00:00\", \"is_less_specific\": true, \"resource\": \"123.32.0.0/12\", \"actual_num_related\": 0, \"num_filtered_out\": 0, \"asns\": [{\"holder\": \"SAMSUNGSDS-AS-KR SamsungSDS Inc.,KR\", \"asn\": 6619}], \"block\": {\"resource\": \"123.0.0.0/8\", \"name\": \"IANA IPv4 Address Space Registry\", \"desc\": \"APNIC (Status: ALLOCATED)\"}, \"related_prefixes\": [], \"type\": \"prefix\", \"announced\": true}}";
	
	@Test
	public void decode1() {
		ASInfo info = ASInfo.deserialize(json1);
		
		assertEquals(5432, info.number);
		assertEquals("BELGACOM-SKYNET-AS Proximus NV,BE", info.holder);
		assertEquals("87.0.0.0/8", info.block);
	}
	
	@Test
	public void decode2() {
		ASInfo info = ASInfo.deserialize(json2);
		
		assertEquals(3269, info.number);
		assertEquals("ASN-IBSNAZ Telecom Italia S.p.a.,IT", info.holder);
		assertEquals("87.0.0.0/8", info.block);
	}
	
	@Test
	public void decode3() {
		ASInfo info = ASInfo.deserialize(json3);
		
		assertEquals(6619, info.number);
		assertEquals("SAMSUNGSDS-AS-KR SamsungSDS Inc.,KR", info.holder);
		assertEquals("123.0.0.0/8", info.block);
	}
}
