package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.joyplus.tv.Adapters.ZongYiAdapter;
import com.joyplus.tv.entity.MovieItemData;
import com.joyplus.tv.ui.MyMovieGridView;
import com.joyplus.tv.ui.NavigateView;
import com.joyplus.tv.ui.NavigateView.OnResultListener;
import com.joyplus.tv.ui.WaitingDialog;
import com.joyplus.tv.utils.ItemStateUtils;
import com.joyplus.tv.utils.Log;

public class ShowTVActivity extends AbstractShowActivity {

	private static final String TAG = "ShowTVActivity";

	private static final int DALUJU = 4;
	private static final int GANGJU = 5;
	private static final int TAIJU = 6;
	private static final int HANJU = 7;
	private static final int MEIJU = 8;
	private static final int RIJU = 9;

	private static final int DIALOG_WAITING = 0;

	private AQuery aq;
	private App app;

	private EditText searchEt;
	private MyMovieGridView playGv;
	private LinearLayout topLinearLayout;
	private View activeView;
	private int popWidth = 0, popHeight = 0;
	private boolean isGridViewUp = false;
	private int[] beforeFirstAndLastVible = { 0, 9 };
	private View beforeGvView = null;
	private ZongYiAdapter searchAdapter = null;
	private int beforepostion = 0;
	private int currentListIndex;
	private String search;
	private String filterSource;
	private PopupWindow popupWindow;
	
	private int activeRecordIndex = -1;

	private LinearLayout dalujuLL, ganjuLL, taijuLL, hanjuLL, meijuLL, rijuLL;

	private Button zuijinguankanBtn, zhuijushoucangBtn, mFenLeiBtn;
	private List<MovieItemData>[] lists = new List[10];
	private boolean[] isNextPagePossibles = new boolean[10];
	private int[] pageNums = new int[10];
	
	private boolean isCurrentKeyVertical = false;//水平方向移动
	private boolean isFirstActive = true;//是否界面初始化
	private SparseArray<View> mSparseArray = new SparseArray<View>();
	
	private List<MovieItemData> shoucangList = null;
	private boolean isShowShoucang = false;
	
	private LinearLayout shoucangTitlleLL;
	private int qitaNextPoistion = -1;
	private TextView shoucangTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_tv);

		app = (App) getApplication();
		aq = new AQuery(this);
		
		//本地收藏，有没有更新
		shoucangList = StatisticsUtils.getList4DB(getApplicationContext(), app.getUserInfo().getUserId(), TV_TYPE);
		
		Log.i(TAG, "shoucangList--->:" + shoucangList.size());
		
		if(shoucangList != null && !shoucangList.isEmpty()) {
			
			if(shoucangList.size() > 0) {
				
				Log.i(TAG, "shoucangList--->:" + shoucangList.size());
				
				isShowShoucang = true;
			}
		}
		

		initActivity();
		
		searchAdapter = new ZongYiAdapter(this, aq);
		
		if(isShowShoucang) {
			
			searchAdapter.setShouCangCount(shoucangList.size());
			searchAdapter.setQita_name(getString(R.string.qitadongman_play_name));
			searchAdapter.setShoucangShow(true);
		}
		
		playGv.setAdapter(searchAdapter);
		
		playGv.requestFocus();
		playGv.setSelection(-1);
		
		getQuan10Data(StatisticsUtils.getTV_Quan10URL());
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		
		if(v.getId() == R.id.et_search) {
			
			if (hasFocus == true) {
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
						.showSoftInput(v, InputMethodManager.SHOW_FORCED);

			} else { // ie searchBoxEditText doesn't have focus
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(v.getWindowToken(), 0);

			}
		} else {
			
			if (hasFocus) {

				ItemStateUtils.viewToFocusState(getApplicationContext(), v);
			} else {

				ItemStateUtils.viewToOutFocusState(getApplicationContext(), v,
						activeView);
			}
		}
		
		if(!isCurrentKeyVertical) {
			
			int postion = playGv.getSelectedItemPosition();
			View view =mSparseArray.get(postion);
			
			if(view != null) {
				
				if (hasFocus) {// 如果gridview没有获取焦点，把item中高亮取消

					ItemStateUtils.viewOutAnimation(getApplicationContext(),
							view);
				} else {
					
					ItemStateUtils.viewInAnimation(getApplicationContext(), view);
					activeRecordIndex = postion;
				}
			}
		}

	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		int action = event.getAction();

		if (action == KeyEvent.ACTION_DOWN) {

			switch (keyCode) {
			case KEY_UP:

//				isGridViewUp = true;
				isCurrentKeyVertical = true;
				break;
			case KEY_DOWN:

//				isGridViewUp = false;
				isCurrentKeyVertical = true;
				break;
			case KEY_LEFT:

				isCurrentKeyVertical = false;
				break;
			case KEY_RIGHT:

				isCurrentKeyVertical = false;
				break;

			default:
				break;
			}

		}
		
		return super.onKeyDown(keyCode, event);
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
		if(app.getUserInfo()!=null){
			aq.id(R.id.iv_head_user_icon).image(
					app.getUserInfo().getUserAvatarUrl(), false, true, 0,
					R.drawable.avatar_defult);
			aq.id(R.id.tv_head_user_name).text(app.getUserInfo().getUserName());
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

	@Override
	protected void initViewListener() {
		// TODO Auto-generated method stub

		dalujuLL.setOnKeyListener(this);
		ganjuLL.setOnKeyListener(this);
		taijuLL.setOnKeyListener(this);
		hanjuLL.setOnKeyListener(this);
		meijuLL.setOnKeyListener(this);
		rijuLL.setOnKeyListener(this);
		searchEt.setOnKeyListener(this);
		
		playGv.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				
				int action = event.getAction();

				if (action == KeyEvent.ACTION_DOWN) {
					Log.i(TAG, "onKeyDown--->" + keyCode);

					switch (keyCode) {
					case KEY_UP:

						isGridViewUp = true;
//						isCurrentKeyVertical = true;
						Log.i(TAG, "onKeyDown--->KEY_UP" + keyCode + " "+isGridViewUp);
						break;
					case KEY_DOWN:

						isGridViewUp = false;
//						isCurrentKeyVertical = true;
						Log.i(TAG, "onKeyDown--->KEY_DOWN" + keyCode + " "+isGridViewUp);
						break;
					case KEY_LEFT:

//						isCurrentKeyVertical = false;
						break;
					case KEY_RIGHT:

//						isCurrentKeyVertical = false;
						break;

					default:
						break;
					}

				}
				
				return false;
			}
		});

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
		searchEt.setOnFocusChangeListener(this);

		zuijinguankanBtn.setOnFocusChangeListener(this);
		zhuijushoucangBtn.setOnFocusChangeListener(this);
		mFenLeiBtn.setOnFocusChangeListener(this);

		playGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				List<MovieItemData> list = searchAdapter.getMovieList();
				if (list != null && !list.isEmpty()) {
					String pro_type = list.get(position).getMovieProType();
					Log.i(TAG, "pro_type:" + pro_type);
					if (pro_type != null && !pro_type.equals("")) {
						Intent intent = new Intent();
						if (pro_type.equals("2")) {
							Log.i(TAG, "pro_type:" + pro_type + "   --->2");
							intent.setClass(ShowTVActivity.this,
									ShowXiangqingTv.class);
							intent.putExtra("ID", list.get(position)
									.getMovieID());
						} else if (pro_type.equals("1")) {
							Log.i(TAG, "pro_type:" + pro_type + "   --->1");
							intent.setClass(ShowTVActivity.this,
									ShowXiangqingMovie.class);
						} else if (pro_type.equals("131")) {

							intent.setClass(ShowTVActivity.this,
									ShowXiangqingDongman.class);
						} else if (pro_type.equals("3")) {

							intent.setClass(ShowTVActivity.this,
									ShowXiangqingZongYi.class);
						}

						intent.putExtra("ID", list.get(position).getMovieID());

						intent.putExtra("prod_url", list.get(position)
								.getMoviePicUrl());
						intent.putExtra("prod_name", list.get(position)
								.getMovieName());
						intent.putExtra("stars", list.get(position).getStars());
						intent.putExtra("directors", list.get(position)
								.getDirectors());
						intent.putExtra("summary", list.get(position)
								.getSummary());
						intent.putExtra("support_num", list.get(position)
								.getSupport_num());
						intent.putExtra("favority_num", list.get(position)
								.getFavority_num());
						intent.putExtra("definition", list.get(position)
								.getDefinition());
						intent.putExtra("score", list.get(position)
								.getMovieScore());
						startActivity(intent);

					}
				}
			}
		});

		playGv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, final View view,
					int position, long id) {
				// TODO Auto-generated method stub
				// if (BuildConfig.DEBUG)
				Log.i(TAG, "Positon:" + position + " View:" + view + 
						" before: " + activeRecordIndex);

				if (view == null) {

					return;
				}else {
					
//					Log.i(TAG, "mSparseArray: " + mSparseArray.get(position));
					if(view.getTag() != null){
						
						mSparseArray.put(position,view);
					} else {
						
						mSparseArray.delete(position);
					}
				}

				final float y = view.getY();

				boolean isSmoonthScroll = false;

				boolean isSameContent = position >= beforeFirstAndLastVible[0]
						&& position <= beforeFirstAndLastVible[1];
//				if (position >= 5 && !isSameContent) {
//
//					if (beforepostion >= beforeFirstAndLastVible[0]
//							&& beforepostion <= beforeFirstAndLastVible[0] + 4) {
//
//						if (isGridViewUp) {
//
//							playGv.smoothScrollBy(-popHeight, 1000);
//							isSmoonthScroll = true;
//						}
//					} else {
//
//						if (!isGridViewUp) {
//
//							playGv.smoothScrollBy(popHeight, 1000 * 2);
//							isSmoonthScroll = true;
//
//						}
//					}
//
//				}
				
				if(isShowShoucang) {
					
					int shoucangNum = shoucangList.size();
					if(!StatisticsUtils.isPostionEmpty(position, shoucangNum)) {
						
						if(StatisticsUtils.isPositionShowQitaTitle(position, shoucangNum)) {
							Log.i(TAG, "Position:--->" + position + " isGridViewUp--->" + isGridViewUp);
							if(isGridViewUp) {
								
								playGv.setSelection(position - 5);
							} else {
								
								playGv.setSelection(position + 5);
								qitaNextPoistion = position + 5;
							}
						} else {
							
							if(!isGridViewUp && qitaNextPoistion + 5 == position) {
								
								playGv.smoothScrollBy(35, -1);
//								int scrolly = playGv.getScrollY();

								shoucangTv.setText(R.string.qitadongman_play_name);
								
							} else if(isGridViewUp && qitaNextPoistion - 10  == position) {
								
								shoucangTv.setText(R.string.shoucang_update_name);
							}
							
							if (mSparseArray.get(activeRecordIndex) != null && activeRecordIndex != position) {

								ItemStateUtils.viewOutAnimation(getApplicationContext(),
										mSparseArray.get(activeRecordIndex));
							}

							if (position != activeRecordIndex && isFirstActive) {

								ItemStateUtils.viewInAnimation(getApplicationContext(),
										view);
								activeRecordIndex = position;
							}
							
							if(!isFirstActive) {//如果不是初始化，那就设为true
								
								isFirstActive = true;
							}
						}
					} else {
						//当前位置为空的组件
						if(isGridViewUp) {//向上
							
							playGv.setSelection(StatisticsUtils.stepToFirstInThisRow(position));
						} else {//向下
							
							playGv.setSelection(StatisticsUtils.stepToFirstInThisRow(position));
						}
						
					}
				} else {
					
					if (mSparseArray.get(activeRecordIndex) != null && activeRecordIndex != position) {

						ItemStateUtils.viewOutAnimation(getApplicationContext(),
								mSparseArray.get(activeRecordIndex));
					}

					if (position != activeRecordIndex && isFirstActive) {

						ItemStateUtils.viewInAnimation(getApplicationContext(),
								view);
						activeRecordIndex = position;
					}
					
					if(!isFirstActive) {//如果不是初始化，那就设为true
						
						isFirstActive = true;
					}
				}

				int[] firstAndLastVisible = new int[2];
				firstAndLastVisible[0] = playGv.getFirstVisiblePosition();
				firstAndLastVisible[1] = playGv.getLastVisiblePosition();

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

//				beforepostion = position;

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

			}
		});

		searchEt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Editable editable = searchEt.getText();
				String searchStr = editable.toString();
				// searchEt.setText("");
				playGv.setNextFocusForwardId(searchEt.getId());//

				ItemStateUtils
						.viewToNormal(getApplicationContext(), activeView);
				activeView = searchEt;

				if (searchStr != null && !searchStr.equals("")) {
					resetGvActive();
					showDialog(DIALOG_WAITING);
					search = searchStr;
					StatisticsUtils.clearList(lists[SEARCH]);
					currentListIndex = SEARCH;
					String url = StatisticsUtils.getSearch_FirstURL(searchStr);
					getFilterData(url);
				}

			}
		});
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
	protected void notifyAdapter(List<MovieItemData> list) {
		// TODO Auto-generated method stub

		int height = searchAdapter.getHeight(), width = searchAdapter
				.getWidth();

		if (height != 0 && width != 0) {

			popWidth = width;
			popHeight = height;
		}

		if(currentListIndex != SEARCH &&
				currentListIndex != QUAN_FILTER) {
			
			searchAdapter.setList(list,true);
		}else {
			
			searchAdapter.setList(list,false);
		}
		
		if(searchAdapter.getItemId() == list.size()) {
			
			searchAdapter.setItemId(list.size() + 1);
		} else {
			
			searchAdapter.setItemId(list.size());
		}
		

		if (list.size() <= 0) {

			app.MyToast(getApplicationContext(),
					getString(R.string.toast_no_play));
		}

		if (list != null && !list.isEmpty() && QUANBUFENLEI != currentListIndex) {// 判断其能否向获取更多数据

			if (list.size() == StatisticsUtils.FIRST_NUM) {

				isNextPagePossibles[currentListIndex] = true;
			} else if (list.size() < StatisticsUtils.FIRST_NUM) {

				isNextPagePossibles[currentListIndex] = false;
			}
		}
		lists[currentListIndex] = list;

		playGv.setSelection(0);
		searchAdapter.notifyDataSetChanged();
		
		removeDialog(DIALOG_WAITING);
//		if(isFirstActive) {
//			
//			playGv.requestFocus();
//		}

	}

	@Override
	protected void filterVideoSource(String[] choice) {
		// TODO Auto-generated method stub

		String quanbu = getString(R.string.quanbu_name);
		String quanbufenlei = getString(R.string.quanbufenlei_name);
		String tempStr = StatisticsUtils.getQuanBuFenLeiName(choice,
				quanbufenlei, quanbu);
		mFenLeiBtn.setText(tempStr);

		if (tempStr.equals(quanbufenlei)) {

			currentListIndex = QUANBUFENLEI;
			if (lists[QUANBUFENLEI] != null && !lists[QUANBUFENLEI].isEmpty()) {

				notifyAdapter(lists[QUANBUFENLEI]);
			}

			return;
		}

		showDialog(DIALOG_WAITING);
		StatisticsUtils.clearList(lists[QUAN_FILTER]);
		currentListIndex = QUAN_FILTER;
		resetGvActive();
		filterSource = StatisticsUtils.getFileterURL3Param(choice, quanbu);
		String url = StatisticsUtils.getFilter_DianshijuFirstURL(filterSource);
		Log.i(TAG, "POP--->URL:" + url);
		getFilterData(url);
	}

	@Override
	protected void getQuan10Data(String url) {
		// TODO Auto-generated method stub

		showDialog(DIALOG_WAITING);
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

		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, interfaceName);

		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
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

		searchAdapter.setList(srcList,true);
		lists[currentListIndex] = srcList;

		searchAdapter.notifyDataSetChanged();
	}


	@Override
	protected void getMoreFilterData(String url) {
		// TODO Auto-generated method stub
		getServiceData(url, "initMoreFilerServiceData");
	}

	@Override
	protected void getMoreBangDanData(String url) {
		// TODO Auto-generated method stub
		getServiceData(url, "initMoreBangDanServiceData");
	}


	@Override
	protected void filterPopWindowShow() {
		// TODO Auto-generated method stub

		if (popupWindow == null) {
			NavigateView view = new NavigateView(this);
			int[] location = new int[2];
			mFenLeiBtn.getLocationOnScreen(location);
			view.Init(
					getResources()
							.getStringArray(R.array.diqu_dianshiju_fenlei),
					getResources().getStringArray(
							R.array.leixing_dianshiju_fenlei), getResources()
							.getStringArray(R.array.shijian_dianying_fenlei),
					location[0], location[1], mFenLeiBtn.getWidth(), mFenLeiBtn
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
//									Toast.makeText(
//											ShowTVActivity.this,
//											"selected is " + choice[0] + ","
//													+ choice[1] + ","
//													+ choice[2],
//											Toast.LENGTH_LONG).show();
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
				| Gravity.BOTTOM, 0, 0);
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
			
			if(json == null || json.equals("")) 
				return;
			
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
	
	public void initMoreBangDanServiceData(String url, JSONObject json,
			AjaxStatus status) {
		// TODO Auto-generated method stub

		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {

			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}

		try {
			
			if(json == null || json.equals("")) 
				return;
			
			Log.d(TAG, json.toString());

			refreshAdpter(StatisticsUtils
					.returnTVBangDanList_YueDanListJson(json.toString()));
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
			// getFilterData(StatisticsUtils.getTV_QuanAllCacheURL(pageNum));
			getMoreFilterData(StatisticsUtils.getTV_QuanAllCacheURL(pageNum));
			break;
		case QUAN_TEN:

			break;
		case QUAN_FILTER:
			getMoreFilterData(StatisticsUtils.getFilter_DianshijuCacheURL(
					pageNum, filterSource));
			break;
		case SEARCH:
			getMoreFilterData(StatisticsUtils.getSearch_CacheURL(pageNum,
					search) + "&type=" + TV_TYPE);
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
			
			if(json == null || json.equals("")) 
				return;
			
			Log.d(TAG, json.toString());
			if (lists[QUAN_TEN] != null && !lists[QUAN_TEN].isEmpty()) {

				List<MovieItemData> temp10List = new ArrayList<MovieItemData>(
						lists[QUAN_TEN]);
				List<MovieItemData> tempList = new ArrayList<MovieItemData>();
				tempList = StatisticsUtils.returnFilterMovieSearch_TVJson(json
						.toString());

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

				Log.i(TAG, "Temp size:" + tempList.size());
				if (tempList.size() == StatisticsUtils.CACHE_NUM) {

					isNextPagePossibles[currentListIndex] = true;
				}
				
				if(isShowShoucang) {
					
					int tianchongEmpty = StatisticsUtils.tianchongEmptyItem(shoucangList.size());
					
					Log.i(TAG, "tianchongEmpty--->" + tianchongEmpty + " temp10List" + temp10List.size());
					
					List<MovieItemData> tempList2 = new ArrayList<MovieItemData>(
							shoucangList);
					
					for(int i=0;i<tianchongEmpty;i++) {
						
						MovieItemData item = new MovieItemData();
						tempList2.add(item);
						
					}
					
					Log.i(TAG, "tempList2 start--->" + tempList2.size());
					
					tempList2.addAll(temp10List);
					
					Log.i(TAG, "tempList2 end--->" + tempList2.size());
					
					notifyAdapter(tempList2);
				} else {
					
					notifyAdapter(temp10List);
				}
				

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
			
			if(json == null || json.equals("")) 
				return;
			
			Log.d(TAG, json.toString());

			notifyAdapter(StatisticsUtils
					.returnTVBangDanList_YueDanListJson(json.toString()));

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
			
			if(json == null || json.equals("")) 
				return;
			
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
	public void initQuan10ServiceData(String url, JSONObject json,
			AjaxStatus status) {
		// TODO Auto-generated method stub

		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {

			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		try {
			
			if(json == null || json.equals("")) 
				return;
			
			Log.d(TAG, json.toString());
			lists[QUAN_TEN] = StatisticsUtils
					.returnTVBangDanList_YueDanListJson(json.toString());
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
		
		if(isShowShoucang) {
			
			shoucangTitlleLL.setVisibility(View.VISIBLE);
		} else {
			
			shoucangTitlleLL.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void initView() {
		// TODO Auto-generated method stub

		searchEt = (EditText) findViewById(R.id.et_search);
		mFenLeiBtn = (Button) findViewById(R.id.bt_quanbufenlei);
		playGv = (MyMovieGridView) findViewById(R.id.gv_movie_show);

		dalujuLL = (LinearLayout) findViewById(R.id.ll_daluju);
		ganjuLL = (LinearLayout) findViewById(R.id.ll_gangju);
		taijuLL = (LinearLayout) findViewById(R.id.ll_taiju);
		hanjuLL = (LinearLayout) findViewById(R.id.ll_hanju);
		meijuLL = (LinearLayout) findViewById(R.id.ll_meiju);
		rijuLL = (LinearLayout) findViewById(R.id.ll_riju);

		zuijinguankanBtn = (Button) findViewById(R.id.bt_zuijinguankan);
		zhuijushoucangBtn = (Button) findViewById(R.id.bt_zhuijushoucang);

		topLinearLayout = (LinearLayout) findViewById(R.id.ll_show_movie_top);

		shoucangTitlleLL = (LinearLayout) findViewById(R.id.ll_shoucanggengxin);
		shoucangTv = (TextView) findViewById(R.id.tv_shoucanggengxin_name);

		playGv.setNextFocusLeftId(R.id.bt_quanbufenlei);
		playGv.setNextFocusUpId(R.id.gv_movie_show);
		playGv.setNextFocusDownId(R.id.gv_movie_show);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.i("Yangzhg", "onClick");

		if (activeView == null) {

			activeView = mFenLeiBtn;
		}

		if (v.getId() == R.id.bt_quanbufenlei
				&& activeView.getId() == R.id.bt_quanbufenlei) {

			filterPopWindowShow();
		}
		
		if(v.getId() == R.id.bt_zuijinguankan) {
			
			startActivity(new Intent(this, HistoryActivity.class));
			
			return;
		} else if( v.getId() == R.id.bt_zhuijushoucang) {
			
			startActivity(new Intent(this, ShowShoucangHistoryActivity.class));
			return;
		}

		if (activeView.getId() == v.getId()) {

			return;
		}

		switch (v.getId()) {
		case R.id.ll_daluju:
			String url1 = StatisticsUtils.getTV_DalujuFirstURL();
//			app.MyToast(aq.getContext(), "ll_daluju");
			currentListIndex = DALUJU;
			resetGvActive();
			if (lists[currentListIndex] != null
					&& !lists[currentListIndex].isEmpty()) {

				notifyAdapter(lists[currentListIndex]);
			} else {
				showDialog(DIALOG_WAITING);
				getUnQuanbuData(url1);
			}
			break;
		case R.id.ll_gangju:
			currentListIndex = GANGJU;
			String url2 = StatisticsUtils.getTV_GangjuFirstURL();
//			app.MyToast(aq.getContext(), "ll_gangju");
			resetGvActive();
			if (lists[currentListIndex] != null
					&& !lists[currentListIndex].isEmpty()) {

				notifyAdapter(lists[currentListIndex]);
			} else {
				showDialog(DIALOG_WAITING);
				getUnQuanbuData(url2);
			}
			break;
		case R.id.ll_taiju:
			currentListIndex = TAIJU;
			resetGvActive();
			String url3 = StatisticsUtils.getTV_TaijuFirstURL();
//			app.MyToast(aq.getContext(), "ll_taiju");
			if (lists[currentListIndex] != null
					&& !lists[currentListIndex].isEmpty()) {

				notifyAdapter(lists[currentListIndex]);
			} else {
				showDialog(DIALOG_WAITING);
				getUnQuanbuData(url3);
			}
			break;
		case R.id.ll_hanju:
			currentListIndex = HANJU;
			resetGvActive();
			String url4 = StatisticsUtils.getTV_HanjuFirstURL();
//			app.MyToast(aq.getContext(), "ll_hanju");
			if (lists[currentListIndex] != null
					&& !lists[currentListIndex].isEmpty()) {

				notifyAdapter(lists[currentListIndex]);
			} else {
				showDialog(DIALOG_WAITING);
				getUnQuanbuData(url4);
			}
			break;
		case R.id.ll_meiju:
			currentListIndex = MEIJU;
			resetGvActive();
			String url5 = StatisticsUtils.getTV_MeijuFirstURL();
//			app.MyToast(aq.getContext(), "ll_meiju");
			if (lists[currentListIndex] != null
					&& !lists[currentListIndex].isEmpty()) {

				notifyAdapter(lists[currentListIndex]);
			} else {
				showDialog(DIALOG_WAITING);
				getUnQuanbuData(url5);
			}
			break;
		case R.id.ll_riju:
			currentListIndex = RIJU;
			resetGvActive();
			String url6 = StatisticsUtils.getTV_RijuFirstURL();
//			app.MyToast(aq.getContext(), "ll_riju");
			if (lists[currentListIndex] != null
					&& !lists[currentListIndex].isEmpty()) {

				notifyAdapter(lists[currentListIndex]);
			} else {
				showDialog(DIALOG_WAITING);
				getUnQuanbuData(url6);
			}
			break;
		case R.id.bt_quanbufenlei:
			currentListIndex = QUANBUFENLEI;
			resetGvActive();
//			app.MyToast(aq.getContext(), "bt_quanbufenlei");
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

		playGv.setNextFocusLeftId(v.getId());
	}

	@Override
	protected void resetGvActive() {
		// TODO Auto-generated method stub
		
		if(currentListIndex == QUANBUFENLEI) {
			
			searchAdapter.setShoucangShow(true);
			shoucangTitlleLL.setVisibility(View.VISIBLE);
			
			if(shoucangList != null && !shoucangList.isEmpty()) {
				
				if(shoucangList.size() > 0) {
					
					Log.i(TAG, "shoucangList--->:" + shoucangList.size());
					
					isShowShoucang = true;
				}
			} else {
				
				isShowShoucang = false;
				searchAdapter.setShoucangShow(false);
				shoucangTitlleLL.setVisibility(View.GONE);
			}
		} else {
			
			searchAdapter.setShoucangShow(false);
			shoucangTitlleLL.setVisibility(View.GONE);
			isShowShoucang = false;
		}
		
		mSparseArray.clear();
		activeRecordIndex = -1;
		isCurrentKeyVertical = false;
		isFirstActive = false;
	}
	
	protected void initFirstFloatView(int position,View view) {
		
	}

}
