package webmining.machinelearning;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import webmining.utils.RuntimeStatistics;

/**
 *
 * @author George Kynigopoulos
 */
public class EmotionsAnalyzer {

    private static final String BASE_PATH = "data//";    
    private static final String PATH_TO_CLASSIFICATION_RESULTS_FROM_WEKA = BASE_PATH + "6classificationResultsFromWEKA/2016_5_";
    private static final String PATH_TO_AFFECTIVE_ANALYSIS = BASE_PATH + "7affectiveAnalysis//affective_analysis_results.csv";     

    private static final String[] EMOTIONS = {"neutral","anger","anxiety","calm","disgust","enthusia","fear","interest","joy","nervous","rejection","sadness","shame","surprise"};
    private static final boolean CSV_CONTAINS_HEADER_ROW = true;        

    private static String pathToCurrentCSVInputFile;
    
    public static void main(String[] args) {
        
        EmotionsAnalyzer analyzer = new EmotionsAnalyzer();
        
        try {        
            
            BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(PATH_TO_AFFECTIVE_ANALYSIS)) ); 
            writer.write("Affective analysis results:");
            writer.write("\n\n");          
            writer.write("Timeframe,Emotion,Frequency\n");            

            int y = 24;
            int t1, t2;

            for ( t1 = 0 ; t1 < 24 ; t1+=2 ){

                if ( (t1 == 0 || t1 == 2) && y == 24 )
                    continue;

                t2 = t1 + 2;
                
                if( t2 == 24 )
                    t2 = 0;
                
                pathToCurrentCSVInputFile = PATH_TO_CLASSIFICATION_RESULTS_FROM_WEKA + y + "_" + t1 + "-" + t2 + ".csv";
                analyzer.analyzeEmotionsPerTimeframe(pathToCurrentCSVInputFile , writer , "2016_5_" + y + "_" + t1 + "-" + t2 );

                if ( t1 == 22 ){
                    t1 = -2;
                    y++;
                }    

                if ( y == 27 && t1 == 2 ) 
                    break;
            }
        
            writer.close();                        
        }
        catch (IOException ex) { System.out.println(ex); }            
    }
    
    public void analyzeEmotionsPerTimeframe( String inputCSVFilename , BufferedWriter writer , String timeframe ){
        
        try {
            
            RuntimeStatistics.addNewCheckPointValues(); //1           

            int[] emotionFrequencies = new int[14];
            for ( int emotion : emotionFrequencies )
                emotion = 0;

            FileInputStream fileInputStream = new FileInputStream(inputCSVFilename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));  
            
            String currentLine;
            
            if(CSV_CONTAINS_HEADER_ROW)
                reader.readLine();
            
            while ( ( currentLine = reader.readLine() ) != null ) {
                
                for ( int x = 0; x < EMOTIONS.length ; x++ ){
                    if( currentLine.matches("(.*)"+EMOTIONS[x]+"(.*)") ){
                        emotionFrequencies[x] += 1;
                        break;
                    }
                }                
            }

            reader.close();
            fileInputStream.close();                       
            
            RuntimeStatistics.addNewCheckPointValues(); //2           

            for ( int i = 0; i < EMOTIONS.length ; i++ )
                writer.write(timeframe + "," + EMOTIONS[i] + "," + emotionFrequencies[i] + "\n" );
            
            RuntimeStatistics.addNewCheckPointValues(); //3

        }
        catch (IOException ex) { System.out.println(ex); }                 
    }    
}