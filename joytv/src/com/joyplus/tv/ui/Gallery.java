package com.joyplus.tv.ui;

import com.joyplus.tv.R;
import com.joyplus.utils.Log;
import com.joyplus.utils.Utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

public class Gallery extends LinearLayout {

	private OnItemClickListener itmeClickListener;
	private OnItemSelectedListener itmeSelectedListener;
	private BaseAdapter adapter;
//	private int selectedIndex;
//	private Scroller mScroller;
//	private int currentLeft;
	private static final String TAG = "Gallery";
//	private LinearLayout layout;
//	private TranslateAnimation leftAnim;
//	private TranslateAnimation rightAnim;
	private int itemWidth = 0;
	private Handler handler = new Handler();
	private int space;

	public Gallery(Context context, AttributeSet attrs) {
		super(context, attrs);
//		mScroller = new Scroller(context);
//		currentLeft = 0;
//		layout = new LinearLayout(context);
//		layout.setGravity(Gravity.CENTER_VERTICAL);
		setGravity(Gravity.CENTER_VERTICAL);
//		this.setVerticalScrollBarEnabled(false); // 禁用垂直滚动
//		this.setHorizontalScrollBarEnabled(true); // 禁用水平滚动
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Gallery); 
		space = Math.round(Utils.getStandardValue(context, (int) array.getDimension(R.styleable.Gallery_spacing, 0)));
//		space = Math.round(Utils.getStandardValue(context,30));
//		TypedArray typedArray=context.obtainStyledAttributes(attrs, android.R.attr.spacing); 
//		// setSc
//		attrs.get
		// TODO Auto-generated constructor stub
	}

	public Gallery(Context context) {
		super(context);
//		mScroller = new Scroller(context);
//		currentLeft = 0;
//		layout = new LinearLayout(context);
//		layout.setGravity(Gravity.CENTER_VERTICAL);
		setGravity(Gravity.CENTER_VERTICAL);
//		this.setVerticalScrollBarEnabled(false); // 禁用垂直滚动
//		this.setHorizontalScrollBarEnabled(true); // 禁用水平滚动
		// TODO Auto-generated constructor stub
	}

	public void setAdapter(BaseAdapter adapter) {
		removeAllViews();
//		layout.removeAllViews();
		this.adapter = adapter;
		if (this.adapter == null) {
			return;
		}
		for (int i = 0; i < adapter.getCount(); i++) {
			// final Map<String,Object> map=adapter.getItem(i);
			View view = adapter.getView(i, null, this);
			final int index = i;
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (itmeClickListener != null) {
						itmeClickListener.onItemClick(null, v, index, 0);
					}
					//鼠标点击view，高亮不切换
//					if (itmeSelectedListener != null) {
//						itmeSelectedListener.onItemSelected(null, v, index, 0);
//					}
				}
			});
			view.setOnTouchListener(new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					
					requestDisallowInterceptTouchEvent(true);
					return false;
				}
			});
//			layout.setOrientation(LinearLayout.HORIZONTAL);
			setOrientation(LinearLayout.HORIZONTAL);
			if(itemWidth ==0 &&view.getLayoutParams().width!=0){
				itemWidth = view.getLayoutParams().width;
			}
//				layout.addView(new TextView(getContext()), new LayoutParams(space, 0));
			Log.d(TAG, "space = " + space);
			if(i!=0){
				TextView tv = new TextView(getContext());
				addView(tv, new LayoutParams(space,0));
//				MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
//				params.setMargins(space, params.topMargin, params.rightMargin, params.rightMargin);
			}
			addView(view, view.getLayoutParams());
//			layout.addView(view, view.getLayoutParams());
		}
//		this.addView(layout, new LayoutParams(LayoutParams.WRAP_CONTENT,
//				LayoutParams.FILL_PARENT));
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		
//		switch (ev.getAction()) {
//		case MotionEvent.ACTION_DOWN:
//			
//			break;
//		case MotionEvent.ACTION_MOVE:
//			
//			return true;
//		default:
//			break;
//		}
		
		return super.onTouchEvent(ev);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.itmeClickListener = listener;
	}

	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		this.itmeSelectedListener = listener;
	}

	public BaseAdapter getAdapter() {
		return this.adapter;
	}

//	public void setSelection(int index) {
//		this.selectedIndex = index - 1;
//		layout.setVisibility(View.INVISIBLE);
//		if (layout.getChildAt(0) != null) {
//					// TODO Auto-generated method stub
//			Log.d(TAG, "selectedIndex ------>" + selectedIndex);
//			Log.d(TAG, "getWidth ------>" + itemWidth);
//			layout.scrollTo(itemWidth*(selectedIndex + 1), 0);
//			layout.setVisibility(View.VISIBLE);
//			// currentLeft = layout.getChildAt(0).getWidth()*(selectedIndex+1);
//			if (itmeSelectedListener != null) {
//				itmeSelectedListener.onItemSelected(null,layout.getChildAt((selectedIndex + 1)), index, 0);
//			}
//		} else {
//			selectedIndex = 0;
//		}
//
//	}

//	public int getSelectedItemPosition() {
//		return (selectedIndex + 1);
//	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		// if(layout.is`)
//		if (layout.getChildCount() < 2) {
//			return true;
//		}
//		if (leftAnim != null && leftAnim.hasStarted() && !leftAnim.hasEnded()) {
//			return true;
//		}
//		if (rightAnim != null && rightAnim.hasStarted()
//				&& !rightAnim.hasEnded()) {
//			return true;
//		}
//		switch (keyCode) {
//		case KeyEvent.KEYCODE_DPAD_LEFT:
//			if (selectedIndex >= 0) {
//				selectedIndex -= 1;
//				// mScroller.startScroll(currentLeft, 0,
//				// -getChildAt((selectedIndex+1)).getWidth(), 0, 500);
//				if(adapter.getCount()-selectedIndex){
//					layout.scrollTo(layout.getChildAt(0).getWidth()*(selectedIndex + 1), 0);
//					// TranslateAnimation anim = new
//					// TranslateAnimation(-layout.getChildAt(0).getWidth(),0,0,0);
//					if (leftAnim == null) {
//						leftAnim = new TranslateAnimation(-layout.getChildAt(0).getWidth(), 0, 0, 0);
//						leftAnim.setDuration(250);
//					}
//					layout.startAnimation(leftAnim);
//				}
//				if (itmeSelectedListener != null) {
//					Log.d(TAG, "selectedIndex = " + selectedIndex);
//					itmeSelectedListener.onItemSelected(null,
//							layout.getChildAt((selectedIndex + 1)),
//							selectedIndex + 1, 0);
//				}
//			}
//			return true;
//		case KeyEvent.KEYCODE_DPAD_RIGHT:
//			if (selectedIndex < adapter.getCount() - 2) {
//				selectedIndex += 1;
//				layout.scrollTo(layout.getChildAt(0).getWidth()
//						* (selectedIndex + 1), 0);
//				if (rightAnim == null) {
//					rightAnim = new TranslateAnimation(layout.getChildAt(0)
//							.getWidth(), 0, 0, 0);
//					rightAnim.setDuration(250);
//				}
//				layout.startAnimation(rightAnim);
//				if (itmeSelectedListener != null) {
//					Log.d(TAG, "selectedIndex = " + selectedIndex);
//					itmeSelectedListener.onItemSelected(null,
//							layout.getChildAt((selectedIndex + 1)),
//							selectedIndex + 1, 0);
//				}
//			}
//			return true;
//		case KeyEvent.KEYCODE_DPAD_CENTER:
//		case KeyEvent.KEYCODE_ENTER:
//			if (itmeClickListener != null) {
//				itmeClickListener.onItemClick(null,
//						layout.getChildAt((selectedIndex + 1)),
//						selectedIndex + 1, 0);
//			}
//			return true;
//		default:
//			super.onKeyDown(keyCode, event);
//			break;
//		}
//		return false;
		return super.onKeyDown(keyCode, event);
	}

//	public void showPre() {
//		if (selectedIndex >= 0) {
//			selectedIndex -= 1;
//			layout.scrollTo(layout.getChildAt(0).getWidth()*(selectedIndex + 1), 0);
//			if (leftAnim == null) {
//				leftAnim = new TranslateAnimation(-layout.getChildAt(0).getWidth(), 0, 0, 0);
//				leftAnim.setDuration(250);
//				// leftAnim.ha
//			}
//			layout.startAnimation(leftAnim);
//			if (itmeSelectedListener != null) {
//				Log.d(TAG, "selectedIndex = " + selectedIndex);
//				itmeSelectedListener.onItemSelected(null,
//						layout.getChildAt((selectedIndex + 1)),
//						selectedIndex + 1, 0);
//			}
//		}
//	}

//	public void showNext() {
//		if (selectedIndex < adapter.getCount() - 2) {
//			selectedIndex += 1;
//			layout.scrollTo(layout.getChildAt(0).getWidth()
//					* (selectedIndex + 1), 0);
//			if (rightAnim == null) {
//				rightAnim = new TranslateAnimation(layout.getChildAt(0)
//						.getWidth(), 0, 0, 0);
//				rightAnim.setDuration(250);
//			}
//			layout.startAnimation(rightAnim);
//			if (itmeSelectedListener != null) {
//				Log.d(TAG, "selectedIndex = " + selectedIndex);
//				itmeSelectedListener.onItemSelected(null,
//						layout.getChildAt((selectedIndex + 1)),
//						selectedIndex + 1, 0);
//			}
//		}
//	}
}
