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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.joyplus.tv.Adapters.YueDanAdapter;
import com.joyplus.tv.entity.MovieItemData;
import com.joyplus.tv.ui.MyMovieGridView;
import com.joyplus.tv.utils.ItemStateUtils;

public class ShowYueDanActivity extends AbstractShowActivity {

	public static final int DIANYING_YUEDAN = 1;
	public static final int DIANSHIJU_YUEDAN = 2;

	private String TAG = "ShowYueDanActivity";
	private AQuery aq;
	private App app;

	private EditText searchEt;
	private MyMovieGridView dinashijuGv;

	private Button zuijinguankanBtn, zhuijushoucangBtn, dianyingyuedanBtn,
			dianshijuyuedanBtn;

	private View firstFloatView;

	private View activeView;

	private boolean isSelectedItem = true;// GridView中参数是否真正初始化

	private int popWidth, popHeight;

	private boolean isGridViewUp = false;

	private int[] beforeFirstAndLastVible = { 0, 9 };

	private View beforeGvView = null;

	private List<MovieItemData> dianyingYueDanList = new ArrayList<MovieItemData>();
	private List<MovieItemData> dianshijuYueDanList = new ArrayList<MovieItemData>();
	private List<MovieItemData> filterList = new ArrayList<MovieItemData>();

	private List<MovieItemData>[] lists = null;

	private int defalutYuedan = 0;

	private int currentItemPostion;

	private YueDanAdapter yueDanAdapter;

	private int beforepostion = 0;

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

		currentItemPostion = defalutYuedan;

		initActivity();

		yueDanAdapter = new YueDanAdapter(this,aq);
		dinashijuGv.setAdapter(yueDanAdapter);

		if (defalutYuedan == DIANYING_YUEDAN) {

			String url = StatisticsUtils.getTopURL(TOP_URL, 1 + "", 50 + "",
					1 + "");
			Log.i(TAG, "URL--->" + url);
			getUnQuanbuData(url);// 进入电影界面时，全部分类电影显示获取焦点，并且显示数据
		} else if (defalutYuedan == DIANSHIJU_YUEDAN) {

			String url = StatisticsUtils.getTopURL(TOP_URL, 1 + "", 50 + "",
					2 + "");
			getUnQuanbuData(url);// 进入电影界面时，全部分类电影显示获取焦点，并且显示数据
		}

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
		int action = event.getAction();
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
			currentItemPostion = 0;
			String url1 = StatisticsUtils.getTopURL(TOP_URL, 1 + "", 50 + "",
					1 + "");
			app.MyToast(aq.getContext(), "ll_daluju");
			if (dianyingYueDanList != null && !dianyingYueDanList.isEmpty()) {

				notifyAdapter(dianyingYueDanList);
			} else {

				getUnQuanbuData(url1);
			}
			break;
		case R.id.bt_dianshijuyuedan:
			currentItemPostion = 1;
			String url2 = StatisticsUtils.getTopURL(TOP_URL, 1 + "", 50 + "",
					2 + "");
			app.MyToast(aq.getContext(), "ll_gangju");
			if (dianshijuYueDanList != null && !dianshijuYueDanList.isEmpty()) {

				notifyAdapter(dianshijuYueDanList);
			} else {

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

		beforeGvView = null;
	}

	@Override
	protected void initView() {
		// TODO Auto-generated method stub

		searchEt = (EditText) findViewById(R.id.et_search);
		dianyingyuedanBtn = (Button) findViewById(R.id.bt_dianyingyuedan);
		dianshijuyuedanBtn = (Button) findViewById(R.id.bt_dianshijuyuedan);
		dinashijuGv = (MyMovieGridView) findViewById(R.id.gv_movie_show);

		zuijinguankanBtn = (Button) findViewById(R.id.bt_zuijinguankan);
		zhuijushoucangBtn = (Button) findViewById(R.id.bt_zhuijushoucang);

		firstFloatView = findViewById(R.id.inclue_movie_show_item);

		dinashijuGv.setNextFocusLeftId(R.id.bt_dianyingyuedan);
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
						List<MovieItemData> list = yueDanAdapter.getMovieList();
						if (list != null && !list.isEmpty()) {

							Intent intent = new Intent(ShowYueDanActivity.this,
									ShowYueDanListActivity.class);
							Bundle bundle = new Bundle();
							bundle.putString("ID", list.get(position)
									.getMovieID());
							bundle.putString("NAME", list.get(position)
									.getMovieName());
							intent.putExtras(bundle);
							startActivity(intent);
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

							ItemStateUtils.viewOutAnimation(
									getApplicationContext(), beforeGvView);
						} else {

							ItemStateUtils
									.floatViewOutAnimaiton(firstFloatView);
						}

						ItemStateUtils.viewInAnimation(getApplicationContext(),
								view);

						int[] firstAndLastVisible = new int[2];
						firstAndLastVisible[0] = dinashijuGv
								.getFirstVisiblePosition();
						firstAndLastVisible[1] = dinashijuGv
								.getLastVisiblePosition();

						if (y == 0 || y - popHeight == 0) {// 顶部没有渐影

							beforeFirstAndLastVible = ItemStateUtils
									.reCaculateFirstAndLastVisbile(
											beforeFirstAndLastVible,
											firstAndLastVisible,
											isSmoonthScroll, false);

						} else {// 顶部有渐影

							beforeFirstAndLastVible = ItemStateUtils
									.reCaculateFirstAndLastVisbile(
											beforeFirstAndLastVible,
											firstAndLastVisible,
											isSmoonthScroll, true);

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
	protected void clearLists() {
		// TODO Auto-generated method stub

		StatisticsUtils.clearList(dianyingYueDanList);
		StatisticsUtils.clearList(dianshijuYueDanList);
		StatisticsUtils.clearList(filterList);
	}

	@Override
	protected void initLists() {
		// TODO Auto-generated method stub

		lists = new List[2];
		lists[0] = dianyingYueDanList;
		lists[1] = dianshijuYueDanList;
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
				.findViewById(R.id.tv_item_layout_other_info);

		List<MovieItemData> list = yueDanAdapter.getMovieList();
		if (list != null && !list.isEmpty()) {

			FrameLayout inFrameLayout = (FrameLayout) firstFloatView
					.findViewById(R.id.inclue_movie_show_item);
			ImageView haibaoIv = (ImageView) inFrameLayout
					.findViewById(R.id.iv_item_layout_haibao);
			aq.id(haibaoIv).image(list.get(0).getMoviePicUrl(), true, true, 0,
					R.drawable.post_active);
			movieName.setText(list.get(0).getMovieName());
			movieScore.setText(list.get(0).getMovieName()
					+ getString(R.string.yingpianshu));
		}

		ItemStateUtils.floatViewInAnimaiton(getApplicationContext(),
				firstFloatView);
	}

	@Override
	protected void notifyAdapter(List<MovieItemData> list) {
		// TODO Auto-generated method stub

		int height = yueDanAdapter.getHeight(), width = yueDanAdapter
				.getWidth();

		if (height != 0 && width != 0) {

			popWidth = width;
			popHeight = height;
		}

		yueDanAdapter.setList(list);

		dinashijuGv.setSelection(0);
		yueDanAdapter.notifyDataSetChanged();
		beforeGvView = null;
		initFirstFloatView();
		dinashijuGv.setFocusable(true);
		dinashijuGv.setSelected(true);
		isSelectedItem = false;
		dinashijuGv.requestFocus();
	}

	@Override
	protected void filterVideoSource(String[] choice) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void getQuan10Data(String url) {
		// TODO Auto-generated method stub

		currentItemPostion = -1;

		getServiceData(url, "initQuan10ServiceData");
	}

	@Override
	protected void getQuanbuData(String url) {
		// TODO Auto-generated method stub

		currentItemPostion = -1;

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

		currentItemPostion = -1;

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

		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {

			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		try {
			Log.d(TAG, json.toString());
			if (currentItemPostion != -1) {

				if (currentItemPostion >= 0
						&& currentItemPostion < lists.length) {

					lists[currentItemPostion] = StatisticsUtils
							.returnTopsJson(json.toString());

					notifyAdapter(lists[currentItemPostion]);
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
			StatisticsUtils.clearList(filterList);
			filterList = StatisticsUtils.returnFilterMovieSearchJson(json
					.toString());

			notifyAdapter(filterList);
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
		
	}

	@Override
	public void initMoreFilerServiceData(String url, JSONObject json,
			AjaxStatus status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void getMoreFilterData(String url) {
		// TODO Auto-generated method stub
		
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

}
