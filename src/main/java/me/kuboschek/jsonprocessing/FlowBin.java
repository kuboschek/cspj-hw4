package me.kuboschek.jsonprocessing;

/**
 *
 * @author rubin
 */
public class FlowBin {

    private long pkts, octs, rpkts, rocts, flows;
    private long start, end;
    private Quantile pktsStats, octsStats, rpktsStats, roctsStats;

    /* Constructor */
    public FlowBin(long start, long end) {
        pkts = octs = rpkts = rocts = flows = 0;
        this.start = start;
        this.end = end;
        pktsStats = new Quantile();
        octsStats = new Quantile();
        rpktsStats = new Quantile();
        roctsStats = new Quantile();
    }

    /* Add properties of a flow to this object */
    public void addFlow(Flow f) {
        pkts += f.getPkt();
        pktsStats.addNumber(f.getPkt());
        octs += f.getOct();
        octsStats.addNumber(f.getOct());
        rpkts += f.getRpkt();
        rpktsStats.addNumber(f.getRpkt());
        rocts += f.getRoct();
        roctsStats.addNumber(f.getRoct());
        flows++;
    }
    
    public void addFlowPortion(Flow f, double portion) {
        long newPkts = (long)(f.getPkt() * portion);
        pkts += newPkts;
        pktsStats.addNumber(newPkts);
        
        long newOcts = (long)(f.getOct() * portion);
        octs += newOcts;
        octsStats.addNumber(newOcts);
        
        long newRpkts = (long)(f.getRpkt() * portion);
        rpkts += newRpkts;
        rpktsStats.addNumber(newRpkts);
        
        long newRocts = (long)(f.getRoct() * portion);
        rocts += newRocts;
        roctsStats.addNumber(newRocts);
        
        flows++;
    }

    /* Getters that might be relevant */
    public long getPkts() {
        return pkts;
    }

    public long getOcts() {
        return octs;
    }

    public long getRpkts() {
        return rpkts;
    }

    public long getRocts() {
        return rocts;
    }

    public long getFlows() {
        return flows;
    }
    
    public long getStart() {
        return start;
    }
    
    public long getEnd() {
        return end;
    }
    
    public Quantile getPktsStats() {
        return pktsStats;
    }
    
    public Quantile getOctsStats() {
        return octsStats;
    }
    
    public Quantile getRoctsStats() {
        return roctsStats;
    }
    
    public Quantile getRpktsStats() {
        return rpktsStats;
    }
}
