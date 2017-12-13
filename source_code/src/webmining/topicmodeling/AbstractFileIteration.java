package webmining.topicmodeling;

import webmining.utils.TimeStampUtils;

public abstract class AbstractFileIteration {

	TimeStampUtils timestamp = new TimeStampUtils(2016, 5, 24, 4, 22);
	int counterFiles = 0;

	int day = timestamp.day;
	int hour1 = timestamp.hour;
	int everyXHours = 2;
	int hour2 = hour1 + everyXHours;
        
	String pathOutput = "data//8topicModeling//";
        String path = "data//4filteredTweetsFromStreamPer" + everyXHours + "Hours//";
	String outPrefix = "inputFileForMallet";
        String extension = ".csv";

	public AbstractFileIteration() {

	}
	//use method run for each file
	public void runLoop() {

		while (counterFiles < 72 / everyXHours) {

			String filename = "" + timestamp.year + "_" + timestamp.month + "_" + day + "_" + hour1 + "-" + hour2;

			run(path, filename, extension, outPrefix, pathOutput);

			hour1 = (hour1 + everyXHours) % 24;
			hour2 = (hour2 + everyXHours) % 24;

			if (hour1 == 0) {
				day = (day + 1) % 31;
			}

			counterFiles++;
			System.out.println("files done: " + counterFiles);

		}
	}

	abstract void run(String path, String filename, String extension, String outPrefix, String pathOutput);
}
