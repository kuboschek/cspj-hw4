package me.kuboschek.jsonprocessing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author rubin
 */
public class App {

    // DURATION of a FlowBin in milliseconds
    // I use milliseconds for more precision on the Flow distribution
    // If the length is specified in seconds just multiply by 1000
    private static final long DURATION = 59000;

    public static void main(String[] args) throws IOException {
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

        // Put filters in a Map with the key being the filter name(String)
        Map<String, FlowFilter> filters = new LinkedHashMap<>();
        addFilters(filters);

        // For each filter create a map from groups to a map of timed flow bins
        Map<String, Map<String, TreeMap<Long, FlowBin>>> binObjects = new LinkedHashMap<>();
        // Create a TreeMap<Long, Flowbin> for every filter
        createBinTreeMap(filters, binObjects);

        // Load stored AS infos from file database into memory cache
        try {
    		Class.forName("org.sqlite.JDBC");
			ASLookup.populateCache();
		} catch (SQLException | ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        // Create a thread pool for annotating objects in parallel
        ExecutorService svc = Executors.newFixedThreadPool(15);

        System.out.println(String.format("Processing %d file(s)", args.length));


        
        // This flow grouper is arbitrary in implementation
        FlowGrouper aspair  = new FlowGrouper() {
            @Override
            public String getKey(Flow f) {
                try {
                    ASInfo srcas = (ASInfo) f.getAnnotation("src-as");
                    ASInfo dstas = (ASInfo) f.getAnnotation("dst-as");

                    if(srcas == null || dstas == null) {
                        return "none";
                    }

                    return String.format("%s-%s", srcas != null ? srcas.number : "?", dstas != null ? dstas.number : "?");
                } catch (ClassCastException ex) {
                    return "none";
                }
            }
            
            @Override
            public String getName() {
            	return "as";
            }
        };
        
        
        for (String arg : args) {
            // Persist flows here, to group them after annotation
            List<Flow> flows = new ArrayList<>();

            JsonReader reader = null;
            try {
            	int num_flows = 0;
                int tasks_submitted = 0;

                reader = new JsonReader(new FileReader(arg));
                // We ecpect to have a list of json records
                // Otherwise ecxeption will be thrown
                reader.beginArray();

                while (reader.hasNext()) {
                	num_flows++;
                	
                    Flow flow = gson.fromJson(reader, Flow.class);

                    // Before anything else, annotate this flow with AS numbers.
                    // This is async, thus we wait for completion of all threads after this loop
                    svc.execute(new AnnotateFlowTask(flow, "src-as", new ASLookup(flow.getSip())));
                    svc.execute(new AnnotateFlowTask(flow, "dst-as", new ASLookup(flow.getDip())));

                    tasks_submitted += 2;

                    // Add to list for grouping later
                    flows.add(flow);
                }
                reader.endArray();
                
                
                System.out.println(String.format("%d flows queued for annotation", num_flows));
                
                
                int old_value = 0;
                
                // Wait for annotation to be completed
                while(!svc.isShutdown()) {
                	try {
						if(svc.awaitTermination(1, TimeUnit.SECONDS))
							break;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
 
                	ThreadPoolExecutor tpe = (ThreadPoolExecutor) svc;
                	int done = tasks_submitted - tpe.getQueue().size();
                	
                	if(tasks_submitted - done == 0) {
                		break;
                	}
                	
                	// Keep user informed
                	System.out.print(String.format("\r%10d of %d\t%5d annotations/s", done, tasks_submitted, done - old_value));
                	
                	old_value = done;
                }
                

                
                // Run the flow pipeline oll all the flows in this file, which are now annotated.
                for (Flow flow : flows) {
                    // Get the start time of a flow, parse it and convert it 
                    // into milliseconds for more precision
                    LocalDateTime flowStartTime = LocalDateTime.parse(flow.getStart(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                    long startTime = flowStartTime.toInstant(ZoneOffset.ofHours(0)).toEpochMilli();
                    
                    LocalDateTime flowEndTime = LocalDateTime.parse(flow.getEnd(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                    long endTime = flowEndTime.toInstant(ZoneOffset.ofHours(0)).toEpochMilli();
                    
                    // We calculate the duration of a flow which would be the same
                    // as converting flow.getDuration()*1000 into a double.
                    // It is more intuitive this way and we just lose a couple of
                    // nanoseconds in precision which we would lose anyway in rounding
                    long duration = endTime - startTime;

                    // Apply filters and add, modify the corresponding TreeMap
                    for (Map.Entry<String, FlowFilter> stringFilter : filters.entrySet()) {
                        // Apply filter
                        if (stringFilter.getValue().test(flow)) {
                            Map<String, TreeMap<Long, FlowBin>> groups = binObjects.get(stringFilter.getKey());
                            
                            // Get the right map for the group
                            TreeMap<Long, FlowBin> treeMap = groups.get(aspair.getKey(flow));
                            
                            // Create and store one if it doesn't exist
                            if(treeMap == null) {
                            	treeMap = new TreeMap<>();
                            	groups.put(aspair.getKey(flow), treeMap);
                            }
                            
                            // Get the first entry that matches the start time of 
                            // the flow
                            Entry<Long, FlowBin> longBin = treeMap.floorEntry(startTime);

                            // Add bins downwards(includes case of empty TreeMap
                            while (longBin == null) {

                                Map.Entry<Long, FlowBin> firstEntry = treeMap.firstEntry();
                                if (firstEntry == null) {
                                    treeMap.put(startTime, new FlowBin(startTime, startTime + DURATION));
                                } else {
                                    // Decrease the start time of the next bin
                                    long newStart = firstEntry.getKey() - DURATION;
                                    treeMap.put(newStart, new FlowBin(newStart, newStart + DURATION));
                                }

                                // Keep checking until the time requirement is met
                                longBin = treeMap.floorEntry(startTime);
                            }
                            
                            // Add bins upwards until the endTime requirement is met
                            while (longBin.getValue().getEnd() < endTime) {
                                long newBeg = longBin.getValue().getEnd();
                                // If bin does not already exist add it
                                if(treeMap.floorEntry(newBeg) != null)
                                    treeMap.put(newBeg, new FlowBin(newBeg, newBeg + DURATION));
                                longBin = treeMap.floorEntry(newBeg);
                            }

                            //Start from correct entry
                            longBin = treeMap.floorEntry(startTime);
                            
                            // The proportional division of the flow
                            Flow remainingFlow = new Flow(flow);
                            while (longBin.getValue().getEnd() < endTime) {
                                double portion = (double) (longBin.getValue().getEnd() - startTime) / duration;
                                
                                // Add portion to correct FlowBin
                                longBin.getValue().addFlowPortion(flow, portion);
                                // Subtract the part of the flow that you added to
                                // The bin and get the next bin
                                remainingFlow.subtractPortion(portion, flow);
                                startTime = longBin.getValue().getEnd();
                                longBin = treeMap.floorEntry(startTime);
                            }
                            
                            // If the flow had no initial duration just add it 
                            // to the current bin, else add the remainder to the 
                            // bin
                            if (duration == 0) {
                                longBin.getValue().addFlow(flow);
                            } else {
                                longBin.getValue().addFlow(remainingFlow);
                            }
                        }
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace(System.out);
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace(System.out);
            } finally {
                if (reader != null) {
                    reader.close();
                }
                System.out.println("File: " + arg + " done!");
                System.out.println();
            }
        }
        
        for(Map.Entry<String, Map<String, TreeMap<Long, FlowBin>>> filterMap : binObjects.entrySet()) {
			try {
				System.out.println("Writing records for " + filterMap.getKey() + " to database.");
				writeDatabaseTable(filterMap, filterMap.getKey() + "_flows_by_" + aspair.getName());
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
        }
        
        
        System.exit(0);
    }

    private static void addFilters(Map<String, FlowFilter> filters) {
        // UDP filter
        filters.put("UDP", new FlowFilter() {
            @Override
            public boolean test(Flow f) {
                return f.getProto() == 17;
            }
        });

        // ICMPv6 filter
        filters.put("ICMPv6", new FlowFilter() {
            @Override
            public boolean test(Flow f) {
                return f.getProto() == 58;

            }
        });

        // OTHER filter
        filters.put("OTHER", new FlowFilter() {
            @Override
            public boolean test(Flow f) {
                return f.getProto() != 6 && f.getProto() != 17 && f.getProto() != 58;
            }
        });

        // TCP filter
        filters.put("TCP", new FlowFilter() {
            @Override
            public boolean test(Flow f) {
                return f.getProto() == 6;
            }
        });

        // HTTP filter
        filters.put("HTTP_HTTPS", new FlowFilter() {
            @Override
            public boolean test(Flow f) {
                return (f.getProto() == 6 && (f.getDp() == 80 || f.getDp() == 443
                        || f.getSp() == 80 || f.getSp() == 443));
            }
        });

        // QUIC filter
        filters.put("QUIC_QUICS", new FlowFilter() {
            @Override
            public boolean test(Flow f) {
                return (f.getProto() == 17 && (f.getDp() == 80 || f.getDp() == 443
                        || f.getSp() == 80 || f.getSp() == 443));
            }
        });
    }

    // Pre-populate the map with entries for all filters. Entries for groups are created when needed
    private static void createBinTreeMap(Map<String, FlowFilter> filters,
    		Map<String, Map<String, TreeMap<Long, FlowBin>>> aggregateObjects) {
        for (Map.Entry<String, FlowFilter> entry : filters.entrySet()) {
            Map<String, TreeMap<Long, FlowBin>> treeMap = new TreeMap<>();
            aggregateObjects.put(entry.getKey(), treeMap);
        }
    }

    private static void writeDatabaseTable(Map.Entry<String, Map<String, TreeMap<Long, FlowBin>>> filterGroups, String tableName) throws ClassNotFoundException, SQLException {    	
    	// Open database connection
		Class.forName("org.sqlite.JDBC");
        Connection dbConn = DriverManager.getConnection("jdbc:sqlite:flows.db");
        
		// Create table if not existant
	    Statement stmt = dbConn.createStatement();
	    String sql =  	"CREATE TABLE IF NOT EXISTS " + tableName + "(time INTEGER not NULL,"
					+	"\"group\" VARCHAR(255),"
					+	"type VARCHAR(4) not NULL,"
					+	"min INTEGER not NULL,"
					+	"q1 INTEGER not NULL,"
					+	"median INTEGER not NULL,"
					+	"q3 INTEGER not NULL,"
					+	"max INTEGER not NULL,"
					+	"size INTEGER not NULL,"
					+	"sum INTEGER not NULL);";
	    stmt.executeUpdate(sql);
    	
		// Prepare statement for inserting lots of things
    	PreparedStatement insert = null;
        insert = dbConn.prepareStatement("INSERT INTO '" + tableName + "' values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

        // Go through all groups, writing them to the same file
        for(Map.Entry<String, TreeMap<Long, FlowBin>> entry : filterGroups.getValue().entrySet()) {
        	String groupName = entry.getKey();
        	
            TreeMap<Long, FlowBin> treeMap = entry.getValue();
            for (Long seconds : treeMap.navigableKeySet()) {
            	
                FlowBin bin = treeMap.get(seconds);
                // Getting quantile objects of the corresponding FlowBin
                Quantile octs = bin.getOctsStats();
                Quantile pkts = bin.getPktsStats();
                Quantile rocts = bin.getRoctsStats();
                Quantile rpkts = bin.getRpktsStats();
                
                
                // Insert into the table
                insert.setLong(1, seconds / 1000);
                insert.setString(2, groupName);
                
                
                // Octets
                insert.setString(3, "octs");
                insert.setLong(4, octs.getMin());
                insert.setLong(5, octs.getFirstQuantile());
                insert.setLong(6, octs.getMedian());
                insert.setLong(7, octs.getThirdQuantile());
                insert.setLong(8, octs.getMax());
                insert.setLong(9, octs.getSize());
                insert.setLong(10, octs.getSum());
                
                insert.executeUpdate();
                
                
                // Octets
                insert.setString(3, "pkts");
                insert.setLong(4, pkts.getMin());
                insert.setLong(5, pkts.getFirstQuantile());
                insert.setLong(6, pkts.getMedian());
                insert.setLong(7, pkts.getThirdQuantile());
                insert.setLong(8, pkts.getMax());
                insert.setLong(9, pkts.getSize());
                insert.setLong(10, pkts.getSum());
                
                insert.executeUpdate();
                
                
                // Octets
                insert.setString(3, "rocts");
                insert.setLong(4, rocts.getMin());
                insert.setLong(5, rocts.getFirstQuantile());
                insert.setLong(6, rocts.getMedian());
                insert.setLong(7, rocts.getThirdQuantile());
                insert.setLong(8, rocts.getMax());
                insert.setLong(9, rocts.getSize());
                insert.setLong(10, rocts.getSum());
                
                insert.executeUpdate();
                
                
                // Octets
                insert.setString(3, "rpkts");
                insert.setLong(4, rpkts.getMin());
                insert.setLong(5, rpkts.getFirstQuantile());
                insert.setLong(6, rpkts.getMedian());
                insert.setLong(7, rpkts.getThirdQuantile());
                insert.setLong(8, rpkts.getMax());
                insert.setLong(9, rpkts.getSize());
                insert.setLong(10, rpkts.getSum());
                
                insert.executeUpdate();

            }
        }
    }
    
    
    // Just prints the results aggregated into the FlowBins according to the
    // specified format
    private static void writeToFile(Map<String, Map<String, TreeMap<Long, FlowBin>>> binObjects, String groupingName) {
        for (Map.Entry<String, Map<String, TreeMap<Long, FlowBin>>> filterGroups : binObjects.entrySet()) {
            PrintWriter writer = null;
            try {
                File file = new File(filterGroups.getKey() + "_by_" + groupingName + ".dat");
                if (!file.exists()) {
                    file.createNewFile();
                }

                writer = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
                // The header of every file
                writer.printf("#%-9s\t%-10s\t%-10s\t%-10s\t%-10s\t%-10s\t%-10s\t%-10s\t%-10s\t%-10s\n", "time", "group", "type", "min", "q1", "median", "q2", "max", "size", "sum");
                
                // Go through all groups, writing them to the same file
                for(Map.Entry<String, TreeMap<Long, FlowBin>> entry : filterGroups.getValue().entrySet()) {
                	String groupName = entry.getKey();
                	
	                TreeMap<Long, FlowBin> treeMap = entry.getValue();
	                for (Long seconds : treeMap.navigableKeySet()) {
	                	
	                    FlowBin bin = treeMap.get(seconds);
	                    // Getting quantile objects of the corresponding FlowBin
	                    Quantile octs = bin.getOctsStats();
	                    Quantile pkts = bin.getPktsStats();
	                    Quantile rocts = bin.getRoctsStats();
	                    Quantile rpkts = bin.getRpktsStats();
	                    // Printing the formated output for every Quantile object of
	                    // the FlowBin
	                    writer.printf("%-10d\t%-10s\t%-10s\t%-10d\t%-10d\t%-10d\t%-10d\t%-10d\t%-10d\t%-20d\n", 
	                            seconds / 1000, groupName, "octs", octs.getMin(), octs.getFirstQuantile(), octs.getMedian()
	                            , octs.getThirdQuantile(), octs.getMax(), octs.getSize(), octs.getSum());
	                    writer.printf("%-10d\t%-10s\t%-10s\t%-10d\t%-10d\t%-10d\t%-10d\t%-10d\t%-10d\t%-20d\n", 
	                            seconds / 1000, groupName, "pkts", pkts.getMin(), pkts.getFirstQuantile(), pkts.getMedian(), 
	                            pkts.getThirdQuantile(), pkts.getMax(), pkts.getSize(), pkts.getSum());
	                    writer.printf("%-10d\t%-10s\t%-10s\t%-10d\t%-10d\t%-10d\t%-10d\t%-10d\t%-10d\t%-20d\n", 
	                            seconds / 1000, groupName, "rocts", rocts.getMin(), rocts.getFirstQuantile(), rocts.getMedian(), 
	                            rocts.getThirdQuantile(), rocts.getMax(), rocts.getSize(), rocts.getSum());
	                    writer.printf("%-10d\t%-10s\t%-10s\t%-10d\t%-10d\t%-10d\t%-10d\t%-10d\t%-10d\t%-20d\n", 
	                            seconds / 1000, groupName, "rpkts", rpkts.getMin(), rpkts.getFirstQuantile(), rpkts.getMedian(), 
	                            rpkts.getThirdQuantile(), rpkts.getMax(), rpkts.getSize(), rpkts.getSum());
	                }
	                
	                writer.write("\n");
                
                }
            } catch (IOException e) {
                e.printStackTrace(System.out);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }
}
