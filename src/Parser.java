import java.util.HashSet;
import java.util.List;

import redis.clients.jedis.*;

/**
 * @author Brendan Ritter
 * @author Riley Butler
 */
public class Parser {

	private Jedis jedis;
	private boolean verb;
	
	public Parser(Jedis jedis){
		this.jedis = jedis;
	}
	
	public void run() {
		run(0);
	}
	
	public void run(int timeout) {
		while (runOnce(timeout)) {}
	}
	
	public boolean runOnce() {
		return runOnce(0);
	}
	
	public boolean runOnce(int timeout) {
		List<String> results = jedis.blpop(timeout, "toParse");
		if (results == null) {
			System.out.println("Parser timeout");
			return false;
		}
		
		String contents = results.get(1);
		String[] split = contents.split(":", 2);
		String index = split[0];
		String text = split[1];
		
		for (String keyword: parseText(text)) {
			jedis.sadd(keyword, index);
		}
		
		if (verb) System.out.println("Parsed: " + jedis.hget("urlIndex", index));
		return true;
	}
	
	public void verbose() {
		verb = true;
	}
	
	public void silent() {
		verb = false;
	}
	
	private static HashSet<String> parseText(String text) {
		HashSet<String> keywords = new HashSet<String>();
		for (String keyword: text.split("\\s+")) {
			keywords.add(keyword.toLowerCase());
		}
		return keywords;
	}
	
}
