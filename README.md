## Source files
	Flow.java
	FlowFilter.java
	FlowBin.java
	Quantile.java
	FlowTest.java
	FlowBinTest.java
	QuantileTest.java
	App.java

## Classes
	Flow.java --- Class store the information of a network flow given in a json format
	FlowTest.java --- Class that provides unit test cases for the Flow class

	FlowBin.java --- Class that can aggregate the (pkt, oct, rpkt, rock) of
		several Flow objects in a given time interval
	FlowBinTest.java --- Class that provides unit test cases for the FlowBin class
	
	Quantile.java --- Class that provides statistical summaries of a list of numbers
	QuantileTest.java --- Class that provides unit test cases for the Quantile class

	ASInfo.java --- Holds information about an AS (number, holder name, IP block)
	ASLookup.java --- Callable that retrieves AS information from web service and
			caches it
	ASInfoTest.java --- Test for ASInfo
	ASLookupTest.java --- Test for ASLookup

	AnnotateFlowTask.java --- Generic Callable implementation to annotate flow
			with the result of another callable, or a set value
	AnnotateFlowTest.java --- Test for the class above

	App.java --- Class that uses all the other classes specified above to produce
			the required results


## Interfaces
	FlowFilter.java --- Interface that provides a single method boolean test(Flow)
	FlowGrouper.java --- Generates keys for grouping flows


## Output
Filter output files:
```
	HTTP_HTTPS.dat
	QUICK_QUICKS.dat
	TCP.dat
	UDP.dat
	ICMPv6.dat
	OTHER.dat
```

Every one of the above files contains statistical summaries for each of the filters
that are specified in the program. If more filters would be specified there would be
more files. (1 file/filter)

The application also outputs ```groups.dat```, which contains statistical summaries
of all flow groups that have been created from the input files.



## Build and run
1. ```mvn package```
2. There are two options:
	* Copy ```run.sh``` to ```/target```, make sure the ```data-json``` folder is
	present there and filled with JSON flow record traces. ```run.sh``` will run
	the application over all of the files in the folder.
	* Manually run it over one file with
	```java -jar target/jsonProcessing-1.0.jar [files to be processed]```

Clean the maven generated files: mvn clean
Clean the produced files: rm *.dat

The app will generate files as specified above.

## Notes
* The App class does not have a test case because it does not have a public method.
  The only public method is the main method which returns no values.
* The ASLookupTest performs a real lookup. Therefore it requires internet
connectivity.
