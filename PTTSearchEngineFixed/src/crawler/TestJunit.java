package crawler;

import org.junit.Test;

public class TestJunit {

	@Test
	public void tryToPrint(){
		
		String s1 = "Remove Last CharacterY";
		
		String n1 = s1.substring(0,s1.length()-2);
		
		System.out.println(n1);
	}
	
	
	@Test
	public void testStringPrint(){
		String text = "Re: [新聞] 瓜皮式安全帽噴飛！　2國中生削頭慘死";
//		System.out.println(text);
		System.out.println(text.substring(0, 2).equals("Re"));
		
		String t1 = text.substring(3).trim();
		System.out.println(t1);
	
		
		
	}
	
}
