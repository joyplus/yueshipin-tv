package com.joyplus.tv;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;

import com.androidquery.callback.AjaxStatus;
import com.joyplus.manager.HaoimsDataSourceManager;
import com.joyplus.tv.entity.HaoimsData;
import com.joyplus.tv.entity.HaoimsSourceType;
import com.joyplus.tv.entity.ISourceData;
import com.joyplus.tv.utils.ItemStateUtils;
import com.joyplus.utils.Log;

public class LeftButtonsController implements OnClickListener,OnFocusChangeListener{

	private Button allButton;
	private Button movieButton;
	private Button tvSeriesButton;
	private Button varietyButton;
	private Button liveButton;
	private Button animeButton;
	private Button musicButton;
	private Button documentaryButton;
	private Button educationButton;
	private Button othersButton;
	
	private Button 				selectedButton;
	
	private ShowHaoimsActivity context;
	private Handler 			mHandler;
	public LeftButtonsController(ShowHaoimsActivity activity){
		this.context = activity;
		initController();
		selectedButton = allButton;//default selected button allButton	
	}
	
	public void setHandler(Handler handler){
		this.mHandler = handler;
	}
	
	private HaoimsSourceType getHaoimsSourceType(HaoimsSourceType sourceType){
		return HaoimsDataSourceManager.getInstance().getHaoimsSourceType(sourceType);
	}
	
	private void initController(){
		initView();
		initListener();
	}
	
	private void initView(){
		allButton 			= (Button) context.findViewById(R.id.bt_haoims_source_all);
		movieButton 		= (Button) context.findViewById(R.id.bt_haoims_source_movie);
		tvSeriesButton		= (Button) context.findViewById(R.id.bt_haoims_source_tvSeries);
		varietyButton 		= (Button) context.findViewById(R.id.bt_haoims_source_variety);
		liveButton 			= (Button) context.findViewById(R.id.bt_haoims_source_live);
		animeButton 		= (Button) context.findViewById(R.id.bt_haoims_source_anime);
		musicButton 		= (Button) context.findViewById(R.id.bt_haoims_source_music);
		documentaryButton 	= (Button) context.findViewById(R.id.bt_haoims_source_documentary);
		educationButton 	= (Button) context.findViewById(R.id.bt_haoims_source_education);
		othersButton 		= (Button) context.findViewById(R.id.bt_haoims_source_others);
	}
	
	private void initListener(){
		allButton.setOnClickListener(this);
		movieButton.setOnClickListener(this);
		tvSeriesButton.setOnClickListener(this);
		varietyButton.setOnClickListener(this);
		liveButton.setOnClickListener(this);
		animeButton.setOnClickListener(this);
		musicButton.setOnClickListener(this);
		documentaryButton.setOnClickListener(this);
		educationButton.setOnClickListener(this);
		othersButton.setOnClickListener(this);
		
		allButton.setOnFocusChangeListener(this);
		movieButton.setOnFocusChangeListener(this);
		tvSeriesButton.setOnFocusChangeListener(this);
		varietyButton.setOnFocusChangeListener(this);
		liveButton.setOnFocusChangeListener(this);
		animeButton.setOnFocusChangeListener(this);
		musicButton.setOnFocusChangeListener(this);
		documentaryButton.setOnFocusChangeListener(this);
		educationButton.setOnFocusChangeListener(this);
		othersButton.setOnFocusChangeListener(this);
	}

	public void onClick(View v) {
		if(selectedButton.getId() == v.getId()) return;
		boolean isDefault = false;
		switch (v.getId()) {
		case R.id.bt_haoims_source_all:
			HaoimsDataSourceManager.getInstance().setCurrentSourceType(getHaoimsSourceType(HaoimsSourceType.SOURCE_ALL));
			break;
		case R.id.bt_haoims_source_movie:
			HaoimsDataSourceManager.getInstance().setCurrentSourceType(getHaoimsSourceType(HaoimsSourceType.SOURCE_MOVIE));
			break;
		case R.id.bt_haoims_source_tvSeries:
			HaoimsDataSourceManager.getInstance().setCurrentSourceType(getHaoimsSourceType(HaoimsSourceType.SOURCE_TVSERIES));
			break;
		case R.id.bt_haoims_source_variety:
			HaoimsDataSourceManager.getInstance().setCurrentSourceType(getHaoimsSourceType(HaoimsSourceType.SOURCE_VARIETY));
			break;
		case R.id.bt_haoims_source_live:
			HaoimsDataSourceManager.getInstance().setCurrentSourceType(getHaoimsSourceType(HaoimsSourceType.SOURCE_LIVE));
			break;
		case R.id.bt_haoims_source_anime:
			HaoimsDataSourceManager.getInstance().setCurrentSourceType(getHaoimsSourceType(HaoimsSourceType.SOURCE_ANIME));
			break;
		case R.id.bt_haoims_source_music:
			HaoimsDataSourceManager.getInstance().setCurrentSourceType(getHaoimsSourceType(HaoimsSourceType.SOURCE_MUSIC));
			break;
		case R.id.bt_haoims_source_documentary:
			HaoimsDataSourceManager.getInstance().setCurrentSourceType(getHaoimsSourceType(HaoimsSourceType.SOURCE_DOCUMENTARY));
			break;
		case R.id.bt_haoims_source_education:
			HaoimsDataSourceManager.getInstance().setCurrentSourceType(getHaoimsSourceType(HaoimsSourceType.SOURCE_EDUCATION));
			break;
		case R.id.bt_haoims_source_others:
			HaoimsDataSourceManager.getInstance().setCurrentSourceType(getHaoimsSourceType(HaoimsSourceType.SOURCE_OTHERS));
			break;
		default:
			isDefault = true;
			break;
		}
		if(!isDefault && v instanceof Button) {
			Button button = (Button) v;
			buttonStateChange(button);
			
			HaoimsSourceType sourceType = HaoimsDataSourceManager.getInstance().getCurrentSourceType();
			Log.i(TAG, "sourceType:" + sourceType.name());
			if(sourceType.getSourceDatalist().size() <= 0){
				getList4Network();
			}
		}
	}
	
	private void buttonStateChange(Button button){
		ItemStateUtils.buttonToPTState(context, selectedButton);
		ItemStateUtils.buttonToActiveState(context, button);
		selectedButton = button;
	}
	
	public void onFocusChange(View v, boolean hasFocus) {
		if(!hasFocus) {
			if(selectedButton.getId() == v.getId()) {
				Button button = (Button) v;
				ItemStateUtils.buttonToActiveState(context, button);
			}
		} else {
			if(selectedButton.getId() == v.getId()) {
				Button button = (Button) v;
				ItemStateUtils.buttonToPTState(context, button);
			}
		}
	}
	
	public void getList4Network(){
		Log.i(TAG, "getList4Network--->");
		mHandler.sendEmptyMessage(ShowHaoimsActivity.MESSAGE_LOADING_DATA);
		HaoimsSourceType sourceType = HaoimsDataSourceManager.getInstance().getCurrentSourceType();
		if(!sourceType.isCanCache()) return;
		String url = sourceType.getRequestURL4Page(sourceType.getPageIndex(), HaoimsSourceType.DEFAULT_PAGE_SIZE);
		Log.i(TAG, "url--->" + url);
		HaoimsDataSourceManager.getInstance().
			getServiceData(url, "initServiceData", this, null, context.getAQuery());
	}
	
	private static final String TAG = "LeftButtonsController";
	
	public void initServiceData(String url, JSONObject json,
			AjaxStatus status) {
		if (status.getCode() == AjaxStatus.NETWORK_ERROR
				||json == null || json.toString().equals("")) {
			mHandler.sendEmptyMessage(ShowHaoimsActivity.MESSAGE_NETWORK_ERROR);
			return;
		}
			Log.d(TAG, json.toString());
			ArrayList<ISourceData> list = getSourceArrayList(json);
			
			if(list != null && list.size() > 0){
				Log.i(TAG, "list size:" + list.size());
				HaoimsSourceType sourceType = HaoimsDataSourceManager.getInstance().getCurrentSourceType();
				if(list.size() >= HaoimsSourceType.DEFAULT_PAGE_SIZE){
					sourceType.setCanCache(true);
				}else{
					sourceType.setCanCache(false);
				}
				int pageIndex = sourceType.getPageIndex();
				sourceType.setPageIndex(sourceType.getPageIndex()+1);
				sourceType.getSourceDatalist().addAll(list);
				if(pageIndex == 1){
					mHandler.sendEmptyMessage(ShowHaoimsActivity.MESSAGE_ADAPTER_SETLIST);
				}else{
					mHandler.sendEmptyMessage(ShowHaoimsActivity.MESSAGE_REFRESH_LISTVIEW);
				}
				
			}
	}
	
	public static ArrayList<ISourceData> getSourceArrayList(JSONObject json){
		if(json == null) return null;
		if(json.has("results")){
			try {
				JSONArray results= json.getJSONArray("results");
				if(results != null && results.length() > 0){
					ArrayList<ISourceData> list = new ArrayList<ISourceData>();
					HaoimsData sourceData = new HaoimsData();
					for(int i=0;i<results.length();i++){
						ISourceData cloneSourceData = sourceData.clone();
						JSONObject resultObject = (JSONObject) results.get(i);
						if(resultObject.has("prod_id"))
							cloneSourceData.setProd_id(resultObject.getString("prod_id"));
						if(resultObject.has("prod_name"))
							cloneSourceData.setProd_name(resultObject.getString("prod_name"));
						if(resultObject.has("prod_type"))
							cloneSourceData.setProd_type(resultObject.getString("prod_type"));
						if(resultObject.has("prod_pic_url"))
							cloneSourceData.setProd_pic_url(resultObject.getString("prod_pic_url"));
						if(resultObject.has("prod_sumary"))
							cloneSourceData.setProd_sumary(resultObject.getString("prod_sumary"));
						if(resultObject.has("star"))
							cloneSourceData.setStar(resultObject.getString("star"));
						if(resultObject.has("director"))
							cloneSourceData.setDirector(resultObject.getString("director"));
						if(resultObject.has("favority_num"))
							cloneSourceData.setFavority_num(resultObject.getString("favority_num"));
						if(resultObject.has("support_num"))
							cloneSourceData.setSupport_num(resultObject.getString("support_num"));
						if(resultObject.has("publish_date"))
							cloneSourceData.setPublish_date(resultObject.getString("publish_date"));
						if(resultObject.has("score"))
							cloneSourceData.setScore(resultObject.getString("score"));
						if(resultObject.has("area"))
							cloneSourceData.setArea(resultObject.getString("area"));
						if(resultObject.has("max_episode"))
							cloneSourceData.setMax_episode(resultObject.getString("max_episode"));
						if(resultObject.has("cur_episode"))
							cloneSourceData.setMax_episode(resultObject.getString("cur_episode"));
						if(resultObject.has("duration"))
							cloneSourceData.setMax_episode(resultObject.getString("duration"));
						list.add(cloneSourceData);
					}
					return list;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
