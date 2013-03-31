package nlpart;

import java.util.*;

import edu.mit.jwi.morph.WordnetStemmer;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;
import com.hp.hpl.jena.sparql.resultset.SPARQLResult;

/*
 * This class stores all the "Literals" and the "Predicates" that are found in the Question asked. 
 * 
 * There are two main functions in this Lexicon class
 * 1. getLiteral()
 * 2. getPredicate()
 * */
public class Lexicon {

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

	public List<LexiconPredicate> getPredicates(String question, int limit)
			throws Exception {

		List<String> permutationList = getPermutations(question);
		permutationList = getPermutations(question);
		for (String string : permutationList) {
			System.out.println("=>" + string);
		}
		/*
		 * This is where we need to add wordnet, because the permutation is just
		 * the permutation of the words in the question. We need to have more
		 * words to get the predicates properly! - We can use the JAWS library.
		 * Java API for Wordnet :D pretty cool name, eh? :D
		 */

		// Now that we have constructed the permutations we need to work on the
		// construction of the predicate queries. God damnit!
		String bifContainsValue = "";
		for (String permutation : permutationList) {

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
					+ "?predicate <http://www.w3.org/2000/01/rdf-schema#label> ?label ."
					+ "?label <bif:contains> \"" + bifContainsValue + "\" } " +

					"} limit " + limit;
			System.out.println(queryString);
			Query queryObj = QueryFactory.create(queryString);
			String sparqlEndpoint = "http://dbpedia.org/sparql";
			QueryExecution qe = QueryExecutionFactory.sparqlService(
					sparqlEndpoint, queryObj);

			try {
				ResultSet predicateResults = qe.execSelect();
				while (predicateResults.hasNext()) {
					QuerySolution qsolution = predicateResults.nextSolution();
					//System.out.println(qsolution.toString());
					
					RDFNode predicateURI = qsolution.get("predicate");
					RDFNode predicateLabel = qsolution.get("label");
					LexiconPredicate tmplexiconpredicate = new LexiconPredicate();

                    // check that the property is used .. not a non-used property 
                    Boolean hasResuts = false;
                    String checkQuery = "select distinct * where { ?x <" + predicateURI + "> ?y } limit 1 ";
                    
                    Query isItUsed = QueryFactory.create(checkQuery);
                    QueryExecution isItUsedObj = QueryExecutionFactory.sparqlService(sparqlEndpoint, isItUsed);
                    ResultSet isItUsedResult = isItUsedObj.execSelect();
                    ResultSetRewindable resultset = ResultSetFactory.copyResults(isItUsedResult);
                    
                    System.out.println("Result Size: "+resultset.size());
                    if (resultset.size() != 0)
                    {
                        hasResuts = true;
                        System.out.println("true");
                    }
                    else
                    	System.out.println("false");

					
					
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		// TODO: Create a SimpleStemmer class and get the stems of the word and
		// use it with
		
		return this.predicateList;
	}

	public List<LexiconLiteral> getLiterals(String question, int limit)
			throws Exception {

		List<String> permutationList = getPermutations(question);
		permutationList = getPermutations(question);

		String bifContainsValue = "";
		for (String permutation : permutationList) {
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
					+ "?literal <bif:contains> '\""
					+ permutation
					+ "\"'.} limit " + limit;

			System.out.println(queryString);
			Query queryObj = QueryFactory.create(queryString);
			String sparqlEndpoint = "http://dbpedia.org/sparql";
			QueryExecution qe = QueryExecutionFactory.sparqlService(
					sparqlEndpoint, queryObj);

			try {
				ResultSet literalResults = qe.execSelect();
				while (literalResults.hasNext()) {
					QuerySolution qsolution = literalResults.nextSolution();
					System.out.println(qsolution.toString());
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return this.literalList;
	}

	public List<String> getPermutations(String question) throws Exception {
		System.out.println("Called get predicates");
		Set<String> permutationList = new LinkedHashSet<>();

		// Removing the unwanted words from the question
		question = question.replaceAll("\\swho\\s", " ");
		question = question.replaceAll("\\swhat\\s", " ");
		question = question.replaceAll("\\show\\s", " ");
		question = question.replaceAll("\\smany\\s", " ");
		question = question.replaceAll("\\smuch\\s", " ");
		question = question.replaceAll("\\s*would\\s*", " ");
		question = question.replaceAll("\\scould\\s", " ");
		question = question.replaceAll("\\scan\\s", " ");
		question = question.replaceAll("\\splease\\s", " ");
		question = question.replaceAll("\\stell\\s", " ");
		question = question.replaceAll("\\sme\\s", " ");
		question = question.replaceAll("\\sa\\s", " ");
		question = question.replaceAll("\\sthe\\s", " ");
		question = question.replaceAll("\\syou\\s", " ");
		question = question.replaceAll("\\sis\\s", " ");
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
		for (int i = 1; i < splitQuestionSize; i++) {
			for (int j = 0; j < (splitQuestionSize - (i - 1)); j++) {
				for (int j2 = j; j2 < (i + j); j2++) {
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
