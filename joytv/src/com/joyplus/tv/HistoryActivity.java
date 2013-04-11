package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import com.joyplus.tv.Service.Return.ReturnUserPlayHistories;
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
	ObjectMapper mapper = new ObjectMapper();
	private List<HotItemInfo> allHistoryList = new ArrayList<HotItemInfo>();
	private List<HotItemInfo> movieHistoryList = new ArrayList<HotItemInfo>();
	private List<HotItemInfo> tvHistoryList = new ArrayList<HotItemInfo>();
	private List<HotItemInfo> dongmanHistoryList = new ArrayList<HotItemInfo>();
	private List<HotItemInfo> zongyiHistoryList = new ArrayList<HotItemInfo>();
	
	private View  selectedView;
	
	private ListView listView;
	
	private PopupWindow popupWindow;
	
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
		btn_fenlei_all.setOnClickListener(this);
		btn_fenlei_movie.setOnClickListener(this);
		btn_fenlei_tv.setOnClickListener(this);
		btn_fenlei_dongman.setOnClickListener(this);
		btn_fenlei_zongyi.setOnClickListener(this);
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
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Toast.makeText(HistoryActivity.this, "seleced index = " + arg2, 100).show();
				
				Dialog dialog = new AlertDialog.Builder(HistoryActivity.this).create();
				dialog.show();
				LayoutInflater inflater = LayoutInflater.from(HistoryActivity.this);
				View view = inflater.inflate(R.layout.layout_history_dialog, null);
				TextView name = (TextView) view.findViewById(R.id.dialog_title);
				name.setText(allHistoryList.get(arg2).prod_name);
				dialog.setContentView(view);
			}
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
				holder.content = (TextView) convertView.findViewById(R.id.content);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.title.setText(data.get(position).prod_name);
			holder.directors.setText(data.get(position).directors);
			holder.stars.setText(data.get(position).stars);
			int prod_type = Integer.valueOf(data.get(position).prod_type);
			String playBack_time = formatDuration(Integer.valueOf(data.get(position).playback_time));
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
			convertView.setBackgroundResource(R.drawable.historty_listitem_drawable_selector);
			return convertView;
		}
		
	}
	
	private String formatDuration(int duration) {
		duration = duration / 1000;
		int h = duration / 3600;
		int m = (duration - h * 3600) / 60;
		int s = duration - (h * 3600 + m * 60);
		String durationValue;
		if (h == 0) {
			durationValue = String.format("%1$02d:%2$02d", m, s);
		} else {
			durationValue = String.format("%1$d:%2$02d:%3$02d", h, m, s);
		}
		return durationValue;
	}

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
			if(popupWindow ==null){
				NavigateView view = new NavigateView(this);
				int [] location = new int[2];
				btn_fenlei_all.getLocationOnScreen(location);
				view.Init(getResources().getStringArray(R.array.navigator_area),
						getResources().getStringArray(R.array.navigator_classification), 
						getResources().getStringArray(R.array.navigator_year), 
						location[0], 
						location[1],
						btn_fenlei_all.getWidth(), 
						btn_fenlei_all.getHeight(),
						new OnResultListener() {
							
							@Override
							public void onResult(View v, boolean isBack, String[] choice) {
								// TODO Auto-generated method stub
								if(isBack){
									popupWindow.dismiss();
								}else{
									if(popupWindow.isShowing()){
										popupWindow.dismiss();
										Toast.makeText(HistoryActivity.this, "selected is " + choice[0] + ","+choice[1]+","+choice[2], Toast.LENGTH_LONG).show();
									}
								}
							}
						});
				view.setLayoutParams(new LayoutParams(0,0));
				popupWindow = new PopupWindow(view, getWindowManager().getDefaultDisplay().getWidth(),
						getWindowManager().getDefaultDisplay().getHeight(), true);
			}
			popupWindow.showAtLocation(listView, Gravity.LEFT | Gravity.BOTTOM, 0, 0);
			break;
		case R.id.fenlei_movie:
			selectedButton.setTextColor(getResources().getColorStateList(R.color.text_color_selector));
			selectedButton.setBackgroundResource(R.drawable.text_drawable_selector);
			btn_fenlei_movie.setTextColor(getResources().getColor(R.color.common_title_selected));
			btn_fenlei_movie.setBackgroundResource(R.drawable.menubg);
			selectedButton = btn_fenlei_movie;
			selectedButton.setPadding(0, 0, 5, 0);
			break;
		case R.id.fenlei_tv:
			selectedButton.setTextColor(getResources().getColorStateList(R.color.text_color_selector));
			selectedButton.setBackgroundResource(R.drawable.text_drawable_selector);
			btn_fenlei_tv.setTextColor(getResources().getColor(R.color.common_title_selected));
			btn_fenlei_tv.setBackgroundResource(R.drawable.menubg);
			selectedButton = btn_fenlei_tv;
			selectedButton.setPadding(0, 0, 5, 0);
			break;
		case R.id.fenlei_dongman:
			selectedButton.setTextColor(getResources().getColorStateList(R.color.text_color_selector));
			selectedButton.setBackgroundResource(R.drawable.text_drawable_selector);
			btn_fenlei_dongman.setTextColor(getResources().getColor(R.color.common_title_selected));
			btn_fenlei_dongman.setBackgroundResource(R.drawable.menubg);
			selectedButton = btn_fenlei_dongman;
			selectedButton.setPadding(0, 0, 5, 0);
			break;
		case R.id.fenlei_zongyi:
			selectedButton.setTextColor(getResources().getColorStateList(R.color.text_color_selector));
			selectedButton.setBackgroundResource(R.drawable.text_drawable_selector);
			btn_fenlei_zongyi.setTextColor(getResources().getColor(R.color.common_title_selected));
			btn_fenlei_zongyi.setBackgroundResource(R.drawable.menubg);
			selectedButton = btn_fenlei_zongyi;
			selectedButton.setPadding(0, 0, 5, 0);
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
		String url = Constant.BASE_URL + "user/playHistories" +"?page_num=1&page_size=10&userid=4742";
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
		initHistoryData(url, json, status, 4);
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
	class ViewHolder{
		TextView title;
		TextView stars;
		TextView directors;
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
