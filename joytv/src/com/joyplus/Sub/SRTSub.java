package com.joyplus.Sub;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import android.util.Log;

import com.joyplus.tv.utils.UtilTools;


public class SRTSub extends JoyplusSub{
  
	
	public SRTSub(String uri) {
		super(uri);
		// TODO Auto-generated constructor stub
		this.mContentType = SubContentType.SUB_SRT;
		CheckUri();
	}

	private void CheckUri() {
		// TODO Auto-generated method stub
		JoyplusSubContentRestrictionFactory.getContentRestriction().checkUri(SubContentType.SUB_SRT, this.getUri());
	}
    private void CheckSize(byte[] Sub){
    	JoyplusSubContentRestrictionFactory.getContentRestriction().checkSubSize(0, Sub.length);
    }
	@Override
	public void parse(byte[] Sub) {
		// TODO Auto-generated method stub
		CheckSize(Sub);
		SRTParser parser = new SRTParser();		
		String charsetName = UtilTools.getCharset(Sub, 512);		
		if(charsetName.equals("")){			
			boolean isUtf8 = UtilTools.isUTF_8(Sub);					
			if(!isUtf8){
				parser.setCharset("GBK");
			} else {
				parser.setCharset("UTF-8");
			}
		}else {
			parser.setCharset(charsetName);
		}
		parser.parse(new ByteArrayInputStream(Sub));
		SRTSub.this.elements = parser.getCollection().getElements();
	}
    
	
}
