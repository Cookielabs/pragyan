package nlpart;

import java.net.URI;
import java.util.*;
import java.util.logging.Level;

import javax.print.attribute.standard.DateTimeAtCreation;

import org.apache.log4j.Logger;

import edu.mit.jwi.*;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;
import com.hp.hpl.jena.sparql.resultset.SPARQLResult;

public class Lexicon
{

	private List<LexiconPredicate> predicateList;
	private List<LexiconLiteral> literalList;
	private Boolean predicateFilled;
	private Boolean literalFilled;
	private String tmplog;
	private List<String> permutationList;

	public Lexicon()
	{
		predicateList = new ArrayList<LexiconPredicate>();
		literalList = new ArrayList<LexiconLiteral>();
		predicateFilled = false;
		literalFilled = false;
	}

	
	//THIS FUNCTION IS OBSOLETE, getPericatesForThese(Literals) is the correct working function
	public List<LexiconPredicate> getPredicates(String question, int limit, int topN) throws Exception
	{

		// The time the query has started
		long startTime = System.currentTimeMillis();

		List<LexiconPredicate> interPredicateList = new ArrayList<LexiconPredicate>();
		
		/*
		permutationList is already found in getLiterals() function
		permutationList = getPermutations(question);
		
		tmplog = "";
		for (String string : permutationList)
		{

			tmplog += string + "\r\n";
		}

		Util.writeToLog(Level.INFO, "List of all permutations" + "\r\n" + tmplog);
		*/
		
		String bifContainsValue = "";
		for (String permutation : permutationList)
		{

			bifContainsValue = "";
			bifContainsValue += "\'" + permutation + "\'";

			String queryString = "SELECT  * WHERE { { "
					+ "?predicate <http://www.w3.org/2000/01/rdf-schema#label> ?label ."
					+ "?predicate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty>."
					+ "?label <bif:contains> \""
					+ bifContainsValue
					+ "\" } "
					+ "union {"
					+ "?predicate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ."
					+ "?predicate <http://www.w3.org/2000/01/rdf-schema#label> ?label ."
					+ "?label <bif:contains> \""
					+ bifContainsValue
					+ "\" } "
					+ "union {"
					+ "?predicate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property>  ."
					+ "?predicate <http://www.w3.org/2000/01/rdf-schema#label> ?label ." + "?label <bif:contains> \""
					+ bifContainsValue + "\" } " +

					"} limit " + limit;

			try
			{

				Query queryObj = QueryFactory.create(queryString);
				String sparqlEndpoint = "http://dbpedia.org/sparql";
				QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, queryObj);
				ResultSet predicateResults = qe.execSelect();
				while (predicateResults.hasNext())
				{
					QuerySolution qsolution = predicateResults.nextSolution();

					RDFNode predicateURI = qsolution.get("predicate");
					RDFNode predicateLabel = qsolution.get("label");
					LexiconPredicate tmplexiconpredicate = new LexiconPredicate();

					Boolean hasResuts = false;
					String checkQuery = "select distinct * where { ?x <" + predicateURI + "> ?y } limit 1 ";

					Query isItUsed = QueryFactory.create(checkQuery);
					QueryExecution isItUsedObj = QueryExecutionFactory.sparqlService(sparqlEndpoint, isItUsed);
					ResultSet isItUsedResult = isItUsedObj.execSelect();
					ResultSetRewindable resultset = ResultSetFactory.copyResults(isItUsedResult);
					if ( resultset.size() != 0 )
					{
						hasResuts = true;

					}
					else
					{
						Util.writeToLog(Level.INFO, "This predicate is not used: \r\n" + predicateURI.toString());
					}
					Boolean exists = false;

					for (LexiconPredicate x : interPredicateList)
					{

						if ( x.URI == predicateURI.toString() && x.QuestionMatch == permutation )
						{
							exists = true;
							break;
						}
					}

					if ( !exists && hasResuts )
					{
						tmplexiconpredicate.URI = predicateURI.toString();
						tmplexiconpredicate.QuestionMatch = permutation;
						tmplexiconpredicate.label = predicateLabel.toString();
						interPredicateList.add(tmplexiconpredicate);
						tmplog += predicateURI.toString() + "\r\n";
					}
				}
			}

			catch (Exception e)
			{
				System.out.println("Exception caught: " + e.toString());
			}
		}

		Util.writeToLog(Level.INFO, "Predicates Found (w/o duplicates) : \r\n" + tmplog);
		tmplog = "";

		predicateList = scorePredicates(interPredicateList, topN);
		long endTime = System.currentTimeMillis();

		System.out.println("The total time taken to get predicates is : " + (endTime - startTime) / 1000 + " seconds");

		Util.writeToLog(Level.INFO, "The total time taken to get predicates is : " + (endTime - startTime) / 1000
				+ " seconds");

		predicateList = addDomainAndRange(predicateList);
		Collections.sort(predicateList);
		for (LexiconPredicate lexiconPredicate : predicateList)
		{
			tmplog += lexiconPredicate.URI + " " + lexiconPredicate.label + "  Score=> " + lexiconPredicate.score
					+ "\r\n";
		}

		Util.writeToLog(Level.INFO, "Predicates with Levenshtein Score \r\n" + tmplog);

		return this.predicateList;
	}

	public List<LexiconLiteral> getLiterals(String question, int limit, int topN, int choiceOfQuestion)
			throws Exception
	{
		String questionClass = "Thing";
		switch (choiceOfQuestion)
		{
		case 1:
			questionClass = "Person";
			break;
		case 2:
			questionClass = "Place";
			break;
		case 3:
			questionClass = "Film";
			break;
		default:
			break;
		}

		long startTime = System.currentTimeMillis();

		List<LexiconLiteral> interLiteralList = new ArrayList<LexiconLiteral>();
		permutationList = new ArrayList<String>();
		permutationList = getPermutations(question);

		
		if ( literalFilled )
		{
			for (LexiconLiteral literal : this.literalList)
			{
				if ( Arrays.asList(permutationList).contains(literal.QuestionMatch) )
				{
					interLiteralList.add(literal);
				}
			}

			return interLiteralList;
		}
		
		else
		{
			String bifContainsValue = "";
			tmplog = "";
			for (String permutation : permutationList)
			{
				bifContainsValue = "";
				bifContainsValue += "\'" + permutation + "\'";
				String queryString = "select distinct ?subject ?literal ?redirects ?typeOfOwner ?redirectsTypeOfOwner where {"
						+ "?subject <http://www.w3.org/2000/01/rdf-schema#label> ?literal ."
						+ "?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?typeOfOwner ."
						+ "optional { ?subject <http://dbpedia.org/ontology/wikiPageRedirects> ?redirects ."
						+ "optional {?redirects <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?redirectsTypeOfOwner ."
						+ "}} Filter ( !bound(?typeOfOwner) || ( (?typeOfOwner = <http://dbpedia.org/ontology/"
						+ questionClass
						+ ">))) . "
						+ "?literal <bif:contains> '\""
						+ permutation
						+ "\"'. } limit "
						+ limit;
				System.out.println(queryString);
				Query queryObj = QueryFactory.create(queryString);
				String sparqlEndpoint = "http://dbpedia.org/sparql";
				QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, queryObj);
				try
				{
					ResultSet literalResults = qe.execSelect();
					while (literalResults.hasNext())
					{
						QuerySolution qsolution = literalResults.nextSolution();
						RDFNode literalURI;
						RDFNode literalLabel = qsolution.get("literal");
						String resultTypeOfOwner = "";
						String resultQuestionMatch = permutation;
						LexiconLiteral tmpLexiconLiteral = new LexiconLiteral();

						if ( qsolution.get("redirects") != null )
						{
							literalURI = qsolution.get("redirects");
						}
						else
						{
							literalURI = qsolution.get("subject");

						}

						Boolean exists = false; // URI + Label only Exists

						for (LexiconLiteral x : interLiteralList)
						{

							if ( x.URI == literalURI.toString() && x.QuestionMatch == resultQuestionMatch )
							{
								exists = true;
								break;
							}
						}
						
						//Each Literal URI contains a list of labels. 
						//Selecting the best Label based on Levenshtein Score
						if ( exists )
						{
							Iterator<LexiconLiteral> it = interLiteralList.iterator();
							String uri1=literalURI.toString();
							String label1=literalLabel.toString();
							while(it.hasNext())
							{
								
								LexiconLiteral obj = it.next();
								
							    if( obj.URI.equals(uri1) && !obj.label.equals(label1) )
							    {
							    
							    	obj.label=bestLabelOf(obj.label, label1, obj.QuestionMatch);
							    }

							}
						}

						if ( !exists )
						{

							tmpLexiconLiteral.URI = literalURI.toString();
							tmpLexiconLiteral.QuestionMatch = resultQuestionMatch;
							String tmplabel=literalLabel.toString();
							
							//Sanitizing the label
							if ( tmplabel.matches(".*@.*") || tmplabel.matches("\\(.*\\)") )
							{
								tmplabel = tmplabel.substring(0, tmplabel.length() - 3);
								if ( tmplabel.matches("\\(.*\\)") )
								{
									tmplabel = tmplabel.replace("\\(.*\\)", " ");
									tmplabel = tmplabel.replace("  ", " ");
									tmplabel = tmplabel.trim();
								}
							}
							
							tmpLexiconLiteral.label = tmplabel;
							
							interLiteralList.add(tmpLexiconLiteral);
						}

					}
				}
				catch (Exception e)
				{
					System.out.println("Exception caught: " + e.toString());
				}

			}
			Util.writeToLog(Level.INFO, "Literals w/o Duplicates are added to Literal List ");
			literalList = scoreLiterals(interLiteralList, topN);

			// adding typeOfOwner to the finally short listed literalList
			literalList = addTypeOfOwner(literalList);

			literalFilled = true;
			long endTime = System.currentTimeMillis();
			System.out.println("The total time taken to get the Literals is : " + (endTime - startTime) / 1000
					+ " seconds");
			for (LexiconLiteral lexiconLiteral : literalList)
			{
				System.out.println("Literal: " + lexiconLiteral.URI + " SCore: " + lexiconLiteral.score);
				
			 // Adding the literals w/ levenshtein score to the log file.
				tmplog += lexiconLiteral.URI + "  permutation:" +
				lexiconLiteral.QuestionMatch + "  label:"
				+ lexiconLiteral.label + "  Score=> " + lexiconLiteral.score
				+ "\r\n";
			}

			Util.writeToLog(Level.INFO, "Literals with Levenshtein Score \r\n" + tmplog);
			return literalList;
		}
	}

	public List<LexiconPredicate> getPredicatesForThese(List<LexiconLiteral> literalList, String question)
			throws Exception
	{

		List<LexiconPredicate> predicateList = new ArrayList<LexiconPredicate>();
		for (LexiconLiteral literal : literalList)
		{
			String literalURI = literal.URI;
			String queryString = "SELECT distinct ?predicate ?label "
					+ "WHERE {<"
					+ literalURI
					+ "> ?predicate ?y . ?predicate <http://www.w3.org/2000/01/rdf-schema#label> ?label . FILTER ( lang(?label) = 'en') }";

			Query queryObj = QueryFactory.create(queryString);
			String sparqlEndpoint = "http://dbpedia.org/sparql";
			QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, queryObj);
			try
			{
				ResultSet literalResults = qe.execSelect();
				while (literalResults.hasNext())
				{
					Boolean exists = false;
					QuerySolution qsolution = literalResults.nextSolution();
					RDFNode predicateURI = qsolution.get("predicate");
					RDFNode predicateLabel = qsolution.get("label");
					
					
					LexiconPredicate tmpPredicate = new LexiconPredicate(predicateURI.toString(),
							predicateLabel.toString());
					for (LexiconPredicate lexiconPredicate : predicateList)
					{
						if ( lexiconPredicate.URI.contentEquals(predicateURI.toString()) )
						{
							// System.out.println("Match Found");
							// System.out.println(lexiconPredicate.URI
							// +" and "+tmpPredicate.URI);
							exists = true;
							break;
						}
						else
						{
							// System.out.println("No match "+lexiconPredicate.URI
							// +" and "+tmpPredicate.URI);
						}

					}
					if ( !exists )
					{
						//Sanitizing the labels
						if ( tmpPredicate.label.matches(".*@.*") || tmpPredicate.label.matches("\\(.*\\)") )
						{
							tmpPredicate.label = tmpPredicate.label.substring(0, tmpPredicate.label.length() - 3);
							if ( tmpPredicate.label.matches("\\(.*\\)") )
							{
								tmpPredicate.label = tmpPredicate.label.replace("\\(.*\\)", " ");
								tmpPredicate.label = tmpPredicate.label.replace("  ", " ");
								tmpPredicate.label = tmpPredicate.label.trim();
							}
						}
						predicateList.add(tmpPredicate);
					}

				}

			}
			catch (Exception e)
			{
				System.out.println("Houston, we have a problem: " + e.getMessage());
			}
		}
		tmplog="";
		for (LexiconPredicate lexiconPredicate : predicateList)
		{
			System.out.println("URI: " + lexiconPredicate.URI + "  Label: " + lexiconPredicate.label);
			
			//adding the list of predicates to the log file
			tmplog+="URI: " + lexiconPredicate.URI + "  Label: " + lexiconPredicate.label+"\r\n";
		}
		
		Util.writeToLog(Level.INFO, "List of predicates :\r\n"+tmplog);
		// Need to Remove the Duplicates.
		List<LexiconPredicate> predicatesAfterScoring =  new ArrayList<LexiconPredicate>();
		predicatesAfterScoring = scoreThesePredicates(predicateList, question);
		
		tmplog="";
		for (LexiconPredicate lexiconPredicate : predicatesAfterScoring)
		{
			System.out.println("URi: " + lexiconPredicate.URI + "  Label: " + lexiconPredicate.label);
			tmplog+="URI: " + lexiconPredicate.URI + "  Label: " + lexiconPredicate.label+"\r\n";;
		}
		
		Util.writeToLog(Level.INFO, "List of predicates that matches w/ permutation :\r\n"+tmplog);
		return predicatesAfterScoring;

	}

	public List<LexiconPredicate> scoreThesePredicates(List<LexiconPredicate> results, String question)
			throws Exception
	{
		List<LexiconPredicate> predicateListToSend = new ArrayList<LexiconPredicate>();
		
		//permutationList is already found in getLiterals() function
		//permutationList = getPermutations(question);
		
		tmplog="";
		
		for (LexiconPredicate lexiconPredicate : results)
		{
			Boolean itContains = false;
			for (String string : permutationList)
			{
				if ( lexiconPredicate.URI.contains(string) )
				{
					System.out.println("Contains: " + lexiconPredicate.URI + " permutation: " + string);
					tmplog+="Contains: " + lexiconPredicate.URI + "   Permutation: " + string+"\r\n";
					itContains = true;

				}
			}
			if ( itContains )
			{
				predicateListToSend.add(lexiconPredicate);
			}

		}
		Util.writeToLog(Level.INFO, "List of Predicates that match w/ Permutation: \r\n"+tmplog);
		return predicateListToSend;
	}

	//THIS FUNCTION IS OBSOLETE, scoreThesePredicates(Predicates) is the correct working function
	public List<LexiconPredicate> scorePredicates(List<LexiconPredicate> results, int n)
	{

		for (LexiconPredicate predicate : results)
		{
			String tmplabel;

			if ( predicate.label.endsWith("@en") || predicate.label.matches("\\(.*\\)") )
			{
				tmplabel = predicate.label.substring(0, predicate.label.length() - 3);
				if ( predicate.label.matches("\\(.*\\)") )
				{
					tmplabel = tmplabel.replace("\\(.*\\)", " ");
					tmplabel = tmplabel.replace("  ", " ");
					tmplabel = tmplabel.trim();
				}

			}
			else
			{
				tmplabel = predicate.label;
			}

			predicate.score = Util.calculateLevenshteinDistance(predicate.QuestionMatch, tmplabel);

		}
		List<LexiconPredicate> resultToSend = new ArrayList<LexiconPredicate>();
		resultToSend.addAll(results);
		Collections.sort(resultToSend);
		if ( resultToSend.size() < n )
		{
			n = resultToSend.size();
		}
		;
		return resultToSend.subList(0, n);

	}

	public List<LexiconLiteral> addTypeOfOwner(List<LexiconLiteral> results)
	{
		for (LexiconLiteral literal : results)
		{

			String queryString = "select distinct ?type where{" + "<" + literal.URI + ">"
					+ "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type.}";

			Query queryObj = QueryFactory.create(queryString);
			String sparqlEndpoint = "http://dbpedia.org/sparql";
			QueryExecution queryExec = QueryExecutionFactory.sparqlService(sparqlEndpoint, queryObj);
			try
			{
				ResultSet typeOfOwnerResults = queryExec.execSelect();
				while (typeOfOwnerResults.hasNext())
				{
					QuerySolution qsolution = typeOfOwnerResults.nextSolution();
					RDFNode typeOfOwner = qsolution.get("type");
					if ( typeOfOwner != null )
					{
						literal.typeOfOwner.add(typeOfOwner.toString());
					}
				}
			}
			catch (Exception e)
			{
				System.out.println("Exception caught: " + e.toString());
			}
		}
		return results;
	}

	public List<LexiconLiteral> scoreLiterals(List<LexiconLiteral> results, int n)
	{

		for (LexiconLiteral literal : results)
		{
			String tmplabel;

			if ( literal.label.matches(".*@.*") || literal.label.matches("\\(.*\\)") )
			{
				tmplabel = literal.label.substring(0, literal.label.length() - 3);
				if ( literal.label.matches("\\(.*\\)") )
				{
					tmplabel = tmplabel.replace("\\(.*\\)", " ");
					tmplabel = tmplabel.replace("  ", " ");
					tmplabel = tmplabel.trim();
				}

			}
			else
			{
				tmplabel = literal.label;
			}

			literal.score = Util.calculateLevenshteinDistance(literal.QuestionMatch, tmplabel);

		}

		HashSet<LexiconLiteral> dupRemovedResults = new LinkedHashSet<LexiconLiteral>();
		for (LexiconLiteral literal : results)
		{
			for (LexiconLiteral literal2 : results)
			{

				if ( literal.equals(literal2) )
				{
					dupRemovedResults.add(literal);

				}
				if ( literal.URI == literal2.URI && !literal.equals(literal2) )
				{

					dupRemovedResults.add((literal.score <= literal2.score) ? literal : literal2);

				}
			}
		}
		List<LexiconLiteral> resultToSend = new ArrayList<LexiconLiteral>();
		resultToSend.addAll(dupRemovedResults);
		Collections.sort(resultToSend);
		if ( resultToSend.size() < n )
		{
			n = resultToSend.size();
		}

		return resultToSend.subList(0, n);
	}

	private List<LexiconPredicate> addDomainAndRange(List<LexiconPredicate> predicateList)
	{

		for (LexiconPredicate lexiconPredicate : predicateList)
		{
			String queryString = "Select distinct ?domain ?range where { {" +

			"<" + lexiconPredicate.URI + ">" + "<http://www.w3.org/2000/01/rdf-schema#domain> ?domain.}" + "union { <"
					+ lexiconPredicate.URI + ">" + " <http://www.w3.org/2000/01/rdf-schema#range> ?range ." + "}}";

			Query queryObj = QueryFactory.create(queryString);
			String sparqlEndpoint = "http://dbpedia.org/sparql";
			QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, queryObj);
			ResultSet domainAndRangeResults = qe.execSelect();

			while (domainAndRangeResults.hasNext())
			{
				QuerySolution domainAndRangeSolution = domainAndRangeResults.nextSolution();
				if ( domainAndRangeSolution.get("domain") != null )
				{
					if ( !lexiconPredicate.domains.contains(domainAndRangeSolution.get("domain").toString()) )
					{
						lexiconPredicate.domains.add(domainAndRangeSolution.get("domain").toString());
					}
				}
				if ( domainAndRangeSolution.get("range") != null )
				{
					if ( !lexiconPredicate.ranges.contains(domainAndRangeSolution.get("range").toString()) )
					{
						lexiconPredicate.ranges.add(domainAndRangeSolution.get("range").toString());
					}
				}
			}
		}
		tmplog = "";
		for (LexiconPredicate lexiconPredicate : predicateList)
		{
			for (String domain : lexiconPredicate.domains)
			{
				tmplog += "The domain of " + lexiconPredicate.URI + " is => " + domain + "\r\n";
			}

		}
		for (LexiconPredicate lexiconPredicate : predicateList)
		{
			for (String range : lexiconPredicate.ranges)
			{
				tmplog += "The range of " + lexiconPredicate.URI + " is => " + range + "\r\n";
			}
		}

		Util.writeToLog(Level.INFO, "The domain and the range of predicates \r\n" + tmplog);
		tmplog = "";
		return predicateList;
	}

	public List<String> getPermutations(String question) throws Exception
	{
		
		Set<String> permutationList = new LinkedHashSet<>();

		question = question.replaceAll("\\s*who\\s*", " ");
		question = question.replaceAll("\\s*what\\s*", " ");
		question = question.replaceAll("\\s*how\\s*", " ");
		question = question.replaceAll("\\smany\\s", " ");
		question = question.replaceAll("\\smuch\\s", " ");
		question = question.replaceAll("\\s*would\\s*", " ");
		question = question.replaceAll("\\s*could\\s*", " ");
		question = question.replaceAll("\\scan\\s", " ");
		question = question.replaceAll("\\s*please\\s*", " ");
		question = question.replaceAll("\\s*tell\\s*", " ");
		question = question.replaceAll("\\sme\\s", " ");
		question = question.replaceAll("\\sa\\s", " ");
		question = question.replaceAll("\\sthe\\s", " ");
		question = question.replaceAll("\\syou\\s", " ");
		question = question.replaceAll("\\sis\\s*", " ");
		question = question.replaceAll("\\s*give\\s*", " ");
		question = question.replaceAll("\\sall\\s", " ");
		question = question.replaceAll("\\sof\\s", " ");
		question = question.replaceAll("  ", " "); // Replacing all 2 spaces
		// with 1 space
		question = question.trim();
		
		Util.writeToLog(Level.INFO, "Question after removing the stop words: "+question);

		List<String> splitQuestion = new ArrayList<String>();
		splitQuestion = Arrays.asList(question.split(" "));
		String questionNoSpace = question.replaceAll(" ", "");
		String wordSpace = "";
		String wordNoSpace = "";

		int splitQuestionSize = splitQuestion.size();
		for (int i = 1; i < splitQuestionSize; i++)
		{
			for (int j = 0; j < (splitQuestionSize - (i - 1)); j++)
			{
				for (int j2 = j; j2 < (i + j); j2++)
				{
					wordSpace += splitQuestion.get(j2) + " ";
					wordNoSpace += splitQuestion.get(j2);
				}
				permutationList.add(wordSpace.trim());
				permutationList.add(wordNoSpace.trim());
				wordNoSpace = "";
				wordSpace = "";
			}
		}
		permutationList.add(question.trim());
		permutationList.add(questionNoSpace.trim());
		
		tmplog = "";
		
		for (String string : permutationList)
		{

			tmplog += string + "\r\n";
		}
		
		Util.writeToLog(Level.INFO, "List of all permutations" + "\r\n" + tmplog);
		
		List<String> returningPermutationList = new ArrayList<String>();
		returningPermutationList.addAll(permutationList);
		return returningPermutationList;
	}
	
	String bestLabelOf(String objlabel, String label1, String permutation)
	{
		if ( label1.matches(".*@.*") || label1.matches("\\(.*\\)") )
		{
			label1 = label1.substring(0, label1.length() - 3);
			if ( label1.matches("\\(.*\\)") )
			{
				label1 = label1.replace("\\(.*\\)", " ");
				label1 = label1.replace("  ", " ");
				label1 = label1.trim();
			}
		}
		
		int objlabelScore = Util.calculateLevenshteinDistance(permutation, objlabel);
		int label1Score = Util.calculateLevenshteinDistance(permutation, label1);
		
		return (objlabelScore<label1Score) ? objlabel:label1;
	}
}
