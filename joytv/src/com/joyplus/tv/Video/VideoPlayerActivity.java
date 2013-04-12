/*
 * Copyright (C) 2007 The Android Open Source Project
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

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.VideoColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.joyplus.tv.App;
import com.joyplus.tv.Main;
import com.joyplus.tv.R;
import com.joyplus.tv.Adapters.CurrentPlayData;
import com.joyplus.tv.Service.Return.ReturnProgramView;

/**
 * This activity plays a video from a specified URI.
 */
public class VideoPlayerActivity extends Activity {
	@SuppressWarnings("unused")
	private static final String TAG = "MovieActivity";

	private MoviePlayer mPlayer;
	private boolean mFinishOnCompletion;
	private Uri mUri;
	private App app;
	private String prod_id = null;
	private String prod_name = null;
	private String prod_url = null;//播放地址
	private String prod_src = null;//来源
	private int prod_qua = 0;//清晰度 1080p 或720p
	private CurrentPlayData mCurrentPlayData = null;
	private ReturnProgramView m_ReturnProgramView = null;
	private AudioManager mAudioManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (App) getApplication();
		// requestWindowFeature(Window.FEATURE_ACTION_BAR);
		// requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		setContentView(R.layout.video_player);
		View rootView = findViewById(R.id.root);

		Intent intent = getIntent();
		prod_name = intent.getStringExtra("title");
		prod_url = intent.getStringExtra("prod_url");
		prod_name = "越来越好之村晚";
//		prod_url = "http://221.130.179.66/25/36/53/kingsoft/movie/47978987920B0079FF686B6370B4E039-xiyoupian.mp4?crypt=61740d1aa7f2e300&b=800&gn=132&nc=1&bf=30&p2p=1&video_type=mp4&check=0&tm=1364191200&key=af7b9ad0697560c682a0070cf225e65e&opck=1&lgn=letv&proxy=3702889363&cipi=2026698610&tsnp=1&tag=ios&tag=kingsoft&sign=coopdown&realext=.mp4test=m3u8";
//		
		prod_url ="http://g3.letv.cn/vod/v2/MjUvNDgvOTEvbGV0di11dHMvMjM0OTQzOC1BVkMtOTU4ODczLUFBQy0xMjc3MjQtNjA4MDk2MC04MjkxMTA0MzYtNjI1NjNmMTQxNDMxNGFkODY3ZGRjMGNhMTFkNjIxNjgtMTM2NDkxMjMxOTkxMC5tcDQ=?b=1090&mmsid=2341519&tm=1365557814&platid=1&splatid=2&key=75e40c4fc8927605afe8456dc0f9b207&m3u8=ios,2341519?_r0.3129199950490147&test=m3u8";
		
		mCurrentPlayData = app.getCurrentPlayData();
		m_ReturnProgramView = app.get_ReturnProgramView();
		if(mCurrentPlayData != null && m_ReturnProgramView != null){
			prod_id= mCurrentPlayData.prod_id;
			prod_name = mCurrentPlayData.prod_name;
			prod_url = mCurrentPlayData.prod_url;
			prod_src = mCurrentPlayData.prod_src;
			prod_qua = mCurrentPlayData.prod_qua;
		}
		((TextView) findViewById(R.id.textView1)).setText(prod_name);
		mFinishOnCompletion = intent.getBooleanExtra(
				MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
		mPlayer = new MoviePlayer(rootView, this, Uri.parse(prod_url),
				savedInstanceState, !mFinishOnCompletion) {
			@Override
			public void onCompletion() {
				if (mFinishOnCompletion) {
					finish();
				}
			}
		};
		if (intent.hasExtra(MediaStore.EXTRA_SCREEN_ORIENTATION)) {
			int orientation = intent.getIntExtra(
					MediaStore.EXTRA_SCREEN_ORIENTATION,
					ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			if (orientation != getRequestedOrientation()) {
				setRequestedOrientation(orientation);
			}
		}
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		winParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
		// winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		win.setAttributes(winParams);

	}

	public void OnClickPause(View v) {
		if(mPlayer.isPause())
			mPlayer.playVideo();
		else
			mPlayer.pauseVideo();
	}
	public void OnClickPre(View v) {

	}
	public void OnClickContinue(View v) {

	}
	public void OnClickNext(View v) {

	}
	public void OnClickFav(View v) {

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		/*
		 * getMenuInflater().inflate(R.menu.movie, menu); ShareActionProvider
		 * provider = GalleryActionBar .initializeShareActionProvider(menu);
		 * 
		 * if (provider != null) { Intent intent = new
		 * Intent(Intent.ACTION_SEND); intent.setType("video/*");
		 * intent.putExtra(Intent.EXTRA_STREAM, mUri);
		 * provider.setShareIntent(intent); }
		 */
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return false;
	}

	@Override
	public void onStart() {
//		((AudioManager) getSystemService(AUDIO_SERVICE)).requestAudioFocus(
//				null, AudioManager.STREAM_MUSIC,
//				AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mAudioManager.requestAudioFocus(
				null, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		mPlayer.setAudioManager(mAudioManager);
		
		super.onStart();
	}

	@Override
	protected void onStop() {
		mAudioManager.abandonAudioFocus(null);
		super.onStop();
	}

	@Override
	public void onPause() {
		mPlayer.onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		mPlayer.onResume();
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mPlayer.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy() {
		mPlayer.onDestroy();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return mPlayer.onKeyDown(keyCode, event)
				|| super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return mPlayer.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event);
	}


	//
	// public void CallVideoPlay(String m_uri) {
	//
	// Intent intent = new Intent(this, MovieActivity.class);
	// intent.putExtra("prod_url", m_uri);
	// intent.putExtra("prod_id", prod_id);
	//
	// try {
	// startActivity(intent);
	// } catch (ActivityNotFoundException ex) {
	// Log.e(TAG, "video failed", ex);
	// }
	//
	// }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// add here.
		super.onActivityResult(requestCode, resultCode, data);
	}

}
