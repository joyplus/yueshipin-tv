package com.joyplus.tv;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;

public class ShowMovieActivity extends Activity implements OnKeyListener ,OnTouchListener{
	private Button mFenLeiBtn;
	private EditText searchEt;
	private GridView movieGv;
	
	private Button dongzuoBtn,lunliBtn,xijuBtn,aiqingBtn,xuanyiBtn,kongbuBtn;
	private LinearLayout dongzuoLL,lunliLL,xijuLL,aiqingLL,xuanyiLL,kongbuLL;
	
	private Button zuijinguankanBtn,zhuijushoucangBtn,lixianshipinBtn;
	private LinearLayout zuijinguankanLL,zhuijushoucangLL,lixianshipinLL;
	
	private int beforeRecordID = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_movie);
		
		initView();
		initState();
		
	}
	
	private void initView() {
		
		searchEt = (EditText) findViewById(R.id.et_search);
		mFenLeiBtn = (Button) findViewById(R.id.bt_quanbufenlei);	
		movieGv = (GridView) findViewById(R.id.gv_movie_show);
		
		dongzuoBtn = (Button) findViewById(R.id.bt_dongzuopian);
		lunliBtn = (Button) findViewById(R.id.bt_lunlipian);
		xijuBtn = (Button) findViewById(R.id.bt_xijupian);
		aiqingBtn = (Button) findViewById(R.id.bt_aiqingpian);
		xuanyiBtn = (Button) findViewById(R.id.bt_xuanyipian);
		kongbuBtn = (Button) findViewById(R.id.bt_kongbupian);
		
		dongzuoLL = (LinearLayout) findViewById(R.id.ll_dongzuopian);
		lunliLL = (LinearLayout) findViewById(R.id.ll_lunlipian);
		xijuLL = (LinearLayout) findViewById(R.id.ll_xijupian);
		aiqingLL = (LinearLayout) findViewById(R.id.ll_aiqingpian);
		xuanyiLL = (LinearLayout) findViewById(R.id.ll_xuanyipian);
		kongbuLL = (LinearLayout) findViewById(R.id.ll_kongbupian);
		
		zuijinguankanBtn = (Button) findViewById(R.id.bt_zuijinguankan);
		zhuijushoucangBtn = (Button) findViewById(R.id.bt_zhuijushoucang);
		lixianshipinBtn = (Button) findViewById(R.id.bt_lixianshipin);
		
		zuijinguankanLL = (LinearLayout) findViewById(R.id.ll_zuijinguankan);
		zhuijushoucangLL = (LinearLayout) findViewById(R.id.ll_zhuijushoucang);
		lixianshipinLL = (LinearLayout) findViewById(R.id.ll_lixianshipin);
		
		mFenLeiBtn.setOnKeyListener(this);
		searchEt.setOnKeyListener(this);
		movieGv.setOnKeyListener(this);
		
		dongzuoLL.setOnKeyListener(this);
		lunliLL.setOnKeyListener(this);
		xijuLL.setOnKeyListener(this);
		aiqingLL.setOnKeyListener(this);
		xuanyiLL.setOnKeyListener(this);
		kongbuLL.setOnKeyListener(this);
		
		zuijinguankanLL.setOnKeyListener(this);
		zhuijushoucangLL.setOnKeyListener(this);
		lixianshipinLL.setOnKeyListener(this);
		
		dongzuoLL.setOnTouchListener(this);
		lunliLL.setOnTouchListener(this);
		xijuLL.setOnTouchListener(this);
		aiqingLL.setOnTouchListener(this);
		xuanyiLL.setOnTouchListener(this);
		kongbuLL.setOnTouchListener(this);
		
		zuijinguankanLL.setOnTouchListener(this);
		zhuijushoucangLL.setOnTouchListener(this);
		lixianshipinLL.setOnTouchListener(this);
		mFenLeiBtn.setOnTouchListener(this);
		
		
		movieGv.setOnItemSelectedListener(new AbsListView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(position > 9) {
					
					movieGv.setAdapter(new BaseAdapter() {
						
						@Override
						public View getView(int position, View convertView, ViewGroup parent) {
							// TODO Auto-generated method stub
							View v;
							LinearLayout parentLayout = (LinearLayout) findViewById(R.id.movie_show_10);
							int width = parentLayout.getWidth();
							int height = parent.getHeight();
							
//							 v = getLayoutInflater().inflate(R.layout.item_show2, null);
//							 AbsListView.LayoutParams lp = new AbsListView.LayoutParams(width/5, (height - height/10)/2);
//							 v.setPadding(20, 20, 20, 20);
//							 v.setLayoutParams(lp);
//							 convertView = v;
							return convertView;
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
							return 15;
						}
					});
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	
	private void initState() {
		beforeRecordID = R.id.bt_quanbufenlei;//全部分类为激活状态
		
		searchEt.setFocusable(false);//搜索焦点消失
		movieGv.setNextFocusLeftId(R.id.bt_quanbufenlei);//网格向左 全部分类获得焦点
		
		movieGv.setSelected(true);//网格获取焦点
		mFenLeiBtn.setTextColor(getResources().getColor(R.color.text_orange));//����������ɫ����Ϊorange
		mFenLeiBtn.setBackgroundResource(R.drawable.menubg);//���� ��Ĭ�ϼ���
		
		movieGv.setAdapter(new MovieAdpter());
	}
	
	

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == KeyEvent.ACTION_UP) {
			
			switch (v.getId()) {
			case R.id.bt_quanbufenlei:
//				Log.i("Yangzhg", "onKey--->>KEYCODE : " + keyCode + " ID :" + v.getId());
				switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_UP:
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					searchEt.setFocusable(true);
					mFenLeiBtn.setTextColor(getResources().getColor(R.color.text_foucs));
					mFenLeiBtn.setBackgroundResource(R.drawable.text_drawable_selector);
					break;

				default:
					break;
				}
				break;
			case R.id.et_search:
//				Log.i("Yangzhg", "onKey--->>KEYCODE : " + keyCode + " ID :" + v.getId());
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_UP:
					mFenLeiBtn.setTextColor(getResources().getColor(R.color.text_orange));//����������ɫ����Ϊorange
					mFenLeiBtn.setBackgroundResource(R.drawable.menubg);//���� ��Ĭ�ϼ���
					break;


				default:
					break;
				}
			case R.id.gv_movie_show:
//				Log.i("Yangzhg", "onKey--->>KEYCODE : " + keyCode + " ID :" + v.getId());
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					mFenLeiBtn.setTextColor(getResources().getColor(R.color.text_orange));//����������ɫ����Ϊorange
					mFenLeiBtn.setBackgroundResource(R.drawable.menubg);//���� ��Ĭ�ϼ���
					break;


				default:
					break;
				}
			case R.id.ll_dongzuopian:
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_UP:
					mFenLeiBtn.setTextColor(getResources().getColor(R.color.text_orange));//����������ɫ����Ϊorange
					mFenLeiBtn.setBackgroundResource(R.drawable.menubg);//���� ��Ĭ�ϼ���
					break;


				default:
					break;
				}
				break;
			case R.id.ll_lunlipian:
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_UP:
					mFenLeiBtn.setTextColor(getResources().getColor(R.color.text_orange));//����������ɫ����Ϊorange
					mFenLeiBtn.setBackgroundResource(R.drawable.menubg);//���� ��Ĭ�ϼ���
					break;


				default:
					break;
				}
				break;
			case R.id.ll_xijupian:
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_UP:
					mFenLeiBtn.setTextColor(getResources().getColor(R.color.text_orange));//����������ɫ����Ϊorange
					mFenLeiBtn.setBackgroundResource(R.drawable.menubg);//���� ��Ĭ�ϼ���
					break;


				default:
					break;
				}
				break;
			case R.id.ll_aiqingpian:
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_UP:
					mFenLeiBtn.setTextColor(getResources().getColor(R.color.text_orange));//����������ɫ����Ϊorange
					mFenLeiBtn.setBackgroundResource(R.drawable.menubg);//���� ��Ĭ�ϼ���
					break;


				default:
					break;
				}
				break;
			case R.id.ll_xuanyipian:
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_UP:
					mFenLeiBtn.setTextColor(getResources().getColor(R.color.text_orange));//����������ɫ����Ϊorange
					mFenLeiBtn.setBackgroundResource(R.drawable.menubg);//���� ��Ĭ�ϼ���
					break;


				default:
					break;
				}
				break;
			case R.id.ll_kongbupian:
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_UP:
					mFenLeiBtn.setTextColor(getResources().getColor(R.color.text_orange));//����������ɫ����Ϊorange
					mFenLeiBtn.setBackgroundResource(R.drawable.menubg);//���� ��Ĭ�ϼ���
					break;


				default:
					break;
				}
				break;
			case R.id.ll_zuijinguankan:
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_UP:
					mFenLeiBtn.setTextColor(getResources().getColor(R.color.text_orange));//����������ɫ����Ϊorange
					mFenLeiBtn.setBackgroundResource(R.drawable.menubg);//���� ��Ĭ�ϼ���
					break;


				default:
					break;
				}
				break;
			case R.id.ll_zhuijushoucang:
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_UP:
					mFenLeiBtn.setTextColor(getResources().getColor(R.color.text_orange));//����������ɫ����Ϊorange
					mFenLeiBtn.setBackgroundResource(R.drawable.menubg);//���� ��Ĭ�ϼ���
					break;


				default:
					break;
				}
				break;
			case R.id.ll_lixianshipin:
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_UP:
					mFenLeiBtn.setTextColor(getResources().getColor(R.color.text_orange));//����������ɫ����Ϊorange
					mFenLeiBtn.setBackgroundResource(R.drawable.menubg);//���� ��Ĭ�ϼ���
					break;


				default:
					break;
				}
				break;
			default:
				break;
				
			}
		}

		return false;
	}
	
	private void rebackState(LinearLayout ll , Button btn) {
		if(ll != null) {
			ll.setBackgroundResource(R.drawable.text_drawable_selector);
			btn.setTextColor(getResources().getColor(R.color.text_pt));
		}else {
			
			btn.setTextColor(getResources().getColor(R.color.text_pt));
			btn.setBackgroundResource(R.drawable.text_drawable_selector);
		}

		
	}
	
	private void changeToActive(LinearLayout ll , Button btn , int action) {
		if(ll!= null) {
			if(action == KeyEvent.ACTION_DOWN) {
				btn.setTextColor(getResources().getColor(R.color.text_press));
				btn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.side_hot_active), null, null, null);

			} else {
//				ll.setBackgroundColor(Color.GREEN);
				ll.setBackgroundResource(R.drawable.menubg);
				btn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.side_hot_normal), null, null, null);
				btn.setTextColor(getResources().getColor(R.color.text_orange));
			}
		}else {
			
			if(action == KeyEvent.ACTION_DOWN) {

			}else {
				btn.setTextColor(getResources().getColor(R.color.text_orange));
			}
		}
		
	}
	
	private void button3rebackState(LinearLayout ll , Button btn) {
		
			ll.setBackgroundResource(R.drawable.text_drawable_selector);
			btn.setTextColor(getResources().getColor(R.color.text_pt));
		
	}
	
	private void button3changToActive(LinearLayout ll , Button btn , int action){
		
		if(action != KeyEvent.ACTION_DOWN) {
			
			ll.setBackgroundColor(Color.GREEN);
			btn.setTextColor(getResources().getColor(R.color.text_orange));
		}else {
			btn.setTextColor(getResources().getColor(R.color.text_press));
		}
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
	if(v!= null) {
		if(v.getId() == beforeRecordID) {
			return false;
		}
		if( action == KeyEvent.ACTION_UP) {
			
			switch (beforeRecordID) {
			case R.id.ll_dongzuopian:
				rebackState(dongzuoLL, dongzuoBtn);
				break;
			case R.id.ll_lunlipian:
				rebackState(lunliLL, lunliBtn);
				break;
			case R.id.ll_xijupian:
				rebackState(xijuLL, xijuBtn);
				break;
			case R.id.ll_aiqingpian:
				rebackState(aiqingLL, aiqingBtn);
				break;
			case R.id.ll_xuanyipian:
				rebackState(xuanyiLL, xuanyiBtn);
				break;
			case R.id.ll_kongbupian:
				rebackState(kongbuLL, kongbuBtn);
				break;
			case R.id.ll_zuijinguankan:
				button3rebackState(zuijinguankanLL, zuijinguankanBtn);
				break;
			case R.id.ll_zhuijushoucang:
				button3rebackState(zhuijushoucangLL, zhuijushoucangBtn);
				break;
			case R.id.ll_lixianshipin:
				button3rebackState(lixianshipinLL, lixianshipinBtn);
				break;
			case R.id.bt_quanbufenlei:
				rebackState(null, mFenLeiBtn);
				break;

			default:
				break;
			}
		}
			switch (v.getId()) {
			case R.id.ll_dongzuopian:
				changeToActive(dongzuoLL, dongzuoBtn, action);
				break;
			case R.id.ll_lunlipian:
				changeToActive(lunliLL, lunliBtn, action);
				break;
			case R.id.ll_xijupian:
				changeToActive(xijuLL, xijuBtn, action);

				break;
			case R.id.ll_aiqingpian:
				changeToActive(aiqingLL, aiqingBtn, action);
				break;
			case R.id.ll_xuanyipian:
				changeToActive(xuanyiLL, xuanyiBtn, action);
				break;
			case R.id.ll_kongbupian:
				changeToActive(kongbuLL, kongbuBtn, action);
				break;
			case R.id.ll_zuijinguankan:
				button3changToActive(zuijinguankanLL, zuijinguankanBtn,action);
				break;
			case R.id.ll_zhuijushoucang:
				button3changToActive(zhuijushoucangLL, zhuijushoucangBtn,action);
				break;
			case R.id.ll_lixianshipin:
				button3changToActive(lixianshipinLL, lixianshipinBtn,action);
				break;
			case R.id.bt_quanbufenlei:
				changeToActive(null, mFenLeiBtn,action);
				break;
			default:
				break;
			}
			if(action == KeyEvent.ACTION_UP)
				beforeRecordID = v.getId();
		}
		return false;
	}
	
	private class MovieAdpter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 15;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v;
			LinearLayout parentLayout = (LinearLayout) findViewById(R.id.movie_show_10);
			int width = parentLayout.getWidth();
			int height = parent.getHeight();
			
			 v = getLayoutInflater().inflate(R.layout.show_item_show, null);
			 AbsListView.LayoutParams lp = new AbsListView.LayoutParams(width/5, (height - height/10)/2);
			 v.setPadding(20, 20, 20, 20);
			 v.setLayoutParams(lp);
			 convertView = v;
		
		return convertView;
		}
		
		
	}

}
