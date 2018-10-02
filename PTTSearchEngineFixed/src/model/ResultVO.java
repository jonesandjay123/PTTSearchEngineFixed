package model;

@SuppressWarnings("serial")
public class ResultVO implements java.io.Serializable{

	private String title;
	private float score;
	private String modified;
	private int sizeNum;
	private int count;
	private String str;
	private String path;
	private String linkUrl;
	private int possibleLimit;
	private int totalResults;
	private int currentPage;
	private int totalPage;
	private boolean accident;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}
	public String getModified() {
		return modified;
	}
	public void setModified(String modified) {
		this.modified = modified;
	}
	public int getSizeNum() {
		return sizeNum;
	}
	public void setSizeNum(int sizeNum) {
		this.sizeNum = sizeNum;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getStr() {
		return str;
	}
	public void setStr(String str) {
		this.str = str;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getLinkUrl() {
		return linkUrl;
	}
	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}
	public int getPossibleLimit() {
		return possibleLimit;
	}
	public void setPossibleLimit(int possibleLimit) {
		this.possibleLimit = possibleLimit;
	}
	public int getTotalResults() {
		return totalResults;
	}
	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	public int getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
	public boolean isAccident() {
		return accident;
	}
	public void setAccident(boolean accident) {
		this.accident = accident;
	}

	
}
