package nlpart;
import java.util.*;
public class QueryBucket {
	//tokens of the query bucket
    public List<LexiconToken> tokens;
    
    //question left of the bucket
    public String questionLeft ;

    //containing all URIs of a certain bucket
    private List<String> uriUsed;

    //containing all URIs not yet resolved!!!!!!
    private List<String> uriToDo; 
}
