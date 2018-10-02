package luceneController;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.FSDirectory;

import model.ResultVO;


public class ManualSearch {
	List<ResultVO> all = new ArrayList<ResultVO>();
	
	
	String index = "D:\\020518\\Downloads\\ServerFiles\\index";
//	Analyzer analyzer = new SmartChineseAnalyzer();
//	Analyzer analyzer = new IKAnalyzer();
	Analyzer analyzer = new MySameAnalyzer(new SimpleSamewordContext());
	
	
	//根據頁碼和分頁大小獲取上一次的最後一個ScoreDoc
	private ScoreDoc getLastScoreDoc( int pageIndex, int pageSize, Query query, IndexSearcher searcher) throws IOException{
		
		if(pageIndex == 1)return null;//如果是第一頁就返回空
		int num = pageSize*(pageIndex-1); //否則獲取上一頁的數量
		TopDocs tds = searcher.search(query, num);
		return tds.scoreDocs[num-1];
	}
	
	public List<ResultVO> searchByTerm(String name, int preViewNum, double point,  int pageIndex, int pageSize, int maxQuery,String directory){
		
		index = directory+"\\index";   //讓外面傳來的directory取代原來的index變數
		IndexReader reader;
		try {
			
			reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
			IndexSearcher searcher = new IndexSearcher(reader);   //創建searcher
			MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"title","contents"}, analyzer);  //實現多個query，以同時搜尋title和content。
			Query query = parser.parse(name);
		
			System.out.print("要搜尋的字詞為: "+name);
			AnalyzerUtils.displayToken(name, new MySameAnalyzer(new SimpleSamewordContext()));
			
			
			//先獲取上一頁的最後一個元素
			ScoreDoc lastSd = getLastScoreDoc(pageIndex, pageSize, query, searcher);
			//通過最後一個元素搜尋下面的pageSize個元素
			TopDocs tds = searcher.searchAfter(lastSd, query, pageSize); //這個正常程序下的TopDocs
			TopDocs preView = searcher.search(query, maxQuery);               //要預覽符合數量用的TopDocs
			
			//下面純粹是要計算並印出符合的筆數量   
			int numTotalHits = searcher.count(query);
			System.out.println( "_____________________________________________");
			System.out.print("可查詢的極限，筆數為: " + numTotalHits +"筆"+"\n");
			
			//下面是要預先跑一遍迴圈，算出符合目前查詢狀況的筆數，數量有多少個。
			int countMatch = 0; //標題前流水的數字
			for(ScoreDoc sd : preView.scoreDocs){
				float score = sd.score; //系統算出的默認的分數
				if(score<point){  //如果分數過低，就不印出顯示後續的資訊(跳過)。
					continue;
				}
				countMatch++;
			}
			System.out.println( "_____________________________________________");
			System.out.println("實際查詢到的結果，筆數有: " + countMatch +"筆"+"\n");
			
			if(countMatch == 0){  //如果沒有結果，就存一個setAccident(true)，送出去給前端。
				ResultVO resultVO = new ResultVO(); //內部迴圈中用一個區域變數的ResultVO，讓它將值一一存入，並塞給下面名為all的全域List
	            resultVO.setAccident(true);
	            all.add(resultVO); //把區域變數List存好的值塞進全域List當中。
	            return all;
			}

//			int maxPage = (int) (numTotalHits/pageSize)+1; //計算最多可以翻到第幾頁
			int maxPage;
			if(countMatch%pageSize != 0){
				maxPage = (int) (countMatch/pageSize)+1;   //拿自製的countMatch數量來做翻頁
			}
			else{
				maxPage = (int) (countMatch/pageSize);   //拿自製的countMatch數量來做翻頁
			}
			System.out.println("第"+pageIndex + "頁/共 "+maxPage+"頁"+"\n");
			System.out.println("==============================================");
			
			if(pageIndex > maxPage){  //如果超頁
				System.out.println("超過檢視頁數，請重新輸入頁碼範圍!");
//				System.exit(0); 
				ResultVO resultVO = new ResultVO(); //內部迴圈中用一個區域變數的ResultVO，讓它將值一一存入，並塞給下面名為all的全域List
	            resultVO.setAccident(true); //如果有問題，就存一個setAccident(true)，送出去給前端。
	            all.add(resultVO); //把區域變數List存好的值塞進全域List當中。
	            return all;
			}
			
			int count = 1; //標題前流水的數字
			if(pageIndex != 1){      //如果頁碼不是在第一頁，就必須調整count流水號的迴圈起始值
				count = pageSize*(pageIndex-1)+1;  //count流水號的起始值要隨著顯示方式改變
			}
			
			for(ScoreDoc sd:tds.scoreDocs){
				Document doc = searcher.doc(sd.doc);
				
				float score = sd.score; //系統算出的默認的分數
				if(score<point){  //如果分數過低，就不印出顯示後續的資訊(跳過)。
					continue;
				}
				String path = doc.get("path");
				System.out.println((count) + ". " + path);

				// 取得URL(只適用於PTT爬蟲，其他平台的話這一行則需要調整，甚至移除)
				String linkUrl = doc.get("linkUrl");
				
				System.out.println("搜尋分數為: " + score);
				
				String title = doc.get("title");
				String contents = doc.get("contents");
		        
		        //把size字串轉成int變數sizeNum，以調整顯示的格式。
		        String size = doc.get("size");
		        int sizeNum = Integer.parseInt(size);
		        if(sizeNum>= 1024){
		        	sizeNum = sizeNum/1024;
		        }
		        else{
		        	sizeNum = 1;
		        }
		          //加入顯示修改日期的四行程式碼
		          long val =  Long.parseLong(doc.get("modified").toString());      
		          Date date= new Date(val);
		          SimpleDateFormat df2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		          String modified = df2.format(date);
		                  
		          //將文件內容進行高亮顯示(意指，只顯示關鍵詞彙附近的幾行字做展示)
		          QueryScorer scorer = new QueryScorer(query);
		          Fragmenter fragment = new SimpleSpanFragmenter(scorer, preViewNum);  //可調整的顯示字數
		          Formatter formatter = new SimpleHTMLFormatter("<b>","</b>");
		          Highlighter highlighter = new Highlighter(formatter, scorer);
		          highlighter.setTextFragmenter(fragment);
		          String str = null;
		  		try {
//					str = highlighter.getBestFragment(new SmartChineseAnalyzer(),"contents", contents);
		  			str = highlighter.getBestFragment(new MySameAnalyzer(new SimpleSamewordContext()),"contents", contents);
		  			
				} catch (InvalidTokenOffsetsException e) {
					e.printStackTrace();
				}
		  		
		  		if(str == null){
		  			str ="資料無法預覽。";
		  		}

	            System.out.print("   文件標題: " + title);
	            System.out.print("   修改日期: " + modified );
	            System.out.println("   檔案大小: " + sizeNum +"(KB)" );
	            System.out.println("   文件內容: ");
	            System.out.println( str + "\n");
	            System.out.println( "_____________________________________________________");
	              
	            ResultVO resultVO = new ResultVO(); //內部迴圈中用一個區域變數的ResultVO，讓它將值一一存入，並塞給下面名為all的全域List
	            resultVO.setTitle(title);
	            resultVO.setStr(str.trim().replaceAll("\\s+", "")); //把str開頭、結尾的空格去除,在把所有空格移除。
	            resultVO.setModified(modified);
	            resultVO.setPath(path);
	            resultVO.setLinkUrl(linkUrl);
	            resultVO.setCount(count);
	            count++;//為了讓VO存，所以放在這麼下面才++。
	            
	            resultVO.setPossibleLimit(numTotalHits);
	            resultVO.setTotalResults(countMatch);
	            resultVO.setCurrentPage(pageIndex);
	            resultVO.setTotalPage(maxPage);	            
	            
	            all.add(resultVO); //把區域變數List存好的值塞進全域List當中。
	            
			}
			reader.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		catch (ParseException e) {
			e.printStackTrace();
		}
		return all;
		
	
		
	}
	
}
