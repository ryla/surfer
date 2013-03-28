import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 *
 */

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

/**
 * @author neghtebas and brendan ritter
 *
 */
public class Crawler {

	private Jedis jedis;
	
	private void addLinks(HashSet<String> links){
		for(String link: links){
			addLink(link);
		}
	}
	
	public boolean seed(String url) {
		addLink(url);
		return jedis.scard("toCawl")>0;
	}
	
	private void addLink(String link){
		if (!jedis.sismember("triedCrawl",link)){
			jedis.sadd("toCrawl", link);
		}
	}
	
	public Crawler(String hostname){
		this.jedis=new Jedis(hostname);
	}
	
	public void run() {
		while (true) {
			runOnce();
		}
	}
	
	public void run(int count) {
		for (int i = 0; i < count; i++) {
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
			return false;
		} 

		addLinks(getLinks(doc));
		
		return true;
	}
    public Document htmlOut(String url){
   	 Document doc=null;
   	 try {
   		 doc = Jsoup.connect(url).get();
   	 } catch (IOException e) {
   		 // TODO Auto-generated catch block
   		 e.printStackTrace();
   	 }
   	 //Gets website's title
   	 //String title = doc.title();
   	 return doc;
    }
    
    //Identifies all the absurl links in a given html doc.
    private static HashSet<String> getLinks(Document doc){
   	 HashSet<String> uniqueLinks=new HashSet<String>();
   	 Elements links = doc.getElementsByTag("a");
   	 for (Element link : links) {
   	   String absUrl = link.absUrl("href");
   		 //System.out.println(link.text());
   	   uniqueLinks.add(absUrl);
   	 }
   	 return uniqueLinks;
    }
    
    private static String getText(Document doc){
   	 //Element head=doc.head();
   	 //String allText=head.text();
   	 String allText= doc.text();
   	 System.out.println(doc.text());
   	 return allText;
   	 
    }
    
}
