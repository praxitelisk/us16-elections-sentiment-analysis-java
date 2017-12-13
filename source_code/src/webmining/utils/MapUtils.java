package webmining.utils;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Iterator;

/**
 * 
 * @author George Kynigopoulos
 */
public class MapUtils {

    public static LinkedHashMap<String, Integer> sortMapByValues(Map<String, Integer> passedMap) {
    
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Integer> mapValues = new ArrayList<>(passedMap.values());
    
        Collections.sort(mapKeys);
        Collections.sort(mapValues, Collections.reverseOrder());
    
        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();

        Iterator<Integer> valueIt = mapValues.iterator();
        
        while (valueIt.hasNext()) {
        
            Integer val = valueIt.next();
        
            Iterator<String> keyIt = mapKeys.iterator();
        
            while (keyIt.hasNext()) {
            
                String key = keyIt.next();
            
                Integer comp1 = passedMap.get(key);            
                Integer comp2 = val;

                if (comp1.equals(comp2)) {

                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }  
        return sortedMap;
    }

    public static void writeMapToFile( Map<String,Integer> map, String filename ){

        try ( BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(filename)) ) ) {

            Iterator<String> iterator = map.keySet().iterator();

            while ( iterator.hasNext() ){

                String key = iterator.next();
                Integer value = map.get(key);

                writer.write( key + " " + value + '\n' );
            }
            
            writer.close();

        }        
        catch (FileNotFoundException ex) { System.out.println(ex); }
        catch (IOException ex) { System.out.println(ex); }            
    }

}
