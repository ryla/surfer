import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import redis.clients.jedis.Tuple;

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
	
	/**
	 * This method takes a search keyword and integer n and returns an 
	 * ArrayList of the most relevant URL's of length n.
	 * 
	 * @param keyword
	 * @param n
	 * @return n most relevant search results
	 */
	private Set<Tuple> keywordLookup(String keyword, int n){
		Set<Tuple> urls = new HashSet<Tuple>();
		for (Tuple tup: jedis.zrevrangeWithScores(keyword, 0, n-1)){
			String index = tup.getElement();
			String url = jedis.hget("urlIndex", index);
			urls.add(new Tuple(url, tup.getScore()));
		}
		
		return urls;	
	} 
	
	public int getHighest()
	{
		Set<String> sortedSet = jedis.zrevrange("urlScore", 0, 0);
		Object [] arraySet = sortedSet.toArray();
		int highest = jedis.zscore("urlScore", (String)arraySet[0]).intValue();
		System.out.println(highest);
		
		return highest;
	}
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Searcher s= new Searcher(new Jedis("localhost"));
	}
	
}
