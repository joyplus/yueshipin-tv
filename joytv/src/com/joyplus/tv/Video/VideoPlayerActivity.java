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
	private String prod_url = null;
	private String prod_name = null;
	private AudioManager mAudioManager;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (App) getApplication();
		// requestWindowFeature(Window.FEATURE_ACTION_BAR);
		// requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		setContentView(R.layout.video_player);
		View rootView = findViewById(R.id.root);
		View contrlView = findViewById(R.id.relativeLayoutControl);
		Intent intent = getIntent();
		prod_name = intent.getStringExtra("title");
		prod_url = intent.getStringExtra("prod_url");
		((TextView) findViewById(R.id.textView1)).setText(prod_name);
		// initializeActionBar(intent);
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

	private void initializeActionBar(Intent intent) {
		// ActionBar actionBar = getActionBar();
		// actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
		// ActionBar.DISPLAY_HOME_AS_UP);
		String title = intent.getStringExtra(Intent.EXTRA_TITLE);
		mUri = intent.getData();
		if (title == null) {
			Cursor cursor = null;
			try {
				cursor = getContentResolver().query(mUri,
						new String[] { VideoColumns.TITLE }, null, null, null);
				if (cursor != null && cursor.moveToNext()) {
					title = cursor.getString(0);
				}
			} catch (Throwable t) {
				Log.w(TAG, "cannot get title from: " + intent.getDataString(),
						t);
			} finally {
				if (cursor != null)
					cursor.close();
			}
		}
		// if (title != null)
		// actionBar.setTitle(title);
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
