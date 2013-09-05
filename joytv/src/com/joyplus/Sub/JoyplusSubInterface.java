package com.joyplus.Sub;

import java.io.InputStream;

public interface JoyplusSubInterface {
	   /*Interface of sub type*/      
       public enum SubContentType{
    	   SUB_UNKNOW,
    	   SUB_SRT
       }
       
       public void parse(byte[] sub);
       
}
