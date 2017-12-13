package webmining.textprocessing;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.stream.Stream;

import webmining.utils.RuntimeStatistics;

/**
 *
 * @author George Kynigopoulos
 */
public class TextPreprocessor {

    // we only need to change some of these variables to execute the feature extractor with diferrent configuration settings
    private static final String BASE_PATH = "data//";
    private static final String PATH_TO_UNPROCESSED_CSV_FILE = BASE_PATH + "1annotatedDataset//AnnotatedDataset.csv";
    private static final String PATH_TO_PREPROCESSED_CSV_FILE = BASE_PATH + "2preprocessedDataset//AnnotatedDatasetPreprocessed.csv";
    private static final boolean IS_DATASET_LABELED = true;    
    private static final boolean KEEP_MENTIONS_AND_RETWEETS = false;        

    private static final TextPreprocessor INSTANCE = new TextPreprocessor();    
    
    private TextPreprocessor(){}    
    
    public static TextPreprocessor getInstance() {
        
        return INSTANCE;
    }     

    public static void main(String[] args) {
        
        preprocess( PATH_TO_UNPROCESSED_CSV_FILE , PATH_TO_PREPROCESSED_CSV_FILE , IS_DATASET_LABELED );      
    }

    public static void preprocess(String inputFileName, String outputFilename , boolean isDatasetLabeled) {
        
        RuntimeStatistics.addNewCheckPointValues(); //1
        
        //reader to a dataset which contains plain text without one class per row
        try ( FileInputStream fileInputStream = new FileInputStream(inputFileName) ) {

            try ( BufferedReader reader = new BufferedReader( new InputStreamReader(fileInputStream) ) ; 
                  BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(outputFilename) ) )) {
                
                RuntimeStatistics.addNewCheckPointValues(); //2            
                
                Stream<String> lines = reader.lines();
                lines
                    //.parallel()
                    .forEach( (line) -> {
                        try {
                            
                            if(isDatasetLabeled){
                                int indexOf = line.lastIndexOf(',');                                                        
                                writer.write( StringUtils.getTextPreprocessedWithPOSTags( line.substring( 0, indexOf ), KEEP_MENTIONS_AND_RETWEETS ).toLowerCase() + ',' + line.substring( indexOf+1 ) + '\n' );                                
                            }
                            else
                                writer.write( StringUtils.getTextPreprocessedWithoutPOSTags(line, KEEP_MENTIONS_AND_RETWEETS ) + '\n' );                                
                        } 
                        catch (IOException ex) { System.out.println(ex); }
                    });                

                RuntimeStatistics.addNewCheckPointValues(); //3                  
                
                reader.close();
            }
        }
        catch (FileNotFoundException ex) { System.out.println(ex); }
        catch (IOException ex) { System.out.println(ex); }        

        RuntimeStatistics.printValuesForAllCheckPoints();           
    }
}