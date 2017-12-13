package webmining.textprocessing;

import java.util.Map;

/**
 *
 * @author George Kynigopoulos
 */
public class ValidWordChecker extends AbstractChecker {

    
    private static final String PATH_TO_CSV_FILE_CONTAINING_VALID_WORDS_LIST = BASE_PATH + "0wordLists//ValidWords.csv";
    private static final Map<String,Integer> WORD_LIST = initializeMap();
    private static final ValidWordChecker INSTANCE = new ValidWordChecker();
    
    private ValidWordChecker(){}    
    
    public static ValidWordChecker getInstance() {
        
        return INSTANCE;
    }   
    
    public static boolean isTextAValidWord(String givenText){
        
        return WORD_LIST.containsKey(givenText);
    }
    
    private static Map initializeMap(){

        System.out.println("initializing wordlist checker");        
        
        return initializeMap(PATH_TO_CSV_FILE_CONTAINING_VALID_WORDS_LIST);
    }
}
