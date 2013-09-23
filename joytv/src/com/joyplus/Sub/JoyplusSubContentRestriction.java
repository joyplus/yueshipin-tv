package com.joyplus.Sub;

import com.joyplus.Sub.JoyplusSubInterface.SubContentType;
import com.joyplus.mediaplayer.ContentRestrictionException;

import android.content.ContentResolver;

public interface JoyplusSubContentRestriction {
     
	void checkSubSize(long SubSize, long increaseSize) throws ContentRestrictionException;
	
	void checkUri(SubContentType type,SubURI uri) throws ContentRestrictionException;
	
}
