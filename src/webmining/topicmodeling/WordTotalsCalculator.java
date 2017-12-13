package webmining.topicmodeling;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import webmining.utils.RuntimeStatistics;
import webmining.utils.MapUtils;

public class WordTotalsCalculator {

	public final String BASE_PATH = "data//8topicModeling//res";
	public final String INPUT_CSV_FILENAME = BASE_PATH + ".txt";
	public final String OUTPUT_CSV_FILENAME_OF_FREQUENCIES = BASE_PATH + "_FrequenciesCalculated.csv";
	public final String OUTPUT_CSV_FILENAME_OF_JUST_OCCURENCES = BASE_PATH + "_OcccurencesCalculated.csv";

	private static Map<String, Integer> mapWithTotalFrequencies = new LinkedHashMap<>();
	private static Map<String, Integer> mapWithTotalOccurrences = new LinkedHashMap<>();


	//we calculate descending words based on frequency or occurence 
	public static ArrayList<Map<String, Integer>> exportCalculatedTotalsForTopicWords(String inputFileName,
			String outputFilenameOfFrequencies, String outputFilenameOfOccurences) {

                ArrayList<Map<String, Integer>> result = new ArrayList<Map<String, Integer>>();
		RuntimeStatistics.addNewCheckPointValues(); // 1
		
		//calculate frequency and occurence for every word in topics
		try (FileInputStream fileInputStream = new FileInputStream(inputFileName)) {

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream))) {

				RuntimeStatistics.addNewCheckPointValues(); // 2

				Stream<String> lines = reader.lines();

				lines
						// .parallel()
						.forEach((line) -> {

							int tabIndex = line.lastIndexOf("\t");

							line = line.substring(tabIndex + 1);

							String[] wordsAndFrequences = line.split(" ");

							// System.out.println(line);

							for (String token : wordsAndFrequences) {

								int indexOf = token.indexOf('-');

								String word = token.substring(0, indexOf);
								Integer frequency = Integer.parseInt(token.substring(indexOf + 1));

								if (!mapWithTotalFrequencies.containsKey(word)) {
									mapWithTotalFrequencies.put(word, frequency);
									mapWithTotalOccurrences.put(word, 1);
								} else {
									mapWithTotalFrequencies.put(word, mapWithTotalFrequencies.get(word) + frequency);
									mapWithTotalOccurrences.put(word, mapWithTotalOccurrences.get(word) + 1);
								}
							}
						});

				RuntimeStatistics.addNewCheckPointValues(); // 3

				reader.close();
			}
			//get descending ordered words
			mapWithTotalFrequencies = MapUtils.sortMapByValues(mapWithTotalFrequencies);
			mapWithTotalOccurrences = MapUtils.sortMapByValues(mapWithTotalOccurrences);

			MapUtils.writeMapToFile(mapWithTotalFrequencies, outputFilenameOfFrequencies);
			MapUtils.writeMapToFile(mapWithTotalOccurrences, outputFilenameOfOccurences);

		} catch (FileNotFoundException ex) {
			System.out.println(ex);
		} catch (IOException ex) {
			System.out.println(ex);
		}

		RuntimeStatistics.printValuesForAllCheckPoints();
		result.add(mapWithTotalFrequencies);
		result.add(mapWithTotalOccurrences);
		
                return result;
	}
}
