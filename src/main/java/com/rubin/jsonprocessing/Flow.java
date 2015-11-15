package com.rubin.jsonprocessing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * @author rubin
 */
public class Flow {

    /* Class attributes */
    private String start, end;
    private double duration, rtt;
    private short proto;
    private String sip;
    private int sp;
    private String dip;
    private int dp;
    private String iflags, uflags, riflags, ruflags;
    private long isn, risn;
    private Byte tag, rtag;
    private long pkt;
    private long oct;
    private long rpkt;
    private long roct;

    // Conctructor, needed by gson, not used directly
    public Flow() {

    }
    
    // Copy Constructor
    public Flow(Flow f) {
        start = f.start;
        end = f.end;
        duration = f.duration;
        rtt = f.rtt;
        proto = f.proto;
        sip = f.sip;
        sp = f.sp;
        dip = f.dip;
        dp = f.dp;
        iflags = f.iflags;
        uflags = f.uflags;
        riflags = f.riflags;
        ruflags = f.ruflags;
        isn = f.isn;
        risn = f.risn;
        tag = f.tag;
        rtag = f.rtag;
        pkt = f.pkt;
        oct = f.oct;
        rpkt = f.rpkt;
        roct = f.roct;
    }
    
    // Remove a certain portion of the flow
    public void subtractPortion(double portion, Flow f) {
        pkt -= (long) (f.pkt * portion);
        oct -= (long) (f.oct * portion);
        rpkt -= (long) (f.rpkt * portion);
        roct -= (long) (f.roct * portion);
    }

    // Serializer
    public String toJson() {
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.toJson(this);
    }

    // Deserializer
    public static Flow fromJson(String str) {
        Gson gson = new Gson();
        return gson.fromJson(str, Flow.class);
    }

    /* Getters for attributes we will use */
    public long getOct() {
        return oct;
    }

    public long getPkt() {
        return pkt;
    }

    public long getRoct() {
        return roct;
    }

    public long getRpkt() {
        return rpkt;
    }

    public short getProto() {
        return proto;
    }

    public int getDp() {
        return dp;
    }

    public int getSp() {
        return sp;
    }
    
    public String getStart() {
        return start;
    }
    
    public double getDuration() {
        return duration;
    }
    
    public String getEnd() {
        return end;
    }
}
