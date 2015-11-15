package com.rubin.jsonprocessing;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author rubin
 */
public class FlowBinTest {

    static final String json1 = "{\"start\":\"2015-09-24 23:32:42.528\",\"end\":\"2015-09-24 23:33:42.528\",\"duration\":58.882,\"rtt\":0.008,\"proto\":6,\"sip\":\"2001:0638:0709::\",\"sp\":58779,\"dip\":\"2a00:1450:4005::\",\"dp\":443,\"iflags\":\"S\",\"uflags\":\"APF\",\"riflags\":\"AS\",\"ruflags\":\"APRF\",\"isn\":1840916291,\"risn\":1440908468,\"tag\":0,\"rtag\":0,\"pkt\":18,\"oct\":2111,\"rpkt\":31,\"roct\":21503}";
    static final String json2 = "{\"start\":\"2015-09-24 23:33:42.528\",\"end\":\"2015-09-24 23:33:42.528\",\"duration\":0.0,\"rtt\":0.0,\"proto\":6,\"sip\":\"2a00:1450:4005::\",\"sp\":443,\"dip\":\"2001:0638:0709::\",\"dp\":58779,\"iflags\":\"R\",\"uflags\":null,\"riflags\":null,\"ruflags\":null,\"isn\":1440928101,\"risn\":0,\"tag\":0,\"rtag\":null,\"pkt\":1,\"oct\":60,\"rpkt\":0,\"roct\":0}";
    static final String json3 = "{\"start\":\"2015-09-24 23:33:42.528\",\"end\":\"2015-09-24 23:33:42.528\",\"duration\":0.0,\"rtt\":0.0,\"proto\":6,\"sip\":\"2a00:1450:4005::\",\"sp\":443,\"dip\":\"2001:0638:0709::\",\"dp\":58779,\"iflags\":\"R\",\"uflags\":null,\"riflags\":null,\"ruflags\":null,\"isn\":1440928101,\"risn\":0,\"tag\":0,\"rtag\":null,\"pkt\":10,\"oct\":60,\"rpkt\":0,\"roct\":0}";
    
    @Test
    public void test1() {
        Flow flow1 = Flow.fromJson(json1);
        
        LocalDateTime flowStartTime = LocalDateTime.parse(flow1.getStart(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        long startTime = flowStartTime.toInstant(ZoneOffset.ofHours(0)).toEpochMilli();

        LocalDateTime flowEndTime = LocalDateTime.parse(flow1.getEnd(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        long endTime = flowEndTime.toInstant(ZoneOffset.ofHours(0)).toEpochMilli();
        
        FlowBin bin = new FlowBin(startTime, endTime);
        bin.addFlow(flow1);

        assertEquals(bin.getOcts(), 2111);
        assertEquals(bin.getPkts(), 18);
        assertEquals(bin.getRocts(), 21503);
        assertEquals(bin.getRpkts(), 31);
        
        Quantile octsQuantile = new Quantile();
        octsQuantile.addNumber(flow1.getOct());
        
        Quantile pktsQuantile = new Quantile();
        pktsQuantile.addNumber(flow1.getPkt());
        
        Quantile roctsQuantile = new Quantile();
        roctsQuantile.addNumber(flow1.getRoct());
        
        Quantile rpktsQuantile = new Quantile();
        rpktsQuantile.addNumber(flow1.getRpkt());
        
        assert equals(octsQuantile, bin.getOctsStats());
        assert equals(pktsQuantile, bin.getPktsStats());
        assert equals(roctsQuantile, bin.getRoctsStats());
        assert equals(rpktsQuantile, bin.getRpktsStats());
        
    }

    @Test
    public void test2() {
        Flow flow2 = Flow.fromJson(json2);
        
        LocalDateTime flowStartTime = LocalDateTime.parse(flow2.getStart(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        long startTime = flowStartTime.toInstant(ZoneOffset.ofHours(0)).toEpochMilli();

        LocalDateTime flowEndTime = LocalDateTime.parse(flow2.getEnd(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        long endTime = flowEndTime.toInstant(ZoneOffset.ofHours(0)).toEpochMilli();
        
        FlowBin bin = new FlowBin(startTime, endTime);
        bin.addFlowPortion(flow2, 0.5);
        
        assertEquals(bin.getOcts(), 30);
        assertEquals(bin.getPkts(), 0);
        assertEquals(bin.getRocts(), 0);
        assertEquals(bin.getRpkts(), 0);
        
        Quantile octsQuantile = new Quantile();
        octsQuantile.addNumber((long)(flow2.getOct() * 0.5));
        
        Quantile pktsQuantile = new Quantile();
        pktsQuantile.addNumber((long)(flow2.getPkt() * 0.5));
        
        Quantile roctsQuantile = new Quantile();
        roctsQuantile.addNumber((long)(flow2.getRoct() * 0.5));
        
        Quantile rpktsQuantile = new Quantile();
        rpktsQuantile.addNumber((long)(flow2.getRpkt() * 0.5));
        
        assert equals(octsQuantile, bin.getOctsStats());
        assert equals(pktsQuantile, bin.getPktsStats());
        assert equals(roctsQuantile, bin.getRoctsStats());
        assert equals(rpktsQuantile, bin.getRpktsStats());
    }

    @Test
    public void test3() {
        Flow flow3 = Flow.fromJson(json3);
        
        LocalDateTime flowStartTime = LocalDateTime.parse(flow3.getStart(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        long startTime = flowStartTime.toInstant(ZoneOffset.ofHours(0)).toEpochMilli();

        LocalDateTime flowEndTime = LocalDateTime.parse(flow3.getEnd(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        long endTime = flowEndTime.toInstant(ZoneOffset.ofHours(0)).toEpochMilli();
        
        FlowBin bin = new FlowBin(startTime, endTime);
        bin.addFlowPortion(flow3, 0.7);
        
        assertEquals(bin.getOcts(), 42);
        assertEquals(bin.getPkts(), 7);
        assertEquals(bin.getRocts(), 0);
        assertEquals(bin.getRpkts(), 0);
        
        Quantile octsQuantile = new Quantile();
        octsQuantile.addNumber((long)(flow3.getOct() * 0.7));
        
        Quantile pktsQuantile = new Quantile();
        pktsQuantile.addNumber((long)(flow3.getPkt() * 0.7));
        
        Quantile roctsQuantile = new Quantile();
        roctsQuantile.addNumber((long)(flow3.getRoct() * 0.7));
        
        Quantile rpktsQuantile = new Quantile();
        rpktsQuantile.addNumber((long)(flow3.getRpkt() * 0.7));
        
        assert equals(octsQuantile, bin.getOctsStats());
        assert equals(pktsQuantile, bin.getPktsStats());
        assert equals(roctsQuantile, bin.getRoctsStats());
        assert equals(rpktsQuantile, bin.getRpktsStats());
    }
    
    @Test
    public void test4() {
        Flow flow1 = Flow.fromJson(json1);
        Flow flow2 = Flow.fromJson(json2);
        Flow flow3 = Flow.fromJson(json3);
        
        LocalDateTime flowStartTime = LocalDateTime.parse(flow1.getStart(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        long startTime = flowStartTime.toInstant(ZoneOffset.ofHours(0)).toEpochMilli();

        LocalDateTime flowEndTime = LocalDateTime.parse(flow3.getEnd(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        long endTime = flowEndTime.toInstant(ZoneOffset.ofHours(0)).toEpochMilli();
        
        FlowBin bin = new FlowBin(startTime, endTime);
        bin.addFlow(flow1);
        bin.addFlowPortion(flow2, 0.5);
        bin.addFlowPortion(flow3, 0.7);
        
        assertEquals(bin.getOcts(), 2183);
        assertEquals(bin.getPkts(), 25);
        assertEquals(bin.getRocts(), 21503);
        assertEquals(bin.getRpkts(), 31);
        
        Quantile octsQuantile = new Quantile();
        octsQuantile.addNumber(flow1.getOct());
        octsQuantile.addNumber((long)(flow2.getOct() * 0.5));
        octsQuantile.addNumber((long)(flow3.getOct() * 0.7));
        
        Quantile pktsQuantile = new Quantile();
        pktsQuantile.addNumber(flow1.getPkt());
        pktsQuantile.addNumber((long)(flow2.getPkt() * 0.5));
        pktsQuantile.addNumber((long)(flow3.getPkt() * 0.7));
        
        Quantile roctsQuantile = new Quantile();
        roctsQuantile.addNumber(flow1.getRoct());
        roctsQuantile.addNumber((long)(flow2.getRoct() * 0.5));
        roctsQuantile.addNumber((long)(flow3.getRoct() * 0.7));
        
        Quantile rpktsQuantile = new Quantile();
        rpktsQuantile.addNumber(flow1.getRpkt());
        rpktsQuantile.addNumber((long)(flow2.getRpkt() * 0.5));
        rpktsQuantile.addNumber((long)(flow3.getRpkt() * 0.7));
        
        assert equals(octsQuantile, bin.getOctsStats());
        assert equals(pktsQuantile, bin.getPktsStats());
        assert equals(roctsQuantile, bin.getRoctsStats());
        assert equals(rpktsQuantile, bin.getRpktsStats());
        
    }
    
    private boolean equals(Quantile q1, Quantile q2) {
        boolean max = q1.getMax() == q2.getMax();
        boolean min = q1.getMin() == q2.getMin();
        boolean size = q1.getSize() == q2.getSize();
        boolean sum = q1.getSum() == q2.getSum();
        boolean median = q1.getMedian() == q2.getMedian();
        boolean firstQuantile = q1.getFirstQuantile() == q2.getFirstQuantile();
        boolean thirdQuantile = q1.getThirdQuantile() == q2.getThirdQuantile();
                
        return max && min && size && sum && median && firstQuantile && thirdQuantile;
    }
}
