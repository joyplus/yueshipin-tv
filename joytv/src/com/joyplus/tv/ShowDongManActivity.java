package com.joyplus.tv;

import com.joyplus.tv.ui.MyGridView;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ShowDongManActivity extends Activity implements
		View.OnKeyListener, MyKeyEventKey, View.OnClickListener {

	private EditText searchEt;
	private MyGridView shoucanggengxinGv, qitadongmanGv;
	private LinearLayout qinziLL, rexueLL, hougongLL, tuiliLL, jizhanLL,
			gaoxiaoLL;

	private Button zuijinguankanBtn, zhuijushoucangBtn, lixianshipinBtn,
			mFenLeiBtn;

	private View beforeView, activeView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_dongman);

		initView();
		initState();
	}

	private void initView() {

		searchEt = (EditText) findViewById(R.id.et_search);
		mFenLeiBtn = (Button) findViewById(R.id.bt_quanbufenlei);

		shoucanggengxinGv = (MyGridView) findViewById(R.id.gv_dongman_shoucang);
		qitadongmanGv = (MyGridView) findViewById(R.id.gv_qitadianshiju);

		qinziLL = (LinearLayout) findViewById(R.id.ll_qinzidongman);
		rexueLL = (LinearLayout) findViewById(R.id.ll_rexuedongman);
		hougongLL = (LinearLayout) findViewById(R.id.ll_hougongdongman);
		tuiliLL = (LinearLayout) findViewById(R.id.ll_tuilidongman);
		jizhanLL = (LinearLayout) findViewById(R.id.ll_jizhandongman);
		gaoxiaoLL = (LinearLayout) findViewById(R.id.ll_gaoxiaodongman);

		zuijinguankanBtn = (Button) findViewById(R.id.bt_zuijinguankan);
		zhuijushoucangBtn = (Button) findViewById(R.id.bt_zhuijushoucang);
		lixianshipinBtn = (Button) findViewById(R.id.bt_lixianshipin);

		searchEt.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();
				if (action == KeyEvent.ACTION_UP) {
					if (keyCode == KEY_UP) {

						turnToNonButtonState();
					}
				}
				return false;
			}
		});

		shoucanggengxinGv.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();

				if (action == KeyEvent.ACTION_UP) {
					if (keyCode == KEY_RIGHT) {

						turnToNonButtonState();
					}
				}
				return false;
			}
		});

		addKeyListener();
		addClickListener();

	}

	private void addKeyListener() {

		qinziLL.setOnKeyListener(this);
		rexueLL.setOnKeyListener(this);
		hougongLL.setOnKeyListener(this);
		tuiliLL.setOnKeyListener(this);
		jizhanLL.setOnKeyListener(this);
		gaoxiaoLL.setOnKeyListener(this);

		zuijinguankanBtn.setOnKeyListener(this);
		zhuijushoucangBtn.setOnKeyListener(this);
		lixianshipinBtn.setOnKeyListener(this);
		mFenLeiBtn.setOnKeyListener(this);
	}

	private void addClickListener() {

		qinziLL.setOnClickListener(this);
		rexueLL.setOnClickListener(this);
		hougongLL.setOnClickListener(this);
		tuiliLL.setOnClickListener(this);
		jizhanLL.setOnClickListener(this);
		gaoxiaoLL.setOnClickListener(this);

		zuijinguankanBtn.setOnClickListener(this);
		zhuijushoucangBtn.setOnClickListener(this);
		lixianshipinBtn.setOnClickListener(this);
		mFenLeiBtn.setOnClickListener(this);
	}

	private void initState() {

		beforeView = mFenLeiBtn;
		activeView = mFenLeiBtn;

		searchEt.setFocusable(false);// 搜索焦点消失
		// movieGv.setNextFocusLeftId(R.id.bt_quanbufenlei);// 网格向左 全部分类获得焦点
		// movieGv.setNextFocusDownId(R.id.bt_quanbufenlei);// 网格向左 全部分类获得焦点
		// movieGv.setNextFocusUpId(R.id.bt_quanbufenlei);// 网格向左 全部分类获得焦点
		// movieGv.setNextFocusRightId(R.id.bt_quanbufenlei);// 网格向左 全部分类获得焦点

		mFenLeiBtn.setTextColor(getResources().getColor(R.color.text_active));// 全部分类首先设为激活状态
		mFenLeiBtn.setBackgroundResource(R.drawable.menubg);// 在换成这张图片时，会刷新组件的padding
		qinziLL.setPadding(0, 0, 5, 0);
		rexueLL.setPadding(0, 0, 5, 0);
		hougongLL.setPadding(0, 0, 5, 0);
		tuiliLL.setPadding(0, 0, 5, 0);
		jizhanLL.setPadding(0, 0, 5, 0);
		gaoxiaoLL.setPadding(0, 0, 5, 0);
		zuijinguankanBtn.setPadding(0, 0, 5, 0);
		zhuijushoucangBtn.setPadding(0, 0, 5, 0);
		lixianshipinBtn.setPadding(0, 0, 5, 0);
		mFenLeiBtn.setPadding(0, 0, 5, 0);

		shoucanggengxinGv.setAdapter(new MovieAdpter());// 网格布局添加适配器
		qitadongmanGv.setAdapter(new MovieAdpter());// 网格布局添加适配器

		shoucanggengxinGv.setSelection(3);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		StatisticsUtils.simulateKey(KeyEvent.KEYCODE_DPAD_LEFT);
//		StatisticsUtils.simulateKey(KeyEvent.KEYCODE_DPAD_RIGHT);
//		shoucanggengxinGv.setSelection(0);

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

	private void turnToNonButtonState() {

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
		linearLayout.setPadding(0, 0, 5, 0);
		tempButton.setTextColor(getResources().getColor(R.color.text_active));
		tempButton.setCompoundDrawablesWithIntrinsicBounds(getResources()
				.getDrawable(R.drawable.side_hot_active), null, null, null);
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
				if (keyCode == KEY_UP || keyCode == KEY_LEFT
						|| keyCode == KEY_DOWN) {
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

	/**
	 * 能够接受 23 和66 但在66值下，没有press效果
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		v.setOnKeyListener(null);
		if (v instanceof LinearLayout) {

			LinearLayout linearLayout = (LinearLayout) v;
			if (v.getId() != activeView.getId()) {
				beforeViewActiveStateBack();
				linearLayoutToActiveState(linearLayout);
				activeView = v;
			}
		} else if (v instanceof Button) {

			if (v.getId() != activeView.getId()) {
				Button button = (Button) v;
				beforeViewActiveStateBack();
				buttonToActiveState(button);
				activeView = v;
			}

		}
		v.setOnKeyListener(this);
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
			ImageView iv = new ImageView(getApplicationContext());
			iv.setBackgroundResource(R.drawable.movie_pic);

			return iv;
		}

	}

}
