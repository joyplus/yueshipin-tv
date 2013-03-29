/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joyplus.tv.Video;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.joyplus.tv.R;

/**
 * The playback controller for the Movie Player.
 */
public class MovieControllerOverlay extends FrameLayout implements
		ControllerOverlay, OnClickListener, AnimationListener {

	private enum State {
		PLAYING, PAUSED, ENDED, ERROR, LOADING
	}

	private static final float ERROR_MESSAGE_RELATIVE_PADDING = 1.0f / 6;

	private Listener listener;

	private final View background;
	private View rootView;
//	private final TimeBar timeBar;

	private View mainView;
	private View mLayoutTop;
	private View mLayoutBottom;
	private View mLayoutTime;
	private final View loadingView;
	private final TextView errorView;
	private View mLayoutControl;
	private final ImageView playPauseReplayView;

	private final Handler handler;
	private final Runnable startHidingRunnable;
	private final Animation hideAnimation;

	private State state;

	private boolean hidden;

	private boolean canReplay = true;
	
	private Handler mHandler = new Handler();
	private long mStartRX = 0;
	
	private TextView mTextViewRate;

	public MovieControllerOverlay(Context context,View rootView) {
		super(context);

		this.rootView = rootView;
		
		mLayoutTop=rootView.findViewById(R.id.LayoutName);
		mLayoutBottom=rootView.findViewById(R.id.relativeLayoutControl);
		mLayoutTime=rootView.findViewById(R.id.LayoutBottomTime);

		state = State.LOADING;

		LayoutParams wrapContent = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		LayoutParams matchParent = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		LayoutInflater inflater = LayoutInflater.from(context);

		background = new View(context);
		background.setBackgroundColor(context.getResources().getColor(
				R.color.darker_transparent));
		addView(background, matchParent);

//		timeBar = new TimeBar(context, this);
//		addView(timeBar, wrapContent);


		loadingView = rootView.findViewById(R.id.LayoutPreload);
		mTextViewRate = (TextView)rootView.findViewById(R.id.textView4);
		
		mStartRX = TrafficStats.getTotalRxBytes();
		if (mStartRX == TrafficStats.UNSUPPORTED) {
			mTextViewRate.setText("Your device does not support traffic stat monitoring.");
		} else {
			mHandler.postDelayed(mRunnable, 500);
		}

		mLayoutControl = rootView.findViewById(R.id.LayoutControl);
		playPauseReplayView= (ImageView)rootView.findViewById(R.id.imageControl1);
		playPauseReplayView.setFocusable(true);
		playPauseReplayView.setClickable(true);
		playPauseReplayView.setOnClickListener(this);
//		mLayoutControl = new ImageView(context);
//		mLayoutControl.setImageResource(R.drawable.player_s_ic_vidcontrol_play);
//		mLayoutControl.setBackgroundResource(R.drawable.player_c_normal);
//		mLayoutControl.setScaleType(ScaleType.CENTER);
//		mLayoutControl.setFocusable(true);
//		mLayoutControl.setClickable(true);
//		mLayoutControl.setOnClickListener(this);
//		addView(mLayoutControl, wrapContent);

		errorView = new TextView(context);
		errorView.setGravity(Gravity.CENTER);
		errorView.setBackgroundColor(0xCC000000);
		errorView.setTextColor(0xFFFFFFFF);
		addView(errorView, matchParent);

		handler = new Handler();
		startHidingRunnable = new Runnable() {
			public void run() {
				startHiding();
			}
		};

		hideAnimation = AnimationUtils
				.loadAnimation(context, R.anim.player_out);
		hideAnimation.setAnimationListener(this);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		setLayoutParams(params);
		hide();
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public void setCanReplay(boolean canReplay) {
		this.canReplay = canReplay;
	}

	public View getView() {
		return this;
	}

	public void showPlaying() {
		state = State.PLAYING;
		showMainView(mLayoutControl);
	}

	public void showPaused() {
		state = State.PAUSED;
		showMainView(mLayoutControl);
	}

	public void showEnded() {
		state = State.ENDED;
		showMainView(mLayoutControl);
	}

	public void showLoading() {
		state = State.LOADING;
		showMainView(loadingView);
	}

	public void showErrorMessage(String message) {
		state = State.ERROR;
		int padding = (int) (getMeasuredWidth() * ERROR_MESSAGE_RELATIVE_PADDING);
		errorView.setPadding(padding, 10, padding, 10);
		errorView.setText(message);
		showMainView(errorView);
	}

	public void resetTime() {
//		timeBar.resetTime();
	}

	public void setTimes(int currentTime, int totalTime) {
		
//		timeBar.setTime(currentTime, totalTime);
	}

	public void hide() {
		boolean wasHidden = hidden;
		hidden = true;
		
		mLayoutTop.setVisibility(View.INVISIBLE);
		mLayoutBottom.setVisibility(View.INVISIBLE);
		mLayoutTime.setVisibility(View.INVISIBLE);
		
		mLayoutControl.setVisibility(View.INVISIBLE);
		loadingView.setVisibility(View.INVISIBLE);
		background.setVisibility(View.INVISIBLE);
//		timeBar.setVisibility(View.INVISIBLE);
		setVisibility(View.INVISIBLE);
		setFocusable(true);
		requestFocus();
		if (listener != null && wasHidden != hidden) {
			listener.onHidden();
		}
	}

	private void showMainView(View view) {
		mainView = view;
		errorView.setVisibility(mainView == errorView ? View.VISIBLE
				: View.INVISIBLE);
		loadingView.setVisibility(mainView == loadingView ? View.VISIBLE
				: View.INVISIBLE);
		mLayoutControl
				.setVisibility(mainView == mLayoutControl ? View.VISIBLE
						: View.INVISIBLE);
		show();
	}

	public void show() {
		boolean wasHidden = hidden;
		hidden = false;
		
		mLayoutTop.setVisibility(View.VISIBLE);
		mLayoutBottom.setVisibility(View.VISIBLE);
		mLayoutTime.setVisibility(View.VISIBLE);
		
		updateViews();
		setVisibility(View.VISIBLE);
		setFocusable(false);
		if (listener != null && wasHidden != hidden) {
			listener.onShown();
		}
		maybeStartHiding();
	}

	private void maybeStartHiding() {
		cancelHiding();
		if (state == State.PLAYING) {
			handler.postDelayed(startHidingRunnable, 2500);
		}
	}

	private void startHiding() {
//		startHideAnimation(timeBar);
		startHideAnimation(mLayoutControl);
	}

	private void startHideAnimation(View view) {
		if (view.getVisibility() == View.VISIBLE) {
			view.startAnimation(hideAnimation);
		}
	}

	private void cancelHiding() {
		handler.removeCallbacks(startHidingRunnable);
		background.setAnimation(null);
//		timeBar.setAnimation(null);
		mLayoutControl.setAnimation(null);
	}

	public void onAnimationStart(Animation animation) {
		// Do nothing.
	}

	public void onAnimationRepeat(Animation animation) {
		// Do nothing.
	}

	public void onAnimationEnd(Animation animation) {
		hide();
	}

	public void onClick(View view) {
		if (listener != null) {
			if (view == mLayoutControl) {
				if (state == State.ENDED) {
					if (canReplay) {
						listener.onReplay();
					}
				} else if (state == State.PAUSED || state == State.PLAYING) {
					listener.onPlayPause();
				}
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (hidden) {
//			show();
//		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (super.onTouchEvent(event)) {
			return true;
		}

		if (hidden) {
			show();
			return true;
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			cancelHiding();
			if (state == State.PLAYING || state == State.PAUSED) {
				listener.onPlayPause();
			}
			break;
		case MotionEvent.ACTION_UP:
			maybeStartHiding();
			break;
		}
		return true;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int bw;
		int bh;
		int y;
		int h = b - t;
		int w = r - l;
		boolean error = errorView.getVisibility() == View.VISIBLE;

//		bw = timeBar.getBarHeight();
//		bh = bw;
		y = b;// - bh;
		
		background.layout(l, y, r, b);

//		if((b - 60- timeBar.getPreferredHeight()) >0 )
//			timeBar.layout(l, b - 60- timeBar.getPreferredHeight(), r, b-60);
//		else
//			timeBar.layout(l, b- timeBar.getPreferredHeight(), r, b);
		// Needed, otherwise the framework will not re-layout in case only the
		// padding is changed
//		timeBar.requestLayout();

		// play pause / next / previous buttons
//		int cx = l + w / 2; // center x
//		int playbackButtonsCenterline = t + h / 2;
		
//		bw = mLayoutControl.getMeasuredWidth();
//		bh = mLayoutControl.getMeasuredHeight();
//		mLayoutControl.layout(cx - bw / 2, playbackButtonsCenterline - bh
//				/ 2, cx + bw / 2, playbackButtonsCenterline + bh / 2);

		// Space available on each side of the error message for the next and
		// previous buttons
		int errorMessagePadding = (int) (w * ERROR_MESSAGE_RELATIVE_PADDING);

		if (mainView != null) {
			layoutCenteredView(mainView, l, t, r, b);
		}
	}

	private void layoutCenteredView(View view, int l, int t, int r, int b) {
		int cw = view.getMeasuredWidth();
		int ch = view.getMeasuredHeight();
		int cl = (r - l - cw) / 2;
		int ct = (b - t - ch) / 2;

		view.layout(cl, ct, cl + cw, ct + ch);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		measureChildren(widthMeasureSpec, heightMeasureSpec);
	}

	private void updateViews() {
		if (hidden) {
			return;
		}
		background.setVisibility(View.VISIBLE);
//		timeBar.setVisibility(View.VISIBLE);
		if(state == State.PAUSED){
			playPauseReplayView.setImageResource(R.drawable.player_s_ic_vidcontrol_play);
		}else if(state == State.PLAYING){
			playPauseReplayView.setImageResource(R.drawable.player_s_ic_vidcontrol_pause);
		}
		else 
			playPauseReplayView.setImageResource(R.drawable.player_s_ic_vidcontrol_reload);
		
		if(state != State.LOADING && state != State.ERROR && 
				!(state == State.ENDED && !canReplay)){
			mLayoutControl.setVisibility(View.VISIBLE);
		}
		else {
			mLayoutControl.setVisibility(View.GONE);
		}
	
//		mLayoutControl
//				.setImageResource(state == State.PAUSED ? R.drawable.player_s_ic_vidcontrol_play
//						: state == State.PLAYING ? R.drawable.player_s_ic_vidcontrol_pause
//								: R.drawable.player_s_ic_vidcontrol_reload);
//		mLayoutControl
//				.setVisibility((state != State.LOADING && state != State.ERROR && !(state == State.ENDED && !canReplay)) ? View.VISIBLE
//						: View.GONE);
		requestLayout();
	}

	// TimeBar listener

	public void onScrubbingStart() {
		cancelHiding();
		listener.onSeekStart();
	}

	public void onScrubbingMove(int time) {
		cancelHiding();
		listener.onSeekMove(time);
	}

	public void onScrubbingEnd(int time) {
		maybeStartHiding();
		listener.onSeekEnd(time);
	}
	private final Runnable mRunnable = new Runnable() {
		long beginTimeMillis, timeTakenMillis, timeLeftMillis, rxByteslast,
				m_bitrate;

		public void run() {

			// long txBytes = TrafficStats.getTotalTxBytes()- mStartTX;
			// TX.setText(Long.toString(txBytes));
			long rxBytes = TrafficStats.getTotalRxBytes() - mStartRX;

			timeTakenMillis = System.currentTimeMillis() - beginTimeMillis;
			beginTimeMillis = System.currentTimeMillis();
			// check how long there is until we reach the desired refresh rate
			m_bitrate = (rxBytes - rxByteslast) * 8 * 1000 / timeTakenMillis;
			rxByteslast = rxBytes;

			mTextViewRate.setText(Long.toString(m_bitrate/8000)+"kb/s");

			// Fun_downloadrate();
			mHandler.postDelayed(mRunnable, 1000);
		}
	};

	@Override
	public void showTimerBar() {
		// TODO Auto-generated method stub
		
	}

}
