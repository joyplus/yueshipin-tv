package com.joyplus.tv;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.tv.Service.Return.ReturnLogInfo;
import com.joyplus.tv.ui.UserInfo;
import com.joyplus.tv.utils.ShowDtailConfig;
import com.joyplus.tv.utils.UtilTools;
import com.umeng.analytics.MobclickAgent;

public class DetailService extends Service {
	
	private static final String TAG = "DetailService";
	
	private Intent mIntent;
	
	private App 	app;
	private AQuery aq;
	private Map<String,String> headers;

	private static final int MSG_LOADING_DATA_SUCCES = 0;

	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LOADING_DATA_SUCCES:
				if(mIntent == null) return;
				String prod_type = mIntent.getStringExtra(ShowDtailConfig.PROD_TYPE);
				mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				if(ShowDtailConfig.MOVIE_TYPE.equals(prod_type)){
					mIntent.setClass(DetailService.this, ShowXiangqingMovie.class);
					startActivity(mIntent);
				}else if(ShowDtailConfig.TVSERIES_TYPE.equals(prod_type)){
					mIntent.setClass(DetailService.this, ShowXiangqingTv.class);
					startActivity(mIntent);
				}else if(ShowDtailConfig.ANIME_TYPE.equals(prod_type)){
					mIntent.setClass(DetailService.this, ShowXiangqingDongman.class);
					startActivity(mIntent);
				}else if(ShowDtailConfig.VARIETY_TYPE.equals(prod_type)){
					mIntent.setClass(DetailService.this, ShowXiangqingZongYi.class);
					startActivity(mIntent);
				}
				break;

			default:
				break;
			}
		}
		
	};
	
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		app = (App) getApplication();
		aq 	= new AQuery(this);
		if(app.getHeaders() == null){
			headers = new HashMap<String, String>();
			initNetWorkData();
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if(aq != null)
			aq.dismiss();
		super.onDestroy();
	}
	
	private void initNetWorkData() {
		if(!Constant.isJoyPlus) {//如果过不是自身应用 
			getLogoInfo(Constant.BASE_URL_TOP + "/open_api_config");
		} else {//如果是自身应用
			initAppkeyAndBaseurl(null);
		}
	}
	
	protected void getLogoInfo(String url) {
		// TODO Auto-generated method stub

		getPostServiceData(url, "initLogoInfo");
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		mIntent = intent;
		if(app.getHeaders() != null && mIntent != null){
			mHandler.sendEmptyMessage(MSG_LOADING_DATA_SUCCES);
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	protected void getPostServiceData(String url, String interfaceName) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("app_key",Constant.APPKEY_TOP );
		params.put("device_name",UtilTools.getUmengChannel(this) );

		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		headers.put("app_channel", UtilTools.getUmengChannel(this));
		cb.SetHeader(headers);
		cb.params(params).url(url).type(JSONObject.class)
				.weakHandler(this, interfaceName);
		aq.ajax(cb);
	}
	
	public void initLogoInfo(String url, JSONObject json,
			AjaxStatus status) {
		// TODO Auto-generated method stub
		
		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {
			initAppkeyAndBaseurl(null);
			return;
		}
		try {
			if (json == null || json.equals("")) {
				initAppkeyAndBaseurl(null);
				return;
			}

			Log.d(TAG, "initLogoInfo" + json.toString());
			ObjectMapper mapper = new ObjectMapper();
			ReturnLogInfo logoInfo = mapper.readValue(json.toString(),
					ReturnLogInfo.class);
			initAppkeyAndBaseurl(logoInfo);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initAppkeyAndBaseurl(ReturnLogInfo returnLogInfo) {

		if(Constant.isJoyPlus) {//如果是本身应用

			headers.put("app_key", Constant.APPKEY);
			headers.put("client", "tv");
			app.setHeaders(headers);

			if (!Constant.TestEnv)
				ReadLocalAppKey();
		} else {

			if(returnLogInfo != null ) {//如果能够获取的到，appkey使用获取数据，图片使用网上下载的

				UtilTools.setLogoUrl(getApplicationContext(), returnLogInfo.logo_url);

				if(returnLogInfo.app_key != null && !returnLogInfo.app_key.equals("")) {

					Constant.APPKEY = returnLogInfo.app_key;
					headers.put("app_key", returnLogInfo.app_key);
					headers.put("client", "tv");
					app.setHeaders(headers);

				} else {

					headers.put("app_key", Constant.APPKEY);
					headers.put("client", "tv");
					app.setHeaders(headers);

					if (!Constant.TestEnv)
						ReadLocalAppKey();
				}

				if(returnLogInfo.api_url != null && !returnLogInfo.api_url.equals("")) {

					Constant.BASE_URL = returnLogInfo.api_url;
				}

			} else {
				headers.put("app_key", Constant.APPKEY);
				headers.put("client", "tv");
				app.setHeaders(headers);

				if (!Constant.TestEnv)
					ReadLocalAppKey();
			}
		}

		checkLogin();
	}
	
	public void ReadLocalAppKey() {
		// online 获取APPKEY
		MobclickAgent.updateOnlineConfig(this);
		String OnLine_Appkey = MobclickAgent.getConfigParams(this, "APPKEY");
		if (OnLine_Appkey != null && OnLine_Appkey.length() > 0) {
			Constant.APPKEY = OnLine_Appkey;
			headers.remove("app_key");
			headers.put("app_key", OnLine_Appkey);
			app.setHeaders(headers);
		}
	}
	
	public boolean checkLogin() {
		String usr_id = null;
		usr_id = app.getUserData("userId");
		if (usr_id == null) {
			String macAddress = null;
			WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = (null == wifiMgr ? null : wifiMgr
					.getConnectionInfo());
			if (info != null) {
				macAddress = info.getMacAddress();
			}
			// 2. 通过调用 service account/generateUIID把UUID传递到服务器
			String url = Constant.BASE_URL + "account/generateUIID";

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("uiid", macAddress);
			params.put("device_type", "Android");

			AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
//			cb.header("User-Agent",
//					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
//			cb.header("app_key", Constant.APPKEY);
			
			cb.SetHeader(headers);
			cb.params(params).url(url).type(JSONObject.class)
					.weakHandler(this, "CallServiceResult");
			aq.ajax(cb);
		} else {
			UserInfo currentUserInfo = new UserInfo();
			currentUserInfo.setUserId(app.getUserData("userId"));
			currentUserInfo.setUserName(app.getUserData("userName"));
			currentUserInfo.setUserAvatarUrl(app.getUserData("userAvatarUrl"));
			headers.put("user_id", currentUserInfo.getUserId());
			app.setUser(currentUserInfo);
			mHandler.sendEmptyMessage(MSG_LOADING_DATA_SUCCES);
		}
		return false;
	}
	
	public void CallServiceResult(String url, JSONObject json, AjaxStatus status) {

		if (json != null) {

			if (json == null || json.equals(""))
				return;

			Log.d(TAG, "CallServiceResult" + json.toString());
			try {
				UserInfo currentUserInfo = new UserInfo();
				if (json.has("user_id")) {
					currentUserInfo.setUserId(json.getString("user_id").trim());
				} else if (json.has("id")) {
					currentUserInfo.setUserId(json.getString("id").trim());
				}

				if (json.has("user_id") || json.has("id")) {

					currentUserInfo.setUserName(json.getString("nickname"));
					currentUserInfo.setUserAvatarUrl(json.getString("pic_url"));
					app.SaveUserData("userId", currentUserInfo.getUserId());
					app.SaveUserData("userName", json.getString("nickname"));
					app.SaveUserData("userAvatarUrl", json.getString("pic_url"));
					app.setUser(currentUserInfo);
					headers.put("user_id", currentUserInfo.getUserId());
				}

			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			mHandler.sendEmptyMessage(MSG_LOADING_DATA_SUCCES);
		}
	}

}
