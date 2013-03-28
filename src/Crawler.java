import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 *
 */

/**
 * @author neghtebas and brendan ritter
 *
 */
public class Crawler {

    public Document htmlOut(String url){
   	 Document doc=null;
   	 try {
   		 doc = Jsoup.connect(url).get();
   	 } catch (IOException e) {
   		 // TODO Auto-generated catch block
   		 e.printStackTrace();
   	 }
   	 //Gets website's title
   	 //String title = doc.title();
   	 return doc;
    }
    
    //Identifies all the absurl links in a given html doc.
    public HashSet<String> linkIdentifier(Document doc){
   	 HashSet<String> uniqueLinks=new HashSet<String>();
   	 Elements links = doc.getElementsByTag("a");
   	 for (Element link : links) {
   	   String absUrl = link.absUrl("href");
   		 //System.out.println(link.text());
   	   uniqueLinks.add(absUrl);
   	 }
   	 return uniqueLinks;
    }
    public String textIdentifier(Document doc){
   	 //Element head=doc.head();
   	 //String allText=head.text();
   	 String allText= doc.text();
   	 System.out.println(doc.text());
   	 return allText;
   	 
    }
    public void emit(String curUrl,HashSet<String> allLinks,String allText){    
   	 HashMap<String, String[]> indexin= new HashMap<String, String[]>();
   	 //Somehow sent to indexer
    }
    
}
