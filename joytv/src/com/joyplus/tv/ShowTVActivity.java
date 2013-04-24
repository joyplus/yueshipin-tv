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
import com.joyplus.tv.Adapters.DianShijuAdapter;
import com.joyplus.tv.entity.MovieItemData;
import com.joyplus.tv.ui.MyMovieGridView;
import com.joyplus.tv.ui.NavigateView;
import com.joyplus.tv.ui.NavigateView.OnResultListener;
import com.joyplus.tv.utils.ItemStateUtils;
public class ShowTVActivity extends AbstractShowActivity{

	private static final String TAG = "ShowTVActivity";
	
	private static final int DALUJU = 4;
	private static final int GANGJU = 5;
	private static final int TAIJU = 6;
	private static final int HANJU = 7;
	private static final int MEIJU = 8;
	private static final int RIJU = 9;
	private AQuery aq;
	private App app;

	private EditText searchEt;
	private MyMovieGridView dinashijuGv;
	private LinearLayout dalujuLL, ganjuLL, taijuLL, hanjuLL, meijuLL,
			rijuLL;

	private Button zuijinguankanBtn, zhuijushoucangBtn,
			mFenLeiBtn;
	
	private LinearLayout topLinearLayout;
	
	private View firstFloatView ;

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
	
	private DianShijuAdapter dianShijuAdapter =  null;
	
	private int beforepostion = 0;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_tv);

		app = (App) getApplication();
		aq = new AQuery(this);
		
		initActivity();

		dianShijuAdapter = new DianShijuAdapter(this,aq);
		dinashijuGv.setAdapter(dianShijuAdapter);

		String url = StatisticsUtils.getTV_Quan10URL();
		getQuan10Data(url);
		
		dinashijuGv.setSelected(true);
		dinashijuGv.requestFocus();
		dinashijuGv.setSelection(0);
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
	
	private PopupWindow popupWindow;

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		 Log.i("Yangzhg", "onClick");

			if(activeView == null) {
				
				activeView = mFenLeiBtn;
			}
			
			if(v.getId() == R.id.bt_quanbufenlei) {
				

			}
			
			if (v.getId() == R.id.bt_quanbufenlei
					&& activeView.getId() == R.id.bt_quanbufenlei) {

				filterPopWindowShow();
			}
			
			switch (v.getId()) {
			case R.id.ll_daluju:
				String url1 = StatisticsUtils.getTV_DalujuFirstURL();
				app.MyToast(aq.getContext(),"ll_daluju");
				currentListIndex = DALUJU;
				if(lists[currentListIndex] != null && !lists[currentListIndex].isEmpty()) {
					
					notifyAdapter(lists[currentListIndex]);
				} else {
					
					getUnQuanbuData(url1);
				}
				break;
			case R.id.ll_gangju:
				currentListIndex = GANGJU;
				String url2 = StatisticsUtils.getTV_GangjuFirstURL();
				app.MyToast(aq.getContext(),"ll_gangju");
				if(lists[currentListIndex] != null && !lists[currentListIndex].isEmpty()) {
					
					notifyAdapter(lists[currentListIndex]);
				} else {
					
					getUnQuanbuData(url2);
				}
				break;
			case R.id.ll_taiju:
				currentListIndex = TAIJU;
				String url3 = StatisticsUtils.getTV_TaijuFirstURL();
				app.MyToast(aq.getContext(),"ll_taiju");
				if(lists[currentListIndex] != null && !lists[currentListIndex].isEmpty()) {
					
					notifyAdapter(lists[currentListIndex]);
				} else {
					
					getUnQuanbuData(url3);
				}
				break;
			case R.id.ll_hanju:
				currentListIndex = HANJU;
				String url4 = StatisticsUtils.getTV_HanjuFirstURL();
				app.MyToast(aq.getContext(),"ll_hanju");
				if(lists[currentListIndex] != null && !lists[currentListIndex].isEmpty()) {
					
					notifyAdapter(lists[currentListIndex]);
				} else {
					
					getUnQuanbuData(url4);
				}
				break;
			case R.id.ll_meiju:
				currentListIndex = MEIJU;
				String url5 = StatisticsUtils.getTV_MeijuFirstURL();
				app.MyToast(aq.getContext(),"ll_meiju");
				if(lists[currentListIndex] != null && !lists[currentListIndex].isEmpty()) {
					
					notifyAdapter(lists[currentListIndex]);
				} else {
					
					getUnQuanbuData(url5);
				}
				break;
			case R.id.ll_riju:
				currentListIndex = RIJU;
				String url6 = StatisticsUtils.getTV_RijuFirstURL();
				app.MyToast(aq.getContext(),"ll_riju");
				if(lists[currentListIndex] != null && !lists[currentListIndex].isEmpty()) {
					
					notifyAdapter(lists[currentListIndex]);
				} else {
					
					getUnQuanbuData(url6);
				}
				break;
			case R.id.bt_quanbufenlei:
				currentListIndex = QUANBUFENLEI;
				app.MyToast(aq.getContext(), "bt_quanbufenlei");
				if (lists[currentListIndex] != null
						&& !lists[currentListIndex].isEmpty()) {

					notifyAdapter(lists[currentListIndex]);
				}
				break;
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
		dinashijuGv = (MyMovieGridView) findViewById(R.id.gv_movie_show);

		dalujuLL = (LinearLayout) findViewById(R.id.ll_daluju);
		ganjuLL = (LinearLayout) findViewById(R.id.ll_gangju);
		taijuLL = (LinearLayout) findViewById(R.id.ll_taiju);
		hanjuLL = (LinearLayout) findViewById(R.id.ll_hanju);
		meijuLL = (LinearLayout) findViewById(R.id.ll_meiju);
		rijuLL = (LinearLayout) findViewById(R.id.ll_riju);

		zuijinguankanBtn = (Button) findViewById(R.id.bt_zuijinguankan);
		zhuijushoucangBtn = (Button) findViewById(R.id.bt_zhuijushoucang);
		
		firstFloatView = findViewById(R.id.inclue_movie_show_item);
		
		topLinearLayout = (LinearLayout) findViewById(R.id.ll_show_movie_top);
		
		dinashijuGv.setNextFocusLeftId(R.id.bt_quanbufenlei);
	}

	@Override
	protected void initViewListener() {
		// TODO Auto-generated method stub
		
		dalujuLL.setOnKeyListener(this);
		ganjuLL.setOnKeyListener(this);
		taijuLL.setOnKeyListener(this);
		hanjuLL.setOnKeyListener(this);
		meijuLL.setOnKeyListener(this);
		rijuLL.setOnKeyListener(this);

		zuijinguankanBtn.setOnKeyListener(this);
		zhuijushoucangBtn.setOnKeyListener(this);
		mFenLeiBtn.setOnKeyListener(this);

		dalujuLL.setOnClickListener(this);
		ganjuLL.setOnClickListener(this);
		taijuLL.setOnClickListener(this);
		hanjuLL.setOnClickListener(this);
		meijuLL.setOnClickListener(this);
		rijuLL.setOnClickListener(this);

		zuijinguankanBtn.setOnClickListener(this);
		zhuijushoucangBtn.setOnClickListener(this);
		mFenLeiBtn.setOnClickListener(this);
		
		dalujuLL.setOnFocusChangeListener(this);
		ganjuLL.setOnFocusChangeListener(this);
		taijuLL.setOnFocusChangeListener(this);
		hanjuLL.setOnFocusChangeListener(this);
		meijuLL.setOnFocusChangeListener(this);
		rijuLL.setOnFocusChangeListener(this);

		zuijinguankanBtn.setOnFocusChangeListener(this);
		zhuijushoucangBtn.setOnFocusChangeListener(this);
		mFenLeiBtn.setOnFocusChangeListener(this);
		
		dinashijuGv.setOnKeyListener(new View.OnKeyListener() {

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
						 dinashijuGv.setSelection(1);
						 } else if (keyCode == KEY_DOWN) {
						 isSelectedItem = true;
						 dinashijuGv.setSelection(5);
						
						 }
					}

				}
				return false;
			}
		});

		dinashijuGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				List<MovieItemData> list = dianShijuAdapter.getMovieList();
				
				if(list != null && !list.isEmpty()) {
					
					Intent intent = new Intent(ShowTVActivity.this,
							ShowXiangqingTv.class);
					intent.putExtra("ID", list.get(position).getMovieID());
					startActivity(intent);
				}
			}
		});

		dinashijuGv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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
				
				dinashijuGv.setNextFocusLeftId(activeView.getId());//如果向左那就跳掉激活

				final float y = view.getY();

				boolean isSmoonthScroll = false;

				boolean isSameContent = position >= beforeFirstAndLastVible[0]
						&& position <= beforeFirstAndLastVible[1];
				if (position >= 5 && !isSameContent) {

					if (beforepostion >= beforeFirstAndLastVible[0]
							&& beforepostion <= beforeFirstAndLastVible[0] + 4) {

						if (isGridViewUp) {
							
							dinashijuGv.smoothScrollBy(-popHeight, 1000);
							isSmoonthScroll = true;
						}
					} else {

						if (!isGridViewUp) {

							dinashijuGv.smoothScrollBy(popHeight, 1000 * 2);
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
				firstAndLastVisible[0] = dinashijuGv.getFirstVisiblePosition();
				firstAndLastVisible[1] = dinashijuGv.getLastVisiblePosition();

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
				int size = dianShijuAdapter.getMovieList().size();
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

		dinashijuGv.setOnFocusChangeListener(new View.OnFocusChangeListener() {

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

					dinashijuGv.setNextFocusLeftId(activeView.getId());

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
		
		ItemStateUtils.setItemPadding(dalujuLL);
		ItemStateUtils.setItemPadding(ganjuLL);
		ItemStateUtils.setItemPadding(taijuLL);
		ItemStateUtils.setItemPadding(hanjuLL);
		ItemStateUtils.setItemPadding(meijuLL);
		ItemStateUtils.setItemPadding(rijuLL);
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
		firstFloatView.setLayoutParams(new FrameLayout.LayoutParams(popWidth, popHeight));
		firstFloatView.setVisibility(View.VISIBLE);
		
		TextView movieName = (TextView) firstFloatView.findViewById(R.id.tv_item_layout_name);
		TextView movieScore = (TextView) firstFloatView.findViewById(R.id.tv_item_layout_score);
		
		List<MovieItemData> list = dianShijuAdapter.getMovieList();
		if (list != null && !list.isEmpty()) {
			
			
			FrameLayout inFrameLayout = (FrameLayout) firstFloatView.findViewById(R.id.inclue_movie_show_item);
			ImageView haibaoIv = (ImageView) inFrameLayout.findViewById(R.id.iv_item_layout_haibao);
			aq.id(haibaoIv).image(
					list.get(0).getMoviePicUrl(), true, true, 0,
					R.drawable.post_active);
			movieName.setText(list.get(0).getMovieName());
			movieScore.setText(list.get(0).getMovieScore());
			
			
			String curEpisode = list.get(0).getMovieCurEpisode();
			String maxEpisode = list.get(0).getMovieMaxEpisode();
			
			if(curEpisode == null || curEpisode.equals("0") || 
					curEpisode.compareTo(maxEpisode) >= 0) {
				
				TextView movieUpdate = (TextView) firstFloatView
						.findViewById(R.id.tv_item_layout_other_info);
				movieUpdate.setText(
						list.get(0).getMovieMaxEpisode() + getString(R.string.dianshiju_jiquan));
				} else if(maxEpisode.compareTo(curEpisode) > 0) {
					
					TextView movieUpdate = (TextView) firstFloatView
							.findViewById(R.id.tv_item_layout_other_info);
					movieUpdate.setText(getString(R.string.zongyi_gengxinzhi) + 
							list.get(0).getMovieCurEpisode());
			}
		}
		
		ItemStateUtils.floatViewInAnimaiton(getApplicationContext(),
				firstFloatView);
	}

	@Override
	protected void notifyAdapter(List<MovieItemData> list) {
		// TODO Auto-generated method stub
		
		int height=dianShijuAdapter.getHeight()
				,width = dianShijuAdapter.getWidth();
		
		if(height !=0 && width !=0) {
			
			popWidth = width;
			popHeight = height;
		}
		
		dianShijuAdapter.setList(list);
		
		if(list != null && !list.isEmpty() && currentListIndex != QUANBUFENLEI) {//判断其能否向获取更多数据
			
			if(list.size() == StatisticsUtils.FIRST_NUM) {
				
				isNextPagePossibles[currentListIndex] = true;
			} else if(list.size() < StatisticsUtils.FIRST_NUM) {
				
				isNextPagePossibles[currentListIndex] = false;
			}
		}
		
		lists[currentListIndex] = list;
		
		dinashijuGv.setSelection(0);
		dianShijuAdapter.notifyDataSetChanged();
		beforeGvView = null;
		isSelectedItem = false;
		dinashijuGv.setFocusable(true);
		dinashijuGv.setSelected(true);
		dinashijuGv.requestFocus();
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
						TV_DIANSHIJU, 1 + "", 50 + "");
				getQuan10Data(url2);
			}

			return;
		}
		String url = StatisticsUtils
				.getFilterURL(FILTER_URL,
						1 + "", 50 + "",
						TV_TYPE)
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
			lists[QUAN_TEN] = StatisticsUtils.returnTVBangDanList_TVJson(json.toString());
			String urlNormal = StatisticsUtils.getTV_QuanAllFirstURL();
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
	
	
	protected void cachePlay(int index, int pageNum) {
		
		switch (index) {
		case QUANBUFENLEI:
//			getFilterData(StatisticsUtils.getTV_QuanAllCacheURL(pageNum));
			getMoreFilterData(StatisticsUtils.getTV_QuanAllCacheURL(pageNum));
			break;
		case QUAN_TEN:
			
			break;
		case QUAN_FILTER:
			
			break;
		case SEARCH:
			
			break;
		case DALUJU:
			getMoreBangDanData(StatisticsUtils.getTV_DalujuCacheURL(pageNum));
			break;
		case GANGJU:
			getMoreBangDanData(StatisticsUtils.getTV_GangjuCacheURL(pageNum));
			break;
		case TAIJU:
			getMoreBangDanData(StatisticsUtils.getTV_TaijuCacheURL(pageNum));
			break;
		case HANJU:
			getMoreBangDanData(StatisticsUtils.getTV_HanjuCacheURL(pageNum));
			break;
		case MEIJU:
			getMoreBangDanData(StatisticsUtils.getTV_MeijuCacheURL(pageNum));
			break;
		case RIJU:
			getMoreBangDanData(StatisticsUtils.getTV_RijuCacheURL(pageNum));
			break;

		default:
			break;
		}
	}
	
	protected void getMoreBangDanData(String url) {
		
		getServiceData(url, "initMoreBangDanServiceData");
	}
	
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
	
	protected void getMoreFilterData(String url) {
		// TODO Auto-generated method stub
		
		getServiceData(url, "initMoreFilerServiceData");
	}
	
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
	
	protected void refreshAdpter(List<MovieItemData> list) {
		
		List<MovieItemData> srcList = dianShijuAdapter.getMovieList();
		
		if(list != null && !list.isEmpty()) {
			
			for(MovieItemData movieItemData:list) {
				
				srcList.add(movieItemData);
			}
			
			if(list.size() == StatisticsUtils.CACHE_NUM) {
				
				isNextPagePossibles[currentListIndex] = true;
			}else {
				
				isNextPagePossibles[currentListIndex] = false;
			}
			
			dianShijuAdapter.setList(srcList);
			lists[currentListIndex] = srcList;
			
			dianShijuAdapter.notifyDataSetChanged();
		}
		
	}

	@Override
	protected void filterPopWindowShow() {
		// TODO Auto-generated method stub
		
		if(popupWindow ==null){
			NavigateView view = new NavigateView(this);
			int [] location = new int[2];
			mFenLeiBtn.getLocationOnScreen(location);
			view.Init(getResources().getStringArray(R.array.diqu_dianshiju_fenlei),
					getResources().getStringArray(R.array.leixing_dianshiju_fenlei), 
					getResources().getStringArray(R.array.shijian_dianying_fenlei), 
					location[0], 
					location[1],
					mFenLeiBtn.getWidth(), 
					mFenLeiBtn.getHeight(),
					new OnResultListener() {
						
						@Override
						public void onResult(View v, boolean isBack, String[] choice) {
							// TODO Auto-generated method stub
							if(isBack){
								popupWindow.dismiss();
							}else{
								if(popupWindow.isShowing()){
									popupWindow.dismiss();
									Toast.makeText(ShowTVActivity.this, "selected is " + choice[0] + ","+choice[1]+","+choice[2], Toast.LENGTH_LONG).show();
									filterVideoSource(choice);
									
								}
							}
						}
					});
			view.setLayoutParams(new LayoutParams(0,0));
//			popupWindow = new PopupWindow(view, getWindowManager().getDefaultDisplay().getWidth(),
//					getWindowManager().getDefaultDisplay().getHeight(), true);
			int width = topLinearLayout.getWidth();
			int height = topLinearLayout.getHeight();
			popupWindow = new PopupWindow(view,width,height, true);
		}
		popupWindow.showAtLocation(mFenLeiBtn.getRootView(), Gravity.LEFT | Gravity.BOTTOM, 0, 0);
	}

}
