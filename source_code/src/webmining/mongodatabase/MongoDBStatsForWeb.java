package webmining.mongodatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import webmining.utils.TimeStampUtils;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

/*
 * An auxillary class to produce prints from mongo
 * to easily transfer to javascript = html files
 */
public class MongoDBStatsForWeb {

	/*
	 * JS print tweets per hour
	 */
	public void printTweetsPerHour() {

		@SuppressWarnings("deprecation")
		Mongo mongo = new Mongo("localhost", 27017);
		@SuppressWarnings("deprecation")
		DB db = mongo.getDB("dbTweetsForAnalysis");
		DBCollection filteredCollection = db.getCollection("filteredTweetsForAnalysisCollection");
		System.out.println("Link Mongodb!");

		TimeStampUtils timestamp = new TimeStampUtils(2016, 5, 24, 4, 22);
		// while loop
		int counter = 0;
		long t1 = timestamp.initialDateInMills;
		long t2 = timestamp.addMinutedToDateInMillis(60);

		int year = 2016;
		int month = 5;
		int day = 24;
		int hour1 = 4;
		int hour2 = 5;

		while (counter < 72) {

			BasicDBObject query = new BasicDBObject("timestampMs", new BasicDBObject("$gte", t1).append("$lt", t2));

			DBCursor cursor = filteredCollection.find(query);

			printForJavascriptChartTemplate(cursor, hour1, hour2);

			t1 = t2;
			t2 = timestamp.addMinutedToDateInMillis(60);

			hour1 = (hour1 + 1) % 24;
			hour2 = (hour2 + 1) % 24;

			if (hour1 == 0) {
				day = (day + 1) % 31;
			}

			counter++;
		}

	}

	/*
	 * Print the total number of references to candidates for JS google charts
	 */
	public void printTotalReferencesToCandidates(String str1, String str2, String str3) {

		@SuppressWarnings("deprecation")
		Mongo mongo = new Mongo("localhost", 27017);
		@SuppressWarnings("deprecation")
		DB db = mongo.getDB("dbTweetsForAnalysis");
		DBCollection filteredCollection = db.getCollection("filteredTweetsForAnalysisCollection");
		System.out.println("Link Mongodb!");

		ArrayList<Integer> mentionTrumpArray = new ArrayList<Integer>();
		ArrayList<Integer> hashtagTrumpArray = new ArrayList<Integer>();
		ArrayList<Integer> totalTrumpArray = new ArrayList<Integer>();

		ArrayList<Integer> mentionHillaryArray = new ArrayList<Integer>();
		ArrayList<Integer> hashtagHillaryArray = new ArrayList<Integer>();
		ArrayList<Integer> totalHillaryArray = new ArrayList<Integer>();

		ArrayList<Integer> mentionSandersArray = new ArrayList<Integer>();
		ArrayList<Integer> hashtagSandersArray = new ArrayList<Integer>();
		ArrayList<Integer> totalSandersArray = new ArrayList<Integer>();

		int counter = 0;
		TimeStampUtils timestamp = new TimeStampUtils(2016, 5, 24, 4, 22);
		long t1 = timestamp.initialDateInMills;
		long t2 = timestamp.addMinutedToDateInMillis(60);

		int year = 2016;
		int month = 5;
		int day = 24;
		int hour1 = 4;
		int hour2 = 5;

		while (counter < 72) {

			BasicDBObject query = new BasicDBObject("mentions.mention", str1).append("timestampMs",
					new BasicDBObject("$gte", t1).append("$lt", t2));

			BasicDBObject query2 = new BasicDBObject("hashtags.hashtag", str1).append("timestampMs",
					new BasicDBObject("$gte", t1).append("$lt", t2));

			// -------------------------------------------------

			BasicDBObject query3 = new BasicDBObject("mentions.mention", str2).append("timestampMs",
					new BasicDBObject("$gte", t1).append("$lt", t2));

			BasicDBObject query4 = new BasicDBObject("hashtags.hashtag", str2).append("timestampMs",
					new BasicDBObject("$gte", t1).append("$lt", t2));

			// -------------------------------------------------

			BasicDBObject query5 = new BasicDBObject("mentions.mention", str3).append("timestampMs",
					new BasicDBObject("$gte", t1).append("$lt", t2));

			BasicDBObject query6 = new BasicDBObject("hashtags.hashtag", str3).append("timestampMs",
					new BasicDBObject("$gte", t1).append("$lt", t2));

			DBCursor cursor = filteredCollection.find(query);
			mentionTrumpArray.add(cursor.count());

			cursor = filteredCollection.find(query2);
			hashtagTrumpArray.add(cursor.count());

			// --------------------------------------------------------------------

			cursor = filteredCollection.find(query3);
			mentionHillaryArray.add(cursor.count());

			cursor = filteredCollection.find(query4);
			hashtagHillaryArray.add(cursor.count());

			// --------------------------------------------------------------------

			cursor = filteredCollection.find(query5);
			mentionSandersArray.add(cursor.count());

			cursor = filteredCollection.find(query6);
			hashtagSandersArray.add(cursor.count());

			t1 = t2;
			t2 = timestamp.addMinutedToDateInMillis(60);

			hour1 = (hour1 + 1) % 24;
			hour2 = (hour2 + 1) % 24;

			if (hour1 == 0) {
				day = (day + 1) % 31;
			}

			counter++;

		} // end of while

		year = 2016;
		month = 5;
		day = 24;
		hour1 = 4;
		hour2 = 5;
		System.out.println("Total references to " + str1 + " " + str2 + " " + str3);
		for (int i = 0; i < 72; i++) {

			totalTrumpArray.add(mentionTrumpArray.get(i) + hashtagTrumpArray.get(i));
			totalHillaryArray.add(mentionHillaryArray.get(i) + hashtagHillaryArray.get(i));
			totalSandersArray.add(mentionSandersArray.get(i) + hashtagSandersArray.get(i));

			System.out.println("['" + hour1 + ":00-" + hour2 + ":00', " + totalTrumpArray.get(i) + ", "
					+ totalHillaryArray.get(i) + ", " + totalSandersArray.get(i) + "],");

			hour1 = (hour1 + 1) % 24;
			hour2 = (hour2 + 1) % 24;

			if (hour1 == 0) {
				day = (day + 1) % 31;
			}
		}

		mongo.close();

	}

	/*
	 * prepare a print for JS google charts for correlations between 2 strings
	 */
	public void printCorrelations(String str1, String str2) {

		@SuppressWarnings("deprecation")
		Mongo mongo = new Mongo("localhost", 27017);
		@SuppressWarnings("deprecation")
		DB db = mongo.getDB("dbTweetsForAnalysis");
		DBCollection filteredCollection = db.getCollection("filteredTweetsForAnalysis");
		System.out.println("Link Mongodb!");

		ArrayList<Integer> mentionArray1 = new ArrayList<Integer>();
		ArrayList<Integer> hashtagArray1 = new ArrayList<Integer>();
		ArrayList<Integer> totalArray1 = new ArrayList<Integer>();

		ArrayList<Integer> mentionArray2 = new ArrayList<Integer>();
		ArrayList<Integer> hashtagArray2 = new ArrayList<Integer>();
		ArrayList<Integer> totalArray2 = new ArrayList<Integer>();

		int counter = 0;
		TimeStampUtils timestamp = new TimeStampUtils(2016, 5, 24, 4, 22);
		long t1 = timestamp.initialDateInMills;
		long t2 = timestamp.addMinutedToDateInMillis(60);
		int year = 2016;
		int month = 5;
		int day = 24;
		int hour1 = 4;
		int hour2 = 5;

		while (counter < 72) {

			BasicDBObject query = new BasicDBObject("mentions.mention", str1).append("timestampMs",
					new BasicDBObject("$gte", t1).append("$lt", t2));

			BasicDBObject query2 = new BasicDBObject("hashtags.hashtag", str1).append("timestampMs",
					new BasicDBObject("$gte", t1).append("$lt", t2));

			// -------------------------------------------------

			BasicDBObject query3 = new BasicDBObject("mentions.mention", str2).append("timestampMs",
					new BasicDBObject("$gte", t1).append("$lt", t2));

			BasicDBObject query4 = new BasicDBObject("hashtags.hashtag", str2).append("timestampMs",
					new BasicDBObject("$gte", t1).append("$lt", t2));

			DBCursor cursor = filteredCollection.find(query);

			mentionArray1.add(cursor.count());

			cursor = filteredCollection.find(query2);
			hashtagArray1.add(cursor.count());

			// --------------------------------------------------------------------

			cursor = filteredCollection.find(query3);
			mentionArray2.add(cursor.count());

			cursor = filteredCollection.find(query4);
			hashtagArray2.add(cursor.count());

			// --------------------------------------------------------------------

			t1 = t2;
			t2 = timestamp.addMinutedToDateInMillis(60);

			hour1 = (hour1 + 1) % 24;
			hour2 = (hour2 + 1) % 24;

			if (hour1 == 0) {
				day = (day + 1) % 31;
			}

			counter++;

		} // end of while

		year = 2016;
		month = 5;
		day = 24;
		hour1 = 4;
		hour2 = 5;
		System.out.println("Correlations between " + str1 + " VS " + str2);
		for (int i = 0; i < 72; i++) {

			totalArray1.add(mentionArray1.get(i) + hashtagArray1.get(i));
			totalArray2.add(mentionArray2.get(i) + hashtagArray2.get(i));

			System.out.println("[" + totalArray1.get(i) + ", " + totalArray2.get(i) + "],");

			hour1 = (hour1 + 1) % 24;
			hour2 = (hour2 + 1) % 24;

			if (hour1 == 0) {
				day = (day + 1) % 31;
			}
		}

		mongo.close();

	}

	/*
	 * prepare a print for the JS-bar charts for all the entities
	 */
	@SuppressWarnings({ "deprecation", "rawtypes" })
	public void printEntities(String entities, String entity) throws JSONException {

		Mongo mongo = new Mongo("localhost", 27017);
		DB db = mongo.getDB("dbTweetsForAnalysis");
		DBCollection filteredCollection = db.getCollection("filteredTweetsForAnalysis");
		System.out.println("Link Mongodb!");
		
		HashMap<String, Integer> personsHashMap = new HashMap<String, Integer>();

		DBCursor cursor = filteredCollection.find();

		while (cursor.hasNext()) {

			String str = cursor.next().toString();
			JSONObject json = new JSONObject(str);
			JSONArray personJsonObject = json.getJSONArray(entities);
			if (!personJsonObject.isNull(0)) {
				for (int i = 0; i < personJsonObject.length(); i++) {
					JSONObject mention = personJsonObject.getJSONObject(i);
					String word = mention.getString(entity).toLowerCase();

					/*
					 * when we process urls, at first preprocess it to remove
					 * certain areas, such as http:// https:// and clean it as
					 * possible
					 */

					// word = processUrl(word);

					Integer count = personsHashMap.get(word);
					if (count == null) {
						personsHashMap.put(word, 1);
					} else {
						personsHashMap.put(word, count + 1);
					}
				}
			}

		} // end of while
		boolean ASC = true;
		boolean DESC = false;

		Map<String, Integer> sortedHashMapPersonAsc = sortByComparator(personsHashMap, DESC);
		// Get a set of the entries
		Set set = sortedHashMapPersonAsc.entrySet();
		// Get an iterator
		Iterator i = set.iterator();
		// Display elements
		while (i.hasNext()) {
			Map.Entry me = (Map.Entry) i.next();
			System.out.println("[\"" + me.getKey() + "\", " + me.getValue() + ", \"silver\"],");
		}
		System.out.println();

		mongo.close();
	}

	/*
	 * An auxillary method to order a hashmap in Ascending or descending order
	 */
	private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, boolean order) {

		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				if (order) {
					return o1.getValue().compareTo(o2.getValue());
				} else {
					return o2.getValue().compareTo(o1.getValue());

				}
			}
		});

		// Maintaining insertion order with the help of LinkedList
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Entry<String, Integer> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	@SuppressWarnings("unused")
	private String processUrl(String word) {

		if (word.startsWith("http") || word.startsWith("https") || word.startsWith("www")) {
			word = word.replaceAll("https://", "");
			word = word.replaceAll("http://", "");
			int t = word.indexOf((int) '/');
			if (t >= 0)
				word = word.substring(0, t);
		}

		return word;
	}

	/*
	 * print hashtags and mentions as arrays for excel use only
	 */
	@SuppressWarnings("deprecation")
	public void printArrays(String search) {

		Mongo mongo = new Mongo("localhost", 27017);
		DB db = mongo.getDB("dbTweetsForAnalysis");
		DBCollection filteredCollection = db.getCollection("filteredTweetsForAnalysisCollection");
		System.out.println("Link Mongodb!");

		TimeStampUtils timestamp = new TimeStampUtils(2016, 5, 24, 4, 22);
		int counter = 0;
		long t1 = timestamp.initialDateInMills;
		long t2 = timestamp.addMinutedToDateInMillis(60);

		int year = 2016;
		int month = 5;
		int day = 24;
		int hour1 = 4;
		int hour2 = 5;

		ArrayList<Integer> mentionTrumpArray = new ArrayList<Integer>();
		ArrayList<Integer> hashtagTrumpArray = new ArrayList<Integer>();
		ArrayList<Integer> totalTrumpArray = new ArrayList<Integer>();

		while (counter < 72) {

			BasicDBObject query2 = new BasicDBObject("mentions.mention", search).append("timestampMs",
					new BasicDBObject("$gte", t1).append("$lt", t2));

			BasicDBObject query3 = new BasicDBObject("hashtags.hashtag", search).append("timestampMs",
					new BasicDBObject("$gte", t1).append("$lt", t2));

			DBCursor cursor = filteredCollection.find(query2);
			mentionTrumpArray.add(cursor.count());

			// printForJavascriptChartTemplate(cursor, hour1, hour2);

			cursor = filteredCollection.find(query3);
			hashtagTrumpArray.add(cursor.count());

			// printForJavascriptChartTemplate(cursor, hour1, hour2);

			t1 = t2;
			t2 = timestamp.addMinutedToDateInMillis(60);

			hour1 = (hour1 + 1) % 24;
			hour2 = (hour2 + 1) % 24;

			if (hour1 == 0) {
				day = (day + 1) % 31;
			}

			counter++;

		} // end of while

		/*
		 * System.out.println("Mentions "+ search); for (int i = 0; i < 72; i++)
		 * {
		 * 
		 * System.out.println(mentionTrumpArray.get(i)); }
		 */

		/*
		 * System.out.println("Hashtags "+ search); for (int i = 0; i < 72; i++)
		 * {
		 * 
		 * System.out.println(hashtagTrumpArray.get(i)); }
		 */

		year = 2016;
		month = 5;
		day = 24;
		hour1 = 4;
		hour2 = 5;
		System.out.println("Total references to " + search);
		for (int i = 0; i < 72; i++) {
			totalTrumpArray.add(mentionTrumpArray.get(i) + hashtagTrumpArray.get(i));
			System.out.println(totalTrumpArray.get(i));
			// System.out.println("['" + hour1 + ":00-" + hour2 + ":00', " +
			// totalTrumpArray.get(i) + "],");
			hour1 = (hour1 + 1) % 24;
			hour2 = (hour2 + 1) % 24;

			if (hour1 == 0) {
				day = (day + 1) % 31;
			}
		}

		mongo.close();

	}

	// a useful method to print for JS google chart it is combined with
	// printArrays method only
	public void printForJavascriptChartTemplate(DBCursor cursor, int hour1, int hour2) {

		// ['2013', 1000],
		System.out.println("['" + hour1 + ":00-" + hour2 + ":00', " + cursor.count() + "],");

	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws JSONException {

		MongoDBStatsForWeb stats = new MongoDBStatsForWeb();
		// stats.printTweetsPerHour();
		// stats.printArrays("HillaryClinton");
		// stats.printTotalReferencesToCandidates("realDonaldTrump",
		// "HillaryClinton", "BernieSanders");
		// stats.printCorrelations("HillaryClinton", "BernieSanders");
		// stats.printEntities("urls", "url");

	} // end of main

}
