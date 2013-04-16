package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.tv.Service.Return.ReturnTops;
import com.joyplus.tv.entity.ShiPinInfo;
import com.joyplus.tv.entity.ShiPinInfoParcelable;
import com.joyplus.tv.entity.YueDanInfo;
import com.joyplus.tv.entity.YueDanInfo2;
import com.joyplus.tv.ui.MyMovieGridView;

public class ShowYueDanActivity extends Activity implements View.OnKeyListener,
		MyKeyEventKey, BangDanKey, JieMianConstant,View.OnClickListener {
	
	public static final int DIANYING_YUEDAN = 1;
	public static final int DIANSHIJU_YUEDAN = 2;

	private String TAG = "ShowYueDanActivity";
	private AQuery aq;
	private App app;

	private EditText searchEt;
	private MyMovieGridView dinashijuGv;

	private Button zuijinguankanBtn, zhuijushoucangBtn, lixianshipinBtn,
			dianyingyuedanBtn,dianshijuyuedanBtn;
	
	private View firstFloatView ;

	private View beforeView, activeView;

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
		
		if(defalutYuedan == DIANYING_YUEDAN) {
			
			String url = StatisticsUtils.getTopURL(TOP_URL, 1+"", 50 + "", 1+ "");
			Log.i(TAG, "URL--->" + url);
			getServiceData(url);// 进入电影界面时，全部分类电影显示获取焦点，并且显示数据
		} else if(defalutYuedan == DIANSHIJU_YUEDAN){
			
			String url = StatisticsUtils.getTopURL(TOP_URL, 1+"", 50 + "", 2+ "");
			getServiceData(url);// 进入电影界面时，全部分类电影显示获取焦点，并且显示数据
		}
		
		initView();
		initState();

		dinashijuGv.setAdapter(movieAdapter);
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
		lixianshipinBtn = (Button) findViewById(R.id.bt_lixianshipin);
		
		firstFloatView = findViewById(R.id.inclue_movie_show_item);

		addListener();

	}
	
	private void initState() {

		if(defalutYuedan == DIANYING_YUEDAN) {
			
			beforeView = dianyingyuedanBtn;
			activeView = dianyingyuedanBtn;
			dianyingyuedanBtn.setTextColor(getResources().getColor(R.color.text_active));// 全部分类首先设为激活状态
			dianyingyuedanBtn.setBackgroundResource(R.drawable.menubg);// 在换成这张图片时，会刷新组件的padding
		}else {
			
			beforeView = dianshijuyuedanBtn;
			activeView = dianshijuyuedanBtn;
			dianshijuyuedanBtn.setTextColor(getResources().getColor(R.color.text_active));// 全部分类首先设为激活状态
			dianshijuyuedanBtn.setBackgroundResource(R.drawable.menubg);// 在换成这张图片时，会刷新组件的padding
		}

		searchEt.setFocusable(false);// 搜索焦点消失

		zuijinguankanBtn.setPadding(0, 0, WENZI_PADDING_RIGHT, 0);
		zhuijushoucangBtn.setPadding(0, 0, WENZI_PADDING_RIGHT, 0);
		lixianshipinBtn.setPadding(0, 0, WENZI_PADDING_RIGHT, 0);
		dianyingyuedanBtn.setPadding(0, 0, WENZI_PADDING_RIGHT, 0);
		dianshijuyuedanBtn.setPadding(0, 0, WENZI_PADDING_RIGHT, 0);

	}

	private int beforepostion = 0;

	private void addListener() {

		zuijinguankanBtn.setOnKeyListener(this);
		zhuijushoucangBtn.setOnKeyListener(this);
		lixianshipinBtn.setOnKeyListener(this);
		dianyingyuedanBtn.setOnKeyListener(this);
		dianshijuyuedanBtn.setOnKeyListener(this);

		zuijinguankanBtn.setOnClickListener(this);
		zhuijushoucangBtn.setOnClickListener(this);
		lixianshipinBtn.setOnClickListener(this);
		dianyingyuedanBtn.setOnClickListener(this);
		dianshijuyuedanBtn.setOnClickListener(this);
		
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

						turnToGridViewState();
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
				intent.putParcelableArrayListExtra("yuedan_list_type",movieList.get(position).shiPinList);
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

				final float x = view.getX();
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

				// if (!isSmoonthScroll) {// 没有强行拖动时候的动画效果

				if (beforeGvView != null) {

					ImageView iv = (ImageView) beforeGvView
							.findViewById(R.id.item_layout_dianying_reflact);
					iv.setVisibility(View.VISIBLE);
					beforeGvView.setBackgroundColor(getResources().getColor(
							android.R.color.transparent));
					ScaleAnimation outScaleAnimation = StatisticsUtils.getOutScaleAnimation();
					beforeGvView.startAnimation(outScaleAnimation);

				} else {
					
					ScaleAnimation outScaleAnimation = StatisticsUtils.getOutScaleAnimation();
					firstFloatView.startAnimation(outScaleAnimation);
					
					firstFloatView.setVisibility(View.GONE);
				}
				
				ImageView iv2 = (ImageView) view
						.findViewById(R.id.item_layout_dianying_reflact);
				iv2.setVisibility(View.GONE);
				ScaleAnimation inScaleAnimation = StatisticsUtils.getInScaleAnimation();

				view.setPadding(GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING, 
						GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING);
				view.setBackgroundColor(getResources()
						.getColor(R.color.text_active));
				view.startAnimation(inScaleAnimation);
//				 }

				if (y == 0 || y - popHeight == 0) {// 顶部没有渐影

					if (!isSmoonthScroll) {

						beforeFirstAndLastVible[0] = dinashijuGv
								.getFirstVisiblePosition();
						beforeFirstAndLastVible[1] = dinashijuGv
								.getFirstVisiblePosition() + 9;
					} else {

						beforeFirstAndLastVible[0] = dinashijuGv
								.getFirstVisiblePosition() - 5;
						beforeFirstAndLastVible[1] = dinashijuGv
								.getFirstVisiblePosition() + 9 - 5;
					}

				} else {// 顶部有渐影

					if (!isSmoonthScroll) {

						beforeFirstAndLastVible[0] = dinashijuGv
								.getLastVisiblePosition() - 9;
						beforeFirstAndLastVible[1] = dinashijuGv
								.getLastVisiblePosition();
					} else {

						beforeFirstAndLastVible[0] = dinashijuGv
								.getLastVisiblePosition() - 9 + 5;
						beforeFirstAndLastVible[1] = dinashijuGv
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

		dinashijuGv.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub

				if (!hasFocus) {// 如果gridview没有获取焦点，把item中高亮取消

					ScaleAnimation outScaleAnimation = StatisticsUtils.getOutScaleAnimation();
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

					ScaleAnimation inScaleAnimation = StatisticsUtils.getInScaleAnimation();
					if (beforeGvView != null) {

						ImageView iv = (ImageView) beforeGvView
								.findViewById(R.id.item_layout_dianying_reflact);
						iv.setVisibility(View.GONE);
						beforeGvView.setPadding(GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING, 
								GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING);
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
		linearLayout.setPadding(0, 0, WENZI_PADDING_RIGHT, 0);
		tempButton.setTextColor(getResources().getColor(R.color.text_active));
		tempButton.setCompoundDrawablesWithIntrinsicBounds(getResources()
				.getDrawable(R.drawable.side_hot_active), null, null, null);
	}

	private void buttonToActiveState(Button button) {

		button.setBackgroundResource(R.drawable.menubg);
		button.setPadding(0, 0, WENZI_PADDING_RIGHT, 0);
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
			ReturnTops result = mapper.readValue(json.toString(),
					ReturnTops.class);
			// hot_list.clear();
			if(movieList != null && !movieList.isEmpty()) {
				
				movieList.clear();
			}
			for(int i=0; i<result.tops.length; i++){
				YueDanInfo2 yuedanInfo = new YueDanInfo2();
				ArrayList<ShiPinInfoParcelable> tempList = new ArrayList<ShiPinInfoParcelable>();
				yuedanInfo.name = result.tops[i].name;
				yuedanInfo.id = result.tops[i].id;
				yuedanInfo.prod_type = result.tops[i].prod_type;
				yuedanInfo.pic_url = result.tops[i].big_pic_url;
				yuedanInfo.num = result.tops[i].num;
				yuedanInfo.content = result.tops[i].content;
				for (int j = 0; j < result.tops[i].items.length; j++) {
					ShiPinInfoParcelable shipinInfo = new ShiPinInfoParcelable();
					shipinInfo.setArea(result.tops[i].items[j].area);
					shipinInfo.setBig_prod_pic_url(result.tops[i].items[j].big_prod_pic_url);
					shipinInfo.setCur_episode(result.tops[i].items[j].cur_episode);
//					shipinInfo.setCur_item_name(result.tops[i].items[j].cur_i);
					shipinInfo.setDefinition(result.tops[i].items[j].definition);
					shipinInfo.setDirectors(result.tops[i].items[j].directors);
					shipinInfo.setDuration(result.tops[i].items[j].duration);
					shipinInfo.setFavority_num(result.tops[i].items[j].favority_num);
					shipinInfo.setId(result.tops[i].items[j].id);
					shipinInfo.setMax_episode(result.tops[i].items[j].max_episode);
					shipinInfo.setProd_id(result.tops[i].items[j].prod_id);
					shipinInfo.setProd_name(result.tops[i].items[j].prod_name);
					shipinInfo.setProd_pic_url(result.tops[i].items[j].prod_pic_url);
					shipinInfo.setProd_type(result.tops[i].items[j].prod_type);
					shipinInfo.setPublish_date(result.tops[i].items[j].publish_date);
					shipinInfo.setScore(result.tops[i].items[j].score);
					shipinInfo.setStars(result.tops[i].items[j].stars);
					shipinInfo.setSupport_num(result.tops[i].items[j].support_num);
					tempList.add(shipinInfo);
				}
				yuedanInfo.shiPinList = tempList;
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
		
		aq = new AQuery(firstFloatView);
		aq.id(R.id.iv_item_layout_haibao).image(
				movieList.get(0).pic_url);
		movieName.setText(movieList.get(0).name);
		movieScore.setText(movieList.get(0).num + getString(R.string.yingpianshu));
		firstFloatView.setPadding(GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING, 
				GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING);
		firstFloatView.setBackgroundColor(getResources()
				.getColor(R.color.text_active));
		ScaleAnimation inScaleAnimation = StatisticsUtils.getInScaleAnimation();
		
		firstFloatView.startAnimation(inScaleAnimation);
	}


	private BaseAdapter movieAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v;

			int width = parent.getWidth() / 5;
			int height = (int) (width / 1.0f / STANDARD_PIC_WIDTH * STANDARD_PIC_HEIGHT);

			if (convertView == null) {
				View view = getLayoutInflater().inflate(
						R.layout.show_item_layout_yuedan, null);
				v = view;
			} else {

				v = convertView;
			}
			AbsListView.LayoutParams params = new AbsListView.LayoutParams(
					width, height);
			v.setLayoutParams(params);

			TextView movieName = (TextView) v
					.findViewById(R.id.tv_item_layout_name);
			movieName.setText(movieList.get(position).name);
			TextView movieScore = (TextView) v
					.findViewById(R.id.tv_item_layout_other_info);
			movieScore.setText(movieList.get(position).num + getString(R.string.yingpianshu));
			v.setPadding(GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING);

			if (width != 0) {

				popWidth = width;
				popHeight = height;
				// Log.i(TAG, "Width:" + popWidth);
			}

			aq = new AQuery(v);
			aq.id(R.id.iv_item_layout_haibao).image(
					movieList.get(position).pic_url);
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
