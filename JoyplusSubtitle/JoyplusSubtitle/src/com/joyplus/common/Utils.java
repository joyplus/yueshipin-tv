package com.joyplus.common;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class Utils {

	public static boolean isUTF_8(byte[] file) {
		if (file.length < 3) return false;
		if (file[0] == -17 
				&& file[1] == -69 
				&& file[2] == -65) return true;
		return false;
	}
    
    public static String getCharset(byte[] subTitle,int length){
    	
    	if(subTitle != null){
    		if(subTitle.length < length) length = subTitle.length;
    		
    		ByteArrayInputStream in = new ByteArrayInputStream(subTitle);
    		CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
//    		detector.add(new ParsingDetector(false));
    		detector.add(JChardetFacade.getInstance());
    		try {
    			Charset charset = detector.detectCodepage(in, length);
    			return charset!= null ? charset.name() : "";
    		} catch (IllegalArgumentException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
		return "";
    }
}
