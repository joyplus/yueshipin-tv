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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.joyplus.tv.R;

public class MoviePlayer implements MediaPlayer.OnErrorListener,
		MediaPlayer.OnCompletionListener, 
		MediaPlayer.OnPreparedListener ,
		MediaPlayer.OnBufferingUpdateListener,
		MediaPlayer.OnInfoListener,
		MediaPlayer.OnSeekCompleteListener,
		MediaPlayer.OnVideoSizeChangedListener,
		ControllerOverlay.Listener {
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
	
	private Context mContext;
	private final VideoView mVideoView;
	private final Bookmarker mBookmarker;
	private final Uri mUri;
	private Handler mHandler = new Handler();
//	private final ActionBar mActionBar;
	private ControllerOverlay mController;

	private long mResumeableTime = Long.MAX_VALUE;
	private int mVideoPosition = 0;
	private boolean mHasPaused = false;

	// If the time bar is being dragged.
	private boolean mDragging;

	// If the time bar is visible.
	private boolean mShowing;
	
	private SeekBar sb;
	private TextView textView1;
	private TextView textView2;
	private View mLayoutBottomTime;
	private int totalTime;
	private int currentTime;
	
	private AudioManager mAudioManager;
	/** 最大声音 */
	private int mMaxVolume;
	/** 当前声音 */
	private int mVolume = -1;
	
	private final Runnable mPlayingChecker = new Runnable() {
		public void run() {
			if (mVideoView.isPlaying()) {
				mController.showPlayingAtFirstTime();
			} else {
				mHandler.postDelayed(mPlayingChecker, 250);
			}
		}
	};

	private final Runnable mProgressChecker = new Runnable() {
		public void run() {
			int pos = setProgress();
			mHandler.postDelayed(mProgressChecker, 1000 - (pos % 1000));
		}
	};

	public MoviePlayer(View rootView,Context movieActivity,
			Uri videoUri, Bundle savedInstance, boolean canReplay) {
		this.mContext = movieActivity;
		mVideoView = (VideoView) rootView.findViewById(R.id.surface_view);

		mBookmarker = new Bookmarker(mContext);
//		mActionBar = movieActivity.getActionBar();
		mUri = videoUri;
		
		sb = (SeekBar) rootView.findViewById(R.id.seekBar1);
		sb.setOnSeekBarChangeListener(sbLis);
		
		textView1 = (TextView) rootView.findViewById(R.id.textViewTime1);
		textView2 = (TextView) rootView.findViewById(R.id.textViewTime2);
		
		mLayoutBottomTime = (View) rootView.findViewById(R.id.LayoutBottomTime);
		

		mController = new MovieControllerOverlay(mContext,rootView);
		((ViewGroup) rootView).addView(mController.getView());
		mController.setListener(this);
		mController.setCanReplay(canReplay);

		mVideoView.setOnErrorListener(this);
		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnPreparedListener(this);
		
		
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
		mVideoView
				.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
					public void onSystemUiVisibilityChange(int visibility) {
						if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
							mController.show();
						}
					}
				});

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
			final Integer bookmark = mBookmarker.getBookmark(mUri);
			if (bookmark != null) {
				showResumeDialog(mContext, bookmark);
			} else {
				startVideo();
			}
		}
	}
	public void setAudioManager(AudioManager mAudioManager){
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
		outState.putInt(KEY_VIDEO_POSITION, mVideoPosition);
		outState.putLong(KEY_RESUMEABLE_TIME, mResumeableTime);
	}

	private void showResumeDialog(Context context, final int bookmark) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.resume_playing_title);
        builder.setMessage(String.format(
                context.getString(R.string.resume_playing_message),
               formatDuration(bookmark / 1000)));
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
		mHandler.post(mProgressChecker);
	}

	public void onDestroy() {
		mVideoView.stopPlayback();
		if(sb != null)
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
	
		RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		parms.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
				RelativeLayout.TRUE);
		if (duration > 0) 
			parms.leftMargin = position * (sb.getWidth() - 4) / duration + 20;
		else 
			parms.leftMargin = 20;
		parms.bottomMargin = 20 + 8;
		mLayoutBottomTime.setLayoutParams(parms);

		textView1.setText(formatDuration(position));
		textView1.setVisibility(View.VISIBLE);
		sb.setProgress(position);
		this.currentTime = position;
		setTime(duration);

		mController.setTimes(position, duration);
		return position;
	}
	public void setTime(int totalTime) {
		if ( this.totalTime == totalTime) {
			return;
		}
		this.totalTime = totalTime;
		sb.setMax(totalTime);
		textView2.setText(formatDuration(totalTime));
		

	}
	private String formatDuration(int duration) {
		duration = duration / 1000;
		int h = duration / 3600;
		int m = (duration - h * 3600) / 60;
		int s = duration - (h * 3600 + m * 60);
		String durationValue;
		if (h == 0) {
			durationValue = String.format("%1$02d:%2$02d", m, s);
		} else {
			durationValue = String.format("%1$d:%2$02d:%3$02d", h, m, s);
		}
		return durationValue;
	}
	private void startVideo() {
		// For streams that we expect to be slow to start up, show a
		// progress spinner until playback starts.
		String scheme = mUri.getScheme();
		if ("http".equalsIgnoreCase(scheme) || "rtsp".equalsIgnoreCase(scheme)) {
			mController.showLoading();
			mHandler.removeCallbacks(mPlayingChecker);
			mHandler.postDelayed(mPlayingChecker, 250);
		} else {
			mController.showPlaying();
		}

		mVideoView.start();
		
		setProgress();
	}

	private void playVideo() {
		mVideoView.start();
		mController.showPlaying();
		
		setProgress();
	}

	private void pauseVideo() {
		mVideoView.pause();
		mController.showPaused();
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
		setProgress();
	}

	public void onShown() {
		mShowing = true;
//		mActionBar.show();
		showSystemUi(true);
		setProgress();
	}

	public void onHidden() {
		mShowing = false;
//		mActionBar.hide();
		showSystemUi(false);
	}

	public void onReplay() {
		startVideo();
	}

	// Below are key events passed from MovieActivity.
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// Some headsets will fire off 7-10 events on a single click
		if (event.getRepeatCount() > 0) {
			return isMediaKey(keyCode);
		}

		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
				if(!mShowing){
					mController.showTimerBar(); 
//					onShown();
				}
				if(JUMP_TIME == 0)
					JUMP_TIME = mVideoView.getCurrentPosition() + 10000;
				else 
					JUMP_TIME = JUMP_TIME + 10000;
				System.out.println("Play forward");  
				
				return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_MEDIA_REWIND:
				if(!mShowing){
					mController.showTimerBar(); 
//					onShown();
				}
				if(JUMP_TIME == 0)
					JUMP_TIME = mVideoView.getCurrentPosition() - 10000;
				else 
					JUMP_TIME = JUMP_TIME - 10000;
				
	            System.out.println("Play back");  
				return true;
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_VOLUME_UP:
			mVolume++;
			if (mVolume > mMaxVolume)
				mVolume = mMaxVolume;
			mController.showVolume(mVolume);
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			mVolume--;
			 if (mVolume < 0)
				 mVolume = 0;
			mController.showVolume(mVolume);
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
		case KeyEvent.KEYCODE_ENTER:
			if (mController.isHidden()) {
				mController.show();
			}
			if (mVideoView.isPlaying()) {
				pauseVideo();
			}else {
				playVideo();
			}
			return true;
		case KeyEvent.KEYCODE_MEDIA_PAUSE:
			if (mController.isHidden()) {
				mController.show();
			}
			if (mVideoView.isPlaying()) {
				pauseVideo();
			}
			return true;
		case KeyEvent.KEYCODE_MEDIA_PLAY:
			if (mController.isHidden()) {
				mController.show();
			}
			if (!mVideoView.isPlaying()) {
				playVideo();
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
		
		}
		return false;
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_MEDIA_REWIND:
				mVideoView.seekTo(JUMP_TIME);
				JUMP_TIME =0;
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
				|| keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE;
	}
	//VOLUME
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
			// TODO Auto-generated method stub
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
//			mMediaPlayer.seekTo(sb.getProgress());
			mVideoView.seekTo(sb.getProgress());
			// SeekBar确定位置后，跳到指定位置
		}

	};

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		sb.setMax(100);
		sb.setProgress(percent);
	}
	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
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