package webmining.textprocessing;

import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.simple.Sentence;

public class StringUtilsForTwitter {

	/*
	 * Remove text's stop words, urls, rt and via
	 */
	public String removeStopWordsAndUrls(String text) {

		StringBuilder stringTemp = new StringBuilder();
		StringTokenizer defaultTokenizer = new StringTokenizer(text);
		String str;

		while (defaultTokenizer.hasMoreTokens()) {
			str = defaultTokenizer.nextToken();
			// System.out.println("stop word: " +str);
			if (str.endsWith("n't") || str.endsWith("not"))
				str = "not";

                        if ( !StopWordChecker.isTextAStopWord(str) && !str.startsWith("ht") && !str.startsWith("htt")
					&& !str.startsWith("http") && !str.startsWith("https") && !str.startsWith("RT")
					&& !str.startsWith("rt") && !str.startsWith("&amp;") && !str.startsWith("via")
					&& !str.startsWith("..."))

				stringTemp.append(str + " ");
		}

		return stringTemp.toString().trim();
	}

	/*
	 * Lemmatization the text
	 */
	public String lemmatizeText(Sentence sent) {

		// Sentence sent = new Sentence(str);
		List<String> limmas = sent.lemmas();

		String lemmaString = "";
		for (String s : limmas) {
			lemmaString += s + " ";
		}

		return lemmaString.trim();
	}

	/*
	 * Detect and convert emoticons after lemmatization
	 */
	public String detectAndReplaceEmoticons(String text) {

		text = text.replace(": p", ":p");

		text = text.replace(":-LRB-", ":(");
		text = text.replace(":-lrb-", ":(");
		text = text.replace(":--LRB-", ":-(");
		text = text.replace(":--lrb-", ":-(");

		text = text.replace(":-rrb-", ":)");
		text = text.replace(":-RRB-", ":)");
		text = text.replace(":--RRB-", ":-)");
		text = text.replace(":--rrb-", ":-)");

		text = text.replace(": *", ":*");
		text = text.replace(";-rrb-", ";)");
		text = text.replace(": d", ":d");
		text = text.replace(": o", ":o");
		text = text.replace(": $", ":$");

		return text;
	}

	/*
	 * Stemming version 2 Text
	 */
	public String stemmingText(String text) {

		Stemmer stemmer = new Stemmer();
		StringTokenizer defaultTokenizer = new StringTokenizer(text);
		StringBuilder strBuilder = new StringBuilder();
		while (defaultTokenizer.hasMoreTokens()) {
			strBuilder.append(stemmer.stem(defaultTokenizer.nextToken()) + " ");
		}

		return strBuilder.toString().trim();

	}

	// url regular expression matcher
	public boolean IsUrl(String s) {

		String pattern = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

		try {
			Pattern patt = Pattern.compile(pattern);
			Matcher matcher = patt.matcher(s);
			return matcher.matches();
		} catch (RuntimeException e) {
			return false;
		}
	}
}

//import cue.lang.stop.StopWords;
//	private StopWords stopWords = StopWords.English;
//if (stopWords != null && !stopWords.isStopWord(str) && !str.startsWith("ht") && !str.startsWith("htt")