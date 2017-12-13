package webmining.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * we utilize this class in programs to collect and print information about their memory usage and execution time
 * @author George Kynigopoulos
 */
public class RuntimeStatistics {
    
    private static final long MEGABYTE = 1024 * 1024;
    private static final Runtime RUNTIME = Runtime.getRuntime(); 
    private static final List<Long> RAM_CHECKPOINTS = new ArrayList<>();
    private static final List<String> TIME_CHECKPOINTS = new ArrayList<>();   
    private static final RuntimeStatistics INSTANCE = new RuntimeStatistics();

    private RuntimeStatistics(){}

    public static RuntimeStatistics getInstance() {
        return INSTANCE;
    }
    
    public static void addNewCheckPointValues(){
        
            RAM_CHECKPOINTS.add((RUNTIME.totalMemory() - RUNTIME.freeMemory()) / MEGABYTE ) ;
            TIME_CHECKPOINTS.add( LocalDateTime.now().format( DateTimeFormatter.ofPattern("dd/MM/yyyy, hh:mm:ss:SSS") ) ) ;        
    }
    
    public static void printValuesForAllCheckPoints(){
        
        System.out.println( "\n\nPrinting runtime/memory information:" );
        
        Iterator<Long> ramIterator = RAM_CHECKPOINTS.iterator();
        Iterator<String> timeIterator = TIME_CHECKPOINTS.iterator();
        
        int counter = 1;        
        
        while (ramIterator.hasNext()){   
            System.out.println( "Checkpoint " + counter + ", Timestamp:" + timeIterator.next() + ", RAM usage:" + ramIterator.next() + "MB" );
            counter++;
        }
    }    
}