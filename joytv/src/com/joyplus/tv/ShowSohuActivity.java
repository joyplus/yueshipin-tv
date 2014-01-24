package com.joyplus.tv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.joyplus.tv.Adapters.SohuAdapter;
import com.joyplus.tv.entity.MovieItemData;
import com.joyplus.tv.ui.MyMovieGridView;
import com.joyplus.tv.ui.WaitingDialog;
import com.joyplus.tv.utils.ItemStateUtils;
import com.joyplus.tv.utils.JieMianConstant;
import com.joyplus.tv.utils.URLUtils;
import com.joyplus.tv.utils.UtilTools;
import com.joyplus.utils.Log;
import com.joyplus.utils.MyKeyEventKey;
import com.umeng.analytics.MobclickAgent;

public class ShowSohuActivity extends Activity implements JieMianConstant, MyKeyEventKey, 
						View.OnFocusChangeListener, OnClickListener, OnItemClickListener{
	
	private static final String TAG = "ShowSohuActivity";
	private static final int DIALOG_WAITING = 0;
	
	private static final int TYPE_SOHU_VIDEO = 0;
	private static final int TYPE_MOVIE 	= 1;
	private static final int TYPE_TV 		= 2;
	private static final int TYPE_SHOW 		= 3;
	private static final int TYPE_DONGMAN 	= 4;
	private static final int TYPE_JILU      = 5;
	
	private static final int REQUSET_NUMBER = 30;
	
	private App 	 app;
	private AQuery  aQuery;
	
	private MyMovieGridView playGv;
	private View selectedItem;
	
	private boolean isCurrentKeyVertical = false;
	private boolean isLeft = false;
	private boolean isFirstGridActive = false;
	
	private Map<Integer, Boolean> hasMores = new HashMap<Integer, Boolean>();
	private Map<Integer, Integer> requestIndexs = new HashMap<Integer, Integer>();
	private boolean isRequesting = false;
	
	private List<MovieItemData> list_sohu_video;
	private List<MovieItemData> list_movie;
	private List<MovieItemData> list_tv;
	private List<MovieItemData> list_show;
	private List<MovieItemData> list_dongman;
	private List<MovieItemData> list_jilu;
	
	private SohuAdapter adapter;
	
//	private SohuAdapter adapter_movie;
//	private SohuAdapter adapter_tv;
//	private SohuAdapter adapter_show;
//	private SohuAdapter adapter_dongman;
	
	private Button btn_video,btn_movie, btn_tv, btn_show, btn_dongman, btn_jilu;
	private Button selecteButton;
	private int selecte_type;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_sohu);
		app = (App) getApplication();
		aQuery = new AQuery(this);
		hasMores.put(TYPE_SOHU_VIDEO, true);
		hasMores.put(TYPE_MOVIE, true);
		hasMores.put(TYPE_TV, true);
		hasMores.put(TYPE_SHOW, true);
		hasMores.put(TYPE_DONGMAN, true);
		hasMores.put(TYPE_JILU, true);
		requestIndexs.put(TYPE_SOHU_VIDEO, 1);
		requestIndexs.put(TYPE_MOVIE, 1);
		requestIndexs.put(TYPE_TV, 1);
		requestIndexs.put(TYPE_SHOW, 1);
		requestIndexs.put(TYPE_DONGMAN, 1);
		requestIndexs.put(TYPE_JILU, 1);
		playGv = (MyMovieGridView) findViewById(R.id.gv_movie_show);
		btn_video = (Button) findViewById(R.id.bt_sohu_source_video);
		btn_movie = (Button) findViewById(R.id.bt_sohu_source_movie);
		btn_tv = (Button) findViewById(R.id.bt_sohu_source_tvSeries);
		btn_show = (Button) findViewById(R.id.bt_sohu_source_variety);
		btn_dongman = (Button) findViewById(R.id.bt_sohu_source_anime);
		btn_jilu = (Button) findViewById(R.id.bt_sohu_source_jilu);
		
		btn_video.setPadding(0, 0, 5, 0);
		btn_movie.setPadding(0, 0, 5, 0);
		btn_tv.setPadding(0, 0, 5, 0);
		btn_show.setPadding(0, 0, 5, 0);
		btn_dongman.setPadding(0, 0, 5, 0);
		btn_jilu.setPadding(0, 0, 5, 0);
		
		btn_video.setOnFocusChangeListener(this);
		btn_movie.setOnFocusChangeListener(this);
		btn_tv.setOnFocusChangeListener(this);
		btn_show.setOnFocusChangeListener(this);
		btn_dongman.setOnFocusChangeListener(this);
		btn_jilu.setOnFocusChangeListener(this);
		
		btn_video.setOnClickListener(this);
		btn_movie.setOnClickListener(this);
		btn_tv.setOnClickListener(this);
		btn_show.setOnClickListener(this);
		btn_dongman.setOnClickListener(this);
		btn_jilu.setOnClickListener(this);
		
		List<MovieItemData> lists  = new ArrayList<MovieItemData>();
		for(int i=0; i<15; i++){
			MovieItemData d = new MovieItemData();
			d.setMovieName(" ");
			lists.add(d);
		}
		adapter = new SohuAdapter(this,lists);
		playGv.setAdapter(adapter);
		
		playGv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, final View view,
					int position, long id) {
				// TODO Auto-generated method stub
				// if (BuildConfig.DEBUG)
				if(view == null) return;
				if(!isFirstGridActive){
					isFirstGridActive = true;
					Log.d(TAG, "first onItemSelected" + position);
					selectedItem = null;
				}else{
					Log.d(TAG, "onItemSelected" + position);
					if(selectedItem!=null){
						ItemStateUtils.viewOutAnimation(getApplicationContext(),selectedItem);
					}
					ItemStateUtils.viewInAnimation(getApplicationContext(),view);
					selectedItem = view;
					if(((SohuAdapter)playGv.getAdapter()).getDateList().size()-1-playGv.getLastVisiblePosition()<10){
						getServiceDate(selecte_type);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onNothingSelected");
			}
		});
		playGv.setOnScrollListener(new AbsListView.OnScrollListener() {
			
			int tempfirstVisibleItem;
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:
					Log.i(TAG, "playGv--->SCROLL_STATE_IDLE" + " tempfirstVisibleItem--->" + tempfirstVisibleItem);
					
					// 缓存
//					playGv.setSelection(tempfirstVisibleItem);
					SohuAdapter adapter = (SohuAdapter) playGv.getAdapter();
					if (adapter != null) {
			
						if (adapter.getDateList() != null) {
			
							int size = adapter.getDateList().size();
			
							if (size > 0) {
			
								if (size - 1 - (tempfirstVisibleItem + 9) < URLUtils.CACHE_NUM) {
			//request ...
//									if (isNextPagePossibles[currentListIndex]) {
//			
//										pageNums[currentListIndex]++;
//										cachePlay(currentListIndex,
//												pageNums[currentListIndex]);
//									}
								}
							}
						}
					}
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					Log.i(TAG, "playGv--->SCROLL_STATE_TOUCH_SCROLL");
					if(selectedItem!=null){
						ItemStateUtils.viewOutAnimation(getApplicationContext(),selectedItem);
						selectedItem = null;
					}
					break;
				case OnScrollListener.SCROLL_STATE_FLING:
					Log.i(TAG, "playGv--->SCROLL_STATE_FLING");
					
//					isDragGridView = false;
					break;
				default:
					break;
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
				tempfirstVisibleItem = firstVisibleItem;
			}
		});
		playGv.setOnItemClickListener(this);
		playGv.requestFocus();
		playGv.setFocusableInTouchMode(true);
		playGv.setSelected(true);
		playGv.setSelection(3);
		showDialog(DIALOG_WAITING);
		selecteButton = btn_video;
		selecte_type = TYPE_SOHU_VIDEO;
		getServiceDate(selecte_type);
		selecteButton.setTextColor(getResources().getColor(R.color.common_title_selected));
		selecteButton.setBackgroundResource(R.drawable.menubg);
		selecteButton.setPadding(0, 0, 5, 0);
	}


	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (this.aQuery != null){
			this.aQuery.dismiss();
		}
		super.onDestroy();
	}
	
	protected void onResume() {
		super.onResume();

		MobclickAgent.onResume(this);

		if (app.getUserInfo() != null) {
			aQuery.id(R.id.iv_head_user_icon).image(
					app.getUserInfo().getUserAvatarUrl(), false, true, 0,
					R.drawable.avatar_defult);
			updateUserName();
		}
	}
	
	
	
	private void updateUserName(){
		if(VIPLoginActivity.isLogin(this))
			aQuery.id(R.id.tv_head_user_name).text(UtilTools.getVIP_NickName(this));
		else
			aQuery.id(R.id.tv_head_user_name).text(app.getUserInfo().getUserName());
	}
	
	protected void onPause() {
		super.onPause();

		MobclickAgent.onPause(this);
	}
	
	
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_WAITING:
			WaitingDialog dlg = new WaitingDialog(this);
			dlg.show();
			dlg.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			});
			dlg.setDialogWindowStyle();
			return dlg;
		default:
			return super.onCreateDialog(id);
		}
	}
	private void networkError(){
		app.MyToast(ShowSohuActivity.this,
				getResources().getString(R.string.networknotwork));
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		int action = event.getAction();

		//isDragGridView = false;//不是拖动
		
		if (action == KeyEvent.ACTION_DOWN) {

			switch (keyCode) {
			case KEY_UP:
//				isGridViewUp = true;
				isCurrentKeyVertical = true;
				isLeft = false;
				break;
			case KEY_DOWN:

//				isGridViewUp = false;
				isCurrentKeyVertical = true;
				isLeft = false;
				break;
			case KEY_LEFT:

				isCurrentKeyVertical = false;
				isLeft = true;
				break;
			case KEY_RIGHT:

				isCurrentKeyVertical = false;
				isLeft = false;
				break;

			default:
				break;
			}

		}
		
		return super.onKeyDown(keyCode, event);
	}



	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		if(hasFocus){
			if(isLeft){
				if(selectedItem!=null){
					ItemStateUtils.viewOutAnimation(getApplicationContext(),selectedItem);
					//selectedItem = null;
//					playGv.setSelection(-1);
				}
				
			}
		}else{
			if(!isCurrentKeyVertical){
//				playGv.setSelection(selection);
				if(selectedItem!=null){
					ItemStateUtils.viewInAnimation(getApplicationContext(),selectedItem);
				}else{
					Log.d(TAG, "setSelection 0");
					playGv.setSelection(0);
				}
			}
		}
	}
	
	private void getServiceDate(int type){
		if(!hasMores.get(type)||isRequesting){
			return;
		}else{
			isRequesting = true;
			String url = null;
			AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
			switch (type) {
			case TYPE_SOHU_VIDEO:
				//sohuchupin
				url = Constant.BASE_URL + "top/SohuProduce?app_key=" + Constant.APPKEY + 
				"&page_num=" + requestIndexs.get(TYPE_SOHU_VIDEO) +
				"&page_size=" + REQUSET_NUMBER;
				break;
			case TYPE_MOVIE:
				url = Constant.BASE_URL + "filter?app_key=" + Constant.APPKEY + 
				"&page_num=" + requestIndexs.get(TYPE_MOVIE) +
				"&page_size=" + REQUSET_NUMBER + 
				"&playfrom=so_hu_cp" + 
				"&type=" + 1;
				break;
			case TYPE_TV:
				url = Constant.BASE_URL + "filter?app_key=" + Constant.APPKEY + 
				"&page_num=" + requestIndexs.get(TYPE_TV) +
				"&page_size=" + REQUSET_NUMBER + 
				"&playfrom=so_hu_cp" + 
				"&type=" + 2;
				break;
			case TYPE_SHOW:
				url = Constant.BASE_URL + "filter?app_key=" + Constant.APPKEY + 
				"&page_num=" + requestIndexs.get(TYPE_SHOW) +
				"&page_size=" + REQUSET_NUMBER + 
				"&playfrom=so_hu_cp" + 
				"&type=" + 3;
				break;
			case TYPE_DONGMAN:
				url = Constant.BASE_URL + "filter?app_key=" + Constant.APPKEY + 
				"&page_num=" + requestIndexs.get(TYPE_DONGMAN) +
				"&page_size=" + REQUSET_NUMBER + 
				"&playfrom=so_hu_cp" + 
				"&type=" + 131;
				break;
			case TYPE_JILU:
				url = Constant.BASE_URL + "filter?app_key=" + Constant.APPKEY + 
				"&page_num=" + requestIndexs.get(TYPE_JILU) +
				"&page_size=" + REQUSET_NUMBER + 
				"&playfrom=so_hu_cp" + 
				"&type=" + 5;
				break;
			}
			if(url!=null){
				cb.url(url).type(JSONObject.class).weakHandler(this, "initMoreList");
				cb.SetHeader(app.getHeaders());
				aQuery.ajax(cb);
			}
		}
	}
	
	public void initMoreList(String url, JSONObject json,
			AjaxStatus status) {
		isRequesting = false;
		dismissDialog(DIALOG_WAITING);
		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {

			app.MyToast(aQuery.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		try {
			
			if(json == null || json.equals("")) 
				return;
			Log.d(TAG, json.toString());
			requestIndexs.put(selecte_type, requestIndexs.get(selecte_type)+1);
			List<MovieItemData> lists = UtilTools.returnFilterMovieSearch_TVJson(json.toString());
			if(lists.size()<REQUSET_NUMBER){
				hasMores.put(selecte_type, false);
			}else{
				hasMores.put(selecte_type, true);
			}
			switch (selecte_type) {
			case TYPE_SOHU_VIDEO:
				if(list_sohu_video == null){
					list_sohu_video = new ArrayList<MovieItemData>();
					playGv.setSelection(0);
				}
				for (MovieItemData movieItemData : lists) {
					list_sohu_video.add(movieItemData);
				}
				adapter.setDateList(list_sohu_video);
				adapter.notifyDataSetChanged();
				break;
			case TYPE_MOVIE:
				if(list_movie == null){
					list_movie = new ArrayList<MovieItemData>();
//					playGv.setSelection(0);
				}
				for (MovieItemData movieItemData : lists) {
					list_movie.add(movieItemData);
				}
				adapter.setDateList(list_movie);
				adapter.notifyDataSetChanged();
				break;
			case TYPE_TV:
				if(list_tv == null){
					list_tv = new ArrayList<MovieItemData>();
				}
				for (MovieItemData movieItemData : lists) {
					list_tv.add(movieItemData);
				}
				adapter.setDateList(list_tv);
				adapter.notifyDataSetChanged();
				break;
			case TYPE_SHOW:
				if(list_show == null){
					list_show = new ArrayList<MovieItemData>();
				}
				for (MovieItemData movieItemData : lists) {
					list_show.add(movieItemData);
				}
				adapter.setDateList(list_show);
				adapter.notifyDataSetChanged();
				break;
			case TYPE_DONGMAN:
				if(list_dongman == null){
					list_dongman = new ArrayList<MovieItemData>();
				}
				for (MovieItemData movieItemData : lists) {
					list_dongman.add(movieItemData);
				}
				adapter.setDateList(list_dongman);
				adapter.notifyDataSetChanged();
				break;
			case TYPE_JILU:
				if(list_jilu == null){
					list_jilu = new ArrayList<MovieItemData>();
				}
				for (MovieItemData movieItemData : lists) {
					list_jilu.add(movieItemData);
				}
				adapter.setDateList(list_jilu);
				adapter.notifyDataSetChanged();
				break;
			}
			//TODO : handle the date
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	
//	public void initMoreTV(String url, JSONObject json,
//			AjaxStatus status) {
//		isRequesting = false;
//		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {
//
//			app.MyToast(aQuery.getContext(),
//					getResources().getString(R.string.networknotwork));
//			return;
//		}
//		try {
//			
//			if(json == null || json.equals("")) 
//				return;
//			Log.d(TAG, json.toString());
//			
//			//TODO : handle the date
//		}catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
//	}
//	
//	public void initMoreShow(String url, JSONObject json,
//			AjaxStatus status) {
//		isRequesting = false;
//		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {
//
//			app.MyToast(aQuery.getContext(),
//					getResources().getString(R.string.networknotwork));
//			return;
//		}
//		try {
//			
//			if(json == null || json.equals("")) 
//				return;
//			Log.d(TAG, json.toString());
//			
//			//TODO : handle the date
//		}catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
//	}
//	
//	public void initMoreDongman(String url, JSONObject json,
//			AjaxStatus status) {
//		isRequesting = false;
//		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {
//
//			app.MyToast(aQuery.getContext(),
//					getResources().getString(R.string.networknotwork));
//			return;
//		}
//		try {
//			
//			if(json == null || json.equals("")) 
//				return;
//			Log.d(TAG, json.toString());
//			
//			//TODO : handle the date
//		}catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
//	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v==selecteButton){
			return;
		}
		isFirstGridActive = false;
		
		selecteButton.setTextColor(getResources().getColorStateList(R.color.text_color_selector));
		selecteButton.setBackgroundResource(R.drawable.text_drawable_selector);
		switch (v.getId()) {
		case R.id.bt_sohu_source_video:
			selecte_type = TYPE_SOHU_VIDEO;
			selecteButton = btn_video;
			if(list_sohu_video==null){
				getServiceDate(TYPE_SOHU_VIDEO);
				showDialog(DIALOG_WAITING);
			}else{
				adapter.setDateList(list_sohu_video);
				adapter.notifyDataSetChanged();
			}
			break;
		case R.id.bt_sohu_source_movie:
			selecte_type = TYPE_MOVIE;
			selecteButton = btn_movie;
			if(list_movie==null){
				getServiceDate(TYPE_MOVIE);
				showDialog(DIALOG_WAITING);
			}else{
				adapter.setDateList(list_movie);
				adapter.notifyDataSetChanged();
			}
			break;
		case R.id.bt_sohu_source_tvSeries:
			selecte_type = TYPE_TV;
			selecteButton = btn_tv;
			if(list_tv==null){
				getServiceDate(TYPE_TV);
				showDialog(DIALOG_WAITING);
			}else{
				adapter.setDateList(list_tv);
				adapter.notifyDataSetChanged();
			}
			break;
		case R.id.bt_sohu_source_variety:
			selecte_type = TYPE_SHOW;
			selecteButton = btn_show;
			if(list_show==null){
				getServiceDate(TYPE_SHOW);
				showDialog(DIALOG_WAITING);
			}else{
				adapter.setDateList(list_show);
				adapter.notifyDataSetChanged();
			}
			break;
		case R.id.bt_sohu_source_anime:
			selecte_type = TYPE_DONGMAN;
			selecteButton = btn_dongman;
			if(list_dongman==null){
				getServiceDate(TYPE_DONGMAN);
				showDialog(DIALOG_WAITING);
			}else{
				adapter.setDateList(list_dongman);
				adapter.notifyDataSetChanged();
			}
			break;
		case R.id.bt_sohu_source_jilu:
			selecte_type = TYPE_JILU;
			selecteButton = btn_jilu;
			if(list_jilu==null){
				getServiceDate(TYPE_JILU);
				showDialog(DIALOG_WAITING);
			}else{
				adapter.setDateList(list_jilu);
				adapter.notifyDataSetChanged();
			}
			break;
		}
		selecteButton.setTextColor(getResources().getColor(R.color.common_title_selected));
		selecteButton.setBackgroundResource(R.drawable.menubg);
		selecteButton.setPadding(0, 0, 5, 0);
		playGv.setSelection(1);
	}



	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		try{
			Intent intent = null;
			MovieItemData info = null;
			switch (selecte_type) {
			case TYPE_SOHU_VIDEO:
				info = list_sohu_video.get(position);
				String type = info.getMovieProType();
				int prod_type = Integer.valueOf(type);
				switch (prod_type) {
				case 1:
					intent = new Intent(this, ShowXiangqingMovie.class);
					break;
				case 2:
					intent = new Intent(this, ShowXiangqingTv.class);
					break;
				case 3:
					intent = new Intent(this, ShowXiangqingZongYi.class);
					break;
				case 131:
					intent = new Intent(this, ShowXiangqingDongman.class);
					break;
				case 5:
					intent = new Intent(this, ShowXiangqingJilu.class);
					break;
				}
				if(intent!=null){
					intent.putExtra("ID", info.getMovieID());
					intent.putExtra("prod_name", info.getMovieName());
					intent.putExtra("prod_url", info.getMoviePicUrl());
					intent.putExtra("directors", info.getDirectors());
					intent.putExtra("stars", info.getStars());
					intent.putExtra("summary", info.getSummary());
					intent.putExtra("support_num", info.getSupport_num());
					intent.putExtra("favority_num", info.getFavority_num());
					intent.putExtra("definition", info.getDefinition());
					intent.putExtra("score", info.getMovieScore());
				}
				break;
			case TYPE_MOVIE:
				intent = new Intent(this, ShowXiangqingMovie.class);
				info = list_movie.get(position);
				intent.putExtra("ID", info.getMovieID());
				intent.putExtra("prod_name", info.getMovieName());
				intent.putExtra("prod_url", info.getMoviePicUrl());
				intent.putExtra("directors", info.getDirectors());
				intent.putExtra("stars", info.getStars());
				intent.putExtra("summary", info.getSummary());
				intent.putExtra("support_num", info.getSupport_num());
				intent.putExtra("favority_num", info.getFavority_num());
				intent.putExtra("definition", info.getDefinition());
				intent.putExtra("score", info.getMovieScore());
				break;
			case TYPE_TV:
				info = list_tv.get(position);
				intent = new Intent(this, ShowXiangqingTv.class);
				intent.putExtra("ID", info.getMovieID());
				intent.putExtra("prod_name", info.getMovieName());
				intent.putExtra("prod_url", info.getMoviePicUrl());
				intent.putExtra("directors", info.getDirectors());
				intent.putExtra("stars", info.getStars());
				intent.putExtra("summary", info.getSummary());
				intent.putExtra("support_num", info.getSupport_num());
				intent.putExtra("favority_num", info.getFavority_num());
				intent.putExtra("definition", info.getDefinition());
				intent.putExtra("score", info.getMovieScore());
				break;
			case TYPE_DONGMAN:
				info = list_dongman.get(position);
				intent = new Intent(this, ShowXiangqingDongman.class);
				intent.putExtra("ID", info.getMovieID());
				intent.putExtra("prod_name", info.getMovieName());
				intent.putExtra("prod_url", info.getMoviePicUrl());
				intent.putExtra("directors", info.getDirectors());
				intent.putExtra("stars", info.getStars());
				intent.putExtra("summary", info.getSummary());
				intent.putExtra("support_num", info.getSupport_num());
				intent.putExtra("favority_num", info.getFavority_num());
				intent.putExtra("definition", info.getDefinition());
				intent.putExtra("score", info.getMovieScore());
				break;
			case TYPE_SHOW:
				info = list_show.get(position);
				intent = new Intent(this, ShowXiangqingZongYi.class);
				intent.putExtra("ID", info.getMovieID());
				intent.putExtra("prod_name", info.getMovieName());
				intent.putExtra("prod_url", info.getMoviePicUrl());
				intent.putExtra("directors", info.getDirectors());
				intent.putExtra("stars", info.getStars());
				intent.putExtra("summary", info.getSummary());
				intent.putExtra("support_num", info.getSupport_num());
				intent.putExtra("favority_num", info.getFavority_num());
				intent.putExtra("definition", info.getDefinition());
				intent.putExtra("score", info.getMovieScore());
				break;
			case TYPE_JILU:
				info = list_jilu.get(position);
				intent = new Intent(this, ShowXiangqingJilu.class);
				intent.putExtra("ID", info.getMovieID());
				intent.putExtra("prod_name", info.getMovieName());
				intent.putExtra("prod_url", info.getMoviePicUrl());
				intent.putExtra("directors", info.getDirectors());
				intent.putExtra("stars", info.getStars());
				intent.putExtra("summary", info.getSummary());
				intent.putExtra("support_num", info.getSupport_num());
				intent.putExtra("favority_num", info.getFavority_num());
				intent.putExtra("definition", info.getDefinition());
				intent.putExtra("score", info.getMovieScore());
				break;
			default:
				break;
			}
			if(intent!=null){
				startActivity(intent);
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		//startActivity(new Intent(this, PlaySohuVideoActivity.class));
	}
}
