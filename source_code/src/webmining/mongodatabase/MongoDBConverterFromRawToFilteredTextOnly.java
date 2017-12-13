package webmining.mongodatabase;

import webmining.textprocessing.StringUtils;

import twitter4j.JSONException;
import twitter4j.JSONObject;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

/*
 * a class to preprocess only the text, creata a new mongo collection and insert the new
 * documents there. Preprocessing uses state of the art class StringUtils
 */
public class MongoDBConverterFromRawToFilteredTextOnly {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws JSONException {
		

		/*
		 * link to mongo
		 */
		MongoDBConverterFromRawToFilteredTextOnly preprocess = new MongoDBConverterFromRawToFilteredTextOnly();

		Mongo mongo = new Mongo("localhost", 27017);
		DB db = mongo.getDB("dbTweetsForAnalysis");
		DBCollection rawCollection = db.getCollection("rawTweetsForAnalysis");
		DBCollection filteredCollection = db.getCollection("filteredTweetsTextOnlyVersionTwo");
		System.out.println("Link Mongodb!");

		String str = "";
		String text = "";
		JSONObject json, rtJson = null;
		String jsonString = "";
		DBObject dbObject =null;
		JSONObject filteredJson = null;
		long timestamp = 0;

		DBCursor cursor = rawCollection.find();

		while (cursor.hasNext()) {

			str = cursor.next().toString();
			json = new JSONObject(str);
			if (json.has("retweeted_status")) {
				rtJson = json.getJSONObject("retweeted_status");
				text = rtJson.getString("text");
			} else
				text = json.getString("text");

			System.out.println("initial text: " + text);

			//state of the art preprocessing
			text = StringUtils.getTextPreprocessedWithoutPOSTags(text, true);

			System.out.println("final text: " + text);

			// get timestamp in m_seconds
			timestamp = Long.parseLong(json.getString("timestamp_ms"));

			filteredJson = preprocess.createFilteredJSON(text, timestamp);

			jsonString = filteredJson.toString();
			dbObject = (DBObject) JSON.parse(jsonString);
			filteredCollection.insert(dbObject);

		}
		mongo.close();
	}

	/*
	 * a method to create a new filtered Json file
	 */
	public JSONObject createFilteredJSON(String textOfTweet, Long timestamp) {

		JSONObject fiteredJSON = new JSONObject();

		try {

			// put text
			fiteredJSON.put("text", textOfTweet);

			// put timestamp
			fiteredJSON.put("timestampMs", timestamp);

			// System.out.println(fiteredJSON);
			return fiteredJSON;

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return fiteredJSON;
	}
}
