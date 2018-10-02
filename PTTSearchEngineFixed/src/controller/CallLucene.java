package controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import luceneController.IndexFiles;
import luceneController.ManualSearch;
import model.ResultVO;

@WebServlet("/CallLucene")
public class CallLucene extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public CallLucene() {
        super();
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  	
    	String action = request.getParameter("action");
    	
    	if("search".equals(action)){
    				
    		String value = request.getParameter("value");
    		String num1 = request.getParameter("accuracy");
    		double accuracy =Double.parseDouble(num1) ;
    		
    		String num2 = request.getParameter("pageRange");
    		int pageRange = Integer.parseInt(num2);
    		
    		String num3 = request.getParameter("currentPage");
    		int currentPage = Integer.parseInt(num3);
    		
    		String num4 = request.getParameter("preViewNum");
    		int preViewNum = Integer.parseInt(num4);
    		
    		String num5 = request.getParameter("maxQuery");
    		int maxQuery = Integer.parseInt(num5);
    		
    		
    		String directory = request.getParameter("directory");
    		
    		ManualSearch bs = new ManualSearch();
    		//List<List> results = bs.searchByTerm(value, 0.02);
    		List<ResultVO> results = bs.searchByTerm(value, preViewNum, accuracy, currentPage, pageRange, maxQuery,directory);
    		
            String json = new Gson().toJson(results);
            
            response.setContentType("application/json");  // Set content type of the response so that jQuery knows what it can expect.
        	response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);       // Write response body.

		}

    	if("index".equals(action)){
 		
    		String directory = request.getParameter("directory");
    		
    		String[] args = {directory};
    		IndexFiles.main(args);
    		
    		String text = "索引建立成功!!";
    		System.out.println(text);
    		
    		response.setContentType("text/plain");  // Set content type of the response so that jQuery knows what it can expect.
    	    response.setCharacterEncoding("UTF-8"); // You want world domination, huh?
    	    response.getWriter().write(text);       // Write response body.

    	}
 
    	
    }


}
