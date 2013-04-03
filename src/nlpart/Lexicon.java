package nlpart;

import java.util.*;

import javax.print.attribute.standard.DateTimeAtCreation;

import edu.mit.jwi.morph.WordnetStemmer;

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

	public Lexicon() {
		predicateList = new ArrayList<LexiconPredicate>();
		literalList = new ArrayList<LexiconLiteral>();
		predicateFilled = false;
		literalFilled = false;
	}

	public List<LexiconPredicate> getPredicates(String question, int limit, int topN) throws Exception {

		/*
		 * The following things are to be done in this function
		 * 
		 * 1. Get the permutations 2. Trim the permutations 3. Build the query
		 * and execute it to get the predicates 4. Store the values of the
		 * predicates in the appropriate vars ( URI, Label, QuestionMatch ) 5.
		 */
		List<LexiconPredicate> interPredicateList = new ArrayList<LexiconPredicate>();
		List<String> permutationList = getPermutations(question);
		permutationList = getPermutations(question);
		for (String string : permutationList)
		{
			System.out.println("=>" + string);
		}

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
			System.out.println(queryString);
			Query queryObj = QueryFactory.create(queryString);
			String sparqlEndpoint = "http://dbpedia.org/sparql";
			QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, queryObj);

			try
			{
				ResultSet predicateResults = qe.execSelect();
				while (predicateResults.hasNext())
				{
					QuerySolution qsolution = predicateResults.nextSolution();
					// System.out.println(qsolution.toString());

					RDFNode predicateURI = qsolution.get("predicate");
					RDFNode predicateLabel = qsolution.get("label");
					LexiconPredicate tmplexiconpredicate = new LexiconPredicate();

					// check that the property is used .. not a non-used
					// property
					Boolean hasResuts = false;
					String checkQuery = "select distinct * where { ?x <" + predicateURI + "> ?y } limit 1 ";

					Query isItUsed = QueryFactory.create(checkQuery);
					QueryExecution isItUsedObj = QueryExecutionFactory.sparqlService(sparqlEndpoint, isItUsed);
					ResultSet isItUsedResult = isItUsedObj.execSelect();
					ResultSetRewindable resultset = ResultSetFactory.copyResults(isItUsedResult);
					System.out.println(predicateURI.toString());
					// System.out.println("Result Size: " + resultset.size());
					if ( resultset.size() != 0 )
					{
						hasResuts = true;
						// System.out.println("true");
						tmplexiconpredicate.URI = predicateURI.toString();
						tmplexiconpredicate.label = predicateLabel.toString();
						tmplexiconpredicate.QuestionMatch = permutation;
						interPredicateList.add(tmplexiconpredicate);
					}
					else
					{
						System.out.println("No result. URI not used");

					}

				}
			}
			catch (Exception e)
			{
				System.out.println("Exception caught: " + e.toString());
			}
		}
		System.out.println("outside");
		for (LexiconPredicate lexiconPredicate : interPredicateList)
		{
			System.out.println(lexiconPredicate.label);
		}
		System.out.println("--------------------------------------------");
		predicateList = scorePredicates(interPredicateList, topN);
		// predicateList = addDomainAndRange(predicateList);
		return this.predicateList;
	}

	public List<LexiconLiteral> getLiterals(String question, int limit, int topN) throws Exception {

		List<LexiconLiteral> interLiteralList = new ArrayList<LexiconLiteral>();
		List<String> permutationList = new ArrayList<String>();
		permutationList = getPermutations(question);

		for (String permutations : permutationList)
		{
			System.out.println("=> " + permutations);
		}
		String bifContainsValue = "";
		for (String permutation : permutationList)
		{
			bifContainsValue = "";
			bifContainsValue += "\'" + permutation + "\'";

			String queryString = "select distinct ?subject ?literal ?redirects ?typeOfOwner ?redirectsTypeOfOwner where{"
					+ "?subject <http://www.w3.org/2000/01/rdf-schema#label> ?literal."
					+ "optional { ?subject <http://dbpedia.org/ontology/wikiPageRedirects> ?redirects . "
					+ "optional {?redirects <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?redirectsTypeOfOwner ."
					+ "}}."
					+ "optional {?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?typeOfOwner}."
					+ "Filter ( !bound(?typeOfOwner) || "
					+ " ( !(?typeOfOwner = <http://www.w3.org/2004/02/skos/core#Concept>)"
					+ " && !(?typeOfOwner = <http://www.w3.org/2002/07/owl#Thing>) "
					+ " && !(?typeOfOwner = <http://www.opengis.net/gml/_Feature>) "
					+ " && !(?typeOfOwner = <http://www.w3.org/2002/07/owl#ObjectProperty>) "
					+ " && !(?typeOfOwner = <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ) "
					+ " && !(?typeOfOwner = <http://www.w3.org/2002/07/owl#DatatypeProperty> )))."
					+ "?literal <bif:contains> '\"" + permutation + "\"'. } limit " + limit ;

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
					RDFNode literalURI = qsolution.get("subject");
					RDFNode literalLabel = qsolution.get("literal");
					RDFNode literalTypeOfOwner = qsolution.get("typeOfOwner");
					LexiconLiteral tmplexiconLiteral = new LexiconLiteral();

					tmplexiconLiteral.URI = literalURI.toString();
					tmplexiconLiteral.label = literalLabel.toString();
					tmplexiconLiteral.QuestionMatch = permutation;
					tmplexiconLiteral.typeOfOwner = literalTypeOfOwner.toString();
					interLiteralList.add(tmplexiconLiteral);
				}
			}
			catch (Exception e)
			{
				System.out.println("Exception caught: " + e.toString());
			}

		}
		literalList = scoreLiterals(interLiteralList, topN);

		return literalList;
	}

	public List<LexiconPredicate> scorePredicates(List<LexiconPredicate> results, int n) {
		// TODO: the raking algo and the query part. Wasted time talking to rulz
		// and suggu boy.

		for (LexiconPredicate predicate : results)
		{
			// adding a levenshtein score to each one of them where predicates
			// of high score will make a bad match
			// removing the @en in the end of each label
			// removing the terms between brackets like the dark knight (the
			// film)
			String tmplabel;

			// use match instead regex
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
			System.out.println(tmplabel);
			predicate.score = Util.calculateLevenshteinDistance(predicate.QuestionMatch, tmplabel);
			System.out.println("Levenshtein Score: " + predicate.score);
			/*
			 * Now that we have got the scores of the predicates, we can sort
			 * them in ascending order. The one with the least score is the
			 * winner. I'm going to write the sorting method tomorrow. Too
			 * sleepy now. I'm writing off the same thing for getLiterals
			 */
		}
		return results;
	}

	public List<LexiconLiteral> scoreLiterals(List<LexiconLiteral> results, int n) {
		// TODO: the raking algo and the query part. Wasted time talking to rulz
		// and suggu boy.

		for (LexiconLiteral literal : results)
		{
			// adding a levenshtein score to each one of them where predicates
			// of high score will make a bad match
			// removing the @en in the end of each label
			// removing the terms between brackets like the dark knight (the
			// film)
			String tmplabel;

			// use match instead regex
			if ( literal.label.endsWith("@en") || literal.label.matches("\\(.*\\)") )
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
			System.out.println(tmplabel);
			literal.score = Util.calculateLevenshteinDistance(literal.QuestionMatch, tmplabel);
			System.out.println("Levenshtein Score: " + literal.score);
			/*
			 * Now that we have got the scores of the predicates, we can sort
			 * them in ascending order. The one with the least score is the
			 * winner. I'm going to write the sorting method tomorrow. Too
			 * sleepy now. I'm writing off the same thing for getLiterals
			 */
		}
		return results;
	}

	public List<String> getPermutations(String question) throws Exception {
		System.out.println("Called get predicates");
		Set<String> permutationList = new LinkedHashSet<>();

		// Removing the unwanted words from the question
		question = question.replaceAll("\\s*who\\s*", " ");
		question = question.replaceAll("\\s*what\\s*", " ");
		question = question.replaceAll("\\s*how\\s*", " ");
		question = question.replaceAll("\\s*many\\s*", " ");
		question = question.replaceAll("\\s*much\\s*", " ");
		question = question.replaceAll("\\s*would\\s*", " ");
		question = question.replaceAll("\\s*could\\s*", " ");
		question = question.replaceAll("\\s*can\\s*", " ");
		question = question.replaceAll("\\s*please\\s*", " ");
		question = question.replaceAll("\\s*tell\\s*", " ");
		question = question.replaceAll("\\s*me\\s*", " ");
		question = question.replaceAll("\\s*a\\s*", " ");
		question = question.replaceAll("\\s*the\\s*", " ");
		question = question.replaceAll("\\s*you\\s*", " ");
		question = question.replaceAll("\\s*is\\s*", " ");
		question = question.replaceAll("\\s*give\\s*", " ");
		question = question.replaceAll("\\s*all\\s*", " ");
		question = question.replaceAll("\\s*of\\s*", " ");
		question = question.replaceAll("  ", " "); // Replacing all 2 spaces
		// with 1 space
		question = question.trim();

		System.out.println(question);
		List<String> splitQuestion = new ArrayList<String>();
		splitQuestion = Arrays.asList(question.split(" "));
		String questionNoSpace = question.replaceAll(" ", "");
		String wordSpace = "";
		String wordNoSpace = "";

		// This is the permutation algorithm. Important
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

		// Converting the LinkedHashSet to ArrayList because of the return
		// type.Arrrgh!
		List<String> returningPermutationList = new ArrayList<String>();
		returningPermutationList.addAll(permutationList);
		return returningPermutationList;
	}

}
