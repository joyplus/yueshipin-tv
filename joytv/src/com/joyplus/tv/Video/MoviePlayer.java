/*
 * Copyright (C) 2009 The Android Open Source Project
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.R.integer;
import android.R.string;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.net.http.SslCertificate;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.androidquery.AQuery;
import com.joyplus.tv.App;
import com.joyplus.tv.BuildConfig;
import com.joyplus.tv.Constant;
import com.joyplus.tv.HistoryActivity;
import com.joyplus.tv.R;
import com.joyplus.tv.StatisticsUtils;
import com.joyplus.tv.Adapters.CurrentPlayData;
import com.joyplus.tv.Service.Return.ReturnProgramView;

public class MoviePlayer implements MediaPlayer.OnErrorListener,
		MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
		MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener,
		MediaPlayer.OnSeekCompleteListener,
		MediaPlayer.OnVideoSizeChangedListener, ControllerOverlay.Listener {
	@SuppressWarnings("unused")
	private static final String TAG = "MoviePlayer";

	private static final String KEY_VIDEO_POSITION = "video-position";
	private static final String KEY_RESUMEABLE_TIME = "resumeable-timeout";

	// Copied from MediaPlaybackService in the Music Player app.
	private static final String SERVICECMD = "com.android.music.musicservicecommand";
	private static final String CMDNAME = "command";
	private static final String CMDPAUSE = "pause";

	// If we resume the acitivty with in RESUMEABLE_TIMEOUT, we will keep
	// playing.
	// Otherwise, we pause the player.
	private static final long RESUMEABLE_TIMEOUT = 3 * 60 * 1000; // 3 mins

	private int JUMP_TIME = 0;
	private int JUMP_TIME_TIMES = 0;// 检查是否处在快进模式中
	private boolean RETURNMODE = false;// 检查是否处在tv的返回模式中
	private int CURRENT_KEY = 0;
	private int prod_type = 0;
	private int currentKeyEvent = 0;

	private int seekBarWidthOffset = 24;

	private Context mContext;
	private final VideoView mVideoView;
	private final Bookmarker mBookmarker;
	private final Uri mUri;
	private Handler mHandler = new Handler();
	// private final ActionBar mActionBar;
	private ControllerOverlay mController;

	private long mResumeableTime = Long.MAX_VALUE;
	private int mVideoPosition = 0;
	private boolean mHasPaused = false;

	// If the time bar is being dragged.
	private boolean mDragging;

	// If the time bar is visible.
	private boolean mShowing;
	
	private boolean mSeekComplete;

	private SeekBar sb;
	private TextView textView1;
	private TextView textView2;
	private TextView mTextViewTime2;
	private TextView saveTime;
	private TextView mTextViewProdName;

	private View mLayoutBottomTime;
	private int totalTime;
	private int currentTime;
	private int firstJumpTime;

	private AudioManager mAudioManager;
	/** 最大声音 */
	private int mMaxVolume;
	/** 当前声音 */
	private int mVolume = -1;

	private int mVideoWidth = 0;
	private int mVideoHeight = 0;
	private int mPreparedPercent = 0;

	private String PROD_SOURCE = null;

	// private final Runnable mPlayingChecker = new Runnable() {
	// public void run() {
	// if (mVideoView.isPlaying()
	// && mVideoView.getCurrentPosition() >1) {
	// mController.showPlayingAtFirstTime();
	// } else {
	// mHandler.postDelayed(mPlayingChecker, 250);
	// }
	// }
	// };
	// private final Runnable mPreparedProgress = new Runnable() {
	// public void run() {
	// if(mPreparedPercent<100){
	// mPreparedPercent+=1;
	// sb.setProgress(mPreparedPercent);
	// mHandler.postDelayed(mPreparedProgress, 200);
	// }
	// }
	// };
	private final Runnable mProgressChecker = new Runnable() {
		public void run() {
			int pos = setProgress();
			mHandler.postDelayed(mProgressChecker, 1000 - (pos % 1000));
		}
	};

	public MoviePlayer(View rootView, Context movieActivity, int Time,
			int prod_type, Uri videoUri, Bundle savedInstance, boolean canReplay) {
		this.mContext = movieActivity;
		mSeekComplete= false;
		mVideoView = (VideoView) rootView.findViewById(R.id.surface_view);

		mBookmarker = new Bookmarker(mContext);
		// mActionBar = movieActivity.getActionBar();
		mUri = videoUri;
		this.prod_type = prod_type;

		sb = (SeekBar) rootView.findViewById(R.id.seekBar1);
		// sb.setMax(100);
		// mHandler.post(mPreparedProgress);
		mTextViewProdName = (TextView) rootView.findViewById(R.id.textView1);
		textView1 = (TextView) rootView.findViewById(R.id.textViewTime1);
		textView2 = (TextView) rootView.findViewById(R.id.textViewTime2);
		mTextViewTime2 = (TextView) rootView.findViewById(R.id.textViewTimes);
		saveTime = (TextView) rootView.findViewById(R.id.textView7);

		mLayoutBottomTime = (View) rootView.findViewById(R.id.LayoutBottomTime);

		mController = new MovieControllerOverlay(mContext, rootView,mBookmarker);
		((ViewGroup) rootView).addView(mController.getView());
		mController.setListener(this);
		mController.setCanReplay(canReplay);

		mVideoView.setOnErrorListener(this);
		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnPreparedListener(this);

		PROD_SOURCE = mUri.toString();

		mVideoView.setVideoURI(mUri);
		mVideoView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				mController.show();
				return true;
			}
		});

		// When the user touches the screen or uses some hard key, the framework
		// will change system ui visibility from invisible to visible. We show
		// the media control at this point.
		// mVideoView
		// .setOnSystemUiVisibilityChangeListener(new
		// View.OnSystemUiVisibilityChangeListener() {
		// public void onSystemUiVisibilityChange(int visibility) {
		// if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
		// mController.show();
		// }
		// }
		// });

		Intent i = new Intent(SERVICECMD);
		i.putExtra(CMDNAME, CMDPAUSE);
		movieActivity.sendBroadcast(i);

		if (savedInstance != null) { // this is a resumed activity
			mVideoPosition = savedInstance.getInt(KEY_VIDEO_POSITION, 0);
			mResumeableTime = savedInstance.getLong(KEY_RESUMEABLE_TIME,
					Long.MAX_VALUE);
			mVideoView.start();
			mVideoView.suspend();
			mHasPaused = true;
		} else {
			Integer bookmark = 0;
			firstJumpTime = 0;
			if (Time > 0) {
				firstJumpTime = Time;
				bookmark = Time;
			} else
				bookmark = mBookmarker.getBookmark(mUri);
			if (bookmark != null) {
				saveTime.setText(StatisticsUtils.formatDuration(bookmark));
				mVideoView.seekTo(bookmark);
				// showResumeDialog(mContext, bookmark);
			}
			startVideo();
		}
	}

	public void setVideoURI(Uri mUri, int Time) {
		totalTime = 0;
		mVideoView.setVideoURI(mUri);
		PROD_SOURCE = mUri.toString();
		if (Time > 0)
			mVideoView.seekTo(Time);
		// mVideoView.start();
		// mHasPaused = false;
		startVideo();

	}

	public void setAudioManager(AudioManager mAudioManager) {
		this.mAudioManager = mAudioManager;
		mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

		if (mVolume < 0)
			mVolume = 0;
		mMaxVolume = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mController.setAudioManager(mAudioManager);
	}

	private void showSystemUi(boolean visible) {
		int flag = visible ? 0 : View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LOW_PROFILE;
		mVideoView.setSystemUiVisibility(flag);
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putLong(KEY_VIDEO_POSITION, mVideoPosition);
		outState.putLong(KEY_RESUMEABLE_TIME, mResumeableTime);
	}

	private void showResumeDialog(Context context, final int bookmark) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.resume_playing_title);
		builder.setMessage(String.format(
				context.getString(R.string.resume_playing_message),
				StatisticsUtils.formatDuration(bookmark)));
		builder.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				onCompletion();
			}
		});
		builder.setPositiveButton(R.string.resume_playing_resume,
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mVideoView.seekTo(bookmark);
						startVideo();
					}
				});
		builder.setNegativeButton(R.string.resume_playing_restart,
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						startVideo();
					}
				});
		builder.show();
	}

	public void onPause() {
		mHasPaused = true;
		mHandler.removeCallbacksAndMessages(null);
		mVideoPosition = mVideoView.getCurrentPosition();
		mBookmarker.setBookmark(mUri, mVideoPosition, mVideoView.getDuration());
		mVideoView.suspend();
		mResumeableTime = System.currentTimeMillis() + RESUMEABLE_TIMEOUT;
	}

	public void onResume() {
		if (mHasPaused) {
			mVideoView.seekTo(mVideoPosition);
			mVideoView.resume();

			// If we have slept for too long, pause the play
			if (System.currentTimeMillis() > mResumeableTime) {
				pauseVideo();
			}
		}
		// mHandler.post(mProgressChecker);
	}

	public void onDestroy() {
		mVideoView.stopPlayback();
		if (sb != null)
			sb.removeCallbacks(mProgressChecker);
		mController.hide();
	}

	// This updates the time bar display (if necessary). It is called every
	// second by mProgressChecker and also from places where the time bar needs
	// to be updated immediately.
	private int setProgress() {
		if (mDragging) {
			return 0;
		}
		int position = mVideoView.getCurrentPosition();
		int duration = mVideoView.getDuration();

		if (mVideoView.isPlaying() && duration > 1) {
			if(firstJumpTime == 0){
				mController.showPlayingAtFirstTime();
				sb.setMax(duration);
				sb.setOnSeekBarChangeListener(sbLis);
				sb.setProgress(position);
				this.currentTime = position;
				setTime(duration);
				mController.setTimes(position, duration);
				
			}else if (firstJumpTime > 0 && mSeekComplete) {
				firstJumpTime = 0;
				mSeekComplete = false;
				RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				parms.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
						RelativeLayout.TRUE);

				double mLeft = (double) firstJumpTime / totalTime
						* (sb.getMeasuredWidth() - seekBarWidthOffset) + 20;

				if (firstJumpTime > 0)
					parms.leftMargin = (int) mLeft;
				else
					parms.leftMargin = 20;
				parms.bottomMargin = 20 + 10;
				mLayoutBottomTime.setLayoutParams(parms);

				textView1.setText(StatisticsUtils.formatDuration(firstJumpTime));
				textView1.setVisibility(View.VISIBLE);
				// mHandler.removeCallbacks(mPreparedProgress);
				mController.showPlayingAtFirstTime();
				sb.setMax(duration);
				sb.setOnSeekBarChangeListener(sbLis);
				sb.setProgress(firstJumpTime);
				this.currentTime = position;
				setTime(duration);
				mController.setTimes(position, duration);
			}
		}

		return position;
	}

	public int getCurrentPositon() {
		return mVideoPosition;
	}

	public String getCurrentUrl() {
		return PROD_SOURCE;
	}

	public int getDuration() {
		return totalTime;
	}

	public void setTime(int totalTime) {
		if (this.totalTime == totalTime) {
			return;
		}
		this.totalTime = totalTime;
		sb.setMax(totalTime);
		textView2.setText(StatisticsUtils.formatDuration(totalTime));

	}

	private void startVideo() {
		// For streams that we expect to be slow to start up, show a
		// progress spinner until playback starts.
		String scheme = mUri.getScheme();
		if ("http".equalsIgnoreCase(scheme) || "rtsp".equalsIgnoreCase(scheme)) {
			mController.showLoading();
			// mHandler.removeCallbacks(mPlayingChecker);
			// mHandler.postDelayed(mPlayingChecker, 250);
		} else {
			mController.showPlaying();
		}

		mVideoView.start();
		mHasPaused = false;
		// setProgress();
	}

	public void playVideo() {
		mHasPaused = false;
		mVideoView.start();
		mController.showPlaying();

		// setProgress();
	}

	public void pauseVideo() {
		mHasPaused = true;
		mVideoView.pause();
		mController.showPaused();
	}

	public void playTVVideo() {
		mHasPaused = false;
		mVideoView.start();
		mController.showTVPlaying();

		// setProgress();
	}

	public void pauseTVVideo() {
		mHasPaused = true;
		mVideoView.pause();
		mController.showTVPaused();
	}
	public void returnTVVideo() {
		mHasPaused = true;
		mVideoView.pause();
		mController.showTVReturn();
	}
	
	
	// Below are notifications from VideoView
	public boolean onError(MediaPlayer player, int arg1, int arg2) {
		mHandler.removeCallbacksAndMessages(null);
		// VideoView will show an error dialog if we return false, so no need
		// to show more message.
		mController.showErrorMessage("");
		return false;
	}

	public void onCompletion(MediaPlayer mp) {
		mController.showEnded();
		onCompletion();
	}

	public void onCompletion() {
	}

	// Below are notifications from ControllerOverlay
	public void onPlayPause() {
		if (mVideoView.isPlaying()) {
			pauseVideo();
		} else {
			playVideo();
		}
	}

	public void onSeekStart() {
		mDragging = true;
	}

	public void onSeekMove(int time) {
		mVideoView.seekTo(time);
	}

	public void onSeekEnd(int time) {
		mDragging = false;
		mVideoView.seekTo(time);
		// setProgress();
	}

	public void onShown() {
		mShowing = true;
		// mActionBar.show();
		showSystemUi(true);
		// setProgress();
	}

	public void onHidden() {
		mShowing = false;
		// mActionBar.hide();
		showSystemUi(false);
	}

	public void onReplay() {
		startVideo();
	}

	public boolean isPause() {
		return mHasPaused;
	}

	// Below are key events passed from MovieActivity.
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// Some headsets will fire off 7-10 events on a single click
		if (event.getRepeatCount() > 0) {
			return isMediaKey(keyCode);
		}
		if(totalTime <=0 ){//
			if(keyCode == KeyEvent.KEYCODE_BACK)
				return false;
			else
				return true;
		}

		// Toast.makeText(mContext, Integer.toString(keyCode),100).show();
		if (JUMP_TIME_TIMES != 0 && !isFastForwardKey(keyCode)) // 快进模式才能按的键
			return true;

		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_RIGHT:
//			if(!mController.isHidden())
//				return false;
			if (mHasPaused == false)
				OnMediaFastForward();
			else {
				if (CURRENT_KEY == 3) {
					mController.focusLayoutControl(0);
					CURRENT_KEY = 0;
				} else {
					mController.focusLayoutControl(1);
					CURRENT_KEY = 1;
				}
			}
			return true;
		case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
//			OnMediaFastForward();
//			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if(!mController.isHidden())
				return false;
			if (mHasPaused == false)
				OnMediaRewind();
			else {
				if (CURRENT_KEY == 1) {
					mController.focusLayoutControl(0);
					CURRENT_KEY = 0;
				} else {
					mController.focusLayoutControl(3);
					CURRENT_KEY = 3;
				}
			}
			return true;
		case KeyEvent.KEYCODE_MEDIA_REWIND:
			OnMediaRewind();
			return true;
		case KeyEvent.KEYCODE_DPAD_UP:
			if (mHasPaused == false)
				OnVolumeUp();
			else {
				if (CURRENT_KEY == 2) {
					mController.focusLayoutControl(0);
					CURRENT_KEY = 0;
				} else {
					mController.focusLayoutControl(4);
					CURRENT_KEY = 4;
				}
			}
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if (mHasPaused == false)
				OnVolumeDown();
			else {
				if (CURRENT_KEY == 4) {
					mController.focusLayoutControl(0);
					CURRENT_KEY = 0;
				} else {
					mController.focusLayoutControl(2);
					CURRENT_KEY = 2;
				}

			}
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			OnVolumeUp();
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			OnVolumeDown();
			return true;
		case KeyEvent.KEYCODE_VOLUME_MUTE:
			mController.showVolume(0);
			return true;
		case KeyEvent.KEYCODE_HEADSETHOOK:
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			if (mController.isHidden()) {
				mController.show();
			}
			if (mVideoView.isPlaying()) {
				pauseVideo();
			} else {
				playVideo();
			}
			return true;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			if (JUMP_TIME_TIMES != 0) {// 快进模式
				mDragging = false;
				if (JUMP_TIME != totalTime)
					mVideoView.seekTo(JUMP_TIME);
				JUMP_TIME = 0;
				JUMP_TIME_TIMES = 0;
				mHandler.removeCallbacks(mMediaFastForwardRunnable);
				mController.hideTimerBar();
				mController.HidingTimes();
			} else {
				if (mController.isHidden()) {
					mController.hideVolume();
					mController.show();
				}
				if (prod_type == 1){
					if (mVideoView.isPlaying()) {	
						pauseVideo();	
						mController.focusLayoutControl(0);
						CURRENT_KEY = 0;
					} else {
						playVideo();
					}
				}else{
					if (mVideoView.isPlaying()) {	
						pauseTVVideo();	
						mController.focusLayoutControl(0);
						CURRENT_KEY = 0;
					} else {
						playTVVideo();
					}
				}
				

			}

			return true;
		case KeyEvent.KEYCODE_MEDIA_PAUSE:
			if (JUMP_TIME_TIMES != 0) {// 快进模式
				mDragging = false;
				mVideoView.seekTo(JUMP_TIME);
				JUMP_TIME = 0;
				JUMP_TIME_TIMES = 0;
				mHandler.removeCallbacks(mMediaFastForwardRunnable);
				mController.hideTimerBar();
				mController.HidingTimes();
			} else {
				if (mController.isHidden()) {
					mController.hideVolume();
					mController.show();
				}
				if (mVideoView.isPlaying()) {
					pauseVideo();
					mController.focusLayoutControl(0);
					CURRENT_KEY = 0;
				}
			}
			return true;
		case KeyEvent.KEYCODE_MEDIA_PLAY:
			if (JUMP_TIME_TIMES != 0) {// 快进模式
				mDragging = false;
				mVideoView.seekTo(JUMP_TIME);
				JUMP_TIME = 0;
				JUMP_TIME_TIMES = 0;
				mHandler.removeCallbacks(mMediaFastForwardRunnable);
				mController.hideTimerBar();
				mController.HidingTimes();
			} else {
				if (mController.isHidden()) {
					mController.hideVolume();
					mController.show();
				}
				if (!mVideoView.isPlaying()) {
					playVideo();
				}

			}
			return true;
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
		case KeyEvent.KEYCODE_MEDIA_NEXT:
			if (mController.isHidden()) {
				mController.show();
			}
			// TODO: Handle next / previous accordingly, for now we're
			// just consuming the events.
			return true;
		case KeyEvent.KEYCODE_BACK:
			currentKeyEvent = KeyEvent.KEYCODE_BACK;
			if (JUMP_TIME_TIMES != 0) {// 快进模式
				mDragging = false;
				JUMP_TIME = 0;
				JUMP_TIME_TIMES = 0;
				mHandler.removeCallbacks(mMediaFastForwardRunnable);
				mController.hideTimerBar();
				mController.HidingTimes();
				return true;
			} else if (prod_type != 1) {
				// 没有加载完就返回，bug,加到前面去了。
//				if (totalTime <= 0)
//					return false;
				RETURNMODE = true;
//				if (mVideoView.isPlaying()) {
					returnTVVideo();
					mController.focusLayoutControl(0);
					CURRENT_KEY = 0;
//				} 
//				else {
//					playVideo();
//				}
				
				return true;
			}

		}
		return false;
	}

	public int getCurrentKeyEvent() {
		return currentKeyEvent;
	}
	public boolean getCurrentReturnMode(){
		return RETURNMODE;
	}
	public void exitReturnMode(){
		if(RETURNMODE){
			RETURNMODE = false;
			mController.TVControlViewGone(false);//一会消失
		}
	}
	private void OnMediaRewind() {
		if (JUMP_TIME_TIMES > 1)
			JUMP_TIME_TIMES = 1;
		else if (JUMP_TIME_TIMES - 1 < -3)
			return;
		else if (JUMP_TIME_TIMES == 1)
			JUMP_TIME_TIMES = -1;
		else
			JUMP_TIME_TIMES--;

		mDragging = true;
		if (!mShowing) {
			mController.showTimerBar();
		}
		if (JUMP_TIME == 0)
			JUMP_TIME = mVideoView.getCurrentPosition();
		// else
		// JUMP_TIME = JUMP_TIME - 10000;
		if (JUMP_TIME_TIMES != 0) {
			mTextViewTime2.setText("×"
					+ Integer.toString(Math.abs(JUMP_TIME_TIMES)));
			mController.ShowTimes();
		} else {
			mController.HidingTimes();
		}

		mHandler.removeCallbacks(mMediaFastForwardRunnable);
		mHandler.postDelayed(mMediaFastForwardRunnable, 1000);
		System.out.println("Play back");
	}

	private void OnMediaFastForward() {
		if (JUMP_TIME_TIMES < -1)
			JUMP_TIME_TIMES = -1;
		else if (JUMP_TIME_TIMES + 1 > 3)
			return;
		else if (JUMP_TIME_TIMES == -1)
			JUMP_TIME_TIMES = 1;
		else
			JUMP_TIME_TIMES++;

		mDragging = true;
		if (!mShowing) {
			mController.showTimerBar();
		}
		if (JUMP_TIME == 0)
			JUMP_TIME = mVideoView.getCurrentPosition();
		// else
		// JUMP_TIME = JUMP_TIME + 10000;
		if (JUMP_TIME_TIMES != 0) {
			mTextViewTime2.setText("×"
					+ Integer.toString(Math.abs(JUMP_TIME_TIMES)));
			mController.ShowTimes();
		} else {
			mController.HidingTimes();
		}

		mHandler.removeCallbacks(mMediaFastForwardRunnable);
		mHandler.postDelayed(mMediaFastForwardRunnable, 1000);
		System.out.println("Play forward");
	}

	private final Runnable mMediaFastForwardRunnable = new Runnable() {
		int[] mTimes = { 1000, 333, 55 };

		public void run() {

			if (JUMP_TIME_TIMES < 0) { // 快退模式
				if (JUMP_TIME - 10000 < 0)
					JUMP_TIME = 0;
				else
					JUMP_TIME = JUMP_TIME
							- 10000;
			} else if (JUMP_TIME_TIMES > 0) {// 快进模式
				if (JUMP_TIME + 10000 >= totalTime)
					JUMP_TIME = totalTime;
				else
					JUMP_TIME = JUMP_TIME + 10000;
			}
			RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			parms.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
					RelativeLayout.TRUE);

			double mLeft = (double) JUMP_TIME / totalTime
					* (sb.getMeasuredWidth() - seekBarWidthOffset) + 20;
			if (totalTime > 0)
				parms.leftMargin = (int) mLeft;
			else
				parms.leftMargin = 20;
			parms.bottomMargin = 20 + 10;

			mLayoutBottomTime.setLayoutParams(parms);

			textView1.setText(StatisticsUtils.formatDuration(JUMP_TIME));
			textView1.setVisibility(View.VISIBLE);

			sb.setMax(totalTime);
			sb.setProgress(JUMP_TIME);

			// Fun_downloadrate();
			mHandler.postDelayed(mMediaFastForwardRunnable, mTimes[Math.abs(JUMP_TIME_TIMES)-1]);
		}
	};

	private void OnVolumeDown() {
		mVolume--;
		if (mVolume < 0)
			mVolume = 0;
		mController.showVolume(mVolume);
	}

	private void OnVolumeUp() {
		mVolume++;
		if (mVolume > mMaxVolume)
			mVolume = mMaxVolume;
		mController.showVolume(mVolume);
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_MEDIA_REWIND:
			return true;
		}
		return isMediaKey(keyCode);
	}

	private static boolean isMediaKey(int keyCode) {
		return keyCode == KeyEvent.KEYCODE_HEADSETHOOK
				|| keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS
				|| keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
				|| keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
				|| keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
				|| keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
				|| keyCode == KeyEvent.KEYCODE_MEDIA_REWIND
				|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE
				|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER
				|| keyCode == KeyEvent.KEYCODE_ENTER;
	}

	private static boolean isFastForwardKey(int keyCode) {
		return keyCode == KeyEvent.KEYCODE_ENTER
				|| keyCode == KeyEvent.KEYCODE_ENTER
				|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
				|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN
				|| keyCode == KeyEvent.KEYCODE_DPAD_UP;
	}

	// VOLUME
	private static boolean isVolumeKey(int keyCode) {
		return keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
				|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
				|| keyCode == KeyEvent.KEYCODE_DPAD_UP
				|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN;

	}

	private OnSeekBarChangeListener sbLis = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// if (mVideoView.isPlaying() && mVideoView.getCurrentPosition() >
			// 1){
			// mHandler.removeCallbacks(mPreparedProgress);
			// mController.showPlayingAtFirstTime();
			// }

			if (JUMP_TIME_TIMES == 0) {
				RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				parms.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
						RelativeLayout.TRUE);

				double mLeft = (double) progress / totalTime
						* (sb.getMeasuredWidth() - seekBarWidthOffset) + 20;

				if (progress > 0)
					parms.leftMargin = (int) mLeft;
				else
					parms.leftMargin = 20;
				parms.bottomMargin = 20 + 10;
				mLayoutBottomTime.setLayoutParams(parms);

				textView1.setText(StatisticsUtils.formatDuration(progress));
				textView1.setVisibility(View.VISIBLE);
				if (totalTime > 0 && totalTime - progress <= 5000
						&& (prod_type == 2 || prod_type == 3)) {

					OnContinueVideoPlay();
				}
			}
			// TODO Auto-generated method stub
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onStartTrackingTouch");
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			// mMediaPlayer.seekTo(sb.getProgress());
			mVideoView.seekTo(sb.getProgress());
			// SeekBar确定位置后，跳到指定位置
		}

	};

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onPrepared");
		mVideoWidth = mp.getVideoWidth();
		mVideoHeight = mp.getVideoHeight();
		// if(firstJumpTime < 1 ){
		mController.setPrepared(true);
		// sb.setProgress(100);
		mSeekComplete= true; 
		mHandler.postDelayed(mProgressChecker, 2000);
//		mHandler.post(mProgressChecker);
		
		// }
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onBufferingUpdate:   " + Integer.toString(percent));
		// sb.setMax(100);
		// sb.setProgress(percent);
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onVideoSizeChanged, width:" + Integer.toString(width)
				+ "height: " + Integer.toString(height));
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onSeekComplete");
		mSeekComplete= true; 
		// if(firstJumpTime > 0 ){
		// mController.setPrepared(true);
		// sb.setProgress(100);
		// mHandler.post(mProgressChecker);
		// }
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		if (mp != null) {
			switch (what) {
			case MediaPlayer.MEDIA_INFO_UNKNOWN:
				Log.d(TAG, "MEDIA_INFO_UNKNOWN");
				break;
			case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
				Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING");
				break;
			case MediaPlayer.MEDIA_INFO_BUFFERING_START:
				Log.d(TAG, "MEDIA_INFO_BUFFERING_START");
				break;
			case MediaPlayer.MEDIA_INFO_BUFFERING_END:
				Log.d(TAG, "MEDIA_INFO_BUFFERING_END");
				break;
			case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
				Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING");
				break;
			case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
				Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE");
				break;
			case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
				Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE");
				break;
			}

		}
		return true;
	}

	public void OnPreVideoPlay() {
		PROD_SOURCE = null;
		App app = (App) this.mContext.getApplicationContext();
		CurrentPlayData mCurrentPlayData = app.getCurrentPlayData();
		ReturnProgramView m_ReturnProgramView = app.get_ReturnProgramView();
		if (mCurrentPlayData != null && m_ReturnProgramView != null) {
			int index = mCurrentPlayData.CurrentIndex - 1;
			mCurrentPlayData.CurrentIndex -= 1;
			app.setCurrentPlayData(mCurrentPlayData);

			String title = null;

			switch (mCurrentPlayData.prod_type) {
			case 1:
				break;
			case 131:
			case 2:
				if (m_ReturnProgramView.tv.episodes[index].down_urls != null) {
					// videoSourceSort(m_ReturnProgramView.tv.episodes[index].down_urls);
					for (int i = 0; i < m_ReturnProgramView.tv.episodes[index].down_urls.length; i++) {

						for (int j = 0; j < Constant.video_index.length; j++) {
							if (PROD_SOURCE == null
									&& m_ReturnProgramView.tv.episodes[index].down_urls[i].source
											.trim().equalsIgnoreCase(
													Constant.video_index[j])) {

								String name = m_ReturnProgramView.tv.name;
								title = "第"
										+ m_ReturnProgramView.tv.episodes[index].name
										+ "集";
								mTextViewProdName.setText(name + title);
								PROD_SOURCE = GetSource(m_ReturnProgramView,
										mCurrentPlayData.prod_type, index, i);

								// yangzhg
								StatisticsUtils
										.StatisticsClicksShow(
												new AQuery(mContext),
												app,
												m_ReturnProgramView.tv.id,
												m_ReturnProgramView.tv.name,
												m_ReturnProgramView.tv.episodes[index].name,
												mCurrentPlayData.prod_type);

								break;
							}
						}
					}
				}
				break;
			case 3:
				if (m_ReturnProgramView.show.episodes[index].down_urls != null) {
					// videoSourceSort(m_ReturnProgramView.show.episodes[index].down_urls);
					for (int i = 0; i < m_ReturnProgramView.show.episodes[index].down_urls.length; i++) {
						for (int j = 0; j < Constant.video_index.length; j++) {

							if (PROD_SOURCE == null
									&& m_ReturnProgramView.show.episodes[index].down_urls[i].source
											.trim().equalsIgnoreCase(
													Constant.video_index[j])) {

								String name = m_ReturnProgramView.show.name;
								title = m_ReturnProgramView.show.episodes[index].name;

								mTextViewProdName.setText(name + title);
								PROD_SOURCE = GetSource(m_ReturnProgramView,
										mCurrentPlayData.prod_type, index, i);

								// yangzhg
								StatisticsUtils
										.StatisticsClicksShow(
												new AQuery(mContext),
												app,
												m_ReturnProgramView.show.id,
												m_ReturnProgramView.show.name,
												m_ReturnProgramView.show.episodes[index].name,
												3);

								break;
							}
						}
					}

				}
				break;
			}

			// ShowQuality();

			if (PROD_SOURCE != null)
				setVideoURI(Uri.parse(PROD_SOURCE), 0);
		}
	}

	public void OnContinueVideoPlay() {
		PROD_SOURCE = null;
		App app = (App) this.mContext.getApplicationContext();
		CurrentPlayData mCurrentPlayData = app.getCurrentPlayData();
		ReturnProgramView m_ReturnProgramView = app.get_ReturnProgramView();
		if (mCurrentPlayData != null && m_ReturnProgramView != null) {
			int index = mCurrentPlayData.CurrentIndex + 1;
			mCurrentPlayData.CurrentIndex += 1;
			app.setCurrentPlayData(mCurrentPlayData);

			String title = null;

			switch (mCurrentPlayData.prod_type) {
			case 1:
				break;
			case 131:
			case 2:
				if (m_ReturnProgramView.tv.episodes[index].down_urls != null) {
					// videoSourceSort(m_ReturnProgramView.tv.episodes[index].down_urls);
					for (int i = 0; i < m_ReturnProgramView.tv.episodes[index].down_urls.length; i++) {

						for (int j = 0; j < Constant.video_index.length; j++) {
							if (PROD_SOURCE == null
									&& m_ReturnProgramView.tv.episodes[index].down_urls[i].source
											.trim().equalsIgnoreCase(
													Constant.video_index[j])) {

								String name = m_ReturnProgramView.tv.name;
								title = "第"
										+ m_ReturnProgramView.tv.episodes[index].name
										+ "集";
								mTextViewProdName.setText(name + title);
								PROD_SOURCE = GetSource(m_ReturnProgramView,
										mCurrentPlayData.prod_type, index, i);

								// yangzhg
								StatisticsUtils
										.StatisticsClicksShow(
												new AQuery(mContext),
												app,
												m_ReturnProgramView.tv.id,
												m_ReturnProgramView.tv.name,
												m_ReturnProgramView.tv.episodes[index].name,
												mCurrentPlayData.prod_type);

								break;
							}
						}
					}
				}
				break;
			case 3:
				if (m_ReturnProgramView.show.episodes[index].down_urls != null) {
					// videoSourceSort(m_ReturnProgramView.show.episodes[index].down_urls);
					for (int i = 0; i < m_ReturnProgramView.show.episodes[index].down_urls.length; i++) {
						for (int j = 0; j < Constant.video_index.length; j++) {

							if (PROD_SOURCE == null
									&& m_ReturnProgramView.show.episodes[index].down_urls[i].source
											.trim().equalsIgnoreCase(
													Constant.video_index[j])) {

								String name = m_ReturnProgramView.show.name;
								title = m_ReturnProgramView.show.episodes[index].name;

								mTextViewProdName.setText(name + title);
								PROD_SOURCE = GetSource(m_ReturnProgramView,
										mCurrentPlayData.prod_type, index, i);

								// yangzhg
								StatisticsUtils
										.StatisticsClicksShow(
												new AQuery(mContext),
												app,
												m_ReturnProgramView.show.id,
												m_ReturnProgramView.show.name,
												m_ReturnProgramView.show.episodes[index].name,
												3);

								break;
							}
						}
					}

				}
				break;
			}

			// ShowQuality();

			if (PROD_SOURCE != null)
				setVideoURI(Uri.parse(PROD_SOURCE), 0);
		}
	}

	private boolean CheckUrl(String srcUrl) {

		// url本身不正常 直接返回
		if (srcUrl == null || srcUrl.length() <= 0) {

			return false;
		} else {

			if (!URLUtil.isValidUrl(srcUrl)) {

				return false;
			}
		}
		return true;
	}

	private String GetSource(ReturnProgramView m_ReturnProgramView,
			int CurrentCategory, int proi_index, int sourceIndex) {
		switch (CurrentCategory) {
		case 1:
			break;
		case 131:
		case 2:
			for (int k = 0; k < m_ReturnProgramView.tv.episodes[proi_index].down_urls[sourceIndex].urls.length; k++) {
				ReturnProgramView.DOWN_URLS.URLS CurrentURLS = m_ReturnProgramView.tv.episodes[proi_index].down_urls[sourceIndex].urls[k];
				if (CurrentURLS != null && CurrentURLS.url != null
						&& CheckUrl(CurrentURLS.url.trim())) {
					for (int i = 0; i < Constant.quality_index.length; i++) {
						if (PROD_SOURCE == null
								&& CurrentURLS.type.trim().equalsIgnoreCase(
										Constant.quality_index[i])) {
							PROD_SOURCE = CurrentURLS.url.trim();
							break;
						}
					}
				}
				if (PROD_SOURCE != null)
					break;
			}
			break;
		case 3:
			for (int k = 0; k < m_ReturnProgramView.show.episodes[proi_index].down_urls[sourceIndex].urls.length; k++) {
				ReturnProgramView.DOWN_URLS.URLS CurrentURLS = m_ReturnProgramView.show.episodes[proi_index].down_urls[sourceIndex].urls[k];
				if (CurrentURLS != null && CurrentURLS.url != null
						&& CheckUrl(CurrentURLS.url.trim())) {
					for (int i = 0; i < Constant.quality_index.length; i++) {
						if (PROD_SOURCE == null
								&& CurrentURLS.type.trim().equalsIgnoreCase(
										Constant.quality_index[i])) {
							PROD_SOURCE = CurrentURLS.url.trim();
							break;
						}
					}
				}
				if (PROD_SOURCE != null)
					break;
			}
			break;

		}

		return PROD_SOURCE;

	}

}

class Bookmarker {
	private static final String TAG = "Bookmarker";

	private static final String BOOKMARK_CACHE_FILE = "bookmark";
	private static final int BOOKMARK_CACHE_MAX_ENTRIES = 100;
	private static final int BOOKMARK_CACHE_MAX_BYTES = 10 * 1024;
	private static final int BOOKMARK_CACHE_VERSION = 1;

	private static final int HALF_MINUTE = 30 * 1000;
	private static final int TWO_MINUTES = 4 * HALF_MINUTE;

	private final Context mContext;

	public Bookmarker(Context context) {
		mContext = context;
	}

	public void setBookmark(Uri uri, int bookmark, int duration) {
		try {
			BlobCache cache = CacheManager.getCache(mContext,
					BOOKMARK_CACHE_FILE, BOOKMARK_CACHE_MAX_ENTRIES,
					BOOKMARK_CACHE_MAX_BYTES, BOOKMARK_CACHE_VERSION);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeUTF(uri.toString());
			dos.writeInt(bookmark);
			dos.writeInt(duration);
			dos.flush();
			cache.insert(uri.hashCode(), bos.toByteArray());
		} catch (Throwable t) {
			Log.w(TAG, "setBookmark failed", t);
		}
	}

	public Integer getBookmark(Uri uri) {
		try {
			@SuppressWarnings("deprecation")
			BlobCache cache = CacheManager.getCache(mContext,
					BOOKMARK_CACHE_FILE, BOOKMARK_CACHE_MAX_ENTRIES,
					BOOKMARK_CACHE_MAX_BYTES, BOOKMARK_CACHE_VERSION);

			byte[] data = cache.lookup(uri.hashCode());
			if (data == null)
				return null;

			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
					data));

			String uriString = dis.readUTF(dis);
			int bookmark = dis.readInt();
			int duration = dis.readInt();

			if (!uriString.equals(uri.toString())) {
				return null;
			}

			if ((bookmark < HALF_MINUTE) || (duration < TWO_MINUTES)
					|| (bookmark > (duration - HALF_MINUTE))) {
				return null;
			}
			return Integer.valueOf(bookmark);
		} catch (Throwable t) {
			Log.w(TAG, "getBookmark failed", t);
		}
		return null;
	}

}
