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




public class LexiconPredicate implements Comparable<LexiconPredicate>{

	
	public List<String> domains;
    public List<String> ranges;
    public String type;
    public String URI;
    public String label;
    public String QuestionMatch ;
    //public String identifier ;
    public int score ;
    
    public LexiconPredicate(){
    	
    	domains = new ArrayList<String>();
        ranges = new ArrayList<String>();
    }
    
    public int compareTo(LexiconPredicate lexpre) {
		 
		int score2 = ((LexiconPredicate) lexpre).score; 
 
		//ascending order
		return this.score- score2;
 
		//descending order
		//return compareQuantity - this.quantity;
 
	}

	public static Comparator<LexiconPredicate> scoreComparator = new Comparator<LexiconPredicate>() {

		public int compare(LexiconPredicate lexpre1, LexiconPredicate lexpre2) {

			
			int score1 = lexpre1.score;
			int score2 = lexpre2.score;
			// ascending order
			return lexpre1.compareTo(lexpre2);

		
		}

	};
}
