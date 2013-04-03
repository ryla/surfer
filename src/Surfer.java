import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;

import redis.clients.jedis.*;

public class Surfer {
	
	public static void getHighest()
	{
		
		Jedis jedis = new Jedis("localhost");
		Set<String> sortedSet = jedis.zrevrange("urlScore", 0, 0);
		Object [] arraySet = sortedSet.toArray();
		int highest = jedis.zscore("urlScore", (String)arraySet[0]).intValue();
		System.out.println(highest);
	}

	public static void main(String[] args) {
		Crawler crawlerSeed = new Crawler(new Jedis("localhost"));
		crawlerSeed.seed("http://loquimity.org/c.html");
		Parser parser = new Parser(new Jedis("localhost"));
		//parser.verbose();
		Thread[] crawlerPool = new Thread[10];
		for(Thread thread: crawlerPool){
			Crawler crawler = new Crawler(new Jedis("localhost"));
			//crawler.verbose();
			thread = new Thread(crawler);
			thread.start();
		}
		Thread parserThread= new Thread(parser);
		parserThread.start();
		getHighest();
	}
	

}
