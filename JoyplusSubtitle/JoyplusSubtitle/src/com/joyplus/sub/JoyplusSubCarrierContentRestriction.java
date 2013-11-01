package com.joyplus.sub;

import com.joyplus.sub.JoyplusSubInterface.SubContentType;

public class JoyplusSubCarrierContentRestriction implements JoyplusSubContentRestriction{
	private final static int MAXSIZE = 500*1024;
	private Object mObject=new Object();
	@Override
	public void checkSubSize(long SubSize, long increaseSize) throws ContentRestrictionException {
		// TODO Auto-generated method stub
		synchronized (mObject) {
			if(SubSize<0 || increaseSize<0){
				throw new ContentRestrictionException();
			}
			if((SubSize+increaseSize)>MAXSIZE){
				throw new ContentRestrictionException();
			}
		}		
	}
	@Override
	public void checkUri(SubContentType type, SubURI uri)
			throws ContentRestrictionException {
		// TODO Auto-generated method stub
		synchronized (mObject){
			if(type == null || uri==null || type==SubContentType.SUB_UNKNOW){
				throw new ContentRestrictionException();
			}
			if(type == SubContentType.SUB_SRT && uri.SubType==SUBTYPE.NETWORK){
				CheckSRTUri(uri.getUrl());
			}
		}
	}
	private void CheckSRTUri(String uri) {
		// TODO Auto-generated method stub
		synchronized (mObject){
			if(uri == null || "".equals(uri))throw new ContentRestrictionException();
			if(uri.contains("scid=" ))return;
			if(uri.length() == uri.indexOf(".srt") + 4)return;
			throw new ContentRestrictionException();
		}
	}

	
}
