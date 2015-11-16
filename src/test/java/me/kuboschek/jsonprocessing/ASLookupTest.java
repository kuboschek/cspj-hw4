package me.kuboschek.jsonprocessing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import me.kuboschek.jsonprocessing.ASInfo;
import me.kuboschek.jsonprocessing.ASLookup;

public class ASLookupTest {
	@Test
	public void decode1() throws Exception {
		ASLookup lookup = new ASLookup("87.67.0.0");
		ASInfo info = lookup.call();
		
		assertEquals(5432, info.number);
		assertEquals("BELGACOM-SKYNET-AS Proximus NV,BE", info.holder);
		assertEquals("87.0.0.0/8", info.block);
	}
	
	@Test
	public void decode2() throws Exception {
		ASLookup lookup = new ASLookup("87.23.0.0");
		ASInfo info = lookup.call();
		
		assertEquals(3269, info.number);
		assertEquals("ASN-IBSNAZ Telecom Italia S.p.a.,IT", info.holder);
		assertEquals("87.0.0.0/8", info.block);
	}
	
	@Test
	public void decode3() throws Exception {
		ASLookup lookup = new ASLookup("123.45.67.0");
		ASInfo info = lookup.call();
		
		assertEquals(6619, info.number);
		assertEquals("SAMSUNGSDS-AS-KR SamsungSDS Inc.,KR", info.holder);
		assertEquals("123.0.0.0/8", info.block);
	}
}
