package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.manager.RequestAQueryManager;
import com.joyplus.tv.Service.Return.ReturnProgramView;
import com.joyplus.tv.Service.Return.ReturnProgramView.DOWN_URLS.URLS;
import com.joyplus.tv.entity.CurrentPlayDetailData;
import com.joyplus.tv.entity.REQUEST_URL;
import com.joyplus.tv.entity.REQUEST_URL.URL_TYPE;
import com.joyplus.tv.ui.WaitingDialog;
import com.joyplus.tv.utils.BangDanConstant;
import com.joyplus.tv.utils.URLUtils;
import com.joyplus.tv.utils.UtilTools;
import com.joyplus.utils.DesUtils;
import com.joyplus.utils.Log;
import com.joyplus.utils.MyKeyEventKey;
import com.joyplus.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class ShowDetailHaoimsActivity extends Activity 
		implements View.OnClickListener,View.OnKeyListener, MyKeyEventKey{

	private static final String TAG = "ShowDetailHaoimsActivity";
	private static final int DIALOG_WAITING = 0;
	
	private AQuery aq;
	private App    app;
	private String prod_id;
	
	private View 			popupView;
	private View 			beforeView;
	private View 			beforeTempPop;
	private View			currentBofangViewPop;
	
	private Button 			dingBt;
	private Button 			xiaiBt;
	private Button 			bofangBt;
	private Button			gaoqingBt;
	private Button			yingpingBt;
	
	private LinearLayout 	bofangLL;
	private LinearLayout 	overTimeLL;
	private LinearLayout 	chaoqingLL;
	private	 LinearLayout	gaoqingLL;
	private LinearLayout	biaoqingLL;
	private GridView		tuijianGv;
	
	private PopupWindow 	popupWindow;
	
	private ReturnProgramView movieReturnProgramView;
	
	private static final int MESSAGE_LOAD_DATA_FAIL 		= 0;
	private static final int MESSAGE_LOAD_DATA_SUCCESS 	= 1;
	private static final int MESSAGE_LOADING_DATA 	= 2;
	
	private boolean hasChaoqing = false;
	private boolean hasGaoqing= false;
	private boolean haspuqing = false;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_LOAD_DATA_FAIL:
				app.MyToast(ShowDetailHaoimsActivity.this, getString(R.string.haoims_show_load_data_fail));
				break;
			case MESSAGE_LOAD_DATA_SUCCESS:
				loadDataSuccess();
				removeDialog(DIALOG_WAITING);
				break;
			case MESSAGE_LOADING_DATA:
				showDialog(DIALOG_WAITING);
				break;
			default:
				break;
			}
		}
		
	};
	
	private int supportDefination;
	
	private void loadDataSuccess(){
		for(int i = 0; i < movieReturnProgramView.movie.episodes[0].down_urls.length; i++){
			for (int j = 0; j < movieReturnProgramView.movie.episodes[0].down_urls[i].urls.length; j++){
				if(Constant.player_quality_index[0].equalsIgnoreCase(movieReturnProgramView.movie.episodes[0].down_urls[i].urls[j].type)){
					hasChaoqing = true;
				}else if(Constant.player_quality_index[1].equalsIgnoreCase(movieReturnProgramView.movie.episodes[0].down_urls[i].urls[j].type)){
					hasGaoqing = true;
				}else if(Constant.player_quality_index[2].equalsIgnoreCase(movieReturnProgramView.movie.episodes[0].down_urls[i].urls[j].type)){
					haspuqing = true;
				}else if(Constant.player_quality_index[3].equalsIgnoreCase(movieReturnProgramView.movie.episodes[0].down_urls[i].urls[j].type)){
					haspuqing = true;
				}else{
					haspuqing = true;
				}
			}
		}
		updatePopButton();
	}
	
	private void updatePopButton(){

		if (!hasChaoqing) {
			supportDefination -= 1;
			chaoqingLL.setVisibility(View.GONE);
			Log.i(TAG, "chaoqing_url--->");
		} else {
			gaoqingBt.setText(R.string.gaoqing_chaogaoqing);
			currentBofangViewPop = chaoqingLL;
			beforeTempPop = chaoqingLL;
		}
		
		if (!hasGaoqing) {
			supportDefination -= 1;
			gaoqingLL.setVisibility(View.GONE);
			Log.i(TAG, "gaoqing_url--->");
		} else {
			if (!hasChaoqing) {
				gaoqingBt.setText(R.string.gaoqing_gaoqing);
				currentBofangViewPop = gaoqingLL;
				beforeTempPop = gaoqingLL;
			}
		}

		if (!haspuqing) {
			supportDefination -= 1;
			biaoqingLL.setVisibility(View.GONE);
			Log.i(TAG, "puqing_url--->");
		} else {
			if (!hasChaoqing && !hasGaoqing) {
				gaoqingBt.setText(R.string.gaoqing_biaoqing);
				currentBofangViewPop = biaoqingLL;
				beforeTempPop = biaoqingLL;
			}
		}
		if (supportDefination == 0) {

			bofangLL.setEnabled(false);
		}
		
		initPopWindowData();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.show_detail_haoims_layout);
		
		prod_id = getIntent().getStringExtra("ID");
		if (TextUtils.isEmpty(prod_id)) {
			Log.e(TAG, "pram error prod_id is error value");
			finish();
			return;
		}
		supportDefination = 3;
		this.aq 	= new AQuery(this);
		this.app 	= (App) getApplication();
		
		this.showDefaultData();
		this.initLogPic();
		this.initView();
		this.mHandler.sendEmptyMessage(MESSAGE_LOADING_DATA);
		this.getServiceDate();
	}
	
	/**
	 * 显示默认数据
	 */
	private void showDefaultData() {

		Intent intent = getIntent();
		if (intent != null) {
			aq.id(R.id.image).image(intent.getStringExtra("prod_url"), false,
					true, 0, R.drawable.post_normal);
			aq.id(R.id.text_name).text(intent.getStringExtra("prod_name"));
			aq.id(R.id.text_directors).text(intent.getStringExtra("directors"));
			aq.id(R.id.text_starts).text(intent.getStringExtra("stars"));
			aq.id(R.id.text_introduce).text(intent.getStringExtra("summary"));
			aq.id(R.id.bt_xiangqingding).text(intent.getStringExtra("support_num"));
			aq.id(R.id.bt_xiangqing_xiai).text(intent.getStringExtra("favority_num"));
			if (intent.getStringExtra("definition") != null
					&& !"".equals(intent.getStringExtra("definition"))) {
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
			if (!TextUtils.isEmpty(intent.getStringExtra("score"))) {
				updateScore(intent.getStringExtra("score"));
			}
		}
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

		overTimeLL = (LinearLayout) findViewById(R.id.ll_over_time);

		bofangLL.requestFocus();

		addListener();

		initPopWindow();

		dingBt.setNextFocusUpId(R.id.bt_xiangqingding);
		xiaiBt.setNextFocusUpId(R.id.bt_xiangqing_xiai);
		yingpingBt.setNextFocusUpId(R.id.bt_xiangqing_yingping);

		beforeView = bofangLL;
		
		if(RequestAQueryManager.getInstance().getcurrentRequest_URL() == REQUEST_URL.HAOIMS){
			dingBt.setEnabled(false);
			xiaiBt.setEnabled(false);
			yingpingBt.setEnabled(false);
			overTimeLL.setVisibility(View.GONE);
		}

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
		
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setOutsideTouchable(true);
		
		currentBofangViewPop = chaoqingLL;
		beforeTempPop = chaoqingLL;
	}
	
	private void backToNormalPopView() {

		LinearLayout ll = (LinearLayout) beforeTempPop;
		Button button1 = (Button) ll.getChildAt(0);
		Button button2 = (Button) ll.getChildAt(1);
		button1.setVisibility(View.INVISIBLE);
		button2.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
	}

	private void setLinearLayoutVisible(View v) {

		LinearLayout ll = (LinearLayout) v;
		Button button1 = (Button) ll.getChildAt(0);
		Button button2 = (Button) ll.getChildAt(1);
		button1.setVisibility(View.VISIBLE);
		button2.setCompoundDrawablesWithIntrinsicBounds(0, 0,
				R.drawable.icon_play, 0);
	}

	private void initPopWindowData() {
		setLinearLayoutVisible(currentBofangViewPop);

		OnClickListener gaoqingListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()) {
				case R.id.ll_gaoqing_chaoqing:
					gaoqingBt.setText(R.string.gaoqing_chaogaoqing);
					currentBofangViewPop = v;
					break;
				case R.id.ll_gaoqing_gaoqing:
					gaoqingBt.setText(R.string.gaoqing_gaoqing);
					currentBofangViewPop = v;
					break;
				case R.id.ll_gaoqing_biaoqing:
					gaoqingBt.setText(R.string.gaoqing_biaoqing);
					currentBofangViewPop = v;
					break;
				default:
					break;
				}
				play();
				backToNormalPopView();
				setLinearLayoutVisible(v);

				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				}
				
				beforeTempPop = v;
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
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					if (popupWindow != null) {
						popupWindow.dismiss();
					}
					return true;
				} else if (action == KeyEvent.ACTION_UP) {

					switch (v.getId()) {
					case R.id.ll_gaoqing_chaoqing:
						if (keyCode == KEY_UP) {
							backToNormalPopView();
							setLinearLayoutVisible(v);
						}
						break;
					case R.id.ll_gaoqing_gaoqing:
						if (keyCode == KEY_UP || keyCode == KEY_DOWN) {
							backToNormalPopView();
							setLinearLayoutVisible(v);
						}
						break;
					case R.id.ll_gaoqing_biaoqing:
						if (keyCode == KEY_DOWN) {
							backToNormalPopView();
							setLinearLayoutVisible(v);
						}
						break;
					default:
						break;
					}
					beforeTempPop = v;
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						if (popupWindow != null && popupWindow.isShowing()) {
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
	
	private void addListener() {

		dingBt.setOnKeyListener(this);
		xiaiBt.setOnKeyListener(this);
		bofangLL.setOnKeyListener(this);
		tuijianGv.setOnKeyListener(this);

		dingBt.setOnClickListener(this);
		xiaiBt.setOnClickListener(this);
		bofangLL.setOnClickListener(this);
		yingpingBt.setOnClickListener(this);

		bofangLL.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				showPopUpWindow(v);
				return false;
			}
		});
	}
	
	private void showPopUpWindow(View v){
		if (supportDefination == 3) {

			int width = v.getWidth();
			int height = v.getHeight() * 3;
			int locationY = v.getHeight() * 2;
			int[] location = new int[2];
			v.getLocationOnScreen(location);
			popupWindow.setFocusable(true);
			popupWindow.setWidth((int) (width + Utils.getStandardValue(getApplicationContext(),10)));
			popupWindow.setHeight((int) (height + Utils.getStandardValue(getApplicationContext(),40)));
			popupWindow.showAtLocation(v, Gravity.NO_GRAVITY,
					(int)(location[0] - Utils.getStandardValue(getApplicationContext(),6)), (int)(location[1] - locationY
							- Utils.getStandardValue(getApplicationContext(),40)));
		} else if (supportDefination == 2) {

			int width = v.getWidth();
			int height = v.getHeight() * 2;
			int locationY = v.getHeight() * 1;
			int[] location = new int[2];
			v.getLocationOnScreen(location);
			popupWindow.setFocusable(true);
			popupWindow.setWidth((int) (width + Utils.getStandardValue(getApplicationContext(),10)));
			popupWindow.setHeight((int) (height + Utils.getStandardValue(getApplicationContext(),40)));
			popupWindow.showAtLocation(v, Gravity.NO_GRAVITY,
					(int)(location[0] - Utils.getStandardValue(getApplicationContext(),6)), (int)(location[1] - locationY
							- Utils.getStandardValue(getApplicationContext(),40)));
		}
	}
	
	private void initLogPic(){
		UtilTools.setLogoPic(getApplicationContext(), aq, (ImageView)findViewById(R.id.iv_head_logo));
	}
	
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		return false;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_xiangqingding:
			break;
		case R.id.bt_xiangqing_xiai:
			break;
		case R.id.ll_xiangqing_bofang_gaoqing:
			play();
			break;
		case R.id.gv_xiangqing_tuijian:
			break;
		case R.id.bt_xiangqing_yingping:
			break;
		default:
			break;
		}
	}
	
	private void play(){
		
		if(movieReturnProgramView == null || movieReturnProgramView.movie == null) return;
		if("true".equals(movieReturnProgramView.movie.fee) && !VIPLoginActivity.isLogin(this)
				&& "t035001".equals(UtilTools.getUmengChannel(this))){
			Intent loginIntent = new Intent(this, VIPLoginActivity.class);
			loginIntent.putExtra(VIPLoginActivity.START_FROM, VIPLoginActivity.START_FROM_DETAIL);
			startActivityForResult(loginIntent, VIPLoginActivity.RESULTCODE_FOR_DETAIL);
			return;
		}

		Intent intent = new Intent(this, VideoPlayerJPActivity.class);
		CurrentPlayDetailData playData = new CurrentPlayDetailData();

		playData.prod_id = movieReturnProgramView.movie.id;
		playData.prod_name = movieReturnProgramView.movie.name;
//		playData.prod_favority = isXiai;
		if (getResources().getString(R.string.gaoqing_gaoqing).equals(gaoqingBt.getText())) {

			playData.prod_qua = BangDanConstant.GAOQING;
		} else if (getResources().getString(R.string.gaoqing_chaogaoqing).equals(gaoqingBt.getText())) {

			playData.prod_qua = BangDanConstant.CHAOQING;
		} else if (getResources().getString(R.string.gaoqing_biaoqing).equals(gaoqingBt.getText())) {

			playData.prod_qua = BangDanConstant.CHANGXIAN;
		}

		Log.i(TAG, "playData.prod_qua--->" + playData.prod_qua + " text--->" + gaoqingBt.getText());
		playData.prod_type = 1;//电影

		app.setmCurrentPlayDetailData(playData);
		app.set_ReturnProgramView(movieReturnProgramView);

		startActivity(intent);
	}
	
	protected void onDestroy() {
		if (this.aq != null)
			this.aq.dismiss();
		super.onDestroy();
	}

	protected void onResume() {
		super.onResume();

		MobclickAgent.onResume(this);

		if (app.getUserInfo() != null) {
			aq.id(R.id.iv_head_user_icon).image(
					app.getUserInfo().getUserAvatarUrl(), false, true, 0,
					R.drawable.avatar_defult);
			updateUserName();
		}
	}
	
	private void updateUserName(){
		if(VIPLoginActivity.isLogin(this))
			aq.id(R.id.tv_head_user_name).text(UtilTools.getVIP_NickName(this));
		else
			aq.id(R.id.tv_head_user_name).text(app.getUserInfo().getUserName());
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
	
	private void updateScore(String score) {
		aq.id(R.id.textView_score).text(score);
		float f = Float.valueOf(score);
		// int i = Math.round(f);
		int i = (int) Math.ceil(f);
		// int i = (f%1>=0.5)?(int)(f/1):(int)(f/1+1);
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
	
	public void initData(String url, JSONObject json, AjaxStatus status) {
		if (status.getCode() == AjaxStatus.NETWORK_ERROR ||
				json == null || json.toString().equals("")) {
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}

		Log.d(TAG, "initData = " + json.toString());
		ObjectMapper mapper = new ObjectMapper();
		try {
			movieReturnProgramView = mapper.readValue(json.toString(),
					ReturnProgramView.class);
			if(json.has("tv")){
				Utils.convertToMovieData(movieReturnProgramView, RETURN_VIEW_TYPE.TV);
			}else if(json.has("show")){
				Utils.convertToMovieData(movieReturnProgramView, RETURN_VIEW_TYPE.SHOW);
			}else {//当movie处理
				Utils.convertToMovieData(movieReturnProgramView, RETURN_VIEW_TYPE.MOVIE);
			}
			if(movieReturnProgramView == null || movieReturnProgramView.movie == null) return;
			pic_url =  movieReturnProgramView.movie.ipad_poster;
			if(TextUtils.isEmpty(pic_url) || pic_url.equals(UtilTools.EMPTY)){
				pic_url = movieReturnProgramView.movie.poster;
			}
			updateView();

			if(movieReturnProgramView.movie.episodes != null 
					&& movieReturnProgramView.movie.episodes.length > 0){
				if(movieReturnProgramView.movie.episodes[0].down_urls != null
						&& movieReturnProgramView.movie.episodes[0].down_urls.length > 0
						&& movieReturnProgramView.movie.episodes[0].down_urls[0]!= null
						&& movieReturnProgramView.movie.episodes[0].down_urls[0].urls != null
						&& movieReturnProgramView.movie.episodes[0].down_urls[0].urls.length > 0){
					List<URLS> tempList = new ArrayList<URLS>();
					for(int i=0;i<movieReturnProgramView.movie.episodes[0].down_urls[0].urls.length;i++){
						if(movieReturnProgramView.movie.episodes[0].down_urls[0].urls[i] != null){
							URLS tempURLS = movieReturnProgramView.movie.episodes[0].down_urls[0].urls[i];
							String playUrl = tempURLS.url;
							if(!TextUtils.isEmpty(playUrl)){
								Log.i(TAG, "haoims data:" + tempURLS.toString());
								tempList.add(tempURLS);
							}
						}
					}
					if(tempList.size() > 0){
						movieReturnProgramView.movie.episodes[0].down_urls[0].urls = tempList.toArray(new URLS[tempList.size()]);
						mHandler.sendEmptyMessage(MESSAGE_LOAD_DATA_SUCCESS);
						return;
					}else{
						if (movieReturnProgramView.movie.episodes[0].video_urls != null
								&& movieReturnProgramView.movie.episodes[0].video_urls.length > 0
								&& movieReturnProgramView.movie.episodes[0].video_urls[0] != null) {
							String sourceUrl = movieReturnProgramView.movie.episodes[0].video_urls[0].url;
							Log.i(TAG, "sourceUrl--->" + sourceUrl);
							if (!TextUtils.isEmpty(sourceUrl)) {
								getDownloadUrlFromService(sourceUrl);
								return;
							}
						}
					}
				}
			}

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mHandler.sendEmptyMessage(MESSAGE_LOAD_DATA_FAIL);
	}
	
	public void initSourceUrlParserServiceData(String url, JSONObject json, AjaxStatus status) {
		if (status.getCode() == AjaxStatus.NETWORK_ERROR ||
				json == null || json.toString().equals("")) {
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}

		Log.d(TAG, "initSourceUrlParserServiceData = " + json.toString());
		try {
			if(json.has("error") && !json.getBoolean("error")){
				String downloadUrl = json.getString("downurl");
				if(!TextUtils.isEmpty(downloadUrl)){
					String data = DesUtils.decode(Constant.DES_KEY, downloadUrl);
					String[] urls = data.split("\\{mType\\}");
					List<URLS> tempList = new ArrayList<URLS>();
					for(String str : urls){
						URLS entity = new URLS();
						String[] p = str.split("\\{m\\}");
						if(p.length<2){
							continue;
						}
						entity.type = p[0];
						if("hd".equals(p[0])){
							entity.type="mp4";
						}else if("hd2".equals(p[0])){
							entity.type="hd2";
						}else {
							entity.type="flv";
						}
						entity.url = p[1];
						Log.i(TAG, "initSourceUrlParserServiceData-->" + entity.toString());
						tempList.add(entity);
					}
					if(tempList.size() > 0){
						movieReturnProgramView.movie.episodes[0].down_urls[0].urls = tempList.toArray(new URLS[tempList.size()]);
						mHandler.sendEmptyMessage(MESSAGE_LOAD_DATA_SUCCESS);
						return;
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mHandler.sendEmptyMessage(MESSAGE_LOAD_DATA_FAIL);
	}
	
	private String pic_url;
	
	private void updateView() {
		aq.id(R.id.image)
		.image(pic_url, false, true, 0, R.drawable.post_normal);
		if(!TextUtils.isEmpty(movieReturnProgramView.movie.score))
			updateScore(movieReturnProgramView.movie.score);
	}
	
	public enum RETURN_VIEW_TYPE{
		MOVIE	("movie"),//movie
		TV		("tv"),//tv anime
		SHOW	("show");//show
		
		private String name;
		private RETURN_VIEW_TYPE(String _name){
			this.name = _name;
		}
		
		public String getName(){
			return this.name;
		}
	}
	
	private void getDownloadUrlFromService(String sourceUrl) {
		
		REQUEST_URL currentURL = RequestAQueryManager.getInstance().getcurrentRequest_URL();
		String md5 = null;
		if(Constant.TestEnv){
			md5 = MobclickAgent.getConfigParams(this, "TEST_P2P_TV_MD5");
		}else {
			md5 = MobclickAgent.getConfigParams(this, "P2P_TV_MD5");
		}
		Map<String,String> headers = new HashMap<String, String>();
		String request =URLUtils.getXunLeiUrlURL(Constant.P2P_PARSE_URL_URL,sourceUrl,md5);
		if(currentURL == REQUEST_URL.JOYPLUS_URL){
			headers = app.getHeaders();
		}else if(currentURL == REQUEST_URL.HAOIMS){
		}else {
		}
		RequestAQueryManager.getInstance().getRequest(this, request, headers, aq, URL_TYPE.SOURCE_URL_PARSER.getInterfaceName());
	}
	
	private void getRecommendMovieFormService() {
		REQUEST_URL currentURL = RequestAQueryManager.getInstance().getcurrentRequest_URL();
		String url = currentURL.getURL4URL_Type(URL_TYPE.RECOMMEND_MOVIE_URL);
		Map<String,String> headers = new HashMap<String, String>();
		if(currentURL == REQUEST_URL.JOYPLUS_URL){
			headers = app.getHeaders();
			url = url + "?prod_id="+ prod_id;
		}else if(currentURL == REQUEST_URL.HAOIMS){
		}else {
		}
		RequestAQueryManager.getInstance().getRequest(this, url, headers, aq, URL_TYPE.RECOMMEND_MOVIE_URL.getInterfaceName());
	}
	
	protected void getYingpingData() {
		REQUEST_URL currentURL = RequestAQueryManager.getInstance().getcurrentRequest_URL();
		String url = currentURL.getURL4URL_Type(URL_TYPE.YINGPING_URL);
		Map<String,String> headers = new HashMap<String, String>();
		if(currentURL == REQUEST_URL.JOYPLUS_URL){
			headers = app.getHeaders();
			url = URLUtils.getYingPin_1_URL(prod_id);
		}else if(currentURL == REQUEST_URL.HAOIMS){
		}else {
		}
		RequestAQueryManager.getInstance().getRequest(this, url, headers, aq, URL_TYPE.YINGPING_URL.getInterfaceName());
	}
	
	private void cancelshoucang() {
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		REQUEST_URL currentURL = RequestAQueryManager.getInstance().getcurrentRequest_URL();
		String url = currentURL.getURL4URL_Type(URL_TYPE.CANCEL_SHOUCANG_URL);
		if(currentURL == REQUEST_URL.JOYPLUS_URL){
			params.put("prod_id" , prod_id);
			headers = app.getHeaders();
		}else if(currentURL == REQUEST_URL.HAOIMS){
		}else {
		}
		RequestAQueryManager.getInstance().request(this, url, params, headers, aq, URL_TYPE.CANCEL_SHOUCANG_URL.getInterfaceName());
	}
	
	private void shoucang() {
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		REQUEST_URL currentURL = RequestAQueryManager.getInstance().getcurrentRequest_URL();
		String url = currentURL.getURL4URL_Type(URL_TYPE.SHOUCANG_URL);
		if(currentURL == REQUEST_URL.JOYPLUS_URL){
			params.put("prod_id" , prod_id);
			headers = app.getHeaders();
		}else if(currentURL == REQUEST_URL.HAOIMS){
		}else {
		}
		RequestAQueryManager.getInstance().request(this, url, params, headers, aq, URL_TYPE.SHOUCANG_URL.getInterfaceName());
	}
	
	private void getIsShoucangData() {
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		REQUEST_URL currentURL = RequestAQueryManager.getInstance().getcurrentRequest_URL();
		if(currentURL == REQUEST_URL.JOYPLUS_URL){
			params.put("prod_id" , prod_id);
			headers = app.getHeaders();
		}else if(currentURL == REQUEST_URL.HAOIMS){
		}else {
		}
		String url = currentURL.getURL4URL_Type(URL_TYPE.IS_SHOUCANG_URL);
		RequestAQueryManager.getInstance().request(this, url, params, headers, aq, URL_TYPE.IS_SHOUCANG_URL.getInterfaceName());
	}
	
	private void dingService() {
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		REQUEST_URL currentURL = RequestAQueryManager.getInstance().getcurrentRequest_URL();
		String url = currentURL.getURL4URL_Type(URL_TYPE.DING_URL);
		if(currentURL == REQUEST_URL.JOYPLUS_URL){
			params.put("prod_id" , prod_id);
			headers = app.getHeaders();
		}else if(currentURL == REQUEST_URL.HAOIMS){
		}else {
		}

		RequestAQueryManager.getInstance().request(this, url, params, headers, aq, URL_TYPE.DING_URL.getInterfaceName());
	}
	
	private void getServiceDate() {
		REQUEST_URL currentURL = RequestAQueryManager.getInstance().getcurrentRequest_URL();
		String url = currentURL.getURL4URL_Type(URL_TYPE.DETAIL_SERVICE_URL);
		Map<String,String> headers = new HashMap<String, String>();
		if(currentURL == REQUEST_URL.JOYPLUS_URL){
			headers = app.getHeaders();
			url = url + "?prod_id=" + prod_id;
		}else if(currentURL == REQUEST_URL.HAOIMS){
			url = url + "?action=view&app_key=null&prod_id=" + prod_id;
		}else {
		}
		Log.i(TAG, "dongman url--->" + url);
		RequestAQueryManager.getInstance().getRequest(this, url, headers, aq, URL_TYPE.DETAIL_SERVICE_URL.getInterfaceName());
	}
}
