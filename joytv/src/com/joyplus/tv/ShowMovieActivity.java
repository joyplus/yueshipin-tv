package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.joyplus.tv.Adapters.MovieAdapter;
import com.joyplus.tv.entity.MovieItemData;
import com.joyplus.tv.ui.MyMovieGridView;
import com.joyplus.tv.ui.NavigateView;
import com.joyplus.tv.ui.NavigateView.OnResultListener;
import com.joyplus.tv.utils.ItemStateUtils;

public class ShowMovieActivity extends AbstractShowActivity {

	public static final String TAG = "ShowMovieActivity";
	
	private static final int DONGZUOPIAN = 4;
	private static final int KEHUANPIAN = 5;
	private static final int LUNLIPIAN = 6;
	private static final int XIJUPIAN = 7;
	private static final int AIQINGPIAN = 8;
	private static final int XUANYIPIAN = 9;
	private static final int KONGBUPIAN = 10;
	private static final int DONGHUAPIAN = 11;
	
	private AQuery aq;
	private App app;

	private EditText searchEt;
	private MyMovieGridView movieGv;
	private LinearLayout dongzuoLL, kehuanLL, lunliLL, xijuLL, aiqingLL,
			xuanyiLL, kongbuLL, donghuaLL;
	private Button zuijinguankanBtn, zhuijushoucangBtn, mFenLeiBtn;
	private LinearLayout topLinearLayout;

	private View firstFloatView;
	private View activeView;

	private boolean isSelectedItem = true;// GridView中参数是否真正初始化

	private int popWidth, popHeight;

	private boolean isGridViewUp = false;

	private int[] beforeFirstAndLastVible = { 0, 9 };

	private View beforeGvView = null;
	
	private List<MovieItemData>[] lists = new List[10];
	private boolean[] isNextPagePossibles = new boolean[10];
	private int[] pageNums = new int[10];
	
	private int currentListIndex;

	private PopupWindow popupWindow;

	private int beforepostion = 0;
	
	private MovieAdapter movieAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_movie);

		app = (App) getApplication();
		aq = new AQuery(this);
		
		initActivity();//初始化界面

		movieAdapter = new MovieAdapter(this,aq);
		movieGv.setAdapter(movieAdapter);

//		String url2 = StatisticsUtils.getTopItemURL(TOP_ITEM_URL,
//				TV_DIANYING, 1 + "", StatisticsUtils.CACHE_NUM + "");
		getQuan10Data(StatisticsUtils.getMovie_Quan10URL());

		movieGv.setSelected(true);
		movieGv.requestFocus();
		movieGv.setSelection(0);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub

		if (hasFocus) {

			ItemStateUtils.viewToFocusState(getApplicationContext(), v);
		} else {

			ItemStateUtils.viewToOutFocusState(getApplicationContext(), v,
					activeView);
		}

	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (aq != null)
			aq.dismiss();
		
		clearLists();
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		aq.id(R.id.iv_head_user_icon).image(app.getUserInfo().getUserAvatarUrl(),false,true,0,R.drawable.avatar);
		aq.id(R.id.tv_head_user_name).text(app.getUserInfo().getUserName());
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.i("Yangzhg", "onClick");

		if (activeView == null) {

			activeView = mFenLeiBtn;
		}

		if (v.getId() == R.id.bt_quanbufenlei) {

			if (popupWindow == null) {
				NavigateView view = new NavigateView(this);
				int[] location = new int[2];
				mFenLeiBtn.getLocationOnScreen(location);
				view.Init(
						getResources().getStringArray(
								R.array.diqu_dianying_fenlei),
						getResources().getStringArray(
								R.array.leixing_dianying_fenlei),
						getResources().getStringArray(
								R.array.shijian_dianying_fenlei), location[0],
						location[1], mFenLeiBtn.getWidth(), mFenLeiBtn
								.getHeight(), new OnResultListener() {

							@Override
							public void onResult(View v, boolean isBack,
									String[] choice) {
								// TODO Auto-generated method stub
								if (isBack) {
									popupWindow.dismiss();
								} else {
									if (popupWindow.isShowing()) {
										popupWindow.dismiss();
										Toast.makeText(
												ShowMovieActivity.this,
												"selected is " + choice[0]+ "," + choice[1] + ","
														+ choice[2],Toast.LENGTH_LONG).show();

										filterVideoSource(choice);
									}
								}
							}
						});
				view.setLayoutParams(new LayoutParams(0, 0));
				// popupWindow = new PopupWindow(view,
				// getWindowManager().getDefaultDisplay().getWidth(),
				// getWindowManager().getDefaultDisplay().getHeight(), true);
				int width = topLinearLayout.getWidth();
				int height = topLinearLayout.getHeight();
				popupWindow = new PopupWindow(view, width, height, true);
			}
			popupWindow.showAtLocation(mFenLeiBtn.getRootView(), Gravity.LEFT
					| Gravity.TOP, 0, 0);
		}

		if (activeView.getId() == v.getId()) {

			return;
		}

		switch (v.getId()) {
		case R.id.ll_dongzuopian:
			currentListIndex = DONGZUOPIAN;
			String url1 = StatisticsUtils.getMovie_DongzuoFirstURL();
			app.MyToast(aq.getContext(), "DONGZUO");
			if(lists[currentListIndex] != null && !lists[currentListIndex].isEmpty()) {
				
				notifyAdapter(lists[currentListIndex]);
			} else {
				
				getUnQuanbuData(url1);
			}
			break;
		case R.id.ll_kehuanpian:
			currentListIndex = KEHUANPIAN;
			String url2 = StatisticsUtils.getMovie_KehuanFirstURL();
			app.MyToast(aq.getContext(), "ll_kehuanpian");
			if(lists[currentListIndex] != null && !lists[currentListIndex].isEmpty()) {
				
				notifyAdapter(lists[currentListIndex]);
			} else {
				
				getUnQuanbuData(url2);
			}
			break;
		case R.id.ll_lunlipian:
			currentListIndex = LUNLIPIAN;
			String url3 = StatisticsUtils.getMovie_LunliFirstURL();
			app.MyToast(aq.getContext(), "ll_lunlipian");
			if(lists[currentListIndex] != null && !lists[currentListIndex].isEmpty()) {
				
				notifyAdapter(lists[currentListIndex]);
			} else {
				
				getUnQuanbuData(url3);
			}
			break;
		case R.id.ll_xijupian:
			currentListIndex = XIJUPIAN;
			String url4 = StatisticsUtils.getMovie_DongzuoFirstURL();
			app.MyToast(aq.getContext(), "ll_xijupian");
			if(lists[currentListIndex] != null && !lists[currentListIndex].isEmpty()) {
				
				notifyAdapter(lists[currentListIndex]);
			} else {
				
				getUnQuanbuData(url4);
			}
			break;
		case R.id.ll_aiqingpian:
			currentListIndex = AIQINGPIAN;
			String url5 = StatisticsUtils.getMovie_AiqingFirstURL();
			app.MyToast(aq.getContext(), "ll_aiqingpian");
			if(lists[currentListIndex] != null && !lists[currentListIndex].isEmpty()) {
				
				notifyAdapter(lists[currentListIndex]);
			} else {
				
				getUnQuanbuData(url5);
			}
			break;
		case R.id.ll_xuanyipian:
			currentListIndex = XUANYIPIAN;
			String url6 = StatisticsUtils.getMovie_XuanyiFirstURL();
			app.MyToast(aq.getContext(), "ll_xuanyipian");
			if(lists[currentListIndex] != null && !lists[currentListIndex].isEmpty()) {
				
				notifyAdapter(lists[currentListIndex]);
			} else {
				
				getUnQuanbuData(url6);
			}
			break;
		case R.id.ll_kongbupian:
			currentListIndex = KONGBUPIAN;
			String url7 = StatisticsUtils.getMovie_KongbuFirstURL();
			app.MyToast(aq.getContext(), "ll_kongbupian");
			if(lists[currentListIndex] != null && !lists[currentListIndex].isEmpty()) {
				
				notifyAdapter(lists[currentListIndex]);
			} else {
				
				getUnQuanbuData(url7);
			}
			break;
		case R.id.ll_donghuapian:
			currentListIndex = DONGHUAPIAN;
			String url8 = StatisticsUtils.getMovie_DonghuaFirstURL();
			app.MyToast(aq.getContext(), "ll_donghuapian");
			if(lists[currentListIndex] != null && !lists[currentListIndex].isEmpty()) {
				
				notifyAdapter(lists[currentListIndex]);
			} else {
				
				getUnQuanbuData(url8);
			}
			break;
		// case R.id.bt_quanbufenlei:
		// String url9 = StatisticsUtils.getTopItemURL(TOP_ITEM_URL,
		// TV_DIANYING, 1 + "", 50 + "");
		// app.MyToast(aq.getContext(),"bt_quanbufenlei");
		// getServiceData(url9);
		// break;
		case R.id.bt_zuijinguankan:
			startActivity(new Intent(this, HistoryActivity.class));
			break;
		case R.id.bt_zhuijushoucang:
			startActivity(new Intent(this, ShowShoucangHistoryActivity.class));
			break;
		default:
			break;
		}

		View tempView = ItemStateUtils.viewToActive(getApplicationContext(), v,
				activeView);

		if (tempView != null) {

			activeView = tempView;
		}

		beforeGvView = null;
	}

	@Override
	protected void initView() {
		// TODO Auto-generated method stub
		
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

		firstFloatView = findViewById(R.id.inclue_movie_show_item);
		topLinearLayout = (LinearLayout) findViewById(R.id.ll_show_movie_top);

		movieGv.setNextFocusLeftId(R.id.bt_quanbufenlei);
	}

	@Override
	protected void initViewListener() {
		// TODO Auto-generated method stub
		
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
		mFenLeiBtn.setOnClickListener(this);

		dongzuoLL.setOnFocusChangeListener(this);
		kehuanLL.setOnFocusChangeListener(this);
		lunliLL.setOnFocusChangeListener(this);
		xijuLL.setOnFocusChangeListener(this);
		aiqingLL.setOnFocusChangeListener(this);
		xuanyiLL.setOnFocusChangeListener(this);
		kongbuLL.setOnFocusChangeListener(this);
		donghuaLL.setOnFocusChangeListener(this);

		zuijinguankanBtn.setOnFocusChangeListener(this);
		zhuijushoucangBtn.setOnFocusChangeListener(this);
		mFenLeiBtn.setOnFocusChangeListener(this);

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
				List<MovieItemData> list = movieAdapter.getMovieList();
				if(list != null && !list.isEmpty()) {
					
					Intent intent = new Intent(ShowMovieActivity.this,
							ShowXiangqingMovie.class);
					intent.putExtra("ID", list.get(position).getMovieID());
					startActivity(intent);
				}
			}
		});

		movieGv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, final View view,
					int position, long id) {
				// TODO Auto-generated method stub
				// if (BuildConfig.DEBUG)
				Log.i(TAG, "Positon:" + position + " View:" + view
						+ " beforGvView:" + beforeGvView);

				if (view == null) {

					isSelectedItem = false;
					return;
				}

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

				if (beforeGvView != null) {

					ItemStateUtils.viewOutAnimation(getApplicationContext(),
							beforeGvView);
				} else {

					ItemStateUtils.floatViewOutAnimaiton(firstFloatView);
				}

				ItemStateUtils.viewInAnimation(getApplicationContext(), view);

				int[] firstAndLastVisible = new int[2];
				firstAndLastVisible[0] = movieGv.getFirstVisiblePosition();
				firstAndLastVisible[1] = movieGv.getLastVisiblePosition();

				if (y == 0 || y - popHeight == 0) {// 顶部没有渐影

					beforeFirstAndLastVible = ItemStateUtils
							.reCaculateFirstAndLastVisbile(
									beforeFirstAndLastVible,
									firstAndLastVisible, isSmoonthScroll, false);

				} else {// 顶部有渐影

					beforeFirstAndLastVible = ItemStateUtils
							.reCaculateFirstAndLastVisbile(
									beforeFirstAndLastVible,
									firstAndLastVisible, isSmoonthScroll, true);

				}

				beforeGvView = view;
				beforepostion = position;
				
				//缓存
				int size = movieAdapter.getMovieList().size();
				if(size-1-firstAndLastVisible[1] < StatisticsUtils.CACHE_NUM) {
					
					if(isNextPagePossibles[currentListIndex]) {
						
						pageNums[currentListIndex] ++;
						cachePlay(currentListIndex, pageNums[currentListIndex]);
					}
				}

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

					if (beforeGvView != null) {

						ItemStateUtils.viewOutAnimation(
								getApplicationContext(), beforeGvView);
					} else {

						// firstFloatView.setVisibility(View.GONE);
						ItemStateUtils.floatViewOutAnimaiton(firstFloatView);
					}
				} else {

					movieGv.setNextFocusLeftId(activeView.getId());

					if (beforeGvView != null) {

						ItemStateUtils.viewInAnimation(getApplicationContext(),
								beforeGvView);

					} else {
						initFirstFloatView();
					}
				}
			}
		});

		searchEt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Editable editable = searchEt.getText();
				String searchStr = editable.toString();

				if (searchStr != null && !searchStr.equals("")) {

					String url = StatisticsUtils.getSearchURL(SEARCH_URL,
							1 + "", 30 + "", searchStr);
					getFilterData(url);
				}
			}
		});

		searchEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus == true) {
					((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
							.showSoftInput(v, InputMethodManager.SHOW_FORCED);

				} else { // ie searchBoxEditText doesn't have focus
					((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(v.getWindowToken(), 0);

				}
			}
		});
	}

	@Override
	protected void initViewState() {
		// TODO Auto-generated method stub
		
		activeView = mFenLeiBtn;

		ItemStateUtils.buttonToActiveState(getApplicationContext(), mFenLeiBtn);

		ItemStateUtils.setItemPadding(dongzuoLL);
		ItemStateUtils.setItemPadding(kehuanLL);
		ItemStateUtils.setItemPadding(lunliLL);
		ItemStateUtils.setItemPadding(xijuLL);
		ItemStateUtils.setItemPadding(aiqingLL);
		ItemStateUtils.setItemPadding(xuanyiLL);
		ItemStateUtils.setItemPadding(kongbuLL);
		ItemStateUtils.setItemPadding(donghuaLL);
		ItemStateUtils.setItemPadding(zuijinguankanBtn);
		ItemStateUtils.setItemPadding(zhuijushoucangBtn);
		ItemStateUtils.setItemPadding(mFenLeiBtn);
	}

	@Override
	protected void clearLists() {
		// TODO Auto-generated method stub
		
		for(int i= 0;i<lists.length;i++) {
			
			StatisticsUtils.clearList(lists[i]);
		}
	}

	@Override
	protected void initLists() {
		// TODO Auto-generated method stub
		
		for(int i= 0;i<lists.length;i++) {
			
			lists[i] = new ArrayList<MovieItemData>();
			isNextPagePossibles[i] = false;//认为所有的不能够翻页
			pageNums[i]=0;
		}
	}

	@Override
	protected void initFirstFloatView() {
		// TODO Auto-generated method stub
		
		firstFloatView.setX(0);
		firstFloatView.setY(0);
		firstFloatView.setLayoutParams(new FrameLayout.LayoutParams(popWidth,
				popHeight));
		firstFloatView.setVisibility(View.VISIBLE);

		TextView movieName = (TextView) firstFloatView
				.findViewById(R.id.tv_item_layout_name);
		TextView movieScore = (TextView) firstFloatView
				.findViewById(R.id.tv_item_layout_score);

		List<MovieItemData> list = movieAdapter.getMovieList();
		if (list != null && !list.isEmpty()) {

			FrameLayout inFrameLayout = (FrameLayout) firstFloatView.findViewById(R.id.inclue_movie_show_item);
			ImageView haibaoIv = (ImageView) inFrameLayout.findViewById(R.id.iv_item_layout_haibao);
			aq.id(haibaoIv).image(
					list.get(0).getMoviePicUrl(), true, true, 0,
					R.drawable.post_active);

			movieName.setText(list.get(0).getMovieName());
			movieScore.setText(list.get(0).getMovieScore());

			String duration = list.get(0).getMovieDuration();
			if (duration != null && !duration.equals("")) {

				TextView movieDuration = (TextView) firstFloatView
						.findViewById(R.id.tv_item_layout_other_info);
				movieDuration.setText(StatisticsUtils.formatMovieDuration(duration));
			}
		}

		ItemStateUtils.floatViewInAnimaiton(getApplicationContext(),
				firstFloatView);
	}

	@Override
	protected void notifyAdapter(List<MovieItemData> list) {
		// TODO Auto-generated method stub
		
		int height=movieAdapter.getHeight()
				,width = movieAdapter.getWidth();
		
		if(height !=0 && width !=0) {
			
			popWidth = width;
			popHeight = height;
		}
		
		movieAdapter.setList(list);
		
		if(list != null && !list.isEmpty() && currentListIndex != QUANBUFENLEI) {//判断其能否向获取更多数据
			
			if(list.size() == StatisticsUtils.FIRST_NUM) {
				
				isNextPagePossibles[currentListIndex] = true;
			} else if(list.size() < StatisticsUtils.FIRST_NUM) {
				
				isNextPagePossibles[currentListIndex] = false;
			}
		}
		
		lists[currentListIndex] = list;
		
		movieGv.setSelection(0);
		movieAdapter.notifyDataSetChanged();
		beforeGvView = null;
		movieGv.setFocusable(true);
		movieGv.setSelected(true);
		isSelectedItem = false;
		movieGv.requestFocus();
	}

	@Override
	protected void filterVideoSource(String[] choice) {
		// TODO Auto-generated method stub
		
		String quanbu = getString(R.string.quanbu_name);
		String quanbufenlei = getString(R.string.quanbufenlei_name);
		String tempStr = StatisticsUtils
				.getQuanBuFenLeiName(choice,
						quanbufenlei, quanbu);
		mFenLeiBtn.setText(tempStr);

		if (tempStr.equals(quanbufenlei)) {

			if(lists[QUAN_FILTER] != null && !lists[QUAN_FILTER].isEmpty()) {
				
				notifyAdapter(lists[QUAN_FILTER]);
			} else {
				
				String url2 = StatisticsUtils.getTopItemURL(TOP_ITEM_URL,
						TV_DIANYING, 1 + "", 50 + "");
				getQuan10Data(url2);
			}

			return;
		}
		String url = StatisticsUtils
				.getFilterURL(FILTER_URL,
						1 + "", 50 + "",
						MOVIE_TYPE)
				+ StatisticsUtils
						.getFileterURL3Param(
								choice, quanbu);
		Log.i(TAG, "POP--->URL:" + url);
		getFilterData(url);
	}

	@Override
	protected void getQuan10Data(String url) {
		// TODO Auto-generated method stub
		
		getServiceData(url, "initQuan10ServiceData");
	}

	@Override
	protected void getQuanbuData(String url) {
		// TODO Auto-generated method stub
		
		getServiceData(url, "initQuanbuServiceData");
	}

	@Override
	protected void getUnQuanbuData(String url) {
		// TODO Auto-generated method stub
		
		getServiceData(url, "initUnQuanbuServiceData");
	}

	@Override
	protected void getFilterData(String url) {
		// TODO Auto-generated method stub
		
		getServiceData(url, "initFilerServiceData");
	}

	@Override
	protected void getServiceData(String url, String interfaceName) {
		// TODO Auto-generated method stub
		
		firstFloatView.setVisibility(View.INVISIBLE);
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, interfaceName);

		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}

	@Override
	public void initQuan10ServiceData(String url, JSONObject json,
			AjaxStatus status) {
		// TODO Auto-generated method stub
		
		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {

			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		try {
			Log.d(TAG, json.toString());
			lists[QUAN_TEN] = StatisticsUtils.returnTVBangDanListJson(json.toString());
			String urlNormal = StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
					10 + "", MOVIE_TYPE);
			currentListIndex = QUANBUFENLEI;
			getQuanbuData(urlNormal);
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

	@Override
	public void initQuanbuServiceData(String url, JSONObject json,
			AjaxStatus status) {
		// TODO Auto-generated method stub
		
		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {

			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		try {
			Log.d(TAG, json.toString());
			if(lists[QUAN_TEN] != null && !lists[QUAN_TEN].isEmpty()) {
				
				List<MovieItemData> temp10List = new ArrayList<MovieItemData>(lists[QUAN_TEN]);
				List<MovieItemData> tempList = new ArrayList<MovieItemData>();
				tempList = StatisticsUtils.returnFilterMovieSearch_TVJson(json.toString());
				
				for (MovieItemData movieItemData : tempList) {

					boolean isSame = false;
					String proId = movieItemData.getMovieID();
					for (int i = 0; i < temp10List.size(); i++) {

						if (proId.equals(temp10List.get(i).getMovieID())) {

							isSame = true;
							break;// 符合条件跳出本次循环

						}
					}
					if (!isSame) {

						temp10List.add(movieItemData);
					}
				}
				
				if(tempList.size() == StatisticsUtils.CACHE_NUM) {
					
					isNextPagePossibles[currentListIndex] = true;
				}
				notifyAdapter(temp10List);
				initFirstFloatView();
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

	@Override
	public void initUnQuanbuServiceData(String url, JSONObject json,
			AjaxStatus status) {
		// TODO Auto-generated method stub
		
		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {

			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		try {
			Log.d(TAG, json.toString());
			notifyAdapter(StatisticsUtils.returnTVBangDanList_TVJson(json.toString()));
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

	@Override
	public void initFilerServiceData(String url, JSONObject json,
			AjaxStatus status) {
		// TODO Auto-generated method stub
		
		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {

			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		
		try {
			Log.d(TAG, json.toString());
			notifyAdapter(StatisticsUtils.returnFilterMovieSearch_TVJson(json.toString()));
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

	@Override
	protected void refreshAdpter(List<MovieItemData> list) {
		// TODO Auto-generated method stub
		
		List<MovieItemData> srcList = movieAdapter.getMovieList();
		
		if(list != null && !list.isEmpty()) {
			
			for(MovieItemData movieItemData:list) {
				
				srcList.add(movieItemData);
			}
		}
		
		if(list.size() == StatisticsUtils.CACHE_NUM) {
			
			isNextPagePossibles[currentListIndex] = true;
		}else {
			
			isNextPagePossibles[currentListIndex] = false;
		}
		
		movieAdapter.setList(srcList);
		lists[currentListIndex] = srcList;
		
		movieAdapter.notifyDataSetChanged();
	}

	@Override
	public void initMoreFilerServiceData(String url, JSONObject json,
			AjaxStatus status) {
		// TODO Auto-generated method stub
		
		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {

			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		
		try {
			Log.d(TAG, json.toString());
			
			refreshAdpter(StatisticsUtils.returnFilterMovieSearch_TVJson(json.toString()));
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

	@Override
	protected void getMoreFilterData(String url) {
		// TODO Auto-generated method stub
		
		getServiceData(url, "initMoreFilerServiceData");
	}

	@Override
	public void initMoreBangDanServiceData(String url, JSONObject json,
			AjaxStatus status) {
		// TODO Auto-generated method stub
		
		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {

			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		
		try {
			Log.d(TAG, json.toString());
			
			refreshAdpter(StatisticsUtils.returnTVBangDanList_TVJson(json.toString()));
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

	@Override
	protected void getMoreBangDanData(String url) {
		// TODO Auto-generated method stub
		
		getServiceData(url, "initMoreBangDanServiceData");
	}

	@Override
	protected void cachePlay(int index, int pageNum) {
		// TODO Auto-generated method stub
		
		switch (index) {
		case QUANBUFENLEI:
//			getFilterData(StatisticsUtils.getTV_QuanAllCacheURL(pageNum));
			getMoreFilterData(StatisticsUtils.getMovie_QuanAllCacheURL(pageNum));
			break;
		case QUAN_TEN:
			
			break;
		case QUAN_FILTER:
			
			break;
		case SEARCH:
			
			break;
		case DONGZUOPIAN:
			getMoreBangDanData(StatisticsUtils.getMovie_DongzuoCacheURL(pageNum));
			break;
		case KEHUANPIAN:
			getMoreBangDanData(StatisticsUtils.getMovie_KehuanCacheURL(pageNum));
			break;
		case LUNLIPIAN:
			getMoreBangDanData(StatisticsUtils.getMovie_LunliCacheURL(pageNum));
			break;
		case XIJUPIAN:
			getMoreBangDanData(StatisticsUtils.getMovie_XijuCacheURL(pageNum));
			break;
		case AIQINGPIAN:
			getMoreBangDanData(StatisticsUtils.getMovie_AiqingCacheURL(pageNum));
			break;
		case XUANYIPIAN:
			getMoreBangDanData(StatisticsUtils.getMovie_XuanyiCacheURL(pageNum));
			break;
		case KONGBUPIAN:
			getMoreBangDanData(StatisticsUtils.getMovie_KongbuCacheURL(pageNum));
			break;
		case DONGHUAPIAN:
			getMoreBangDanData(StatisticsUtils.getMovie_DonghuaCacheURL(pageNum));
			break;

		default:
			break;
		}
	}

}
