package com.joyplus.tv.httphelper;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Paint.Join;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.joyplus.tv.utils.UtilTools;


public class LoginHttpHelper {

	private static LoginHttpHelper mLoginHttpHelper = null ;
	
	public static final int LOGIN_FAIL = 0;
	public static final int LOGIN_SUCESS = 1 + LOGIN_FAIL;
	public static LoginHttpHelper getInstance(){
		if(mLoginHttpHelper == null){
			mLoginHttpHelper = new LoginHttpHelper();
		}
		
		return mLoginHttpHelper;
	}
	
	/**
	 * @return -1 网络不正常 0 密码或者用户名错误 1登陆成功
	 */
	public int Login(Context context,Map<String,String> headers,String userName,String passWd,String url){
		
		UtilTools.clearVIPData(context);
		
		AQuery aq = new AQuery(context);
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class);
		cb.SetHeader(headers);
		Map<String,String> params = new HashMap<String, String>();
		params.put("name", userName);
		params.put("pwd", passWd);
		cb.params(params);
		aq.sync(cb);
		AjaxStatus status = cb.getStatus();
		if(status.getCode() == AjaxStatus.NETWORK_ERROR) return -1;
		JSONObject jsonObject = cb.getResult();
//		if(jsonObject != null) Log.i("TAG", "json--->" + jsonObject.toString());
		if( jsonObject != null &&
				jsonObject.has("id")&& jsonObject.has("name")){
			try {
				String nickName = jsonObject.getString("name");
				String vipID = jsonObject.getString("id");
				if(nickName != null && !"".equals(nickName)
						&& vipID != null && !"".equals(vipID)){
					UtilTools.saveVIPNameAndPasswd(context, userName, passWd);
					UtilTools.saveVIPNickNameAndID(context, nickName, vipID);
					return 1;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}
}
