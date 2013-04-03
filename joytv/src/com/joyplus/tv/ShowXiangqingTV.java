package com.joyplus.tv;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ShowXiangqingTV extends Activity{

	private LinearLayout dingLL, xiaiLL, bofangLL;

	private ImageView dingIv, xiaiIv, bofangIv;
	private TextView dingTv, xiaiTv, bofangTv, gaoqingTv;
	private Button xiazaiBt, yingpingBt;
	private GridView gv;

	private View beforeView;
	
	private PopupWindow popupWindow;
	private View popupView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.show_dianying_xiangxi_layout);

		initView();
	}
	
	private void initPopWindow() {
		LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);  
		popupView = inflater.inflate(R.layout.show_gaoqing_item, null); 
		popupWindow = new PopupWindow(popupView);
	}

	private void initView() {

		dingLL = (LinearLayout) findViewById(R.id.ll_xiangqingding);
		xiaiLL = (LinearLayout) findViewById(R.id.ll_xiangqing_xiai);
		bofangLL = (LinearLayout) findViewById(R.id.ll_xiangqing_bofang_gaoqing);

		dingIv = (ImageView) findViewById(R.id.iv_xiangqingding);
		xiaiIv = (ImageView) findViewById(R.id.iv_xiangqing_xiai);
		bofangIv = (ImageView) findViewById(R.id.iv_xiangqing_bofang);

		dingTv = (TextView) findViewById(R.id.tv_xiangqingding);
		xiaiTv = (TextView) findViewById(R.id.tv_xiangqing_xiai);
		bofangTv = (TextView) findViewById(R.id.tv_xiangqing_bofang);
		gaoqingTv = (TextView) findViewById(R.id.tv_xiangqing_gaoqing);

		xiazaiBt = (Button) findViewById(R.id.bt_xiangqing_xiazai);
		yingpingBt = (Button) findViewById(R.id.bt_xiangqing_yingping);

		gv = (GridView) findViewById(R.id.gv_xiangqing_tuijiandianying);
		
		initPopWindow();
		gv.setNextFocusUpId(R.id.ll_xiangqingding);
		gv.setNextFocusDownId(R.id.ll_xiangqingding);
		gv.setNextFocusLeftId(R.id.ll_xiangqingding);
		gv.setNextFocusRightId(R.id.ll_xiangqingding);
		
		
		gv.setAdapter(baseAdapter);
		
		xiazaiBt.setFocusable(false);
		bofangLL.setFocusable(true);
		
		beforeView = dingLL;
		dingLL.setSelected(true);

		gv.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();

				if (action == KeyEvent.ACTION_UP) {
				}
				return false;
			}
		});

		dingLL.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();

				if (action == KeyEvent.ACTION_UP) {
					
					if (keyCode == KeyEvent.KEYCODE_NUMPAD_4) {
						beforeViewActiveStateBack();
						v.setBackgroundResource(R.drawable.btn_active);
						dingIv.setImageResource(R.drawable.icon_dig_active);
						dingTv.setTextColor(getResources().getColor(R.color.xiangqing_active_text_color));
					} else if( keyCode == KeyEvent.KEYCODE_NUMPAD_5){
						v.setBackgroundResource(R.drawable.btn_active);
						dingIv.setImageResource(R.drawable.icon_dig_active);
					}
				}else {
					if( keyCode == KeyEvent.KEYCODE_NUMPAD_5){
						v.setBackgroundResource(R.drawable.btn_press);
						dingIv.setImageResource(R.drawable.icon_dig_pressed);
						dingTv.setTextColor(getResources().getColor(R.color.xiangqing_active_text_color));
					}
				}
				beforeView = v;
				return false;
			}
		});
		xiaiLL.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();

				if (action == KeyEvent.ACTION_UP) {
					
					if (keyCode == KeyEvent.KEYCODE_NUMPAD_4||
							keyCode == KeyEvent.KEYCODE_NUMPAD_6) {
						beforeViewActiveStateBack();
						v.setBackgroundResource(R.drawable.btn_active);
						xiaiIv.setImageResource(R.drawable.icon_fav_active);
						xiaiTv.setTextColor(getResources().getColor(R.color.xiangqing_active_text_color));
					} else if( keyCode == KeyEvent.KEYCODE_NUMPAD_5){
						v.setBackgroundResource(R.drawable.btn_active);
						dingIv.setImageResource(R.drawable.icon_dig_active);
					}
				}else {
					if( keyCode == KeyEvent.KEYCODE_NUMPAD_5){
						v.setBackgroundResource(R.drawable.btn_press);
						xiaiIv.setImageResource(R.drawable.icon_fav_pressed);
						xiaiTv.setTextColor(getResources().getColor(R.color.xiangqing_active_text_color));
					}
				}
				beforeView = v;
				return false;
			}
		});
		 bofangLL.setOnKeyListener(new View.OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					int action = event.getAction();

					if (action == KeyEvent.ACTION_UP) {
						
						if (keyCode == KeyEvent.KEYCODE_NUMPAD_4||
								keyCode == KeyEvent.KEYCODE_NUMPAD_6) {
							beforeViewActiveStateBack();
							v.setBackgroundResource(R.drawable.btn_active);
							bofangTv.setTextColor(getResources().getColor(R.color.xiangqing_active_text_color));
							gaoqingTv.setTextColor(getResources().getColor(R.color.xiangqing_active_text_color));
						} else if( keyCode == KeyEvent.KEYCODE_NUMPAD_5){
//							v.setBackgroundResource(R.drawable.btn_active);
							LinearLayout chaogaoingLL = (LinearLayout) popupView.findViewById(R.id.ll_gaoqing_chaogaoqing);
							LinearLayout gaoqingLL = (LinearLayout) popupView.findViewById(R.id.ll_gaoqing_gaoqing);
							LinearLayout biaoqingLL = (LinearLayout) popupView.findViewById(R.id.ll_gaoqing_biaoqing);
							
							chaogaoingLL.setFocusable(true);
							
							chaogaoingLL.setOnKeyListener(new View.OnKeyListener() {
								
								@Override
								public boolean onKey(View v, int keyCode, KeyEvent event) {
									// TODO Auto-generated method stub
									int action = event.getAction();
									if(action == KeyEvent.ACTION_UP) 
									popupWindow.dismiss();
									return false;
								}
							});
							
							gaoqingLL.setOnKeyListener(new View.OnKeyListener() {
								
								@Override
								public boolean onKey(View v, int keyCode, KeyEvent event) {
									// TODO Auto-generated method stub

									int action = event.getAction();
									if(action == KeyEvent.ACTION_UP) 
									popupWindow.dismiss();
									return false;
								}
							});
							
							biaoqingLL.setOnKeyListener(new View.OnKeyListener() {
								
								@Override
								public boolean onKey(View v, int keyCode, KeyEvent event) {
									// TODO Auto-generated method stub

									int action = event.getAction();
									if(action == KeyEvent.ACTION_UP) 
									popupWindow.dismiss();
									return false;
								}
							});
							int[] location = new int[2];  
				            v.getLocationOnScreen(location); 
							int width = v.getWidth();
							int height = 3 * v.getHeight() + 20;
							float locationX = location[0] ;
							float locationY = location[1] - height; 
							popupWindow.setWidth(v.getWidth());
							popupWindow.setHeight(3 * v.getHeight() + 20);
							popupWindow.showAtLocation(v.getRootView(), Gravity.NO_GRAVITY, (int)locationX, (int)locationY);
							popupWindow.setFocusable(true);
							chaogaoingLL.setSelected(true);
						}
					}
					beforeView = v;
					return false;
				}
			});
		// xiazaiBt.setOnKeyListener(this);
		 yingpingBt.setOnKeyListener(new View.OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					int action = event.getAction();

					if (action == KeyEvent.ACTION_UP) {
						
						if (keyCode == KeyEvent.KEYCODE_NUMPAD_6) {
							beforeViewActiveStateBack();
							v.setBackgroundResource(R.drawable.btn_active);
							((Button)v).setTextColor(getResources().getColor(R.color.xiangqing_active_text_color));
						} else if( keyCode == KeyEvent.KEYCODE_NUMPAD_5){
//							v.setBackgroundResource(R.drawable.btn_active);
							v.setBackgroundResource(R.drawable.btn_active);
							((Button)v).setTextColor(getResources().getColor(R.color.xiangqing_active_text_color));
						}
					}else if( keyCode == KeyEvent.KEYCODE_NUMPAD_5){
						v.setBackgroundResource(R.drawable.btn_press);
						((Button)v).setTextColor(getResources().getColor(R.color.xiangqing_active_text_color));
					}
					beforeView = v;
					return false;
				}
			});
		 
		 gv.setOnKeyListener(new View.OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					int action = event.getAction();

					if (action == KeyEvent.ACTION_UP) {
						
						if (keyCode == KeyEvent.KEYCODE_NUMPAD_2) {
							beforeViewActiveStateBack();
						} 
					}
					return false;
				}
			});

	}
	
	private void beforeViewActiveStateBack() {
		
		int id = beforeView.getId();
		
		switch (id) {
		case R.id.ll_xiangqingding:
			dingLL.setBackgroundResource(R.drawable.btn_dark);
			dingTv.setTextColor(getResources().getColor(R.color.time_color));
			break;
		case R.id.ll_xiangqing_xiai:
			xiaiLL.setBackgroundResource(R.drawable.btn_dark);
			xiaiTv.setTextColor(getResources().getColor(R.color.time_color));
			break;
		case R.id.ll_xiangqing_bofang_gaoqing:
			bofangLL.setBackgroundResource(R.drawable.btn_dark);
			break;
		case R.id.bt_xiangqing_xiazai:
			
			break;
		case R.id.bt_xiangqing_yingping:
			yingpingBt.setBackgroundResource(R.drawable.btn_normal);
			break;

		default:
			break;
		}
	}
	
	private BaseAdapter baseAdapter = new BaseAdapter() {
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl_xiangqing_tuijiandianying);
			RelativeLayout rl2 = (RelativeLayout) findViewById(R.id.rl_xiangqing_hotline);
			int height = rl.getHeight() - rl2.getHeight();
			int width = rl.getWidth();
			ImageView iv = new ImageView(getApplicationContext());
			iv.setImageResource(R.drawable.movie_pic);
			iv.setLayoutParams(new AbsListView.LayoutParams(width/6,height - 10 ));
			iv.setPadding(10, 10, 10, 10);
			
			return iv;
		}
		
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 6;
		}
	};

}
