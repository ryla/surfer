
public class Surfer {

	public static void main(String[] args) {
		Crawler crawler = new Crawler("localhost");
		crawler.seed("http://www.olin.edu/");
		crawler.run(500);
	}
}
