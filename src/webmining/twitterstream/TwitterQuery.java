package webmining.twitterstream;

import java.util.List;

import twitter4j.conf.ConfigurationBuilder;
import twitter4j.TwitterFactory;
import twitter4j.json.DataObjectFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.Status;
import twitter4j.Query;
import twitter4j.QueryResult;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;


@SuppressWarnings("deprecation")
public class TwitterQuery {

	private ConfigurationBuilder cb;
	private Twitter twitter;
	private long globalCounter;

	
	//constructor
	public TwitterQuery() {
		
		/*
		 * Give the credentials to twitter
		 */
		this.cb = new ConfigurationBuilder();
		cb.setJSONStoreEnabled(true);
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey("bdyd7u1ORUktPN5wb9tropZXv");
		cb.setOAuthConsumerSecret("81i9eA6nvy90lULUCALhWQmN4mMqn7AFaSYSPAMj45N3U1oJhE");
		cb.setOAuthAccessToken("715798005509660673-IFbmxIl3gCDaOSKsAxPkm3w01aSWrqt");
		cb.setOAuthAccessTokenSecret("fO4KGRzPUpzFdgd6DJkB1sX9lnQU8oAIWrDexODIK9pp5");
		twitter = new TwitterFactory(cb.build()).getInstance();
	}

	public long getGlobalCounter() {
		return this.globalCounter;
	}

	/*
	 * query twitter and save tweets to database
	 */
	@SuppressWarnings("static-access")
	public void queryStreamByNumberOfTweets(int numOfTweets) {

		try {
			
			//link to mongo
			MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
			DB db = mongoClient.getDB("dbtest");
			DBCollection coll = db.getCollection("TwitterStreamingSample1");
			
			int queryCounter=0;
			
			globalCounter = 0;
			while (globalCounter < numOfTweets) {
				QueryResult result;
				Query query = new Query("Trump");
				result = twitter.search(query);
				queryCounter++;
				List<Status> tweets = result.getTweets();

				for (Status tweet : tweets) {
					globalCounter++;
					System.out.println(
							globalCounter + " tweet, @" + tweet.getUser().getScreenName() + " - " + tweet.getText());
					String json = DataObjectFactory.getRawJSON(tweet);
					DBObject dbObject = (DBObject)JSON.parse(json);
					coll.insert(dbObject);
				}

				//due to twitter limitations 180 queries per 15 minutes
				//window are allowed, so sleep until the limitation is passed
				if (queryCounter> 178) {
					Thread.currentThread().sleep(1000*15*60);
					queryCounter=0;
				}	
			}
			mongoClient.close();
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to search tweets: " + te.getMessage());
			System.exit(-1);
		} catch (InterruptedException e) {
			System.out.println("Inturrupt exception thread: " + e.getMessage());
			System.exit(-1);
			e.printStackTrace();
		}
	}

	/*
	 * query tweeter for some days
	 */
	@SuppressWarnings("static-access")
	public void queryStreamByRunningTime(double days) {

		try {
			
			double startTime = System.currentTimeMillis();
			double endTime = System.currentTimeMillis();
			double totalTime = endTime - startTime;
			
			MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
			DB db = mongoClient.getDB("dbtest");
			DBCollection coll = db.getCollection("TwitterQuerySample1");
			
			int queryCounter=0;
			
			globalCounter = 0;
			while (totalTime / (1000 * 60 * 60 * 24) < days) {
				QueryResult result;
				Query query = new Query("Trump");
				result = twitter.search(query);
				queryCounter++;
				List<Status> tweets = result.getTweets();

				for (Status tweet : tweets) {
					globalCounter++;
					String json = DataObjectFactory.getRawJSON(tweet);
					DBObject dbObject = (DBObject)JSON.parse(json);
					coll.insert(dbObject);
				}

				endTime = System.currentTimeMillis();
				totalTime = endTime - startTime;
				
				//due to twitter limitations 180 queries per 15 minutes
				//window are allowed, so sleep until the limitation is passed
				if (queryCounter > 178) {
					Thread.currentThread().sleep(1000 * 15 * 60);
					queryCounter=0;
				}
			}
			mongoClient.close();
			

		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to search tweets: " + te.getMessage());
			System.exit(-1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("Inturrupt exception thread: " + e.getMessage());
			System.exit(-1);
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		double startTime = System.currentTimeMillis();

		TwitterQuery twitterStreamInstance = new TwitterQuery();

		// uncomment the line bellow to run by number of tweets as parementer
		//twitterStreamInstance.streamByNumberOfTweets(1);

		// uncomment the line bellow to run by days of execution as parementer
		twitterStreamInstance.queryStreamByRunningTime(1);

		double endTime = System.currentTimeMillis();
		double totalTime = endTime - startTime;
		System.out.println("\n" + totalTime / 1000 + " seconds running time");
		System.out.println("\n" + twitterStreamInstance.getGlobalCounter() + "# of tweets");
	}

}
