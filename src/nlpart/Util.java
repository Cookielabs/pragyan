package nlpart;

import java.io.*;
import java.util.logging.*;
import java.util.regex.Pattern;
import java.util.*;

public class Util extends Exception {

	private static FileHandler fileHandle;
	private static Logger logger;
	
	//A Logger that logs in myLogFile.log as simple text 
	public static void openLogFile() throws Exception {
		Logger globalLogger = Logger.getLogger("");
		Handler[] handlers = globalLogger.getHandlers();
		for(Handler handler : handlers) {
		    globalLogger.removeHandler(handler);
		}
		logger = Logger.getLogger("myLog");
		fileHandle = new FileHandler("myLogFile.log", false);
		logger.addHandler(fileHandle);
		fileHandle.setFormatter(new SimpleFormatter());
	}
	
	public static void writeToLog(Level level, String s) {
		//logging along with class name and method name
		logger.logp(level, Thread.currentThread().getStackTrace()[2].getClassName() ,Thread.currentThread().getStackTrace()[2].getMethodName() , s+"\r\n");
		
	}
	
	public static String getPartURI(String URI){
		String nameToSend = URI.substring(URI.lastIndexOf('/')+1, URI.length());
		return nameToSend;
	}
	
	public static void closeLogFile() {
		fileHandle.close();
	}
	

	public static List<String> getQuestionType(String question) throws Exception {
		List<String> questionAndType = new ArrayList<String>();
		List<String> allQuestionTypeList = new ArrayList<String>();
		String inputFromFile;
		String questionType;

		
		questionAndType.add(question);
		questionAndType.add("normal");

				return questionAndType;
	}

	
	public static int calculateLevenshteinDistance(String s, String t) {
		t=t.toLowerCase();
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
