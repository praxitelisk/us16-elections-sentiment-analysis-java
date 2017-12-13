package webmining.topicmodeling;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.*;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.*;

//we use MalletTopics for running Mallet algorithm
public class MalletTopics {

        private final String BASE_PATH = "data//";    
        private final String STOPWORDS_LIST = BASE_PATH + "0wordLists//en_mallet.txt";            
    
	private int numberOfThreads = 1;
	private int topWordsPerTopic;
	private int numOfTopics;
	
	private boolean showFrequencies ;
	private int numOfIterations; //for test 50, for real applications 1000-2000
										

	public MalletTopics(int numOfTopics, int wordPerTopic, int numOfIterations, boolean showFreqencies) {
		this.numOfTopics = numOfTopics;
		this.topWordsPerTopic = wordPerTopic;
		this.numOfIterations = numOfIterations;
		this.showFrequencies = showFreqencies;
	}

	public MalletTopics() {

	}

	public void run(String fileInput, String fileOutput) throws Exception {

		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		pipeList.add(new TokenSequenceRemoveStopwords(new File(STOPWORDS_LIST), "UTF-8", false, false, false));
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));

		Reader fileReader = new InputStreamReader(new FileInputStream(new File(fileInput)), "UTF-8");
		instances
				.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1)); // data,
																															// label,
																															// name
																															// fields

		// Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
		// Note that the first parameter is passed as the sum over topics, while
		// the second is the parameter for a single dimension of the Dirichlet
		// prior.

		ParallelTopicModel model = new ParallelTopicModel(numOfTopics, 1.0, 0.01);

		model.addInstances(instances);

		// Use two parallel samplers, which each look at one half the corpus and
		// combine
		// statistics after every iteration.
		model.setNumThreads(numberOfThreads);

		// Run the model for 50 iterations and stop (this is for testing only,
		// for real applications, use 1000 to 2000 iterations)
		model.setNumIterations(numOfIterations);
		model.estimate();

		// Show the words and topics in the first instance

		// The data alphabet maps word IDs to strings
		Alphabet dataAlphabet = instances.getDataAlphabet();

		FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();

		LabelSequence topics = model.getData().get(0).topicSequence;

		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		for (int position = 0; position < tokens.getLength(); position++) {
			out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)),
					topics.getIndexAtPosition(position));
		}
		System.out.println(out);
		double[] topicDistribution = model.getTopicProbabilities(0);

		// Get an array of sorted sets of word ID/count pairs

		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		// Show top 5 words in topics with proportions for the first document

		// Create a new instance with high probability of topic 0

		// Show top 5 words in topics with proportions for the first document
		for (int topic = 0; topic < numOfTopics; topic++) {
			Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

			out = new Formatter(new StringBuilder(), Locale.US);
			out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
			int rank = 0;
			while (iterator.hasNext() && rank < topWordsPerTopic) {
				IDSorter idCountPair = iterator.next();

				// out.format("%s (%.0f) ",
				// dataAlphabet.lookupObject(idCountPair.getID()),
				// idCountPair.getWeight());
				out.format("%s-%.0f ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
				rank++;
			}
			try {
				if (new File(fileOutput).exists()) {
					Files.write(Paths.get(fileOutput), ("\n" + out.toString()).getBytes(), StandardOpenOption.APPEND);
				} else {
					Files.write(Paths.get(fileOutput), out.toString().getBytes(), StandardOpenOption.CREATE);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			System.out.println(out);
		}

	}

}