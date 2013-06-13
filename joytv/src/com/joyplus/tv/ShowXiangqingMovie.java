package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.tv.Adapters.CurrentPlayData;
import com.joyplus.tv.Service.Return.ReturnProgramRelatedVideos;
import com.joyplus.tv.Service.Return.ReturnProgramView;
import com.joyplus.tv.Video.VideoPlayerActivity;
import com.joyplus.tv.entity.URLS_INDEX;
import com.joyplus.tv.ui.WaitingDialog;
import com.joyplus.tv.utils.DefinationComparatorIndex;
import com.joyplus.tv.utils.ItemStateUtils;
import com.joyplus.tv.utils.Log;
import com.joyplus.tv.utils.MyKeyEventKey;
import com.joyplus.tv.utils.SouceComparatorIndex1;
import com.joyplus.tv.utils.UtilTools;
import com.joyplus.tv.utils.URLUtils;
import com.umeng.analytics.MobclickAgent;

public class ShowXiangqingMovie extends Activity implements View.OnClickListener,
		View.OnKeyListener, MyKeyEventKey {

	private static final String TAG = "ShowXiangqingMovie";
	private static final int DIALOG_WAITING = 0;
	private LinearLayout bofangLL;
	private String pic_url;
	private Button dingBt,xiaiBt, yingpingBt;
	private Button bofangBt,gaoqingBt;

	private View beforeView;

	private PopupWindow popupWindow;
	private View popupView;

	private boolean isDing = false, isXiai = false;
	private boolean isPopupWindowShow;

	private View beforeTempPop, currentBofangViewPop;
	private LinearLayout chaoqingLL, gaoqingLL, biaoqingLL;
	
	private GridView tuijianGv;
	/**
	 * 高清地址
	 */
	private String gaoqing_url;
	private String gaoqing_url_souce;
	/**
	 * 超清地址
	 */
	private String chaoqing_url;
	private String chaoqing_url_souce;
	/**
	 * 标清地址
	 */
	private String puqing_url;
	private String puqing_url_souce;
	private ReturnProgramView movieData;
	private ReturnProgramRelatedVideos recommendMoviesData;
	
	private AQuery aq;
	private App app;
	private String prod_id;
	
	private int supportDefination;
	
	private static int favNum = 0;
	
	private boolean isYingPing = false;
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			initPopWindowData();
//			int sign = -1;
//			LinearLayout[] linLayouts = {chaoqingLL,gaoqingLL,biaoqingLL};
//			int[] strIds = {R.string.gaoqing_chaogaoqing,R.string.gaoqing_gaoqing,R.string.gaoqing_biaoqing};
			
			if(chaoqing_url==null){
				supportDefination-=1;
				chaoqingLL.setVisibility(View.GONE);
//				sign = 0;
			} else {
				
				gaoqingBt.setText(R.string.gaoqing_chaogaoqing);
			}
			
			if(gaoqing_url==null){
				supportDefination-=1;
				gaoqingLL.setVisibility(View.GONE);
//				sign = 1;
			} else {
				
				if(gaoqing_url==null) {
					
					gaoqingBt.setText(R.string.gaoqing_gaoqing);
				}
			}

			if(puqing_url==null){
				supportDefination-=1;
				biaoqingLL.setVisibility(View.GONE);
//				sign =2;
			} else {
				
				if(gaoqing_url==null && chaoqing_url == null) {
					
					gaoqingBt.setText(R.string.gaoqing_biaoqing);
				}
			}
//			Log.d(TAG, "sign = " + sign);
			
			if(supportDefination == 0) {
				
				bofangLL.setEnabled(false);
			} else if(supportDefination == 1) {
				
//				for (int i = 0; i < linLayouts.length; i++) {
//					if(sign == i) {
//						
//						LinearLayout ll = linLayouts[i];
//						Button button1= (Button) ll.getChildAt(0);
//						Button button2= (Button) ll.getChildAt(1);
//						button2.setText(strIds[i]);
//					}
//				}
			}
			removeDialog(DIALOG_WAITING);
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.show_xiangxi_dianying_layout);
		aq = new AQuery(this);
		app = (App) getApplication();
		supportDefination = 3;
		prod_id = getIntent().getStringExtra("ID");
		if(prod_id==null||"".equals(prod_id)){
			Log.e(TAG, "pram error prod_id is error value");
			finish();
		}
		showDefultDate();
		initView();
		showDialog(DIALOG_WAITING);
		getIsShoucangData();
		getMovieDateFromService();
		getRecommendMovieFormService();
	}

	private void initPopWindow() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		popupView = inflater.inflate(R.layout.show_gaoqing_item, null);

		chaoqingLL = (LinearLayout) popupView
				.findViewById(R.id.ll_gaoqing_chaoqing);
		gaoqingLL = (LinearLayout) popupView
				.findViewById(R.id.ll_gaoqing_gaoqing);
		biaoqingLL = (LinearLayout) popupView
				.findViewById(R.id.ll_gaoqing_biaoqing);
		popupWindow = new PopupWindow(popupView);
		currentBofangViewPop = chaoqingLL;
		beforeTempPop = chaoqingLL;
	}

	private void initView() {

		dingBt = (Button) findViewById(R.id.bt_xiangqingding);
		xiaiBt = (Button) findViewById(R.id.bt_xiangqing_xiai);
		bofangLL = (LinearLayout) findViewById(R.id.ll_xiangqing_bofang_gaoqing);
		bofangLL.setNextFocusUpId(R.id.ll_xiangqing_bofang_gaoqing);
		
		bofangBt = (Button) findViewById(R.id.bt_xiangqing_bofang);
		gaoqingBt = (Button) findViewById(R.id.bt_xiangqing_gaoqing);


		yingpingBt = (Button) findViewById(R.id.bt_xiangqing_yingping);
		
		tuijianGv = (GridView) findViewById(R.id.gv_xiangqing_tuijian);
		tuijianGv.setNextFocusRightId(R.id.gv_xiangqing_tuijian);
		
		bofangLL.requestFocus();

		addListener();

		initPopWindow();
		
		dingBt.setNextFocusUpId(R.id.bt_xiangqingding);
		xiaiBt.setNextFocusUpId(R.id.bt_xiangqing_xiai);
		yingpingBt.setNextFocusUpId(R.id.bt_xiangqing_yingping);

		beforeView = dingBt;

	}

	private void addListener() {

		dingBt.setOnKeyListener(this);
		xiaiBt.setOnKeyListener(this);
		bofangLL.setOnKeyListener(this);
		tuijianGv.setOnKeyListener(this);

		dingBt.setOnClickListener(this);
		xiaiBt.setOnClickListener(this);
		bofangLL.setOnClickListener(this);
		yingpingBt.setOnClickListener(this);
		
		tuijianGv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ShowXiangqingMovie.this,
						ShowXiangqingMovie.class);
				intent.putExtra("ID", recommendMoviesData.items[position].prod_id);
				startActivity(intent);
			}
		});
		
		xiaiBt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				
				if(hasFocus) {
					if(isXiai) {
						
						ItemStateUtils.shoucangButtonToFocusState(xiaiBt, getApplicationContext());
					} else {
						
						ItemStateUtils.shoucangButtonToNormalState(xiaiBt, getApplicationContext());
					}
					
				} else {
					
					if(isXiai) {
						
						ItemStateUtils.shoucangButtonToFocusState(xiaiBt, getApplicationContext());
					} else {
						
						ItemStateUtils.shoucangButtonToNormalState(xiaiBt, getApplicationContext());
					}
				}
			}
		});
		
		dingBt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				
				if(hasFocus) {
					if(isDing) {
						
						ItemStateUtils.dingButtonToFocusState(dingBt, getApplicationContext());
					} else {
						
						ItemStateUtils.dingButtonToNormalState(dingBt, getApplicationContext());
					}
					
				} else {
					
					if(isDing){
						
						ItemStateUtils.dingButtonToFocusState(dingBt, getApplicationContext());
					}else {
						
						ItemStateUtils.dingButtonToNormalState(dingBt, getApplicationContext());
					}
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bt_xiangqingding:
			dingService();
			String dingNum = dingBt.getText().toString();
			if(dingNum != null && !dingNum.equals("")) {
				
				int nums = Integer.valueOf(dingNum) + 1;
				dingBt.setText(nums + "");
			}
			ItemStateUtils.dingButtonToFocusState(dingBt, getApplicationContext());
			dingBt.setEnabled(false);
			isDing = true;
			break;
		case R.id.bt_xiangqing_xiai:
			if(isXiai) {
				
				cancelshoucang();
				
			} else {
				
				shoucang();

			}

			break;
		case R.id.ll_xiangqing_bofang_gaoqing:
			// bofangLL.setN
			// xiaiIv.setImageResource(R.drawable.icon_fav_active);
			// xiaiTv.setTextColor(getResources().getColor(R.color.text_foucs));
//			String str0 = "984192";
//			String str1 = "西游降魔篇";
//			String str2 = "http://221.130.179.66/25/36/53/kingsoft/movie/47978987920B0079FF686B6370B4E039-xiyoupian.mp4?crypt=61740d1aa7f2e300&b=800&gn=132&nc=1&bf=30&p2p=1&video_type=mp4&check=0&tm=1364191200&key=af7b9ad0697560c682a0070cf225e65e&opck=1&lgn=letv&proxy=3702889363&cipi=2026698610&tsnp=1&tag=ios&tag=kingsoft&sign=coopdown&realext=.mp4test=m3u8";

			Intent intent = new Intent(this, VideoPlayerActivity.class);
			CurrentPlayData playDate = new CurrentPlayData();
			intent = new Intent(this,VideoPlayerActivity.class);
			playDate.prod_id = movieData.movie.id;
			playDate.prod_type = 1;
			playDate.prod_name = movieData.movie.name;
			
			//清晰度
			playDate.prod_qua = UtilTools.string2Int(movieData.movie.definition);
			
			if(gaoqing_url!=null){
				playDate.prod_url = gaoqing_url;
				playDate.prod_src = gaoqing_url_souce;
			}else if(chaoqing_url != null){
				playDate.prod_url = chaoqing_url;
				playDate.prod_src = chaoqing_url_souce;
			}else if(puqing_url !=null){
				playDate.prod_url = puqing_url;
				playDate.prod_src = puqing_url_souce;
			}
//			playDate.prod_src = "";
//			playDate.prod_qua = Integer.valueOf(info.definition);
			app.setCurrentPlayData(playDate);
			app.set_ReturnProgramView(null);
			startActivity(intent);
			break;
		case R.id.gv_xiangqing_tuijian:
			break;
		case R.id.bt_xiangqing_yingping:
			Intent yingpingIntent = new Intent(this, DetailComment.class);
//			yingpingIntent.putExtra("ID", prod_id);
//			int yingpingSize = movieData.comments.length;
//			Log.i(TAG, "Comments : " + yingpingSize);
			
			if(isYingPing) {
				
				Bundle bundle = new Bundle();
				bundle.putString("prod_id", prod_id);
				bundle.putString("prod_name", movieData.movie.name);
				bundle.putString("prod_dou", movieData.movie.score);
				bundle.putString("prod_url", pic_url);
				yingpingIntent.putExtras(bundle);
				startActivity(yingpingIntent);
			}
			
			break;
		default:
			break;
		}

	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();

		if (action == KeyEvent.ACTION_UP) {

			switch (v.getId()) {
			case R.id.ll_xiangqing_bofang_gaoqing:
				if (keyCode == KEY_UP || keyCode == KEY_LEFT
						|| keyCode == KEY_RIGHT) {

					Log.i("Yangzhg", "UPPPPPPPP!");
					if (keyCode == KEY_UP && beforeView.getId() == v.getId()
							&& !isPopupWindowShow) {
						if(supportDefination == 3) {
							
							int width = v.getWidth();
							int height = v.getHeight() * 3;
							int locationY = v.getHeight() * 2;
							int[] location = new int[2];
							v.getLocationOnScreen(location);
							popupWindow.setFocusable(true);
							popupWindow.setWidth(width + 10);
							popupWindow.setHeight(height + 40);
							popupWindow.showAtLocation(v, Gravity.NO_GRAVITY,
									location[0] - 6, location[1] - locationY -40);
						} else if(supportDefination == 2) {
							
							int width = v.getWidth();
							int height = v.getHeight() * 2;
							int locationY = v.getHeight() * 1;
							int[] location = new int[2];
							v.getLocationOnScreen(location);
							popupWindow.setFocusable(true);
							popupWindow.setWidth(width + 10);
							popupWindow.setHeight(height + 40);
							popupWindow.showAtLocation(v, Gravity.NO_GRAVITY,
									location[0] - 6, location[1] - locationY -40);
						}

					}
					// Log.i("Yangzhg", "UPUP!!!!!!");
					// bofangLL.setN
					// xiaiIv.setImageResource(R.drawable.icon_fav_active);
					// xiaiTv.setTextColor(getResources().getColor(R.color.text_foucs));
				}
				break;
			default:
				break;
			}

			beforeView = v;
		}
		return false;
	}
	
	private void backToNormalPopView() {
		
		LinearLayout ll = (LinearLayout) beforeTempPop;
		Button button1= (Button) ll.getChildAt(0);
		Button button2= (Button) ll.getChildAt(1);
		button1.setVisibility(View.INVISIBLE);
		button2.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
	}
	
	private void setLinearLayoutVisible(View v) {
		
		LinearLayout ll = (LinearLayout) v;
		Button button1= (Button) ll.getChildAt(0);
		Button button2= (Button) ll.getChildAt(1);
		button1.setVisibility(View.VISIBLE);
		button2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_play, 0);
	}

	private void initPopWindowData() {
		setLinearLayoutVisible(currentBofangViewPop);

		OnClickListener gaoqingListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int id = v.getId();
				Intent intent = new Intent(ShowXiangqingMovie.this, VideoPlayerActivity.class);
				CurrentPlayData playDate = new CurrentPlayData();
				playDate.prod_id = movieData.movie.id;
				playDate.prod_type = 1;
				playDate.prod_name = movieData.movie.name;
				
				//清晰度
				playDate.prod_qua = UtilTools.string2Int(movieData.movie.definition);
				
				playDate.prod_favority = isXiai;
//				if(gaoqing_url!=null){
//					playDate.prod_url = gaoqing_url;
//				}else if(chaoqing_url != null){
//					playDate.prod_url = chaoqing_url;
//				}else if(puqing_url !=null){
//					playDate.prod_url = puqing_url;
//				}
//				playDate.prod_src = "";
//				playDate.prod_qua = Integer.valueOf(info.definition);
				app.setCurrentPlayData(playDate);
				app.set_ReturnProgramView(null);
				startActivity(intent);
				switch (id) {
				case R.id.ll_gaoqing_chaoqing:
					gaoqingBt.setText(R.string.gaoqing_chaogaoqing);
					currentBofangViewPop = v;
					playDate.prod_url = chaoqing_url;
					break;
				case R.id.ll_gaoqing_gaoqing:
					gaoqingBt.setText(R.string.gaoqing_gaoqing);
					currentBofangViewPop = v;
					playDate.prod_url = gaoqing_url;
					break;
				case R.id.ll_gaoqing_biaoqing:
					gaoqingBt.setText(R.string.gaoqing_biaoqing);
					currentBofangViewPop = v;
					playDate.prod_url = puqing_url;
					break;
				default:
					break;
				}

				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				}
				startActivity(intent);
			}
		};
		chaoqingLL.setOnClickListener(gaoqingListener);
		gaoqingLL.setOnClickListener(gaoqingListener);
		biaoqingLL.setOnClickListener(gaoqingListener);

		OnKeyListener gaoqingKeyListener = new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();
				if(keyCode == KeyEvent.KEYCODE_BACK){
					if(popupWindow!=null){
						popupWindow.dismiss();
					}
					return true;
				}else if(action == KeyEvent.ACTION_UP) {

					switch (v.getId()) {
					case R.id.ll_gaoqing_chaoqing:
						if(keyCode == KEY_UP) {
							backToNormalPopView();
							setLinearLayoutVisible(v);
						}
						break;
					case R.id.ll_gaoqing_gaoqing:
						if(keyCode == KEY_UP || keyCode == KEY_DOWN) {
							backToNormalPopView();
							setLinearLayoutVisible(v);
						}
						break;
					case R.id.ll_gaoqing_biaoqing:
						if(keyCode == KEY_DOWN) {
							backToNormalPopView();
							setLinearLayoutVisible(v);
						}
						break;
					default:
						break;
					}
					beforeTempPop = v;
					if(keyCode == KeyEvent.KEYCODE_BACK){
						if(popupWindow != null && popupWindow.isShowing()){
							popupWindow.dismiss();
						}
					}
				}
				return false;
			}
		};
		
		chaoqingLL.setOnKeyListener(gaoqingKeyListener);
		gaoqingLL.setOnKeyListener(gaoqingKeyListener);
		biaoqingLL.setOnKeyListener(gaoqingKeyListener);

	}
	
	private void getMovieDateFromService(){
		String url = Constant.BASE_URL + "program/view" +"?prod_id=" + prod_id;
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, "initMovieDate");
		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}
	
	private void getRecommendMovieFormService(){
		String url = Constant.BASE_URL + "program/relatedVideos" +"?prod_id=" + prod_id;
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, "initRecommendMovieDate");
		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}
	
	public void initMovieDate(String url, JSONObject json, AjaxStatus status){
		if (status.getCode() == AjaxStatus.NETWORK_ERROR||json == null) {
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		
		if(json == null || json.equals("")) 
			return;
		
		Log.d(TAG, "data = " + json.toString());
		ObjectMapper mapper = new ObjectMapper();
		try {
			movieData = null;
			movieData  = mapper.readValue(json.toString(), ReturnProgramView.class);
			new Thread(new CheckPlayUrl()).start();
			if(movieData!=null){
				
				if(movieData.movie == null) {
					
					return;
				}
				
				String bigPicUrl = movieData.movie.ipad_poster;
				if(bigPicUrl == null || bigPicUrl.equals("")
						||bigPicUrl.equals(UtilTools.EMPTY)) {
					
					bigPicUrl = movieData.movie.poster;
				}
				pic_url = bigPicUrl;
				updateView();
			}
			
			getYingpingData(URLUtils.getYingPin_1_URL(prod_id));
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
	
	protected void getServiceData(String url, String interfaceName) {
		// TODO Auto-generated method stub

		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, interfaceName);

		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}

	protected void getYingpingData(String url) {
		// TODO Auto-generated method stub

		Log.i(TAG, "getYingpingData--->");
		getServiceData(url, "initYingpingServiceData");
	}
	
	public void initYingpingServiceData(String url, JSONObject json,
			AjaxStatus status) {
		// TODO Auto-generated method stub

		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {

			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		
		if (json == null || json.equals(""))
			return;
		
		String str = json.toString();
		if(str.contains("review_id")) {
			
			isYingPing = true;
		}
		
		Log.i(TAG, "isYingPing--->" + isYingPing + "   --->" + str);
		
		if(!isYingPing) {
			
			yingpingBt.setEnabled(false);
			yingpingBt.setBackgroundResource(R.drawable.yingping_button_unuse_selector);
			yingpingBt.setTextColor(getResources().getColor(R.color.unuse_color));
//			yingpingBt.setFocusable(false);
		}
	}
	
	private void updateView(){
		
		String strNum = movieData.movie.favority_num;
		
		if(strNum != null && !strNum.equals("")){
			
			favNum = Integer.valueOf(strNum);
		}
		
		aq.id(R.id.image).image(pic_url, false, true,0, R.drawable.post_normal);
		aq.id(R.id.text_name).text(movieData.movie.name);
		aq.id(R.id.text_directors).text(movieData.movie.directors);
		aq.id(R.id.text_starts).text(movieData.movie.stars);
		aq.id(R.id.text_introduce).text(movieData.movie.summary);
		aq.id(R.id.bt_xiangqingding).text(movieData.movie.support_num);
		aq.id(R.id.bt_xiangqing_xiai).text(movieData.movie.favority_num);
		int definition = Integer.valueOf((movieData.movie.definition));
		switch (definition) {
		case 6:
			aq.id(R.id.img_definition).image(R.drawable.icon_ts);
			break;
		case 7:
			aq.id(R.id.img_definition).image(R.drawable.icon_hd);
			break;
		case 8:
			aq.id(R.id.img_definition).image(R.drawable.icon_bd);
			break;
		default:
			aq.id(R.id.img_definition).gone();
			break;
		}
		updateScore(movieData.movie.score);
	}
	
	public void initRecommendMovieDate(String url, JSONObject json, AjaxStatus status){
		if (status.getCode() == AjaxStatus.NETWORK_ERROR||json == null) {
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		
		if(json == null || json.equals("")) 
			return;
		
		Log.d(TAG, "data = " + json.toString());
		ObjectMapper mapper = new ObjectMapper();
		try {
			recommendMoviesData = null;
			recommendMoviesData  = mapper.readValue(json.toString(), ReturnProgramRelatedVideos.class);
			tuijianGv.setAdapter(tuiJianAdapter);
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
	
	private void updateScore(String score){
		aq.id(R.id.textView_score).text(score);
		float f = Float.valueOf(score);
//		int i = Math.round(f);
		int i = (int) Math.ceil(f);
//		int i = (f%1>=0.5)?(int)(f/1):(int)(f/1+1);
		switch (i) {
		case 0:
			aq.id(R.id.start1).image(R.drawable.star_off);
			aq.id(R.id.start2).image(R.drawable.star_off);
			aq.id(R.id.start3).image(R.drawable.star_off);
			aq.id(R.id.start4).image(R.drawable.star_off);
			aq.id(R.id.start5).image(R.drawable.star_off);
			break;
		case 1:
			aq.id(R.id.start1).image(R.drawable.star_half);
			aq.id(R.id.start2).image(R.drawable.star_off);
			aq.id(R.id.start3).image(R.drawable.star_off);
			aq.id(R.id.start4).image(R.drawable.star_off);
			aq.id(R.id.start5).image(R.drawable.star_off);
			break;
		case 2:
			aq.id(R.id.start1).image(R.drawable.star_on);
			aq.id(R.id.start2).image(R.drawable.star_off);
			aq.id(R.id.start3).image(R.drawable.star_off);
			aq.id(R.id.start4).image(R.drawable.star_off);
			aq.id(R.id.start5).image(R.drawable.star_off);
			break;
		case 3:
			aq.id(R.id.start1).image(R.drawable.star_on);
			aq.id(R.id.start2).image(R.drawable.star_half);
			aq.id(R.id.start3).image(R.drawable.star_off);
			aq.id(R.id.start4).image(R.drawable.star_off);
			aq.id(R.id.start5).image(R.drawable.star_off);
			break;
		case 4:
			aq.id(R.id.start1).image(R.drawable.star_on);
			aq.id(R.id.start2).image(R.drawable.star_on);
			aq.id(R.id.start3).image(R.drawable.star_off);
			aq.id(R.id.start4).image(R.drawable.star_off);
			aq.id(R.id.start5).image(R.drawable.star_off);
			break;
		case 5:
			aq.id(R.id.start1).image(R.drawable.star_on);
			aq.id(R.id.start2).image(R.drawable.star_on);
			aq.id(R.id.start3).image(R.drawable.star_half);
			aq.id(R.id.start4).image(R.drawable.star_off);
			aq.id(R.id.start5).image(R.drawable.star_off);
			break;
		case 6:
			aq.id(R.id.start1).image(R.drawable.star_on);
			aq.id(R.id.start2).image(R.drawable.star_on);
			aq.id(R.id.start3).image(R.drawable.star_on);
			aq.id(R.id.start4).image(R.drawable.star_off);
			aq.id(R.id.start5).image(R.drawable.star_off);
			break;
		case 7:
			aq.id(R.id.start1).image(R.drawable.star_on);
			aq.id(R.id.start2).image(R.drawable.star_on);
			aq.id(R.id.start3).image(R.drawable.star_on);
			aq.id(R.id.start4).image(R.drawable.star_half);
			aq.id(R.id.start5).image(R.drawable.star_off);
			break;
		case 8:
			aq.id(R.id.start1).image(R.drawable.star_on);
			aq.id(R.id.start2).image(R.drawable.star_on);
			aq.id(R.id.start3).image(R.drawable.star_on);
			aq.id(R.id.start4).image(R.drawable.star_on);
			aq.id(R.id.start5).image(R.drawable.star_off);
			break;
		case 9:
			aq.id(R.id.start1).image(R.drawable.star_on);
			aq.id(R.id.start2).image(R.drawable.star_on);
			aq.id(R.id.start3).image(R.drawable.star_on);
			aq.id(R.id.start4).image(R.drawable.star_on);
			aq.id(R.id.start5).image(R.drawable.star_half);
			break;
		case 10:
			aq.id(R.id.start1).image(R.drawable.star_on);
			aq.id(R.id.start2).image(R.drawable.star_on);
			aq.id(R.id.start3).image(R.drawable.star_on);
			aq.id(R.id.start4).image(R.drawable.star_on);
			aq.id(R.id.start5).image(R.drawable.star_on);
			break;
		default:
			break;
		}
	}
	
	private void cancelshoucang(){
		
		xiaiBt.setEnabled(false);
		String url = Constant.BASE_URL + "program/unfavority";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("prod_id", prod_id);

		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.SetHeader(app.getHeaders());

		cb.params(params).url(url).type(JSONObject.class)
				.weakHandler(this, "cancelshoucangResult");
		aq.ajax(cb);
	}
	
	public void cancelshoucangResult(String url, JSONObject json, AjaxStatus status){
		
		xiaiBt.setEnabled(true);
		
		
			if(favNum - 1 >=0) {
				
				favNum --;
				xiaiBt.setText((favNum) + "");
				ItemStateUtils.shoucangButtonToNormalState(xiaiBt, getApplicationContext());
			}
		isXiai = false;
		
		if(json == null || json.equals("")) 
			return;
		
		Log.d(TAG, "cancel:----->"+json.toString());
	}
	
	private void shoucang(){
		xiaiBt.setEnabled(false);
		String url = Constant.BASE_URL + "program/favority";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("prod_id", prod_id);

		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.SetHeader(app.getHeaders());

		cb.params(params).url(url).type(JSONObject.class)
				.weakHandler(this, "shoucangResult");
		aq.ajax(cb);
	}
	
	public void shoucangResult(String url, JSONObject json, AjaxStatus status){
		xiaiBt.setEnabled(true);
		favNum ++;
		
		xiaiBt.setText(favNum + "");
		ItemStateUtils.shoucangButtonToFocusState(xiaiBt, getApplicationContext());
		isXiai = true;
		
		if(json == null || json.equals("")) 
			return;
		
		Log.d(TAG, "shoucangResult:----->" + json.toString());
	}
	
	private void dingService(){
		String url = Constant.BASE_URL + "program/support";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("prod_id", prod_id);

		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.SetHeader(app.getHeaders());

		cb.params(params).url(url).type(JSONObject.class)
				.weakHandler(this, "dingResult");
		aq.ajax(cb);
	}
	
	public void dingResult(String url, JSONObject json, AjaxStatus status){
		
		if(json == null || json.equals("")) 
			return;
		
		Log.d(TAG, json.toString());
	}
	
	private void getIsShoucangData(){
		xiaiBt.setEnabled(false);
		String url = Constant.BASE_URL + "program/is_favority";
//	+"?prod_id=" + prod_id;
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		Map<String, String> params = new HashMap<String, String>();
		params.put("prod_id" , prod_id);
		cb.params(params).url(url).type(JSONObject.class).weakHandler(this, "initIsShoucangData");
		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}
	
	public void initIsShoucangData(String url, JSONObject json, AjaxStatus status){
		
		xiaiBt.setEnabled(true);
		
		if (status.getCode() == AjaxStatus.NETWORK_ERROR||json == null) {
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		
		if(json == null || json.equals("")) 
			return;
		
		Log.d(TAG, "data = " + json.toString());
		
		String flag = json.toString();
		
		if(!flag.equals("")) {
			
			if(flag.contains("true")) {
				isXiai = true;
				ItemStateUtils.shoucangButtonToFocusState(xiaiBt, getApplicationContext());
			} else {
				
				isXiai = false;
				ItemStateUtils.shoucangButtonToNormalState(xiaiBt, getApplicationContext());
			}
		} else {
			
			isXiai = true;
			ItemStateUtils.shoucangButtonToFocusState(xiaiBt, getApplicationContext());
		}
	}
	
	private BaseAdapter tuiJianAdapter = new BaseAdapter() {
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if(convertView == null){
				convertView = LayoutInflater.from(ShowXiangqingMovie.this).inflate(R.layout.item_layout_gallery, null);
				holder = new ViewHolder();
				holder.firstTitle = (TextView) convertView.findViewById(R.id.first_title);
				holder.secondTitle = (TextView) convertView.findViewById(R.id.second_title);
				holder.content = (TextView) convertView.findViewById(R.id.content);
				holder.score = (TextView) convertView.findViewById(R.id.score);
				holder.image = (ImageView) convertView.findViewById(R.id.image);
				holder.definition = (ImageView) convertView.findViewById(R.id.definition);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
//			aq = new AQuery(convertView);
			holder.image.setTag(recommendMoviesData.items[position].prod_pic_url);
//			holder.image.setImageResource(R.drawable.test1);
			String bigPicUrl = recommendMoviesData.items[position].big_prod_pic_url;
			if(bigPicUrl == null || bigPicUrl.equals("")
					||bigPicUrl.equals(UtilTools.EMPTY)) {
				bigPicUrl = recommendMoviesData.items[position].prod_pic_url;
			}
			aq.id(holder.image).image(bigPicUrl,true,true,0,R.drawable.post_normal);
			holder.firstTitle.setVisibility(View.INVISIBLE);
			holder.secondTitle.setText(recommendMoviesData.items[position].prod_name);
//			holder.content.setText(recommendMoviesData.items[position].duration);
			if("".equals(recommendMoviesData.items[position].duration)){
				holder.content.setText("");
//				holder.content.setText("时长未知");
			}else{
//				holder.content.setText("时长："+hot_list.get(position).duration);
//				holder.content.setText("时长:"+recommendMoviesData.items[position].duration.replace("：00", "分钟"));
				holder.content.setText(UtilTools.formatMovieDuration(recommendMoviesData.items[position].duration ));
			}
			holder.score.setText(recommendMoviesData.items[position].score);
			switch (Integer.valueOf(recommendMoviesData.items[position].definition)) {
			case 8:
				holder.definition.setImageResource(R.drawable.icon_bd);
				break;
			case 7:
				holder.definition.setImageResource(R.drawable.icon_hd);
				break;
			case 6:
				holder.definition.setImageResource(R.drawable.icon_ts);
				break;
			default:
				holder.definition.setVisibility(View.GONE);
				break;
			}
			
			int width = parent.getWidth();
			convertView.setLayoutParams(new GridView.LayoutParams((int)((width-150)/6),
						(int)((width-100)*2/9)+20));
			return convertView;
		}
		
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if(recommendMoviesData.items.length>6){
				return 6;
			}else{
				return recommendMoviesData.items.length;
			}
		}
	}; 
	
	@Override
	protected void onDestroy() {
		if (aq != null)
			aq.dismiss();
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		MobclickAgent.onResume(this);
		
		if(app.getUserInfo()!=null){
			aq.id(R.id.iv_head_user_icon).image(
					app.getUserInfo().getUserAvatarUrl(), false, true, 0,
					R.drawable.avatar_defult);
			aq.id(R.id.tv_head_user_name).text(app.getUserInfo().getUserName());
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		MobclickAgent.onPause(this);
	}
	
	class ViewHolder{
		TextView firstTitle;
		TextView secondTitle;
		TextView content;
		TextView score;
		ImageView image;
		ImageView definition;
	}
	
	
	class CheckPlayUrl implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<URLS_INDEX> playUrls = new ArrayList<URLS_INDEX>();
			if(movieData.movie.episodes[0].down_urls==null){
				handler.sendEmptyMessage(0);	
				return ;
			}
			for(int i=0; i<movieData.movie.episodes[0].down_urls.length; i++){
					for(int j =0;j<movieData.movie.episodes[0].down_urls[i].urls.length; j++){
							URLS_INDEX url_index = new URLS_INDEX();
							url_index.source_from = movieData.movie.episodes[0].down_urls[i].source;
							url_index.url  = movieData.movie.episodes[0].down_urls[i].urls[j].url;
							if (movieData.movie.episodes[0].down_urls[i].source.trim().equalsIgnoreCase(Constant.video_index[0])) {
								url_index.souces = 0;
							} else if (movieData.movie.episodes[0].down_urls[i].source.trim().equalsIgnoreCase(Constant.video_index[1])) {
								url_index.souces = 1;
							} else if (movieData.movie.episodes[0].down_urls[i].source.trim().equalsIgnoreCase(Constant.video_index[2])) {
								url_index.souces = 2;
							} else if (movieData.movie.episodes[0].down_urls[i].source.trim().equalsIgnoreCase(Constant.video_index[3])) {
								url_index.souces = 3;
							} else if (movieData.movie.episodes[0].down_urls[i].source.trim().equalsIgnoreCase(Constant.video_index[4])) {
								url_index.souces = 4;
							} else if (movieData.movie.episodes[0].down_urls[i].source.trim().equalsIgnoreCase(Constant.video_index[5])) {
								url_index.souces = 5;
							} else if (movieData.movie.episodes[0].down_urls[i].source.trim().equalsIgnoreCase(Constant.video_index[6])) {
								url_index.souces = 6;
							} else if (movieData.movie.episodes[0].down_urls[i].source.trim().equalsIgnoreCase(Constant.video_index[7])) {
								url_index.souces = 7;
							} else if (movieData.movie.episodes[0].down_urls[i].source.trim().equalsIgnoreCase(Constant.video_index[8])) {
								url_index.souces = 8;
							} else if (movieData.movie.episodes[0].down_urls[i].source.trim().equalsIgnoreCase(Constant.video_index[9])) {
								url_index.souces = 9;
							} else if (movieData.movie.episodes[0].down_urls[i].source.trim().equalsIgnoreCase(Constant.video_index[10])) {
								url_index.souces = 10;
							} else if (movieData.movie.episodes[0].down_urls[i].source.trim().equalsIgnoreCase(Constant.video_index[11])) {
								url_index.souces = 11;
							} else {
								url_index.souces = 12;
							}
							if(movieData.movie.episodes[0].down_urls[i].urls[j].type.trim().equalsIgnoreCase(Constant.player_quality_index[1])){
								url_index.defination = 1;
							}else if(movieData.movie.episodes[0].down_urls[i].urls[j].type.trim().equalsIgnoreCase(Constant.player_quality_index[0])){
								url_index.defination = 2;
							}else if(movieData.movie.episodes[0].down_urls[i].urls[j].type.trim().equalsIgnoreCase(Constant.player_quality_index[2])){
								url_index.defination = 3;
							}else if(movieData.movie.episodes[0].down_urls[i].urls[j].type.trim().equalsIgnoreCase(Constant.player_quality_index[3])){
								url_index.defination = 4;
							} 
							playUrls.add(url_index);
						}
					}
					
					if(playUrls.size()>1){
						Collections.sort(playUrls, new DefinationComparatorIndex());
						Collections.sort(playUrls, new SouceComparatorIndex1());
					}
					Log.d(TAG, "test----------------playUrls size = " +playUrls.size());
					for(int n=0; n<playUrls.size(); n++){
						
						switch (playUrls.get(n).defination) {
						case 1:
							if(gaoqing_url==null&&app.CheckUrl(playUrls.get(n).url)){
								Log.d(TAG, "gaoqing_url-------ok----->" + playUrls.get(n).url);
								gaoqing_url = playUrls.get(n).url;
								gaoqing_url_souce = playUrls.get(n).source_from;
							}
							break;
						case 2:
							if(chaoqing_url==null&&app.CheckUrl(playUrls.get(n).url)){
								Log.d(TAG, "chaoqing_url-------ok----->" + playUrls.get(n).url);
								chaoqing_url = playUrls.get(n).url;
								chaoqing_url_souce = playUrls.get(n).source_from;
							}
							break;
						case 3:
							if(puqing_url==null&&app.CheckUrl(playUrls.get(n).url)){
								Log.d(TAG, "puqing_url-------ok----->" + playUrls.get(n).url);
								puqing_url = playUrls.get(n).url;
								puqing_url_souce = playUrls.get(n).source_from;
							}
							break;
						case 4:
							if(puqing_url==null&&app.CheckUrl(playUrls.get(n).url)){
								Log.d(TAG, "puqing_url-------ok----->" + playUrls.get(n).url);
								puqing_url = playUrls.get(n).url;
								puqing_url_souce = playUrls.get(n).source_from;
							}
							break;
						}
				}
				handler.sendEmptyMessage(0);	
			}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case DIALOG_WAITING:
			WaitingDialog dlg = new WaitingDialog(this);
			dlg.show();
			dlg.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					finish();
				}
			});
			dlg.setDialogWindowStyle();
			return dlg;
		default:
			return super.onCreateDialog(id);
		}
	}
	
	private void showDefultDate(){
		Intent intent = getIntent();
		if(intent!=null){
			aq.id(R.id.image).image(intent.getStringExtra("prod_url"), false, true,0, R.drawable.post_normal);
			aq.id(R.id.text_name).text(intent.getStringExtra("prod_name"));
			aq.id(R.id.text_directors).text(intent.getStringExtra("directors"));
			aq.id(R.id.text_starts).text(intent.getStringExtra("stars"));
			aq.id(R.id.text_introduce).text(intent.getStringExtra("summary"));
			aq.id(R.id.bt_xiangqingding).text(intent.getStringExtra("support_num"));
			aq.id(R.id.bt_xiangqing_xiai).text(intent.getStringExtra("favority_num"));
			if(intent.getStringExtra("definition")!=null&&!"".equals(intent.getStringExtra("definition"))){
				int definition = Integer.valueOf(intent.getStringExtra("definition"));
				switch (definition) {
				case 6:
					aq.id(R.id.img_definition).image(R.drawable.icon_ts);
					break;
				case 7:
					aq.id(R.id.img_definition).image(R.drawable.icon_hd);
					break;
				case 8:
					aq.id(R.id.img_definition).image(R.drawable.icon_bd);
					break;
				default:
					aq.id(R.id.img_definition).gone();
					break;
				}
			}
			if(intent.getStringExtra("score")!=null&&!"".equals(intent.getStringExtra("score"))){
				updateScore(intent.getStringExtra("score"));
			}
		}
	}
}
