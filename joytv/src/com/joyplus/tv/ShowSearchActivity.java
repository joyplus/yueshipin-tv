package com.joyplus.tv;

import com.joyplus.tv.ui.MyMovieGridView;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ShowSearchActivity extends Activity {
	
	private EditText searchEt;
	private MyMovieGridView showGv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_search);
		
		searchEt = (EditText) findViewById(R.id.et_search);
		showGv = (MyMovieGridView) findViewById(R.id.gv_search_show);
		
		showGv.setAdapter(new MovieAdpter());// 网格布局添加适配器
//		movieGv.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
//		movieGv.setFocusable(true);
//		movieGv.setFocusableInTouchMode(true);
//		movieGv.requestFocus();
		showGv.setSelection(3);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		StatisticsUtils.simulateKey(KeyEvent.KEYCODE_DPAD_LEFT);
		StatisticsUtils.simulateKey(KeyEvent.KEYCODE_DPAD_RIGHT);
		showGv.setSelection(0);
		
	}
	
	private class MovieAdpter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 15;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v;
			
			LinearLayout parentLayout = (LinearLayout) findViewById(R.id.ll_show);
			int width = parentLayout.getWidth();
			int height = parent.getHeight();

			v = getLayoutInflater().inflate(R.layout.show_item_show, null);
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
					width / 5, (height - height / 10) / 2);
			v.setPadding(20, 20, 20, 20);
			v.setLayoutParams(lp);
			convertView = v;

			return convertView;
		}

	}

}
