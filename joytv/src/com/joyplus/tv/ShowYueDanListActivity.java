package com.joyplus.tv;

import com.joyplus.tv.ui.MyMovieGridView;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;

public class ShowYueDanListActivity extends Activity implements
		View.OnKeyListener, MyKeyEventKey, View.OnClickListener {

	private Button zuijinguankanBtn, zhuijushoucangBtn, lixianshipinBtn;

	private View beforeView, activeView;

	private MyMovieGridView movieGv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_yuedan_list);

		zuijinguankanBtn = (Button) findViewById(R.id.bt_zuijinguankan);
		zhuijushoucangBtn = (Button) findViewById(R.id.bt_zhuijushoucang);
		lixianshipinBtn = (Button) findViewById(R.id.bt_lixianshipin);
		movieGv = (MyMovieGridView) findViewById(R.id.gv_movie_show);

		zuijinguankanBtn.setTextColor(getResources().getColor(
				R.color.text_active));// 全部分类首先设为激活状态
		zuijinguankanBtn.setBackgroundResource(R.drawable.menubg);// 在换成这张图片时，会刷新组件的padding
		zuijinguankanBtn.setPadding(0, 0, 5, 0);
		zhuijushoucangBtn.setPadding(0, 0, 5, 0);
		lixianshipinBtn.setPadding(0, 0, 5, 0);

		beforeView = zuijinguankanBtn;
		activeView = zuijinguankanBtn;
		
		movieGv.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				beforeViewFoucsStateBack();
				return false;
			}
		});

		zuijinguankanBtn.setOnKeyListener(this);
		zhuijushoucangBtn.setOnKeyListener(this);
		lixianshipinBtn.setOnKeyListener(this);

		zuijinguankanBtn.setOnClickListener(this);
		zhuijushoucangBtn.setOnClickListener(this);
		lixianshipinBtn.setOnClickListener(this);

		movieGv.setAdapter(new MovieAdpter());// 网格布局添加适配器
		movieGv.setSelection(3);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// StatisticsUtils.simulateKey(KeyEvent.KEYCODE_DPAD_LEFT);
		// StatisticsUtils.simulateKey(KeyEvent.KEYCODE_DPAD_RIGHT);
		// movieGv.setSelection(0);

	}

	private void beforeViewFoucsStateBack() {
		if (beforeView instanceof Button) {

			Button tempButton = (Button) beforeView;
			if (beforeView.getId() == activeView.getId()) {
				buttonToActiveState(tempButton);
			} else {
				buttonToPTState(tempButton);
			}
		}
	}

	private void beforeViewActiveStateBack() {
		if (activeView instanceof Button) {

			Button tempButton = (Button) activeView;
			buttonToPTState(tempButton);
		}
	}

	private void buttonToPTState(Button button) {

		button.setBackgroundResource(R.drawable.text_drawable_selector);
		button.setTextColor(getResources().getColorStateList(
				R.color.text_color_selector));
	}

	private void buttonToActiveState(Button button) {

		button.setBackgroundResource(R.drawable.menubg);
		button.setPadding(0, 0, 5, 0);
		button.setTextColor(getResources().getColor(R.color.text_active));
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		if (v instanceof Button) {
			if (action == KeyEvent.ACTION_UP) {
				Button button = (Button) v;
				if (keyCode == KEY_UP || keyCode == KEY_LEFT
						|| keyCode == KEY_DOWN) {
					// searchEt.setFocusable(true);//能够获取焦点
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

	/**
	 * 能够接受 23 和66 但在66值下，没有press效果
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v instanceof Button) {

			if (v.getId() != activeView.getId()) {
				Button button = (Button) v;
				beforeViewActiveStateBack();
				buttonToActiveState(button);
				activeView = v;
			}

		}
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
