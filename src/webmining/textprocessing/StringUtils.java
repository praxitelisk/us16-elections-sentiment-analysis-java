package webmining.textprocessing;

import java.util.List;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

/**
 *
 * @author George Kynigopoulos
 */
public class StringUtils {
        
    private static final StringUtils INSTANCE = new StringUtils(); 
    
    private StringUtils(){}

    public static StringUtils getInstance() {
        
        return INSTANCE;
    }

    public static List<Sentence> doDefaultPreprocessing(String documentText, boolean keepMentionedAndRetweetedNames){

        // diagrafoume entelws to &amp; argotera de ginetai giati 8a exoun vgei ektos ta symbols
        documentText = documentText.replaceAll("&amp;", "");

        // diathroume mono mia emfanish an yparxoun polles synexomenes twn ! or ?
        documentText = documentText.replaceAll("[!][!]+", "!");
        documentText = documentText.replaceAll("[?][?]+", "?");
        
        //keep valence shifter before removing apostrophe symbols from text
        documentText = documentText.replaceAll("n't ","not "); 
        
        // kratame sto keimeno mono latinika grammata, ta symbols !?,./:;)(|- kai kena spaces
        // ta ! ? ta xreiazomaste apo thn ekfwnhsh san features
        // ta , . kai ta kena spaces ta xreiazomaste gia na katalavei tis sentences kai ta lemmata o nlp parser
        // ta . : / ta xreiazomaste gia na ginei swsta pio meta to URL links cleaning
        // ta : ; ) ( |  ta xreiazomaste gia na vroume emoticons argotera
        // ta @ : ta xreiazomaste gia na ginei swsta pio meta to retweets kai mentions cleaning
        documentText = documentText.replaceAll("[^a-zA-z!?,./:;)(|\\-@ ]", "");

        //extra cleaning
        documentText = documentText.replaceAll("[_]", "");
        documentText = documentText.replaceAll("[`]", "");
        documentText = documentText.replaceAll("\\^", "");
        
        //epeidh o nlp parser exei provlhma sto lemmatization otan vriskei ( )
        //vriskoume twra pou yparxoun 8 eidh emoticons k ta antika8istoume me epilegmenous ari8mous ws kwdikous
        //pou einai ok ston nlp parser k den yparxoun sto text afou exoun hdh filtraristei
        // :)  -> 1
        // :(  -> 2
        // :p  -> 3
        // :d  -> 4
        // :-) -> 5
        // ;-) -> 6
        // ;)  -> 7
        // :|  -> 8        
        documentText = documentText.replaceAll(":[)]", "1");
        documentText = documentText.replaceAll(":[(]", "2");
        documentText = documentText.replaceAll(":p", "3");
        documentText = documentText.replaceAll(":d", "4");  
        documentText = documentText.replaceAll(":[-][)]", "5");  
        documentText = documentText.replaceAll(";[-][)]", "6");  
        documentText = documentText.replaceAll(";[)]", "7");
        documentText = documentText.replaceAll(":[|]", "8");

        // ka8arizoume twra to text apo perittous xarakthres pou de mas xreiazontai pia alla tous 8elame mono gia ta emoticons
        // reminder, eixame krathsei pio panw ta symbols !? ,. /: ;)(|-@
        documentText = documentText.replaceAll(";", "");
        documentText = documentText.replaceAll("[)]", "");
        documentText = documentText.replaceAll("[)]", "");
        documentText = documentText.replaceAll("[|]", "");
        documentText = documentText.replaceAll("\\-", "");
      
        // kratame apo edw k meta mono (pera apo latinikous xarakthres kai spaces)
        // !? gia machine learning 
        // ,. gia sentences 
        // @ : gia retweets/mentions
        // / : gia URLs
        
        if(keepMentionedAndRetweetedNames){
            documentText = documentText.replaceAll("rt @([\\w]+):", "$1");
            documentText = documentText.replaceAll("RT @([\\w]+):", "$1");
            documentText = documentText.replaceAll("via @([\\w]+)", "$1");
            documentText = documentText.replaceAll("@([\\w]+)", "$1");            
        }
        else{
            // taking care of both uppercase and lowercase input text
            // vgazoume ektos ta retweets kai ta mentions
            documentText = documentText.replaceAll("rt @([\\w]+):", "");
            documentText = documentText.replaceAll("RT @([\\w]+):", "");
            documentText = documentText.replaceAll("via @([\\w]+)", "");
            documentText = documentText.replaceAll("@([\\w]+)", "");            
        }

        // kovoume ta @ opou exoun apomeinei, de ta xreiazomaste pleon
        documentText = documentText.replaceAll("@", "");

        // kovoume ta symbols / : opote opoiodhpote URL yparxei enopoieitai to link se ena string me latin characters
        documentText = documentText.replaceAll("/", "");
        documentText = documentText.replaceAll(":", "");

        // kratame apo edw k meta ws symbols mono !? gia machine learning kai ,. gia sentences 
        // syn latinikous xarakthres, ari8mous (pou symvolizoun emoticons) kai spaces
        // a-zA-Z0-9!?,.
        
        Document document = new Document( documentText );
        List<Sentence> sentences = document.sentences();
        
        return sentences;
    }

    public static String getTextPreprocessedWithoutPOSTags(String documentText, boolean keepMentionedAndRetweetedNames){

        StringBuilder builder = new StringBuilder();

        List<Sentence> sentences = doDefaultPreprocessing(documentText,keepMentionedAndRetweetedNames);

        sentences
                //.parallelStream()
                .forEach( (sentence) -> {

                    List<String> lemmas = sentence.lemmas();
                    
                    lemmas
                            .stream() //.parallelStream()
                            .filter( (lemma) -> ( !StopWordChecker.isTextAStopWord(lemma) && 
                                                  !containsURL(lemma) //&&
                                                  // ( keepMentionedAnRetweetedNames || WordListChecker.isTextAValidWord(lemma) || lemma.matches("[0-9!?]") )
                                                )
                                    )
                            .map( (lemma) -> removeMultipleCharacterOccurrencesFromWord(lemma) )
                            .forEach( (lemma) -> { 
                                builder.append(lemma).append(" "); 
                            });
                });
        
        return builder.toString().trim();  //return builder.toString().trim().replaceAll("[,.]", "");        
    }

    public static String getTextPreprocessedWithPOSTags(String documentText, boolean keepMentionedAndRetweetedNames){   
        
        StringBuilder builder = new StringBuilder();

        List<Sentence> sentences = doDefaultPreprocessing(documentText,keepMentionedAndRetweetedNames);

        sentences
                //.parallelStream()
                .forEach( (sentence) -> {

                    List<String> lemmas = sentence.lemmas();
                    
                    int currentWordInSentenceCounter = 0;
                    
                    for ( String lemma : lemmas ){
                        
                        if( !StopWordChecker.isTextAStopWord(lemma) &&
                            !containsURL(lemma) //&&
                            // ( keepMentionedAnRetweetedNames || WordListChecker.isTextAValidWord(lemma) || lemma.matches("[0-9!?]") )
                        ){
                            lemma = removeMultipleCharacterOccurrencesFromWord(lemma);                            
                            builder.append(lemma).append("_").append(sentence.posTag(currentWordInSentenceCounter)).append(" ");
                        }  
                        
                        currentWordInSentenceCounter++;
                    }
                });

        return builder.toString().trim();  //return builder.toString().trim().replaceAll("[,.]", "");       
    }     

    public static boolean containsURL(String givenText){
        
        if ( givenText == null ) return false;

        return givenText.contains("http");
    }

    public static String removeMultipleCharacterOccurrencesFromWord(String givenToken){
        
        if( givenToken.length() < 2 )
            return givenToken;
        
        char[] stringCharacters = givenToken.toCharArray();
        
        StringBuilder builder = new StringBuilder();

        // xekiname na ftiaxoume ena string apo to givenToken to opoio na exei max 2 diadoxikes emfaniseis tou idiou char
        
        for ( int i = 0 ; i < stringCharacters.length-2 ; i++ ){
            
            if ( ! ( stringCharacters[i] == stringCharacters[i+1] && stringCharacters[i+1] == stringCharacters[i+2] ) )

                builder.append(stringCharacters[i]);
        }

        builder.append( stringCharacters[stringCharacters.length-2] );
        builder.append( stringCharacters[stringCharacters.length-1] );

        givenToken = builder.toString();    

        // apo edw kai katw to givenToken exei max 2 diadoxikes emfaniseis tou idiou char
        
        String temp;

        // elegxoi an to givenToken exei se 3 shmeia tou 2ples diadoxikes emfaniseis xarakthrwn
        // px estw oti einai ffunnyy
        // psaxnoume mesw regular expressions na vroume se dictionary ena substring tou ffunnyy to opoio na einai valid
        // an vre8ei epistrefoume auto ws lexh enanti tou ffunnyy, diaforetika epistrefoume to arxiko
        // ka8ws mporei na htan token pou den yparxei se dictionary alla einai xrhsimo, px abbreviation opws lol
        
        temp = givenToken.replaceAll("(\\w)\\1(\\w)\\2(\\w)\\3", "$1$1$2$2$3");
        if ( ValidWordChecker.getInstance().isTextAValidWord(temp) ) return temp;
        
        temp = givenToken.replaceAll("(\\w)\\1(\\w)\\2(\\w)\\3", "$1$1$2$3$3");
        if ( ValidWordChecker.getInstance().isTextAValidWord(temp) ) return temp;
        
        temp = givenToken.replaceAll("(\\w)\\1(\\w)\\2(\\w)\\3", "$1$2$2$3$3");
        if ( ValidWordChecker.getInstance().isTextAValidWord(temp) ) return temp;
        
        temp = givenToken.replaceAll("(\\w)\\1(\\w)\\2(\\w)\\3", "$1$1$2$3");
        if ( ValidWordChecker.getInstance().isTextAValidWord(temp) ) return temp;
        
        temp = givenToken.replaceAll("(\\w)\\1(\\w)\\2(\\w)\\3", "$1$2$2$3");
        if ( ValidWordChecker.getInstance().isTextAValidWord(temp) ) return temp;

        temp = givenToken.replaceAll("(\\w)\\1(\\w)\\2(\\w)\\3", "$1$2$3$3");
        if ( ValidWordChecker.getInstance().isTextAValidWord(temp) ) return temp;
        
        temp = givenToken.replaceAll("(\\w)\\1(\\w)\\2", "$1$2$2");
        if ( ValidWordChecker.getInstance().isTextAValidWord(temp) ) return temp;
        
        temp = givenToken.replaceAll("(\\w)\\1(\\w)\\2", "$1$1$2");        
        if ( ValidWordChecker.getInstance().isTextAValidWord(temp) ) return temp;

        temp = givenToken.replaceAll("(\\w)\\1(\\w)\\2", "$1$2");
        if ( ValidWordChecker.getInstance().isTextAValidWord(temp) ) return temp;
        
        temp = givenToken.replaceAll("(\\w)\\1", "$1");        
        if ( ValidWordChecker.getInstance().isTextAValidWord(temp) ) return temp;
        
        return givenToken;
    }  
   
}