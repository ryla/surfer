import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import redis.clients.jedis.Tuple;

import redis.clients.jedis.*;

/**
 * @author Chloe Eghtebas
 * @author Riley Butler
 * @author Pati Martin
 * @author Brendan Ritter
 */ 
public class Searcher {
	private Jedis jedis;
	private int nResults = 10;
	
	public Searcher(Jedis jedis){
		this.jedis = jedis;
	}

	/**
	 * Find URLs containing your search query.
	 * 
	 * @param query A string to search for. Currently fails on non-alphanumeric strings.
	 * @return List of URLs as Strings, sorted by quality and relevance.
	 */
	public List<String> search(String query){
		List<String> toReturn= new ArrayList<String>();
		for(Tuple tup: searchKeyword(query.toLowerCase())){
			toReturn.add(tup.getElement());
		}
		return toReturn;
	}
	
	/**
	 * Find URLs containing a single sanitized keyword.
	 * 
	 * @param keyword Keyword to find.
	 * @return A set of Tuples, containing URL and score.
	 */
	private Set<Tuple> searchKeyword(String keyword) {
		SortedSet<Tuple> finalOrder=new TreeSet<Tuple>();
		Map<String,Double>terms = keywordLookup(keyword);
		double maxQual = maxQuality();
		double docFreq = docFreq(keyword);
		for (String url: terms.keySet()){
			double qualScore = jedis.zscore("urlScore", url);
			double normalizedQual =  qualScore / maxQual;
			double normalizedRel= terms.get(url) / docFreq;
			double finalScore = (normalizedQual+normalizedRel)/2;
			finalOrder.add(new Tuple(url,finalScore));			
		}
		return finalOrder;
	}
	
	/**
	 * Get the document frequency of a sanitized keyword.
	 * 
	 * @param keyword Keyword to look up.
	 * @return The global number of times keyword was encountered.
	 */
	private double docFreq(String keyword) {
		return jedis.zscore("globalKeywords", keyword);
	}
	
	/**
	 * Get up to nResults of the top URLs containing keyword, sorted by relevance.
	 * 
	 * @param keyword Sanitized keyword to look up.
	 * @return nResults most relevant search results
	 */
	private Map<String,Double> keywordLookup(String keyword){
		Hashtable<String,Double> urls = new Hashtable<String,Double>();
		for (Tuple tup: jedis.zrevrangeWithScores(keyword, 0, nResults-1)){
			String index = tup.getElement();
			String url = jedis.hget("urlIndex", index);
			urls.put(url, tup.getScore());
		}
		
		return urls;	
	} 
	
	/**
	 * Find the highest incoming link count of any URL crawled.
	 * 
	 */
	private double maxQuality()
	{
		Set<String> sortedSet = jedis.zrevrange("urlScore", 0, 0);
		Object [] arraySet = sortedSet.toArray();
		double highest = jedis.zscore("urlScore", (String)arraySet[0]);
		
		return highest;
	}
	
	public static void main(String[] args) {
		Searcher s= new Searcher(new Jedis("localhost"));
		System.out.println(s.search("olin"));
	}
	
}
