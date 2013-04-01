package nlpart;

import java.util.*;


/*Documentation:
 * The lexicon predicate is the class that contains all the predicates.
 * 
 * A predicate has the following thigs:
 * 1. Predicate URI
 * 2. Predicate Label
 * 3. Predicate Score
 * 4. Predicate Permutation 
 * */




public class LexiconPredicate {

	
	public List<String> domains;
    public List<String> ranges;
    public String type;
    public String URI;
    public String label;
    public String QuestionMatch ;
    public String identifier ;
    public int score ;
    
    public LexiconPredicate(){
    	
    	domains = new ArrayList<String>();
        ranges = new ArrayList<String>();
    }
}
