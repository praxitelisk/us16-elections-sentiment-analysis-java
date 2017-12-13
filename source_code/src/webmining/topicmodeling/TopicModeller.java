package webmining.topicmodeling;

public class TopicModeller {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//create inputFiles for mallet
		AbstractFileIteration inputMallet = new FileIterationInputMallet();
		inputMallet.runLoop();
		
		//mallet parameters
		MalletTopics mt = new MalletTopics(3, 10, 1000, true);
		//run mallet and create new file
		AbstractFileIteration outputMallet = new FileIterationOuputMallet(mt);
		outputMallet.runLoop();
		//filter topics with weight 0.3
		FilterTopics filterMalletResults = new FilterTopics();
		filterMalletResults.run(true,0.3);
		
		//print in console code for creating graph on website
		new CreateElementsForWebSite().createGraphCode(0.3,CreateElementsForWebSite.OCCURENCE);

	}

}
