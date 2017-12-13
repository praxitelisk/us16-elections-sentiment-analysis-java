package webmining.topicmodeling;

public class FileIterationOuputMallet extends AbstractFileIteration {

        private final String BASE_PATH = "data//";    
        private final String FILE_OUTPUT = BASE_PATH + "8topicModeling//res.txt";
	MalletTopics maletTopic;

	public FileIterationOuputMallet(MalletTopics maletTopic) {
		this.maletTopic = maletTopic;
	}

	// run mallet and export results to a file
	@Override
	void run(String pathIn, String filenameInput, String extension, String outPrefix, String pathOutput) {

		try {
			maletTopic.run(pathOutput + outPrefix + filenameInput + extension, FILE_OUTPUT);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

}
