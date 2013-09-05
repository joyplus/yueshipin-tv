package com.joyplus.Sub;

import java.util.ArrayList;
import java.util.List;

public abstract class JoyplusSub implements JoyplusSubInterface{
    
	protected String           mTag;
	protected SubContentType   mContentType;
	
	private String     mUri;
	public  String     getUri(){
		return mUri;
	}
	protected List<Element> elements = new ArrayList<Element>();

	public List<Element> getElements() {
		return elements;
	}
	
	public JoyplusSub(String uri){
		mUri = uri;
		mContentType = SubContentType.SUB_UNKNOW;
	}
	
}
