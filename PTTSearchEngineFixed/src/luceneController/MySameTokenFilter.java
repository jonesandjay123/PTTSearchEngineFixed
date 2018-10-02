package luceneController;

import java.io.IOException;
import java.util.Stack;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

public class MySameTokenFilter extends TokenFilter {

	private CharTermAttribute cta = null;
	private PositionIncrementAttribute pia = null;
	private AttributeSource.State current;
	private Stack<String> sames = null;
	private SamewordContext samewordContext;
	
	protected MySameTokenFilter(TokenStream input, SamewordContext samewordContext) {
		super(input);
		cta = this.addAttribute(CharTermAttribute.class);
		pia = this.addAttribute(PositionIncrementAttribute.class);
		sames = new Stack<String>();
		this.samewordContext = samewordContext;
	}

	@Override
	public boolean incrementToken() throws IOException {
		
		if(sames.size()>0){
			//將元素出棧，並且獲取這個同意詞
			String str = sames.pop();
			//還原狀態
			restoreState(current);
			cta.setEmpty();
			cta.append(str);
			//設置位置為0
			pia.setPositionIncrement(0);
			return true;
		}
		
		if(!input.incrementToken()) return false;
		
		if(addSames(cta.toString())){
			//如果有同義詞，將當前狀態保存
			current = captureState();
		}
		return true;
	}
	
	private boolean addSames(String name){
	
		String[] sws = samewordContext.getSamewords(name);
		if(sws!=null){
			for(String str:sws){
				sames.push(str);
			}
			return true;
		}
		return false;
	}

}
