package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.tv.Service.Return.ReturnTops;
import com.joyplus.tv.entity.GridViewItemHodler;
import com.joyplus.tv.entity.YueDanInfo2;
import com.joyplus.tv.ui.MyMovieGridView;
import com.joyplus.tv.utils.BangDanKey;
import com.joyplus.tv.utils.ItemStateUtils;
import com.joyplus.tv.utils.JieMianConstant;
import com.joyplus.tv.utils.MyKeyEventKey;

public class ShowYueDanActivity extends Activity implements View.OnKeyListener,
MyKeyEventKey, BangDanKey, JieMianConstant, View.OnClickListener,
View.OnFocusChangeListener {
	
	public static final int DIANYING_YUEDAN = 1;
	public static final int DIANSHIJU_YUEDAN = 2;

	private String TAG = "ShowYueDanActivity";
	private AQuery aq;
	private App app;

	private EditText searchEt;
	private MyMovieGridView dinashijuGv;

	private Button zuijinguankanBtn, zhuijushoucangBtn,
			dianyingyuedanBtn,dianshijuyuedanBtn;
	
	private View firstFloatView ;

	private View activeView;

	private boolean isSelectedItem = true;// GridView中参数是否真正初始化

	private int popWidth, popHeight;

	private boolean isGridViewUp = false;

	private int[] beforeFirstAndLastVible = { 0, 9 };

	private View beforeGvView = null;

	private ObjectMapper mapper = new ObjectMapper();

	private List<YueDanInfo2> movieList = new ArrayList<YueDanInfo2>();
	
	private int defalutYuedan = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_yuedan);

		app = (App) getApplication();
		aq = new AQuery(this);

		Intent intent = getIntent();
		
		String yuedanType = intent.getStringExtra("yuedan_type");
		
		if(yuedanType != null && !yuedanType.equals("")) {
			
			int tempInt = Integer.valueOf(yuedanType);
			if(tempInt == DIANSHIJU_YUEDAN || tempInt == DIANYING_YUEDAN) {
				
				defalutYuedan = tempInt;
			}
		}
		
		initView();
		initState();
		
		dinashijuGv.setAdapter(movieAdapter);
		
		if(defalutYuedan == DIANYING_YUEDAN) {
			
			String url = StatisticsUtils.getTopURL(TOP_URL, 1+"", 50 + "", 1+ "");
			Log.i(TAG, "URL--->" + url);
			getServiceData(url);// 进入电影界面时，全部分类电影显示获取焦点，并且显示数据
		} else if(defalutYuedan == DIANSHIJU_YUEDAN){
			
			String url = StatisticsUtils.getTopURL(TOP_URL, 1+"", 50 + "", 2+ "");
			getServiceData(url);// 进入电影界面时，全部分类电影显示获取焦点，并且显示数据
		}

		dinashijuGv.setSelected(true);
		dinashijuGv.requestFocus();
		dinashijuGv.setSelection(0);
	}

	private void initView() {

		searchEt = (EditText) findViewById(R.id.et_search);
		dianyingyuedanBtn = (Button) findViewById(R.id.bt_dianyingyuedan);
		dianshijuyuedanBtn = (Button) findViewById(R.id.bt_dianshijuyuedan);
		dinashijuGv = (MyMovieGridView) findViewById(R.id.gv_movie_show);

		zuijinguankanBtn = (Button) findViewById(R.id.bt_zuijinguankan);
		zhuijushoucangBtn = (Button) findViewById(R.id.bt_zhuijushoucang);
		
		firstFloatView = findViewById(R.id.inclue_movie_show_item);
		
		dinashijuGv.setNextFocusLeftId(R.id.bt_dianyingyuedan);

		addListener();

	}
	
	private void initState() {

		if(defalutYuedan == DIANYING_YUEDAN) {
			
			activeView = dianyingyuedanBtn;
			ItemStateUtils.buttonToActiveState(getApplicationContext(), dianyingyuedanBtn);
		}else {
			
			activeView = dianshijuyuedanBtn;
			ItemStateUtils.buttonToActiveState(getApplicationContext(), dianshijuyuedanBtn);
		}
		
		ItemStateUtils.setItemPadding(zuijinguankanBtn);
		ItemStateUtils.setItemPadding(zhuijushoucangBtn);
		ItemStateUtils.setItemPadding(dianyingyuedanBtn);
		ItemStateUtils.setItemPadding(dianshijuyuedanBtn);

	}

	private int beforepostion = 0;

	private void addListener() {

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

		dinashijuGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ShowYueDanActivity.this,
						ShowYueDanListActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("ID", movieList.get(position).id);
				bundle.putString("NAME", movieList.get(position).name);
				intent.putExtras(bundle);
				startActivity(intent);
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
//					getFilterServiceData(url);
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
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		 Log.i("Yangzhg", "onClick");

			if(activeView == null) {
				
				if(defalutYuedan == DIANYING_YUEDAN) {
					activeView = dianyingyuedanBtn;
				} else {
					
					activeView = dianshijuyuedanBtn;
				}
				
			}
			
			if(activeView.getId() == v.getId()) {
				
				return;
			}
			
			switch (v.getId()) {
			case R.id.bt_dianyingyuedan:
				String url1 = StatisticsUtils.getTopURL(TOP_URL, 1+"", 50 + "", 1+ "");
				app.MyToast(aq.getContext(),"ll_daluju");
				getServiceData(url1);
				break;
			case R.id.bt_dianshijuyuedan:
				String url2 = StatisticsUtils.getTopURL(TOP_URL, 1+"", 50 + "", 2+ "");
				app.MyToast(aq.getContext(),"ll_gangju");
				getServiceData(url2);
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

	private void getServiceData(String url) {

		firstFloatView.setVisibility(View.INVISIBLE);
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
			ReturnTops result = mapper.readValue(json.toString(),
					ReturnTops.class);
			// hot_list.clear();
			if(movieList != null && !movieList.isEmpty()) {
				
				movieList.clear();
			}
			for(int i=0; i<result.tops.length; i++){
				YueDanInfo2 yuedanInfo = new YueDanInfo2();
//				ArrayList<ShiPinInfoParcelable> tempList = new ArrayList<ShiPinInfoParcelable>();
				yuedanInfo.name = result.tops[i].name;
				yuedanInfo.id = result.tops[i].id;
				yuedanInfo.prod_type = result.tops[i].prod_type;
				String bigPicUrl = result.tops[i].big_pic_url;
				if(bigPicUrl == null || bigPicUrl.equals("")) {
					
					bigPicUrl = result.tops[i].pic_url;
				}
				yuedanInfo.pic_url = bigPicUrl;
				yuedanInfo.num = result.tops[i].num;
				yuedanInfo.content = result.tops[i].content;
//				for (int j = 0; j < result.tops[i].items.length; j++) {
//					ShiPinInfoParcelable shipinInfo = new ShiPinInfoParcelable();
//					shipinInfo.setArea(result.tops[i].items[j].area);
//					shipinInfo.setBig_prod_pic_url(result.tops[i].items[j].big_prod_pic_url);
//					shipinInfo.setCur_episode(result.tops[i].items[j].cur_episode);
////					shipinInfo.setCur_item_name(result.tops[i].items[j].cur_i);
//					shipinInfo.setDefinition(result.tops[i].items[j].definition);
//					shipinInfo.setDirectors(result.tops[i].items[j].directors);
//					shipinInfo.setDuration(result.tops[i].items[j].duration);
//					shipinInfo.setFavority_num(result.tops[i].items[j].favority_num);
//					shipinInfo.setId(result.tops[i].items[j].id);
//					shipinInfo.setMax_episode(result.tops[i].items[j].max_episode);
//					shipinInfo.setProd_id(result.tops[i].items[j].prod_id);
//					shipinInfo.setProd_name(result.tops[i].items[j].prod_name);
//					shipinInfo.setProd_pic_url(result.tops[i].items[j].prod_pic_url);
//					shipinInfo.setProd_type(result.tops[i].items[j].prod_type);
//					shipinInfo.setPublish_date(result.tops[i].items[j].publish_date);
//					shipinInfo.setScore(result.tops[i].items[j].score);
//					shipinInfo.setStars(result.tops[i].items[j].stars);
//					shipinInfo.setSupport_num(result.tops[i].items[j].support_num);
//					tempList.add(shipinInfo);
//				}
//				yuedanInfo.shiPinList = tempList;
				movieList.add(yuedanInfo);
				
			}
			// Log.d

			movieAdapter.notifyDataSetChanged();
			beforeGvView = null;
			initFirstFloatView();
			dinashijuGv.setFocusable(true);
			dinashijuGv.setSelected(true);
			isSelectedItem = false;
			dinashijuGv.requestFocus();
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
		
		TextView movieName = (TextView) firstFloatView.findViewById(R.id.tv_item_layout_name);
		TextView movieScore = (TextView) firstFloatView.findViewById(R.id.tv_item_layout_other_info);
		
		if(movieList.size() > 0) {
			
			aq = new AQuery(firstFloatView);
			aq.id(R.id.iv_item_layout_haibao).image(movieList.get(0).pic_url, 
					true, true,0, R.drawable.post_active);
			movieName.setText(movieList.get(0).name);
			movieScore.setText(movieList.get(0).num + getString(R.string.yingpianshu));
		}
		
		ItemStateUtils.floatViewInAnimaiton(getApplicationContext(),
				firstFloatView);
	}


	private BaseAdapter movieAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			GridViewItemHodler viewItemHodler = null;

			int width = parent.getWidth() / 5;
			int height = (int) (width / 1.0f / STANDARD_PIC_WIDTH * STANDARD_PIC_HEIGHT);

			if (convertView == null) {
				viewItemHodler = new GridViewItemHodler();
				convertView = getLayoutInflater().inflate(
						R.layout.show_item_layout_dianying, null);
				viewItemHodler.nameTv = (TextView) convertView.findViewById(R.id.tv_item_layout_name);
				viewItemHodler.scoreTv = (TextView) convertView.findViewById(R.id.tv_item_layout_score);
				viewItemHodler.otherInfo = (TextView) convertView.findViewById(R.id.tv_item_layout_other_info);
				convertView.setTag(viewItemHodler);
				
			} else {

				viewItemHodler = (GridViewItemHodler) convertView.getTag();
			}
			
			AbsListView.LayoutParams params = new AbsListView.LayoutParams(
					width, height);
			convertView.setLayoutParams(params);
			convertView.setPadding(GRIDVIEW_ITEM_PADDING_LEFT, GRIDVIEW_ITEM_PADDING,
					GRIDVIEW_ITEM_PADDING_LEFT, GRIDVIEW_ITEM_PADDING);
			
			if (width != 0) {

				popWidth = width;
				popHeight = height;
				// Log.i(TAG, "Width:" + popWidth);
			}
			
			if(movieList.size() <= 0) {
				
				return convertView;
			}

			viewItemHodler.nameTv.setText(movieList.get(position).name);
			viewItemHodler.otherInfo.setText(movieList.get(position).num + getString(R.string.yingpianshu));

			aq = new AQuery(convertView);
			aq.id(R.id.iv_item_layout_haibao).image(movieList.get(position).pic_url, 
					true, true,0, R.drawable.post_normal);
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			if(movieList.size() <= 0 ) {
				
				return null;
			}
			return movieList.get(position);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if(movieList.size() <= 0 ) {
				
				return DEFAULT_ITEM_NUM;
			}
			return movieList.size();
		}
	};

}
