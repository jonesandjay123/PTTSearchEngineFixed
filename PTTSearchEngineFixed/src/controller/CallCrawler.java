package controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import crawler.HotForumsGetter;
import crawler.PTTcrawler;

@WebServlet("/CallCrawler")
public class CallCrawler extends HttpServlet {
	private static final long serialVersionUID = 1L;
      
    public CallCrawler() {
        super();
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  	
    	String action = request.getParameter("action");

    	if("start".equals(action)){

    		String forum = request.getParameter("forum");
    		String URL = request.getParameter("URL");
    		String directory = request.getParameter("directory");
    		String push = request.getParameter("pushAmount");
    		int pushAmount = Integer.parseInt(push);
    		String crawlerPg = request.getParameter("crawlerPg");
    		int page = Integer.parseInt(crawlerPg);
    		
    		try {
    			PTTcrawler.crawlerStart(forum, URL, directory, page, pushAmount);
			} catch (ParseException e) {
				e.printStackTrace();
			}
    		
    		String text = "程式爬蟲完畢!!!";
		
    		response.setContentType("text/plain");  // Set content type of the response so that jQuery knows what it can expect.
    	    response.setCharacterEncoding("UTF-8"); // You want world domination, huh?
    	    response.getWriter().write(text);       // Write response body.
    	}
    		
    	if("getForums".equals(action)){
    		Map<String, String> forums = new HashMap<String, String>();
    		
    		String[] args = {};
    		forums = HotForumsGetter.main(args);
    		
    		String json = new Gson().toJson(forums);
            
            response.setContentType("application/json");  // Set content type of the response so that jQuery knows what it can expect.
        	response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);       // Write response body.
    	}
    	
    }




}
