package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.tv.Adapters.CurrentPlayData;
import com.joyplus.tv.Service.Return.ReturnUserPlayHistories;
import com.joyplus.tv.Video.VideoPlayerActivity;
import com.joyplus.tv.entity.HotItemInfo;
import com.joyplus.tv.ui.NavigateView;
import com.joyplus.tv.ui.NavigateView.OnResultListener;

public class HistoryActivity extends Activity implements OnClickListener, OnItemSelectedListener {
	private static final String TAG = "HistoryActivity";
	private Button btn_fenlei_all;
	private Button btn_fenlei_movie;
	private Button btn_fenlei_tv;
	private Button btn_fenlei_dongman;
	private Button btn_fenlei_zongyi;
	private Button selectedButton;
	private Button delBtn;
	private int index = 0;
	ObjectMapper mapper = new ObjectMapper();
	private List<HotItemInfo> allHistoryList = new ArrayList<HotItemInfo>();
	private List<HotItemInfo> movieHistoryList = new ArrayList<HotItemInfo>();
	private List<HotItemInfo> tvHistoryList = new ArrayList<HotItemInfo>();
	private List<HotItemInfo> dongmanHistoryList = new ArrayList<HotItemInfo>();
	private List<HotItemInfo> zongyiHistoryList = new ArrayList<HotItemInfo>();
	
	private View  selectedView;
	private ListView listView;
	private App app;
	private AQuery aq;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		app = (App) getApplication();
		aq = new AQuery(this);
		listView = (ListView) findViewById(R.id.history_list);
		btn_fenlei_all = (Button) findViewById(R.id.fenlei_all);
		btn_fenlei_movie = (Button) findViewById(R.id.fenlei_movie);
		btn_fenlei_tv = (Button) findViewById(R.id.fenlei_tv);
		btn_fenlei_dongman = (Button) findViewById(R.id.fenlei_dongman);
		btn_fenlei_zongyi = (Button) findViewById(R.id.fenlei_zongyi);
		delBtn = (Button) findViewById(R.id.delete_btn);
		btn_fenlei_all.setOnClickListener(this);
		btn_fenlei_movie.setOnClickListener(this);
		btn_fenlei_tv.setOnClickListener(this);
		btn_fenlei_dongman.setOnClickListener(this);
		btn_fenlei_zongyi.setOnClickListener(this);
		delBtn.setOnClickListener(this);
		btn_fenlei_all.setPadding(0, 0, 5, 0);
		btn_fenlei_movie.setPadding(0, 0, 5, 0);
		btn_fenlei_tv.setPadding(0, 0, 5, 0);
		btn_fenlei_dongman.setPadding(0, 0, 5, 0);
		btn_fenlei_zongyi.setPadding(0, 0, 5, 0);
		btn_fenlei_all.setTextColor(getResources().getColor(R.color.common_title_selected));
		btn_fenlei_all.setBackgroundResource(R.drawable.menubg);
		selectedButton = btn_fenlei_all;
		selectedButton.setPadding(0, 0, 5, 0);
//		listView.setAdapter(new MovieAdapter());
		listView.setOnItemSelectedListener(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if(arg2>=((HistortyAdapter)listView.getAdapter()).data.size()){
					setResult(Activity.RESULT_OK);
					finish();
					return;
				}
				Toast.makeText(HistoryActivity.this, "seleced index = " + arg2, 100).show();
				final Dialog dialog = new AlertDialog.Builder(HistoryActivity.this).create();
				dialog.show();
				LayoutInflater inflater = LayoutInflater.from(HistoryActivity.this);
				View view = inflater.inflate(R.layout.layout_history_dialog, null);
				TextView nameText = (TextView) view.findViewById(R.id.dialog_title);
				Button playButton = (Button) view.findViewById(R.id.history_play);
				playButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						CurrentPlayData playDate = new CurrentPlayData();
						Intent intent = new Intent(HistoryActivity.this,VideoPlayerActivity.class);
						playDate.prod_id = ((HistortyAdapter)listView.getAdapter()).data.get(arg2).prod_id;
						playDate.prod_type = Integer.valueOf(((HistortyAdapter)listView.getAdapter()).data.get(arg2).prod_type);
						playDate.prod_name = ((HistortyAdapter)listView.getAdapter()).data.get(arg2).prod_name;
						playDate.prod_url = ((HistortyAdapter)listView.getAdapter()).data.get(arg2).video_url;
//						playDate.prod_src = "";
						if(!"".equals(((HistortyAdapter)listView.getAdapter()).data.get(arg2).playback_time)){
							playDate.prod_time = Long.valueOf(((HistortyAdapter)listView.getAdapter()).data.get(arg2).playback_time);
						}
//						playDate.prod_qua = Integer.valueOf(info.definition);
						app.setCurrentPlayData(playDate);
						startActivity(intent);
					}
				});
				Button viewDetailButton = (Button) view.findViewById(R.id.history_view);
				viewDetailButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						int  prod_type = Integer.valueOf(((HistortyAdapter)listView.getAdapter()).data.get(arg2).prod_type);
						Intent intent = null;
						switch (prod_type) {
						case 1:
							intent = new Intent(HistoryActivity.this,ShowXiangqingMovie.class);
							break;
						case 2:
							intent = new Intent(HistoryActivity.this,ShowXiangqingTv.class);
							break;
						case 3:
							intent = new Intent(HistoryActivity.this,ShowXiangqingZongYi.class);
							break;
						case 131:
							intent = new Intent(HistoryActivity.this,ShowXiangqingDongman.class);
							break;
						}
						if(intent == null){ 
							return; 
						}else{
							intent.putExtra("ID", ((HistortyAdapter)listView.getAdapter()).data.get(arg2).prod_id);
							startActivity(intent);
						}
					}
				});
				Button delButton = (Button) view.findViewById(R.id.history_del);
				delButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						deleteHistory(true, ((HistortyAdapter)listView.getAdapter()).data.get(arg2).id);
						((HistortyAdapter)listView.getAdapter()).data.remove(arg2);
						((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
						dialog.dismiss();
					}
				});
				nameText.setText(((HistortyAdapter)listView.getAdapter()).data.get(arg2).prod_name);
				dialog.setContentView(view);
			}
//				switch (index) {
//				case 0://all
//					if(arg2<allHistoryList.size()){
//						final Dialog dialog = new AlertDialog.Builder(HistoryActivity.this).create();
//						dialog.show();
//						LayoutInflater inflater = LayoutInflater.from(HistoryActivity.this);
//						View view = inflater.inflate(R.layout.layout_history_dialog, null);
//						TextView nameText = (TextView) view.findViewById(R.id.dialog_title);
//						Button playButton = (Button) view.findViewById(R.id.history_play);
//						playButton.setOnClickListener(new OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								dialog.dismiss();
//								CurrentPlayData playDate = new CurrentPlayData();
//								Intent intent = new Intent(HistoryActivity.this,VideoPlayerActivity.class);
//								playDate.prod_id = allHistoryList.get(arg2).prod_id;
//								playDate.prod_type = Integer.valueOf(allHistoryList.get(arg2).prod_type);
//								playDate.prod_name = allHistoryList.get(arg2).prod_name;
//								playDate.prod_url = allHistoryList.get(arg2).video_url;
////								playDate.prod_src = "";
//								if(!"".equals(allHistoryList.get(arg2).playback_time)){
//									playDate.prod_time = Long.valueOf(allHistoryList.get(arg2).playback_time);
//								}
////								playDate.prod_qua = Integer.valueOf(info.definition);
//								app.setCurrentPlayData(playDate);
//								startActivity(intent);
//							}
//						});
//						Button viewDetailButton = (Button) view.findViewById(R.id.history_view);
//						viewDetailButton.setOnClickListener(new OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								dialog.dismiss();
//								int  prod_type = Integer.valueOf(allHistoryList.get(arg2).prod_type);
//								Intent intent = null;
//								switch (prod_type) {
//								case 1:
//									intent = new Intent(HistoryActivity.this,ShowXiangqingMovie.class);
//									break;
//								case 2:
//									intent = new Intent(HistoryActivity.this,ShowXiangqingTv.class);
//									break;
//								case 3:
//									intent = new Intent(HistoryActivity.this,ShowXiangqingZongYi.class);
//									break;
//								case 131:
//									intent = new Intent(HistoryActivity.this,ShowXiangqingDongman.class);
//									break;
//								}
//								if(intent == null){ 
//									return; 
//								}else{
//									intent.putExtra("ID", allHistoryList.get(arg2).prod_id);
//									startActivity(intent);
//								}
//							}
//						});
//						Button delButton = (Button) view.findViewById(R.id.history_del);
//						delButton.setOnClickListener(new OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								deleteHistory(true, allHistoryList.get(arg2).id);
//								allHistoryList.remove(arg2);
//								((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
//								dialog.dismiss();
//							}
//						});
//						nameText.setText(allHistoryList.get(arg2).prod_name);
//						dialog.setContentView(view);
//					}
//					break;
//				case 1://movie
//					if(arg2<movieHistoryList.size()){
//						final Dialog dialog = new AlertDialog.Builder(HistoryActivity.this).create();
//						dialog.show();
//						LayoutInflater inflater = LayoutInflater.from(HistoryActivity.this);
//						View view = inflater.inflate(R.layout.layout_history_dialog, null);
//						TextView nameText = (TextView) view.findViewById(R.id.dialog_title);
//						Button playButton = (Button) view.findViewById(R.id.history_play);
//						playButton.setOnClickListener(new OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								dialog.dismiss();
//								CurrentPlayData playDate = new CurrentPlayData();
//								Intent intent = new Intent(HistoryActivity.this,VideoPlayerActivity.class);
//								playDate.prod_id = movieHistoryList.get(arg2).prod_id;
//								playDate.prod_type = Integer.valueOf(movieHistoryList.get(arg2).prod_type);
//								playDate.prod_name = movieHistoryList.get(arg2).prod_name;
//								playDate.prod_url = movieHistoryList.get(arg2).video_url;
////									playDate.prod_src = "";
//								if(!"".equals(movieHistoryList.get(arg2).playback_time)){
//									playDate.prod_time = Long.valueOf(movieHistoryList.get(arg2).playback_time);
//								}
////									playDate.prod_qua = Integer.valueOf(info.definition);
//								app.setCurrentPlayData(playDate);
//								startActivity(intent);
//							}
//						});
//						Button viewDetailButton = (Button) view.findViewById(R.id.history_view);
//						viewDetailButton.setOnClickListener(new OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								dialog.dismiss();
//								Intent intent = new Intent(HistoryActivity.this,ShowXiangqingMovie.class);
//								intent.putExtra("ID", movieHistoryList.get(arg2).prod_id);
//								startActivity(intent);
//							}
//						});
//						Button delButton = (Button) view.findViewById(R.id.history_del);
//						delButton.setOnClickListener(new OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								deleteHistory(true, movieHistoryList.get(arg2).id);
//								movieHistoryList.remove(arg2);
//								((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
//								dialog.dismiss();
//							}
//						});
//						nameText.setText(movieHistoryList.get(arg2).prod_name);
//						dialog.setContentView(view);
//					}
//					break;
//				case 2://tv
//					if(arg2<tvHistoryList.size()){
//						final Dialog dialog = new AlertDialog.Builder(HistoryActivity.this).create();
//						dialog.show();
//						LayoutInflater inflater = LayoutInflater.from(HistoryActivity.this);
//						View view = inflater.inflate(R.layout.layout_history_dialog, null);
//						TextView nameText = (TextView) view.findViewById(R.id.dialog_title);
//						Button playButton = (Button) view.findViewById(R.id.history_play);
//						playButton.setOnClickListener(new OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								dialog.dismiss();
//								CurrentPlayData playDate = new CurrentPlayData();
//								Intent intent = new Intent(HistoryActivity.this,VideoPlayerActivity.class);
//								playDate.prod_id = tvHistoryList.get(arg2).prod_id;
//								playDate.prod_type = Integer.valueOf(tvHistoryList.get(arg2).prod_type);
//								playDate.prod_name = tvHistoryList.get(arg2).prod_name;
//								playDate.prod_url = tvHistoryList.get(arg2).video_url;
////									playDate.prod_src = "";
//								if(!"".equals(tvHistoryList.get(arg2).playback_time)){
//									playDate.prod_time = Long.valueOf(tvHistoryList.get(arg2).playback_time);
//								}
////									playDate.prod_qua = Integer.valueOf(info.definition);
//								app.setCurrentPlayData(playDate);
//								startActivity(intent);
//							}
//						});
//						Button viewDetailButton = (Button) view.findViewById(R.id.history_view);
//						viewDetailButton.setOnClickListener(new OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								dialog.dismiss();
//								Intent intent = new Intent(HistoryActivity.this,ShowXiangqingTv.class);
//								intent.putExtra("ID", tvHistoryList.get(arg2).prod_id);
//								startActivity(intent);
//							}
//						});
//						Button delButton = (Button) view.findViewById(R.id.history_del);
//						delButton.setOnClickListener(new OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								deleteHistory(true, tvHistoryList.get(arg2).id);
//								tvHistoryList.remove(arg2);
//								((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
//								dialog.dismiss();
//							}
//						});
//						nameText.setText(tvHistoryList.get(arg2).prod_name);
//						dialog.setContentView(view);
//					}
//					break;
//				case 131://dongman
//					if(arg2<dongmanHistoryList.size()){
//						final Dialog dialog = new AlertDialog.Builder(HistoryActivity.this).create();
//						dialog.show();
//						LayoutInflater inflater = LayoutInflater.from(HistoryActivity.this);
//						View view = inflater.inflate(R.layout.layout_history_dialog, null);
//						TextView nameText = (TextView) view.findViewById(R.id.dialog_title);
//						Button playButton = (Button) view.findViewById(R.id.history_play);
//						playButton.setOnClickListener(new OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								dialog.dismiss();
//								CurrentPlayData playDate = new CurrentPlayData();
//								Intent intent = new Intent(HistoryActivity.this,VideoPlayerActivity.class);
//								playDate.prod_id = dongmanHistoryList.get(arg2).prod_id;
//								playDate.prod_type = Integer.valueOf(dongmanHistoryList.get(arg2).prod_type);
//								playDate.prod_name = dongmanHistoryList.get(arg2).prod_name;
//								playDate.prod_url = dongmanHistoryList.get(arg2).video_url;
////										playDate.prod_src = "";
//								if(!"".equals(dongmanHistoryList.get(arg2).playback_time)){
//									playDate.prod_time = Long.valueOf(dongmanHistoryList.get(arg2).playback_time);
//								}
////										playDate.prod_qua = Integer.valueOf(info.definition);
//								app.setCurrentPlayData(playDate);
//								startActivity(intent);
//							}
//						});
//						Button viewDetailButton = (Button) view.findViewById(R.id.history_view);
//						viewDetailButton.setOnClickListener(new OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								dialog.dismiss();
//								Intent intent = new Intent(HistoryActivity.this,ShowXiangqingDongman.class);
//								intent.putExtra("ID", dongmanHistoryList.get(arg2).prod_id);
//								startActivity(intent);
//							}
//						});
//						Button delButton = (Button) view.findViewById(R.id.history_del);
//						delButton.setOnClickListener(new OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								deleteHistory(true, dongmanHistoryList.get(arg2).id);
//								dongmanHistoryList.remove(arg2);
//								((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
//								dialog.dismiss();
//							}
//						});
//						nameText.setText(dongmanHistoryList.get(arg2).prod_name);
//						dialog.setContentView(view);
//					}
//					break;
//				case 4://zhongyi
//					if(arg2<zongyiHistoryList.size()){
//						final Dialog dialog = new AlertDialog.Builder(HistoryActivity.this).create();
//						dialog.show();
//						LayoutInflater inflater = LayoutInflater.from(HistoryActivity.this);
//						View view = inflater.inflate(R.layout.layout_history_dialog, null);
//						TextView nameText = (TextView) view.findViewById(R.id.dialog_title);
//						Button playButton = (Button) view.findViewById(R.id.history_play);
//						playButton.setOnClickListener(new OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								dialog.dismiss();
//								CurrentPlayData playDate = new CurrentPlayData();
//								Intent intent = new Intent(HistoryActivity.this,VideoPlayerActivity.class);
//								playDate.prod_id = zongyiHistoryList.get(arg2).prod_id;
//								playDate.prod_type = Integer.valueOf(zongyiHistoryList.get(arg2).prod_type);
//								playDate.prod_name = zongyiHistoryList.get(arg2).prod_name;
//								playDate.prod_url = zongyiHistoryList.get(arg2).video_url;
////											playDate.prod_src = "";
//								if(!"".equals(zongyiHistoryList.get(arg2).playback_time)){
//									playDate.prod_time = Long.valueOf(zongyiHistoryList.get(arg2).playback_time);
//								}
////											playDate.prod_qua = Integer.valueOf(info.definition);
//								app.setCurrentPlayData(playDate);
//								startActivity(intent);
//							}
//						});
//						Button viewDetailButton = (Button) view.findViewById(R.id.history_view);
//						viewDetailButton.setOnClickListener(new OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								dialog.dismiss();
//								Intent intent = new Intent(HistoryActivity.this,ShowXiangqingZongYi.class);
//								intent.putExtra("ID", zongyiHistoryList.get(arg2).prod_id);
//								startActivity(intent);
//							}
//						});
//						Button delButton = (Button) view.findViewById(R.id.history_del);
//						delButton.setOnClickListener(new OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								deleteHistory(true, zongyiHistoryList.get(arg2).id);
//								zongyiHistoryList.remove(arg2);
//								((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
//								dialog.dismiss();
//							}
//						});
//						nameText.setText(zongyiHistoryList.get(arg2).prod_name);
//						dialog.setContentView(view);
//					}
//					break;
//				}
//			}
		});
		listView.setSelected(true);
		listView.setSelection(0);
		listView.requestFocus();
		listView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(!hasFocus){
					if(selectedView!=null){
						selectedView.setBackgroundResource(R.drawable.bg_teat_repeat);
					}
				}else{
					if(selectedView!=null){
						selectedView.setBackgroundResource(R.drawable.historty_listitem_drawable_selector);
					}
				}
			}
		});
		
		getHistoryData(0);
	}
	
	class HistortyAdapter extends BaseAdapter{
		private List<HotItemInfo> data;
		
		public HistortyAdapter(List<HotItemInfo> data){
			this.data = data;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if(data.size()==0){
				return 1;
			}else{
				return data.size();
			}
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if(convertView ==null){
				convertView = LayoutInflater.from(HistoryActivity.this).inflate(R.layout.item_history_list,null);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.img = (ImageView) convertView.findViewById(R.id.image);
				holder.directors = (TextView) convertView.findViewById(R.id.directors);
				holder.stars = (TextView) convertView.findViewById(R.id.stars);
				holder.directors_notice = (TextView) convertView.findViewById(R.id.directors_notice);
				holder.stars_notice = (TextView) convertView.findViewById(R.id.stars_notice);
				holder.content = (TextView) convertView.findViewById(R.id.content);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			if(data.size()>0){
				holder.title.setText(data.get(position).prod_name);
				holder.directors.setText(data.get(position).directors);
				holder.stars.setText(data.get(position).stars);
				int prod_type = Integer.valueOf(data.get(position).prod_type);
				String playBack_time = StatisticsUtils.formatDuration1(Integer.valueOf(data.get(position).playback_time));
				switch (prod_type) {
				case 1:
					holder.content.setText("上次观看到：" + playBack_time);
					break;
				case 2:
					holder.content.setText("上次观看到：第" + data.get(position).cur_episode+"集"+playBack_time);
					break;
				case 3:
					holder.content.setText("上次观看到：第" + data.get(position).cur_episode+"期"+playBack_time);
					break;
				case 131:
					holder.content.setText("上次观看到：第" + data.get(position).cur_episode+"集"+playBack_time);
					break;
				}
				aq.id(holder.img).image(data.get(position).prod_pic_url);
			}else{
				holder.img.setImageResource(R.drawable.post_normal);
				holder.title.setText("您还未观看过任何影片。去热播看看最近流行什么吧^_^~");
				holder.stars.setVisibility(View.GONE);
				holder.directors.setVisibility(View.GONE);
				holder.content.setVisibility(View.GONE);
				holder.stars_notice.setVisibility(View.GONE);
				holder.directors_notice.setVisibility(View.GONE);
			}
			
			convertView.setBackgroundResource(R.drawable.historty_listitem_drawable_selector);
			return convertView;
		}
		
	}
	
//	private String formatDuration(int duration) {
////		duration = duration;
//		int h = duration / 3600;
//		int m = (duration - h * 3600) / 60;
//		int s = duration - (h * 3600 + m * 60);
//		String durationValue;
//		if (h == 0) {
//			durationValue = String.format("%1$02d:%2$02d", m, s);
//		} else {
//			durationValue = String.format("%1$d:%2$02d:%3$02d", h, m, s);
//		}
//		return durationValue;
//	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(selectedButton.equals(v)){
			return ;
		}
		
		switch (v.getId()) { 
		case R.id.fenlei_all:
			selectedButton.setTextColor(getResources().getColorStateList(R.color.text_color_selector));
			selectedButton.setBackgroundResource(R.drawable.text_drawable_selector);
			btn_fenlei_all.setTextColor(getResources().getColor(R.color.common_title_selected));
			btn_fenlei_all.setBackgroundResource(R.drawable.menubg);
			selectedButton = btn_fenlei_all;
			selectedButton.setPadding(0, 0, 5, 0);
			index = 0;
			getHistoryData(0);
			break;
		case R.id.fenlei_movie:
			selectedButton.setTextColor(getResources().getColorStateList(R.color.text_color_selector));
			selectedButton.setBackgroundResource(R.drawable.text_drawable_selector);
			btn_fenlei_movie.setTextColor(getResources().getColor(R.color.common_title_selected));
			btn_fenlei_movie.setBackgroundResource(R.drawable.menubg);
			selectedButton = btn_fenlei_movie;
			selectedButton.setPadding(0, 0, 5, 0);
			index = 1;
			getHistoryData(1);
			break;
		case R.id.fenlei_tv:
			selectedButton.setTextColor(getResources().getColorStateList(R.color.text_color_selector));
			selectedButton.setBackgroundResource(R.drawable.text_drawable_selector);
			btn_fenlei_tv.setTextColor(getResources().getColor(R.color.common_title_selected));
			btn_fenlei_tv.setBackgroundResource(R.drawable.menubg);
			selectedButton = btn_fenlei_tv;
			selectedButton.setPadding(0, 0, 5, 0);
			index = 2;
			getHistoryData(2);
			break;
		case R.id.fenlei_dongman:
			selectedButton.setTextColor(getResources().getColorStateList(R.color.text_color_selector));
			selectedButton.setBackgroundResource(R.drawable.text_drawable_selector);
			btn_fenlei_dongman.setTextColor(getResources().getColor(R.color.common_title_selected));
			btn_fenlei_dongman.setBackgroundResource(R.drawable.menubg);
			selectedButton = btn_fenlei_dongman;
			selectedButton.setPadding(0, 0, 5, 0);
			index = 131;
			getHistoryData(131);
			break;
		case R.id.fenlei_zongyi:
			selectedButton.setTextColor(getResources().getColorStateList(R.color.text_color_selector));
			selectedButton.setBackgroundResource(R.drawable.text_drawable_selector);
			btn_fenlei_zongyi.setTextColor(getResources().getColor(R.color.common_title_selected));
			btn_fenlei_zongyi.setBackgroundResource(R.drawable.menubg);
			selectedButton = btn_fenlei_zongyi;
			selectedButton.setPadding(0, 0, 5, 0);
			index = 3;
			getHistoryData(3);
			break;
		case R.id.delete_btn:
			deleteHistory(false, "");
			((HistortyAdapter)listView.getAdapter()).data.clear();
			((HistortyAdapter)listView.getAdapter()).notifyDataSetChanged();
			break;
		default:
			break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		selectedView = arg1;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	private void getHistoryData(int type){
		String url = Constant.BASE_URL + "user/playHistories" +"?page_num=1&page_size=10&userid=" + app.getUserInfo().getUserId();
		if(type!=0){
			url = url + "&vod_type=" + type;
		}
//		String url = Constant.BASE_URL + "user/playHistories" +"?page_num=1&page_size=10&userid=4742";

//		String url = Constant.BASE_URL;
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		switch (type) {
		case 0:
			cb.url(url).type(JSONObject.class).weakHandler(this, "initAllHistoryData");
			break;
		case 1:
			cb.url(url).type(JSONObject.class).weakHandler(this, "initMovieHistoryData");
			break;
		case 2:
			cb.url(url).type(JSONObject.class).weakHandler(this, "initTvHistoryData");
			break;
		case 3:
			cb.url(url).type(JSONObject.class).weakHandler(this, "initZhongyiHistoryData");
			break;
		case 131:
			cb.url(url).type(JSONObject.class).weakHandler(this, "initDongmanHistoryData");
			break;
		default:
			break;
		}
		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}
	
	
	public void initAllHistoryData(String url, JSONObject json, AjaxStatus status){
		initHistoryData(url, json, status, 0);
	}
	public void initMovieHistoryData(String url, JSONObject json, AjaxStatus status){
		initHistoryData(url, json, status, 1);
	}
	public void initTvHistoryData(String url, JSONObject json, AjaxStatus status){
		initHistoryData(url, json, status, 2);
	}
	public void initZhongyiHistoryData(String url, JSONObject json, AjaxStatus status){
		initHistoryData(url, json, status, 3);
	}
	public void initDongmanHistoryData(String url, JSONObject json, AjaxStatus status){
		initHistoryData(url, json, status, 131);
	}
	
	
	public void initHistoryData(String url, JSONObject json, AjaxStatus status,int type){
		if (status.getCode() == AjaxStatus.NETWORK_ERROR)  {
//			aq.id(R.id.ProgressText).invisible();
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		Log.d(TAG, "history data = " + json.toString());
		try {
			ReturnUserPlayHistories result  = mapper.readValue(json.toString(), ReturnUserPlayHistories.class);
			List<HotItemInfo> list = new ArrayList<HotItemInfo>();
			for(int i=0; i<result.histories.length; i++){
				HotItemInfo item  =  new HotItemInfo();
				item.id = result.histories[i].id;
				item.prod_id = result.histories[i].prod_id;
				item.prod_name = result.histories[i].prod_name;
				item.prod_type = result.histories[i].prod_type;
				item.prod_pic_url = result.histories[i].prod_pic_url;
				item.stars = result.histories[i].stars;
				item.directors = result.histories[i].directors;
				item.favority_num = result.histories[i].favority_num;
				item.support_num = result.histories[i].support_num;
				item.publish_date = result.histories[i].publish_date;
				item.score = result.histories[i].score;
				item.area = result.histories[i].area;
				item.cur_episode = result.histories[i].cur_episode;
				item.max_episode = result.histories[i].max_episode;
				item.definition = result.histories[i].definition;
				item.prod_summary = result.histories[i].prod_summary;
				item.duration = result.histories[i].duration;
				item.video_url = result.histories[i].video_url;
				item.playback_time = result.histories[i].playback_time;
				item.prod_subname = result.histories[i].prod_subname;
				item.play_type = result.histories[i].play_type;
				list.add(item);
			}
			switch (type) {
			case 0:
				allHistoryList = list;
				listView.setAdapter(new HistortyAdapter(allHistoryList));
				break;
			case 1:
				movieHistoryList = list;
				listView.setAdapter(new HistortyAdapter(movieHistoryList));
				break;
			case 2:
				tvHistoryList = list;
				listView.setAdapter(new HistortyAdapter(tvHistoryList));
				break;
			case 3:
				zongyiHistoryList = list;
				listView.setAdapter(new HistortyAdapter(zongyiHistoryList));  
				break;
			case 131:
				dongmanHistoryList = list;
				listView.setAdapter(new HistortyAdapter(dongmanHistoryList));
				break;
			}
			listView.setSelection(0);
			listView.requestFocus();
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			e.printStackTrace();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void deleteHistory(boolean isSingle,String id){
		String url = "";
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.SetHeader(app.getHeaders());
		if(isSingle){
			url = Constant.BASE_URL + "user/clearPlayHistory" ;
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("history_id", id);
			cb.params(params).url(url).type(JSONObject.class)
			.weakHandler(this, "deleteResult");
		}else{
			if(index == 0){
				url = Constant.BASE_URL + "user/clearPlayHistories";
				Map<String, Object> params = new HashMap<String, Object>();
//				params.put("vod_type", index);
				cb.params(params).url(url).type(JSONObject.class)
				.weakHandler(this, "deleteResult");
			}else{
				url = Constant.BASE_URL + "user/clearPlayHistories";
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("vod_type", index);
				cb.params(params).url(url).type(JSONObject.class)
				.weakHandler(this, "deleteResult");
			}
			
		}
		aq.ajax(cb);
//		
//		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
//		Log.d("del", url);
//		cb.url(url).type(JSONObject.class).weakHandler(this, "deleteResult");
//		cb.SetHeader(app.getHeaders());
//		aq.ajax(cb);
	}
	
	public void deleteResult(String url, JSONObject json, AjaxStatus status){
		if (status.getCode() == AjaxStatus.NETWORK_ERROR)  {
//			aq.id(R.id.ProgressText).invisible();
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		Log.d(TAG, json.toString());
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		aq.id(R.id.iv_head_user_icon).image(app.getUserInfo().getUserAvatarUrl(),false,true,0,R.drawable.avatar);
		aq.id(R.id.tv_head_user_name).text(app.getUserInfo().getUserName());
	}
	
	class ViewHolder{
		TextView title;
		TextView stars;
		TextView stars_notice;
		TextView directors;
		TextView directors_notice;
		TextView content;
		ImageView img;
	}
//	class HistoryData{
//		public String id;
//		public String prod_id;
//		public String prod_name;
//		public String prod_type; //1，电影; 2，电视剧; 3，综艺; 4，动漫;
//		public String prod_pic_url;
//		public String stars;
//		public String directors;
//		public String favority_num;
//		public String support_num;
//		public String publish_date;
//		public String score;
//		public String area;
//		public String cur_episode;
//		public String max_episode;
//		public String definition;
//		public String prod_summary;
//		public String duration;
//		public String video_url;
//		public String playback_time;
//		public String prod_subname;
//		public String play_type;
//	}
}
