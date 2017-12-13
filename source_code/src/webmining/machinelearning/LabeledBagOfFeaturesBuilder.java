package webmining.machinelearning;

import webmining.textprocessing.TextPreprocessor;
import webmining.utils.RuntimeStatistics;

/**
 *
 * @author George Kynigopoulos
 */
public class LabeledBagOfFeaturesBuilder extends AbstractBagOfFeaturesBuilder {

    // we only need to change some of these variables to execute the builder with diferrent configuration settings
    private static final boolean PREPROCESS_INPUT_FILE_BEFORE_BAG_CREATION = false;

    private static final String BASE_PATH = "data//";
    private static final String PATH_TO_UNPROCESSED_CSV_FILE = BASE_PATH + "1annotatedDataset//AnnotatedDataset.csv";
    private static final String PATH_TO_PREPROCESSED_CSV_FILE = BASE_PATH + "2preprocessedDataset//AnnotatedDatasetPreprocessed.csv";

    private static final boolean COUNT_FREQUENCIES = true;
    private static final boolean INCLUDE_BIGRAMS = true;
    private static final int LOWEST_TOTAL_FREQUENCY_PER_FEATURE = 10;    
    private static final String PATH_TO_TRAINING_ARFF_FILE = BASE_PATH + "3trainingFilesForWEKA//BagOfFeatures_CountFrequencies" + String.valueOf(COUNT_FREQUENCIES).toUpperCase() + "_IncludeBigrams" + String.valueOf(INCLUDE_BIGRAMS).toUpperCase() + "_LowestTotalFrequencyPerFeature" + LOWEST_TOTAL_FREQUENCY_PER_FEATURE + ".arff";

    private static final String RELATION = "US_ELECTIONS";
    private static final String CLASS_ATTRIBUTE_NAME = "EMOTION_CLASS";
    
    public static void main(String[] args) {
        
        RuntimeStatistics.addNewCheckPointValues(); //1        
        
        if( PREPROCESS_INPUT_FILE_BEFORE_BAG_CREATION )
            TextPreprocessor.preprocess( PATH_TO_UNPROCESSED_CSV_FILE , PATH_TO_PREPROCESSED_CSV_FILE , true );

        RuntimeStatistics.addNewCheckPointValues(); //2        
        
        //create a bag of features for the above text input using a dataset which contains plain text plus 1 class per row
        //parameters: filename, delimiter, count frequencies, include bi-grams, minimumfrequency
        AbstractBagOfFeatures bagOfFeatures = new LabeledBagOfFeatures( RELATION , CLASS_ATTRIBUTE_NAME , PATH_TO_PREPROCESSED_CSV_FILE, ',' , COUNT_FREQUENCIES , INCLUDE_BIGRAMS, LOWEST_TOTAL_FREQUENCY_PER_FEATURE );
        
        RuntimeStatistics.addNewCheckPointValues(); //3            

        LabeledBagOfFeaturesBuilder builder = new LabeledBagOfFeaturesBuilder();                
        builder.exportBagToFile(bagOfFeatures, PATH_TO_TRAINING_ARFF_FILE);      

        RuntimeStatistics.addNewCheckPointValues(); //4
        
        RuntimeStatistics.printValuesForAllCheckPoints(); 
    }    
}