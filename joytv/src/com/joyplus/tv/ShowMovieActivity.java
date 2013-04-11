package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

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
	private BaseAdapter adapter;

	private View floatView;

	private int popWidth, popHeight;

	private boolean isGridViewUp = false;
//			isGridViewDown = false;

	private int[] beforeFirstAndLastVible = { 0, 9 };

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
		// movieGv.set

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

		floatView = findViewById(R.id.inclue_movie_show_item);

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

				if (keyCode == KEY_UP) {

					isGridViewUp = true;
//					isGridViewDown = false;
				} else if (keyCode == KEY_DOWN) {

					isGridViewUp = false;
//					isGridViewDown = true;
				}
				if (action == KeyEvent.ACTION_UP) {
					if (keyCode == KEY_RIGHT) {

						turnToGridViewState();
					} 
//					else if (keyCode == KEY_UP) {
//
//						isGridViewUp = true;
////						isGridViewDown = false;
//					} else if (keyCode == KEY_DOWN) {
//
//						isGridViewUp = false;
////						isGridViewDown = true;
//					}
					if (!isSelectedItem) {

						if (keyCode == KEY_RIGHT) {
							isSelectedItem = true;
							movieGv.setSelection(1);
						} else if (keyCode == KEY_DOWN) {
							isSelectedItem = true;
							movieGv.setSelection(5);

						}
					}

				}
				return false;
			}
		});

		movieGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				startActivity(new Intent(ShowMovieActivity.this,
						ShowXiangqingMovie.class));
			}
		});

		movieGv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				// if (BuildConfig.DEBUG)
				// Log.i(TAG, "Positon:" + position);

				if (view == null) {

					isSelectedItem = false;
					return;
				}

				final float x = view.getX();
				final float y = view.getY();

				boolean isSmoonthScroll = false;

				boolean isSameContent = position >= beforeFirstAndLastVible[0]
						&& position <= beforeFirstAndLastVible[1];
				if (position >= 5 && !isSameContent) {

					if (beforepostion >= beforeFirstAndLastVible[0]
							&& beforepostion <= beforeFirstAndLastVible[0] + 4) {

						if (isGridViewUp) {

							movieGv.smoothScrollBy(-popHeight, 1000);
							isSmoonthScroll = true;
							// movieGv.requestLayout();

							TranslateAnimation translateAnimation = new TranslateAnimation(
									x, x, y, y + popHeight);
							translateAnimation.setDuration(1000);
							translateAnimation.setFillAfter(false);
//							floatView.layout((int) x, (int) y,
//									(int) (x + popWidth),
//									(int) (y + + popHeight + popHeight));
							floatView.startAnimation(translateAnimation);
						}
					} else {

						if (!isGridViewUp) {

							movieGv.smoothScrollBy(popHeight, 1000*2);
//							movieGv.scrollBy(x, y)
							// movieGv.requestLayout();
							isSmoonthScroll = true;
							
							floatView.layout((int) x, (int) y,
									(int) (x + popWidth),
									(int) (y + popHeight));
							TranslateAnimation translateAnimation;
							
//							floatView.layout((int) x, (int) y,
//							(int) (x + popWidth),
//							(int) (y + + popHeight + popHeight));
							int jianYingHeight = (int) (y- popHeight);
							if(jianYingHeight < 0) {
								
								jianYingHeight = (int) y;
							}
							translateAnimation = new TranslateAnimation(
									x, x, y, y  - 2 * popHeight - jianYingHeight);
							translateAnimation.setDuration(1000);
							translateAnimation.setFillAfter(true);
							floatView.startAnimation(translateAnimation);
						}
					}

				}

				if (!isSmoonthScroll) {// 没有强行拖动时候的动画效果

					ScaleAnimation outScaleAnimation = new ScaleAnimation(1.0f,
							0.8f, 1.0f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);

					outScaleAnimation.setDuration(80);
					outScaleAnimation.setFillAfter(false);

					outScaleAnimation
							.setAnimationListener(new Animation.AnimationListener() {

								@Override
								public void onAnimationStart(Animation animation) {
									// TODO Auto-generated method stub
								}

								@Override
								public void onAnimationRepeat(
										Animation animation) {
									// TODO Auto-generated method stub

								}

								@Override
								public void onAnimationEnd(Animation animation) {
									// TODO Auto-generated method stub
									ScaleAnimation inScaleAnimation = new ScaleAnimation(
											0.8f, 1.0f, 0.8f, 1.0f,
											Animation.RELATIVE_TO_SELF, 0.5f,
											Animation.RELATIVE_TO_SELF, 0.5f);
									inScaleAnimation.setDuration(80);
									inScaleAnimation.setFillAfter(false);

									floatView.layout((int) x, (int) y,
											(int) (x + popWidth),
											(int) (y + popHeight));

									floatView.setPadding(10, 10, 10, 10);
									floatView.setBackgroundColor(getResources()
											.getColor(R.color.text_active));
									floatView.startAnimation(inScaleAnimation);
								}
							});
					floatView.startAnimation(outScaleAnimation);

				}

				if (y == 0 || y - popHeight == 0) {// 顶部没有渐影

					if (!isSmoonthScroll) {

						beforeFirstAndLastVible[0] = movieGv
								.getFirstVisiblePosition();
						beforeFirstAndLastVible[1] = movieGv
								.getFirstVisiblePosition() + 9;
					} else {

						beforeFirstAndLastVible[0] = movieGv
								.getFirstVisiblePosition() - 5;
						beforeFirstAndLastVible[1] = movieGv
								.getFirstVisiblePosition() + 9 - 5;
					}

				} else {// 顶部有渐影

					if (!isSmoonthScroll) {

						beforeFirstAndLastVible[0] = movieGv
								.getLastVisiblePosition() - 9;
						beforeFirstAndLastVible[1] = movieGv
								.getLastVisiblePosition();
					} else {

						beforeFirstAndLastVible[0] = movieGv
								.getLastVisiblePosition() - 9 + 5;
						beforeFirstAndLastVible[1] = movieGv
								.getLastVisiblePosition() + 5;
					}

				}

				beforepostion = position;

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

				isSelectedItem = false;
			}
		});

		addListener();

	}

	private int beforepostion = 0;

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
		// movieGv.setNextFocusLeftId(R.id.bt_quanbufenlei);// 网格向左 全部分类获得焦点
		// movieGv.setNextFocusDownId(R.id.bt_quanbufenlei);// 网格向左 全部分类获得焦点
		// movieGv.setNextFocusUpId(R.id.bt_quanbufenlei);// 网格向左 全部分类获得焦点
		// movieGv.setNextFocusRightId(R.id.bt_quanbufenlei);// 网格向左 全部分类获得焦点

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

	private class MovieAdpter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			// return dataList.size();
			// return dataList.size();
			return 50;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			// return dataList.get(position);
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

			// int viewH = (int) (height / (1.0f * (2 + 0.10))) + 5;
			// int viewW = (int) (viewH * 1.0f / 370 * 264) - 24;
			int width = parent.getWidth() / 5;
			int height = (int) (width / 1.0f / 264 * 370);

			if (convertView == null) {
				View view = getLayoutInflater().inflate(
						R.layout.show_item_layout_dianying, null);
				v = view;
			} else {

				v = convertView;
			}
			AbsListView.LayoutParams params = new AbsListView.LayoutParams(
					width, height);
			v.setLayoutParams(params);

			TextView tv = (TextView) v.findViewById(R.id.tv_item_layout_name);
			tv.setText("" + position);

			v.setPadding(10, 10, 10, 10);

			if (width != 0) {

				popWidth = width;
				popHeight = height;
				// Log.i(TAG, "Width:" + popWidth);
			}

			if (position == 0 && !isSelectedItem && width != 0) {

				FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(
						popWidth, popHeight);
				floatView.setLayoutParams(params2);
				floatView.setX(movieGv.getX());
				floatView.setY(movieGv.getY());
				floatView.setPadding(10, 10, 10, 10);
				floatView.setBackgroundColor(getResources().getColor(
						R.color.text_active));
				isSelectedItem = true;
			}

			// aq = new AQuery(v);
			// aq.id(R.id.iv_item_layout_haibao).image(dataList.get(position).getPic_url());
			return v;
		}

	}

}
