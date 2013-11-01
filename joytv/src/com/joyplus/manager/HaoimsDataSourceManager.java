package com.joyplus.manager;

import java.util.Map;

import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.joyplus.tv.App;
import com.joyplus.tv.Constant;
import com.joyplus.tv.ShowHaoimsActivity;
import com.joyplus.tv.entity.HaoimsSourceType;

public class HaoimsDataSourceManager {

	private static HaoimsDataSourceManager manager =
			new HaoimsDataSourceManager();
	
	private HaoimsDataSourceManager(){ }
	
	public static HaoimsDataSourceManager getInstance(){
		return manager;
	}
	
	public static final HaoimsSourceType DEFAULT_SOURCETYPE = HaoimsSourceType.SOURCE_ALL;
	private ShowHaoimsActivity 			  	context;
	private HaoimsDataSourceServer 	server;
	public void initManager(ShowHaoimsActivity _context){
		this.context = _context;
		this.server = new HaoimsDataSourceServer();
	}
	
	public void initArray(){
		this.server.initArray();
	}
	public HaoimsSourceType[] getHaoimsSourceTypes(){
		return this.server.getHaoimsSourceTypes();
	}
	public HaoimsSourceType getHaoimsSourceType(HaoimsSourceType sourceType){
		return this.server.getHaoimsSourceType(sourceType);
	}
	public void setCurrentSourceType(HaoimsSourceType sourceType){
		this.server.setCurrentSourceType(sourceType);
	}
	public HaoimsSourceType getCurrentSourceType(){
		return this.server.getCurrentSourceType();
	}
	
	public void getServiceData(String url, String interfaceName,Object handler,Map<String,String> headers,AQuery aQuery) {
		this.server.getServiceData(url, interfaceName, handler, headers, aQuery);
	}
	
	class HaoimsDataSourceServer {
		private HaoimsSourceType[] haoimsSourceTypes;
		private HaoimsSourceType 	currentSourceType;
		
		public HaoimsDataSourceServer(){
			initSourceType();
		}
		
		private void initSourceType(){
			this.haoimsSourceTypes = HaoimsSourceType.values();
			initArray();
			this.currentSourceType = getHaoimsSourceType(DEFAULT_SOURCETYPE);
		}
		
		public void initArray(){
			for(int i=0;i<this.haoimsSourceTypes.length;i++){
				this.haoimsSourceTypes[i].setPageIndex(1);
				this.haoimsSourceTypes[i].getSourceDatalist().clear();
				this.haoimsSourceTypes[i].setCanCache(true);
			}
		}
		
		public HaoimsSourceType[] getHaoimsSourceTypes(){
			return this.haoimsSourceTypes;
		}
		public HaoimsSourceType getHaoimsSourceType(HaoimsSourceType sourceType){
			return this.haoimsSourceTypes[sourceType.getIndex()];
		}
		public void setCurrentSourceType(HaoimsSourceType sourceType){
			this.currentSourceType = getHaoimsSourceType(sourceType);
		}
		public HaoimsSourceType getCurrentSourceType(){
			return this.currentSourceType;
		}
		
		public void getServiceData(String url, String interfaceName,Object handler,Map<String,String> headers,AQuery aQuery) {
			// TODO Auto-generated method stub
			RequestAQueryManager.getInstance().getRequest(handler, url, headers, aQuery, interfaceName);
		}
	}
	
}
