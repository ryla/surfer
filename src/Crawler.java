import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import redis.clients.jedis.Jedis;

/**
 * @author Chloe Eghtebas
 * @author Brendan Ritter
 * @author Riley Butler
 * @author Pati Martin
 */
public class Crawler implements Runnable{

	private Jedis jedis;
	private boolean verb;
	private int timeout = 30000;
	
	public Crawler(Jedis jedis){
		this.jedis = jedis;
	}
	/**
	 * Runs continuously until you run out of memory or you interrupt it.
	 */
	public void run() {
		while (true) {
			runOnce();
		}
	}
	/**
	 * Gets attempts number of urls from toCrawl and finds all their urls.
	 * 
	 * @param how many times to run
	 */
	public void run(int attempts) {
		for (int i = 0; i < attempts; i++) {
			runOnce();
		}
	}
	/**
	 * Gets one url from jedis key 'toCrawl' connects to it 
	 * and adds all found urls to jedis key 'toParse'. 
	 * 
	 * @return Whether the function ran correctly or not
	 */
	public boolean runOnce(){
		Document doc = null;
		Connection con = null;
		URL urlObj = null;
		String url = jedis.spop("toCrawl");
		if (url == null) return false;
		jedis.sadd("triedCrawl", url);
		
		try {
			urlObj = new URL(url);
		} catch (MalformedURLException e) {
			if (verb) System.out.println("Malformed: " + url);
			return false;
		}

		con = Jsoup.connect(urlObj.toString());
		con.timeout(timeout);
		
		try {
			doc=con.get();
		} catch (IOException e) {
			if (verb) System.out.println("Failed: " + url);
			return false;
		} 

		addLinks(getLinks(doc));
		if (verb) System.out.println("Crawled: "+ url);
		Long index = addIndex(url);
		jedis.rpush("toParse", index.toString() + ":" + getText(doc));
		return true;
	}
/**
 * A wrapper for addLink. This function starts crawling at the given url.
 * 
 * @param url, the url to begin searching at.
 * @return whether or not the function worked.
 */
	public boolean seed(String url) {
		addLink(url);
		return jedis.scard("toCawl")>0;
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
	/**
	 * Adds an index to jedis key 'urlIndex' This operates as a lookup from url to index. 
	 * @param url
	 * @return
	 */
	private long addIndex(String url) {
		Long index = jedis.incr("urlCount");
		jedis.hset("urlIndex", index.toString(), url);
		return (long)index;
	}
	
	/**
	 * Records a url to be crawled if it hasn't already. 
	 * Also keeps track of how many links go to the url for quality scoring purposes.
	 * 
	 * @param link
	 */
	private void addLink(String link){
		if (!jedis.sismember("triedCrawl",link)){
			jedis.sadd("toCrawl", link);
		}
		jedis.zincrby("urlScore", 1, link);
	}
	/**
	 * Adds a multiple of links to be searched. 
	 * A wrapper for addLink.
	 * @param links
	 */
	private void addLinks(HashSet<String> links){
		for(String link: links){
			addLink(link);
		}
	}
	/**
	 * Returns all links found on a webpage. 
	 * 
	 * @param doc
	 * @return Returns all unique links on a page.
	 */
	private static HashSet<String> getLinks(Document doc){
		HashSet<String> uniqueLinks=new HashSet<String>();
		Elements links = doc.getElementsByTag("a");
		for (Element link : links) {
			String absUrl = link.absUrl("href");
			uniqueLinks.add(absUrl);
		}
		return uniqueLinks;
	}
	/**
	 * Gets all text on a webpage
	 * 
	 * @param doc
	 * @return all the text on a webpage
	 */
	private static String getText(Document doc){
		String allText = doc.title() + " " +doc.text();
		return allText;

	}
    
}
