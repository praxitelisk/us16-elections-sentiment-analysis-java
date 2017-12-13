package webmining.mongodatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import webmining.textprocessing.StringUtilsForTwitter;

import edu.stanford.nlp.simple.Sentence;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;


public class MongoDBConverterFromRawToFullJsonStructure {

	private String dbCollectionRawTweetsName = "rawTweetsForAnalysis";
	private String dbCollectionFilteredTweetsName = "filteredTweetsForAnalysis";
	private String databaseName = "dbTweetsForAnalysis";
	private Mongo mongo;
	private DB db;
	private DBCollection rawCollection;
	private DBCollection filteredCollection;
	private StringUtilsForTwitter stringUtils = new StringUtilsForTwitter();

	/*
	 * constructor - link to mongo
	 */
	@SuppressWarnings("deprecation")
	public MongoDBConverterFromRawToFullJsonStructure() {
		// Create connection to mongoDB and get links for 2 collections
		mongo = new Mongo("localhost", 27017);
		db = mongo.getDB(databaseName);
		rawCollection = db.getCollection(dbCollectionRawTweetsName);
		filteredCollection = db.getCollection(dbCollectionFilteredTweetsName);
		System.out.println("Link Mongodb!");
	}

	public static void main(String[] args) throws JSONException {

		// create a thread to call the garbage collector
		Thread GarbageCollectorThread = new Thread() {
			int minutes = 5;

			public void run() {
				while (true) {
					try {
						System.out.println("\n*****GARBAGE COLLECTOR IS CALLED*****\n");
						System.gc();
						Thread.sleep(minutes * 60 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		};
		// call garbage collector periodically
		GarbageCollectorThread.start();

		MongoDBConverterFromRawToFullJsonStructure preprocessTweets = new MongoDBConverterFromRawToFullJsonStructure();

		// ask mongoDB for all raw tweets
		DBCursor cursor = preprocessTweets.getRawCollection().find().skip(900000);

		int counter = 0;
		String str;

		String text = "";
		JSONObject json = null, rtJson = null, filteredJson = null;
		JSONObject entitiesJsonObject, userJsonObject;
		JSONArray urls, hashtags, user_mentions;
		Sentence sent = null;
		long timestamp = 0;
		int followers = 0;
		long raw_id = 0;
		String created_at = "";
		String jsonString = "";
		DBObject dbObject;

		long start, start1, start2;

		long startTime = System.currentTimeMillis();
		while (cursor.hasNext()) {

			start = System.currentTimeMillis();
			start1 = start;

			Iterator<String> posTags; // part of speech
			Iterator<String> words; // tokenize the sentence
			Iterator<String> personTags;
			Iterator<String> organizationTags;
			Iterator<String> locationTags;
			ArrayList<String> user_mentionsArray = new ArrayList<String>();
			ArrayList<String> hashtagArray = new ArrayList<String>();
			ArrayList<String> URLExpandedArray = new ArrayList<String>();

			// clear all lists - initialize variables:
			if (counter > 0) {
				json = null;
				rtJson = null;
				filteredJson = null;
				entitiesJsonObject = null;
				userJsonObject = null;
				urls = null;
				hashtags = null;
				user_mentions = null;
				sent = null;
				timestamp = 0;
				followers = 0;
				raw_id = 0;
				text = "";

				created_at = "";
				jsonString = "";
				dbObject = null;
			}

			// get raw json tweet from mongoDB
			str = cursor.next().toString();
			json = new JSONObject(str);

			// get raw json tweet's text
			if (json.has("retweeted_status")) {
				rtJson = json.getJSONObject("retweeted_status");
				text = rtJson.getString("text");
				System.out.print("retweet ");
			} else
				text = json.getString("text");

			System.out.println("initial text: " + text);

			// replace multiples of ! and ? with one only
			text = text.replaceAll("[!]+", " ! ");
			text = text.replaceAll("[\\?]+", " ? ");
			text = text.replaceAll("&amp;", " & ");

			// remove stopwords
			text = preprocessTweets.getStringUtils().removeStopWordsAndUrls(text);

			sent = new Sentence(text);

			// lemmatize the text
			text = preprocessTweets.getStringUtils().lemmatizeText(sent);

			// repair emoticons
			text = preprocessTweets.getStringUtils().detectAndReplaceEmoticons(text);

			// some more post process
			text = text.replaceAll("``", "\"");
			text = text.replaceAll("''", "\"");

			System.out.println("final text: " + text);

			// find part of speech and entities
			// nerTags = sent.nerTags();
			posTags = sent.posTags().iterator(); // part of speech
			words = sent.words().iterator(); // tokenize the sentence
			personTags = sent.mentions("PERSON").iterator();
			organizationTags = sent.mentions("ORGANIZATION").iterator();
			locationTags = sent.mentions("LOCATION").iterator();

			// lowercase text
			text = text.toLowerCase();

			// get timestamp in m_seconds
			timestamp = Long.parseLong(json.getString("timestamp_ms"));

			// get created_at
			created_at = json.getString("created_at");

			// get entities (to get mentions and hashtags)
			entitiesJsonObject = json.getJSONObject("entities");

			// get urls
			urls = entitiesJsonObject.getJSONArray("urls");
			// -------->JSONArray URLs =
			// entitiesJsonObject.getJSONArray("expanded_url");

			if (!urls.isNull(0)) {

				for (int i = 0; i < urls.length(); i++) {
					// tweetsContainingHashtagEntityCounter++;
					URLExpandedArray.add(urls.getJSONObject(i).getString("expanded_url"));
				}
			}

			// get hashtags
			hashtags = entitiesJsonObject.getJSONArray("hashtags");

			if (!hashtags.isNull(0)) {

				for (int i = 0; i < hashtags.length(); i++) {
					hashtagArray.add(hashtags.getJSONObject(i).getString("text"));
				}
			}

			// get mentions
			user_mentions = entitiesJsonObject.getJSONArray("user_mentions");

			if (!user_mentions.isNull(0)) {

				for (int i = 0; i < user_mentions.length(); i++) {

					user_mentionsArray.add(user_mentions.getJSONObject(i).getString("screen_name"));
				}
			}

			// get authoritative Users - Followers from user's profile
			userJsonObject = json.getJSONObject("user");
			followers = userJsonObject.getInt("followers_count");
			// System.out.println(followers);

			// get ref_id from rawJSON as a reference to original tweet-json
			raw_id = json.getLong("id");

			// create filtered Json
			filteredJson = preprocessTweets.createFilteredJSON(text, timestamp, created_at, followers, raw_id,
					user_mentionsArray, hashtagArray, words, personTags, organizationTags, locationTags, posTags,
					URLExpandedArray);

			System.out.println((System.currentTimeMillis() - start1) / 1000.0 + " seconds preprocess #" + counter);
			start2 = System.currentTimeMillis();

			// enter filtered json in mongoDB
			 jsonString = filteredJson.toString(); dbObject = (DBObject)
			 JSON.parse(jsonString);
			 preprocessTweets.getFilteredCollection().insert(dbObject);
			 

			// print time passed for preprocessing and for testing
			System.out.println((System.currentTimeMillis() - start2) / 1000.0 + " seconds mongoDB insert #" + counter);
			System.out.println((System.currentTimeMillis() - start) / 1000.0 + " seconds total #" + counter);

			counter++;
		}

		System.out.println((System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
		preprocessTweets.getMongo().close();
		System.exit(0);

	}

	//a method to create the filtered json file
	@SuppressWarnings("deprecation")
	public JSONObject createFilteredJSON(String textOfTweet, Long timestamp, String created_at, int followers,
			long raw_id, ArrayList<String> user_mentionsArray, ArrayList<String> hashtagArray, Iterator<String> words,
			Iterator<String> personTags, Iterator<String> organizationTags, Iterator<String> locationTags,
			Iterator<String> posTags, List<String> urlList) {

		JSONObject fiteredJSON = new JSONObject();

		try {

			// put text
			fiteredJSON.put("text", textOfTweet);

			// put timestamp
			fiteredJSON.put("timestampMs", timestamp);

			// put created_at
			fiteredJSON.put("created_at", (new Date(created_at)));

			// put followers
			fiteredJSON.put("followers", followers);

			// put raw_id
			fiteredJSON.put("refId", raw_id);

			// put hashtags
			JSONArray hashtagList = new JSONArray();
			for (int i = 0; i < hashtagArray.size(); i++) {
				JSONObject hashtag = new JSONObject();
				hashtag.put("hashtag", hashtagArray.get(i));
				hashtagList.put(hashtag);
			}
			fiteredJSON.put("hashtags", hashtagList);

			// put mentions
			JSONArray mentionList = new JSONArray();
			for (int i = 0; i < user_mentionsArray.size(); i++) {
				JSONObject mention = new JSONObject();
				mention.put("mention", user_mentionsArray.get(i));
				mentionList.put(mention);
			}
			fiteredJSON.put("mentions", mentionList);

			// put pos
			JSONArray posList = new JSONArray();
			while (posTags.hasNext()) {
				String str = posTags.next();
				String word = words.next();
				JSONObject pos = new JSONObject();
				if (IsNotASymbol(str)) {
					pos.put(str, word);
					posList.put(pos);
				} else {
					pos.put("punctuation", str);
					posList.put(pos);
				}
			}
			fiteredJSON.put("partOfSpeech", posList);

			// put persons
			JSONArray personList = new JSONArray();
			while (personTags.hasNext()) {
				String str = personTags.next();
				JSONObject person = new JSONObject();
				person.put("person", str);
				personList.put(person);
			}
			fiteredJSON.put("persons", personList);

			// put organization
			JSONArray organizationList = new JSONArray();
			while (organizationTags.hasNext()) {
				String str = organizationTags.next();
				JSONObject organization = new JSONObject();
				organization.put("organization", str);
				organizationList.put(organization);
			}
			fiteredJSON.put("organizations", organizationList);

			// put location
			JSONArray locationList = new JSONArray();
			while (locationTags.hasNext()) {
				String str = locationTags.next();
				JSONObject location = new JSONObject();
				location.put("location", str);
				locationList.put(location);
			}
			fiteredJSON.put("locations", locationList);

			// put urls
			JSONArray urlsList = new JSONArray();
			for (int i = 0; i < urlList.size(); i++) {
				JSONObject url = new JSONObject();
				url.put("url", urlList.get(i));
				urlsList.put(url);
			}
			fiteredJSON.put("urls", urlsList);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		// System.out.println(fiteredJSON);
		return fiteredJSON;

	}

	// method that checks whether a string is not a symbol
	public boolean IsNotASymbol(String s) {

		String pattern = "[A-Z]+";

		try {
			Pattern patt = Pattern.compile(pattern);
			Matcher matcher = patt.matcher(s);
			return matcher.matches();
		} catch (RuntimeException e) {
			return false;
		}
	}

	public StringUtilsForTwitter getStringUtils() {
		return stringUtils;
	}

	public void setStringUtils(StringUtilsForTwitter stringUtils) {
		this.stringUtils = stringUtils;
	}

	public String getDbCollectionRawTweetsName() {
		return dbCollectionRawTweetsName;
	}

	public void setDbCollectionRawTweetsName(String dbCollectionRawTweetsName) {
		this.dbCollectionRawTweetsName = dbCollectionRawTweetsName;
	}

	public String getDbCollectionFilteredTweetsName() {
		return dbCollectionFilteredTweetsName;
	}

	public void setDbCollectionFilteredTweetsName(String dbCollectionFilteredTweetsName) {
		this.dbCollectionFilteredTweetsName = dbCollectionFilteredTweetsName;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public Mongo getMongo() {
		return mongo;
	}

	public void setMongo(Mongo mongo) {
		this.mongo = mongo;
	}

	public DB getDb() {
		return db;
	}

	public void setDb(DB db) {
		this.db = db;
	}

	public DBCollection getRawCollection() {
		return rawCollection;
	}

	public void setRawCollection(DBCollection rawCollection) {
		this.rawCollection = rawCollection;
	}

	public DBCollection getFilteredCollection() {
		return filteredCollection;
	}

	public void setFilteredCollection(DBCollection filteredCollection) {
		this.filteredCollection = filteredCollection;
	}

}
