package crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.joda.time.DateTime;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PTTcrawler {
	
    static BufferedWriter writer = null;
    static String domain = "https://www.ptt.cc";
    static String fileDir = null;
    static int pushNum = 0;
    
	public static void crawlerStart(String forum, String URL, String directory,int page, int pushAmount) throws IOException, ParseException{
		
		fileDir = directory;
		pushNum= pushAmount;
		
		System.out.println("PTT爬蟲開始。要爬的頁數數量為: "+page+" 首先執行首頁內容。"+"\n"+"_________________________________________________");

		/**
		 * 因為ptt網頁版的首頁網址為:index.html or index0.html
		 * 跟後面有串數字的非首頁 index20XXX.html明顯不同，所以一開始先獨立跑一遍首頁的方法。
		 * 跑完這個index.html頁面之後，才繼續用迴圈遞減數字往別的頁碼跑。
		 */
//		String URL = "https://www.ptt.cc/bbs/Gossiping/index.html";
		Document doc = CrawlerPack.start()
		    .addCookie("over18","1")  // 必需在 getFromXXX 前設定Cookie滿18歲
		    .getFromHtml(URL);
		
		getIndexPage(doc);  //先送index.html版本的doc給它跑
		System.out.println("首頁內容執行完畢完了!!!"+"\n"+"=================================================");
		
		/**
		 * 首頁完成後，要先取得"上一頁"(ptt叫它做上一頁)按鈕連結的網址，做成新的doc給它跑。然後開始用迴圈，一頁、一頁找下去爬~
		 */
		String number = doc.select(".action-bar").first().childNode(3).childNode(3).attr("href"); //取得上一頁鈕的URI
		number = number.split("index")[1];  //去掉index之前的字串
		int loopNum = Integer.parseInt(number.split(".html")[0]); //去掉.html之後的字串並轉成數字
		int flag = loopNum-page; //維持loop前推的數字差。
		int count = 1;           //顯示目前跑到的是總頁數的第幾頁。
		
		while(loopNum > flag){
			
			String nextPg = domain +"/bbs/"+forum+"/index" + loopNum +".html";
			System.out.println("接著要轉跳到第:"+loopNum+"頁。 "+" 一共有:("+count+"/"+page+")頁"+"\n"+nextPg+"\n"+"=================================================");
			
			doc = CrawlerPack.start()
				    .addCookie("over18","1")  // 必需在 getFromXXX 前設定Cookie
				    .getFromHtml(nextPg);
			getIndexPage(doc);  //做成新的doc給它跑
			count++;
			loopNum--;
			
		}
		System.out.println("");
		System.out.println("程式爬蟲完畢!!!");

	}

	
	/**
	 * index頁面的方法。index頁面也可看到多篇文章，每篇文章的連結就是要從這邊取得。
	 */
	public static void getIndexPage(Document doc) throws IOException, ParseException{

		Elements rtddt = doc.select(".r-ent");   //巢狀要用Elements

		for (Element li : rtddt) {
//			String title = li.select(".title").text();
//			String date = li.select(".date").text();
//			String author = li.select(".author").text();
			String link = domain + li.select("a").attr("href");
			String likes = li.select(".nrec").text();
//			System.out.println("人氣:"+likes+" 標題:"+title + "    日期:" + date + "    作者:" + author + " " + link);
			singleArticle(link, likes);
		}
	}

	
	/**
	 * 擷取單一文章內容的方法
	 */
	public static void singleArticle(String uri, String likes){
		
		Document page = CrawlerPack.start()
		    .addCookie("over18","1")  // 必需在 getFromXXX 前設定Cookie
		    .getFromHtml(uri);

		Elements rtddt = page.select("#main-content");
		
		for (Element div : rtddt) {

			try {
				String author = div.select(".article-metaline > span").get(1).text();
				String title = div.select(".article-metaline > span").get(3).text();
				String time = div.select(".article-metaline > span").get(5).text();
				String article = null;
				
				
				//如果文章內容不是空的，就把最後兩個字給移除。
				if(div.ownText() != null && div.ownText().length() > 0 ){
					article = div.ownText().substring(0,div.ownText().length()-2);
					
					article += "\n"; // Needed to handle last line correctly
					article = article.replaceAll("(.{1,50})\\s+", "$1\n");   //每50個字自動換行。(換行效果只有從Notepad讀取才看的出來)
				}
				else{
					article = "--";
				}
				
				//時間格式轉換
				SimpleDateFormat dateParser = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy", Locale.US);  //PTT時間格式範例為:Fri Dec 31 12:00:00 2016
				Date date = dateParser.parse(time);
				DateTime jodadate = new DateTime(date);
				String currentTime = jodadate.toString("yyyy.MM.dd_HH.mm.ss");

				//寫出檔案前，先排除標題格式問題(存成txt檔時標題有諸多限制，而title將用作存檔時的檔名，因此要預先把標題作處哩，統一轉成底線。)
				File file;
				
				if( title.subSequence(0, 3).equals("Re:") ){  //如果標題為"Re:"
					title = title.substring(3).trim();        //就把Re:的字樣給去掉，移到後面。(目的是為了在資料夾中按標題排序時，可以跟原PO文放置在一起。)
					title = title.replaceAll("['/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' ]", "_");
					if(likes.isEmpty()){    //判斷是否顯示人氣值
						file = new File(fileDir+ "\\" +title+currentTime+"[Re]"+".txt");  //檔名為標題+"[Re]"+轉好格式的時間
					}
					else{
						file = new File(fileDir+ "\\" +title+currentTime+"[Re]"+"人氣"+likes+".txt");  //檔名為標題+"[Re]"+轉好格式的時間
					}
				}
				else{
					title = title.replaceAll("['/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' ]", "_");
					if(likes.isEmpty()){    //判斷是否顯示人氣值
						file = new File(fileDir+ "\\" +title+currentTime+".txt");  //檔名為標題+轉好格式的時間
					}
					else{
						file = new File(fileDir+ "\\" +title+currentTime+"人氣"+likes+".txt");  //檔名為標題+轉好格式的時間
					}
				}
				
				/**
				 *下面三行的PrintStream，是用System.out.print就可以寫出的方法。
				 *雖然直接切換就可以做寫出動作，看似便利，但是一旦啟用了System.setOut之後，
				 *就會讓後面所有的System.out.print都將改為寫出txt，反而無法再從Console.log中做顯示了。
				 *因此為了讓部分內容仍可於Console中做顯示，後面選擇用BufferedWriter來做txt輸出。
				 */
				/*
				FileOutputStream fos = new FileOutputStream(file);
				PrintStream ps = new PrintStream(fos);
				System.setOut(ps);
				
				System.out.println("網址: "+uri);	
				System.out.println("標題: "+title);	
				System.out.println("作者: "+author);	
				System.out.println("時間: "+time);	
				System.out.println("");
				System.out.println("文章內容: "+"\n"+article);
				System.out.println("");	
				*/
				writer = new BufferedWriter(new FileWriter(file));

				writer.write("網址: "+uri);
				writer.newLine();
				writer.write("標題: "+title);
				writer.newLine();
				writer.write("作者: "+author);
				writer.newLine();
				writer.write("時間: "+time);
				writer.newLine();
				writer.newLine();
				writer.write("文章內容: "+"\n"+article);
				writer.newLine();

				
				//以下是有人推文，才會繼續做下去的動作。
				if(div.select("div").hasClass("push")){

					int lineOfPush = div.select(".push").size();
//					System.out.println("========");
//					System.out.println("回文行數為: "+lineOfPush+"行(主要用以判斷迴圈要繞的次數，最多只展示前pushNum則回文。)");
//					System.out.println("");
					
					writer.write("========");
					writer.newLine();
					writer.write("回文行數為: "+lineOfPush+"行");  //(主要用以判斷迴圈要繞的次數，最多只展示前pushNum則回文。)"
					writer.newLine();
					writer.newLine();
					writer.newLine();
				
					
					
					//假設pushNum=100，則如果回應篇數超過100則，設定迴圈只跑出前100筆。
					if(lineOfPush > pushNum){
						lineOfPush = pushNum;
					}

					int like = 0;
					int dislike = 0;
					String pusher = null;
					String pushcontent = null;
					
					for(int x=0; x<lineOfPush; x++){
						
						String pushType = div.select(".push").get(x).select("span").get(0).text();
						if(pushType.equals("推")){
							like++;
						}
						if(pushType.equals("噓")){
							dislike++;
						}
				
						pusher = div.select(".push").get(x).select("span").get(1).text();
						//如果文章的內容只有":"或是長度只有1(皆表示該使用者沒打任何東西)，則發文內容以""展示。
						if( div.select(".push").get(x).select("span").get(2).text().equals(":") || (div.select(".push").get(x).select("span").get(2).text().trim()).length() == 1){
							pushcontent= "";
						}
						//反之，則自動去掉頭兩個字元": "，然後展示內容。
						else{
							pushcontent = div.select(".push").get(x).select("span").get(2).text().substring(2);
						}
//						System.out.println((x+1)+" "+pushType+" "+pusher+": "+pushcontent);	

						writer.write((x+1)+" "+pushType+" "+pusher+": "+pushcontent);
						writer.newLine();
						
					}
//					System.out.println("");
//					System.out.print("推:"+like+" ");
//					System.out.println("  噓:"+dislike);
//					System.out.println("___________________________________");
					
					writer.newLine();
					writer.write("推:"+like+" "+"  噓:"+dislike);
					writer.newLine();
					writer.write("___________________________________");
					writer.newLine();
					
				}
				
				//否則，沒人推文就跳過繼續下一筆。
				else{
//					System.out.println("___________________________________");
					
					writer.write("___________________________________");
					writer.newLine();
					continue;
				}
				
			} catch (ParseException e) {
				e.printStackTrace();
				continue;
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} catch (Exception e){
				e.printStackTrace();
				continue;
			}
			
		}
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	

	
}
