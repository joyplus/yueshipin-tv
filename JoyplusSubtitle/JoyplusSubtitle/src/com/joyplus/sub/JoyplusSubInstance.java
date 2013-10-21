package com.joyplus.sub;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.joyplus.sub.JoyplusSubInterface.SubContentType;

public class JoyplusSubInstance {
	
    private Context    mContext;
    private JoyplusSub mSub;
    private SubURI     mSubURI;
    private byte[] Subtitle;
    public  byte[] getSubTitle(){
    	return Subtitle;
    }
    public boolean IsSubAviable(){
    	if(mSubURI == null)return false;
    	if(mSubURI.SubType == SUBTYPE.NETWORK)
    	   return (mSub!=null && mSub.elements.size()>2 && Subtitle!=null);
    	else if(mSubURI.SubType == SUBTYPE.LOCAL)
    	   return (mSub!=null && mSub.elements.size()>2 );
    	return false;
    }
    public JoyplusSub getJoyplusSub(){
    	return mSub;
    }
    public SubURI     getSubURI(){
    	return mSubURI;
    }
    public JoyplusSubInstance(Context context){
    	 mContext = context;
    }
    
    private JoyplusSub getJoyplusSub(SubContentType type , SubURI uri){
    	if(type == SubContentType.SUB_ASS)return new ASSSub(uri);
    	else if(type == SubContentType.SUB_SCC)return new SCCSub(uri);
    	else if(type == SubContentType.SUB_SRT)return new SRTSub(uri);
    	else if(type == SubContentType.SUB_SSA)return new SSASub(uri);
    	else if(type == SubContentType.SUB_STL)return new STLSub(uri);
    	else return null;
    }    
    
	public boolean InstanceSub(SubURI uri){
		mSubURI  = uri;
		Subtitle = null;
		if(uri.SubType == SUBTYPE.NETWORK)
			Subtitle = getSubByte(uri.getUrl());
		mSub = InstanceSub(uri,Subtitle);
		if(mSub != null){
			java.util.Collections.sort(mSub.elements, new SubTitleElementComparator(false));
			if(SubFeature.SUBDELAY)
				java.util.Collections.sort(mSub.elements, new SubTitleElementComparator(true));
			return true;
		}
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
				if(subtitle != null)
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
		(new AQuery(mContext)).sync(cb);
		byte[] subTitle = cb.getResult();
		return subTitle;
	}
   
     /*Interface of parser uri to get download sub uri*/
	  public List<SubURI> getNetworkSubURI(String subtitle_parse_url_url, String url, String MD5,
			String app_key,Context context) {
		List<SubURI> list = new ArrayList<SubURI>();
		String subTitleUrl = subtitle_parse_url_url + "?url="
				+ URLEncoder.encode(url) + "&md5_code=" + MD5;
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(subTitleUrl).type(JSONObject.class);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("app_key", app_key);
		cb.SetHeader(headers);
		(new AQuery(context)).sync(cb);
		JSONObject jo = cb.getResult();
		if (jo != null && jo.toString() != null && !"".equals(jo.toString())) {
			try {
				JSONObject subtitlesJsonObject = (JSONObject) new JSONTokener(
						jo.toString()).nextValue();
				if (subtitlesJsonObject.has("error")) {
					if (!subtitlesJsonObject.getBoolean("error")
							&& subtitlesJsonObject.has("subtitles")) {
						JSONArray subtitleContents = subtitlesJsonObject
								.getJSONArray("subtitles");
						if (subtitleContents != null
								&& subtitleContents.length() > 0) {
							for (int i = 0; i < subtitleContents.length(); i++) {
								String tempsubTitleUrl = subtitleContents.getString(i);
								SubURI subURI = new SubURI();
								subURI.SubType = SUBTYPE.NETWORK;
								subURI.setUrl(tempsubTitleUrl);
								list.add(subURI);
							}
						}
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}
}
