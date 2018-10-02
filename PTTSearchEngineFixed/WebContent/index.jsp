<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"> -->
<script src="js/jquery-3.1.1.min.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>搜尋的頁面</title>
<style>
 font{
 	padding-left: 40px;
 	font-size: 150%;
 }
 p{
 	padding-left: 12px;
 	display: inline-block;
 }
 
 b{
 	font-size: 20px;
 	color: red;
 }
 
</style>

<script >

//程式的進入點
$(function() {
	init();
});

//清理程式-清空原先的div內容和符合筆數的紅字
function empty(){  
	$('#displayBox').empty();   //先清空原先的div內容
	$('#totalResult').text(""); //清空符合筆數的紅字
	$('#pageSelector').empty(); //清空跳頁下拉選單的內容
	$('#totalPage').empty();
}	

//專門用來更新頁面的function
function updatePage(currentPage, pageRange, maxQuery) {
	var value = $.trim($("#inputBox").val());
	if( value ){      //如果inputBox裏頭是有值的話。
		
		var accuracy = $('#slider').val();  //抓到span當前的數值
		var preViewNum= $('#preView').val();//抓到預覽數字當前的數值
		var directory = $("#regestPath").val(); //檔案路徑
		
		$.ajax({
	  		url: "CallLucene" ,
	  		method:'GET',
	  		data: {	"action": "search", "value" : value, "preViewNum": preViewNum, "accuracy": accuracy, "pageRange": pageRange, "currentPage":currentPage, "maxQuery":maxQuery, "directory":directory},
	  		dataType: 'json',
	  		success: function(res){
	  					
	  			var totalResult = res.length; //計算這筆Ajax資料回傳的陣列有多長(正常的狀況下，長度要是pageRange的數值)
		   			
	   			var count = 0;
		   		$.each(res, function(index, result) { // Iterate over the JSON array.
		   			 
		   			if(result.accident){  //送來的結果，如果有出現異常時，在這邊接。
		   				$('#totalResult').text("沒有符合的搜尋結果!"); //顯示在符合筆數
	   				}
		   			else{
		   			
			            $("<font color=blue>"+result.count+". "+result.title+"</font><p>"+result.str+"</p><p>"+result.modified+"</p><BR><a href="+'file:///'+result.path+" style='padding-left: 60px'>"+result.path+"</a>     <BR><a href="+result.linkUrl+" target='_blank' style='padding-left: 60px'>"+result.linkUrl+"</a>     <hr>").appendTo($("#displayBox"));      // Create HTML <li> element, set its text content with currently iterated item and append it to the <ul>.
				        count++;  
			            
			   			if(count == totalResult){ //當迴圈跑到這筆Ajax資料裡面的最後一條的時候，要做的事。 
		   				
			   				currentPage = result.currentPage;
			   				$('#currentPage').text(currentPage); //顯示在第幾頁
			   					
			   				totalPage = result.totalPage; 
			   				$('#totalPage').text(totalPage);     //顯示共幾頁
			   			
			   				var matchCount = result.totalResults; 
			   				$('#totalResult').text(matchCount+"筆"); //顯示在符合筆數
			   				
			   				$("#pageSelector").empty();   //清空掉select下拉選單當中的內容
			   				for(i=1; i<=totalPage; i++ ){
			   					$('<option value='+i+'>第&nbsp; '+i+' &nbsp;頁</option>').appendTo($("#pageSelector"));  //用迴圈把頁數塞回去
			   				}	
			   				
			   				$('#pageSelector').find('option[value='+currentPage+']').attr("selected", true);  //自動將選項更新為當前頁數 (與上面的效果仍有點不同，本例當中適合用這個)
			   				console.log("(隱藏數值)搜尋引擎可能搜出的最大筆數: "+result.possibleLimit); //為隱藏數值。
		   				}
		   			}
		   		});
	  		
	  		},
	  		beforeSend:function(){    //事前先放動畫
	  			empty();              //清空原先的div內容和符合筆數的紅字
                $('#loading_img').show();
            },
            complete:function(){      //最終動畫消失
                $('#loading_img').hide();
            }
	  		
		});
	
	}
	else{
		alert("搜尋的內容是空的!");
	}
}


function init(){
	
	var currentPage;  //目前在第幾頁
	var pageRange;    //一頁要顯示多少筆資料
	var totalPage;    //每次的查詢，一共有幾頁
	var maxQuery;     //後端要查詢出多少筆符合的資料出來
	
	//slider的動作
	$("#slider").change(function() {      //span當中的數，會隨著拉動而變動。
		var accuracy = $('#slider').val();  
		$('#range').text(accuracy);
		currentPage = 1; //改變查詢時，永遠要先將目前頁面拉回第一頁
		pageRange = $('#selectPage option:selected').val();
		maxQuery = $('#maxQuery option:selected').val();
		
		if($('#inputBox').val()!='' && currentPage!= null && pageRange != null){
			updatePage(currentPage, pageRange, maxQuery);
		}
	});
	
	//preView slider的動作
	$("#preView").change(function() {      //span當中的數，會隨著拉動而變動。
		var preViewNum = $('#preView').val();  
		$('#preViewNum').text(preViewNum);
		currentPage = 1; //改變查詢時，永遠要先將目前頁面拉回第一頁
		pageRange = $('#selectPage option:selected').val();
		maxQuery = $('#maxQuery option:selected').val();
		
		if($('#inputBox').val()!='' && currentPage!= null && pageRange != null){
			updatePage(currentPage, pageRange, maxQuery);
		}
	});
	
	//顯示筆數下拉選單改變時的動作
	$("#selectPage").change(function() { 
		currentPage = 1; //改變每頁顯示時，永遠要先將目前頁面拉回第一頁
		pageRange = $('#selectPage option:selected').val();
		maxQuery = $('#maxQuery option:selected').val();
		
		if($('#inputBox').val()!='' && currentPage!= null && pageRange != null){
			updatePage(currentPage, pageRange, maxQuery);
		}
	});
	
	//顯示筆數下拉選單改變時的動作
	$("#maxQuery").change(function() { 
		currentPage = 1; //改變每頁顯示時，永遠要先將目前頁面拉回第一頁
		pageRange = $('#selectPage option:selected').val();
		maxQuery = $('#maxQuery option:selected').val();
		
		if($('#inputBox').val()!='' && currentPage!= null && pageRange != null){
			updatePage(currentPage, pageRange, maxQuery);
		}
	});
	
	//頁面下拉選單改變時的動作
	$("#pageSelector").change(function() {      
		currentPage = $('#pageSelector option:selected').val(); 
		pageRange = $('#selectPage option:selected').val(); 
		maxQuery = $('#maxQuery option:selected').val();
		
// 		if($('#inputBox').val()!='' && currentPage!= null && pageRange != null){
			updatePage(currentPage, pageRange, maxQuery);
// 		}
	});
	
	//上一頁的反應
	$( "#lastPage" ).click(function(){
		currentPage = $('#pageSelector option:selected').val();  //找出現在在第幾頁 
		if(currentPage > 1 ){	//如果目前頁數大於一，就讓點下去的反應變成頁數減一。	
			currentPage = currentPage-1;
			$("#pageSelector").val(currentPage).change(); //將選項內容做調整
// 			$('#pageSelector').find('option[value='+currentPage+']').attr("selected", true);  //自動將選項更新為當前頁數 (與上面的效果仍有點不同，本例當中適合用這個)
		}
	});
	
	//下一頁的反應
	$( "#nextPage" ).click(function(){
		currentPage = $('#pageSelector option:selected').val();  //找出現在在第幾頁 	
		totalPage= $('#totalPage').text();                       //找出一共有多少頁
		if( currentPage != totalPage ){	//如果目前頁數不等於總頁數，就讓點下去的反應變成頁數加一。	
			currentPage++;
			$("#pageSelector").val(currentPage).change(); //將選項內容做調整
// 			$('#pageSelector').find('option[value='+currentPage+']').attr("selected", true);  //自動將選項更新為當前頁數 (與上面的效果仍有點不同，本例當中適合用這個)
		}
	});
	
	//查詢鍵的動作
	$( "#search" ).click(function(){
		currentPage = 1; 
		pageRange = $('#selectPage option:selected').val(); 
		maxQuery = $('#maxQuery option:selected').val();
		updatePage(currentPage, pageRange, maxQuery);	
	});
			
	//清空鍵的動作
	$( "#clear" ).click(function(){
		empty();
		$('#inputBox').val(''); //清空搜尋內容
		$('#selectPage option:eq(1)').prop("selected", true); //"每頁顯示"回到第二個選項
		$('#maxQuery option:eq(1)').prop("selected", true); //"要查詢出多少筆符合的資料"回到第二個選項
		$('#slider').prop("value","0.02"); //查詢精準度回到0.02
		$('#preView').prop("value","50"); //預覽字數回到50
		$('#range').text('0.02(預設值)');          //span當中的顯示數值也回到0.02
		
	});  
	
	//更新索引的動作
	$( "#index" ).click(function() {
		empty();  //清空原先的div內容和符合筆數的紅字
		$('#selectPage option:eq(1)').prop("selected", true); //"每頁顯示"回到第一個選項
		$('#maxQuery option:eq(1)').prop("selected", true); //"要查詢出多少筆符合的資料"回到第二個選項
		$('#slider').prop("value","0.02"); //查詢精準度回到0.02
		$('#preView').prop("value","50"); //預覽字數回到50
		$('#range').text('0.02(預設值)');    
		$('#loading_img').show(); //開始跑讀取的動畫
		
		var directory = $("#regestPath").val();
		
		$.ajax({
	  		url: "CallLucene" ,   //運行建立索引的Controller
	  		method:'GET',
	  		data: {	"action": "index" , "directory":directory},
	  		dataType: 'json',
	  		complete: function(res){
	  			$('#loading_img').hide(); //最終動畫消失
	  			console.log(res);
	  			console.log(res.responseText);
	  			alert(res.responseText);
	  		}
		});
		
	});
	
	//下拉選單切換要選擇的看板。
	$("#selectForum").change(function(){
		var forum = $( "#selectForum option:selected" ).val();
		$("#forumName").text(forum);
	});
	
	//radio選項的改變，改變URL顯示路徑
	$("#pageType input").on("change",function(){
		pageChecker();
	});
	
	//運行頁數的變動。
	$("#myNumber").change(function(){
		pageChecker();
	});
	
	//共用的方法。
	function pageChecker(){
		var option = $( 'input[name=option]:checked', '#pageType' ).val();  //檢驗到底是選了default還是manual
		var val = $( "#myNumber" ).val();
		
		if(option == 'manual'){
			var page = $("#indexPage").val();
			val = page - val;
			$("#pageStarts").text("index"+page);  //URL顯示更新
			$("#runPages").text("從index"+page+"頁 跑到index"+val+"頁，之後完畢。");  //中文小字提示更新
			$( "#crawl" ).show();
			if(val<0){  //如果相減以後的結果變成負數，則要將數字拉回來。
				alert("輸入頁數超過範圍!!");
				$( "#crawl" ).hide();
			}
		}
		else{
			$("#pageStarts").text("index");  //URL顯示更新
			$("#runPages").text("首頁之後，再跑"+val+"頁 "); //中文小字提示更新
		}
	}
	
	//如果檔案路徑是空的，就隱藏"開始爬蟲"按鈕。
	$("#regestPath").change(function(){
		var pathVal = $.trim($( "#regestPath" ).val());
		if(pathVal.length == 0){
			$( "#crawl" ).hide();
		}
		else{
			$( "#crawl" ).show();
		}
	});
	
	//按下"匯入熱門看板"
	$("#getPopForum").click(function(){
		
		$('#loading_img').show(); //開始跑讀取的動畫
		$("#selectForum").empty();
		
		$.ajax({
	  		url: "CallCrawler" ,   //運行建立索引的Controller
	  		method:'POST',
	  		data: {	"action": "getForums"},
	  		dataType: 'json',
	  		success: function(res){	  			
	  			$.each(res , function( key, value ) {
	  	             console.log( key + ": " + value ); 
	  	           $("#selectForum").append($("<option></option>").attr("value", value).text(key));
	  	         });
	  		},
	  		complete: function(res){
	  			$('#loading_img').hide(); //最終動畫消失
	  			$("#forumName").text($("#selectForum option:selected" ).val());  //更新選擇項目的內容到URL文字樣式中。
	  		}
		});
	});
	
	//執行爬蟲程式
	$( "#crawl" ).click(function() {
		var page = 0;
		var option = $( 'input[name=option]:checked', '#pageType' ).val();  //檢驗到底是選了default還是manual
		if(option == 'manual'){
			page = $("#indexPage").val();
		}
		var forum = $( "#selectForum option:selected" ).val();
		var URL = "https://www.ptt.cc/bbs/"+forum+"/"+"index"+page+".html";
		var crawlerPg = $("#myNumber").val();
		var directory = $("#regestPath").val();
		var pushAmount = $("#pushAmount").val();
		
		$('#loading_img').show(); //開始跑讀取的動畫
		
		$.ajax({
	  		url: "CallCrawler" ,   //運行建立索引的Controller
	  		method:'POST',
	  		data: {	"action": "start", "forum": forum, "URL":URL ,"crawlerPg": crawlerPg, "directory":directory, "pushAmount": pushAmount},
	  		dataType: 'json',
	  		complete: function(res){
	  			$('#loading_img').hide(); //最終動畫消失
		  		alert(res.responseText);
	  		}
		});
		
	});
	
}

	  
</script>


</head>
<body>

<img src="image/picachu.png" width="100" height="100" border="0">
<div style="display: inline-block; vertical-align:top; width: 450px">
	<label>PTT看板名稱: </label>
	<select id="selectForum">
	    <option value="Gossiping">八卦板</option>
	 	<option value="Movie">電影板</option>
	 	<option value="BoardGame">桌遊板</option>
	 	<option value="MobileComm">手機板</option>
	 	<option value="Sagittarius">射手版</option>
	 	
	</select>
	<button type="button" id="getPopForum" style="margin-left: 10px">匯入熱門看板</button> 
	<br>
	
	<form id="pageType">
	  <input type="radio" id="default" name="option" value="default" checked><label for="default">從首頁開始</label><br>
	  <input type="radio" id="manual" name="option" value="manual"><label for="manual">特定頁碼開始</label>   <input type="number" id="indexPage" min="1" value="20000" style="width: 65px;"> <br>
	</form>
	<p>https://www.ptt.cc/bbs/</p><span id="forumName">Gossiping</span><span>/</span><span id="pageStarts">index</span><span>.html</span>
</div>

<div style="display: inline-block; vertical-align:top;">
	<br>
	<label>顯示的推文筆數: </label><input type="number" id="pushAmount" min="0" max="300" value="100" style="width: 60px;"><br>
	<label>檔案路徑: </label>
	<input type="text" id="regestPath" style="width: 400px" value="D:\\020518\\Downloads\\ServerFiles" />
	<br>
	<label>運行頁數: </label><input type="number" id="myNumber" min="0" value="1" style="width: 65px;">
	<small id="runPages">首頁之後，再跑1頁</small>
	<button type="button" id="crawl" style="margin-left: 100px; margin-top: 10px; ">開始爬蟲</button> 
</div>
<hr>
<div style="display: inline-block; ">
	<br>
	<Input type="text" name="search" id="inputBox" value="">
    <button type="button" id="search">查詢</button> 
    <label>每頁顯示: </label>
    <select id="selectPage">
        <option value="5">5筆搜尋</option>
	    <option value="10" selected>10筆搜尋</option>
	    <option value="25">25筆搜尋</option>
	    <option value="50">50筆搜尋</option>
    </select>
    <br>
    <button type="button" id="clear">清空</button> 
    <button type="button" id="index">更新索引</button> 
    
    <label>精準度: </label>
	    <input id="slider" type="range" min="0.002" max="0.05" step="0.001" value="0.02" />
	    <span id="range">0.02(預設值)</span>	    
</div>
<div style="display: inline-block; vertical-align:top; margin-top: 10px; ">
	<label>&nbsp;&nbsp;預覽字數: </label>
	    <input id="preView" type="range" min="1" max="100" step="1" value="50" />
	    <span id="preViewNum">50(預設值)</span>   
	<br><br>
	<label>&nbsp;&nbsp;符合比數上限: </label>
    <select id="maxQuery">
        <option value="50">50筆結果</option>
	    <option value="100" selected>100筆結果</option>
	    <option value="200">200筆結果</option>
	    <option value="300">300筆結果</option>
    </select>     
</div>
    <hr>
    <p>符合筆數為:&nbsp;</p><b id="totalResult"></b>
    <button id="lastPage" type="button" style="margin-left: 200px; margin-right: 8px">上一頁</button> 
    <select id="pageSelector">
<!-- 	    <option value="50">第&nbsp; 3 &nbsp;頁</option> -->
    </select>
    
    
    <p>/&nbsp;&nbsp;&nbsp;共</p><p id="totalPage"></p><p>頁</p>
    <button id="nextPage" type="button" style="margin-left: 8px">下一頁</button> 
    <br><br>
    <img id="loading_img" src="image/ajax-loader.gif" width="50" height="50" border="0" style="padding-left: 200px; padding-top: 20px; display:none">
	<div id="displayBox">
	</div>
    
</body>
</html>