import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.*;

/**
 * @author Brendan Ritter
 * @author Riley Butler
 */
public class Parser implements Runnable {

	private Jedis jedis;
	private boolean verb;
	private int timeout = 0;
	
	public Parser(Jedis jedis){
		this.jedis = jedis;
	}
	
	public void run() {
		while (runOnce());
	}
	
	/**
	 * This method calls runOnce a given number
	 * of times
	 * 
	 * @param attempts
	 */
	public void run(int attempts) {
		for (int i = 0; i < attempts; i++) {
			if (! runOnce()) return;
		}
	}
	
	/**
	 * This method returns true if you want to
	 * print the output and false if you don't
	 *  
	 * @return boolean
	 */
	public boolean runOnce() {
		List<String> results = jedis.blpop(timeout, "toParse");
		if (results == null) {
			System.out.println("Parser timeout");
			return false;
		}
		String contents = results.get(1);
		String[] split = contents.split(":", 2);
		String index = split[0];
		String text = split[1];
		
		ArrayList<String> keywords = parseText(text);
		if (verb) System.out.println("Keywords: "+keywords.toString());
		
		for (String keyword: keywords) {
			jedis.zincrby("globalKeywords", 1, keyword);
			jedis.zincrby(keyword, 1, index);
		}
		jedis.incrBy("pageCount", 1);
		
		if (verb) System.out.println("Parsed: " + jedis.hget("urlIndex", index));
		return true;
	}
	
	public void setTimeout(int timeout) {
		if (timeout >= 0) this.timeout = timeout;
		else this.timeout = 0;
	}
	
	public int getTimeout() {
		return timeout;
	}

	/**
	 * This method prints the output
	 */
	public void verbose() {
		verb = true;
	}
	
	/**
	 * This method does not print the output
	 */
	public void silent() {
		verb = false;
	}
	
	/**
	 * This method takes a string, parses it and returns 
	 * an ArrayList with all the parsed text
	 * 
	 * @param text
	 * @return keywords 
	 */
	private static ArrayList<String> parseText(String text) {
		ArrayList<String> keywords = new ArrayList<String>();
		for (String keyword: text.split("\\s+")) {
			String lower=keyword.toLowerCase();
			String nopunc=lower.replaceAll("[^a-z0-9]",""); 
			if (!nopunc.equals("")){
				keywords.add(nopunc);
			}
		}
		return keywords;
	}
	
}
