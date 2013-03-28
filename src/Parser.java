import java.util.HashSet;
import java.util.List;

import redis.clients.jedis.*;


public class Parser {

	private Jedis jedis;
	private boolean verb;
	
	public Parser(String hostname){
		this.jedis=new Jedis(hostname);
	}
	
	public void run() {
		while (true) {
			runOnce();
		}
	}
	
	public void run(int attempts) {
		for (int i = 0; i < attempts; i++) {
			runOnce();
		}
	}
	
	public boolean runOnce() {
		List<String> results = jedis.blpop(5, "toParse");
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
