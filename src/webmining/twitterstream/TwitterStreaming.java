package webmining.twitterstream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import twitter4j.conf.ConfigurationBuilder;
import twitter4j.TwitterStreamFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;
import twitter4j.JSONObject;
import twitter4j.JSONException;
import twitter4j.StatusListener;
import twitter4j.Status;
import twitter4j.StallWarning;
import twitter4j.StatusDeletionNotice;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;


@SuppressWarnings("deprecation")

/*
 * A class to stream - download tweets from tweeter using twitter4j library api
 * and saving the tweets in a mongoDB collection.
 */
public class TwitterStreaming {

	// constant variables for tracking the twitter and
	public static final String[] trackArray = { "realDonaldTrump", "HillaryClinton", "BernieSanders" };

	// generic counters for tweets
	private int tweetsInsertedToDatabasedCounter = 0;
	private int tweetsReceivedFromStreamCounter = 0;
	private int tweetsWithLowQuaility = 0;

	// averate rate per minute - statistics
	private ArrayList<Double> averageRatePerMinute = new ArrayList<Double>();
	private int oldValueTweetsInsertedInDB = 0;
	private int newValueTweetsInsertedInDB = 0;
	private boolean isFirstTimeInsertedInAvgList = true;

	// halt app conditions
	private int maxFilteredTweetsForDBInsertion = 1000000;
	private int limit_days = 3;
	private long startTime = System.currentTimeMillis();
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss:S");

	// DB variables
	private String dbCollectionRawTweetsName = "tweetsRaw";
	private String dbCollectionFilteredTweetsName = "tweetsFiltered";
	private String databaseName = "tweetsDB";
	private Mongo mongo;
	private DB db;
	private DBCollection rawCollection;
	private DBCollection filteredCollection;

	/*
	 * constructor
	 */
	public TwitterStreaming() {
		// Create connection to mongoDB and get links for 2 collections
		mongo = new Mongo("localhost", 27017);
		db = mongo.getDB(databaseName);
		rawCollection = db.getCollection(dbCollectionRawTweetsName);
		filteredCollection = db.getCollection(dbCollectionFilteredTweetsName);
		System.out.println("Link Mongodb!");
	}

	/*
	 * Check the tweet's quality in order to continue the preprocess
	 */
	public boolean isTweetValidForCollection(String tweet) {

		boolean isGoodQualityTweet = true;

		try {
			JSONObject json = new JSONObject(tweet);

			// Test #0, check whether certain fields exist
			if (json.has("text") && json.has("entities") && json.has("lang") && json.has("timestamp_ms")
					&& json.has("id_str")) {

				// Test #1 remove non english tweets
				if (!json.getString("lang").equals("en")) {
					this.tweetsWithLowQuaility++;
					isGoodQualityTweet = false;
					return isGoodQualityTweet;
				}

				// get text from json and lowercase it
				String text = json.getString("text").toLowerCase();

				// Test #2 tokenize tweet's text and ditch tweets with 4 or less
				// words
				StringTokenizer defaultTokenizer = new StringTokenizer(text);
				if (defaultTokenizer.countTokens() <= 4) {

					this.tweetsWithLowQuaility++;
					isGoodQualityTweet = false;
					return isGoodQualityTweet;
				}

				// Test #3 null tweet remove - from twitterbot
				// e.g. ca 19th May, 2016 20:00pm und
				if (text.contains(", " + Calendar.getInstance().get(Calendar.YEAR))) {

					this.tweetsWithLowQuaility++;
					isGoodQualityTweet = false;
					return isGoodQualityTweet;
				}

				// Test #4 check whether any mention or hashtag in text or
				// in entities (mentions and hashtags) to the candidates does
				// not exist
				boolean foundAnyCandidateReference = false;

				// check at first the text
				while (defaultTokenizer.hasMoreTokens() && !foundAnyCandidateReference) {
					String str = defaultTokenizer.nextToken();
					// System.out.println("str = "+str);
					if (Pattern.compile(Pattern.quote("realdonaldtrump"), Pattern.CASE_INSENSITIVE).matcher(str).find()
							|| Pattern.compile(Pattern.quote("hillaryclinton"), Pattern.CASE_INSENSITIVE).matcher(str)
									.find()
							|| Pattern.compile(Pattern.quote("berniesanders"), Pattern.CASE_INSENSITIVE).matcher(str)
									.find())
						foundAnyCandidateReference = true;

				}

				if (!foundAnyCandidateReference) {
					this.tweetsWithLowQuaility++;
					isGoodQualityTweet = false;
					return isGoodQualityTweet;
				}
			} else {
				isGoodQualityTweet = false;
				return isGoodQualityTweet;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		// if everything is ok - all tests are passed
		return isGoodQualityTweet;
	}

	/*
	 * print Statistics
	 */
	public void printStatistics() {

		this.oldValueTweetsInsertedInDB = this.newValueTweetsInsertedInDB;
		this.newValueTweetsInsertedInDB = this.tweetsInsertedToDatabasedCounter;
		double rateInDBPerMinute = Math.abs(this.newValueTweetsInsertedInDB - this.oldValueTweetsInsertedInDB);

		if (this.isFirstTimeInsertedInAvgList)
			this.isFirstTimeInsertedInAvgList = false;
		else
			this.averageRatePerMinute.add(rateInDBPerMinute);

		System.out.println();
		System.out.println("_________________________________________________________________");
		System.out.println("Program started at " + simpleDateFormat.format(this.startTime));
		System.out.println("Current date and time " + simpleDateFormat.format(new Date()));
		System.out.println(tweetsReceivedFromStreamCounter + " tweets got from streaming");
		System.out.println(tweetsInsertedToDatabasedCounter + " tweets inserted in db");
		System.out.println(rateInDBPerMinute + " tweets/minute inserted in DB in the last minute");
		System.out.println(
				calculateAverage(this.averageRatePerMinute) + " tweets/minute on average inserted in DB per minute");
		System.out.println(tweetsWithLowQuaility + " or "
				+ getFrequency(tweetsWithLowQuaility, tweetsReceivedFromStreamCounter) + "% tweets with low quality");

		System.out.println((System.currentTimeMillis() - startTime) / (1000 * 60 * 60) + " hours passed, "
				+ ((System.currentTimeMillis() - startTime) / (1000 * 60)) % 60 + " minutes passed");
		System.out.println("_________________________________________________________________");
		System.out.println();
	}

	public double getFrequency(double nominator, double denominator) {
		if (denominator != 0)
			return Math.round((nominator / denominator) * 100.0 * 100.0) / 100.0;
		else
			return 0.0;
	}

	public double calculateAverage(ArrayList<Double> marks) {

		if (marks.size() != 0) {
			int sum = 0;
			for (int i = 0; i < marks.size(); i++) {
				sum += marks.get(i);
			}
			return sum / marks.size();
		} else {
			return 0.0;
		}
	}

	// main class
	public static void main(String[] args) throws TwitterException, JSONException {

		TwitterStreaming twitterStreaming = new TwitterStreaming();

		// Configure a connection to Twitter and get a Stream object
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);

		// mine auth 
		cb.setOAuthConsumerKey("******************************");
		cb.setOAuthConsumerSecret("******************************");
		cb.setOAuthAccessToken("******************************");
		cb.setOAuthAccessTokenSecret("******************************");
		cb.setJSONStoreEnabled(true);
		TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
		TwitterStream twitterStream = tf.getInstance();

		// some variables

		// create a thread to call the garbage collector
		Thread GarbageCollectorThread = new Thread() {
			int minutes = 20;

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

		// create a thread which counts and prints # of tweets fetched, filtered
		// and inserted to database
		Thread CounterThread = new Thread() {
			int seconds = 60;

			public void run() {
				while (true) {
					twitterStreaming.printStatistics();
					try {
						Thread.sleep(seconds * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		};
		CounterThread.start();

		// o listener einai ena java object to opoio akouei thn kinhsh sto
		// twitterStream
		StatusListener listener = new StatusListener() {

			String rawTweet = "";

			// trexei ka8e fora pou yparxei nea kinhsh sto twitterStream (neo
			// tweet)
			public void onStatus(Status status) {

				rawTweet = DataObjectFactory.getRawJSON(status);

				twitterStreaming.incrementStreamingTweetsCounter();

				if (twitterStreaming.isTweetValidForCollection(rawTweet)) {

					// insert raw json in db

					twitterStreaming.getRawCollection().insert((DBObject) JSON.parse(rawTweet));
					twitterStreaming.incrementTweetsInDBCounter();
				}
				try {

					if (twitterStreaming.checkTime() || (twitterStreaming
							.getTweetsInsertedToDatabasedCounter() >= twitterStreaming.getLimit_tweets())) {
						twitterStreaming.printStatistics();
						twitterStreaming.getMongo().close();
						System.exit(0);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
			}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
			}

			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
			}

			public void onStallWarning(StallWarning warning) {
				System.out.println("Got stall warning:" + warning);
			}

			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};

		twitterStream.addListener(listener);

		twitterStream.filter(trackArray);

	} // end of main

	public Mongo getMongo() {
		return this.mongo;
	}

	public int getTweetsInsertedToDatabasedCounter() {
		return tweetsInsertedToDatabasedCounter;
	}

	public void incrementTweetsInDBCounter() {
		this.tweetsInsertedToDatabasedCounter++;
	}

	public void incrementStreamingTweetsCounter() {
		this.tweetsReceivedFromStreamCounter++;
	}

	public String getRawDbCollection() {
		return dbCollectionRawTweetsName;
	}

	public String getFilteredDbCollection() {
		return dbCollectionFilteredTweetsName;
	}

	public void setDbCollection(String dbCollection) {
		this.dbCollectionRawTweetsName = dbCollection;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String database) {
		this.databaseName = database;
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

	public int getLimit_days() {
		return limit_days;
	}

	public void setLimit_days(int limit_days) {
		this.limit_days = limit_days;
	}

	public long getStartTime() {
		return startTime;
	}

	public int getTweetsReceivedFromStreamCounter() {
		return tweetsReceivedFromStreamCounter;
	}

	public int getTweetsWithLowQuaility() {
		return tweetsWithLowQuaility;
	}

	public int getLimit_tweets() {
		return maxFilteredTweetsForDBInsertion;
	}

	public void setLimit_tweets(int limit_tweets) {
		this.maxFilteredTweetsForDBInsertion = limit_tweets;
	}

	public boolean checkTime() {

		long currTime = System.currentTimeMillis();
		if ((currTime - startTime) > (limit_days * 24 * 60 * 60 * 1000))
			return true;
		return false;

	}
}
