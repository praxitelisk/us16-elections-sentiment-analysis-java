package webmining.topicmodeling;

import java.util.LinkedHashMap;
import java.util.Set;

public class Topic {
    
    private int timeWindowOfTopic;
    private LinkedHashMap<String,Integer> wordsOfTopic;
    private double frequencyOfTopic;

    public Topic(int timeWindowOfTopic,double frequencyOfTopic){        
        this.timeWindowOfTopic = timeWindowOfTopic;
        wordsOfTopic = new LinkedHashMap<>();
        this.frequencyOfTopic = frequencyOfTopic;
    }
            
    public void setWord(String wordOfTopic, int frequencyOfWord){
        
        wordsOfTopic.put(wordOfTopic, frequencyOfWord);
    }
    
    public boolean getWord(String givenWord){
              
        return wordsOfTopic.containsKey(givenWord);
    }

    public Set<String> getWords(){
        
        return wordsOfTopic.keySet();
    }

    public int getSize(){
		return wordsOfTopic.size();
    	
    }
      
    public int getFrequency(String givenWord){
              
        if (wordsOfTopic.containsKey(givenWord))
            return wordsOfTopic.get(givenWord);
        else 
            return -1;
    }  
    
}
