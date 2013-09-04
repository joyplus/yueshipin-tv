package com.joyplus.Sub;

import java.util.ArrayList;
import java.util.List;

import org.blaznyoght.subtitles.model.Collection;
import org.blaznyoght.subtitles.model.Element;

import android.net.Uri;

public abstract class JoyplusSub {
    
	protected String   mTag;
	protected String   mContentType;
	
	private Uri        mUri;
	public Uri getUri(){
		return mUri;
	}
	private List<Element> elements = new ArrayList<Element>();

	public List<Element> getElements() {
		return elements;
	}
	
	public JoyplusSub(Uri uri){
		mUri = uri;
	}
	
}
