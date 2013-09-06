package com.joyplus.Sub;

import com.joyplus.Sub.JoyplusSubInterface.SubContentType;
import com.joyplus.mediaplayer.ContentRestrictionException;

import android.content.ContentResolver;

public interface JoyplusSubContentRestriction {
     
	void checkSubSize(int SubSize, int increaseSize) throws ContentRestrictionException;
	
	void checkUri(SubContentType type,String uri) throws ContentRestrictionException;
	
}
