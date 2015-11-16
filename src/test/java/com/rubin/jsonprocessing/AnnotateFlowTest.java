package com.rubin.jsonprocessing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AnnotateFlowTest {
	@Test
	public void annotate1() {
		Flow f = new Flow();
		
		new AnnotateFlowTask(f, "testcase", "value").run();
		
		assertEquals("value", (String)f.getAnnotation("testcase"));
	}
}
