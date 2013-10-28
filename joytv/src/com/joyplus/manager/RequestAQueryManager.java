package com.joyplus.manager;

import java.util.Map;

import org.json.JSONObject;

import android.text.TextUtils;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;

public class RequestAQueryManager {
	
	private static RequestAQueryManager manager ;
	
	private RequestAQueryManager(){ 
	}
	
	public static RequestAQueryManager getInstance(){
		if(manager == null)
			manager = new RequestAQueryManager();
		return manager;
	}
	public void postRequest(Object handler,String url,
			Map<String,String> params,Map<String,String> headers,AQuery aQuery,String interfaceName){
		request(handler, url, params, headers, aQuery, interfaceName);
	}
	
	public void getRequest(Object handler,String url,
			Map<String,String> headers,AQuery aQuery,String interfaceName){
		request(handler, url, null, headers, aQuery, interfaceName);
	}
	
	public void request(Object handler,String url,
			Map<String,String> params,Map<String,String> headers,AQuery aQuery,String interfaceName){
		if(TextUtils.isEmpty(url)) return;
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		if(params != null && !params.isEmpty()){
			cb.params(params);
		}
		cb.url(url).type(JSONObject.class).weakHandler(handler, interfaceName);
		if(headers != null && !headers.isEmpty()){
			cb.SetHeader(headers);
		}
		aQuery.ajax(cb);
	}
}
