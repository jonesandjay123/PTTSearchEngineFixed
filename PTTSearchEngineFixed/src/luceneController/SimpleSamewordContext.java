package luceneController;

import java.util.HashMap;
import java.util.Map;

public class SimpleSamewordContext implements SamewordContext {

	Map<String, String[]> maps = new HashMap<String, String[]>();
	public SimpleSamewordContext() {
		maps.put("台灣", new String[]{"中華民國","福爾摩沙"});
		maps.put("中華民國", new String[]{"台灣","福爾摩沙"});
		maps.put("福爾摩沙", new String[]{"台灣","中華民國"});
		maps.put("大安區", new String[]{"天龍國"});
		maps.put("天龍國", new String[]{"大安區"});
	}
	
	@Override
	public String[] getSamewords(String name) {
		return maps.get(name);
	}

}
