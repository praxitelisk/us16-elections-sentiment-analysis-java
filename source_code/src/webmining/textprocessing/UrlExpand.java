package webmining.textprocessing;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

/*
 * Expand a tiny url into its original form
 */
public class UrlExpand {

	public String expandUrl(String link) {

		URL url = null;
		HttpURLConnection connection = null;
		String expandedURL = link;
		String oldURL = "";
		
		
			try {
				oldURL = expandedURL;
				url = new URL(expandedURL);
				connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
				connection.setInstanceFollowRedirects(false);
				expandedURL = connection.getHeaderField("Location");
				connection.getInputStream().close();
			} catch (MalformedURLException e) {
				return oldURL;
				// e.printStackTrace();
			} catch (IOException e) {
				return oldURL;
				// e.printStackTrace();
			} catch (Exception e) {
				return oldURL;
				// e.printStackTrace();
			}
		

		return expandedURL;
	};

	@SuppressWarnings("unused")
	private String expandShortURLonce(String address) throws IOException, MalformedURLException {
		URL url = new URL(address);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY); // using
																								// proxy																				// latency
		int statusCode = connection.getResponseCode();
		if (statusCode != 301 && statusCode != 302) {
			System.out.println(statusCode);
		}
		return address;

	}
}
