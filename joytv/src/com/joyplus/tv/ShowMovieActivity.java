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
import android.util.DisplayMetrics;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.tv.Adapters.MainHotItemAdapter;
import com.joyplus.tv.Adapters.MovieBangDanData;
import com.joyplus.tv.Service.Return.ReturnMainHot;
import com.joyplus.tv.Service.Return.ReturnTVBangDanList;
import com.joyplus.tv.Service.Return.ReturnTops;
import com.joyplus.tv.entity.HotItemInfo;
import com.joyplus.tv.entity.MovieItemData;
import com.joyplus.tv.ui.MyMovieGridView;

public class ShowMovieActivity extends Activity implements View.OnKeyListener,
		MyKeyEventKey, BangDanKey, View.OnClickListener {

	private String TAG = "ShowMovieActivity";
	private AQuery aq;
	private App app;

	private EditText searchEt;
	private MyMovieGridView movieGv;
	private LinearLayout dongzuoLL, kehuanLL, lunliLL, xijuLL, aiqingLL,
			xuanyiLL, kongbuLL, donghuaLL;

	private Button zuijinguankanBtn, zhuijushoucangBtn, lixianshipinBtn,
			mFenLeiBtn;
	
	private View firstFloatView ;

	private View beforeView, activeView;

	private boolean isSelectedItem = true;// GridView中参数是否真正初始化

	private int popWidth, popHeight;

	private boolean isGridViewUp = false;

	private int[] beforeFirstAndLastVible = { 0, 9 };

	private View beforeGvView = null;

	private ObjectMapper mapper = new ObjectMapper();

	private List<MovieItemData> movieList = new ArrayList<MovieItemData>();

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
		String url = StatisticsUtils.getTopItemURL(TOP_ITEM_URL, 
		TV_DIANYING, 1 + "", 50 + "");
		getServiceData(url);// 进入电影界面时，全部分类电影显示获取焦点，并且显示数据
//		movieGv.setAdapter(movieAdapter);// 网格布局添加适配器
		movieGv.setAdapter(movieAdapter);
		movieGv.setSelected(true);
		movieGv.requestFocus();
		movieGv.setSelection(0);
		
		DisplayMetrics dm = new DisplayMetrics();

		getWindowManager().getDefaultDisplay().getMetrics(dm);

//		int width = dm.widthPixels;
//
//		int height = dm.heightPixels;
//		
//		app.MyToast(aq.getContext(),"Screen-->Width: " + width + " height:" + height);

	}

	private void initView() {

		searchEt = (EditText) findViewById(R.id.et_search);
		mFenLeiBtn = (Button) findViewById(R.id.bt_quanbufenlei);
		movieGv = (MyMovieGridView) findViewById(R.id.gv_movie_show);

		dongzuoLL = (LinearLayout) findViewById(R.id.ll_dongzuopian);
		kehuanLL = (LinearLayout) findViewById(R.id.ll_kehuanpian);
		lunliLL = (LinearLayout) findViewById(R.id.ll_lunlipian);
		xijuLL = (LinearLayout) findViewById(R.id.ll_xijupian);
		aiqingLL = (LinearLayout) findViewById(R.id.ll_aiqingpian);
		xuanyiLL = (LinearLayout) findViewById(R.id.ll_xuanyipian);
		kongbuLL = (LinearLayout) findViewById(R.id.ll_kongbupian);
		donghuaLL = (LinearLayout) findViewById(R.id.ll_donghuapian);

		zuijinguankanBtn = (Button) findViewById(R.id.bt_zuijinguankan);
		zhuijushoucangBtn = (Button) findViewById(R.id.bt_zhuijushoucang);
		lixianshipinBtn = (Button) findViewById(R.id.bt_lixianshipin);
		
		firstFloatView = findViewById(R.id.inclue_movie_show_item);

		addListener();

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
		kehuanLL.setPadding(0, 0, 5, 0);
		lunliLL.setPadding(0, 0, 5, 0);
		xijuLL.setPadding(0, 0, 5, 0);
		aiqingLL.setPadding(0, 0, 5, 0);
		xuanyiLL.setPadding(0, 0, 5, 0);
		kongbuLL.setPadding(0, 0, 5, 0);
		donghuaLL.setPadding(0, 0, 5, 0);
		zuijinguankanBtn.setPadding(0, 0, 5, 0);
		zhuijushoucangBtn.setPadding(0, 0, 5, 0);
		lixianshipinBtn.setPadding(0, 0, 5, 0);
		mFenLeiBtn.setPadding(0, 0, 5, 0);

	}

	private int beforepostion = 0;

	private void addListener() {

		dongzuoLL.setOnKeyListener(this);
		kehuanLL.setOnKeyListener(this);
		lunliLL.setOnKeyListener(this);
		xijuLL.setOnKeyListener(this);
		aiqingLL.setOnKeyListener(this);
		xuanyiLL.setOnKeyListener(this);
		kongbuLL.setOnKeyListener(this);
		donghuaLL.setOnKeyListener(this);

		zuijinguankanBtn.setOnKeyListener(this);
		zhuijushoucangBtn.setOnKeyListener(this);
		lixianshipinBtn.setOnKeyListener(this);
		mFenLeiBtn.setOnKeyListener(this);

		dongzuoLL.setOnClickListener(this);
		kehuanLL.setOnClickListener(this);
		lunliLL.setOnClickListener(this);
		xijuLL.setOnClickListener(this);
		aiqingLL.setOnClickListener(this);
		xuanyiLL.setOnClickListener(this);
		kongbuLL.setOnClickListener(this);
		donghuaLL.setOnClickListener(this);

		zuijinguankanBtn.setOnClickListener(this);
		zhuijushoucangBtn.setOnClickListener(this);
		lixianshipinBtn.setOnClickListener(this);
		mFenLeiBtn.setOnClickListener(this);
		
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
					// isGridViewDown = false;
				} else if (keyCode == KEY_DOWN) {

					isGridViewUp = false;
					// isGridViewDown = true;
				}
				if (action == KeyEvent.ACTION_UP) {
					if (keyCode == KEY_RIGHT) {

						turnToGridViewState();
					}
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
				Intent intent = new Intent(ShowMovieActivity.this,
						ShowXiangqingMovie.class);
				intent.putExtra("ID", movieList.get(position).getMovieID());
				startActivity(intent);
			}
		});

		movieGv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, final View view,
					int position, long id) {
				// TODO Auto-generated method stub
				// if (BuildConfig.DEBUG)
				Log.i(TAG, "Positon:" + position + " View:" + view + 
						" beforGvView:" + beforeGvView );

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
						}
					} else {

						if (!isGridViewUp) {

							movieGv.smoothScrollBy(popHeight, 1000 * 2);
							isSmoonthScroll = true;

						}
					}

				}

				// if (!isSmoonthScroll) {// 没有强行拖动时候的动画效果

				if (beforeGvView != null) {

					ImageView iv = (ImageView) beforeGvView
							.findViewById(R.id.item_layout_dianying_reflact);
					iv.setVisibility(View.VISIBLE);
					beforeGvView.setBackgroundColor(getResources().getColor(
							android.R.color.transparent));
					ScaleAnimation outScaleAnimation = new ScaleAnimation(1.0f,
							0.8f, 1.0f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);

					outScaleAnimation.setDuration(80);
					outScaleAnimation.setFillAfter(false);
					beforeGvView.startAnimation(outScaleAnimation);
					
					
					ImageView iv2 = (ImageView) view
							.findViewById(R.id.item_layout_dianying_reflact);
					iv2.setVisibility(View.GONE);
					ScaleAnimation inScaleAnimation = new ScaleAnimation(
							0.8f, 1.0f, 0.8f, 1.0f,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					inScaleAnimation.setDuration(80);
					inScaleAnimation.setFillAfter(false);

					view.setPadding(10, 10, 10, 10);
					view.setBackgroundColor(getResources()
							.getColor(R.color.text_active));
					view.startAnimation(inScaleAnimation);

				} else {
					
					ScaleAnimation outScaleAnimation = new ScaleAnimation(0.8f,
							1.0f, 0.8f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);

					outScaleAnimation.setDuration(80);
					outScaleAnimation.setFillAfter(false);
					firstFloatView.startAnimation(outScaleAnimation);
					
					firstFloatView.setVisibility(View.GONE);
					ImageView iv = (ImageView) view
							.findViewById(R.id.item_layout_dianying_reflact);
					iv.setVisibility(View.GONE);
					ScaleAnimation inScaleAnimation = new ScaleAnimation(
							0.8f, 1.0f, 0.8f, 1.0f,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					inScaleAnimation.setDuration(80);
					inScaleAnimation.setFillAfter(false);

					view.setPadding(10, 10, 10, 10);
					view.setBackgroundColor(getResources()
							.getColor(R.color.text_active));
					view.startAnimation(inScaleAnimation);
				}
//				 }

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

				beforeGvView = view;
				beforepostion = position;

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

				isSelectedItem = false;
			}
		});

		movieGv.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub

				if (!hasFocus) {// 如果gridview没有获取焦点，把item中高亮取消

					ScaleAnimation outScaleAnimation = new ScaleAnimation(
							1.0f, 0.8f, 1.0f, 0.8f,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);

					outScaleAnimation.setDuration(80);
					outScaleAnimation.setFillAfter(false);
					if (beforeGvView != null) {
						ImageView iv = (ImageView) beforeGvView
								.findViewById(R.id.item_layout_dianying_reflact);
						iv.setVisibility(View.VISIBLE);
						beforeGvView.setBackgroundColor(getResources()
								.getColor(android.R.color.transparent));
						beforeGvView.startAnimation(outScaleAnimation);
					} else {
						
						firstFloatView.setVisibility(View.GONE);
					}
				} else {

					ScaleAnimation inScaleAnimation = new ScaleAnimation(
							0.8f, 1.0f, 0.8f, 1.0f,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					inScaleAnimation.setDuration(80);
					inScaleAnimation.setFillAfter(false);
					if (beforeGvView != null) {

						ImageView iv = (ImageView) beforeGvView
								.findViewById(R.id.item_layout_dianying_reflact);
						iv.setVisibility(View.GONE);
						beforeGvView.setPadding(10, 10, 10, 10);
						beforeGvView.setBackgroundColor(getResources()
								.getColor(R.color.text_active));

						beforeGvView.startAnimation(inScaleAnimation);

					} else {
						initFirstFloatView();
					}
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

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
		tempButton.setTextColor(getResources().getColorStateList(
				R.color.text_color_selector));
		tempButton.setCompoundDrawablesWithIntrinsicBounds(getResources()
				.getDrawable(R.drawable.side_hot_normal), null, null, null);
	}

	private void buttonToPTState(Button button) {

		button.setBackgroundResource(R.drawable.text_drawable_selector);
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
		 Log.i("Yangzhg", "onClick");

		 
			if(activeView == null) {
				
				activeView = mFenLeiBtn;
			}
			
			if(activeView.getId() == v.getId()) {
				
				return;
			}
			
			switch (v.getId()) {
			case R.id.ll_dongzuopian:
				String url1 = StatisticsUtils.getTopItemURL(TOP_ITEM_URL, 
				REBO_DONGZUO_MOVIE, 1 + "", 50 + "");
				app.MyToast(aq.getContext(),"DONGZUO");
				getServiceData(url1);
				break;
			case R.id.ll_kehuanpian:
				String url2 = StatisticsUtils.getTopItemURL(TOP_ITEM_URL, 
				REBO_KEHUAN_MOVIE, 1 + "", 50 + "");
				app.MyToast(aq.getContext(),"ll_kehuanpian");
				getServiceData(url2);
				break;
			case R.id.ll_lunlipian:
				String url3 = StatisticsUtils.getTopItemURL(TOP_ITEM_URL, 
				REBO_LUNLI_MOVIE, 1 + "", 50 + "");
				app.MyToast(aq.getContext(),"ll_lunlipian");
				getServiceData(url3);
				break;
			case R.id.ll_xijupian:
				String url4 = StatisticsUtils.getTopItemURL(TOP_ITEM_URL, 
				REBO_XIJU_MOVIE, 1 + "", 50 + "");
				app.MyToast(aq.getContext(),"ll_xijupian");
				getServiceData(url4);
				break;
			case R.id.ll_aiqingpian:
				String url5 = StatisticsUtils.getTopItemURL(TOP_ITEM_URL, 
				REBO_KEHUAN_MOVIE, 1 + "", 50 + "");
				app.MyToast(aq.getContext(),"ll_aiqingpian");
				getServiceData(url5);
				break;
			case R.id.ll_xuanyipian:
				String url6 = StatisticsUtils.getTopItemURL(TOP_ITEM_URL, 
				REBO_KEHUAN_MOVIE, 1 + "", 50 + "");
				app.MyToast(aq.getContext(),"ll_xuanyipian");
				getServiceData(url6);
				break;
			case R.id.ll_kongbupian:
				String url7 = StatisticsUtils.getTopItemURL(TOP_ITEM_URL, 
				REBO_KONGBU_MOVIE, 1 + "", 50 + "");
				app.MyToast(aq.getContext(),"ll_kongbupian");
				getServiceData(url7);
				break;
			case R.id.ll_donghuapian:
				String url8 = StatisticsUtils.getTopItemURL(TOP_ITEM_URL, 
				REBO_DONGHUA_MOVIE, 1 + "", 50 + "");
				app.MyToast(aq.getContext(),"ll_donghuapian");
				getServiceData(url8);
				break;
			case R.id.bt_quanbufenlei:
				String url9 = StatisticsUtils.getTopItemURL(TOP_ITEM_URL, 
						TV_DIANYING, 1 + "", 50 + "");
				app.MyToast(aq.getContext(),"bt_quanbufenlei");
				getServiceData(url9);
				break;
			case R.id.bt_zuijinguankan:
				startActivity(new Intent(this, HistoryActivity.class));
				break;
			default:
				break;
			}
		 
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
		beforeGvView = null;
		v.setOnKeyListener(this);
	}

	private void getServiceData(String url) {

		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, "initData");

		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}

	public void initData(String url, JSONObject json, AjaxStatus status) {

		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {

			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		try {
			Log.d(TAG, json.toString());
			ReturnTVBangDanList result = mapper.readValue(json.toString(),
					ReturnTVBangDanList.class);
			// hot_list.clear();
			if(movieList != null && !movieList.isEmpty()) {
				
				movieList.clear();
			}
			for (int i = 0; i < result.items.length; i++) {

				MovieItemData movieItemData = new MovieItemData();
				movieItemData.setMovieName(result.items[i].prod_name);
				movieItemData.setMoviePicUrl(result.items[i].prod_pic_url);
				movieItemData.setMovieScore(result.items[i].score);
				movieItemData.setMovieID(result.items[i].prod_id);
				movieList.add(movieItemData);
			}
			// Log.d

			movieAdapter.notifyDataSetChanged();
			beforeGvView = null;
			initFirstFloatView();
			movieGv.setFocusable(true);
			movieGv.setSelected(true);
			isSelectedItem = false;
			movieGv.requestFocus();
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
	
	private void  initFirstFloatView() {
		
		firstFloatView.setX(0);
		firstFloatView.setY(0);
		firstFloatView.setLayoutParams(new FrameLayout.LayoutParams(popWidth, popHeight));
		firstFloatView.setVisibility(View.VISIBLE);
		
//		ImageView iv = (ImageView) firstFloatView.findViewById(R.id.iv_item_layout_haibao);
		TextView movieName = (TextView) firstFloatView.findViewById(R.id.tv_item_layout_name);
		TextView movieScore = (TextView) firstFloatView.findViewById(R.id.tv_item_layout_score);
		aq = new AQuery(firstFloatView);
		aq.id(R.id.iv_item_layout_haibao).image(
				movieList.get(0).getMoviePicUrl());
		movieName.setText(movieList.get(0).getMovieName());
		movieScore.setText(movieList.get(0).getMovieScore());
		firstFloatView.setPadding(10, 10, 10, 10);
		firstFloatView.setBackgroundColor(getResources()
				.getColor(R.color.text_active));
		ScaleAnimation inScaleAnimation = new ScaleAnimation(
				0.8f, 1.0f, 0.8f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		inScaleAnimation.setDuration(80);
		inScaleAnimation.setFillAfter(false);
		
		firstFloatView.startAnimation(inScaleAnimation);
	}


	private BaseAdapter movieAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v;

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

			TextView movieName = (TextView) v
					.findViewById(R.id.tv_item_layout_name);
			movieName.setText(movieList.get(position).getMovieName());
			TextView movieScore = (TextView) v
					.findViewById(R.id.tv_item_layout_score);
			movieScore.setText(movieList.get(position).getMovieScore());
			v.setPadding(10, 10, 10, 10);

			if (width != 0) {

				popWidth = width;
				popHeight = height;
				// Log.i(TAG, "Width:" + popWidth);
			}

			aq = new AQuery(v);
			aq.id(R.id.iv_item_layout_haibao).image(
					movieList.get(position).getMoviePicUrl());
			return v;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return movieList.get(position);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return movieList.size();
		}
	};

}
