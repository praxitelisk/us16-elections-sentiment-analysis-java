package webmining.machinelearning;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 *
 * @author George Kynigopoulos
 */
public class LabeledBagOfFeatures extends AbstractBagOfFeatures {

    private Map<Integer,String> mapOfClassesIntegerKeysStringValues;
    private Map<String,Integer> mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue;    
    private final String DELIMITER_BETWEEN_FEATURES_AND_CLASS_IN_TOGETHERMAP = " ";

    private int lowestTotalFrequencyPerFeatureInBag_Limit;
    
    // dexetai ws input ena preprocessed file to opoio exei text + delimiter + classValue se ka8e line
    // aparaithto h class ths ka8e grammhs na einai dhlwmenh ws teleutaio pedio ths grammhs
    public LabeledBagOfFeatures(String relation, String classAttributeName, String pathToCSVInputFile, char delimiterBetweenFeaturesAndClassInInputFile, boolean countFrequencies, boolean includeBigrams, int lowestTotalFrequencyPerFeatureInBag_Limit) {    

        super(relation, classAttributeName);     
        
        this.lowestTotalFrequencyPerFeatureInBag_Limit = lowestTotalFrequencyPerFeatureInBag_Limit;
        
        mapOfClassesIntegerKeysStringValues = new LinkedHashMap<>();        

        String currentLine;
        int positionOfClassDelimiterInLine;
        String classOfCurrentLine;
        // o xarakthras mesw tou opoiou xwrizontai ta feature tokens ths line
        Pattern pattern = Pattern.compile(String.valueOf(" "));        
        String[] tokensExceptClassOfCurrentLine;
        String currentWord, previousWord="", currentPartOfSpeech, previousPartOfSpeech="";

        try {

            FileInputStream fileInputStream = new FileInputStream(pathToCSVInputFile) ;
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            String key;
            int linesOfInputFileCounter = 0;

            // 1) enarxh dhmiourgias: mapOfFeaturesStringKeysIntegerValues, mapOfClassesStringKeysIntegerValues, mapOfClassesIntegerKeysStringValues
            // gia ka8e line tou arxeiou eisodou
            while ( ( currentLine = reader.readLine() ) != null ) {

                positionOfClassDelimiterInLine = currentLine.lastIndexOf(delimiterBetweenFeaturesAndClassInInputFile);                    

                // anakthsh olwn twn tokens ths line ektos tou teleutaiou pou einai h klash
                // an den yparxoun ws features ston featuresmap ta pros8etoume k enhmerwnoume to sxetiko metrhth
                // ka8e prosti8emeno feature ston map pairnei ws key to featureName k ws value to featureID tou counter
                tokensExceptClassOfCurrentLine = pattern.split(currentLine.substring(0, positionOfClassDelimiterInLine));
                for (String currentToken : tokensExceptClassOfCurrentLine) {

                    if ( currentToken.equals("") || currentToken.contains(",") )
                        continue;

                    int index = currentToken.lastIndexOf(DELIMITER_BETWEEN_FEATURE_AND_PART_OF_SPEECH);//indexOf
                    currentWord = currentToken.substring(0,index);
                    currentPartOfSpeech = currentToken.substring(index+1);

                    if ( currentPartOfSpeech.startsWith("r") || currentPartOfSpeech.startsWith("j") ){
                        addFeatureToMapIfNotExistsAndUpdateUniqueFeaturesCounter(mapOfFeaturesStringKeysIntegerValues,currentWord);

                        if ( includeBigrams && previousPartOfSpeech.startsWith("r") ) {
                            key = PREFIX_FOR_FEATURES_OF_BIGRAM_TYPE + previousWord + DELIMITER_BETWEEN_FEATURE_AND_PART_OF_SPEECH + currentWord;
                            addFeatureToMapIfNotExistsAndUpdateUniqueFeaturesCounter(mapOfFeaturesStringKeysIntegerValues,key);
                        }
                    }
                    else if( currentPartOfSpeech.startsWith("n") || currentPartOfSpeech.startsWith("v") ||
                            currentPartOfSpeech.startsWith("cd") || currentWord.equals("!") || currentWord.equals("?") ){
                        addFeatureToMapIfNotExistsAndUpdateUniqueFeaturesCounter(mapOfFeaturesStringKeysIntegerValues,currentWord);
                    }                        

                    previousPartOfSpeech = currentPartOfSpeech;
                    previousWord = currentWord;
                }

                // an h class ths trexousas line den yparxei sto mapOfClasses thn pros8etoume mazi me to classID tou counter
                classOfCurrentLine = currentLine.substring( positionOfClassDelimiterInLine + 1 );
                if( !mapOfClassesStringKeysIntegerValues.containsKey( classOfCurrentLine ) ){
                    mapOfClassesStringKeysIntegerValues.put( classOfCurrentLine, uniqueClassesCounterAsClassID );
                    mapOfClassesIntegerKeysStringValues.put( uniqueClassesCounterAsClassID , classOfCurrentLine );
                    uniqueClassesCounterAsClassID++;
                }

                linesOfInputFileCounter++;
            }
            // 1) lhxh dhmiourgias: mapOfFeaturesStringKeysIntegerValues, mapOfClassesStringKeysIntegerValues, mapOfClassesIntegerKeysStringValues

            // 2) enarxh initialization: bagOfFeatures table, me apodosh timhs 0 se ka8e keli
            // + 1 column sto bag gia to attribute ths klashs
            bagOfFeatures = new int[linesOfInputFileCounter][uniqueFeaturesCounterUsedAsFeatureID+1];
            for (int i = 0; i <  bagOfFeatures.length ; i++) {
                for (int j = 0; j < bagOfFeatures[0].length; j++) {
                    bagOfFeatures[i][j] = 0;
                }
            }
            // 2) lhxh initialization: bagOfFeatures table

            mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue = new LinkedHashMap<>();

            // proetoimasia gia deuterh sarwsh tou fileinputstream apo thn arxh
            fileInputStream.getChannel().position(0);
            reader = new BufferedReader(new InputStreamReader(fileInputStream));
            int featureID;                    
            int currentRow = 0;

            // 3) enarxh ypologismou: swstwn timwn gia bagOfFeatures table
            // gia ka8e line tou arxeiou eisodou
            while ( ( currentLine = reader.readLine() ) != null ) {
                positionOfClassDelimiterInLine = currentLine.lastIndexOf(delimiterBetweenFeaturesAndClassInInputFile);

                // pairnei to class ths line eisodou
                String classOfLine = currentLine.substring(positionOfClassDelimiterInLine+1);

                // pairnei ws tokens ta features ths line eisodou
                // kai ekteleitai to loop gia ka8e feature
                tokensExceptClassOfCurrentLine = pattern.split(currentLine.substring(0, positionOfClassDelimiterInLine));
                for ( String currentToken : tokensExceptClassOfCurrentLine ){

                    if ( currentToken.equals("") || currentToken.contains(",") )
                        continue;

                    int index = currentToken.lastIndexOf(DELIMITER_BETWEEN_FEATURE_AND_PART_OF_SPEECH);//indexOf
                    currentWord = currentToken.substring(0,index);
                    currentPartOfSpeech = currentToken.substring(index+1);

                    if( currentPartOfSpeech.startsWith("r") || currentPartOfSpeech.startsWith("j") ){

                        // +1 sth syxnothta tou feature ws aplh lexh sto bag
                        featureID = mapOfFeaturesStringKeysIntegerValues.get(currentWord);
                        if (countFrequencies)
                            bagOfFeatures[currentRow][featureID] += 1; 
                        else
                            bagOfFeatures[currentRow][featureID] = 1;

                        //pros8hkh syndyasmou currentFeature+classOfLine se map, katagrafontas syxnothta emfanishs sto dataset
                        key = currentWord + DELIMITER_BETWEEN_FEATURES_AND_CLASS_IN_TOGETHERMAP + classOfLine;
                        if ( mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue.containsKey(key) )
                            mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue.put(key, mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue.get(key) + 1 );
                        else
                            mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue.put(key, 1 );                                    

                        if( includeBigrams && previousPartOfSpeech.startsWith("r") ){

                            // +1 sth syxnothta tou feature ws bigram (h aplh lexh + thn prohgoumenh ths sth line) sto bag
                            featureID = mapOfFeaturesStringKeysIntegerValues.get(PREFIX_FOR_FEATURES_OF_BIGRAM_TYPE + previousWord + DELIMITER_BETWEEN_FEATURE_AND_PART_OF_SPEECH + currentWord );
                            if (countFrequencies)
                                bagOfFeatures[currentRow][featureID] += 1;
                            else
                                bagOfFeatures[currentRow][featureID] = 1;

                            //pros8hkh syndyasmou currentBigram+classOfLine se map, katagrafontas syxnothta emfanishs sto dataset
                            key = PREFIX_FOR_FEATURES_OF_BIGRAM_TYPE + previousWord + DELIMITER_BETWEEN_FEATURE_AND_PART_OF_SPEECH + currentWord + DELIMITER_BETWEEN_FEATURES_AND_CLASS_IN_TOGETHERMAP + classOfLine;
                            if ( mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue.containsKey(key) )
                                mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue.put(key, mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue.get(key) + 1 );
                            else
                                mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue.put(key, 1 );
                        }
                    }
                    else if( currentPartOfSpeech.startsWith("n") || currentPartOfSpeech.startsWith("v") || currentPartOfSpeech.startsWith("cd") || currentWord.equals("!") || currentWord.equals("?") ){

                        // +1 sth syxnothta tou feature ws aplh lexh sto bag
                        featureID = mapOfFeaturesStringKeysIntegerValues.get(currentWord);
                        if (countFrequencies)
                            bagOfFeatures[currentRow][featureID] += 1;
                        else
                            bagOfFeatures[currentRow][featureID] = 1;

                        //pros8hkh syndyasmou currentFeature+classOfLine se map, katagrafontas syxnothta emfanishs sto dataset
                        key = currentWord + DELIMITER_BETWEEN_FEATURES_AND_CLASS_IN_TOGETHERMAP + classOfLine;
                        if ( mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue.containsKey(key) )
                            mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue.put(key, mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue.get(key) + 1 );
                        else
                            mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue.put(key, 1 );
                    }

                    previousPartOfSpeech = currentPartOfSpeech;
                    previousWord = currentWord;
                }

                // ana8esh timhs sto teleutaio keli(column) ths trexousas row,
                bagOfFeatures[currentRow][bagOfFeatures[0].length-1] = mapOfClassesStringKeysIntegerValues.get(classOfLine);                    

                currentRow++;
            }
            // 3) lhxh ypologismou: swstwn timwn gia bagOfFeatures table

            reader.close();   
            fileInputStream.close();
        }
        catch (IOException exception) { System.out.println(exception); }            
    }      

    private void addFeatureToMapIfNotExistsAndUpdateUniqueFeaturesCounter(Map map, String feature){
        
        if( !map.containsKey( feature ) ){
            map.put(feature, uniqueFeaturesCounterUsedAsFeatureID );
            uniqueFeaturesCounterUsedAsFeatureID++;
        }
    }         
    
    @Override
    final boolean shouldFeatureBeIncludedInBag(String feature){
        
        Iterator<String> iterator = mapOfClassesStringKeysIntegerValues.keySet().iterator();        
        while( iterator.hasNext() ){            
            String currentClassName = iterator.next();            
            String key = feature + DELIMITER_BETWEEN_FEATURES_AND_CLASS_IN_TOGETHERMAP + currentClassName;
            if ( mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue.containsKey(key) && 
                 mapOfFeatureAndClassTogetherAsKeyFrequencyAsValue.get(key) >= lowestTotalFrequencyPerFeatureInBag_Limit )
                return true;
        }
        
        return false;
    }

    @Override
    String getClassValueForDataRow(int row){
        
        // entopise to column tou bag pou diathrei tis times gia to class attribute
        int classAtributeColumn = bagOfFeatures[row].length-1;
        
        // anakthse to ID tou class attribute gia to sygkekrimeno row
        int classID = bagOfFeatures[row][classAtributeColumn];          

        // anakthse to class name/value gia to class ID kai epestrepse to
        return mapOfClassesIntegerKeysStringValues.get(classID);        
    }    
}