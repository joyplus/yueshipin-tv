package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import com.joyplus.tv.Adapters.CurrentPlayData;
import com.joyplus.tv.Adapters.MainHotItemAdapter;
import com.joyplus.tv.Adapters.MainLibAdapter;
import com.joyplus.tv.Adapters.MainYueDanItemAdapter;
import com.joyplus.tv.Service.Return.ReturnMainHot;
import com.joyplus.tv.Service.Return.ReturnMainHot.URLS;
import com.joyplus.tv.Service.Return.ReturnTops;
import com.joyplus.tv.Service.Return.ReturnUserPlayHistories;
import com.joyplus.tv.Video.VideoPlayerActivity;
import com.joyplus.tv.entity.HotItemInfo;
import com.joyplus.tv.entity.ShiPinInfoParcelable;
import com.joyplus.tv.entity.YueDanInfo;
import com.joyplus.tv.ui.CustomGallery;
import com.joyplus.tv.ui.MyScrollLayout;
import com.joyplus.tv.ui.MyScrollLayout.OnViewChangeListener;
import com.joyplus.tv.ui.UserInfo;
import com.joyplus.tv.ui.WaitingDialog;
import com.saulpower.fayeclient.FayeClient;
import com.saulpower.fayeclient.FayeService;
import com.umeng.analytics.MobclickAgent;


public class Main extends Activity implements OnItemSelectedListener, OnItemClickListener{
	private String TAG = "Main";
	private App app;
	private AQuery aq;
	
	private int initStep = 0;
	
	private static final int DIALOG_WAITING = 0;
	
	private static final int MESSAGE_STEP1_SUCESS = 100; //热播列表加载完成
	private static final int MESSAGE_STEP2_SUCESS = MESSAGE_STEP1_SUCESS + 1; //悦单加载完成
	
	private static final int MESSAGE_START_TIMEOUT = MESSAGE_STEP2_SUCESS + 1;//stating画面超时
	private static final int MESSAGE_UPDATEUSER = MESSAGE_START_TIMEOUT + 1;//userid确认或者更改
	
	
	private static final int MESSAGE_UPDATEUSER_HISTORY_SUCEESS = MESSAGE_UPDATEUSER + 1;
	
	private ImageView startingImageView;
	private RelativeLayout rootLayout;
	private Map<String, String> headers;
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MESSAGE_UPDATEUSER://userInfo相关验证完成
				if(initStep ==0){
					initStep += 1;
					getHotServiceData();
				}
				Log.d(TAG, "MESSAGE_UPDATEUSER --- >Initstep = " + initStep);
				aq.id(R.id.iv_head_user_icon).image(app.getUserInfo().getUserAvatarUrl(),false,true,0,R.drawable.avatar);
				aq.id(R.id.tv_head_user_name).text(app.getUserInfo().getUserName());
				getHistoryServiceData();
				break;
			case MESSAGE_STEP1_SUCESS://热播列表加载完成
				Log.d(TAG, "MESSAGE_STEP1_SUCESS --- >Initstep = " + initStep); 
				if(initStep ==1){
					initStep +=1;
					getMovieYueDanServiceData();
					getTVYueDanServiceData();
				}
				break;//悦单加载完成
			case MESSAGE_STEP2_SUCESS://悦单加载完成
				Log.d(TAG, "MESSAGE_STEP2_SUCESS --- >Initstep = " + initStep);
				if(initStep ==2){
					initStep +=1;
					if(startingImageView.getVisibility() == View.VISIBLE){
						startingImageView.setVisibility(View.GONE);
						startingImageView.startAnimation(alpha_disappear);
						rootLayout.setVisibility(View.VISIBLE);
						gallery1.requestFocus();
						handler.removeMessages(MESSAGE_START_TIMEOUT);
					}else{
						removeDialog(DIALOG_WAITING);
						gallery1.requestFocus();
					}
				}
				break;
			case MESSAGE_START_TIMEOUT://超时还未加载好
				if(initStep<3){
					startingImageView.setVisibility(View.GONE);
					contentLayout.setVisibility(View.INVISIBLE);
					showDialog(DIALOG_WAITING);
				}
				break;
			case MESSAGE_UPDATEUSER_HISTORY_SUCEESS://超时还未加载好
//				if(initStep<3){
//					startingImageView.setVisibility(View.GONE);
//					contentLayout.setVisibility(View.INVISIBLE);
//					showDialog(DIALOG_WAITING);
//				}
				break;
			default:
				break;
			}
			
		}
		
	};
	ObjectMapper mapper = new ObjectMapper();
	private List<View> hot_contentViews = new ArrayList<View>();
	private List<View> yuedan_contentViews = new ArrayList<View>();
	private List<HotItemInfo> hot_list = new ArrayList<HotItemInfo>();
	private List<YueDanInfo> yuedan_list = new ArrayList<YueDanInfo>();
	private int isHotLoadedFlag = 0;
	private int isYueDanLoadedFlag = 0;
	
	private int [] resouces_lib_nomal = {
			R.drawable.movie_normal,
			R.drawable.episode_normal,
			R.drawable.cartoon_normal,
			R.drawable.variety_normal,
			R.drawable.search_normal
		};
	private int [] resouces_lib_active = {
			R.drawable.movie_active,
			R.drawable.episode_active,
			R.drawable.cartoon_active,
			R.drawable.variety_active,
			R.drawable.search_active,
		};
	
	private int [] resouces_my_nomal = {
			R.drawable.follow_normal,
			R.drawable.recent_normal,
			R.drawable.down_normal,
			R.drawable.system_normal
		};
	private int [] resouces_my_active = {
			R.drawable.follow_active,
			R.drawable.recent_active,
			R.drawable.down_active,
			R.drawable.system_active,
		};
	
	private CustomGallery gallery1;
	private float density;
	private int displayWith;
	private MyScrollLayout titleGroup;
	private FrameLayout itemFram;
	private ImageView highlightImageView;
	private ImageView playIcon;
	private LinearLayout contentLayout;
	private TextView noticeView;
	
//	private View hotView;
//	private View yeuDanView;
	private View kuView;
	private View myView;
	
//	private TextView hot_name_tv;
//	private TextView hot_score_tv;
//	private TextView hot_directors_tv;
//	private TextView hot_starts_tv;
//	private TextView hot_introduce_tv;
	
	private Map<Integer, Integer> indexCaces = new HashMap<Integer, Integer>();
	
	private Animation alpha_appear;
	private Animation alpha_disappear;
	
	private TranslateAnimation leftTranslateAnimationStep1;
	private TranslateAnimation leftTranslateAnimationStep2;
	private TranslateAnimation rightTranslateAnimationStep1;
	private TranslateAnimation rightTranslateAnimationStep2;
	
	private FayeClient mClient;
	private String macAddress;
	
	
	private BroadcastReceiver receiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if(FayeService.ACTION_RECIVEACTION_BAND.equals(action)){
				//band
				updateUser(app.getUserData("phoneID"));
			}else if(FayeService.ACTION_RECIVEACTION_UNBAND.equals(action)){
				//unband by mobile
				Log.d(TAG, "unband userId = " + app.getUserData("userId"));
				updateUser(app.getUserData("userId"));
			}
		}

		
	};
	
//	private Handler mHandler = new Handler();
	
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        startingImageView = (ImageView) findViewById(R.id.image_starting);
        rootLayout = (RelativeLayout) findViewById(R.id.root_layout);
        gallery1 = (CustomGallery) findViewById(R.id.gallery);
        contentLayout = (LinearLayout) findViewById(R.id.contentlayout);
        noticeView = (TextView) findViewById(R.id.notice_text);
        titleGroup = (MyScrollLayout) findViewById(R.id.group);
        titleGroup.setFocusable(false);
        titleGroup.setFocusableInTouchMode(false);
        
        kuView = LayoutInflater.from(Main.this).inflate(R.layout.layout_lib, null);
        myView = LayoutInflater.from(Main.this).inflate(R.layout.layout_my, null);
        ImageView erweimaImage = (ImageView) myView.findViewById(R.id.img_erweima);
        
        erweimaImage.setImageBitmap(CreateBarCode());
        titleGroup.SetOnViewChangeListener(new OnViewChangeListener() {
			
			@Override
			public void OnViewChange(int index) {
				// TODO Auto-generated method stub
				gallery1.startAnimation(alpha_appear);
				handler.removeCallbacks(null, null);
				switch (index) {
				case 1:
					if(isHotLoadedFlag == 2){
						itemFram.setVisibility(View.VISIBLE);
						contentLayout.removeAllViews();
						if(indexCaces.get(index) != null&&indexCaces.get(index)<hot_contentViews.size()-1){
							View hotView = hot_contentViews.get(indexCaces.get(index));
							hotView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
							contentLayout.startAnimation(alpha_appear);
							contentLayout.addView(hotView);
						}
						gallery1.setAdapter(new MainHotItemAdapter(Main.this, hot_list));
						if(indexCaces.get(index) == null){
							gallery1.setSelection(0);
						}else{
							gallery1.setSelection(indexCaces.get(index));
						}
//						changeContent(0);
						int seletedindex = gallery1.getSelectedItemPosition();
						ImageView img = null;
						if(seletedindex>=0&&seletedindex<hot_list.size()-1){
							img = (ImageView) gallery1.findViewWithTag(hot_list.get(gallery1.getSelectedItemPosition()).prod_pic_url);
						}
						if(img != null){
							if(img.getDrawable()!=null){
								highlightImageView.setImageDrawable(img.getDrawable());
							}else{
								aq.id(highlightImageView).image(hot_list.get(gallery1.getSelectedItemPosition()).prod_pic_url,true,true);
							}
						}else{
							if(seletedindex>=0&&seletedindex<hot_list.size()-1){
								aq.id(highlightImageView).image(hot_list.get(gallery1.getSelectedItemPosition()).prod_pic_url,true,true);
							}
						}
//						aq.id(highlightImageView).image(hot_list.get(gallery1.getSelectedItemPosition()).prod_pic_url);
//						noticeView.setText(gallery1.getSelectedItemPosition()+1 + "/" + hot_list.size());
						
					}else{
						itemFram.setVisibility(View.INVISIBLE);
						hot_list.clear();
						hot_contentViews.clear();
						gallery1.setAdapter(null);
						contentLayout.removeAllViews();
						getHistoryServiceData();
						getHotServiceData();
					}
					playIcon.setVisibility(View.VISIBLE);
					break;
				case 2:
					playIcon.setVisibility(View.INVISIBLE);
					if(isYueDanLoadedFlag==2){
						itemFram.setVisibility(View.VISIBLE);
						contentLayout.removeAllViews();
						if(indexCaces.get(index)!=null&&indexCaces.get(index)<yuedan_contentViews.size()-1){
							View yeuDanView = yuedan_contentViews.get(indexCaces.get(index));
							yeuDanView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
							contentLayout.startAnimation(alpha_appear);
							contentLayout.addView(yeuDanView);
						}
						gallery1.setAdapter(new MainYueDanItemAdapter(Main.this, yuedan_list));
						if(indexCaces.get(index) == null){
							gallery1.setSelection(0);
						}else{
							gallery1.setSelection(indexCaces.get(index));
						}
						if(gallery1.getSelectedItemPosition()==6){
							highlightImageView.setImageResource(R.drawable.more_movie_active);
						}else if(gallery1.getSelectedItemPosition()==7){
							highlightImageView.setImageResource(R.drawable.more_episode_active);
						}else {
							ImageView img2 = (ImageView) gallery1.findViewWithTag(yuedan_list.get(gallery1.getSelectedItemPosition()).pic_url);
							if(img2 != null){
								if(img2.getDrawable()!=null){
									highlightImageView.setImageDrawable(img2.getDrawable());
								}else{
									aq.id(highlightImageView).image(yuedan_list.get(gallery1.getSelectedItemPosition()).pic_url,true,true);
								}
							}else{
								aq.id(highlightImageView).image(yuedan_list.get(gallery1.getSelectedItemPosition()).pic_url,true,true);
							}
						}
						noticeView.setText(gallery1.getSelectedItemPosition()+1 + "/" + yuedan_list.size());
					}else{
						yuedan_list.clear();
						yuedan_contentViews.clear();
						gallery1.setAdapter(null);
						contentLayout.removeAllViews();
						itemFram.setVisibility(View.INVISIBLE);
						getMovieYueDanServiceData();
						getTVYueDanServiceData();
					}
					
					break;
				case 3:
					itemFram.setVisibility(View.VISIBLE);
					contentLayout.removeAllViews();
					kuView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
					contentLayout.startAnimation(alpha_appear);
					contentLayout.addView(kuView);
					gallery1.setAdapter(new MainLibAdapter(Main.this, resouces_lib_nomal));
					if(indexCaces.get(index) == null){
						gallery1.setSelection(0);
					}else{
						gallery1.setSelection(indexCaces.get(index));
					}
					playIcon.setVisibility(View.INVISIBLE);
					highlightImageView.setImageResource(resouces_lib_active[gallery1.getSelectedItemPosition()]);
					noticeView.setText(gallery1.getSelectedItemPosition()+1 + "/" + resouces_lib_active.length);
					break;
				case 4:
					itemFram.setVisibility(View.VISIBLE);
					contentLayout.removeAllViews();
					myView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
					contentLayout.startAnimation(alpha_appear);
					contentLayout.addView(myView);
					gallery1.setAdapter(new MainLibAdapter(Main.this, resouces_my_nomal));
					if(indexCaces.get(index) == null){
						gallery1.setSelection(0);
					}else{
						gallery1.setSelection(indexCaces.get(index));
					}
					playIcon.setVisibility(View.INVISIBLE);
					highlightImageView.setImageResource(resouces_my_active[gallery1.getSelectedItemPosition()]);
					noticeView.setText(gallery1.getSelectedItemPosition() +1+ "/" + resouces_my_active.length);
					break;
				}
			}
		});
        itemFram = (FrameLayout) findViewById(R.id.itemFram);
//        clock = (ClockTextView) findViewById(R.id.clock);
        highlightImageView = (ImageView) findViewById(R.id.highlight_img);
        playIcon = (ImageView) findViewById(R.id.play_icon);
//        MarginLayoutParams mlp = (MarginLayoutParams) gallery1.getLayoutParams();
        DisplayMetrics metrics = new DisplayMetrics();
        density = metrics.density;
        Display display = getWindowManager().getDefaultDisplay();
        displayWith = display.getWidth();
//        Toast.makeText(this, "widthPixels = " + display.get, 100).show();
//        Toast.makeText(this, "topMargin = " + mlp.topMargin, 100).show();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        mlp.setMargins(-displayWith+displayWith/2, 
//                       mlp.topMargin, 
//                       mlp.rightMargin, 
//                       mlp.bottomMargin
//        );
        gallery1.setAdapter(new MainHotItemAdapter(Main.this, hot_list));
//        gallery1.setCallbackDuringFling(false);
        gallery1.setOnItemSelectedListener(this);
        gallery1.setOnItemClickListener(this);
        gallery1.setSelection(1);
        aq = new AQuery(this);
        MarginLayoutParams mlp2 = (MarginLayoutParams) titleGroup.getLayoutParams();
        mlp2.setMargins((displayWith-40)/6+15, 
		        		mlp2.topMargin, 
		        		mlp2.rightMargin, 
		        		mlp2.bottomMargin);
        MarginLayoutParams mlp3 = (MarginLayoutParams) noticeView.getLayoutParams();
        mlp3.setMargins((displayWith-40)/6+15, 
        		mlp3.topMargin, 
        		mlp3.rightMargin, 
        		mlp3.bottomMargin);
//        MarginLayoutParams mlp4 = (MarginLayoutParams) contentLayout.getLayoutParams();
//        mlp4.setMargins((displayWith-40)/6+15, 
//        		mlp4.topMargin, 
//        		mlp4.rightMargin, 
//        		mlp4.bottomMargin);
        LayoutParams param = itemFram.getLayoutParams();
        param.height = 2*displayWith/9+3;
        param.width = displayWith/6+3;
        itemFram.setVisibility(View.INVISIBLE);
        alpha_appear = AnimationUtils.loadAnimation(this, R.anim.alpha_appear);
        alpha_disappear = AnimationUtils.loadAnimation(this, R.anim.alpha_disappear);
		app = (App) getApplicationContext();
		headers = new HashMap<String, String>();
		headers.put("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			headers.put("version", pInfo.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		IntentFilter filter = new IntentFilter();
        filter.addAction(FayeService.ACTION_RECIVEACTION_BAND);
        filter.addAction(FayeService.ACTION_RECIVEACTION_UNBAND);
        registerReceiver(receiver, filter);
	
		headers.put("app_key", Constant.APPKEY);
		headers.put("client","android");
		app.setHeaders(headers);

		if(!Constant.TestEnv)
			ReadLocalAppKey();
		checkLogin();
//		getHotServiceData();
		handler.sendEmptyMessageDelayed(MESSAGE_START_TIMEOUT, 10*1000);
//		getHistoryServiceData();
//		
//		DisplayMetrics dm = new DisplayMetrics();
//		this.getWindowManager().getDefaultDisplay().getMetrics(dm);
//		int width = dm.widthPixels;//屏幕宽度
//		int height = dm.heightPixels;//屏幕高度
//		
//		Toast.makeText(this, "Width:" + width + " Height:" + height, Toast.LENGTH_LONG).show();
    }
    
	@Override
	protected void onDestroy() {
		if (aq != null)
			aq.dismiss();
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	public void ReadLocalAppKey() {
	// online 获取APPKEY
	MobclickAgent.updateOnlineConfig(this);
	String OnLine_Appkey = MobclickAgent.getConfigParams(this, "APPKEY");
	if (OnLine_Appkey != null && OnLine_Appkey.length() >0) {
		Constant.APPKEY = OnLine_Appkey;
		headers.remove("app_key");
		headers.put("app_key", OnLine_Appkey);
		app.setHeaders(headers);
	}
}
public boolean checkLogin() {
	String usr_id = null;
	usr_id = app.GetServiceData("userId");
	if(usr_id == null){
		macAddress = null;
		WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = (null == wifiMgr ? null : wifiMgr
				.getConnectionInfo());
		if (info != null) {
			macAddress = info.getMacAddress();
			// 2. 通过调用 service account/generateUIID把UUID传递到服务器
			String url = Constant.BASE_URL + "account/generateUIID";

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("uiid", macAddress);
			params.put("device_type", "Android");

			AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
			cb.header("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
			cb.header("app_key", Constant.APPKEY);

			cb.params(params).url(url).type(JSONObject.class)
					.weakHandler(this, "CallServiceResult");
			aq.ajax(cb);
		}
	}else {
		UserInfo currentUserInfo = new UserInfo();
		currentUserInfo.setUserId(app.getUserData("userId"));
		currentUserInfo.setUserName(app.getUserData("userName"));
		currentUserInfo.setUserAvatarUrl(app.getUserData("userAvatarUrl"));
		headers.put("user_id", currentUserInfo.getUserId());
		app.setUser(currentUserInfo);
//			handler.sendEmptyMessage(MESSAGE_UPDATEUSER);
		checkBand();
		
	}
	return false;
}

private void checkBand(){
	
	if(app.getUserData("isBand") == null||"0".equals(app.getUserData("isBand"))){
		handler.sendEmptyMessage(MESSAGE_UPDATEUSER); 
	}else{
		String url = Constant.FAYESERVERURL_CHECKBAND + "?user_id=" + app.getUserData("phoneID") + "&tv_channel=" + (Constant.FAYECHANNEL_TV_BASE+StatisticsUtils.MD5(macAddress)).replace(Constant.FAYECHANNEL_TV_HEAD, "");
//		String url = Constant.BASE_URL;
		Log.d(TAG, url);
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, "CheckBandResult");
		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}
}

public void CheckBandResult(String url, JSONObject json, AjaxStatus status){

	Log.d(TAG, json.toString());
	if (json != null) {
		try {
			String result = json.getString("status");
			if("1".equals(result)){
				updateUser(app.getUserData("phoneID"));
			}else{
//				handler.sendEmptyMessage(MESSAGE_UPDATEUSER);
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
}

public void CallServiceResult(String url, JSONObject json, AjaxStatus status){

	Log.d(TAG, json.toString());
	if (json != null) {
		try {
			UserInfo currentUserInfo = new UserInfo();
			if(json.has("user_id"))
			{
				currentUserInfo.setUserId(json.getString("user_id").trim());
			}
			else
			{
				currentUserInfo.setUserId(json.getString("id").trim());
			}
			currentUserInfo.setUserName(json.getString("nickname"));
			currentUserInfo.setUserAvatarUrl(json.getString("pic_url"));
			app.SaveUserData("userId", currentUserInfo.getUserId());
			app.SaveUserData("userName", json.getString("nickname"));
			app.SaveUserData("userAvatarUrl", json.getString("pic_url"));
			app.setUser(currentUserInfo);
			headers.put("user_id", currentUserInfo.getUserId());
//			handler.sendEmptyMessage(MESSAGE_UPDATEUSER);
			checkBand();
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
}
    
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1,final int arg2,
			long arg3) {
		final int positon1 = gallery1.getSelectedItemPosition();
		if(leftTranslateAnimationStep2 == null){
			leftTranslateAnimationStep2  = new TranslateAnimation(itemFram.getLeft()+itemFram.getWidth()/5-itemFram.getWidth(), 
					itemFram.getLeft()-itemFram.getWidth(), 
					0, 
					0);
			leftTranslateAnimationStep2.setDuration(250);
			leftTranslateAnimationStep2.setInterpolator(new AccelerateInterpolator(0.1f));
		}
		if(rightTranslateAnimationStep2 == null){
			rightTranslateAnimationStep2  = new TranslateAnimation(itemFram.getLeft()-itemFram.getWidth()/4-itemFram.getWidth(), 
					itemFram.getLeft()-itemFram.getWidth(), 
					0, 
					0);
			rightTranslateAnimationStep2.setDuration(250);
			rightTranslateAnimationStep2.setInterpolator(new AccelerateInterpolator(0.1f));
		}
		switch (titleGroup.getSelectedTitleIndex()) {
		case 1:
			if(arg2<hot_list.size()&&arg2>=0){
				ImageView img = (ImageView) gallery1.findViewWithTag(hot_list.get(arg2).prod_pic_url);
				if(img != null){
					if(img.getDrawable()!=null){
						highlightImageView.setImageDrawable(img.getDrawable());
					}else{
						aq.id(highlightImageView).image(hot_list.get(arg2).prod_pic_url,true,true);
					}
				}else{
					aq.id(highlightImageView).image(hot_list.get(arg2).prod_pic_url,true,true);
				}
				noticeView.setText(arg2+ 1 + "/" + hot_list.size());
				handler.removeCallbacks(null, null);
				handler.postDelayed((new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Log.d(TAG, "index = " + arg2 + "lengh = " +  hot_contentViews.size());
						contentLayout.removeAllViews();
						View hotView = hot_contentViews.get(arg2);
						hotView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
						contentLayout.startAnimation(alpha_appear);
						contentLayout.addView(hotView);
					}
				}),300);
			}
			break;
		case 2:
			if(arg2<yuedan_list.size()&&arg2>=0){
				if("0".equals(yuedan_list.get(arg2).prod_type)){
					if("-1".equals(yuedan_list.get(arg2).id)){
						highlightImageView.setImageResource(R.drawable.more_movie_active);
					}else if("-2".equals(yuedan_list.get(arg2).id)){
						highlightImageView.setImageResource(R.drawable.more_episode_active);
					}
					noticeView.setText(arg2+ 1 + "/" + yuedan_list.size());
					handler.removeCallbacks(null, null);
					handler.postDelayed((new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							
							contentLayout.removeAllViews();
							if(arg2<yuedan_contentViews.size()){
								View yeuDanView = yuedan_contentViews.get(arg2);
								yeuDanView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
								contentLayout.startAnimation(alpha_appear);
								contentLayout.addView(yeuDanView);
							}
						}
					}),300);
				}else{
					if(gallery1.getSelectedItemPosition()==6){
						highlightImageView.setImageResource(R.drawable.more_movie_active);
					}else if(gallery1.getSelectedItemPosition()==7){
						highlightImageView.setImageResource(R.drawable.more_episode_active);
					}else {
						ImageView img2 = (ImageView) gallery1.findViewWithTag(yuedan_list.get(gallery1.getSelectedItemPosition()).pic_url);
						if(img2 != null){
							if(img2.getDrawable()!=null){
								highlightImageView.setImageDrawable(img2.getDrawable());
							}else{
								aq.id(highlightImageView).image(yuedan_list.get(gallery1.getSelectedItemPosition()).pic_url,true,true);
							}
						}else{
							aq.id(highlightImageView).image(yuedan_list.get(gallery1.getSelectedItemPosition()).pic_url,true,true);
						}
					}
					noticeView.setText(arg2+ 1 + "/" + yuedan_list.size());
					handler.removeCallbacks(null, null);
					handler.postDelayed((new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							
							contentLayout.removeAllViews();
							if(arg2<yuedan_contentViews.size()){
								View yeuDanView = yuedan_contentViews.get(arg2);
								yeuDanView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
								contentLayout.startAnimation(alpha_appear);
								contentLayout.addView(yeuDanView);
							}
						}
					}),300);
				}
			}
				
			break;
		case 3:
			if(positon1<resouces_lib_active.length){
				highlightImageView.setImageResource(resouces_lib_active[positon1]);
				noticeView.setText(positon1+1 + "/" + resouces_lib_active.length);
			}
			break;
		case 4:
			if(positon1<resouces_my_active.length){
				highlightImageView.setImageResource(resouces_my_active[positon1]);
				noticeView.setText(positon1+1 + "/" + resouces_my_active.length);
			}
			break;
		
		}
		if(indexCaces.get(titleGroup.getSelectedTitleIndex())!=null&&indexCaces.get(titleGroup.getSelectedTitleIndex())<arg2){
			itemFram.startAnimation(leftTranslateAnimationStep2);
		}
		if(indexCaces.get(titleGroup.getSelectedTitleIndex())!=null&&indexCaces.get(titleGroup.getSelectedTitleIndex())>arg2){
			itemFram.startAnimation(rightTranslateAnimationStep2);
		} 
		indexCaces.put(titleGroup.getSelectedTitleIndex(), arg2);
		// TODO Auto-generated method stub
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
//		Toast.makeText(this, "key code = " + keyCode, 100).show();
		if(initStep>3){
			return true;
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
			titleGroup.selectPreTitle();
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			titleGroup.selectNextTitle();
			return true;
		
		case KeyEvent.KEYCODE_DPAD_RIGHT:
//			final int positon1 = gallery1.getSelectedItemPosition();
//			if(leftTranslateAnimationStep2 == null){
//				leftTranslateAnimationStep2  = new TranslateAnimation(itemFram.getLeft()+itemFram.getWidth()/5-itemFram.getWidth(), 
//						itemFram.getLeft()-itemFram.getWidth(), 
//						0, 
//						0);
//				leftTranslateAnimationStep2.setDuration(250);
//				leftTranslateAnimationStep2.setInterpolator(new AccelerateInterpolator(0.1f));
//			}
//			switch (titleGroup.getSelectedTitleIndex()) {
//			case 1:
//				if(positon1<hot_list.size()-1){
////					aq.id(highlightImageView).image(hot_list.get(positon1+1).prod_pic_url,true,true);
////					itemFram.startAnimation(leftTranslateAnimationStep2);
////					noticeView.setText(positon1+2 + "/" + hot_list.size());
//					changeContent(1);
//				}
//				break;
//			case 2:
//				if(positon1<resouces_lib_active.length-1){
////					highlightImageView.setImageResource(resouces_lib_active[positon1+1]);
////					itemFram.startAnimation(leftTranslateAnimationStep2);
////					noticeView.setText(positon1+2 + "/" + resouces_lib_active.length);
//					changeContent(1);
//				}
//				break;
//			case 3:
//				if(positon1<resouces_lib_active.length-1){
//					highlightImageView.setImageResource(resouces_lib_active[positon1+1]);
//					itemFram.startAnimation(leftTranslateAnimationStep2);
//					noticeView.setText(positon1+2 + "/" + resouces_lib_active.length);
//				}
//				break;
//			case 4:
//				if(positon1<resouces_my_active.length-1){
//					highlightImageView.setImageResource(resouces_my_active[positon1+1]);
//					itemFram.startAnimation(leftTranslateAnimationStep2);
//					noticeView.setText(positon1+2 + "/" + resouces_my_active.length);
//				}
//				break;
//			
//			}
			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
//			
//			
//			final int positon2 = gallery1.getSelectedItemPosition();
//			if(rightTranslateAnimationStep2 == null){
//				rightTranslateAnimationStep2  = new TranslateAnimation(itemFram.getLeft()-itemFram.getWidth()/4-itemFram.getWidth(), 
//						itemFram.getLeft()-itemFram.getWidth(), 
//						0, 
//						0);
//				rightTranslateAnimationStep2.setDuration(250);
//				rightTranslateAnimationStep2.setInterpolator(new AccelerateInterpolator(0.1f));
//			}
//			
//			
//		switch (titleGroup.getSelectedTitleIndex()) {
//		case 1:
////			if(positon2>0){
////				highlightImageView.setImageResource(resouces[positon2]);
////				itemFram.startAnimation(rightTranslateAnimationStep2);
////				noticeView.setText(positon2 + "/" + resouces.length);
////			}
//			if(positon2>0){
////				aq.id(highlightImageView).image(hot_list.get(positon2-1).prod_pic_url,true,true);
////				itemFram.startAnimation(rightTranslateAnimationStep2);
////				noticeView.setText(positon2 + "/" + hot_list.size());
//				changeContent(-1);
//			}
//			break;
//		case 2:
//			if(positon2>0){
////				highlightImageView.setImageResource(resouces_lib_active[positon2-1]);
////				itemFram.startAnimation(leftTranslateAnimationStep2);
////				noticeView.setText(positon2 + "/" + resouces_lib_active.length);
//				changeContent(-1);
//			}
//			break;
//		case 3:
//			if(positon2>0){
//				highlightImageView.setImageResource(resouces_lib_active[positon2-1]);
//				itemFram.startAnimation(leftTranslateAnimationStep2);
//				noticeView.setText(positon2 + "/" + resouces_lib_active.length);
//			}
//			break;
//		case 4:
//			if(positon2>0){
//				highlightImageView.setImageResource(resouces_my_active[positon2-1]);
//				itemFram.startAnimation(leftTranslateAnimationStep2);
//				noticeView.setText(positon2 + "/" + resouces_my_active.length);
//			}
//			break;
//		
//		}
//			
			return true;
		}
		return false;
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "item click index = " + titleGroup.getSelectedTitleIndex()+"[" + index + "]", 100).show();
		switch (titleGroup.getSelectedTitleIndex()) {
		case 1:
			HotItemInfo info = hot_list.get(index);
			Intent intent;
			if(info.type==0){
				//历史
				CurrentPlayData playDate = new CurrentPlayData();
				intent = new Intent(this,VideoPlayerActivity.class);
				playDate.prod_id = info.prod_id;
				playDate.prod_type = Integer.valueOf(info.prod_type);
				playDate.prod_name = info.prod_name;
				playDate.prod_url = info.video_url;
//				playDate.prod_src = "";
				if(!"".equals(info.playback_time)){
					playDate.prod_time = Long.valueOf(info.playback_time);
				}
//				playDate.prod_qua = Integer.valueOf(info.definition);
				app.setCurrentPlayData(playDate);
				startActivity(intent);
			}else if(info.type == 1){
				int prod_type = Integer.valueOf(info.prod_type);
				switch (prod_type) {
				case 1:
					ArrayList<PLAY_URLS_INDEX> list = new ArrayList<PLAY_URLS_INDEX>();
					for(int i=0; i<info.play_urls.length; i++){
						PLAY_URLS_INDEX indexObj = new PLAY_URLS_INDEX();
						indexObj.source = info.play_urls[i].source;
						indexObj.urls = info.play_urls[i].urls;
						if (indexObj.source.equalsIgnoreCase("letv")) {
							indexObj.index = 0;
						} else if (indexObj.source.equalsIgnoreCase("fengxing")) {
							indexObj.index = 1;
						} else if (indexObj.source.equalsIgnoreCase("qiyi")) {
							indexObj.index = 2;
						} else if (indexObj.source.equalsIgnoreCase("youku")) {
							indexObj.index = 3;
						} else if (indexObj.source.equalsIgnoreCase("sinahd")) {
							indexObj.index = 4;
						} else if (indexObj.source.equalsIgnoreCase("sohu")) {
							indexObj.index = 5;
						} else if (indexObj.source.equalsIgnoreCase("56")) {
							indexObj.index = 6;
						} else if (indexObj.source.equalsIgnoreCase("qq")) {
							indexObj.index = 7;
						} else if (indexObj.source.equalsIgnoreCase("pptv")) {
							indexObj.index = 8;
						} else if (indexObj.source.equalsIgnoreCase("m1905")) {
							indexObj.index = 9;
						}
						list.add(indexObj);
					}
					if(list.size()<1){
						Collections.sort(list, new EComparatorIndex());
					}
					PLAY_URLS_INDEX index1 = list.get(0);
					URLS[] urls = index1.urls;
					List<URLS_INDEX> list1 = new ArrayList<Main.URLS_INDEX>();
					String videoUrl;
					for(int i=0; i<urls.length; i++){
						URLS_INDEX url_index = new URLS_INDEX();
						url_index.type = urls[i].type.trim();
						url_index.url = urls[i].url.trim();
						if(url_index.type.equalsIgnoreCase("mp4")){
							url_index.index = 1;
						}else if(urls[i].type.trim().equalsIgnoreCase("hd2")){
							url_index.index = 2;
						}else if(urls[i].type.trim().equalsIgnoreCase("flv")){
							url_index.index = 3;
						}else if(urls[i].type.trim().equalsIgnoreCase("3GP")){
							url_index.index = 4;
						}
						list1.add(url_index);
					}
					if(list.size()<1){
						Collections.sort(list1, new EComparatorIndex1());
					}
					videoUrl = list1.get(0).url;
					Log.d(TAG, "url---->" + videoUrl);
					Log.d(TAG, "name---->" + info.prod_name);
					CurrentPlayData playDate = new CurrentPlayData();
					intent = new Intent(this,VideoPlayerActivity.class);
					playDate.prod_id = info.prod_id;
					playDate.prod_type = Integer.valueOf(info.prod_type);
					playDate.prod_name = info.prod_name;
					playDate.prod_url = videoUrl;
//					playDate.prod_src = "";
					if(!"".equals(info.playback_time)){
						playDate.prod_time = Long.valueOf(info.playback_time);
					}
//					playDate.prod_qua = Integer.valueOf(info.definition);
					app.setCurrentPlayData(playDate);
					startActivity(intent);
					break;
				case 2:
					intent = new Intent(this,ShowXiangqingTv.class);
					intent.putExtra("ID", info.prod_id);
					startActivity(intent);
					break;
				case 3:
//					intent = new Intent(this,ShowXiangqingDongman.class);
//					intent.putExtra("ID", info.prod_id);
					break;
				case 131:
					intent = new Intent(this,ShowXiangqingDongman.class);
					intent.putExtra("ID", info.prod_id);
					startActivity(intent);
					break;
				}
			}
			break;
		case 2:
			Intent yuedanIntent = new Intent();
			YueDanInfo yueDan = yuedan_list.get(index);
			if(Integer.valueOf(yueDan.prod_type) == 0){
				yuedanIntent.setClass(Main.this, ShowYueDanActivity.class);
				if("-1".equals(yueDan.id)){
					yuedanIntent.putExtra("yuedan_type", "1"); 
				}else if("-2".equals(yueDan.id)){
					yuedanIntent.putExtra("yuedan_type", "2");
				}
			}else{
				Bundle bundle = new Bundle();
				bundle.putString("ID", yuedan_list.get(index).id);
				bundle.putString("NAME", yuedan_list.get(index).name);
				yuedanIntent.putExtras(bundle);
//				yuedanIntent.putParcelableArrayListExtra("yuedan_list_type", yuedan_list.get(index).shiPinList);
				yuedanIntent.setClass(Main.this, ShowYueDanListActivity.class);
			}
			startActivity(yuedanIntent);
			break;
		case 3:
			switch (index) {
			case 0:
				startActivity(new Intent(this,ShowMovieActivity.class));
				break;
			case 1:
				startActivity(new Intent(this,ShowTVActivity.class));
				break;
			case 2:
				startActivity(new Intent(this,ShowDongManActivity.class));
				break;
			case 3:
				startActivity(new Intent(this,ShowZongYiActivity.class));
				break;
			case 4:
				startActivity(new Intent(this,ShowSearchActivity.class));
				break;
			}
			break;
		case 4:
			switch (index) {
			case 0:
				startActivity(new Intent(this,ShowShoucangHistoryActivity.class));
				break;
			case 1:
				startActivity(new Intent(this,HistoryActivity.class));
				break;
			case 2:
//				startActivity(new Intent(this,ShowDongManActivity.class));
				break;
			case 3:
//				startActivity(new Intent(this,ShowZongYiActivity.class));
				break;
			case 4:
//				startActivity(new Intent(this,ShowSearchActivity.class));
				break;
			}
			break;
		default:
			break; 
		}
	}
	
	
	public void getHotServiceData() { 
		String url = Constant.BASE_URL + "tv_net_top" +"?page_num=1&page_size=10";

//		String url = Constant.BASE_URL;
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>(); 
		cb.url(url).type(JSONObject.class).weakHandler(this, "initHotData");

		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}
	
	public void getHistoryServiceData() {
		String url = Constant.BASE_URL + "user/playHistories" +"?page_num=1&page_size=1";

//		String url = Constant.BASE_URL;
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, "initHistoryData");

		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}
	
	public void getMovieYueDanServiceData() {
		String url = Constant.BASE_URL + "tops" +"?page_num=1&page_size=3&topic_type=1";

//		String url = Constant.BASE_URL;
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, "initYueDanData");

		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}
	public void getTVYueDanServiceData() {
		String url = Constant.BASE_URL + "tops" +"?page_num=1&page_size=3&topic_type=2";
		
//		String url = Constant.BASE_URL;
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, "initYueDanData");
		
		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}
	
	public synchronized void initYueDanData(String url, JSONObject json, AjaxStatus status){
		if (status.getCode() == AjaxStatus.NETWORK_ERROR)  {
//			aq.id(R.id.ProgressText).invisible();
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		Log.d(TAG, "history data = " + json.toString());
		try {
			ReturnTops result  = mapper.readValue(json.toString(), ReturnTops.class);
			for(int i=0; i<result.tops.length; i++){
				YueDanInfo yuedanInfo = new YueDanInfo();
				ArrayList<ShiPinInfoParcelable> shiPinInfos = new ArrayList<ShiPinInfoParcelable>();
				yuedanInfo.name = result.tops[i].name;
				yuedanInfo.id = result.tops[i].id;
				yuedanInfo.prod_type = result.tops[i].prod_type;
				yuedanInfo.pic_url = result.tops[i].big_pic_url;
				yuedanInfo.num = result.tops[i].num;
				yuedanInfo.content = result.tops[i].content;
				for (int j = 0; j < result.tops[i].items.length; j++) {
					ShiPinInfoParcelable shipinInfo = new ShiPinInfoParcelable();
					shipinInfo.setArea(result.tops[i].items[j].area);
					shipinInfo.setBig_prod_pic_url(result.tops[i].items[j].big_prod_pic_url);
					shipinInfo.setCur_episode(result.tops[i].items[j].cur_episode);
//					shipinInfo.setCur_item_name(result.tops[i].items[j].cur_i);
					shipinInfo.setDefinition(result.tops[i].items[j].definition);
					shipinInfo.setDirectors(result.tops[i].items[j].directors);
					shipinInfo.setDuration(result.tops[i].items[j].duration);
					shipinInfo.setFavority_num(result.tops[i].items[j].favority_num);
					shipinInfo.setId(result.tops[i].items[j].id);
					shipinInfo.setMax_episode(result.tops[i].items[j].max_episode);
					shipinInfo.setProd_id(result.tops[i].items[j].prod_id);
					shipinInfo.setProd_name(result.tops[i].items[j].prod_name);
					shipinInfo.setProd_pic_url(result.tops[i].items[j].prod_pic_url);
					shipinInfo.setProd_type(result.tops[i].items[j].prod_type);
					shipinInfo.setPublish_date(result.tops[i].items[j].publish_date);
					shipinInfo.setScore(result.tops[i].items[j].score);
					shipinInfo.setStars(result.tops[i].items[j].stars);
					shipinInfo.setSupport_num(result.tops[i].items[j].support_num);
					shiPinInfos.add(shipinInfo);
				}
				yuedanInfo.shiPinList = shiPinInfos;
				View yueDanView = LayoutInflater.from(Main.this).inflate(R.layout.layout_list, null);
				
				LinearLayout layout1 = (LinearLayout) yueDanView.findViewById(R.id.shipin_layout1);
				LinearLayout layout2 = (LinearLayout) yueDanView.findViewById(R.id.shipin_layout2);
				LinearLayout layout3 = (LinearLayout) yueDanView.findViewById(R.id.shipin_layout3);
				LinearLayout layout4 = (LinearLayout) yueDanView.findViewById(R.id.shipin_layout4);
				TextView yueDanNameText = (TextView) yueDanView.findViewById(R.id.yuedan_name);
				TextView shipNameText1 = (TextView) yueDanView.findViewById(R.id.shipin1);
				TextView shipNameText2 = (TextView) yueDanView.findViewById(R.id.shipin2);
				TextView shipNameText3 = (TextView) yueDanView.findViewById(R.id.shipin3);
				TextView shipNameText4 = (TextView) yueDanView.findViewById(R.id.shipin4);
				TextView shiPinNumNotice = (TextView) yueDanView.findViewById(R.id.notice_num);
				yueDanNameText.setText(yuedanInfo.name);
				if(yuedanInfo.shiPinList!=null){
					switch (yuedanInfo.shiPinList.size()) {
					case 0:
						layout1.setVisibility(View.GONE);
						layout2.setVisibility(View.GONE);
						layout3.setVisibility(View.GONE);
						layout4.setVisibility(View.GONE);
						shiPinNumNotice.setVisibility(View.GONE);
						break;
					case 1:
						shipNameText1.setText(yuedanInfo.shiPinList.get(0).getProd_name());
						layout2.setVisibility(View.GONE);
						layout3.setVisibility(View.GONE);
						layout4.setVisibility(View.GONE);
						shiPinNumNotice.setVisibility(View.GONE);
						break;
					case 2:
						shipNameText1.setText(yuedanInfo.shiPinList.get(0).getProd_name());
						shipNameText2.setText(yuedanInfo.shiPinList.get(1).getProd_name());
						layout3.setVisibility(View.GONE);
						layout4.setVisibility(View.GONE);
						shiPinNumNotice.setVisibility(View.GONE);
						break;
					case 3:
						shipNameText1.setText(yuedanInfo.shiPinList.get(0).getProd_name());
						shipNameText2.setText(yuedanInfo.shiPinList.get(1).getProd_name());
						shipNameText3.setText(yuedanInfo.shiPinList.get(2).getProd_name());
						layout4.setVisibility(View.GONE);
						shiPinNumNotice.setVisibility(View.GONE);
						break;
					case 4:
						shipNameText1.setText(yuedanInfo.shiPinList.get(0).getProd_name());
						shipNameText2.setText(yuedanInfo.shiPinList.get(1).getProd_name());
						shipNameText3.setText(yuedanInfo.shiPinList.get(2).getProd_name());
						shipNameText4.setText(yuedanInfo.shiPinList.get(3).getProd_name());
						shiPinNumNotice.setVisibility(View.GONE);
						break;

					default:
						shipNameText1.setText(yuedanInfo.shiPinList.get(0).getProd_name());
						shipNameText2.setText(yuedanInfo.shiPinList.get(1).getProd_name());
						shipNameText3.setText(yuedanInfo.shiPinList.get(2).getProd_name());
						shipNameText4.setText(yuedanInfo.shiPinList.get(3).getProd_name());
						shiPinNumNotice.setText(getResources().getString(R.string.yuedan_notice_num, yuedanInfo.num));
						break;
					}
				}
				if("1".equals(yuedanInfo.prod_type)){
					yuedan_list.add(i,yuedanInfo);
					yuedan_contentViews.add(i, yueDanView);
					
				}else{
					yuedan_list.add(yuedanInfo);
					yuedan_contentViews.add(yueDanView);
				}
			}
			if(isYueDanLoadedFlag==1){
				isYueDanLoadedFlag = 2;
			}else{
				isYueDanLoadedFlag = 1;
			}
			if(isYueDanLoadedFlag ==2){
				YueDanInfo yuedanInfo1 = new YueDanInfo();
				yuedanInfo1.prod_type = "0";    
				yuedanInfo1.id = "-1";
				yuedan_list.add(yuedanInfo1);
				YueDanInfo yuedanInfo2 = new YueDanInfo();
				yuedanInfo2.prod_type = "0";
				yuedanInfo2.id = "-2";
				yuedan_list.add(yuedanInfo2);
//				itemFram.setVisibility(View.VISIBLE);
//				itemFram.setVisibility(View.VISIBLE);
//				gallery1.setAdapter(new MainYueDanItemAdapter(Main.this, yuedan_list));
//				gallery1.setSelection(0);
				handler.sendEmptyMessage(MESSAGE_STEP2_SUCESS);
				return ;
			}
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
	
	public void initHistoryData(String url, JSONObject json, AjaxStatus status){
		if (status.getCode() == AjaxStatus.NETWORK_ERROR)  {
//			aq.id(R.id.ProgressText).invisible();
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		Log.d(TAG, "history data = " + json.toString());
		try {
			ReturnUserPlayHistories result  = mapper.readValue(json.toString(), ReturnUserPlayHistories.class);
			HotItemInfo item  =  new HotItemInfo();
			if(hot_list.size()>0){
				if(hot_list.get(0).type == 0){
					hot_list.remove(0);
					hot_contentViews.remove(0);
				}
			}
			if(result.histories.length == 0){
				if(isHotLoadedFlag == 1){
					if(titleGroup.getSelectedTitleIndex()==1){
						itemFram.setVisibility(View.VISIBLE);
						gallery1.setAdapter(new MainHotItemAdapter(Main.this, hot_list));
						gallery1.setSelection(0);
						handler.sendEmptyMessage(MESSAGE_STEP1_SUCESS);
					}
					isHotLoadedFlag = 2;	
				}else if(isHotLoadedFlag == 0){
					isHotLoadedFlag = 1;
				}else if(isHotLoadedFlag == 2){
					itemFram.setVisibility(View.VISIBLE);
					gallery1.setAdapter(new MainHotItemAdapter(Main.this, hot_list));
					gallery1.setSelection(0);
					handler.sendEmptyMessage(MESSAGE_UPDATEUSER_HISTORY_SUCEESS);
				}
				return ;
			}
			item.type = 0;
			item.id = result.histories[0].id;
			item.prod_id = result.histories[0].prod_id;
			item.prod_name = result.histories[0].prod_name;
			item.prod_type = result.histories[0].prod_type;
			item.prod_pic_url = result.histories[0].big_prod_pic_url;
			item.stars = result.histories[0].stars;
			item.directors = result.histories[0].directors;
			item.favority_num = result.histories[0].favority_num;
			item.support_num = result.histories[0].support_num;
			item.publish_date = result.histories[0].publish_date;
			item.score = result.histories[0].score;
			item.area = result.histories[0].area;
			item.cur_episode = result.histories[0].cur_episode;
			item.definition = result.histories[0].definition;
			item.prod_summary = result.histories[0].prod_summary;
			item.duration = result.histories[0].duration;
			item.playback_time = result.histories[0].playback_time;
			
			View hotView = LayoutInflater.from(Main.this).inflate(R.layout.layout_hot, null);
			TextView hot_name_tv = (TextView) hotView.findViewById(R.id.hot_content_name);
			TextView hot_score_tv = (TextView) hotView.findViewById(R.id.hot_content_score);
			TextView hot_directors_tv = (TextView) hotView.findViewById(R.id.hot_content_directors);
			TextView hot_starts_tv = (TextView) hotView.findViewById(R.id.hot_content_stars);
			TextView hot_introduce_tv = (TextView) hotView.findViewById(R.id.hot_content_introduce);
			
			hot_name_tv.setText(item.prod_name);
			hot_score_tv.setText(item.score);
			hot_directors_tv.setText(item.directors);
			hot_starts_tv.setText(item.stars);
			hot_introduce_tv.setText(item.prod_summary);
			hot_list.add(0,item);
			hot_contentViews.add(0,hotView);
			Log.d(TAG, "lengh = " + hot_contentViews.size());
			if(isHotLoadedFlag ==1 ){
				if(titleGroup.getSelectedTitleIndex()==1){
					itemFram.setVisibility(View.VISIBLE);
					gallery1.setAdapter(new MainHotItemAdapter(Main.this, hot_list));
					if(hot_list.size()>0){
						if(hot_list.get(0).type == 0){
							gallery1.setSelection(1);
						}else{
							gallery1.setSelection(0);
						}
					}
					handler.sendEmptyMessage(MESSAGE_STEP1_SUCESS);
				}
				isHotLoadedFlag = 2;
				return;
			}else if(isHotLoadedFlag == 0){
				isHotLoadedFlag = 1;
			}else if(isHotLoadedFlag == 2){
				if(titleGroup.getSelectedTitleIndex()==1){
					itemFram.setVisibility(View.VISIBLE);
					gallery1.setAdapter(new MainHotItemAdapter(Main.this, hot_list));
					if(hot_list.size()>0){
						if(hot_list.get(0).type == 0){
							gallery1.setSelection(1);
						}else{
							gallery1.setSelection(0);
						}
					}
					handler.sendEmptyMessage(MESSAGE_UPDATEUSER_HISTORY_SUCEESS);
				}
			}
			isHotLoadedFlag = 1;
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
	
	public void initHotData(String url, JSONObject json, AjaxStatus status){
		if (status.getCode() == AjaxStatus.NETWORK_ERROR)  {
//			aq.id(R.id.ProgressText).invisible();
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		try {
			Log.d(TAG, json.toString());
			ReturnMainHot result  = mapper.readValue(json.toString(), ReturnMainHot.class);
//			hot_list.clear();
			for(int i=0; i <result.items.length; i++){
				HotItemInfo item  =  new HotItemInfo();
				item.type = 1;
				item.id = result.items[i].id;
				item.prod_id = result.items[i].prod_id;
				item.prod_name = result.items[i].prod_name;
				item.prod_type = result.items[i].prod_type;
				item.prod_pic_url = result.items[i].prod_pic_url;
				item.stars = result.items[i].stars;
				item.directors = result.items[i].directors;
				item.favority_num = result.items[i].favority_num;
				item.support_num = result.items[i].support_num;
				item.publish_date = result.items[i].publish_date;
				item.score = result.items[i].score;
				item.area = result.items[i].area;
				item.cur_episode = result.items[i].cur_episode;
				item.definition = result.items[i].definition;
				item.prod_summary = result.items[i].prod_summary;
				item.duration = result.items[i].duration;
				item.play_urls = result.items[i].play_urls;
				item.playback_time = "";
				hot_list.add(item);
				View hotView = LayoutInflater.from(Main.this).inflate(R.layout.layout_hot, null);
				TextView hot_name_tv = (TextView) hotView.findViewById(R.id.hot_content_name);
				TextView hot_score_tv = (TextView) hotView.findViewById(R.id.hot_content_score);
				TextView hot_directors_tv = (TextView) hotView.findViewById(R.id.hot_content_directors);
				TextView hot_starts_tv = (TextView) hotView.findViewById(R.id.hot_content_stars);
				TextView hot_introduce_tv = (TextView) hotView.findViewById(R.id.hot_content_introduce);
				
				hot_name_tv.setText(item.prod_name);
				hot_score_tv.setText(item.score);
				hot_directors_tv.setText(item.directors);
				hot_starts_tv.setText(item.stars);
				hot_introduce_tv.setText(item.prod_summary);
				hot_contentViews.add(hotView);
			}
//			Log.d
			
			if(isHotLoadedFlag == 1){
				if(titleGroup.getSelectedTitleIndex() == 1){
					gallery1.setAdapter(new MainHotItemAdapter(Main.this, hot_list));
					if(hot_list.size()>0){
						if(hot_list.get(0).type == 0){
							gallery1.setSelection(1);
						}else{
							gallery1.setSelection(0);
						}
					}
					itemFram.setVisibility(View.VISIBLE);
					handler.sendEmptyMessage(MESSAGE_STEP1_SUCESS);
				}
				isHotLoadedFlag = 2;
				return ;
			}
			isHotLoadedFlag = 1;
//			aq.id(highlightImageView).image(hot_list.get(gallery1.getSelectedItemPosition()).prod_pic_url,true,true);
//			noticeView.setText(gallery1.getSelectedItemPosition()+1 + "/" + hot_list.size());
//			changeContent(0);
//			hot_list.add(arg0);
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
	
	
//	private void changeContent1(int dx){
//		final int positon = gallery1.getSelectedItemPosition();
//		switch (titleGroup.getSelectedTitleIndex()) {
//		case 1:
//			
////			hot_name_tv = (TextView) hotView.findViewById(R.id.hot_content_name);
////	        hot_score_tv = (TextView) hotView.findViewById(R.id.hot_content_score);
////	        hot_directors_tv = (TextView) hotView.findViewById(R.id.hot_content_directors);
////	        hot_starts_tv = (TextView) hotView.findViewById(R.id.hot_content_stars);
////	        hot_introduce_tv = (TextView) hotView.findViewById(R.id.hot_content_introduce);
////			Log.d(TAG, "------------------------");
//			
////			aq.id(highlightImageView).image(hot_list.get(positon+dx).prod_pic_url,true,true); 
//			
//			ImageView img = (ImageView) gallery1.findViewWithTag(hot_list.get(positon+dx).prod_pic_url);
//			if(img != null){
//				highlightImageView.setImageDrawable(img.getDrawable());
//			}else{
//				aq.id(highlightImageView).image(hot_list.get(positon+dx).prod_pic_url,true,true);
//			}
////			if(dx==1){
////				itemFram.startAnimation(leftTranslateAnimationStep2);
////			}
////			if(dx==-1){
////				itemFram.startAnimation(rightTranslateAnimationStep2);
////			} 
//			
//			if(indexCaces.get(1)!=null&&indexCaces.get(1)<positon){
//				itemFram.startAnimation(leftTranslateAnimationStep2);
//			}
//			Log.d(TAG, "positon = " + positon + "laset = " + indexCaces.get(1));
//			if(indexCaces.get(1)!=null&&indexCaces.get(1)>positon){
//				itemFram.startAnimation(rightTranslateAnimationStep2);
//			} 
//			
////			itemFram.setVisibility(View.GONE);
//			handler.removeCallbacksAndMessages(null);
//			handler.postDelayed((new Runnable() {
//				
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					contentLayout.removeAllViews();
//					View hotView = hot_contentViews.get(positon);
//					hotView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
//					contentLayout.startAnimation(alpha_appear);
//					contentLayout.addView(hotView);
//				}
//			}),280);
//			
//			
////			contentLayout.startAnimation(alpha_appear);
////			hotView.invalidate();
//			
////			aq.id(R.id.hot_content_name).text(hot_list.get(positon+dx).prod_name);
////			aq.id(R.id.hot_content_score).text(hot_list.get(positon+dx).score);
////			aq.id(R.id.hot_content_directors).text(hot_list.get(positon+dx).directors);
////			aq.id(R.id.hot_content_stars).text(hot_list.get(positon+dx).stars);
////			aq.id(R.id.hot_content_introduce).text(hot_list.get(positon+dx).prod_summary);
//			break;
//		case 2:
//			highlightImageView.setImageResource(resouces_lib_active[positon]);
//			noticeView.setText(positon+1 + "/" + resouces_lib_active.length);
//			contentLayout.removeAllViews();
//			yeuDanView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
//			contentLayout.startAnimation(alpha_appear);
//			contentLayout.addView(yeuDanView);
//			break;
//		case 3:
//			highlightImageView.setImageResource(resouces_lib_active[positon]);
//			noticeView.setText(positon+1 + "/" + resouces_lib_active.length);
//			break;
//		case 4:
//			highlightImageView.setImageResource(resouces_my_active[positon]);
//			noticeView.setText(positon+1 + "/" + resouces_my_active.length);
//			break;
//		default:
//			break;
//		}
//	}
	
	private void updateUser(String userId) { 
		// TODO Auto-generated method stub
		if(userId.equals(app.getUserData("userId"))){
			UserInfo currentUserInfo = new UserInfo();
			currentUserInfo.setUserId(app.getUserData("userId"));
			currentUserInfo.setUserName(app.getUserData("userName"));
			currentUserInfo.setUserAvatarUrl(app.getUserData("userAvatarUrl"));
			headers.put("user_id", currentUserInfo.getUserId());
			app.setUser(currentUserInfo);
			handler.sendEmptyMessage(MESSAGE_UPDATEUSER);
		}else{
			String url = Constant.BASE_URL + "user/view?userid=" + userId;
			AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
			cb.url(url).type(JSONObject.class).weakHandler(this, "getBandUserInfoResult");
			cb.SetHeader(app.getHeaders());
			aq.ajax(cb);
		}
	}
	
	
	public void getBandUserInfoResult(String url, JSONObject json, AjaxStatus status){

		if (json != null) {
			Log.d(TAG, json.toString());
			try {
				UserInfo currentUserInfo = new UserInfo();
				if(json.has("user_id"))
				{
					currentUserInfo.setUserId(json.getString("user_id").trim());
				}
				else
				{
					currentUserInfo.setUserId(json.getString("id").trim());
				}
				currentUserInfo.setUserName(json.getString("nickname"));
				currentUserInfo.setUserAvatarUrl(json.getString("pic_url"));
				headers.put("user_id", currentUserInfo.getUserId());
				app.setUser(currentUserInfo);
				handler.sendEmptyMessage(MESSAGE_UPDATEUSER);
//				headers.put("user_id", currentUserInfo.get);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
	}
	
	private Bitmap CreateBarCode(){
		//根据字符串生成二维码图片并显示在界面上，第二个参数为图片的大小（350*350）
		Bitmap b = null;
		String macAddress = StatisticsUtils.getMacAdd(this);
		String date = Constant.CHANNELHEADER + StatisticsUtils.MD5(macAddress);
		try {
			b = EncodingHandler.createQRCode(date, 500);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Intent service = new Intent(this,FayeService.class);  
        startService(service);
		return b;
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
	
	
	// 将片源排序
	class EComparatorIndex implements Comparator {

		@Override
		public int compare(Object first, Object second) {
			// TODO Auto-generated method stub
			int first_name = ((PLAY_URLS_INDEX) first).index;
			int second_name = ((PLAY_URLS_INDEX) second).index;
			if (first_name - second_name < 0) {
				return -1;
			} else {
				return 1;
			}
		}
	}
	
	class EComparatorIndex1 implements Comparator {

		@Override
		public int compare(Object first, Object second) {
			// TODO Auto-generated method stub
			int first_name = ((URLS_INDEX) first).index;
			int second_name = ((URLS_INDEX) second).index;
			if (first_name - second_name < 0) {
				return -1;
			} else {
				return 1;
			}
		}
	}
	
	
	class PLAY_URLS_INDEX{
		int index;
		public String source;
		public URLS[] urls;
	}
	
	class URLS_INDEX{
		int index;
		public String type;
		public String url;
	}

}
