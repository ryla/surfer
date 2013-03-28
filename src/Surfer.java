
public class Surfer {

	public static void main(String[] args) {
		Crawler crawler = new Crawler("localhost");
		crawler.seed("http://www.olin.edu/");
		crawler.verbose();
		Parser parser = new Parser("localhost");
		parser.verbose();
		crawler.run(100);
		parser.run(100);
	}
}
