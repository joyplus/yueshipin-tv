package com.joyplus.manager;

import java.util.Map;

import org.json.JSONObject;

import android.text.TextUtils;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.joyplus.tv.entity.REQUEST_URL;

public class RequestAQueryManager {
	
	private static RequestAQueryManager manager ;
	
	private RequestAQueryManager(){ 
		currentRequest_URL = DEAULT_REQUEST_URL;
	}
	
	public static RequestAQueryManager getInstance(){
		if(manager == null)
			manager = new RequestAQueryManager();
		return manager;
	}
	
	public static final REQUEST_URL DEAULT_REQUEST_URL = REQUEST_URL.JOYPLUS_URL;
	private REQUEST_URL currentRequest_URL;
	
	public void setcurrentRequest_URL(REQUEST_URL request_URL){
		this.currentRequest_URL = request_URL;
	}
	public REQUEST_URL getcurrentRequest_URL(){
		return this.currentRequest_URL;
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
