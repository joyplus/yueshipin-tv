package com.joyplus.Sub;

public interface JoyplusSubInterface {
	   /*Interface of sub type*/
       void setContentType(SubContentType type);
       SubContentType getContentType();
       
       public enum SubContentType{
    	   SUB_UNKNOW,
    	   SUB_SRT
       }
       
       
}
