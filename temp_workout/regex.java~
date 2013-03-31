import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.*;

public class regex{
	public static void main(String args[])throws Exception{

		List<String> questionAndType = new ArrayList<String>();
		List<String> allQuestionTypeList= new ArrayList<String>();

			String matchedString;
			String regex = "^@([a-zA-Z ]+)";
			String countRegex = "[could | would | can | you | please | tell | me ]* [(how many) | (how much) ]+ [a-zA-Z }]+";
			String normalRegex = "[could | would | can | you | please | tell | me ]* [(who | what )* ]+[a-zA-Z }]+";
			
			String question = "who is the president of India";

			if(question.matches(countRegex)){
			System.out.println("Count type");
			}
			else if(question.matches(normalRegex)){
			System.out.println("Normal Question");
			}

	}
}
