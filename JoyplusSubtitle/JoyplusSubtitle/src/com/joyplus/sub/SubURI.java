package com.joyplus.sub;

public class SubURI extends SubModel{
      
	public String  Uri;
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return Uri;
	}
	public void setUrl(String url) {
		this.Uri = url;
	}
	
	public SUBTYPE SubType;
	
	public boolean Instanced = false;//flog of this sub have instance. default is false
 
}
