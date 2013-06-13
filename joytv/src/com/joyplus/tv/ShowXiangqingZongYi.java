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
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.tv.Adapters.CurrentPlayData;
import com.joyplus.tv.Service.Return.ReturnProgramView;
import com.joyplus.tv.Service.Return.ReturnProgramView.DOWN_URLS;
import com.joyplus.tv.Video.VideoPlayerActivity;
import com.joyplus.tv.entity.HotItemInfo;
import com.joyplus.tv.entity.URLS_INDEX;
import com.joyplus.tv.ui.WaitingDialog;
import com.joyplus.tv.utils.BangDanKey;
import com.joyplus.tv.utils.DBUtils;
import com.joyplus.tv.utils.DefinationComparatorIndex;
import com.joyplus.tv.utils.ItemStateUtils;
import com.joyplus.tv.utils.JieMianConstant;
import com.joyplus.tv.utils.Log;
import com.joyplus.tv.utils.MyKeyEventKey;
import com.joyplus.tv.utils.SouceComparatorIndex1;
import com.joyplus.tv.utils.UtilTools;
import com.umeng.analytics.MobclickAgent;

public class ShowXiangqingZongYi extends Activity implements View.OnClickListener,
		View.OnKeyListener, MyKeyEventKey {

	private static final String TAG = "ShowXiangqingZongYi";
	private static final int DIALOG_WAITING = 0;
	private LinearLayout bofangLL;
	private String pic_url;
	private Button dingBt,xiaiBt;
	private Button bofangBt,gaoqingBt;

	private View beforeView;

	private PopupWindow popupWindow;
	private View popupView;

	private boolean isDing = false, isXiai= false;
	private boolean isPopupWindowShow;

	private View beforeTempPop, currentBofangViewPop;
	private LinearLayout chaoqingLL, gaoqingLL, biaoqingLL;
	
	private LinearLayout layout;
	private TableLayout table;
	private static final int COUNT = 20;
	
	private String prod_id;
	
	private AQuery aq;
	private App app;
	private ReturnProgramView date;
	
	private static int favNum = 0;
	
	private boolean isOver = false;//默认倒序 是否倒序或者正序的判断
	private int num = 0;//影片的总集数
	private int totle_pagecount;//20一页，页数
	
	private Button seletedTitleButton;//选中页数的Button
	private Button seletedIndexButton;//选中当前页数下 当前想要播放集数的Button
	private int seletedButtonIndex=0;//选中当前页数下，当前想要播放集数的Button的索引 1开始
	private int selectedIndex;//选中页数的索引 1开始
	
	private int historyPlayIndex4DB = -1;//当前数据库中播放的集数
	private String historyPlayProdSubName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.show_xiangqing_zongyi);
		Intent intent = getIntent();
		prod_id = intent.getStringExtra("ID");
		if(prod_id == null||"".equals(prod_id)){
			Log.e(TAG, "pram error");
			finish();
		}
		aq = new AQuery(this);
		app = (App) getApplication();
		showDefultDate();
		initView();
		showDialog(DIALOG_WAITING);
		
		//从DB文件中获取历史播放集数
		HotItemInfo info = DBUtils.
				getHotItemInfo4DB_History(getApplicationContext(),
						UtilTools.getCurrentUserId(getApplicationContext()), prod_id);
		historyPlayProdSubName = info.prod_subname;
		Log.i(TAG, "onCreate--->historyPlayIndex4DB:" + historyPlayIndex4DB);
		
		getIsShoucangData();
		getServiceDate();
	}
	
	private void setTitleButtonEnable(int index , int tempStartTag,int tempEndTag,Button button) {
		
		int max = 0;
		int min = 0;
		
		if(historyPlayIndex4DB != -1) {//能够获取历史记录
			
			if(tempStartTag < tempEndTag) {//正序
				
				max = tempEndTag;
				min = tempStartTag;
			} else {//倒序
				
				max = tempStartTag;
				min = tempEndTag;
			}
			
			//如果历史播放记录集数属于这个区间
			if(historyPlayIndex4DB <= max && historyPlayIndex4DB >= min) {
				
				seletedTitleButton = button;
				seletedTitleButton.setEnabled(false);
				seletedTitleButton.requestFocus();
				selectedIndex = index;
			}
		} else {
			
			selectedIndex = 1;//默认选择
		}
	}
	
	private boolean isShowHeadTable = false;

	private void initButton() {
		// TODO Auto-generated method stub
		
		int indexButton = -1;
		
		totle_pagecount = (num%COUNT ==0)? num/COUNT:num/COUNT+1;
		if(totle_pagecount<2){
			selectedIndex = 1;
			isShowHeadTable = false;
			initTableView(num);
			aq.id(R.id.arrow_left).invisible();
			aq.id(R.id.arrow_right).invisible();
			aq.id(R.id.scrollview).gone();
			return;
		}
		
		isShowHeadTable = true;
		
		for(int i=0; i<totle_pagecount; i++){
			Button b = new Button(this);
//			b.setWidth(table.getWidth()/5);
//			b.setHeight(layout.getHeight());
			b.setLayoutParams(new LayoutParams((table.getWidth()-80)/5,35));
			if(isOver){
				if((i+1)*COUNT>num){
					b.setText((i*COUNT+1) +"-"+num);
					
					int tempStartTag = i*COUNT+1;
					int tempEndTag = num;
					
					setTitleButtonEnable(i+1,tempStartTag, tempEndTag, b);
				}else{
					b.setText((i*COUNT+1) +"-"+(i+1)*COUNT);
					
					int tempStartTag = i*COUNT+1;
					int tempEndTag = (i+1)*COUNT;
					
					setTitleButtonEnable(i+1,tempStartTag, tempEndTag, b);
				}
			}else{
				if(num-(i+1)*COUNT<0){
					b.setText((num-i*COUNT) + "-1");
					
					setTitleButtonEnable(i+1,(num-i*COUNT), 1, b);
				}else{
					b.setText((num-i*COUNT) + "-" + (num-(i+1)*COUNT+1));
					
					setTitleButtonEnable(i+1,(num-i*COUNT), num-(i+1)*COUNT+1, b);
				}
				
			}
			if(i==0 && historyPlayIndex4DB == -1){//如果获取不到历史播放记录集数，就使用默认值
				seletedTitleButton = b;
				seletedTitleButton.setEnabled(false);
			}
			b.setBackgroundResource(R.drawable.bg_button_tv_title_selector);
			b.setTextColor(getResources().getColorStateList(R.color.tv_title_btn_text_color_selector));
//			b.setCompoundDrawablesWithIntrinsicBounds(getResources()
//					.getDrawable(R.drawable.bg_right_play_icon_selector), null, null, null);
			b.setId((i+1)*10000);
			b.setOnClickListener(this);
			layout.addView(b);
			if(i!=totle_pagecount-1){
				TextView t = new TextView(this);
				t.setLayoutParams(new LayoutParams(20,35));
				layout.addView(t);
			}
			
			
			if(i == 0) {
				
//				b.setNextFocusLeftId(b.getId());
			} else if(i == 4) {
				
//				b.setNextFocusRightId(b.getId());
			}
			
			if(indexButton >= 0 && indexButton <= 2) {
				
				b.setNextFocusUpId(bofangLL.getId());
			}
			
			indexButton ++;
			
		}
		
//		selectedIndex = 1;
//		if(num>COUNT){
//			initTableView(COUNT);
//		}else{
//			initTableView(num);
//		}
		
		if(num>COUNT*selectedIndex){
			initTableView(COUNT);
		}else{
			initTableView(num-COUNT*(selectedIndex-1));
		}
		
		bofangLL.requestFocus();
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


//		yingpingBt = (Button) findViewById(R.id.bt_xiangqing_yingping);
		
		layout = (LinearLayout) findViewById(R.id.layout);
		table  = (TableLayout) findViewById(R.id.table);
		
		bofangLL.requestFocus();

		addListener();

		initPopWindow();
		
		dingBt.setNextFocusUpId(R.id.bt_xiangqingding);
		xiaiBt.setNextFocusUpId(R.id.bt_xiangqing_xiai);

		beforeView = dingBt;

	}

	private void addListener() {

		dingBt.setOnKeyListener(this);
		xiaiBt.setOnKeyListener(this);
		bofangLL.setOnKeyListener(this);

		dingBt.setOnClickListener(this);
		xiaiBt.setOnClickListener(this);
		bofangLL.setOnClickListener(this);
//		yingpingBt.setOnClickListener(this);
		
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
//
//			Intent intent = new Intent(this, VideoPlayerActivity.class);
//			intent.putExtra("prod_url", str2);
//			intent.putExtra("title", str1);
//			startActivity(intent);
			Log.i(TAG, "ll_xiangqing_bofang_gaoqing--->" +seletedButtonIndex );
			if(seletedButtonIndex<=0){
				seletedButtonIndex = 1;
				Button b = (Button) table.findViewById(1);
				seletedIndexButton = b;
				if(b!=null){
					b.setBackgroundResource(R.drawable.bg_button_tv_selector_1);
					b.setTextColor(getResources().getColorStateList(R.color.tv_btn_text_color_selector_1));
					b.setPadding(8, 0, 0, 0);
				}
				
				//从DB文件中获取历史播放集数
				HotItemInfo info = DBUtils.
						getHotItemInfo4DB_History(getApplicationContext(),
								UtilTools.getCurrentUserId(getApplicationContext()), prod_id);
				if(info != null) {
					
//					int index = playData.CurrentIndex;
					String prod_subName = info.prod_subname;
					
					if(prod_subName != null && !prod_subName.equals("")) {
						
						if(date != null) {
							
							if(date.show.episodes != null) {
								
								boolean isPiPei = false;
								Log.i(TAG, "date.show.episodes.length--->" + date.show.episodes.length);
								for(int i=0;i< date.show.episodes.length;i++) {
									
									if(prod_subName.equals(date.show.episodes[i].name) ) {
										
										Log.i(TAG, "date.show.episodes.length--->" + date.show.episodes.length + 
												" i--->" + i);
										isPiPei = true;
										play(i);
									}
								}
								
								if(!isPiPei) {
									
									play(0);
								}
							} else {
								
								play(0);
							}
						}
					}else {
						
						play(0);
					}

				} else {
					
					play(0);
				}
				
			}else{
				play(seletedButtonIndex-1);
			}
			break;
//		case R.id.bt_xiangqing_yingping:
//			Intent yingpingIntent = new Intent(this, DetailComment.class);
////			yingpingIntent.putExtra("ID", prod_id);
//			int yingpingSize = date.comments.length;
//			
//			if(yingpingSize >0) {
//				
//				Bundle bundle = new Bundle();
//				bundle.putString("prod_id", prod_id);
//				bundle.putString("prod_name", date.tv.name);
//				bundle.putString("prod_dou", date.tv.score);
//				bundle.putString("prod_url", date.tv.poster);
//				yingpingIntent.putExtras(bundle);
//				startActivity(yingpingIntent);
//			} else {
//				
//				Toast.makeText(this, getString(R.string.xiangqing_yingping_no),
//						Toast.LENGTH_SHORT).show();
//			}
//			break;
		default:
			
			if(v.getId()>=10000){
				selectedIndex = v.getId()/10000;
				if(num>COUNT*selectedIndex){
					initTableView(COUNT);
				}else{
					initTableView(num-COUNT*(selectedIndex-1));
				}
				seletedTitleButton.setEnabled(true);
				seletedTitleButton = (Button) v;
				seletedTitleButton.setEnabled(false);
			}else{
//				Toast.makeText(this, "click btn = " + v.getId(), 100).show();
				if(seletedIndexButton == null){
					seletedIndexButton = (Button) v;
					seletedIndexButton.setBackgroundResource(R.drawable.bg_button_tv_selector_1);
					seletedIndexButton.setTextColor(getResources().getColorStateList(R.color.tv_btn_text_color_selector_1));
//					seletedIndexButton.setEnabled(false);
				}else{
//					seletedIndexButton.setEnabled(true);
					seletedIndexButton.setBackgroundResource(R.drawable.bg_button_tv_selector);
					seletedIndexButton.setTextColor(getResources().getColorStateList(R.color.tv_btn_text_color_selector));
					seletedIndexButton = (Button) v;
					seletedIndexButton.setBackgroundResource(R.drawable.bg_button_tv_selector_1);
					seletedIndexButton.setTextColor(getResources().getColorStateList(R.color.tv_btn_text_color_selector_1));
//					seletedIndexButton.setEnabled(false);
				}
				seletedButtonIndex = v.getId();
				play(v.getId()-1);
//				scrollView.smoothScrollBy(20, 0);
			}
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

					if (keyCode == KEY_UP && beforeView.getId() == v.getId()
							&& !isPopupWindowShow) {
						initPopWindowData();
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
				switch (id) {
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

				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				}
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
				}else if (action == KeyEvent.ACTION_UP) {

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
				}
				return false;
			}
		};
		
		chaoqingLL.setOnKeyListener(gaoqingKeyListener);
		gaoqingLL.setOnKeyListener(gaoqingKeyListener);
		biaoqingLL.setOnKeyListener(gaoqingKeyListener);
	}
	
	
	private void initTableView(int count){
		
		int indexButton = -1;
		boolean isSelected = false;
		
		table.removeAllViews();
		int col = (count%5 ==0)? count/5:count/5+1;
		for(int j=0; j<col; j++){
			TableRow row = new TableRow(this);
//			row.setId(6-flag);
			for(int i =0; i<5 ; i++){
				TextView btn = new Button(this);
				btn.setWidth((table.getWidth()-80)/5);
				btn.setTextSize(18);
				btn.setHeight(25);
//				isOver = true;//综艺总为没有完结
//				if(isOver){
//					btn.setText("" + (j*5+i+1 + (selectedIndex-1)*COUNT));
				int tempCount = (j*5+i+1 + (selectedIndex-1)*COUNT) - 1;
//				Log.i(TAG, "Count:" + count2);
				if(tempCount <= date.show.episodes.length - 1) {
					
					btn.setText(date.show.episodes[((j*5+i+1 + (selectedIndex-1)*COUNT)) - 1].name + "");
				}
				btn.setId(j*5+i+1 + (selectedIndex-1)*COUNT);
//				}
//				else{
//					btn.setText("" + (num-((j*5+i)+ (selectedIndex-1)*COUNT)));
////					btn.setText(date.show.episodes[(num-((j*5+i)+ (selectedIndex-1)*COUNT)] + "");
//					btn.setId(num-((j*5+i)+ (selectedIndex-1)*COUNT));
//				}
				btn.setSingleLine(true);
				btn.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
				btn.setEllipsize(TruncateAt.MARQUEE);
				btn.setMarqueeRepeatLimit(-1);
				btn.setOnClickListener(this);
				btn.setOnKeyListener(this);
				if(seletedButtonIndex==btn.getId()){
//					btn.setEnabled(false);
					seletedIndexButton = (Button) btn;
					btn.setBackgroundResource(R.drawable.bg_button_tv_selector_1);
					btn.setTextColor(getResources().getColorStateList(R.color.tv_btn_text_color_selector_1));
					isSelected = true;
				}else{
					btn.setBackgroundResource(R.drawable.bg_button_tv_selector);
					btn.setTextColor(getResources().getColorStateList(R.color.tv_btn_text_color_selector)) ;
				}
				if(j*5+i+1>count){
					btn.setVisibility(View.INVISIBLE);
				}
				TextView t = new TextView(this);
				t.setWidth(20);
				row.addView(btn);
				if(i!=4){
					row.addView(t);
				}
				
				if(i == 0) {
					
					btn.setNextFocusLeftId(btn.getId());
				} else if(i == 4) {
					
					btn.setNextFocusRightId(btn.getId());
				}
				
				if(!isShowHeadTable) {
					
					if(indexButton >=0 && indexButton <= 2) {
						
						btn.setNextFocusUpId(bofangLL.getId());
					}
				}
				
				indexButton ++;
			}
			row.setLayoutParams(new LayoutParams(table.getWidth(),35));
			row.setPadding(0, 5, 0, 5);
			table.addView(row);
			
			if(!isSelected){//如果当前页没有一个button被选中
				
				if(seletedIndexButton == null) { //如果当前页没有一个button被选中
					
					if(historyPlayIndex4DB != -1) {
						
						Button button = (Button) findViewById(num-historyPlayIndex4DB + 1);
						if(button != null) {
							
							button.setBackgroundResource(R.drawable.bg_button_tv_selector_1);
							button.setTextColor(getResources().getColorStateList(R.color.tv_btn_text_color_selector_1));
//							button.setPadding(8, 0, 0, 0);
							
							seletedIndexButton = button;
							seletedButtonIndex = num-historyPlayIndex4DB + 1;
						}
					}

				}
			}
			
		}
	}
	
	
	private void getServiceDate(){
		String url = Constant.BASE_URL + "program/view" +"?prod_id=" + prod_id;
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, "initDate");
		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}
	
	public void initDate(String url, JSONObject json, AjaxStatus status){
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
			date = null;
			date  = mapper.readValue(json.toString(), ReturnProgramView.class);
			if(date == null){
				Log.e(TAG, "tv date error---->date == null");
				return;
			}
			
			if(date.show == null) {
				
				return;
			}
//			if("".equals(date.tv.cur_episode)||"0".equals(date.tv.cur_episode)
//					||date.tv.cur_episode.equals(date.tv.max_episode)){
//				isOver = true;
//				num = Integer.valueOf(date.tv.max_episode);
//			}else if(Integer.valueOf(date.tv.cur_episode)>Integer.valueOf(date.tv.max_episode)){
//				isOver = true;
//				num = Integer.valueOf(date.tv.max_episode);
//			}else{
//				isOver = false;
//				num = Integer.valueOf(date.tv.cur_episode);
//			}
			String bigPicUrl = date.show.ipad_poster;
			if(bigPicUrl == null || bigPicUrl.equals("")
					||bigPicUrl.equals(UtilTools.EMPTY)) {
				
				bigPicUrl = date.show.poster;
			}
			pic_url = bigPicUrl;
			removeDialog(DIALOG_WAITING);
			if(date.show.episodes.length <= 0) {
				
				return;
			}
			
			num = Integer.valueOf(date.show.episodes.length);
			
			Log.i(TAG, "historyPlayProdSubName--->" + historyPlayProdSubName);
			for(int i=0;i<date.show.episodes.length;i++) {
				
				if(historyPlayProdSubName != null && !historyPlayProdSubName.equals("")) {
					
					if(historyPlayProdSubName.equals(date.show.episodes[i].name)) {
						
						historyPlayIndex4DB = date.show.episodes.length - i;
						Log.i(TAG, "onCreate--->historyPlayIndex4DB:" + historyPlayIndex4DB);
					}
				}
			}
			
			updateView();
			
//			showHistorySelect();
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
	
	private void updateView(){
		
		String strNum = date.show.favority_num;
		
		if(strNum != null && !strNum.equals("")){
			
			favNum = Integer.valueOf(strNum);
		}
		
		initButton();
		aq.id(R.id.image).image(pic_url, false, true,0, R.drawable.post_normal);
		aq.id(R.id.image).image(date.show.poster, false, true,0, R.drawable.post_normal);
		aq.id(R.id.text_name).text(date.show.name);
		aq.id(R.id.text_directors).text(date.show.stars);
		aq.id(R.id.text_starts).text(date.show.directors);
		aq.id(R.id.text_introduce).text(date.show.summary);
		aq.id(R.id.bt_xiangqingding).text(date.show.support_num);
		aq.id(R.id.bt_xiangqing_xiai).text(date.show.favority_num);
		int definition = Integer.valueOf((date.show.definition));
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
		updateScore(date.show.score);
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
	
//	private synchronized void  showHistorySelect() {
//		
//		HotItemInfo info = StatisticsUtils.getHotItemInfo4DB_History(getApplicationContext(),
//				app.getUserInfo().getUserId(), prod_id);
//		
//		if(info != null){
//			
//			String type = info.prod_type;
//			Log.i(TAG, "type--->" + type);
//			if(type != null && type.equals(BangDanKey.ZONGYI_TYPE)){
//				
//				String prod_subName = info.prod_subname;
//				Log.i(TAG, "prod_subName--->" + prod_subName);
//				if(prod_subName != null && !prod_subName.equals("")
//						&& !prod_subName.equals("EMPTY")) {
//					
//					int currentIndex = Integer.valueOf(prod_subName);
//					
//					if(currentIndex > 0 && currentIndex < 2000) {
//						Log.i(TAG, "currentIndex--->" + currentIndex);
//						
//						if(isOver) {//如果为正序
//							
//							//选择多少页，然后选择多少页下的第几个Button
//							int chu = (currentIndex - 1)/COUNT;
//							int quyu = currentIndex%COUNT;
//							
//							selectedIndex = chu+ 1;//当前页数
//							seletedButtonIndex = currentIndex;
//						} else {//如果为倒序
//							
//							int tempFirstIndex = num%COUNT;
//							int tempTotal = num - tempFirstIndex;
//							
//							if(currentIndex >=1 && currentIndex <= tempFirstIndex) {//最后一页
//								
//								selectedIndex = totle_pagecount;
//								seletedButtonIndex = currentIndex;
//							} else if(currentIndex > tempFirstIndex){
//								
//								int tempIndex = (currentIndex - tempFirstIndex - 1)/COUNT;
//								selectedIndex = totle_pagecount - tempIndex;
//								seletedButtonIndex = currentIndex;
//							}
//						}
//					}
//				}
//			}
//		}
//		
//		Log.i(TAG, "selectedIndex--->" + selectedIndex);
//		
//		if(date != null) {
//			
//			if(isShowHeadTable) {//导航栏显示出来
//				
//				if(num>COUNT*selectedIndex){
//					initTableView(COUNT);
//				}else{
//					initTableView(num-COUNT*(selectedIndex-1));
//				}
//				
//				if(seletedTitleButton != null) {
//					
//					seletedTitleButton.setEnabled(true);
//				}
//				
//				seletedTitleButton = (Button) findViewById(selectedIndex*10000);
//				seletedTitleButton.setEnabled(false);
//				seletedTitleButton.requestFocus();
//				
//				if(seletedIndexButton == null){
//					seletedIndexButton = (Button) findViewById(seletedButtonIndex);
//					seletedIndexButton.setBackgroundResource(R.drawable.bg_button_tv_selector_1);
//					seletedIndexButton.setTextColor(getResources().getColorStateList(R.color.tv_btn_text_color_selector_1));
//					seletedIndexButton.setPadding(8, 0, 0, 0);
////					seletedIndexButton.setEnabled(false);
//				}else{
////					seletedIndexButton.setEnabled(true);
//					seletedIndexButton.setBackgroundResource(R.drawable.bg_button_tv_selector);
//					seletedIndexButton.setTextColor(getResources().getColorStateList(R.color.tv_btn_text_color_selector));
//					seletedIndexButton.setPadding(8, 0, 0, 0);
//					seletedIndexButton = (Button) findViewById(seletedButtonIndex);
//					seletedIndexButton.setBackgroundResource(R.drawable.bg_button_tv_selector_1);
//					seletedIndexButton.setTextColor(getResources().getColorStateList(R.color.tv_btn_text_color_selector_1));
//					seletedIndexButton.setPadding(8, 0, 0, 0);
////					seletedIndexButton.setEnabled(false);
//				}
//				
//			} else {//导航栏没显示出来
//				
//				if(seletedIndexButton == null){
//					seletedIndexButton = (Button) findViewById(seletedButtonIndex);
//					seletedIndexButton.setBackgroundResource(R.drawable.bg_button_tv_selector_1);
//					seletedIndexButton.setTextColor(getResources().getColorStateList(R.color.tv_btn_text_color_selector_1));
//					seletedIndexButton.setPadding(8, 0, 0, 0);
////					seletedIndexButton.setEnabled(false);
//				}else{
////					seletedIndexButton.setEnabled(true);
//					seletedIndexButton.setBackgroundResource(R.drawable.bg_button_tv_selector);
//					seletedIndexButton.setTextColor(getResources().getColorStateList(R.color.tv_btn_text_color_selector));
//					seletedIndexButton.setPadding(8, 0, 0, 0);
//					seletedIndexButton = (Button) findViewById(seletedButtonIndex);
//					seletedIndexButton.setBackgroundResource(R.drawable.bg_button_tv_selector_1);
//					seletedIndexButton.setTextColor(getResources().getColorStateList(R.color.tv_btn_text_color_selector_1));
//					seletedIndexButton.setPadding(8, 0, 0, 0);
////					seletedIndexButton.setEnabled(false);
//				}
//			}
//			
//		}
//		
//	}
	
	private void play(int index){
		Log.i(TAG, "play--->" + index);
		
		if(num<=index){
			return;
		}
		CurrentPlayData playDate = new CurrentPlayData();
		Intent intent = new Intent(this,VideoPlayerActivity.class);
		playDate.prod_id = prod_id;
		playDate.prod_type = 3;
		playDate.prod_name = date.show.name+" " + date.show.episodes[index].name;
//		playDate.prod_url = date.show.episodes[0].down_urls[0].urls[0].url;
//		playDate.prod_src = date.show.episodes[0].down_urls[0].source;
		List<URLS_INDEX> urls = getBofangList(index);
		if(urls == null||urls.size()==0){
			Toast.makeText(this, "没有可以播放的地址", Toast.LENGTH_SHORT).show();
			return;
		}
		playDate.CurrentIndex = index;
//		playDate.CurrentIndex = date.show.episodes.length -1 - index;
		playDate.prod_url = urls.get(0).url;
		playDate.prod_src = urls.get(0).source_from;
		playDate.prod_favority = isXiai;
//		if(Constant.player_quality_index[0].equals(date.show.episodes[0].down_urls[0].urls[0].type)){
//			//mp4
//		}else if(Constant.player_quality_index[1].equals(date.show.episodes[0].down_urls[0].urls[0].type)){
//			//hd2
//		}else {
//			//other
//		}
//		playDate.prod_qua = Integer.valueOf(info.definition);
//		playDate.CurrentIndex = index;
		app.set_ReturnProgramView(date);
		app.setCurrentPlayData(playDate);
		startActivityForResult(intent, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
//		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == JieMianConstant.SHOUCANG_ADD) {
			
			favNum ++;
			
			xiaiBt.setText(favNum + "");
			ItemStateUtils.shoucangButtonToFocusState(xiaiBt, getApplicationContext());
			isXiai = true;
		}else if(resultCode == JieMianConstant.SHOUCANG_CANCEL){
//			Toast.makeText(getApplicationContext(), "SHOUCANG_CANCEL:" + requestCode, Toast.LENGTH_SHORT);
			if(favNum - 1 >=0) {
				
				favNum --;
				xiaiBt.setText((favNum) + "");
				ItemStateUtils.shoucangButtonToNormalState(xiaiBt, getApplicationContext());
			}
			isXiai = false;
		}
	}
	
	private List<URLS_INDEX> getBofangList(int index){
		List<URLS_INDEX> list = new ArrayList<URLS_INDEX>();
		
		if(index >= date.tv.episodes.length) {
			
			return null;
		}
		
		DOWN_URLS[] urls = date.show.episodes[index].down_urls;
		if(urls == null){
			return null;
		}
		for(int i=0;i<urls.length; i++){
			for(int j=0; j<urls[i].urls.length; j++){
				URLS_INDEX url_index = new URLS_INDEX();
				url_index.source_from = urls[i].source;
				url_index.url = urls[i].urls[j].url;
				if (urls[i].source.trim().equalsIgnoreCase(Constant.video_index[0])) {
					url_index.souces = 0;
				} else if (urls[i].source.trim().equalsIgnoreCase(Constant.video_index[1])) {
					url_index.souces = 1;
				} else if (urls[i].source.trim().equalsIgnoreCase(Constant.video_index[2])) {
					url_index.souces = 2;
				} else if (urls[i].source.trim().equalsIgnoreCase(Constant.video_index[3])) {
					url_index.souces = 3;
				} else if (urls[i].source.trim().equalsIgnoreCase(Constant.video_index[4])) {
					url_index.souces = 4;
				} else if (urls[i].source.trim().equalsIgnoreCase(Constant.video_index[5])) {
					url_index.souces = 5;
				} else if (urls[i].source.trim().equalsIgnoreCase(Constant.video_index[6])) {
					url_index.souces = 6;
				} else if (urls[i].source.trim().equalsIgnoreCase(Constant.video_index[7])) {
					url_index.souces = 7;
				} else if (urls[i].source.trim().equalsIgnoreCase(Constant.video_index[8])) {
					url_index.souces = 8;
				} else if (urls[i].source.trim().equalsIgnoreCase(Constant.video_index[9])) {
					url_index.souces = 9;
				} else if (urls[i].source.trim().equalsIgnoreCase(Constant.video_index[10])) {
					url_index.souces = 10;
				} else if (urls[i].source.trim().equalsIgnoreCase(Constant.video_index[11])) {
					url_index.souces = 11;
				} else {
					url_index.souces = 12;
				}
				if(urls[i].urls[j].type.trim().equalsIgnoreCase(Constant.player_quality_index[1])){
					url_index.defination = 1;
				}else if(urls[i].urls[j].type.trim().equalsIgnoreCase(Constant.player_quality_index[0])){
					url_index.defination = 2;
				}else if(urls[i].urls[j].type.trim().equalsIgnoreCase(Constant.player_quality_index[2])){
					url_index.defination = 3;
				}else if(urls[i].urls[j].type.trim().equalsIgnoreCase(Constant.player_quality_index[3])){
					url_index.defination = 4;
				} else {
					url_index.defination = 5;
				}
				list.add(url_index);
			}
		}
		if(list.size()>1){
			Collections.sort(list, new DefinationComparatorIndex());
			Collections.sort(list, new SouceComparatorIndex1());
		}
		return list;
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
		
		//闹钟开启的情况下，取消收藏删除数据库中相关数据
		if(UtilTools.is48TimeClock(getApplicationContext()))
			DBUtils.deleteData4ProId(getApplicationContext(), 
				UtilTools.getCurrentUserId(getApplicationContext()), prod_id);
		
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
			aq.id(R.id.text_directors).text(intent.getStringExtra("stars"));
			aq.id(R.id.text_starts).text(intent.getStringExtra("directors"));
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
