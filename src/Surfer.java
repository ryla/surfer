import redis.clients.jedis.*;

public class Surfer {

	public static void main(String[] args) {
		Crawler crawler = new Crawler(new Jedis("localhost"));
		crawler.seed("http://www.olin.edu/");
		crawler.verbose();
		Parser parser = new Parser(new Jedis("localhost"));
		parser.verbose();
		crawler.run(100);
		parser.run(100);
	}
}
