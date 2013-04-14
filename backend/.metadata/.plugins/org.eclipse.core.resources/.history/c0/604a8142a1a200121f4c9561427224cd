package nlpart;

import java.util.logging.*;
import java.util.*;

public class Nlpart {

	public static void main(String args[]) throws Exception{
		Util.openLogFile();
		Util.writeToLog(Level.INFO, "Starting...");
		String inputQuestion = "population census india";
		Util.writeToLog(Level.INFO, "The question is: "+inputQuestion);
		System.out.println("The question is: "+inputQuestion);
		System.out.println("Querying.. Please wait..");
		answerGenerator answerGenerator = new answerGenerator();
		
		// This will generate the various sparql queries by parsing the question and put it inside the query bucket 
		answerGenerator.generateQueries(inputQuestion);
		Util.closeLogFile();
	}

}
