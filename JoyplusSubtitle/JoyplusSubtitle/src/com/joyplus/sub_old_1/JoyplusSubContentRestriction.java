package com.joyplus.sub_old_1;

import com.joyplus.sub_old_1.JoyplusSubInterface.SubContentType;

public interface JoyplusSubContentRestriction {
     
	void checkSubSize(long SubSize, long increaseSize) throws ContentRestrictionException;
	
	void checkUri(SubContentType type,String uri) throws ContentRestrictionException;
	
}
