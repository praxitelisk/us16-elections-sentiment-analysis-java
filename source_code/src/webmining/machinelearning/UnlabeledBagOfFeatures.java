package webmining.machinelearning;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 *
 * @author George Kynigopoulos
 */
public class UnlabeledBagOfFeatures  extends AbstractBagOfFeatures {  
    
    public UnlabeledBagOfFeatures(String relation, String classAtributeName, String pathToExistingArffTrainingFile, String pathToCSVInputFile, boolean countFrequencies, boolean csvContainsHeaderRow) {

        super(relation, classAtributeName);       
        
        String currentLine, previousLine = null;
        Pattern pattern;
        String[] tokens;

        try {
            
            FileInputStream fileInputStream = new FileInputStream(pathToExistingArffTrainingFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            
            reader.readLine();
            //currentLine = reader.readLine();
            //int spaceIndex = currentLine.indexOf(" ");
            //relation = currentLine.substring(spaceIndex+1);           
            
            reader.readLine();

            pattern = Pattern.compile(String.valueOf(" "));             
            while ( ( currentLine = reader.readLine() ) != null ) {
                tokens = pattern.split(currentLine);
                
                if ( tokens[0].equals("@ATTRIBUTE") )
                    mapOfFeaturesStringKeysIntegerValues.put(tokens[1] , uniqueFeaturesCounterUsedAsFeatureID++ );
                else
                    break;
                
                previousLine = currentLine;
            }
            
            reader.close();
            fileInputStream.close();
            
            tokens = pattern.split(previousLine);
           
            classAttributeName = tokens[1];            
            mapOfFeaturesStringKeysIntegerValues.remove(classAttributeName, --uniqueFeaturesCounterUsedAsFeatureID);

            String classes = tokens[2].substring( 1 , tokens[2].length()-1 );      
            
            pattern = Pattern.compile(String.valueOf(","));
            tokens = pattern.split(classes);            
            for ( String token : tokens ){
                mapOfClassesStringKeysIntegerValues.put(token, uniqueClassesCounterAsClassID++);
            }
            
            // telos anakthshs features + classes apo to training arff

            fileInputStream = new FileInputStream(pathToCSVInputFile) ;
            reader = new BufferedReader(new InputStreamReader(fileInputStream));
            
            if(csvContainsHeaderRow)
                reader.readLine();

            int linesOfInputFileCounter = 0;            
            while ( ( reader.readLine() ) != null ) {
                linesOfInputFileCounter++;
            }            
            
            bagOfFeatures = new int[linesOfInputFileCounter][uniqueFeaturesCounterUsedAsFeatureID+1];
            for (int i = 0; i <  bagOfFeatures.length ; i++) {
                for (int j = 0; j < bagOfFeatures[0].length; j++) {
                    bagOfFeatures[i][j] = 0;
                }
            }

            //System.out.println(linesOfInputFileCounter);    
            
            fileInputStream.getChannel().position(0);
            reader = new BufferedReader(new InputStreamReader(fileInputStream));
            int featureID;                    
            int currentRow = 0;
            pattern = Pattern.compile(String.valueOf(" ")); 
            
            if(csvContainsHeaderRow)
                reader.readLine();            
            
            String previousToken="";
            
            while ( ( currentLine = reader.readLine() ) != null ) {
                int index = currentLine.indexOf(",");
                String text = currentLine.substring(index+1);

                text = text.replaceAll("[,\"]", "")
                           .replaceAll("[.]", "")
                           .replaceAll("  ", " ");
               
                tokens = pattern.split(text);                
                for ( String token : tokens ){

                    //System.out.println(token);
                    
                    if ( mapOfFeaturesStringKeysIntegerValues.containsKey(token) || 
                         mapOfFeaturesStringKeysIntegerValues.containsKey( PREFIX_FOR_FEATURES_OF_BIGRAM_TYPE + previousToken + DELIMITER_BETWEEN_FEATURE_AND_PART_OF_SPEECH + token ) 
                       ){                        
                            featureID = mapOfFeaturesStringKeysIntegerValues.get(token);
                            if (countFrequencies)
                                bagOfFeatures[currentRow][featureID] += 1; 
                            else
                                bagOfFeatures[currentRow][featureID] = 1;   
                    }
                    
                    previousToken = token;
                }

                currentRow++;
            }
            
            reader.close(); 
            fileInputStream.close();            
        }
        catch (IOException exception) { System.out.println(exception); }         
    }    
    
    @Override
    final boolean shouldFeatureBeIncludedInBag(String feature){

        return true;
    }    

    @Override
    String getClassValueForDataRow(int row){

        return "?";        
    } 
}