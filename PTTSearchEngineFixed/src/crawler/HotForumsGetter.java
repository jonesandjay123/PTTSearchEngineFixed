package crawler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HotForumsGetter {

	static Map<String, String> forums = new LinkedHashMap<String, String>();
	static String uri = "https://webptt.com/Hot.html";
	
	public static Map<String, String> main(String[] args){
		
		Document page = CrawlerPack.start()
			    .addCookie("over18","1")  // 必需在 getFromXXX 前設定Cookie
			    .getFromHtml(uri);

			Elements rtddt = page.select("tr");  //找到當中的每一個tr元素。
			
			for (Element tr : rtddt) {
				if(tr == rtddt.get(0)) continue; //第一筆資料，跳過。
				if(!tr.hasText())      continue; //沒有顯示字串的，跳過。
					
				String URN = tr.child(0).text(); //串網頁URL需要的關鍵字
				String title = tr.text();        //URN+中文註釋
				forums.put(title, URN);
			
//				System.out.println("URN:"+URN+"  標題:"+title);	
			}
			
//			System.out.println("組共有:"+forums.size());
			return forums;
	}
}
