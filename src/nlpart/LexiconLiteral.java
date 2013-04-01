package nlpart;
import java.util.*;

public class LexiconLiteral {
	public List<String> typeOfOwner;
    public String predicate;
    public List<String> domains;
    public List<String> ranges;
    public String type;
    public String URI;
    public String label;
    public String QuestionMatch ;
    public String identifier ;
    public int score ;
    
    public LexiconLiteral(){
    	
    	typeOfOwner = new ArrayList<String>();
    }
}
