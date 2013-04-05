import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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

	public List<String> search(String keyword, int n){
		SortedSet<Tuple> finalOrder=new TreeSet<Tuple>();
		Map<String,Double>terms = keywordLookup(keyword,n);
		double maxQual = maxQuality();
		double docFreq = docFreq(keyword);
		for (String url: terms.keySet()){
			double qualScore = jedis.zscore("urlScore", url);
			double normalizedQual =  qualScore / maxQual;
			double normalizedRel= terms.get(url) / docFreq;
			double finalScore = (normalizedQual+normalizedRel)/2;
			finalOrder.add(new Tuple(url,finalScore));			
		}
		List<String> toReturn= new ArrayList<String>();
		for(Tuple tup: finalOrder){
			toReturn.add(tup.getElement());
		}
		return toReturn;
	}
	
	private double docFreq(String keyword) {
		return jedis.zscore("globalKeywords", keyword);
	}
	
	/**
	 * This method takes a search keyword and integer n and returns an 
	 * ArrayList of the most relevant URL's of length n.
	 * 
	 * @param keyword
	 * @param n
	 * @return n most relevant search results
	 */
	private Map<String,Double> keywordLookup(String keyword, int n){
		Hashtable<String,Double> urls = new Hashtable<String,Double>();
		for (Tuple tup: jedis.zrevrangeWithScores(keyword, 0, n-1)){
			String index = tup.getElement();
			String url = jedis.hget("urlIndex", index);
			urls.put(url, tup.getScore());
		}
		
		return urls;	
	} 
	
	private double maxQuality()
	{
		Set<String> sortedSet = jedis.zrevrange("urlScore", 0, 0);
		Object [] arraySet = sortedSet.toArray();
		double highest = jedis.zscore("urlScore", (String)arraySet[0]);
		
		return highest;
	}
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Searcher s= new Searcher(new Jedis("localhost"));
		System.out.println(s.search("olin",5));
	}
	
}
