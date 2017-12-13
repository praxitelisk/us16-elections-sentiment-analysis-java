package webmining.mongodatabase;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import webmining.utils.TimeStampUtils;

/*
 * A class to run in system's console mongoexport commands
 * This class creates csv files from mongo collection
 */
public class MongoDBBatchExportFilteredPerTimeWindow {

	/*
	 * 
	 */
	public static void main(String[] args) {
		MongoDBBatchExportFilteredPerTimeWindow obj = new MongoDBBatchExportFilteredPerTimeWindow();

		TimeStampUtils timestamp = new TimeStampUtils(2016, 5, 24, 4, 22);
		// while loop
		int counter = 0;
		long t1 = timestamp.initialDateInMills;
		long t2 = timestamp.addMinutedToDateInMillis(2*60);

		int year = 2016;
		int month = 5;
		int day = 24;
		int hour1 = 4;
		int hour2 = 6;

		while (counter < 72) {

			/*
			 * setup the query for mongoexport
			 */
			String query = "--query \"{timestampMs:{$gte:" + t1 + ",$lt:" + t2 + "}}\" ";
			String filename = "" + year + "_" + month + "_" + day + "_" + hour1 + "-" + hour2;
			
			/*
			 * prepare the console mongoexport command
			 */
			String command3 = "/usr/bin/mongoexport --host 127.0.0.1 --port 27017 --db dbTweetsForAnalysis --collection filteredTweetsTextOnlyVersionTwo "
					+ query + " --csv --fields _id,text --out 4filteredTweetsFromStreamPer2Hours/"+filename+".csv";

			
			System.out.println(command3);
			String output = obj.executeCommand(command3);

			System.out.println(output);

			t1 = t2;
			t2 = timestamp.addMinutedToDateInMillis(60*2);
			
			hour1 = (hour1 + 2) % 24;
			hour2 = (hour2 + 2) % 24;
			
			if (hour1==0) {
				day = (day + 1) % 31;
			}

			counter +=2;

		}
	}

	/*
	 * method to execute the system's console command
	 */
	private String executeCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}

}
