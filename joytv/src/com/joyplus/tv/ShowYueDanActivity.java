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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.joyplus.tv.Adapters.YueDanAdapter;
import com.joyplus.tv.entity.MovieItemData;
import com.joyplus.tv.ui.MyMovieGridView;
import com.joyplus.tv.ui.WaitingDialog;
import com.joyplus.tv.utils.ItemStateUtils;

public class ShowYueDanActivity extends AbstractShowActivity {

	public static final int DIANYING_YUEDAN = 1;
	public static final int DIANSHIJU_YUEDAN = 2;

	private static final int DIANYING = 4;
	private static final int DIANSHI = 5;

	public static final String TAG = "ShowYueDanActivity";
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
	private YueDanAdapter searchAdapter = null;
	private int beforepostion = 0;
	private int currentListIndex;
	private String search;
	private String filterSource;
	private PopupWindow popupWindow;

	private int activeRecordIndex = -1;

	private Button zuijinguankanBtn, zhuijushoucangBtn, dianyingyuedanBtn,
			dianshijuyuedanBtn;
	private List<MovieItemData>[] lists = new List[6];
	private boolean[] isNextPagePossibles = new boolean[6];
	private int[] pageNums = new int[6];
	private int defalutYuedan = 0;

	private boolean isFirstActive = false;
	private View firstFloatView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_yuedan);

		app = (App) getApplication();
		aq = new AQuery(this);

		Intent intent = getIntent();

		String yuedanType = intent.getStringExtra("yuedan_type");

		if (yuedanType != null && !yuedanType.equals("")) {

			int tempInt = Integer.valueOf(yuedanType);
			if (tempInt == DIANSHIJU_YUEDAN || tempInt == DIANYING_YUEDAN) {

				defalutYuedan = tempInt;
			}
		}

		initActivity();

		searchAdapter = new YueDanAdapter(this, aq);
		playGv.setAdapter(searchAdapter);
		isFirstActive = true;
		if (defalutYuedan == DIANYING_YUEDAN) {

			String url = StatisticsUtils.getYueDan_DianyingFirstURL();
			Log.i(TAG, "URL--->" + url);
			showDialog(DIALOG_WAITING);
			getUnQuanbuData(url);// 进入电影界面时，全部分类电影显示获取焦点，并且显示数据
		} else if (defalutYuedan == DIANSHIJU_YUEDAN) {
			showDialog(DIALOG_WAITING);
			String url = StatisticsUtils.getYueDan_DianshiFirstURL();
			getUnQuanbuData(url);// 进入电影界面时，全部分类电影显示获取焦点，并且显示数据
		}

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
		aq.id(R.id.iv_head_user_icon).image(
				app.getUserInfo().getUserAvatarUrl(), false, true, 0,
				R.drawable.avatar_defult);
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
	protected void initViewListener() {
		// TODO Auto-generated method stub

		zuijinguankanBtn.setOnKeyListener(this);
		zhuijushoucangBtn.setOnKeyListener(this);
		dianyingyuedanBtn.setOnKeyListener(this);
		dianshijuyuedanBtn.setOnKeyListener(this);

		zuijinguankanBtn.setOnClickListener(this);
		zhuijushoucangBtn.setOnClickListener(this);
		dianyingyuedanBtn.setOnClickListener(this);
		dianshijuyuedanBtn.setOnClickListener(this);

		zuijinguankanBtn.setOnFocusChangeListener(this);
		zhuijushoucangBtn.setOnFocusChangeListener(this);
		dianyingyuedanBtn.setOnFocusChangeListener(this);
		dianshijuyuedanBtn.setOnFocusChangeListener(this);

		playGv.setOnKeyListener(new View.OnKeyListener() {

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

				}
				return false;
			}
		});

		playGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				List<MovieItemData> list = searchAdapter.getMovieList();
				if (list != null && !list.isEmpty()) {

					String num = list.get(position).getNum();
					if (num != null && !num.equals("")) {

						Intent intent = new Intent(ShowYueDanActivity.this,
								ShowYueDanListActivity.class);
						Bundle bundle = new Bundle();

						bundle.putString("NAME", list.get(position)
								.getMovieName());
						bundle.putString("ID", list.get(position).getMovieID());
						intent.putExtras(bundle);

						startActivity(intent);
					} else {

						String pro_type = list.get(position).getMovieProType();
						Log.i(TAG, "pro_type:" + pro_type);
						if (pro_type != null && !pro_type.equals("")) {
							Intent intent = new Intent();
							if (pro_type.equals("2")) {
								Log.i(TAG, "pro_type:" + pro_type + "   --->2");
								intent.setClass(ShowYueDanActivity.this,
										ShowXiangqingTv.class);
								intent.putExtra("ID", list.get(position)
										.getMovieID());
							} else if (pro_type.equals("1")) {
								Log.i(TAG, "pro_type:" + pro_type + "   --->1");
								intent.setClass(ShowYueDanActivity.this,
										ShowXiangqingMovie.class);
							} else if (pro_type.equals("131")) {

								intent.setClass(ShowYueDanActivity.this,
										ShowXiangqingDongman.class);
							} else if (pro_type.equals("3")) {

								intent.setClass(ShowYueDanActivity.this,
										ShowXiangqingZongYi.class);
							}

							intent.putExtra("ID", list.get(position)
									.getMovieID());

							intent.putExtra("prod_url", list.get(position)
									.getMoviePicUrl());
							intent.putExtra("prod_name", list.get(position)
									.getMovieName());
							intent.putExtra("stars", list.get(position)
									.getStars());
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
			}
		});

		playGv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, final View view,
					int position, long id) {
				// TODO Auto-generated method stub
				// if (BuildConfig.DEBUG)
				Log.i(TAG, "Positon:" + position + " View:" + view
						+ " beforGvView:" + beforeGvView);

				if (view == null) {

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

							// playGv.smoothScrollBy(-popHeight, 1000);
							isSmoonthScroll = true;
						}
					} else {

						if (!isGridViewUp) {

							// playGv.smoothScrollBy(popHeight, 1000 * 2);
							isSmoonthScroll = true;

						}
					}

				}

				if (firstFloatView.isShown()) {

					ItemStateUtils.floatViewOutAnimaiton(
							getApplicationContext(), firstFloatView);
				}

				if (beforeGvView != null && beforeGvView != view
						&& activeRecordIndex != -1) {

					ItemStateUtils.viewOutAnimation(getApplicationContext(),
							beforeGvView);
				}

				if (position != activeRecordIndex) {

					ItemStateUtils.viewInAnimation(getApplicationContext(),
							view);
					activeRecordIndex = position;
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

				beforeGvView = view;
				beforepostion = position;

				// 缓存
				int size = searchAdapter.getMovieList().size();
				if (size - 1 - firstAndLastVisible[1] < StatisticsUtils.CACHE_NUM) {

					if (isNextPagePossibles[currentListIndex]) {

						pageNums[currentListIndex]++;
						playGv.setOnFocusChangeListener(null);
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
				searchEt.setText("");
				playGv.setNextFocusForwardId(searchEt.getId());//
				showDialog(DIALOG_WAITING);
				ItemStateUtils
						.viewToNormal(getApplicationContext(), activeView);
				activeView = searchEt;
				resetGvActive();

				if (searchStr != null && !searchStr.equals("")) {

					search = searchStr;
					StatisticsUtils.clearList(lists[SEARCH]);
					currentListIndex = SEARCH;
					
					String url = StatisticsUtils.getSearch_FirstURL(searchStr)
							+ "&type=" + TV_TYPE +","+MOVIE_TYPE;
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

	private View.OnFocusChangeListener gvOnFocusChangeListener = new View.OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub

			if (!hasFocus) {// 如果gridview没有获取焦点，把item中高亮取消

				if (firstFloatView.isShown()) {

					ItemStateUtils.floatViewOutAnimaiton(
							getApplicationContext(), firstFloatView);
				}

				if (beforeGvView != null) {

					ItemStateUtils.viewOutAnimation(getApplicationContext(),
							beforeGvView);

				}
			} else {
				// Log.i(TAG, "OnFocusChangeListener--->" + beforeGvView +
				// " Height:" + popHeight
				// + " position:" + playGv.getSelectedItemPosition() + "viewY:"
				// + beforeGvView.getY());
				int beforePostion = playGv.getSelectedItemPosition();
				if (beforeGvView != null) {

					// ItemStateUtils.viewInAnimation(
					// getApplicationContext(), beforeGvView);
					if (beforeGvView.getY() < popHeight / 2) {
						initFirstFloatView(playGv.getSelectedItemPosition(),
								beforeGvView);
					}
					activeRecordIndex = -1;
					beforeGvView = null;
					// playGv.setSelection(beforePostion);

				} else {

					initFirstFloatView(0, null);
					// playGv.setSelection(playGv.getSelectedItemPosition());
				}
			}
		}
	};

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

		searchAdapter.setList(list);

		if (list.size() <= 0) {

			playGv.setAdapter(null);
		} else {

			ListAdapter adapter = playGv.getAdapter();
			if (adapter == null) {

				playGv.setAdapter(searchAdapter);
			} else {

				if (!isFirstActive) {

					playGv.setAdapter(searchAdapter);
				}
			}
		}

		if (list != null && !list.isEmpty() && currentListIndex != QUANBUFENLEI) {// 判断其能否向获取更多数据

			if (list.size() == StatisticsUtils.FIRST_NUM) {

				isNextPagePossibles[currentListIndex] = true;
			} else if (list.size() < StatisticsUtils.FIRST_NUM) {

				isNextPagePossibles[currentListIndex] = false;
			}
		}
		lists[currentListIndex] = list;

		// playGv.setSelection(0);
		searchAdapter.notifyDataSetChanged();
		removeDialog(DIALOG_WAITING);
		if (isFirstActive) {

			playGv.requestFocus();
			isFirstActive = false;
		}
		beforeGvView = null;
		activeRecordIndex = -1;
		playGv.setOnFocusChangeListener(gvOnFocusChangeListener);

	}

	@Override
	protected void filterVideoSource(String[] choice) {
		// TODO Auto-generated method stub

		String quanbu = getString(R.string.quanbu_name);
		String quanbufenlei = getString(R.string.quanbufenlei_name);
		String tempStr = StatisticsUtils.getQuanBuFenLeiName(choice,
				quanbufenlei, quanbu);

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
		filterSource = StatisticsUtils.getFileterURL3Param(choice, quanbu);
		String url = StatisticsUtils.getFilter_DongmanFirstURL(filterSource);
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

		searchAdapter.setList(srcList);
		lists[currentListIndex] = srcList;

		searchAdapter.notifyDataSetChanged();
		playGv.setOnFocusChangeListener(gvOnFocusChangeListener);
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

	}

	protected void getMoreTopData(String url) {
		// TODO Auto-generated method stub
		getServiceData(url, "initMoreTopServiceData");
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

	public void initMoreTopServiceData(String url, JSONObject json,
			AjaxStatus status) {
		// TODO Auto-generated method stub
		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {

			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}

		try {
			Log.d(TAG, json.toString());
			refreshAdpter(StatisticsUtils.returnTopsJson(json.toString()));
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
	public void initMoreBangDanServiceData(String url, JSONObject json,
			AjaxStatus status) {
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
			getMoreFilterData(StatisticsUtils.getSearch_CacheURL(pageNum,
					search)+ "&type=" + TV_TYPE +","+MOVIE_TYPE);
			break;
		case DIANYING:
			getMoreTopData(StatisticsUtils.getYueDan_DianyingCacheURL(pageNum));
			break;
		case DIANSHI:
			getMoreTopData(StatisticsUtils.getYueDan_DianshiCacheURL(pageNum));
			break;

		default:
			break;
		}
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

		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {

			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		try {
			Log.d(TAG, json.toString());
			notifyAdapter(StatisticsUtils.returnTopsJson(json.toString()));
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

	}

	@Override
	protected void initViewState() {
		// TODO Auto-generated method stub

		if (defalutYuedan == DIANYING_YUEDAN) {

			activeView = dianyingyuedanBtn;
			ItemStateUtils.buttonToActiveState(getApplicationContext(),
					dianyingyuedanBtn);
		} else {

			activeView = dianshijuyuedanBtn;
			ItemStateUtils.buttonToActiveState(getApplicationContext(),
					dianshijuyuedanBtn);
		}

		ItemStateUtils.setItemPadding(zuijinguankanBtn);
		ItemStateUtils.setItemPadding(zhuijushoucangBtn);
		ItemStateUtils.setItemPadding(dianyingyuedanBtn);
		ItemStateUtils.setItemPadding(dianshijuyuedanBtn);
	}

	@Override
	protected void initView() {
		// TODO Auto-generated method stub

		searchEt = (EditText) findViewById(R.id.et_search);
		dianyingyuedanBtn = (Button) findViewById(R.id.bt_dianyingyuedan);
		dianshijuyuedanBtn = (Button) findViewById(R.id.bt_dianshijuyuedan);
		playGv = (MyMovieGridView) findViewById(R.id.gv_movie_show);

		zuijinguankanBtn = (Button) findViewById(R.id.bt_zuijinguankan);
		zhuijushoucangBtn = (Button) findViewById(R.id.bt_zhuijushoucang);
		firstFloatView = findViewById(R.id.inclue_movie_show_item);

		playGv.setNextFocusLeftId(R.id.bt_dianyingyuedan);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.i("Yangzhg", "onClick");

		if (activeView == null) {

			if (defalutYuedan == DIANYING_YUEDAN) {
				activeView = dianyingyuedanBtn;
			} else {

				activeView = dianshijuyuedanBtn;
			}

		}

		if (activeView.getId() == v.getId()) {

			return;
		}

		switch (v.getId()) {
		case R.id.bt_dianyingyuedan:
			currentListIndex = DIANYING;
			resetGvActive();
			String url1 = StatisticsUtils.getYueDan_DianyingFirstURL();
			app.MyToast(aq.getContext(), "ll_daluju");
			if (lists[currentListIndex] != null
					&& !lists[currentListIndex].isEmpty()) {

				notifyAdapter(lists[currentListIndex]);
			} else {
				showDialog(DIALOG_WAITING);
				getUnQuanbuData(url1);
			}
			break;
		case R.id.bt_dianshijuyuedan:
			currentListIndex = DIANSHI;

			resetGvActive();
			String url2 = StatisticsUtils.getYueDan_DianshiFirstURL();
			app.MyToast(aq.getContext(), "ll_gangju");
			if (lists[currentListIndex] != null
					&& !lists[currentListIndex].isEmpty()) {

				notifyAdapter(lists[currentListIndex]);
			} else {
				showDialog(DIALOG_WAITING);
				getUnQuanbuData(url2);
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
		playGv.setOnFocusChangeListener(null);
		// playGv.setSelection(-1);
		activeRecordIndex = -1;
	}

	protected void initFirstFloatView(int position, View view) {

		firstFloatView.setLayoutParams(new FrameLayout.LayoutParams(popWidth,
				popHeight));
		firstFloatView.setVisibility(View.VISIBLE);

		if (view != null) {
			Log.i(TAG, "X:" + view.getX() + "Y: " + view.getY());
			firstFloatView.setX(view.getX());
			firstFloatView.setY(view.getY());
		} else {

			firstFloatView.setX(0);
			firstFloatView.setY(0);
		}

		TextView movieName = (TextView) firstFloatView
				.findViewById(R.id.tv_item_layout_name);
		TextView movieScore = (TextView) firstFloatView
				.findViewById(R.id.tv_item_layout_score);

		List<MovieItemData> list = searchAdapter.getMovieList();
		if (list != null && !list.isEmpty()) {

			aq = new AQuery(firstFloatView);
			aq.id(R.id.iv_item_layout_haibao).image(
					list.get(position).getMoviePicUrl(), true, true, 0,
					R.drawable.post_active);

			movieName.setText(list.get(position).getMovieName());

			String proType = list.get(position).getMovieProType();

			TextView movieUpdate = (TextView) firstFloatView
					.findViewById(R.id.tv_item_layout_other_info);

			if (proType != null && !proType.equals("")) {

				if (proType.equals("1")) {

					movieScore.setText(list.get(position).getMovieScore());
					String duration = list.get(position).getMovieDuration();
					if (duration != null && !duration.equals("")) {

						movieUpdate.setText(StatisticsUtils
								.formatMovieDuration(duration));
					}
				} else if (proType.equals("2") || proType.equals("131")) {

					movieScore.setText(list.get(position).getMovieScore());
					String curEpisode = list.get(position).getMovieCurEpisode();
					String maxEpisode = list.get(position).getMovieMaxEpisode();

					if (maxEpisode != null && !maxEpisode.equals("")) {

						if (curEpisode == null || curEpisode.equals("0")) {

							movieUpdate.setText(maxEpisode
									+ getString(R.string.dianshiju_jiquan));
						} else {

							int max = Integer.valueOf(maxEpisode);
							int min = Integer.valueOf(curEpisode);

							if (min >= max) {

								movieUpdate.setText(maxEpisode
										+ getString(R.string.dianshiju_jiquan));
							} else {

								movieUpdate
										.setText(getString(R.string.zongyi_gengxinzhi)
												+ curEpisode);
							}

						}
					}

				} else if (proType.equals("3")) {

					String curEpisode = list.get(position).getMovieCurEpisode();
					if (curEpisode != null && !curEpisode.equals("")) {

						movieUpdate
								.setText(getString(R.string.zongyi_gengxinzhi)
										+ list.get(position)
												.getMovieCurEpisode());
					}
				}
			}

			// ItemStateUtils.floatViewInAnimaiton(getApplicationContext(),
			// firstFloatView);
			ItemStateUtils.floatViewInAnimaiton(getApplicationContext(),
					firstFloatView);
		}
	}

}
