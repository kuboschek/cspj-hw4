package com.rubin.jsonprocessing;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

/**
 *
 * @author rubin
 */
public class FlowTest {

    static final String json1 = "{\"start\":\"2015-09-24 23:32:43.646\",\"end\":\"2015-09-24 23:33:42.528\",\"duration\":58.882,\"rtt\":0.008,\"proto\":6,\"sip\":\"2001:0638:0709::\",\"sp\":58779,\"dip\":\"2a00:1450:4005::\",\"dp\":443,\"iflags\":\"S\",\"uflags\":\"APF\",\"riflags\":\"AS\",\"ruflags\":\"APRF\",\"isn\":1840916291,\"risn\":1440908468,\"tag\":0,\"rtag\":0,\"pkt\":18,\"oct\":2111,\"rpkt\":31,\"roct\":21503}";
    static final String json2 = "{\"start\":\"2015-09-24 23:33:42.528\",\"end\":\"2015-09-24 23:33:42.528\",\"duration\":0.0,\"rtt\":0.0,\"proto\":6,\"sip\":\"2a00:1450:4005::\",\"sp\":443,\"dip\":\"2001:0638:0709::\",\"dp\":58779,\"iflags\":\"R\",\"uflags\":null,\"riflags\":null,\"ruflags\":null,\"isn\":1440928101,\"risn\":0,\"tag\":0,\"rtag\":null,\"pkt\":1,\"oct\":60,\"rpkt\":0,\"roct\":0}";
    static final String json3 = "{\"start\":\"2015-09-24 23:33:42.528\",\"end\":\"2015-09-24 23:33:42.528\",\"duration\":0.0,\"rtt\":0.0,\"proto\":6,\"sip\":\"2a00:1450:4005::\",\"sp\":443,\"dip\":\"2001:0638:0709::\",\"dp\":58779,\"iflags\":\"R\",\"uflags\":null,\"riflags\":null,\"ruflags\":null,\"isn\":1440928101,\"risn\":0,\"tag\":0,\"rtag\":null,\"pkt\":1,\"oct\":60,\"rpkt\":0,\"roct\":0}";
    static final String json4 = "{\"start\":\"2015-09-24 23:32:43.079\",\"end\":\"2015-09-24 23:33:42.529\",\"duration\":59.45,\"rtt\":0.006,\"proto\":6,\"sip\":\"2001:0638:0709::\",\"sp\":58777,\"dip\":\"2a00:1450:4005::\",\"dp\":443,\"iflags\":\"S\",\"uflags\":\"APF\",\"riflags\":\"AS\",\"ruflags\":\"APRF\",\"isn\":3699078575,\"risn\":4263649564,\"tag\":0,\"rtag\":0,\"pkt\":24,\"oct\":2807,\"rpkt\":42,\"roct\":25185}";
    static final String json5 = "{\"start\":\"2015-09-24 23:33:42.529\",\"end\":\"2015-09-24 23:33:42.529\",\"duration\":0.0,\"rtt\":0.0,\"proto\":6,\"sip\":\"2a00:1450:4005::\",\"sp\":443,\"dip\":\"2001:0638:0709::\",\"dp\":58777,\"iflags\":\"R\",\"uflags\":null,\"riflags\":null,\"ruflags\":null,\"isn\":4263672219,\"risn\":0,\"tag\":0,\"rtag\":null,\"pkt\":1,\"oct\":60,\"rpkt\":0,\"roct\":0}";

    @Test
    public void deserialize1() {
        Flow flow = Flow.fromJson(json1);

        assertEquals(json1, flow.toJson());

        assertEquals(flow.getDp(), 443);
        assertEquals(flow.getProto(), 6);
        assertEquals(flow.getOct(), 2111);
        assertEquals(flow.getPkt(), 18);
        assertEquals(flow.getRoct(), 21503);
        assertEquals(flow.getRpkt(), 31);
    }

    @Test
    public void deserialize2() {
        Flow flow = Flow.fromJson(json2);

        assertEquals(json2, flow.toJson());

        assertEquals(flow.getDp(), 58779);
        assertEquals(flow.getProto(), 6);
        assertEquals(flow.getOct(), 60);
        assertEquals(flow.getPkt(), 1);
        assertEquals(flow.getRoct(), 0);
        assertEquals(flow.getRpkt(), 0);
    }

    @Test
    public void deserialize3() {
        Flow flow = Flow.fromJson(json3);

        assertEquals(json3, flow.toJson());

        assertEquals(flow.getDp(), 58779);
        assertEquals(flow.getProto(), 6);
        assertEquals(flow.getOct(), 60);
        assertEquals(flow.getPkt(), 1);
        assertEquals(flow.getRoct(), 0);
        assertEquals(flow.getRpkt(), 0);
    }

    @Test
    public void deserialize4() {
        Flow flow = Flow.fromJson(json4);

        assertEquals(json4, flow.toJson());

        assertEquals(flow.getDp(), 443);
        assertEquals(flow.getProto(), 6);
        assertEquals(flow.getOct(), 2807);
        assertEquals(flow.getPkt(), 24);
        assertEquals(flow.getRoct(), 25185);
        assertEquals(flow.getRpkt(), 42);
    }

    @Test
    public void deserialize5() {
        Flow flow = Flow.fromJson(json5);

        assertEquals(json5, flow.toJson());

        assertEquals(flow.getDp(), 58777);
        assertEquals(flow.getProto(), 6);
        assertEquals(flow.getOct(), 60);
        assertEquals(flow.getPkt(), 1);
        assertEquals(flow.getRoct(), 0);
        assertEquals(flow.getRpkt(), 0);
    }
    
    @Test
    public void annotate1() {
    	Flow flow = new Flow();
    	int i = new Random().nextInt();
    	
    	flow.addAnnotation("testcase", i);
    	
    	assertEquals(i, flow.getAnnotation("testcase"));
    }
}
