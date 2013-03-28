import java.io.IOException;
import java.util.HashSet;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import redis.clients.jedis.Jedis;


public class Surfer {

	public static void main(String[] args) {
		Jedis jedis = new Jedis("localhost");	
		jedis.sadd("toCrawl", "http://www.olin.edu");
		for (int i = 0; i < 500; i++) {
			Document doc;
			String url = jedis.spop("toCrawl");
			try {
				doc = Jsoup.connect(url).get();
				Elements elems = doc.getElementsByAttribute("href");
				HashSet<String> links = new HashSet<String>();
				for (Element elem: elems) {
					links.add(elem.attr("abs:href"));
				}
				for (String link: links) {
					if (! jedis.sismember("alreadyCrawled", link) && 
							! jedis.sismember("failedCrawl", url)) {
						jedis.sadd("toCrawl", link);
					}
				}
				jedis.sadd("alreadyCrawled", url);
				System.out.println(url);
			} catch (IOException e) {
				jedis.sadd("failedCrawl", url);
			} 
		}
	}


}
