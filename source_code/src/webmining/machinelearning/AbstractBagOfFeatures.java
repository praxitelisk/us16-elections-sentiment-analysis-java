package webmining.machinelearning;

import java.util.Iterator;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 *
 * @author George Kynigopoulos
 */
public abstract class AbstractBagOfFeatures {
   
    Map<String,Integer> mapOfFeaturesStringKeysIntegerValues;
    Map<String,Integer> mapOfClassesStringKeysIntegerValues;
    int uniqueFeaturesCounterUsedAsFeatureID = 0; 
    int uniqueClassesCounterAsClassID = 0;

    String relation; 
    String classAttributeName; 
    final String PREFIX_FOR_FEATURES_OF_BIGRAM_TYPE = "_bi-gram_";
    final String DELIMITER_BETWEEN_FEATURE_AND_PART_OF_SPEECH = "_";
    
    // N columns: N-1 features + 1 last column for class
    // X rows, each row is a features+class based representation of text (e.g. tweet)
    int[][] bagOfFeatures;
    
    AbstractBagOfFeatures(String relation, String classAttributeName){
        mapOfFeaturesStringKeysIntegerValues = new LinkedHashMap<>();
        mapOfClassesStringKeysIntegerValues = new LinkedHashMap<>(); 
        this.relation = relation;
        this.classAttributeName = classAttributeName;
    }
   
    abstract boolean shouldFeatureBeIncludedInBag(String feature);

    abstract String getClassValueForDataRow(int row);
    
    public final int getRowsSize(){
        return bagOfFeatures.length;
    }
    
    public final String getARFFHeader(){

        Iterator<String> iterator = mapOfFeaturesStringKeysIntegerValues.keySet().iterator();               
        StringBuilder stringBuilder = new StringBuilder();

        // start building the ARFF header                
        stringBuilder.append("@RELATION ")
                     .append(relation)
                     .append("\n\n");
        
        // start building lines for feature attributes        
        while ( iterator.hasNext() ){            
            String feature = iterator.next();
            
            if ( shouldFeatureBeIncludedInBag(feature) )
                stringBuilder.append("@ATTRIBUTE ")
                             .append( feature )
                             .append(" NUMERIC\n");    
        }
        // end building lines for feature attributes        
        
        // start building a line for class attribute
        stringBuilder.append("@ATTRIBUTE ")
                     .append( classAttributeName )
                     .append(" {");

        // add class categories/values
        iterator = mapOfClassesStringKeysIntegerValues.keySet().iterator();                
        while ( iterator.hasNext() ){
            stringBuilder.append( iterator.next() )
                         .append(",");
        }
        
        // remove last comma symbol within stringbuilder and
        // add a closing bracket for the class attribute
        // before returning the string containing the whole ARFF header
        return stringBuilder.substring(0, stringBuilder.lastIndexOf(",")) + "}";
    }    
    
    public final String getDataRow(int row){
        
        Iterator<String> iterator = mapOfFeaturesStringKeysIntegerValues.keySet().iterator();
        StringBuilder stringBuilder = new StringBuilder();
        
        int featureID;
        
        //for every feature in one raw of the bag
        while ( iterator.hasNext() ){            
            String feature = iterator.next();
            
            // if feature satisfies condition to be part of the bag, add it as a feature to the raw of the bag
            if ( shouldFeatureBeIncludedInBag(feature) ){
                featureID = mapOfFeaturesStringKeysIntegerValues.get(feature);
                stringBuilder.append( bagOfFeatures[row][featureID] )
                             .append(',');
            }
        }

        // add to name/value tou class attribute gia to sygkekrimeno row tou bag ston builder
        stringBuilder.append( getClassValueForDataRow(row) );

        return stringBuilder.toString();
    }

    public final void printFeatureNames(){
        
        Iterator<String> iterator = mapOfFeaturesStringKeysIntegerValues.keySet().iterator();         
        while ( iterator.hasNext() ){
            System.out.println( iterator.next() );
        }
    }    
}