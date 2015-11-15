1. SOURCE FILES
The source files are: Flow.java FlowFilter.java FlowBin.java Quantile.java FlowTest.java FlowBinTest.java 
QuantileTest.java App.java 

Classes:
	Flow.java --- Class store the information of a network flow given in a json format
	FlowTest.java --- Class that provides unit test cases for the Flow class
	FlowBin.java --- Class that can aggregate the (pkt, oct, rpkt, rock) of several Flow objects
			in a given time interval
	FlowBinTest.java --- Class that provides unit test cases for the FlowBin class
	Quantile.java --- Class that provides statistical summaries of a list of numbers
	Quantile.java --- Class that provides unit test cases for the Quantile class
	App.java --- Class that uses all the other classes specified above to produce
			the required results

Interfaces:
	FlowFilter.java --- Interface that provides a single method boolean test(Flow)


2. OUTPUT
Output files: HTTP_HTTPS.dat QUICK_QUICKS.dat TCP.dat UDP.dat ICMPv6.dat OTHER.dat
	Every one of the above files contains statistical summaries for each of the filters
	that are specified in the program. If more filters would be specified we would have
	more files. (1 file/filter)

3. HOW TO COMPILE AND RUN THE FILES
The following command must be executed from the jsonProcessing directory.
Build project: mvn install
Run project: java -jar target/jsonProcessing-1.0.jar [files to be processed]
Clean the maven generated files: mvn clean
Clean the produced files: rm *.dat

4. RESULTS OF TEST
In the main directory you will find the 6 .dat files specified above

5. COMMENTS
The App class does not have a test case because it does not have a public method.
The only public method is the main method which returns no values.