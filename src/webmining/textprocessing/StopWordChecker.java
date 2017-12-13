package webmining.textprocessing;

import java.util.Map;

/**
 *
 * @author George Kynigopoulos
 */
public class StopWordChecker extends AbstractChecker {

    private static final String PATH_TO_CSV_FILE_CONTAINING_STOP_WORDS_LIST = BASE_PATH + "0wordLists//StopWords.csv";
    private static final Map<String,Integer> STOP_WORDS_LIST = initializeMap();
    private static final StopWordChecker INSTANCE = new StopWordChecker();
    
    private StopWordChecker(){}    
    
    public static StopWordChecker getInstance() {
        
        return INSTANCE;
    }     
    
    public static boolean isTextAStopWord(String givenText){
        
        return STOP_WORDS_LIST.containsKey(givenText);
    }
    
    private static Map initializeMap(){
        
        System.out.println("initializing stopwords checker");        
        
        return initializeMap(PATH_TO_CSV_FILE_CONTAINING_STOP_WORDS_LIST);
    }
}