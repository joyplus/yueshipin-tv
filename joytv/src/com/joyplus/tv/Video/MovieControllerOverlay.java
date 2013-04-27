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

import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.net.TrafficStats;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.joyplus.tv.App;
import com.joyplus.tv.R;
import com.joyplus.tv.StatisticsUtils;
import com.joyplus.tv.Adapters.CurrentPlayData;
import com.joyplus.tv.Service.Return.ReturnProgramView;
import com.joyplus.tv.ui.ArcView;

/**
 * The playback controller for the Movie Player.
 */
public class MovieControllerOverlay extends FrameLayout implements
		ControllerOverlay, View.OnTouchListener, AnimationListener {

	private enum State {
		PLAYING, PAUSED, ENDED, ERROR, LOADING
	}

	private App app;
	private static final float ERROR_MESSAGE_RELATIVE_PADDING = 1.0f / 6;

	private Listener listener;

	private final View background;
	private View rootView;
	// private final TimeBar timeBar;

	private View mainView;
	private View mLayoutTop;
	private View mLayoutBottom;
	private View mLayoutTime;
	private final View loadingView;

	private View mLayoutVolume;
	private final TextView errorView;
	private View mLayoutControl;
	private View mLayoutBottomTime2;
	private ImageButton playPauseReplayView;
	private ImageButton playContinueView;
	private ImageButton playFavView;
	private ImageButton playPreView;
	private ImageButton playNextView;

	private final Handler handler;
	private final Runnable startHidingRunnable;
	private final Runnable startHidingVolumeRunnable;
	private final Runnable startHidingTimerBarRunnable;
	private final Runnable startHidingTimesRunnable;
	private final Animation hideAnimation;

	private State state;

	private boolean hidden;

	private boolean canReplay = true;

	private long mStartRX = 0;

	private TextView mTextViewRate;
	private TextView mTextViewPreparedPercent;
	private long mPreparedPercent = 0;
	boolean mShowVolume = false;
	boolean mShowTimerBar = false;
	private boolean mPrepared = false;

	/** 最大声音 */
	private int mMaxVolume;
	/** 当前声音 */
	private int mVolume = -1;
	private AudioManager mAudioManager;
	private ArcView mArcView;
	private CurrentPlayData mCurrentPlayData = null;

	public MovieControllerOverlay(Context context, View rootView) {
		super(context);
		app = (App) context.getApplicationContext();
		this.rootView = rootView;

		mLayoutTop = rootView.findViewById(R.id.LayoutName);
		mLayoutBottom = rootView.findViewById(R.id.relativeLayoutControl);
		mLayoutTime = rootView.findViewById(R.id.LayoutBottomTime);

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

		// timeBar = new TimeBar(context, this);
		// addView(timeBar, wrapContent);

		loadingView = rootView.findViewById(R.id.LayoutPreload);
		mTextViewRate = (TextView) rootView.findViewById(R.id.textView4);
		mTextViewPreparedPercent = (TextView) rootView
				.findViewById(R.id.textView5);

		mLayoutControl = rootView.findViewById(R.id.LayoutControl);
		mLayoutControl.setOnTouchListener(this);

		playPauseReplayView = (ImageButton) rootView
				.findViewById(R.id.imageControl_c);
		playPauseReplayView.setOnTouchListener(this);

		playContinueView = (ImageButton) rootView
				.findViewById(R.id.imageControl_t);
		playContinueView.setOnTouchListener(this);

		mLayoutVolume = rootView.findViewById(R.id.Layout_Volume);

		playFavView = (ImageButton) rootView.findViewById(R.id.imageControl_b);
		playFavView.setOnTouchListener(this);

		playPreView = (ImageButton) rootView.findViewById(R.id.imageControl_r);
		playPreView.setOnTouchListener(this);

		playNextView = (ImageButton) rootView.findViewById(R.id.imageControl_l);
		playNextView.setOnTouchListener(this);

		mArcView = (ArcView) rootView.findViewById(R.id.arcView1);

		mLayoutBottomTime2 = rootView.findViewById(R.id.LayoutBottomTime2);

		errorView = new TextView(context);
		errorView.setGravity(Gravity.CENTER);
		errorView.setBackgroundColor(0xCC000000);
		errorView.setTextColor(0xFFFFFFFF);
		addView(errorView, matchParent);

		handler = new Handler();

		mStartRX = TrafficStats.getTotalRxBytes();
		if (mStartRX == TrafficStats.UNSUPPORTED) {
			mTextViewRate
					.setText("Your device does not support traffic stat monitoring.");
		} else {
			handler.post(mRunnable);
		}

		startHidingRunnable = new Runnable() {
			public void run() {
				startHiding();
			}
		};
		startHidingVolumeRunnable = new Runnable() {
			public void run() {
				startVolumeHiding();
			}
		};
		startHidingTimerBarRunnable = new Runnable() {
			public void run() {
				startTimerBarHiding();
			}
		};
		startHidingTimesRunnable = new Runnable() {
			public void run() {
				startTimesHiding();
			}
		};
		hideAnimation = AnimationUtils
				.loadAnimation(context, R.anim.player_out);
		hideAnimation.setAnimationListener(this);

		mCurrentPlayData = app.getCurrentPlayData();
		playPauseReplayView.setBackgroundResource(R.drawable.player_btn_pause);
		if (mCurrentPlayData != null) {
			if (mCurrentPlayData.prod_time != 0L) {
				TextView mViewTime = (TextView) rootView
						.findViewById(R.id.textView7);
				;
				mViewTime.setText(StatisticsUtils
						.formatDuration(mCurrentPlayData.prod_time));
			}
			if (mCurrentPlayData.prod_src != null) {
				TextView mViewSrc = (TextView) rootView
						.findViewById(R.id.textView9);
				;
				mViewSrc.setText(mCurrentPlayData.prod_src);
			}
			ImageView mImageSrc = (ImageView) rootView
					.findViewById(R.id.imageView1);
			if (mCurrentPlayData.prod_qua == 0)
				mImageSrc.setImageResource(R.drawable.player_720p);
			else
				mImageSrc.setImageResource(R.drawable.player_1080p);

			if (mCurrentPlayData.prod_time == 0) {
				rootView.findViewById(R.id.textView6).setVisibility(View.GONE);
				rootView.findViewById(R.id.textView7).setVisibility(View.GONE);
			}
			ControlViewGone();

		}

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		setLayoutParams(params);
		hide();
	}

	public void returnShowView() {
		mCurrentPlayData = app.getCurrentPlayData();
		if (mCurrentPlayData.prod_type != 1) {
			playPauseReplayView.setBackgroundResource(R.drawable.player_btn_finish);
			if(mCurrentPlayData.prod_favority)
				playFavView.setBackgroundResource(R.drawable.player_btn_unfav);
			
			playContinueView.setVisibility(View.VISIBLE);
			playFavView.setVisibility(View.VISIBLE);
			playPreView.setVisibility(View.VISIBLE);
			playNextView.setVisibility(View.VISIBLE);
			
			if (mCurrentPlayData.CurrentIndex > 0)
				playPreView.setEnabled(true);
			else
				playPreView.setEnabled(false);
			
			ReturnProgramView m_ReturnProgramView = app.get_ReturnProgramView();
			
			if (m_ReturnProgramView != null) {
				switch (mCurrentPlayData.prod_type) {
				case 2:
					if (mCurrentPlayData.CurrentIndex < m_ReturnProgramView.tv.episodes.length)
						playNextView.setEnabled(
								true);
					else
						playNextView.setEnabled(
								false);
					break;
				case 3:
					if (mCurrentPlayData.CurrentIndex < m_ReturnProgramView.show.episodes.length)
						playNextView.setEnabled(
								true);
					else
						playNextView.setEnabled(
								false);
					break;
				}
			}else{
				playPreView.setEnabled(
						false);
				playNextView.setEnabled(false);
			}
		}
	}

	public void ControlViewGone() {

		playContinueView.setVisibility(View.GONE);
		playFavView.setVisibility(View.GONE);
		playPreView.setVisibility(View.GONE);
		playNextView.setVisibility(View.GONE);
	
	}

	public void focusLayoutControl(int index) {
		switch (index) {
		case 0:
			playPauseReplayView.setFocusable(true);
			playPauseReplayView.requestFocus();
			break;
		case 1:
			playNextView.setFocusable(true);
			playNextView.requestFocus();

			break;
		case 2:
			playFavView.setFocusable(true);
			playFavView.requestFocus();
			break;
		case 3:
			playPreView.setFocusable(true);
			playPreView.requestFocus();

			break;
		case 4:
			playContinueView.setFocusable(true);
			playContinueView.requestFocus();

			break;
		default:
			break;
		}

	}

	public void setAudioManager(AudioManager mAudioManager) {
		this.mAudioManager = mAudioManager;
		mMaxVolume = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

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

	public void showPlayingAtFirstTime() {
		if (loadingView.getVisibility() == View.VISIBLE) {
			state = State.PLAYING;
			errorView.setVisibility(View.GONE);
			loadingView.setVisibility(View.GONE);

			mLayoutTop.setVisibility(View.VISIBLE);
			mLayoutBottom.setVisibility(View.VISIBLE);
			mLayoutTime.setVisibility(View.VISIBLE);
			handler.postDelayed(startHidingTimerBarRunnable, 2500);
		}
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
		// timeBar.resetTime();
	}

	public void removeTimeTakenMillis() {
		handler.removeCallbacks(mRunnable);
	}

	public void setTimes(int currentTime, int totalTime) {

		// if(currentTime >0 && )
		// handler.removeCallbacks(mRunnable);
		// timeBar.setTime(currentTime, totalTime);
	}

	public void hide() {
		boolean wasHidden = hidden;
		hidden = true;

		hideVolume();
		mLayoutBottomTime2.setVisibility(View.GONE);
		mLayoutTop.setVisibility(View.GONE);
		mLayoutBottom.setVisibility(View.GONE);
		mLayoutTime.setVisibility(View.GONE);

		mLayoutControl.setVisibility(View.GONE);
		mLayoutVolume.setVisibility(View.GONE);
		loadingView.setVisibility(View.GONE);

		background.setVisibility(View.GONE);
		// timeBar.setVisibility(View.INVISIBLE);
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
				: View.GONE);
		loadingView.setVisibility(mainView == loadingView ? View.VISIBLE
				: View.GONE);
		mLayoutControl.setVisibility(mainView == mLayoutControl ? View.VISIBLE
				: View.GONE);
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

	public void HidingTimes() {
		handler.removeCallbacks(startHidingTimesRunnable);
		handler.postDelayed(startHidingTimesRunnable, 500);
	}

	public void ShowTimes() {
		mLayoutBottomTime2.setVisibility(View.VISIBLE);
	}

	private void maybeStartHiding() {
		cancelHiding();
		if (state == State.PLAYING) {
			handler.postDelayed(startHidingRunnable, 2500);
		}
	}

	private void startHiding() {
		// startHideAnimation(timeBar);
		startHideAnimation(mLayoutControl);
	}

	private void startVolumeHiding() {
		startHideAnimation(mLayoutVolume);
	}

	private void startTimerBarHiding() {
		startHideAnimation(mLayoutTop);
		startHideAnimation(mLayoutBottom);
		startHideAnimation(mLayoutTime);
		mShowTimerBar = false;
	}

	private void startTimesHiding() {
		startHideAnimation(mLayoutBottomTime2);
	}

	private void startHideAnimation(View view) {
		if (view.getVisibility() == View.VISIBLE) {
			view.startAnimation(hideAnimation);
		}
	}

	private void cancelHiding() {
		handler.removeCallbacks(startHidingRunnable);
		background.setAnimation(null);
		// timeBar.setAnimation(null);
		mLayoutControl.setAnimation(null);
	}

	private void cancelHidingVolume() {
		handler.removeCallbacks(startHidingVolumeRunnable);
		mLayoutVolume.setAnimation(null);
	}

	private void cancelHidingTimerBar() {
		handler.removeCallbacks(startHidingTimerBarRunnable);
		mLayoutTop.setAnimation(null);
		mLayoutBottom.setAnimation(null);
		mLayoutTime.setAnimation(null);
	}

	public void onAnimationStart(Animation animation) {
		// Do nothing.
	}

	public void onAnimationRepeat(Animation animation) {
		// Do nothing.
	}

	public void onAnimationEnd(Animation animation) {
		if (mShowVolume)
			hideVolume();
		else
			hide();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (hidden) {
			show();
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean isHidden() {
		return hidden;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (super.onTouchEvent(event)) {
			return true;
		}

		if (hidden) {
			show();
			return true;
		}
		if (view == playPauseReplayView) {
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
		} else if (view == playContinueView) {

		} else if (view == playFavView) {

		} else if (view == playPreView) {

		} else if (view == playNextView) {

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

		// bw = timeBar.getBarHeight();
		// bh = bw;
		y = b;// - bh;

		background.layout(l, y, r, b);

		// if((b - 60- timeBar.getPreferredHeight()) >0 )
		// timeBar.layout(l, b - 60- timeBar.getPreferredHeight(), r, b-60);
		// else
		// timeBar.layout(l, b- timeBar.getPreferredHeight(), r, b);
		// Needed, otherwise the framework will not re-layout in case only the
		// padding is changed
		// timeBar.requestLayout();

		// play pause / next / previous buttons
		// int cx = l + w / 2; // center x
		// int playbackButtonsCenterline = t + h / 2;

		// bw = mLayoutControl.getMeasuredWidth();
		// bh = mLayoutControl.getMeasuredHeight();
		// mLayoutControl.layout(cx - bw / 2, playbackButtonsCenterline - bh
		// / 2, cx + bw / 2, playbackButtonsCenterline + bh / 2);

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
		// timeBar.setVisibility(View.VISIBLE);
		if (state == State.PAUSED) {
			playPauseReplayView
					.setBackgroundResource(R.drawable.player_btn_play_play);
		} else if (state == State.PLAYING) {
			playPauseReplayView
					.setBackgroundResource(R.drawable.player_btn_pause);
		} else
			playPauseReplayView
					.setBackgroundResource(R.drawable.player_btn_finish);

		if (state != State.LOADING && state != State.ERROR
				&& !(state == State.ENDED && !canReplay)) {
			mLayoutControl.setVisibility(View.VISIBLE);
		} else {
			mLayoutControl.setVisibility(View.GONE);
		}
		// mLayoutControl
		// .setImageResource(state == State.PAUSED ?
		// R.drawable.player_s_ic_vidcontrol_play
		// : state == State.PLAYING ? R.drawable.player_s_ic_vidcontrol_pause
		// : R.drawable.player_s_ic_vidcontrol_reload);
		// mLayoutControl
		// .setVisibility((state != State.LOADING && state != State.ERROR &&
		// !(state == State.ENDED && !canReplay)) ? View.VISIBLE
		// : View.GONE);
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
		long beginTimeMillis, timeTakenMillis, rxByteslast, m_bitrate;

		public void run() {

			// long txBytes = TrafficStats.getTotalTxBytes()- mStartTX;
			// TX.setText(Long.toString(txBytes));
			long rxBytes = TrafficStats.getTotalRxBytes() - mStartRX;

			timeTakenMillis = System.currentTimeMillis() - beginTimeMillis;
			beginTimeMillis = System.currentTimeMillis();
			// check how long there is until we reach the desired refresh rate
			m_bitrate = ((rxBytes - rxByteslast) * 8 * 1000 / timeTakenMillis) / 8000;
			rxByteslast = rxBytes;

			mTextViewRate.setText("（" + Long.toString(m_bitrate) + "kb/s");
			mPreparedPercent = mPreparedPercent + m_bitrate;
			if (mPreparedPercent >= 100 && mPreparedPercent / 100 <= 100)
				mTextViewPreparedPercent.setText("）,已完成"
						+ Long.toString(mPreparedPercent / 100) + "%");

			// Fun_downloadrate();
			handler.postDelayed(mRunnable, 500);
		}
	};

	@Override
	public void showTimerBar() {
		// TODO Auto-generated method stub
		mShowTimerBar = true;
		mLayoutTop.setVisibility(View.VISIBLE);
		mLayoutBottom.setVisibility(View.VISIBLE);
		mLayoutTime.setVisibility(View.VISIBLE);
	}

	public void hideTimerBar() {
		// TODO Auto-generated method stub
		mShowTimerBar = false;
		handler.removeCallbacks(startHidingTimerBarRunnable);
		handler.postDelayed(startHidingTimerBarRunnable, 2500);
	}

	public void showVolume(int index) {
		// TODO Auto-generated method stub
		if (mLayoutControl.getVisibility() == View.VISIBLE) {
			mLayoutControl.setAnimation(null);
			mLayoutControl.setVisibility(View.GONE);
		}
		if (!mShowVolume) {
			mShowVolume = true;
			mLayoutVolume.setVisibility(View.VISIBLE);
		} else
			cancelHidingVolume();
		onVolumeSlide(index);
		handler.postDelayed(startHidingVolumeRunnable, 2500);
	}

	public void hideVolume() {
		// TODO Auto-generated method stub
		mShowVolume = false;
		mLayoutVolume.setAnimation(null);
		mLayoutVolume.setVisibility(View.GONE);
	}

	/**
	 * 滑动改变声音大小
	 * 
	 * @param percent
	 */
	private void onVolumeSlide(int index) {
		if (index > mMaxVolume)
			index = mMaxVolume;
		else if (index < 0)
			index = 0;

		// 变更声音
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
		int mAngle = index * 360 / mMaxVolume;
		// 变更进度条
		if(index == 0)
			mArcView.setBackgroundResource(R.drawable.player_volume_mute);
		else{
			mArcView.setBackgroundResource(R.drawable.player_volume);
			
		}
		mArcView.SetAngle(mAngle);

	}

	public void setPrepared(boolean mPrepared) {
		this.mPrepared = mPrepared;
	}

}
