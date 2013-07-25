package com.joyplus.tv.Service.Return;

public class ReturnFirstFengxingUrlView {
	
	public String type;
	public String source;
	public String error;
	public String code;
	public VIDEO_INFOS[] video_infos;
	public String msg;
	
	public static class VIDEO_INFOS{
		
		public String type;
		public String request_url;
	}

}
