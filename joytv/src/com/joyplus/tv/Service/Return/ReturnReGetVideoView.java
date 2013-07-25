package com.joyplus.tv.Service.Return;

public class ReturnReGetVideoView {
	
	public String code;
	public DownUrl down_urls;
	public String episode;
	public String error;
	public String id;
	public String msg;
	public String source;

	
	public static class DownUrl{
		
		public String source;
		public Url[] urls;
	}
	
	public static class Url{
		
		public String type;
		public String url;
	}
}
