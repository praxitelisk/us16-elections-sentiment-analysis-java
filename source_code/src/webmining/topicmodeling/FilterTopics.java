package webmining.topicmodeling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FilterTopics {

        private final String BASE_PATH = "data//";    
        private String FILE_OUTPUT = BASE_PATH + "8topicModeling//resultTopics.txt";
	
        private int perHour = 2;
	private final int numOfPeriods = 72 / perHour;
	
	//we use this method to filter topic with a threshold and export results to a file
	public TopicModelAsList run(boolean exportFile,double thresholdWeight) {

		FileReader in;
		Topic tempTopic;
		TopicModelAsList topicModel = new TopicModelAsList(numOfPeriods);

		try {
			in = new FileReader("res.txt");
			BufferedReader br = new BufferedReader(in);
			String line;
			int timePeriod = -1;// We want the first timeTimeperiod=0;

			while ((line = br.readLine()) != null) {
				// delete first line ="_ix text"(header)
				String[] columns = line.split("\t");

				if (Integer.parseInt(columns[0]) == 0) {
					timePeriod++;
				}
				if (Double.parseDouble(columns[1]) >= thresholdWeight) {
					tempTopic = new Topic(timePeriod, Double.parseDouble(columns[1]));
					String[] tokensAndFrequencies = columns[2].split(" ");

					for (String token : tokensAndFrequencies) {
						String[] wordFreq = token.split("-");
						tempTopic.setWord(wordFreq[0], Integer.parseInt(wordFreq[1]));
					}

					topicModel.setTopic(timePeriod, tempTopic);
				}

			}
			//if we dont want to exportFile return object 
			if(!exportFile){
				return topicModel;
			}
			//Loop topicModel to export results to a file
			//every line has "numOfperiod word1-frequency word2-frequency etc" 
			StringBuilder out;
			for (int i = 0; i < numOfPeriods; i++) {
				List<Topic> topics = topicModel.getTopics(i);

				for (Topic topic : topics) {
					out = new StringBuilder();
					out.append(i + " ");

					for (String word : topic.getWords()) {
						out.append(word + " ");
					}

					try {
						if (new File(FILE_OUTPUT).exists()) {
							Files.write(Paths.get(FILE_OUTPUT), ("\n" + out.toString()).getBytes(),
									StandardOpenOption.APPEND);
						} else {
							Files.write(Paths.get(FILE_OUTPUT), out.toString().getBytes(), StandardOpenOption.CREATE);
						}
					} catch (Exception e) {
						// TODO: handle exception
					}

				}
			}
			System.out.println("filter is done");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return topicModel;
	}

}
