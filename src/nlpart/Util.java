package nlpart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.*;

public class Util extends Exception {

	private static Logger logger = Logger.getLogger("myLog");

	public static void writeToLog(Level level, String s) {
		logger.log(level, s);

	}

	public static List<String> getQuestionType(String question) throws Exception {
		List<String> questionAndType = new ArrayList<String>();
		List<String> allQuestionTypeList = new ArrayList<String>();
		String inputFromFile;
		String questionType;

		// BufferedReader reader;
		questionAndType.add(question);
		questionAndType.add("normal");

		// This shit needs to be reviewed.Writing the regex in the file and
		// retrieving it is a PITA.So, I'm commenting all the below section

		/*
		 * Boolean cleanQuestion = false; BufferedReader reader = new
		 * BufferedReader(new FileReader("QuestionType.txt")); inputFromFile =
		 * reader.readLine();
		 * 
		 * 
		 * 
		 * while ((inputFromFile != null)) { /* this will force a file format
		 * where: 1)regex of question start with '^' which is a regex reserved
		 * character means "the beginning of a string" 2)line starts with small
		 * english letters [a-z] only and this represent the type of the
		 * question It will allow comment lines (not blocks) in the file that
		 * will start with any character except '^' or [a-z] Better to write
		 * "//" as the comment line sign as its the common adopted style for
		 * writting comments
		 * 
		 * String regex = "^@[a-zA-Z]+"; System.out.print("Regex is:"+ regex);
		 * while (inputFromFile.matches(regex)){ inputFromFile =
		 * reader.readLine(); allQuestionTypeList.add(inputFromFile);
		 * inputFromFile = reader.readLine(); } } for (String string :
		 * allQuestionTypeList) { System.out.println(string); }
		 * 
		 * while (!cleanQuestion) { cleanQuestion = true; //flag to tell if all
		 * the questions kewords are consumed
		 * 
		 * for (int i = 0; i < allQuestionTypeList.size() ; i++) {
		 * if(question.matches(allQuestionTypeList.get(i))) {
		 * 
		 * System.out.print("Regex matched"); question =
		 * question.replaceAll(allQuestionTypeList.get(i), ""); //question =
		 * Regex.Replace(question, questionTypeList[i], "");
		 * 
		 * question = question.trim();
		 * 
		 * questionAndType.set(0, question);
		 * 
		 * questionAndType.set(1, allQuestionTypeList.get(++i));
		 * //questionAndType.get(1)= questionTypeList[++i];
		 * 
		 * cleanQuestion = false;
		 * 
		 * break; } else i++; }
		 * 
		 * }
		 * 
		 * System.out.println("The size of Question and Type is: "+
		 * questionAndType.size()); //return questionAndType; // This shit is
		 * not working.So I'm just returning "normal" question type along with
		 * the question itself
		 */
		return questionAndType;
	}

	/* This whole algorithm implementation is attaya pottufied :P */
	public static int calculateLevenshteinDistance(String s, String t) {

		int m = s.length();
		int n = t.length();
		int[][] d = new int[m + 1][n + 1];
		for (int i = 0; i <= m; i++) {
			d[i][0] = i;
		}
		for (int j = 0; j <= n; j++) {
			d[0][j] = j;
		}
		for (int j = 1; j <= n; j++) {
			for (int i = 1; i <= m; i++) {
				if (s.charAt(i - 1) == t.charAt(j - 1)) {
					d[i][j] = d[i - 1][j - 1];
				} else {
					d[i][j] = min((d[i - 1][j] + 1), (d[i][j - 1] + 1), (d[i - 1][j - 1] + 1));
				}
			}
		}
		return (d[m][n]);
	}

	public static int min(int a, int b, int c) {
		return (Math.min(Math.min(a, b), c));
	}
}
