package com.joyplus.sub;

import com.joyplus.sub.JoyplusSubInterface.SubContentType;

public interface JoyplusSubContentRestriction {
     
	void checkSubSize(long SubSize, long increaseSize) throws ContentRestrictionException;
	
	void checkUri(SubContentType type,SubURI uri) throws ContentRestrictionException;
	
}
