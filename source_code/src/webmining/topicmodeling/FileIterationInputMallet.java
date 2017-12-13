package webmining.topicmodeling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileIterationInputMallet extends AbstractFileIteration {

	//use method run convert CSV files compatible with mallet input
	@Override
	void run(String pathIn, String filename, String extension, String outPrefix, String pathOutput) {

            // TODO Auto-generated method stub
	    createPreprocessedFile(pathIn, filename, extension, outPrefix, pathOutput);	
	}
	
	private void createPreprocessedFile(String pathIn, String fileName, String extension,String outPrefix, String pathOut)  {
		FileReader in;
		FileWriter fw;

		try {
			in = new FileReader(pathIn + fileName + extension);
			BufferedReader br = new BufferedReader(in);
			String line;
			fw = new FileWriter(new File(pathOut + outPrefix + fileName + extension));
			int counterLines=0;
			while ((line = br.readLine()) != null) {
				//delete first line ="_ix text"(header)
				if (counterLines!=0) {
					line = splitStringAddX(line);
					fw.write(line);
					fw.write(System.lineSeparator()); // new line
				}
				counterLines++;
			}
			fw.close();
		} catch (IOException ex) {

			ex.printStackTrace();
		}

	}

        private String splitStringAddX(String text){
		
		String[] tokens=text.split(",");
		text=text.replace(tokens[0]+",", "");
		
		//text preprocessing remove names of candidates
		text=text.toLowerCase()
                        .replace("realdonaldtrump", "")
			.replace("hilaryclinton", "")
			.replace("berniesanders", "")
			.replace("berniesander", "")
			.replace("trump", "")
			.replace("donald", "")
			.replace("hilary", "")
			.replace("hillary", "")
			.replace("clinton", "")
			.replace("sanders", "")
			.replace("bernie", "")
			.replace("t.co","");//link
				
		
		String textFinal = tokens[0] + "\tX\t" + text;
		//sto only text kanoume tin epeksergasia pou 8eloume kai meta to pros8etooume
		
		return textFinal;
		
	}	

}