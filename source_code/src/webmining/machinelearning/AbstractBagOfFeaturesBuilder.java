package webmining.machinelearning;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author George Kynigopoulos
 */
public abstract class AbstractBagOfFeaturesBuilder {
    
    protected void exportBagToFile(AbstractBagOfFeatures bagOfFeatures, String outputFilename){
        
        int rowsOfBagOfFeaturesTable = bagOfFeatures.getRowsSize();
        
        //writer to a dataset which contains features+class and frequencies per row
        try ( BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outputFilename)) ) ) {
            writer.write(bagOfFeatures.getARFFHeader() );                
            writer.write("\n\n");
            writer.write("@DATA");
            writer.write("\n");
            for ( int rowIndex = 0; rowIndex < rowsOfBagOfFeaturesTable-1 ; rowIndex++ ){
                writer.write(bagOfFeatures.getDataRow(rowIndex) + '\n' );
            }
            writer.write(bagOfFeatures.getDataRow(rowsOfBagOfFeaturesTable-1) );
            writer.close();
        }
        catch (IOException ex) { System.out.println(ex); }          
    }    
}
