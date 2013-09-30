package com.joyplus.common;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    
    public static String getCharset(InputStream is,int length){
    	if(is == null) return "";

		CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
//		detector.add(new ParsingDetector(false));
		detector.add(JChardetFacade.getInstance());
		try {
	    	byte[] buffer = new byte[length];
	    	is.read(buffer);
	    	is.reset();
	    	return getCharset(buffer, buffer.length);
//			Charset charset = detector.detectCodepage(is, length);
//			return charset!= null ? charset.name() : "GBK";
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return "";
    }
    
    public static String getCharset(String fileName,int length){
    	if(fileName == null || "".equals(fileName)) return "";

		CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
//		detector.add(new ParsingDetector(false));
		detector.add(JChardetFacade.getInstance());
		try {
			File file = new File(fileName);
			if(!file.exists()) return ""; 
			InputStream in = new FileInputStream(new File(fileName));
	    	byte[] buffer = new byte[length];
	    	in.read(buffer);
	    	in.close();
	    	return getCharset(buffer, buffer.length);
//			Charset charset = detector.detectCodepage(is, length);
//			return charset!= null ? charset.name() : "GBK";
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return "";
    }
}
