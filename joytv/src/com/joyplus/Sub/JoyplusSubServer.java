package com.joyplus.Sub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.joyplus.mediaplayer.ContentRestrictionException;
import com.joyplus.tv.Constant;

public class JoyplusSubServer {

	private List<String> SubUri = new ArrayList<String>();
	
	private JoyplusSub mSub;
    private Context mContext;
    
    public JoyplusSubServer(Context context){
    	mContext = context;
    }
	public void setSubUri(List<String> subUri){
		 if(subUri==null || subUri.size()<=0)return;
		 SubUri = subUri;
		 CheckSubUriList();
	}
	public List<String> getSubList(){
		return SubUri;
	}
    public boolean CheckSubAviable(){
    	if(mSub != null && mSub.getElements().size()>2)return true;
    	return false;
    }
    public JoyplusSub getJoyplusSub() throws Exception{
    	if(mSub != null)return mSub;
    	throw new Exception("JoyplusSub is null");
    }
	private void CheckSubUriList() {
		// TODO Auto-generated method stub
		for(String uri : SubUri){
			if(InstanceSub(uri))return;
			SubUri.remove(uri);
		}
		SubUri = new ArrayList<String>();
		mSub   = null;
	}
	
	private boolean InstanceSub(String uri){
		mSub = InstanceSRTSub(uri);
		if(mSub != null)return true;
		return false;
	}
	private JoyplusSub InstanceSRTSub(String uri){
		try{
			JoyplusSub sub = new SRTSub(uri);
			sub.parse(getSubByte(sub.getUri()));
			if(sub.getElements().size()>2)return sub;
		}catch(ContentRestrictionException e){
		}
		return null;		
	}
	 
   private byte[] getSubByte(String url){		
		AjaxCallback<byte[]> cb = new AjaxCallback<byte[]>();
		cb.url(url).type(byte[].class);		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("app_key", Constant.APPKEY);
		cb.SetHeader(headers); 		
		(new AQuery(mContext)).sync(cb);
		byte[] subTitle = cb.getResult();
		return subTitle;
	}
   
    public void SwitchSub(int index){
    	if(index>0 && index<SubUri.size()){
    		if(!InstanceSub(SubUri.get(index))){
    			SubUri.remove(index);
    			CheckSubUriList();
    		}
    	}
    }
    public int getCurrentSubIndex(){
    	if(mSub == null)return -1;
    	return SubUri.indexOf(mSub.getUri());
    }
}
