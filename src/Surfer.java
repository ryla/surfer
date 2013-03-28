import redis.clients.jedis.*;

public class Surfer {

	public static void main(String[] args) {
		Crawler crawlerSeed = new Crawler(new Jedis("localhost"));
		crawlerSeed.seed("http://www.olin.edu/");
		Parser parser = new Parser(new Jedis("localhost"));
		parser.verbose();
		Thread[] crawlerPool = new Thread[10];
		for(Thread thread: crawlerPool){
			Crawler crawler = new Crawler(new Jedis("localhost"));
			crawler.verbose();
			thread = new Thread(crawler);
			thread.start();
		}
		Thread parserThread= new Thread(parser);
		parserThread.start();
	}
}
