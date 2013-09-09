package com.joyplus.Sub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.joyplus.Sub.JoyplusSubInterface.SubContentType;
import com.joyplus.Sub.SubURI.SUBTYPE;
import com.joyplus.mediaplayer.ContentRestrictionException;
import com.joyplus.tv.Constant;

public class JoyplusSubServer {

	private List<SubURI> SubUri = new ArrayList<SubURI>();
	
	private JoyplusSub mSub;
    private Context mContext;
    private JoyplusSub getJoyplusSub(SubContentType type , SubURI uri){
    	if(type == SubContentType.SUB_ASS)return new ASSSub(uri);
    	else if(type == SubContentType.SUB_SCC)return new SCCSub(uri);
    	else if(type == SubContentType.SUB_SRT)return new SRTSub(uri);
    	else if(type == SubContentType.SUB_SSA)return new SSASub(uri);
    	else if(type == SubContentType.SUB_STL)return new STLSub(uri);
    	else return null;
    }
    public JoyplusSubServer(Context context){
    	mContext = context;
    }
	public void setSubUri(List<SubURI> subUri){
		 if(subUri==null || subUri.size()<=0)return;
		 SubUri = subUri;
		 CheckSubUriList();
	}
	public List<SubURI> getSubList(){
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
		for(SubURI uri : SubUri){
			if(InstanceSub(uri))return;
			SubUri.remove(uri);
		}
		SubUri = new ArrayList<SubURI>();
		mSub   = null;
	}
	
	private boolean InstanceSub(SubURI uri){
		byte[] Subtitle = null;
		if(uri.SubType == SUBTYPE.NETWORK)
			Subtitle = getSubByte(uri.Uri);
		mSub = InstanceSub(uri,Subtitle);
		if(mSub != null)return true;
		return false;
	}
	private JoyplusSub InstanceSub(SubURI uri,byte[] subtitle){
		JoyplusSub sub = null;
		for(int i=1;i<=SubContentType.SUB_MAX.toInt();i++){
			sub = InstanceSub(JoyplusSub.getSubContentType(i),uri,subtitle);
			if(sub !=null)return sub;
		}
		return null;
	}
	private JoyplusSub InstanceSub(SubContentType type ,SubURI uri,byte[] subtitle){
		try{
			if(type.toInt()<=SubContentType.SUB_UNKNOW.toInt()
					||type.toInt()>SubContentType.SUB_MAX.toInt())return null;
			JoyplusSub sub = getJoyplusSub(type,uri);
			if(sub.getUri().SubType == SUBTYPE.NETWORK)
			      sub.parse(subtitle);
			else if(sub.getUri().SubType == SUBTYPE.LOCAL)
				  sub.parseLocal();
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
