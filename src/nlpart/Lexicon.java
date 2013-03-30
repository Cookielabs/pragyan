package nlpart;

import java.util.*;

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

	public List<LexiconPredicate> getPredicates(String question)
			throws Exception {
		List<String> permutationList = getPermutations(question);
		permutationList = getPermutations(question);
		for (String string : permutationList) {
			System.out.println("=>" + string);
		}
		/*This is where we need to add wordnet, because the permutation is just the permutation of the words in the question.
		 * We need to have more words to get the predicates properly! 
		 * - We can use the JAWS library. Java API for Wordnet :D pretty cool name, eh? :D
		 * */
		
		// Now that we have constructed the permutations we need to work on the construction of the predicate queries. God damnit!
		
		for (String permutation : permutationList) {
			
		}
		
		return this.predicateList;
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
		// This is the permutation algorithm
		for (int i = 1; i < splitQuestion.size(); i++) {
			for (int j = 0; j < (splitQuestion.size() - (i - 1)); j++) {
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

		// Converting the LinkedHashSet to ArrayList because of the return type.Arrrgh!
		List<String> returningPermutationList = new ArrayList<String>();
		returningPermutationList.addAll(permutationList);
		return returningPermutationList;
	}

}
