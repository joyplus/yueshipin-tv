package com.joyplus.Sub;

import com.joyplus.mediaplayer.ContentRestrictionException;

import android.content.ContentResolver;

public interface JoyplusSubContentRestriction {
     
	void checkSubSize(int SubSize, int increaseSize, ContentResolver resolver) throws ContentRestrictionException;
	
	void checkSRTContentType(String contentType) throws ContentRestrictionException;
}
