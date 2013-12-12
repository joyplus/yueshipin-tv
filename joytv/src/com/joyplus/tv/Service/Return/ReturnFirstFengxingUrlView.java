package com.joyplus.tv.Service.Return;



public class ReturnFirstFengxingUrlView {
	
	public String type;
	public String source;
	public String error;
	public String code;
	public DownUrl down_urls;
	public String msg;
	
	public static class DownUrl{
		
		public String source;
		public Url[] urls;
	}
	
	public static class Url{
		
		public String file;
		public String type;
		public String url;
	}

}
