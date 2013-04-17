package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;

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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.tv.entity.MovieItemData;
import com.joyplus.tv.entity.ReturnFilterMovieSearch;
import com.joyplus.tv.ui.MyMovieGridView;
import com.joyplus.tv.utils.BangDanKey;
import com.joyplus.tv.utils.JieMianConstant;
import com.joyplus.tv.utils.MyKeyEventKey;

public class ShowSearchActivity extends Activity implements
		View.OnKeyListener, MyKeyEventKey, JieMianConstant,BangDanKey, View.OnClickListener {

	private String TAG = "ShowSearchActivity";
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

	private ArrayList<MovieItemData> movieList = new ArrayList<MovieItemData>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_yuedan_list);

		aq = new AQuery(this);
		app = (App) getApplication();

		initView();
		initState();

		dinashijuGv.setAdapter(movieAdapter);
		dinashijuGv.setSelected(true);
		dinashijuGv.requestFocus();
		dinashijuGv.setSelection(0);
	}

	private void initView() {

		searchEt = (EditText) findViewById(R.id.et_search);
		dinashijuGv = (MyMovieGridView) findViewById(R.id.gv_movie_show);

		firstFloatView = findViewById(R.id.inclue_movie_show_item);

		addListener();

	}

	private void initState() {

//		searchEt.setFocusable(false);// 搜索焦点消失

	}

	private int beforepostion = 0;

	private void addListener() {
		
		searchEt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Editable editable = searchEt.getText();
				String searchStr = editable.toString();
				
				if(searchStr != null && !searchStr.equals("")) {
					
					String url = StatisticsUtils.getSearchURL(SEARCH_URL, 1 + "", 30 + "",searchStr);
					getServiceData(url);
//					searchEt.
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
						String pro_type = movieList.get(position).getMovieProType();
						Log.i(TAG, "pro_type:" + pro_type);
						if(pro_type != null && !pro_type.equals("")) {
							
							if(pro_type.equals("2")) {
								Log.i(TAG, "pro_type:" + pro_type + "   --->2");
								Intent intent = new Intent(ShowSearchActivity.this,
										ShowXiangqingTv.class);
								intent.putExtra("ID", movieList.get(position).getMovieID());
								startActivity(intent);
//								startActivity();
							} else if(pro_type.equals("1")) {
								Log.i(TAG, "pro_type:" + pro_type + "   --->1");
								Intent intent = new Intent(ShowSearchActivity.this,
										ShowXiangqingMovie.class);
								intent.putExtra("ID", movieList.get(position).getMovieID());
								startActivity(intent);
//								startActivity();
							} else if(pro_type.equals("131")) {
								
								Intent intent = new Intent(ShowSearchActivity.this,
										ShowXiangqingDongman.class);
								intent.putExtra("ID", movieList.get(position).getMovieID());
								startActivity(intent);
							} else if(pro_type.equals("3")) {
								
								Intent intent = new Intent(ShowSearchActivity.this,
										ShowXiangqingZongYi.class);
								intent.putExtra("ID", movieList.get(position).getMovieID());
								startActivity(intent);
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

						final float x = view.getX();
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

						// if (!isSmoonthScroll) {// 没有强行拖动时候的动画效果

						if (beforeGvView != null) {

							ImageView iv = (ImageView) beforeGvView
									.findViewById(R.id.item_layout_dianying_reflact);
							iv.setVisibility(View.VISIBLE);
							beforeGvView.setBackgroundColor(getResources()
									.getColor(android.R.color.transparent));
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
						view.setBackgroundColor(getResources().getColor(
								R.color.text_active));
						view.startAnimation(inScaleAnimation);
						// }

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
	
	
	private void getServiceData(String url) {
		
		firstFloatView.setVisibility(View.INVISIBLE);
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, "initData");

		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}
	private ObjectMapper mapper = new ObjectMapper();
	
	public void initData(String url, JSONObject json, AjaxStatus status) {

		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {

			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		try {
			Log.d(TAG, json.toString());
			ReturnFilterMovieSearch result = mapper.readValue(json.toString(),
					ReturnFilterMovieSearch.class);
			// hot_list.clear();
			if(movieList != null && !movieList.isEmpty()) {
				
				movieList.clear();
			}
			for (int i = 0; i < result.results.length; i++) {

				MovieItemData movieItemData = new MovieItemData();
				movieItemData.setMovieName(result.results[i].prod_name);
				String bigPicUrl = result.results[i].big_prod_pic_url;
				if(bigPicUrl == null || bigPicUrl.equals("")) {
					
					bigPicUrl = result.results[i].prod_pic_url;
				}
				movieItemData.setMoviePicUrl(bigPicUrl);
//				movieItemData.setMoviePicUrl(result.results[i].big_prod_pic_url);
				movieItemData.setMovieScore(result.results[i].score);
				movieItemData.setMovieID(result.results[i].prod_id);
				movieItemData.setMovieDuration(result.results[i].duration);
				movieItemData.setMovieProType(result.results[i].prod_type);
				movieList.add(movieItemData);
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

	private void initFirstFloatView() {

		firstFloatView.setX(0);
		firstFloatView.setY(0);
		firstFloatView.setLayoutParams(new FrameLayout.LayoutParams(popWidth,
				popHeight));
		firstFloatView.setVisibility(View.VISIBLE);

		TextView movieName = (TextView) firstFloatView.findViewById(R.id.tv_item_layout_name);
		TextView movieScore = (TextView) firstFloatView.findViewById(R.id.tv_item_layout_score);
		aq = new AQuery(firstFloatView);
//		aq.id(R.id.iv_item_layout_haibao).image(
//				movieList.get(0).getMoviePicUrl());
		aq.id(R.id.iv_item_layout_haibao).image(movieList.get(0).getMoviePicUrl(), 
				true, true,0, R.drawable.post_active);
		movieName.setText(movieList.get(0).getMovieName());
		
		String proType = movieList.get(0).getMovieProType();
		
		if(proType != null && !proType.equals("")) {
			
			if(proType.equals("1")) {
				
				movieScore.setText(movieList.get(0).getMovieScore());
				String duration = movieList.get(0).getMovieDuration();
				if(duration != null && !duration.equals("")) {
					
					TextView movieDuration = (TextView) firstFloatView
							.findViewById(R.id.tv_item_layout_other_info);
					movieDuration.setText(duration);
				}
			} else if(proType.equals("2") || proType.equals("131")){
				movieScore.setText(movieList.get(0).getMovieScore());
				String curEpisode = movieList.get(0).getMovieCurEpisode();
				String maxEpisode = movieList.get(0).getMovieMaxEpisode();
				
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
				
				String curEpisode = movieList.get(0).getMovieCurEpisode();
				if(curEpisode != null && !curEpisode.equals("")) {
					
					TextView movieUpdate = (TextView) firstFloatView
							.findViewById(R.id.tv_item_layout_other_info);
					movieUpdate.setText(getString(R.string.zongyi_gengxinzhi) + 
							movieList.get(0).getMovieCurEpisode());
				}
			}
		}
		
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
				
				AbsListView.LayoutParams params = new AbsListView.LayoutParams(
						width, height);
				convertView.setLayoutParams(params);
				convertView.setPadding(GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING,
						GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING);
			} else {

				viewItemHodler = (GridViewItemHodler) convertView.getTag();
			}
			
			viewItemHodler.nameTv.setText(movieList.get(0).getMovieName());
			
			String proType = movieList.get(0).getMovieProType();
			
			if(proType != null && !proType.equals("")) {
				
				if(proType.equals("1")) {
					
					viewItemHodler.scoreTv.setText(movieList.get(0).getMovieScore());
					String duration = movieList.get(0).getMovieDuration();
					if(duration != null && !duration.equals("")) {
						
						viewItemHodler.otherInfo.setText(duration);
					}
				} else if(proType.equals("2") || proType.equals("131")){
					
					viewItemHodler.scoreTv.setText(movieList.get(0).getMovieScore());
					String curEpisode = movieList.get(0).getMovieCurEpisode();
					String maxEpisode = movieList.get(0).getMovieMaxEpisode();
					
					if(maxEpisode != null && !maxEpisode.equals("")) {
						
						if(curEpisode == null || curEpisode.equals("0") || 
								curEpisode.compareTo(maxEpisode) >= 0) {
							
							viewItemHodler.otherInfo.setText(
									maxEpisode + getString(R.string.dianshiju_jiquan));
							} else if(maxEpisode.compareTo(curEpisode) > 0) {

								viewItemHodler.otherInfo.setText(getString(R.string.zongyi_gengxinzhi) + 
										curEpisode);
						}
					}

				} else if(proType.equals("3")) {
					
					String curEpisode = movieList.get(0).getMovieCurEpisode();
					if(curEpisode != null && !curEpisode.equals("")) {
						
						viewItemHodler.otherInfo.setText(getString(R.string.zongyi_gengxinzhi) + 
								movieList.get(0).getMovieCurEpisode());
					}
				}
			}

			if (width != 0) {

				popWidth = width;
				popHeight = height;
				// Log.i(TAG, "Width:" + popWidth);
			}

			aq = new AQuery(convertView);
//			aq.id(R.id.iv_item_layout_haibao).image(
//					movieList.get(0).getMoviePicUrl());
			aq.id(R.id.iv_item_layout_haibao).image(movieList.get(position).getMoviePicUrl(), 
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
			return movieList.get(position);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return movieList.size();
		}
	};
	
	 private class GridViewItemHodler {
			
		TextView nameTv;
		TextView scoreTv;
		TextView otherInfo;
	}

}
