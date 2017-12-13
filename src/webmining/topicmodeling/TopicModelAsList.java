package webmining.topicmodeling;

import java.util.ArrayList;
import java.util.List;

public class TopicModelAsList {
    
    private int timePeriods;
    private List<List<Topic>> fullModel;
    
    public TopicModelAsList(int timePeriods){
        
        this.timePeriods = timePeriods;

        fullModel = new ArrayList<>();

        for ( int timePeriod = 0; timePeriod < timePeriods ; timePeriod++ )
            
            fullModel.add( timePeriod , new ArrayList<>() );
    }
    
    public void setTopic (int timePeriod, Topic topic){

        List<Topic> topicsOfTimePeriod = fullModel.get(timePeriod);
        topicsOfTimePeriod.add(topic);
    }
    
    public List<Topic> getTopics(int timePeriod){
        
        return fullModel.get(timePeriod);
    }
    public int getNumberOfTimePeriods(){
	
        return timePeriods;
    }
            
}
