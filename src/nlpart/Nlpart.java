package nlpart;

import java.util.logging.*;
import java.util.*;

public class Nlpart {

	public static void main(String args[]) throws Exception{
		Util.openLogFile();
		Util.writeToLog(Level.INFO, "Starting...");
		Scanner in = new Scanner(System.in);
		System.out.println("Welcome to Pragyan - The DBpedia question answering engine: ");
		System.out.println("Choose a type of query: \n1. Person\n2. Places\n3. Movies\n\nChoice: ");
		int choice = Integer.parseInt(in.nextLine());
		String ques = in.nextLine();
		
		long prgStartTime = System.currentTimeMillis();
		
		
		String inputQuestion = ques;
		Util.writeToLog(Level.INFO, "The question is: "+inputQuestion);
		System.out.println("The question is: "+inputQuestion);
		System.out.println("Querying.. Please wait..");
		answerGenerator answerGenerator = new answerGenerator();
		
		// This will generate the various sparql queries by parsing the question and put it inside the query bucket 
		answerGenerator.generateQueries(inputQuestion,choice);
		
		long prgEndTime = System.currentTimeMillis();
		
		Util.writeToLog(Level.INFO, "Total time taken : "
						+ (prgEndTime - prgStartTime) / 1000
						+ " seconds");
		Util.closeLogFile();
	}

}
