package nlpart;

import java.util.*;
import java.util.logging.Level;

import org.apache.log4j.pattern.LiteralPatternConverter;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class answerGenerator
{

	private String parsedQuestion;
	private String question;
	private int questionType;
	private Lexicon lexicon;
	public int choice;
	private String tmplog;

	public answerGenerator() {
		lexicon = new Lexicon();
	}

	public void generateQueries(String question, int choice) throws Exception {

		this.question = question.toLowerCase();
		this.choice = choice;
		
		

		sanitizeQuery();
	
		Util.writeToLog(Level.INFO, "Sanitized question: " + parsedQuestion);
		Util.writeToLog(Level.INFO, "Choice of question : " + choice);
		List<String> queries = buildQueries();
		System.out
				.println("\n\n\n\n\n********************The answer to the Life, Universe and Everything :***************************** ");
		tmplog = "";
		for (String query : queries)
		{

			tmplog += "The query is: " + query + "\r\n\r\n";
			try
			{
				Query queryObj = QueryFactory.create(query);
				String sparqlEndpoint = "http://dbpedia.org/sparql";
				QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, queryObj);
				ResultSet answersList = qe.execSelect();
				while (answersList.hasNext())
				{
					QuerySolution answer = answersList.nextSolution();
					RDFNode ans = answer.get("x");
					if ( ans != null )
					{
						System.out.println("The query is: " + query);
						System.out.println("\n**************************\n");
						System.out.println("The answer is : " + ans.toString());
						System.out.println("\n**************************\n");
						tmplog += "The answer is: " + ans.toString()
								+ "\r\n****************************************************\r\n\r\n";
					}
				}
			}
			catch (Exception e)
			{
				System.out.println("Houston, we have a problem! " + e.getMessage());
			}
		}
		Util.writeToLog(Level.INFO, "Queries and their result \r\n" + tmplog);

	}

	public void sanitizeQuery() {
		parsedQuestion = question.replace("?", "");
		parsedQuestion = parsedQuestion.replace(",", "");
		parsedQuestion = parsedQuestion.replace(";", "");
		parsedQuestion = parsedQuestion.replace("?", "");
		parsedQuestion = parsedQuestion.replace("]", "");
		parsedQuestion = parsedQuestion.replace("[", "");
		parsedQuestion = parsedQuestion.replace(")", "");
		parsedQuestion = parsedQuestion.replace("(", "");
		parsedQuestion = parsedQuestion.replace("  ", " ");
	}


	public List<String> buildQueries() throws Exception {


		List<String> questionAndType = Util.getQuestionType(parsedQuestion);
		List<String> queries = new ArrayList<String>();

		
		List<LexiconLiteral> literalList = lexicon.getLiterals(parsedQuestion, 50, 5, choice);
		List<LexiconPredicate> predicateList = lexicon.getPredicatesForThese(literalList, parsedQuestion);
		
		
		if(predicateList.isEmpty()){
			LexiconPredicate tmpPredicate = new LexiconPredicate("http://dbpedia.org/ontology/abstract", "abstract");
			predicateList.add(tmpPredicate);
		}
		
		tmplog="";
		for (LexiconPredicate lexPredicate : predicateList)
		{
			System.out.println("Predicate is : " + lexPredicate.URI);
			tmplog+=lexPredicate.URI+"\r\n";
		}
		
		Util.writeToLog(Level.INFO, "Predicates are : \r\n"+tmplog);
		
		String query = "";
		for (LexiconLiteral lexiconLiteral : literalList)
		{
			for (LexiconPredicate lexiconPredicate : predicateList)
			{
				if(lexiconPredicate.URI.contains("abstract"))
				{
					query = "Select * where {<"
							+ lexiconLiteral.URI
							+ "> <"
							+ lexiconPredicate.URI
							+ "> ?x. "
							
							+ " FILTER ( lang(?x) = 'en' )}";
					 queries.add(query);					
				}
				else
				{
				 query = "Select * where {<"
						+ lexiconLiteral.URI
						+ "> <"
						+ lexiconPredicate.URI
						+ "> ?x. "
						
						+ "OPTIONAL { ?x <http://www.w3.org/2000/01/rdf-schema#label> ?label FILTER ( lang(?label) = 'en' )}}";
				 queries.add(query);
				}
			}
		}

		return queries;
	}

}
