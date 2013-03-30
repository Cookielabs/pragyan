package nlpart;

import java.util.logging.*;
import java.util.*;

public class Nlpart {

	public static void main(String args[]) throws Exception{
		Util.writeToLog(Level.INFO, "Starting...");
		String inputQuestion = "Would you please ,,,, tell me ;;;Who is the president ;;;of India????";
		Util.writeToLog(Level.INFO, "The question is: "+inputQuestion);
		answerGenerator answerGenerator = new answerGenerator();
		
		// This will generate the various sparql queries by parsing the question and put it inside the query bucket 
		List<QueryBucket> queries = answerGenerator.generateQueries(inputQuestion);
		
	}

}
