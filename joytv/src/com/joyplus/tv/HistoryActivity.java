package com.joyplus.tv;

import java.util.ArrayList;
import java.util.List;

import com.joyplus.tv.entity.ShiPinInfo;
import com.joyplus.tv.ui.NavigateView;
import com.joyplus.tv.ui.NavigateView.OnResultListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

public class HistoryActivity extends Activity implements OnClickListener {
	private Button btn_fenlei_all;
	private Button btn_fenlei_movie;
	private Button btn_fenlei_tv;
	private Button btn_fenlei_dongman;
	private Button btn_fenlei_zongyi;
	private Button selectedButton;
	private List<ShiPinInfo> allHistoryList = new ArrayList<ShiPinInfo>();
	private List<ShiPinInfo> movieHistoryList = new ArrayList<ShiPinInfo>();
	private List<ShiPinInfo> tvHistoryList = new ArrayList<ShiPinInfo>();
	private List<ShiPinInfo> dongmanHistoryList = new ArrayList<ShiPinInfo>();
	private List<ShiPinInfo> zongyiHistoryList = new ArrayList<ShiPinInfo>();
	
	
	private ListView listView;
	
	private PopupWindow popupWindow;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
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
		listView.setAdapter(new MovieAdapter());
	}
	
	class MovieAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 10;
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
			View v = LayoutInflater.from(HistoryActivity.this).inflate(R.layout.item_history_list,null);
			return v;
		}
		
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

}
