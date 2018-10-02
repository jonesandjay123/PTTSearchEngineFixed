package luceneController;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.wltea.analyzer.lucene.IKTokenizer;



public class MySameAnalyzer extends Analyzer {

	private SamewordContext samewordContext;
	
	public MySameAnalyzer(SamewordContext swc) {
		samewordContext = swc;
	}
	

	   public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET; 
	   
	    @Override
	    protected TokenStreamComponents createComponents(String fieldName) {

	        final IKTokenizer src = new IKTokenizer();
	        TokenStream tok = new MySameTokenFilter(src, samewordContext);
	        
	        return new TokenStreamComponents(src, tok);
	    }
	   

	    
}
