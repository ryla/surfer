import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import redis.clients.jedis.Jedis;


public class Surfer {

	public static void main(String[] args){
		Jedis crawled = new Jedis("localhost");	
		crawled.select(0);
		Jedis found = new Jedis("localhost");
		found.select(1);
		found.set("http://www.olin.edu", "0");
		for (int i = 0; i < 500; i++) {
			Document doc;
			try {
				String url = found.randomKey();
				doc = Jsoup.parse(new URL(url), 5000);
				Elements elems = doc.getElementsByAttribute("href");
				HashSet<String> links = new HashSet<String>();
				for (Element elem: elems) {
					links.add(elem.attr("abs:href"));
				}
				for (String link: links) {
					if (! crawled.exists(link)) {
						found.set(link, "0");
					}
				}
				found.move(url, 0);
				System.out.println(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


}
