package webmining.topicmodeling;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreateElementsForWebSite {

        private final String BASE_PATH = "data//8topicModeling//res";    
	private final String INPUT_CSV_FILENAME = BASE_PATH + ".txt";
	private final String OUTPUT_CSV_FILENAME_OF_FREQUENCIES = BASE_PATH + "_FrequenciesCalculated.csv";
	private final String OUTPUT_CSV_FILENAME_OF_JUST_OCCURENCES = BASE_PATH + "_OcccurencesCalculated.csv";
	private final int topNWords = 6;
	public static final int FREQUENCY = 0;
	public static final int OCCURENCE = 1;

	int day = 24;
	int month = 5;
	int hour1 = 4;
	int everyXHours = 2;
	int hour2 = hour1 + everyXHours;
	StringBuilder strbOut;

	// print only in console.
	public void createTableCode(double weightFilter) {

		// take filterTopics
		FilterTopics filterMalletResults = new FilterTopics();
		TopicModelAsList topicModel = filterMalletResults.run(false, 0.3);
		int numOfPeriods = topicModel.getNumberOfTimePeriods();
		int numberOfWords = topicModel.getTopics(0).get(0).getSize();

		// start print table for web (javascript) with google charts
		System.out.println("function drawTable() {var data = new google.visualization.DataTable();");
		System.out.println("data.addColumn('string', 'Period');");
		System.out.println("data.addColumn('number', 'Topic');");

		for (int i = 0; i < numberOfWords; i++) {
			System.out.println("data.addColumn('string', 'Word-" + (i + 1) + "');");
		}

		int counterNumOfTopic = 0;
		System.out.println("data.addRows([");
		for (int i = 0; i < numOfPeriods; i++) {
			List<Topic> topics = topicModel.getTopics(i);

			for (Topic topic : topics) {
				counterNumOfTopic++;
				strbOut = new StringBuilder();
				strbOut.append("[");
				strbOut.append("'" + day + "/" + month + " " + hour1 + "-" + hour2 + "',");// period
				strbOut.append("{v: " + counterNumOfTopic + "},");// number of
																	// topic
				for (String word : topic.getWords()) {
					strbOut.append("'" + word + "',");// append each word of
														// topic
				}
				strbOut.deleteCharAt(strbOut.length() - 1).append("],");// delete
																		// last
																		// ","
				System.out.println(strbOut.toString());
			}

			// we use hours for column period in table
			hour1 = (hour1 + everyXHours) % 24;
			hour2 = (hour2 + everyXHours) % 24;

			if (hour1 == 0) {
				day = (day + 1) % 31;
			}
		}
		System.out
				.println("]);\n" + "var table = new google.visualization.Table(document.getElementById('table_div'));\n"
						+ "table.draw(data, {showRowNumber: false, width: '100%', height: '100%'}); }\n");
		System.out.println("filter is done");

	}

	// create values for graph in website
	public void createGraphCode(double weightFilter, int freqOrOccur) {
		// create list that contains (sum frequencies) per period for n top
		// words
		ArrayList<LinkedHashMap<String, Integer>> periodsList = new ArrayList<LinkedHashMap<String, Integer>>();
		// create sorted descending by frequency linedHashMap for all words in
		// topics
		ArrayList<Map<String, Integer>> listFrequeceAndOccurance = WordTotalsCalculator
				.exportCalculatedTotalsForTopicWords(INPUT_CSV_FILENAME, OUTPUT_CSV_FILENAME_OF_FREQUENCIES,
						OUTPUT_CSV_FILENAME_OF_JUST_OCCURENCES);
		LinkedHashMap<String, Integer> sortedListAllWords;
		if (freqOrOccur == FREQUENCY) {
			sortedListAllWords = (LinkedHashMap<String, Integer>) listFrequeceAndOccurance.get(0);
		} else {
			sortedListAllWords = (LinkedHashMap<String, Integer>) listFrequeceAndOccurance.get(1);
		}

		// create a new LinkedHashMap with N best Words
		LinkedHashMap<String, Integer> bestNWords = new LinkedHashMap<>();

		// fill bestNwords with best first N key-value
		int counterTopN = 0;
		for (String keyWord : sortedListAllWords.keySet()) {
			if (counterTopN == topNWords) {
				break;
			}
			bestNWords.put(keyWord, 0);
			counterTopN++;
		}

		// get map word-frequency for each topic
		FilterTopics filterMalletResults = new FilterTopics();

		TopicModelAsList topicModel = filterMalletResults.run(false, weightFilter);
		int numOfPeriods = topicModel.getNumberOfTimePeriods();

		StringBuilder out;

		//find words frequency for each period(sum topics for each period)
		int tempFreq = 0;
		for (int i = 0; i < numOfPeriods; i++) {

			LinkedHashMap<String, Integer> tempBestTenWords = new LinkedHashMap<>();
			tempBestTenWords.putAll(bestNWords);
			List<Topic> topics = topicModel.getTopics(i);

			for (Topic topic : topics) {
				// find sum frequency for each of the best n words for every
				// period
				for (String keyword : topic.getWords()) {
					tempFreq = 0;
					if (tempBestTenWords.containsKey(keyword)) {
						tempFreq = tempBestTenWords.get(keyword) + topic.getFrequency(keyword);
						tempBestTenWords.put(keyword, tempFreq);
					}
				}

			}
			periodsList.add(tempBestTenWords);

		}
		//print code for creating graph on website	
		out = new StringBuilder();
		out.append("['24/05/2016 � 27/05/2016',");
		// print final results
		for (String word : periodsList.get(0).keySet()) {
			out.append(" '" + word + "',");
		}
		out.deleteCharAt(out.length() - 1).append("],");

		System.out.println(out);
		for (LinkedHashMap<String, Integer> linkedHashMap : periodsList) {
			out = new StringBuilder();
			out.append("['" + hour1 + ":00-" + hour2 + ":00'");
			for (String word : linkedHashMap.keySet()) {
				out.append(", " + linkedHashMap.get(word));

			}
			out.append("],");

			hour1 = (hour1 + everyXHours) % 24;
			hour2 = (hour2 + everyXHours) % 24;
			System.out.println(out);
		}

	}

}
