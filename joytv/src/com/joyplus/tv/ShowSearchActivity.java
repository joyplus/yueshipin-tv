package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.joyplus.tv.Adapters.SearchAdapter;
import com.joyplus.tv.entity.MovieItemData;
import com.joyplus.tv.ui.MyMovieGridView;
import com.joyplus.tv.ui.WaitingDialog;
import com.joyplus.tv.utils.ItemStateUtils;

public class ShowSearchActivity extends AbstractShowActivity {

	public static  String TAG = "ShowSearchActivity";
	private static final int DIALOG_WAITING = 0;
	
	private AQuery aq;
	private App app;

	private EditText searchEt;
	private MyMovieGridView dinashijuGv;

	private View firstFloatView;

	private boolean isSelectedItem = true;// GridView中参数是否真正初始化

	private int popWidth, popHeight;

	private boolean isGridViewUp = false;

	private int[] beforeFirstAndLastVible = { 0, 9 };

	private View beforeGvView = null;

	private List<MovieItemData>[] lists = new List[4];
	private boolean[] isNextPagePossibles = new boolean[4];
	private int[] pageNums = new int[4];

	private int currentListIndex;
	
	private SearchAdapter searchAdapter = null;
	
	private int beforepostion = 0;
	
	private String search;
	private String filterSource;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_search);

		aq = new AQuery(this);
		app = (App) getApplication();
		
		initActivity();
		
		searchAdapter = new SearchAdapter(this,aq);
	}
	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		if (action == KeyEvent.ACTION_UP) {

		}
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.i("Yangzhg", "onClick");
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

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initView() {
		// TODO Auto-generated method stub
		
		searchEt = (EditText) findViewById(R.id.et_search);
		dinashijuGv = (MyMovieGridView) findViewById(R.id.gv_movie_show);

		firstFloatView = findViewById(R.id.inclue_movie_show_item);
	}

	@Override
	protected void initViewListener() {
		// TODO Auto-generated method stub
		
		searchEt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Editable editable = searchEt.getText();
				String searchStr = editable.toString();
				searchEt.setText("");
				dinashijuGv.setNextFocusForwardId(searchEt.getId());//
				showDialog(DIALOG_WAITING);

				if (searchStr != null && !searchStr.equals("")) {

					dinashijuGv.setAdapter(searchAdapter);
					search = searchStr;
					StatisticsUtils.clearList(lists[SEARCH]);
					currentListIndex = SEARCH;
					String url = StatisticsUtils.getSearch_FirstURL(searchStr);
					getFilterData(url);
				}
			}
		});
		
		searchEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus==true){
					((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
					    .showSoftInput(searchEt, InputMethodManager.SHOW_FORCED);

					}else{ //ie searchBoxEditText doesn't have focus
					((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
					    .hideSoftInputFromWindow(searchEt.getWindowToken(), 0);

					}
			}
		});

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

		dinashijuGv
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						List<MovieItemData> list = searchAdapter.getMovieList();
						if(list != null && !list.isEmpty()) {
							String pro_type = list.get(position).getMovieProType();
							Log.i(TAG, "pro_type:" + pro_type);
							if(pro_type != null && !pro_type.equals("")) {
								
								if(pro_type.equals("2")) {
									Log.i(TAG, "pro_type:" + pro_type + "   --->2");
									Intent intent = new Intent(ShowSearchActivity.this,
											ShowXiangqingTv.class);
									intent.putExtra("ID", list.get(position).getMovieID());
									startActivity(intent);
//									startActivity();
								} else if(pro_type.equals("1")) {
									Log.i(TAG, "pro_type:" + pro_type + "   --->1");
									Intent intent = new Intent(ShowSearchActivity.this,
											ShowXiangqingMovie.class);
									intent.putExtra("ID", list.get(position).getMovieID());
									startActivity(intent);
//									startActivity();
								} else if(pro_type.equals("131")) {
									
									Intent intent = new Intent(ShowSearchActivity.this,
											ShowXiangqingDongman.class);
									intent.putExtra("ID", list.get(position).getMovieID());
									startActivity(intent);
								} else if(pro_type.equals("3")) {
									
									Intent intent = new Intent(ShowSearchActivity.this,
											ShowXiangqingZongYi.class);
									intent.putExtra("ID", list.get(position).getMovieID());
									startActivity(intent);
								}
							}
						}
					}
				});

		dinashijuGv
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							final View view, int position, long id) {
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

									dinashijuGv
											.smoothScrollBy(-popHeight, 1000);
									isSmoonthScroll = true;
								}
							} else {

								if (!isGridViewUp) {

									dinashijuGv.smoothScrollBy(popHeight,
											1000 * 2);
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
						
						// 缓存
						int size = searchAdapter.getMovieList().size();
						if (size - 1 - firstAndLastVisible[1] < StatisticsUtils.CACHE_NUM) {

							if (isNextPagePossibles[currentListIndex]) {

								pageNums[currentListIndex]++;
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

					if (beforeGvView != null) {

						ItemStateUtils.viewInAnimation(getApplicationContext(),
								beforeGvView);

					} else {
						initFirstFloatView();
					}
				}
			}
		});
	}

	@Override
	protected void initViewState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void clearLists() {
		// TODO Auto-generated method stub
		
		for (int i = 0; i < lists.length; i++) {

			StatisticsUtils.clearList(lists[i]);
		}
	}

	@Override
	protected void initLists() {
		// TODO Auto-generated method stub
		for (int i = 0; i < lists.length; i++) {

			lists[i] = new ArrayList<MovieItemData>();
			isNextPagePossibles[i] = false;// 认为所有的不能够翻页
			pageNums[i] = 0;
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

		TextView movieName = (TextView) firstFloatView.findViewById(R.id.tv_item_layout_name);
		TextView movieScore = (TextView) firstFloatView.findViewById(R.id.tv_item_layout_score);
		
		List<MovieItemData> list = searchAdapter.getMovieList();
		if (list != null && !list.isEmpty()) {

			FrameLayout inFrameLayout = (FrameLayout) firstFloatView.findViewById(R.id.inclue_movie_show_item);
			ImageView haibaoIv = (ImageView) inFrameLayout.findViewById(R.id.iv_item_layout_haibao);
			aq.id(haibaoIv).image(list.get(0).getMoviePicUrl(), 
					true, true,0, R.drawable.post_active);
			movieName.setText(list.get(0).getMovieName());
			
			String proType = list.get(0).getMovieProType();
			
			if(proType != null && !proType.equals("")) {
				
				if(proType.equals("1")) {
					
					movieScore.setText(list.get(0).getMovieScore());
					String duration = list.get(0).getMovieDuration();
					if(duration != null && !duration.equals("")) {
						
						TextView movieDuration = (TextView) firstFloatView
								.findViewById(R.id.tv_item_layout_other_info);
						movieDuration.setText(StatisticsUtils.formatMovieDuration(duration));
					}
				} else if(proType.equals("2") || proType.equals("131")){
					movieScore.setText(list.get(0).getMovieScore());
					String curEpisode = list.get(0).getMovieCurEpisode();
					String maxEpisode = list.get(0).getMovieMaxEpisode();
					
					if(maxEpisode != null && !maxEpisode.equals("")) {
						
						if(curEpisode == null || curEpisode.equals("0") || 
								curEpisode.compareTo(maxEpisode) >= 0) {
							
							TextView movieUpdate = (TextView) firstFloatView
									.findViewById(R.id.tv_item_layout_other_info);
							movieUpdate.setText(
									maxEpisode + getString(R.string.dianshiju_jiquan));
							} else if(maxEpisode.compareTo(curEpisode) > 0) {
								
								TextView movieUpdate = (TextView) firstFloatView
										.findViewById(R.id.tv_item_layout_other_info);
								movieUpdate.setText(getString(R.string.zongyi_gengxinzhi) + 
										curEpisode);
						}
					}

				} else if(proType.equals("3")) {
					
					String curEpisode = list.get(0).getMovieCurEpisode();
					if(curEpisode != null && !curEpisode.equals("")) {
						
						TextView movieUpdate = (TextView) firstFloatView
								.findViewById(R.id.tv_item_layout_other_info);
						movieUpdate.setText(getString(R.string.zongyi_gengxinzhi) + 
								list.get(0).getMovieCurEpisode());
					}
				}
			}
			ItemStateUtils.floatViewInAnimaiton(getApplicationContext(),
					firstFloatView);
		}
	}

	@Override
	protected void notifyAdapter(List<MovieItemData> list) {
		// TODO Auto-generated method stub
		
		int height=searchAdapter.getHeight()
				,width = searchAdapter.getWidth();
		
		if(height !=0 && width !=0) {
			
			popWidth = width;
			popHeight = height;
		}
		
		searchAdapter.setList(list);
		
		if (list != null && !list.isEmpty() && currentListIndex != QUANBUFENLEI) {// 判断其能否向获取更多数据

			if (list.size() == StatisticsUtils.FIRST_NUM) {

				isNextPagePossibles[currentListIndex] = true;
			} else if (list.size() < StatisticsUtils.FIRST_NUM) {

				isNextPagePossibles[currentListIndex] = false;
			}
		}

		lists[currentListIndex] = list;
		
		dinashijuGv.setSelection(0);
		searchAdapter.notifyDataSetChanged();
		beforeGvView = null;
		initFirstFloatView();
		dinashijuGv.setFocusable(true);
		dinashijuGv.setSelected(true);
		isSelectedItem = false;
		removeDialog(DIALOG_WAITING);
		dinashijuGv.requestFocus();
	}

	@Override
	protected void filterVideoSource(String[] choice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void getQuan10Data(String url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void getQuanbuData(String url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void getUnQuanbuData(String url) {
		// TODO Auto-generated method stub
		
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
		// cb.url(url).type(JSONObject.class).weakHandler(this, "initData");
		cb.url(url).type(JSONObject.class).weakHandler(this, interfaceName);

		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}

	@Override
	public void initQuan10ServiceData(String url, JSONObject json,
			AjaxStatus status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initQuanbuServiceData(String url, JSONObject json,
			AjaxStatus status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initUnQuanbuServiceData(String url, JSONObject json,
			AjaxStatus status) {
		// TODO Auto-generated method stub
		
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
			notifyAdapter(StatisticsUtils.returnFilterMovieSearch_TVJson(json
					.toString()));
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
		

		List<MovieItemData> srcList = searchAdapter.getMovieList();

		if (list != null && !list.isEmpty()) {

			for (MovieItemData movieItemData : list) {

				srcList.add(movieItemData);
			}
		}

		if (list.size() == StatisticsUtils.CACHE_NUM) {

			isNextPagePossibles[currentListIndex] = true;
		} else {

			isNextPagePossibles[currentListIndex] = false;
		}

		searchAdapter.setList(srcList);
		lists[currentListIndex] = srcList;

		searchAdapter.notifyDataSetChanged();
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

			refreshAdpter(StatisticsUtils.returnFilterMovieSearch_TVJson(json
					.toString()));
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
		
	}

	@Override
	protected void getMoreBangDanData(String url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void cachePlay(int index, int pageNum) {
		// TODO Auto-generated method stub
		
		switch (index) {
		case QUANBUFENLEI:
			break;
		case QUAN_TEN:

			break;
		case QUAN_FILTER:
			break;
		case SEARCH:

			getMoreFilterData(StatisticsUtils.getSearch_CacheURL(pageNum, search));
			break;

		default:
			break;
		}
	}

	@Override
	protected void filterPopWindowShow() {
		// TODO Auto-generated method stub
		
	}

}
