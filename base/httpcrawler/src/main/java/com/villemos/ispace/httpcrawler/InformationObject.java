package com.villemos.ispace.httpcrawler;

public class InformationObject {

	public String url;
	public String title;
	public String sourceName;
	public String page;
	public String mimeType;
	
	public InformationObject(String url, String title, String mimeType,	String sourceName, String page) {
		this.url = url;
		this.title = title;
		this.mimeType = mimeType;
		this.sourceName = sourceName;
		this.page = page;
	}
}
