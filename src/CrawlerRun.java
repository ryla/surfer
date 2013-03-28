import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public final class CrawlerRun {
    public static void main(String []args){
   	 
   	 Crawler bug= new Crawler();
   	 
   	 Document newdoc= null;
   	 newdoc= bug.htmlOut("http://www.facebook.com/");
   	 //String title = newdoc.title();
   	 //System.out.println(newdoc.toString());
   	 bug.linkIdentifier(newdoc);
   	 bug.textIdentifier(newdoc);
   	 
   	 //String html = "<html><head><title>First parse</title></head>"
   		 //      + "<body><p>Parsed HTML into a doc.</p></body></html>";
   	 //Document doc = Jsoup.parse(html);
   	 
   	 
   	 //System.out.println(title);
    }
}

