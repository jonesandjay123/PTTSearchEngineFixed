package luceneController;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class AnalyzerUtils {

	public static void displayToken(String str, Analyzer a){
		try {
			TokenStream stream = a.tokenStream("content", new StringReader(str));
			//創建一個屬性，這個屬性會添加在流中，隨著這個TokenStream增加
			CharTermAttribute cta = stream.addAttribute(CharTermAttribute.class); //用來存相應的詞彙
			
			stream.reset();  //新版要加這一行。
			
			while(stream.incrementToken()){
				System.out.print("["+cta+"]");
			}
			System.out.println();
			stream.end();
			stream.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void displayAllTokenInfo(String str, Analyzer a){
		try {
			TokenStream stream = a.tokenStream("content", new StringReader(str));
			//位置增量的屬性: 元素與元素間的空格(語彙單元之間的距離)
			PositionIncrementAttribute pia = stream.addAttribute(PositionIncrementAttribute.class);
			
			//每個語彙單元的位置偏移量
			OffsetAttribute oa = stream.addAttribute(OffsetAttribute.class);
			
			//每一個語彙單元的信息(分詞信息)
			CharTermAttribute cta = stream.addAttribute(CharTermAttribute.class);
			
			//使用分詞器的類型信息
			TypeAttribute ta = stream.addAttribute(TypeAttribute.class);
			
			stream.reset();   //新版要加這一行。
			
//			for(;stream.incrementToken();){
			while(stream.incrementToken()){
				System.out.print(pia.getPositionIncrement()+":");
				System.out.print(cta+"["+oa.startOffset()+"-"+oa.endOffset()+"]-->"+ta.type()+"\n");
			}
			System.out.println();
			stream.end();
			stream.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 自己額外找來用的方法，可掃瞄出文字頁面當中出現的第一筆URL。
	 */
	public static String extractUrls(String text)
	{
	    ArrayList<String> containedUrls = new ArrayList<String>();
	    String result = null;
	    String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
	    Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
	    Matcher urlMatcher = pattern.matcher(text);

	    while (urlMatcher.find())
	    {
	        containedUrls.add(text.substring(urlMatcher.start(0),urlMatcher.end(0)));
	        break;  //加入這行break，就可以再取得第一筆URL時跳出。
	    }

	    result = containedUrls.toString();
	    result = result.substring(1, result.length()-1);  //去掉頭尾的[]字元。
	    return result;
	}
	
	/**
	 * 自己額外找來用的方法，可掃瞄出文字頁面當中出現的所有URL。
	 */
	public static List<String> extractAllUrls(String text)
	{
	    List<String> containedUrls = new ArrayList<String>();
	    String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
	    Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
	    Matcher urlMatcher = pattern.matcher(text);

	    while (urlMatcher.find())
	    {
	        containedUrls.add(text.substring(urlMatcher.start(0),urlMatcher.end(0)));
	        break;  //加入這行break，就可以再取得第一筆URL時跳出。
	    }

	    return containedUrls;
	}
	
}
