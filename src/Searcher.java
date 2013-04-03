import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;

import redis.clients.jedis.*;

/**
 * 
 */

/**
 * @author Chloe Eghtebas
 * @author Reily Buttler
 *
 */ 
public class Searcher {
	private Jedis jedis;
	
	public Searcher(Jedis jedis){
		this.jedis = jedis;
	}
	
	public ArrayList<URL> lookup(String keyword, int n){
		ArrayList<URL> urls = new ArrayList<URL>();
		for (String index: jedis.zrevrange(keyword, 0, n -1)) {
			String url = jedis.hget("urlIndex", index);
			try {
				urls.add(new URL(url));
			} catch (MalformedURLException e) { }
		}
		
		return urls;	
	} 
	
	public static void main(String[] args) {
		Searcher s= new Searcher(new Jedis("localhost"));
		ArrayList<URL> urlist= s.lookup("olin" ,5);
		System.out.println(urlist.toString());
	}
	
}
