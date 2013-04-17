package com.joyplus.tv;

import java.io.IOException;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.joyplus.tv.Service.Return.ReturnProgramView;
import com.joyplus.tv.Video.VideoPlayerActivity;
import com.joyplus.tv.utils.MyKeyEventKey;

public class ShowXiangqingDongman extends Activity implements View.OnClickListener,
		View.OnKeyListener, MyKeyEventKey {

	private static final String TAG = "ShowXiangqingDongman";
	private LinearLayout bofangLL;

	private Button dingBt,xiaiBt, yingpingBt;
	private Button bofangBt,gaoqingBt;

	private View beforeView;

	private PopupWindow popupWindow;
	private View popupView;

	private boolean isDing, isXiai;
	private boolean isPopupWindowShow;

	private View beforeTempPop, currentBofangViewPop;
	private LinearLayout chaoqingLL, gaoqingLL, biaoqingLL;
	
	private LinearLayout layout;
	private TableLayout table;
	private boolean isOver = false;
	private int num = 0;
	private int totle_pagecount;
	private int selectedIndex;
	private static final int COUNT = 20;
	
	private String prod_id;
	
	private AQuery aq;
	private App app;
	private ReturnProgramView date;
	private Handler handler =  new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.show_xiangxi_dongman_layout);
		Intent intent = getIntent();
		prod_id = intent.getStringExtra("ID");
		if(prod_id == null||"".equals(prod_id)){
			Log.e(TAG, "pram error");
			finish();
		}
		aq = new AQuery(this);
		app = (App) getApplication();
		initView();
		getServiceDate();
	}

	private void initButton() {
		// TODO Auto-generated method stub
		
		totle_pagecount = (num%COUNT ==0)? num/COUNT:num/COUNT+1;
		
		for(int i=0; i<totle_pagecount; i++){
			Button b = new Button(this);
//			b.setWidth(table.getWidth()/5);
//			b.setHeight(layout.getHeight());
			b.setLayoutParams(new LayoutParams((table.getWidth()-80)/5,35));
			if(isOver){
				if((i+1)*COUNT>num){
					b.setText((i*COUNT+1) +"-"+num);
				}else{
					b.setText((i*COUNT+1) +"-"+(i+1)*COUNT);
				}
			}else{
				if(num-(i+1)*COUNT<0){
					b.setText((num-i*COUNT) + "-1");
				}else{
					b.setText((num-i*COUNT) + "-" + (num-(i+1)*COUNT+1));
				}
				
			}
			b.setBackgroundResource(R.drawable.xiangqing_button_selector);
			b.setId((i+1)*10000);
			b.setOnClickListener(this);
			layout.addView(b);
			if(i!=totle_pagecount-1){
				TextView t = new TextView(this);
				t.setLayoutParams(new LayoutParams(20,35));
				layout.addView(t);
			}
			
		}
		
		selectedIndex = 1;
		if(num>COUNT){
			initTableView(COUNT);
		}else{
			initTableView(num);
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
		
		layout = (LinearLayout) findViewById(R.id.layout);
		table  = (TableLayout) findViewById(R.id.table);

		addListener();

		initPopWindow();


		beforeView = dingBt;

	}

	private void addListener() {

		dingBt.setOnKeyListener(this);
		xiaiBt.setOnKeyListener(this);
		bofangLL.setOnKeyListener(this);

		dingBt.setOnClickListener(this);
		xiaiBt.setOnClickListener(this);
		bofangLL.setOnClickListener(this);
		yingpingBt.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bt_xiangqingding:
			if (isDing) {
				isDing = false;
			} else {
				isDing = true;
			}
			dingBt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_dig_active, 0, 0,0);
			dingBt.setTextColor(getResources().getColor(R.color.text_foucs));
			break;
		case R.id.bt_xiangqing_xiai:
			if (isXiai) {
				isXiai = false;
			} else {
				isXiai = true;
			}
			xiaiBt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_fav_active, 0, 0,0);
			xiaiBt.setTextColor(getResources().getColor(R.color.text_foucs));
			break;
		case R.id.ll_xiangqing_bofang_gaoqing:
			// bofangLL.setN
			// xiaiIv.setImageResource(R.drawable.icon_fav_active);
			// xiaiTv.setTextColor(getResources().getColor(R.color.text_foucs));
			String str0 = "984192";
			String str1 = "西游降魔篇";
			String str2 = "http://221.130.179.66/25/36/53/kingsoft/movie/47978987920B0079FF686B6370B4E039-xiyoupian.mp4?crypt=61740d1aa7f2e300&b=800&gn=132&nc=1&bf=30&p2p=1&video_type=mp4&check=0&tm=1364191200&key=af7b9ad0697560c682a0070cf225e65e&opck=1&lgn=letv&proxy=3702889363&cipi=2026698610&tsnp=1&tag=ios&tag=kingsoft&sign=coopdown&realext=.mp4test=m3u8";

			Intent intent = new Intent(this, VideoPlayerActivity.class);
			intent.putExtra("prod_url", str2);
			intent.putExtra("title", str1);
			startActivity(intent);
			break;
		case R.id.bt_xiangqing_yingping:
			startActivity(new Intent(this, DetailComment.class));
		default:
			
			if(v.getId()>=10000){
				selectedIndex = v.getId()/10000;
				if(num>COUNT*selectedIndex){
					initTableView(COUNT);
				}else{
					initTableView(num-COUNT*(selectedIndex-1));
				}
			}else{
				Toast.makeText(this, "click btn = " + v.getId(), 100).show();
//				scrollView.smoothScrollBy(20, 0);
			}
			break;
		}

	}

	private void backToNormalState() {
		int id = beforeView.getId();
		switch (id) {
		case R.id.bt_xiangqingding:
			if (!isDing) {
				dingBt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ding_selector, 0, 0,0);
			}
			dingBt.setTextColor(getResources().getColor(R.color.time_color));
			break;
		case R.id.bt_xiangqing_xiai:
			if (!isXiai) {
				xiaiBt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.xiai_selector, 0, 0,0);
			}
			xiaiBt.setTextColor(getResources().getColor(R.color.time_color));
			break;
		case R.id.ll_xiangqing_bofang_gaoqing:
			// bofangLL.setN
			// xiaiIv.setImageResource(R.drawable.icon_fav_active);
			// xiaiTv.setTextColor(getResources().getColor(R.color.text_foucs));
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
			case R.id.bt_xiangqingding:
				if (keyCode == KEY_UP || keyCode == KEY_LEFT) {
					backToNormalState();
					dingBt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_dig_active, 0, 0,0);
					dingBt.setTextColor(getResources().getColor(
							R.color.text_foucs));
				}
				break;
			case R.id.bt_xiangqing_xiai:
				if (keyCode == KEY_UP || keyCode == KEY_LEFT
						|| keyCode == KEY_RIGHT) {
					backToNormalState();
					dingBt.setSelected(false);
					xiaiBt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_fav_active, 0, 0,0);
					xiaiBt.setTextColor(getResources().getColor(
							R.color.text_foucs));
				}
				break;
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

					} else {

						backToNormalState();
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
		table.removeAllViews();
		int col = (count%5 ==0)? count/5:count/5+1;
		for(int j=0; j<col; j++){
			TableRow row = new TableRow(this);
//			row.setId(6-flag);
			for(int i =0; i<5 ; i++){
				Button btn = new Button(this);
				btn.setWidth((table.getWidth()-80)/5);
				btn.setTextSize(18);
				btn.setHeight(25);
				if(isOver){
					btn.setText("" + (j*5+i+1 + (selectedIndex-1)*COUNT));
					btn.setId(j*5+i+1 + (selectedIndex-1)*COUNT);
				}else{
					btn.setText("" + (num-((j*5+i)+ (selectedIndex-1)*COUNT)));
					btn.setId(num-((j*5+i)+ (selectedIndex-1)*COUNT));
				}
				btn.setOnClickListener(this);
				btn.setBackgroundResource(R.drawable.xiangqing_button_selector);
				if(j*5+i+1>count){
					btn.setVisibility(View.INVISIBLE);
				}
				TextView t = new TextView(this);
				t.setWidth(20);
				row.addView(btn);
				if(i!=4){
					row.addView(t);
				}
			}
			row.setLayoutParams(new LayoutParams(table.getWidth(),35));
			row.setPadding(0, 5, 0, 5);
			table.addView(row);
		}
	}
	
	
	private void getServiceDate(){
		String url = Constant.BASE_URL + "program/view" +"?prod_id=" + prod_id;
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, "initData");
		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}
	
	public void initData(String url, JSONObject json, AjaxStatus status){
		if (status.getCode() == AjaxStatus.NETWORK_ERROR||json == null) {
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		Log.d(TAG, "data = " + json.toString());
		ObjectMapper mapper = new ObjectMapper();
		try {
			date = null;
			date  = mapper.readValue(json.toString(), ReturnProgramView.class);
			if(date == null){
				Log.e(TAG, "tv date error---->date == null");
				return;
			}
			if("".equals(date.tv.cur_episode)||"0".equals(date.tv.cur_episode)
					||date.tv.cur_episode.equals(date.tv.max_episode)){
				isOver = true;
				num = Integer.valueOf(date.tv.max_episode);
			}else if(Integer.valueOf(date.tv.cur_episode)>Integer.valueOf(date.tv.max_episode)){
				isOver = true;
				num = Integer.valueOf(date.tv.max_episode);
			}else{
				isOver = false;
				num = Integer.valueOf(date.tv.cur_episode);
			}
			updateView();
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
		initButton();
		aq.id(R.id.image).image(date.tv.poster, false, true,0, R.drawable.post_normal);
		aq.id(R.id.text_name).text(date.tv.name);
		aq.id(R.id.text_directors).text(date.tv.directors);
		aq.id(R.id.text_starts).text(date.tv.stars);
		aq.id(R.id.text_introduce).text(date.tv.summary);
		aq.id(R.id.bt_xiangqingding).text(date.tv.support_num);
		aq.id(R.id.bt_xiangqing_xiai).text(date.tv.favority_num);
		int definition = Integer.valueOf((date.tv.definition));
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
		updateScore(date.tv.score);
	}
	
	private void updateScore(String score){
		aq.id(R.id.textView_score).text(date.tv.score);
		float f = Float.valueOf(score);
		int i = Math.round(f);
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
}
