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
	
	public void run(int attempts) {
		for (int i = 0; i < attempts; i++) {
			if (! runOnce()) return;
		}
	}
	
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
			jedis.sadd(keyword, index);
		}
		
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
	
	public void verbose() {
		verb = true;
	}
	
	public void silent() {
		verb = false;
	}
	
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
