package luceneController;

import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.util.CharArraySet;

public class MyStopAnalyzer extends Analyzer {
	
	
	@SuppressWarnings("rawtypes")
	private Set stops;
	@SuppressWarnings("unchecked")
	public MyStopAnalyzer(String[]sws){
		//會自動將字串數組轉換為set
		stops = StopFilter.makeStopSet(sws, true);
		//將原有的停用詞加入到現在的停用詞中
		stops.addAll(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
	}

	public MyStopAnalyzer(){
		//獲取原來的停用詞
		stops = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
	}
	
	public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;  
	
   @Override
    protected TokenStreamComponents createComponents(String fieldName) {

        final ClassicTokenizer src = new ClassicTokenizer();
        TokenStream tok = new StandardFilter(src);
        tok = new StopFilter(new LowerCaseFilter(tok), (CharArraySet) stops);
  
        return new TokenStreamComponents(src, tok);
    }
	

}
