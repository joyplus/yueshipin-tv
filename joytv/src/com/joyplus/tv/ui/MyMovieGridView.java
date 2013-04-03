package com.joyplus.tv.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.GridView;

public class MyMovieGridView extends GridView {
	
	public MyMovieGridView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MyMovieGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyUp(keyCode, event);
//		super.onKeyUp(keyCode, event);
//		return true;
	}
	
}
