package com.joyplus.tv;

import com.joyplus.tv.ui.MyMovieGridView;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;

public class ShowYueDanActivity extends Activity implements View.OnKeyListener{
	
	private Button zuijinguankanBtn, zhuijushoucangBtn, lixianshipinBtn;
	
	private Button dianshijuYDBtn,dianyingYDBtn;
	
	private View beforeView, activeView;
	
	private MyMovieGridView movieGv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_yuedan);
		
		zuijinguankanBtn = (Button) findViewById(R.id.bt_zuijinguankan);
		zhuijushoucangBtn = (Button) findViewById(R.id.bt_zhuijushoucang);
		lixianshipinBtn = (Button) findViewById(R.id.bt_lixianshipin);
		movieGv = (MyMovieGridView) findViewById(R.id.gv_movie_show);
		
		dianshijuYDBtn = (Button) findViewById(R.id.bt_dianshijuyuedan);
		dianyingYDBtn = (Button) findViewById(R.id.bt_dianyingyuedan);
		
		dianyingYDBtn.setTextColor(getResources().getColor(R.color.text_orange));// 全部分类首先设为激活状态
		dianyingYDBtn.setBackgroundResource(R.drawable.menubg);//在换成这张图片时，会刷新组件的padding
		dianshijuYDBtn.setPadding(0, 0, 5, 0);
		dianyingYDBtn.setPadding(0, 0, 5, 0);
		zuijinguankanBtn.setPadding(0, 0, 5, 0);
		zhuijushoucangBtn.setPadding(0, 0, 5, 0);
		lixianshipinBtn.setPadding(0, 0, 5, 0);
		
		beforeView = dianyingYDBtn;
		activeView = dianyingYDBtn;
		
		zuijinguankanBtn.setOnKeyListener(this);
		zhuijushoucangBtn.setOnKeyListener(this);
		lixianshipinBtn.setOnKeyListener(this);
		
		dianshijuYDBtn.setOnKeyListener(this);
		dianyingYDBtn.setOnKeyListener(this);
		
		movieGv.setAdapter(new MovieAdpter());// 网格布局添加适配器
		movieGv.setSelection(3);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		StatisticsUtils.simulateKey(KeyEvent.KEYCODE_DPAD_LEFT);
		StatisticsUtils.simulateKey(KeyEvent.KEYCODE_DPAD_RIGHT);
		movieGv.setSelection(0);
		
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
	
	//转到类似Gridview组件上
	private void turnToGridViewState(){
		
		if(beforeView.getId() == activeView.getId()) {
			
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
		linearLayout
				.setBackgroundResource(R.drawable.text_drawable_selector);
		tempButton.setTextColor(getResources().getColor(R.color.text_pt));
		tempButton.setCompoundDrawablesWithIntrinsicBounds(getResources()
				.getDrawable(R.drawable.side_hot_normal), null, null, null);
	}
	
	private void buttonToPTState(Button button) {
		
		button.setBackgroundResource(R.drawable.text_drawable_selector);
		button.setTextColor(getResources().getColor(R.color.text_pt));
	}
	
	private void linearLayoutToActiveState(LinearLayout linearLayout) {
		
		Button tempButton = (Button) linearLayout.getChildAt(0);
		linearLayout
				.setBackgroundResource(R.drawable.menubg);
		linearLayout.setPadding(0, 0, 5, 0);
		tempButton.setTextColor(getResources().getColor(R.color.text_orange));
		tempButton.setCompoundDrawablesWithIntrinsicBounds(getResources()
				.getDrawable(R.drawable.side_hot_active), null, null, null);
	}
	
	private void buttonToActiveState(Button button) {
		
		button.setBackgroundResource(R.drawable.menubg);
		button.setPadding(0, 0, 5, 0);
		button.setTextColor(getResources().getColor(R.color.text_orange));
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		if (v instanceof LinearLayout) {
			LinearLayout linearLayout = (LinearLayout) v;
			Button button = (Button) linearLayout.getChildAt(0);
			

			if (action == KeyEvent.ACTION_UP) {

				if (keyCode == KeyEvent.KEYCODE_NUMPAD_8
						|| keyCode == KeyEvent.KEYCODE_NUMPAD_4
						|| keyCode == KeyEvent.KEYCODE_NUMPAD_2) {
					beforeViewFoucsStateBack();
					button.setTextColor(getResources().getColor(
							R.color.text_foucs));
					button.setCompoundDrawablesWithIntrinsicBounds(
							getResources().getDrawable(
									R.drawable.side_hot_active), null, null,
							null);
				} else if (keyCode == KeyEvent.KEYCODE_NUMPAD_5
						&& v.getId() != activeView.getId()) {
					beforeViewActiveStateBack();
					linearLayoutToActiveState(linearLayout);
					activeView = v;
				} 
			}
		} else if (v instanceof Button) {
			if (action == KeyEvent.ACTION_UP) {
				Button button = (Button) v;
				if (keyCode == KeyEvent.KEYCODE_NUMPAD_8
						|| keyCode == KeyEvent.KEYCODE_NUMPAD_4
						|| keyCode == KeyEvent.KEYCODE_NUMPAD_2) {
//					searchEt.setFocusable(true);//能够获取焦点
					beforeViewFoucsStateBack();
					button.setTextColor(getResources().getColor(
							R.color.text_foucs));
					button.setBackgroundResource(R.drawable.text_drawable_selector);
				} else if (keyCode == KeyEvent.KEYCODE_NUMPAD_5
						&& v.getId() != activeView.getId()) {
					beforeViewActiveStateBack();
					buttonToActiveState(button);
					activeView = v;
					
				}
			}
		}
		beforeView = v;
		return false;
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
			
			LinearLayout parentLayout = (LinearLayout) findViewById(R.id.ll_movie_show);
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
