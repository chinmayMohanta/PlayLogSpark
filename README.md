# PlayLogSpark
 Log (structured) analysis


# IMPORTANT ASSUMTION::  
For the purpose demonstration, the application is configured to run in the standalone (single node) mode. In other words all the reads/writes are from/to the local file system. But it can easily be cofigured for any distributed file system such as HDFS.

# Directory structure:

1. In the spark bin directory create the subdirectory "test". Inside test create three sub directories "app","input" and "result".
	<>The app directory is for application jar. Please put the  pop_test.jar in this directory.
	<>The input directory is for the input files, please put raw_pop.json and campaign.csv in this directory.
	<>The result directory is for result files. The enrich_pop.json and aggregate_pop.json files will be generated in this location.

# Running the program:
Please lauch the program using spark-submit:

 spark-submit --class test.PlayLogTest test/app/pop-test.jar  YYYY-MM-DD

Example: spark-submit --class test.PlayLogTest test/app/pop-test.jar 2017-03-29
	It will process only the logs after 2017-03-29 (inclusive) and genereate enrich_pop.json and aggregate_pop.json


Addition information:
1. There are optimization scope in terms of the usage of partitionedMaps, worker thread, JSON DeSer usage etc to be explored in the clustered setup.




