package luceneController;

import java.util.List;

import org.junit.Test;

import model.ResultVO;

public class TestSearch {

	String directory = "D:\\020518\\Downloads\\ServerFiles\\index";
	
	@Test
	public void createIndex(){
		String[] args = {};
		IndexFiles.main(args);
	}
	
	@Test
	public void testManualSearch(){
		ManualSearch bs = new ManualSearch();
		bs.searchByTerm("自然",55, 0.02, 2, 10, 100, directory);
	}
	
	@Test
	public void testPrintList(){
		ManualSearch bs = new ManualSearch();
		List<ResultVO> results = bs.searchByTerm("自然",55, 0.02, 1, 15, 100, directory);
		System.out.println(results);
		System.out.println(results.size());

		for(ResultVO x : results){
			System.out.println(x.getTitle() +" "+ x.getScore() +" "+ x.getModified() +" "+ x.getStr()+" "+ x.getPath());
		}
		System.out.println("\n");
		System.out.println(results.get(2).getTitle());
	}
	
	
	@Test
	public void calculate(){
		int countMatch = 11 ; 
		int pageSize = 5; 
		int maxPage;
		if(countMatch%pageSize != 0){
			maxPage = (int) (countMatch/pageSize)+1;   //拿自製的countMatch數量來做翻頁
		}
		else{
			maxPage = (int) (countMatch/pageSize);   //拿自製的countMatch數量來做翻頁
		}
		System.out.println(maxPage);
	}
	
}
