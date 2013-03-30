package nlpart;
import java.util.*;
public class answerGenerator {
	
	private String parsedQuestion;
    private String question;
    private int questionType;
    private Lexicon lexicon;
    
    public answerGenerator(){
    	lexicon = new Lexicon();
    	
    }
    
    /*
     * Before generating the queries we need to sanitize the queries. Remove the question mark, other symbols and other stuff 
     * */
    
    public List<QueryBucket> generateQueries(String question) throws Exception{
    	
    	this.question = question.toLowerCase();
    	System.out.println(question);
    	
    	// Sanitize the question.
    	
    	sanitizeQuery();
    	System.out.println(parsedQuestion);
    	List<QueryBucket> queries =  buildQueries();
    	return queries;
    }
    
    public void sanitizeQuery(){
    	parsedQuestion  = question.replace("?", "");
    	parsedQuestion  = parsedQuestion.replace(",", "");
    	parsedQuestion  = parsedQuestion.replace(";", "");
    	parsedQuestion  = parsedQuestion.replace("?", "");
    	parsedQuestion  = parsedQuestion.replace("]", "");
    	parsedQuestion  = parsedQuestion.replace("[", "");
    	parsedQuestion  = parsedQuestion.replace(")", "");
    	parsedQuestion  = parsedQuestion.replace("(", "");
    	parsedQuestion  = parsedQuestion.replace("  ", " ");
    }
    
    
    /*
     * THIS IS THE MOST IMPORTANT CLASS:
     * 1. Takes the sanitized question.
     * 2. Gets the predicate and the literal for that.
     * 
     * 
     * */
    public List<QueryBucket> buildQueries() throws Exception{
    	// Finding the type of the question
    	List<String> questionAndType = Util.getQuestionType(parsedQuestion);
    	
    	// Get the predicates of the question
    	List<LexiconPredicate> predicateList = lexicon.getPredicates(parsedQuestion); //find all matching predicates
        //List<LexiconLiteral> literalList = lexicon.getLiterals(parsedQuestion);
    	List<QueryBucket> dummy= null;
    	//System.out.println(questionAndType.get(0));
    	//System.out.println(questionAndType.get(1));
    	return dummy;
    	
    }
}
