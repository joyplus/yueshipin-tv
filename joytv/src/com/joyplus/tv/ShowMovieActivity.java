package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Config;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.tv.Adapters.MovieBangDanData;
import com.joyplus.tv.Service.Return.ReturnTops;
import com.joyplus.tv.ui.MyMovieGridView;

public class ShowMovieActivity extends Activity implements View.OnKeyListener,
		MyKeyEventKey, View.OnClickListener {

	private String TAG = "ShowMovieActivity";
	private AQuery aq;
	private App app;

	private EditText searchEt;
	private MyMovieGridView movieGv;
	private LinearLayout dongzuoLL, lunliLL, xijuLL, aiqingLL, xuanyiLL,
			kongbuLL;

	private Button zuijinguankanBtn, zhuijushoucangBtn, lixianshipinBtn,
			mFenLeiBtn;

	private View beforeView, activeView;

	private boolean isSelectedItem = true;// GridView中参数是否真正初始化
	private boolean isFirstHiddenFirstItem = false;
	private BaseAdapter adapter;

	private PopupWindow popupWindow;
	private View popupView;
	private int beforePostion = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_movie);

		app = (App) getApplication();
		aq = new AQuery(this);

		initView();
		initState();

		// getServiceData();
		adapter = new MovieAdpter();
		movieGv.setAdapter(adapter);// 网格布局添加适配器

	}

	private void initView() {

		searchEt = (EditText) findViewById(R.id.et_search);
		mFenLeiBtn = (Button) findViewById(R.id.bt_quanbufenlei);
		movieGv = (MyMovieGridView) findViewById(R.id.gv_movie_show);

		dongzuoLL = (LinearLayout) findViewById(R.id.ll_dongzuopian);
		lunliLL = (LinearLayout) findViewById(R.id.ll_lunlipian);
		xijuLL = (LinearLayout) findViewById(R.id.ll_xijupian);
		aiqingLL = (LinearLayout) findViewById(R.id.ll_aiqingpian);
		xuanyiLL = (LinearLayout) findViewById(R.id.ll_xuanyipian);
		kongbuLL = (LinearLayout) findViewById(R.id.ll_kongbupian);

		zuijinguankanBtn = (Button) findViewById(R.id.bt_zuijinguankan);
		zhuijushoucangBtn = (Button) findViewById(R.id.bt_zhuijushoucang);
		lixianshipinBtn = (Button) findViewById(R.id.bt_lixianshipin);

		searchEt.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();
				if (action == KeyEvent.ACTION_UP) {
					if (keyCode == KEY_UP) {

						turnToGridViewState();
					}
				}
				return false;
			}
		});
		movieGv.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();

				if (action == KeyEvent.ACTION_UP) {
					if (keyCode == KEY_RIGHT) {

						turnToGridViewState();
					}
					if (keyCode == KEY_RIGHT && !isSelectedItem) {
						isSelectedItem = true;
						movieGv.setSelection(1);
					} else if (keyCode == KEY_DOWN && !isSelectedItem) {
						isSelectedItem = true;
						movieGv.setSelection(5);

					}else if(keyCode == KEY_UP) {
						
						
					}
				}
				return false;
			}
		});

		movieGv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				// startActivity(new Intent(ShowMovieActivity.this,
				// ShowXiangqingTV.class));
				int visiblePosition = movieGv.getFirstVisiblePosition();
				if (BuildConfig.DEBUG)
					Log.i(TAG, "Positon:" + position + " visiblePosition:" + visiblePosition);

				int firstVisiblePostion = movieGv.getFirstVisiblePosition();
				// if(position - firstVisiblePostion >= 10 && position -
				// firstVisiblePostion <= 14) {
				// // adapter.notifyDataSetChanged();
				// // movieGv.setSelection(position);
				// movieGv.smoothScrollToPositionFromTop(position, 10, 1000);
				// }

				if (view == null) {

					isSelectedItem = false;
					return;
				}
				
				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				}

				if (position == 0) {


					if (!popupWindow.isShowing()) {

						popupWindow.setWidth(popWidth);
						popupWindow.setHeight(popHeight);
						int[] location = new int[2];
						movieGv.getLocationOnScreen(location);
						popupWindow.showAtLocation(movieGv, Gravity.NO_GRAVITY,
								location[0], location[1]);
					}
				} else {

					if (!popupWindow.isShowing()) {

						popupWindow.setWidth(popWidth);
						popupWindow.setHeight(popHeight);
						int[] location = new int[2];

						int quyu = position % 5;
						view.getLocationOnScreen(location);
						if (quyu == 0&&position >= 15) {

							int[] location2 = new int[2];
							movieGv.getLocationInWindow(location2);
							int height = movieGv.getHeight();
							int jianyingHeight = height - 2 * popHeight;


							if(visiblePosition < position) {
								
//							movieGv.
							} else {
								
								
							}
//							if(beforePostion < position) {
//								if (position != visiblePosition + 5) {
//
//									location2[1] = location2[1] + height
//											- popHeight;
//								} else{
//
//									location2[1] = location2[1]
//											+ jianyingHeight;
//								}
//							} else if(beforePostion > position){
//								
//								
//							}

								location = location2;

						}
						popupWindow.showAtLocation(view, Gravity.NO_GRAVITY,
								location[0], location[1]);
					} else {

						popupWindow.dismiss();
					}

				}

				beforeViewGv = view;
				beforePostion = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

				isSelectedItem = false;
			}
		});

		addListener();
		initPopupWindow();

	}

	private View beforeViewGv;

	private void addListener() {

		dongzuoLL.setOnKeyListener(this);
		lunliLL.setOnKeyListener(this);
		xijuLL.setOnKeyListener(this);
		aiqingLL.setOnKeyListener(this);
		xuanyiLL.setOnKeyListener(this);
		kongbuLL.setOnKeyListener(this);

		zuijinguankanBtn.setOnKeyListener(this);
		zhuijushoucangBtn.setOnKeyListener(this);
		lixianshipinBtn.setOnKeyListener(this);
		mFenLeiBtn.setOnKeyListener(this);

		dongzuoLL.setOnClickListener(this);
		lunliLL.setOnClickListener(this);
		xijuLL.setOnClickListener(this);
		aiqingLL.setOnClickListener(this);
		xuanyiLL.setOnClickListener(this);
		kongbuLL.setOnClickListener(this);

		zuijinguankanBtn.setOnClickListener(this);
		zhuijushoucangBtn.setOnClickListener(this);
		lixianshipinBtn.setOnClickListener(this);
		mFenLeiBtn.setOnClickListener(this);
	}

	private void initState() {

		beforeView = mFenLeiBtn;
		activeView = mFenLeiBtn;

		searchEt.setFocusable(false);// 搜索焦点消失
		movieGv.setNextFocusLeftId(R.id.bt_quanbufenlei);// 网格向左 全部分类获得焦点
		movieGv.setNextFocusDownId(R.id.bt_quanbufenlei);// 网格向左 全部分类获得焦点
		movieGv.setNextFocusUpId(R.id.bt_quanbufenlei);// 网格向左 全部分类获得焦点
		movieGv.setNextFocusRightId(R.id.bt_quanbufenlei);// 网格向左 全部分类获得焦点

		mFenLeiBtn.setTextColor(getResources().getColor(R.color.text_active));// 全部分类首先设为激活状态
		mFenLeiBtn.setBackgroundResource(R.drawable.menubg);// 在换成这张图片时，会刷新组件的padding
		dongzuoLL.setPadding(0, 0, 5, 0);
		lunliLL.setPadding(0, 0, 5, 0);
		xijuLL.setPadding(0, 0, 5, 0);
		aiqingLL.setPadding(0, 0, 5, 0);
		xuanyiLL.setPadding(0, 0, 5, 0);
		kongbuLL.setPadding(0, 0, 5, 0);
		zuijinguankanBtn.setPadding(0, 0, 5, 0);
		zhuijushoucangBtn.setPadding(0, 0, 5, 0);
		lixianshipinBtn.setPadding(0, 0, 5, 0);
		mFenLeiBtn.setPadding(0, 0, 5, 0);

		// movieGv.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		// movieGv.setFocusable(true);
		// movieGv.setFocusableInTouchMode(true);

	}

	private void initPopupWindow() {

		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		popupView = inflater.inflate(R.layout.show_item_layout_active, null);
		popupView.setPadding(10, 10, 10, 10);
		popupWindow = new PopupWindow(popupView);

		popupView.setBackgroundColor(getResources().getColor(
				R.color.text_active));
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		movieGv.setSelected(true);
		movieGv.requestFocus();

	}

	private void beforeViewFoucsStateBack() {

		if (beforeView instanceof LinearLayout) {

			LinearLayout tempLinearLayout = (LinearLayout) beforeView;
			if (beforeView.getId() == activeView.getId()) {
				linearLayoutToActiveState(tempLinearLayout);
			} else {
				linearLayoutToPTState(tempLinearLayout);
			}
		} else if (beforeView instanceof Button) {

			Button tempButton = (Button) beforeView;
			if (beforeView.getId() == activeView.getId()) {
				buttonToActiveState(tempButton);
			} else {
				buttonToPTState(tempButton);
			}
		}
	}

	private void beforeViewActiveStateBack() {
		if (activeView instanceof LinearLayout) {

			LinearLayout tempLinearLayout = (LinearLayout) activeView;
			linearLayoutToPTState(tempLinearLayout);
		} else if (activeView instanceof Button) {

			Button tempButton = (Button) activeView;
			buttonToPTState(tempButton);
		}
	}

	// 转到类似Gridview组件上
	private void turnToGridViewState() {

		if (beforeView.getId() == activeView.getId()) {

			if (activeView instanceof LinearLayout) {

				LinearLayout tempLinearLayout = (LinearLayout) activeView;
				linearLayoutToActiveState(tempLinearLayout);
			} else if (activeView instanceof Button) {

				Button tempButton = (Button) activeView;
				buttonToActiveState(tempButton);
			}
		} else {
			beforeViewFoucsStateBack();
		}

	}

	private void linearLayoutToPTState(LinearLayout linearLayout) {

		Button tempButton = (Button) linearLayout.getChildAt(0);
		linearLayout.setBackgroundResource(R.drawable.text_drawable_selector);
		// tempButton.setTextColor(getResources().getColor(R.color.text_pt));
		tempButton.setTextColor(getResources().getColorStateList(
				R.color.text_color_selector));
		tempButton.setCompoundDrawablesWithIntrinsicBounds(getResources()
				.getDrawable(R.drawable.side_hot_normal), null, null, null);
	}

	private void buttonToPTState(Button button) {

		button.setBackgroundResource(R.drawable.text_drawable_selector);
		// button.setTextColor(getResources().getColor(R.color.text_pt));
		button.setTextColor(getResources().getColorStateList(
				R.color.text_color_selector));
	}

	private void linearLayoutToActiveState(LinearLayout linearLayout) {

		Button tempButton = (Button) linearLayout.getChildAt(0);
		linearLayout.setBackgroundResource(R.drawable.menubg);
		linearLayout.setPadding(0, 0, 5, 0);
		tempButton.setTextColor(getResources().getColor(R.color.text_active));
		tempButton.setCompoundDrawablesWithIntrinsicBounds(getResources()
				.getDrawable(R.drawable.side_hot_active), null, null, null);
	}

	private void buttonToActiveState(Button button) {

		button.setBackgroundResource(R.drawable.menubg);
		button.setPadding(0, 0, 5, 0);
		button.setTextColor(getResources().getColor(R.color.text_active));
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		if (action == KeyEvent.ACTION_UP) {

			v.setOnClickListener(null);

			if (v instanceof LinearLayout) {
				LinearLayout linearLayout = (LinearLayout) v;
				Button button = (Button) linearLayout.getChildAt(0);

				if (keyCode == KEY_UP || keyCode == KEY_LEFT
						|| keyCode == KEY_DOWN) {
					beforeViewFoucsStateBack();
					button.setTextColor(getResources().getColor(
							R.color.text_foucs));
					button.setCompoundDrawablesWithIntrinsicBounds(
							getResources().getDrawable(
									R.drawable.side_hot_active), null, null,
							null);
				}
			} else if (v instanceof Button) {
				Button button = (Button) v;
				if ((keyCode == KEY_UP || keyCode == KEY_LEFT || keyCode == KEY_DOWN)) {
					searchEt.setFocusable(true);// 能够获取焦点
					beforeViewFoucsStateBack();
					button.setTextColor(getResources().getColor(
							R.color.text_foucs));
					button.setBackgroundResource(R.drawable.text_drawable_selector);
				}
			}

			v.setOnClickListener(this);
		}
		beforeView = v;
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// Log.i("Yangzhg", "onClick");

		v.setOnKeyListener(null);
		if (v instanceof LinearLayout) {
			LinearLayout linearLayout = (LinearLayout) v;
			if (v.getId() != activeView.getId()) {
				beforeViewActiveStateBack();
				linearLayoutToActiveState(linearLayout);
				activeView = v;
			}
		} else if (v instanceof Button) {
			Button button = (Button) v;
			if (v.getId() != activeView.getId()) {
				beforeViewActiveStateBack();
				buttonToActiveState(button);
				activeView = v;
			}
		}
		v.setOnKeyListener(this);
	}

	public void getServiceData() {
		String url = Constant.BASE_URL + "tops"
				+ "?page_num=1&page_size=50&topic_type=1";
		// String url = Constant.BASE_URL + "tv_net_top"
		// +"?page_num=1&page_size=1000";
		// String url =
		// "http://apitest.yue001.com/joyplus-service/index.php/tv_net_top?app_key=ijoyplus_android_0001bj&page_num=1&page_size=1000";
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, "initListData");

		HashMap<String, String> headers = new HashMap<String, String>();
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

		headers.put("app_key", Constant.APPKEY);
		headers.put("client", "android");
		app.setHeaders(headers);
		cb.SetHeader(app.getHeaders());

		aq.ajax(cb);

	}

	private ReturnTops m_ReturnTops = null;

	// private ReturnMainHot m_ReturnTops = null;

	// 初始化list数据函数
	public void initListData(String url, JSONObject json, AjaxStatus status) {
		Log.i(TAG, "initListData:" + status.getCode());
		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {
			return;
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			m_ReturnTops = mapper.readValue(json.toString(), ReturnTops.class);
			// m_ReturnTops = mapper.readValue(json.toString(),
			// ReturnMainHot.class);
			if (BuildConfig.DEBUG)
				Log.i(TAG, "initListData:" + json.toString());
			// if(BuildConfig.DEBUG) Log.i(TAG, "initListData:" +
			// m_ReturnTops.tops.length);
			// if (m_ReturnTops.tops.length > 0)
			// app.SaveServiceData("movie_tops", json.toString());

			// 创建数据源对象
			getVideoMovies();

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

	private List<MovieBangDanData> dataList = new ArrayList<MovieBangDanData>();

	public void getVideoMovies() {

		if (m_ReturnTops.tops == null)
			return;
		for (int i = 0; i < m_ReturnTops.tops.length; i++) {
			MovieBangDanData movieBangDanData = new MovieBangDanData();
			movieBangDanData.setPic_ID(m_ReturnTops.tops[i].id);
			movieBangDanData.setPic_url(m_ReturnTops.tops[i].pic_url);
			movieBangDanData.setPic_name(m_ReturnTops.tops[i].name);
			movieBangDanData.setRight(m_ReturnTops.tops[i].prod_type);
			// if (m_ReturnTops.tops[i].items != null) {
			// int length = m_ReturnTops.tops[i].items.length;
			// String[] strs = new String[length];
			// for (int j = 0; j < length; j++) {
			// strs[i] = m_ReturnTops.tops[i].items[j].prod_name;
			// }
			// movieBangDanData.setPic_lists(strs);
			//
			// }
			dataList.add(movieBangDanData);
		}
		if (BuildConfig.DEBUG)
			Log.i(TAG, dataList.size() + ":size");
		adapter.notifyDataSetChanged();
		// movieGv.setSelection(10);
		// for(int i=0;i<dataList.size();i++) {
		// if(BuildConfig.DEBUG) Log.i(TAG, dataList.get(i) + "");
		// }

	}

	private int popWidth, popHeight;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (!popupWindow.isShowing()) {

				int width = msg.arg1;
				int viewH2 = msg.arg2;
				popWidth = width / 5;
				popHeight = viewH2;
				popupWindow.setWidth(popWidth);
				popupWindow.setHeight(popHeight);
				int[] location = new int[2];
				movieGv.getLocationOnScreen(location);
				popupWindow.showAtLocation(movieGv, Gravity.NO_GRAVITY,
						location[0], location[1]);
			}
		}

	};

	private class MovieAdpter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			// return dataList.size();
			return 30;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			// return dataList.get(position);
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

			LinearLayout parentLayout = (LinearLayout) findViewById(R.id.ll_movie_show);
			int height = parentLayout.getHeight();
			int width = parent.getWidth();
			// int viewH = (int) (height / (1.0f * (2 + 0.10))) + 5;
			// int viewW = (int) (viewH * 1.0f / 370 * 264) - 24;
			int viewH2 = (int) (width / 5 * 1.0f / 264 * 370);

			if (convertView == null) {
				View view = getLayoutInflater().inflate(
						R.layout.show_item_ani_dianying, null);
				AbsListView.LayoutParams params = new AbsListView.LayoutParams(
						width / 5, viewH2);

				view.setLayoutParams(params);
				// view.setPadding(10, 10, 10, 10);
				v = view;
			} else {

				v = convertView;
				AbsListView.LayoutParams params = new AbsListView.LayoutParams(
						width / 5, viewH2);
				v.setLayoutParams(params);
			}

			v.setPadding(10, 10, 10, 10);

			if (position == 0 && !isSelectedItem && !isFirstHiddenFirstItem
					&& width != 0) {

				// View frameLayout =
				// v.findViewById(R.id.include_item_ani_active);
				// View frameLayoutNormal =
				// v.findViewById(R.id.include_item_ani_normal);
				// frameLayout.setVisibility(View.VISIBLE);
				// frameLayoutNormal.setVisibility(View.INVISIBLE);
				// v.setBackgroundColor(getResources().getColor(R.color.text_active));
				Message msg = new Message();

				msg.arg1 = width;
				msg.arg2 = viewH2;
				msg.what = 10021;
				handler.sendMessage(msg);
				isSelectedItem = true;
				// beforeViewGv = v;
			}

			// aq = new AQuery(v);
			// aq.id(R.id.iv_item_layout_haibao).image(dataList.get(position).getPic_url());
			return v;
		}

	}

}
