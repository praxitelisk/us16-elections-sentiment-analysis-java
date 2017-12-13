package webmining.machinelearning;

import webmining.utils.RuntimeStatistics;

/**
 *
 * @author George Kynigopoulos
 */
public class UnlabeledBagOfFeaturesBuilder extends AbstractBagOfFeaturesBuilder {

    private static final String BASE_PATH = "data//";
    private static final String PATH_TO_INPUT_ARFF_FILE = BASE_PATH + "3trainingFilesForWEKA//BagOfFeaturesFinal.arff";            
    private static final int EVERY_X_HOURS = 2;
    private static final String PATH_TO_FILTERED_TWEETS_FROM_STREAM = BASE_PATH + "4filteredTweetsFromStreamPer" + EVERY_X_HOURS + "Hours//" + "2016_5_";
    private static final String PATH_TO_TESTING_FILES_FOR_WEKA = BASE_PATH + "5testingFilesForWEKA//2016_5_";    
    
    private static final boolean COUNT_FREQUENCIES = true;    
    private static final boolean CSV_CONTAINS_HEADER_ROW = true;     
    
    private static final String RELATION = "US_ELECTIONS";
    private static final String CLASS_ATTRIBUTE_NAME = "EMOTION_CLASS";    

    private static String pathToCurrentCSVInputFile;
    private static String pathToCurrentARFFOutputFile; 
    
    public static void main(String[] args) {
        
        UnlabeledBagOfFeaturesBuilder builder = new UnlabeledBagOfFeaturesBuilder();      
        
        int y = 24;
        int t1, t2;
        
        for ( t1 = 0 ; t1 < 24 ; t1 += EVERY_X_HOURS ){
            
            if ( (t1 == 0 || t1 == 2) && y == 24 )
                continue;

            t2 = t1 + EVERY_X_HOURS;
                
            if( t2 == 24 )
                t2 = 0;            
            
            pathToCurrentCSVInputFile = PATH_TO_FILTERED_TWEETS_FROM_STREAM + y + "_" + t1 + "-" + t2 + ".csv";
            pathToCurrentARFFOutputFile = PATH_TO_TESTING_FILES_FOR_WEKA + y + "_" + t1 + "-" + t2 + ".arff";
            builder.buildUnlabeledBagOfFeaturesAndWriteToFile(pathToCurrentCSVInputFile, pathToCurrentARFFOutputFile );
            
            if ( t1 == 22 ){
                t1 = -2;
                y++;
            }    
            
            if ( y == 27 && t1 == 2 ) 
                break;
        }
    }  

    public void buildUnlabeledBagOfFeaturesAndWriteToFile( String inputCSVFileName, String output_ARFF_Filename ) {
    
        RuntimeStatistics.addNewCheckPointValues(); //1

        //create a bag of features for the above text input using a dataset which contains plain text plus 1 class per row
        //parameters: filename, delimiter, count frequencies, include bi-grams, minimumfrequency
        AbstractBagOfFeatures bagOfFeatures = new UnlabeledBagOfFeatures( RELATION , CLASS_ATTRIBUTE_NAME , PATH_TO_INPUT_ARFF_FILE, inputCSVFileName , COUNT_FREQUENCIES , CSV_CONTAINS_HEADER_ROW  );

        RuntimeStatistics.addNewCheckPointValues(); //2        
        
        exportBagToFile( bagOfFeatures, output_ARFF_Filename);

        RuntimeStatistics.addNewCheckPointValues(); //3
        
        RuntimeStatistics.printValuesForAllCheckPoints();                    
    }    
}
