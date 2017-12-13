package webmining.textprocessing;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 *
 * @author George Kynigopoulos
 */
public abstract class AbstractChecker {   

    protected static final String BASE_PATH = "data//";      
    
    protected static Map initializeMap(String pathToCSVFileContainingListOfWords){
        
        Map<String,Integer> map = new HashMap<>();
        
        try ( FileInputStream fileInputStream = new FileInputStream(pathToCSVFileContainingListOfWords) ) {

            try ( BufferedReader reader = new BufferedReader( new InputStreamReader(fileInputStream) ) ) {
                
                Stream<String> lines = reader.lines();
                
                lines
                    //.parallel()
                    .forEach( (line) -> {

                        map.put(line, 0 );
                    });              
            }
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex);
        }
        catch (IOException ex) {
            System.out.println(ex);
        }
        
        return map;
    }
    
}
