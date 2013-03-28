import java.io.IOException;
import java.util.HashSet;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import redis.clients.jedis.Jedis;

/**
 * @author Chloe Eghtebas
 * @author Brendan Ritter
 * @author Riley Butler
 */
public class Crawler {

	private Jedis jedis;
	private boolean verb;
	
	public Crawler(String hostname){
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
	
	public boolean runOnce(){
		Document doc=null;
		String url = jedis.spop("toCrawl");
		jedis.sadd("triedCrawl", url);
		
		try {
			Connection con = Jsoup.connect(url);
			doc=con.get();
		} catch (IOException e) {
			if (verb) System.out.println("Failed: " + url);
			return false;
		} 

		addLinks(getLinks(doc));
		if (verb) System.out.println("Crawled: "+ url);
		Long index = addIndex(url);
		jedis.sadd("toParse", index.toString() + ":" + getText(doc));
		return true;
	}

	public boolean seed(String url) {
		addLink(url);
		return jedis.scard("toCawl")>0;
	}
	
	public void silent() {
		verb = false;
	}
		
	public void verbose() {
		verb = true;
	}
	
	private long addIndex(String url) {
		Long index = jedis.incr("urlCount");
		jedis.hset("urlIndex", index.toString(), url);
		return (long)index;
	}
	
	private void addLink(String link){
		if (!jedis.sismember("triedCrawl",link)){
			jedis.sadd("toCrawl", link);
		}
	}
	
	private void addLinks(HashSet<String> links){
		for(String link: links){
			addLink(link);
		}
	}
	
	private static HashSet<String> getLinks(Document doc){
		HashSet<String> uniqueLinks=new HashSet<String>();
		Elements links = doc.getElementsByTag("a");
		for (Element link : links) {
			String absUrl = link.absUrl("href");
			uniqueLinks.add(absUrl);
		}
		return uniqueLinks;
	}

	private static String getText(Document doc){
		String allText = doc.title() + " " +doc.text();
		return allText;

	}
    
}
