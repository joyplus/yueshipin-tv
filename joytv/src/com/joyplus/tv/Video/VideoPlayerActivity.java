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
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.joyplus.tv.App;
import com.joyplus.tv.Constant;
import com.joyplus.tv.R;
import com.joyplus.tv.StatisticsUtils;
import com.joyplus.tv.Adapters.CurrentPlayData;
import com.joyplus.tv.Service.Return.ReturnProgramView;
import com.umeng.analytics.MobclickAgent;

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
	private AQuery aq;
	private String prod_id = null;
	private int prod_type = 0;
	private String prod_name = null;
	private String prod_url = null;// 播放地址
	private String prod_src = null;// 来源
	private int prod_qua = 0;// 清晰度 1080p 或720p
	private CurrentPlayData mCurrentPlayData = null;
	private ReturnProgramView m_ReturnProgramView = null;
	private AudioManager mAudioManager;
	private static String MOVIE_PLAY = "电影播放";
	private static String TV_PLAY = "电视剧播放";
	private static String SHOW_PLAY = "综艺播放";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (App) getApplication();
		aq = new AQuery(this);

		setContentView(R.layout.video_player);
		View rootView = findViewById(R.id.root);

		Intent intent = getIntent();
		prod_name = intent.getStringExtra("title");
		prod_url = intent.getStringExtra("prod_url");
		prod_name = "未命名";
		// prod_url =
		// "http://221.130.179.66/25/36/53/kingsoft/movie/47978987920B0079FF686B6370B4E039-xiyoupian.mp4?crypt=61740d1aa7f2e300&b=800&gn=132&nc=1&bf=30&p2p=1&video_type=mp4&check=0&tm=1364191200&key=af7b9ad0697560c682a0070cf225e65e&opck=1&lgn=letv&proxy=3702889363&cipi=2026698610&tsnp=1&tag=ios&tag=kingsoft&sign=coopdown&realext=.mp4test=m3u8";
		//

		mCurrentPlayData = app.getCurrentPlayData();
		m_ReturnProgramView = app.get_ReturnProgramView();
		int mTime = 0;
		if (mCurrentPlayData != null) {
			prod_id = mCurrentPlayData.prod_id;
			prod_name = mCurrentPlayData.prod_name;
			prod_url = mCurrentPlayData.prod_url;
			prod_src = mCurrentPlayData.prod_src;
			prod_qua = mCurrentPlayData.prod_qua;
			prod_type = mCurrentPlayData.prod_type;
			mTime = (int) mCurrentPlayData.prod_time;
			if (prod_type == 2 && prod_type == 3) {
				if (mCurrentPlayData.CurrentIndex == 0)
					aq.id(R.id.imageControl_r).gone();

			}
			SaveRecordToService();
			// else {
			// aq.id(R.id.imageControl_r).getView().setVisibility(View.)
			// aq.id(R.id.imageControl_t).v
			// }
		}
		if (prod_url == null || prod_url.length() == 0)
			finish();
		Uri mUri = Uri.parse(prod_url);
		// prod_url
		// ="http://g3.letv.cn/vod/v2/MjUvNDgvOTEvbGV0di11dHMvMjM0OTQzOC1BVkMtOTU4ODczLUFBQy0xMjc3MjQtNjA4MDk2MC04MjkxMTA0MzYtNjI1NjNmMTQxNDMxNGFkODY3ZGRjMGNhMTFkNjIxNjgtMTM2NDkxMjMxOTkxMC5tcDQ=?b=1090&mmsid=2341519&tm=1365557814&platid=1&splatid=2&key=75e40c4fc8927605afe8456dc0f9b207&m3u8=ios,2341519?_r0.3129199950490147&test=m3u8";
		//
		((TextView) findViewById(R.id.textView1)).setText(prod_name);
		mFinishOnCompletion = intent.getBooleanExtra(
				MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
		mPlayer = new MoviePlayer(rootView, this, mTime, prod_type, mUri,
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

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.VIDEOPLAYERCMD);
		registerReceiver(mReceiver, intentFilter);
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(Constant.VIDEOPLAYERCMD)) {
				int mCMD = intent.getIntExtra("cmd", 0);
				String mContent = intent.getStringExtra("content");
				String mProd_url = intent.getStringExtra("prod_url");
				if (!mProd_url.equalsIgnoreCase(mPlayer.getCurrentUrl()))
					return;
				/*
				 * “403”：视频推送后，手机发送播放指令。 “405”：视频推送后，手机发送暂停指令。
				 * “407”：视频推送后，手机发送快进指令。 “409”：视频推送后，手机发送后退指令。
				 */
				switch (mCMD) {
				case 403:
					if (mPlayer.isPause())
						mPlayer.playVideo();
					break;
				case 405:
					if (!mPlayer.isPause())
						mPlayer.pauseVideo();
					break;
				case 407:

					if (Integer.parseInt(mContent) <= mPlayer.getDuration()) {
						if (mPlayer.getDuration() - Integer.parseInt(mContent) < 10000
								&& mCurrentPlayData.prod_type != 1)// 下一集
							mPlayer.OnContinueVideoPlay();
						else
							mPlayer.onSeekMove(Integer.parseInt(mContent));
					}
					break;
				case 409:
					finish();
					break;
				}

			} else {
			}
		}
	};

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		mCurrentPlayData = app.getCurrentPlayData();
		m_ReturnProgramView = app.get_ReturnProgramView();
		int mTime = 0;
		if (mCurrentPlayData != null) {
			prod_id = mCurrentPlayData.prod_id;
			prod_name = mCurrentPlayData.prod_name;
			prod_url = mCurrentPlayData.prod_url;
			prod_src = mCurrentPlayData.prod_src;
			prod_qua = mCurrentPlayData.prod_qua;
			mTime = (int) mCurrentPlayData.prod_time;
		}
		if (mPlayer != null && URLUtil.isNetworkUrl(prod_url)) {
			mPlayer.setVideoURI(Uri.parse(prod_url), mTime);
		}
		super.onNewIntent(intent);

	}

	public void OnClickPause(View v) {
		if (prod_type != 1
				&& mPlayer.getCurrentKeyEvent() == KeyEvent.KEYCODE_BACK
				&& mPlayer.getCurrentReturnMode())
			finish();
		else {
			if (mPlayer.isPause())
				mPlayer.playVideo();
			else
				mPlayer.pauseVideo();
		}
	}

	public void OnClickPre(View v) {
		if (mPlayer != null) {
			mPlayer.exitReturnMode();
			mPlayer.OnPreVideoPlay();
		}
	}

	public void OnClickContinue(View v) {
		if (mPlayer.isPause()) {
			mPlayer.exitReturnMode();
			mPlayer.playTVVideo();
		}
	}

	public void OnClickNext(View v) {
		if (mPlayer != null) {
			mPlayer.exitReturnMode();
			mPlayer.OnContinueVideoPlay();
		}
	}

	public void OnClickFav(View v) {
		mPlayer.exitReturnMode();
		if (!mCurrentPlayData.prod_favority) {
			String url = Constant.BASE_URL + "program/favority";

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("prod_id", prod_id);

			AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
			cb.SetHeader(app.getHeaders());

			cb.params(params).url(url).type(JSONObject.class)
					.weakHandler(this, "CallServiceFavorityResult");
			aq.ajax(cb);
		} else {// 取消收藏
			String url = Constant.BASE_URL + "program/unfavority";

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("prod_id", prod_id);

			AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
			cb.SetHeader(app.getHeaders());

			cb.params(params).url(url).type(JSONObject.class)
					.weakHandler(this, "UnfavorityResult");

			aq.ajax(cb);
		}
	}

	public void CallServiceFavorityResult(String url, JSONObject json,
			AjaxStatus status) {

		if (json != null) {
			try {
				// woof is "00000",now "20024",by yyc
				if (json.getString("res_code").trim().equalsIgnoreCase("00000")) {
					app.MyToast(this, "收藏成功!");
				} else
					app.MyToast(this, "已收藏!");
				setResult(101);
				finish();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// ajax error, show error code
			if (status.getCode() == AjaxStatus.NETWORK_ERROR)
				app.MyToast(aq.getContext(),
						getResources().getString(R.string.networknotwork));
		}

	}

	public void UnfavorityResult(String url, JSONObject json, AjaxStatus status) {
		if (json != null) {
			try {
				if (json.getString("res_code").trim().equalsIgnoreCase("00000")) {
					app.MyToast(this, "取消收藏成功!");
					// GetServiceData(1);
				} else
					app.MyToast(this, "取消收藏失败!");
				setResult(102);
				finish();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

			// ajax error, show error code
			if (status.getCode() == AjaxStatus.NETWORK_ERROR)
				app.MyToast(this,
						getResources().getString(R.string.networknotwork));
		}
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
		// ((AudioManager) getSystemService(AUDIO_SERVICE)).requestAudioFocus(
		// null, AudioManager.STREAM_MUSIC,
		// AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
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

		MobclickAgent.onEventEnd(this, MOVIE_PLAY);
		MobclickAgent.onEventEnd(this, TV_PLAY);
		MobclickAgent.onEventEnd(this, SHOW_PLAY);
		MobclickAgent.onPause(this);
		if (mPlayer != null && URLUtil.isNetworkUrl(prod_url)) {
			/*
			 * 获取当前播放时间和总时间,将播放时间和总时间放在服务器上
			 */
			SaveToServer(mPlayer.getCurrentPositon() / 1000,
					mPlayer.getDuration() / 1000);

		}
		super.onPause();
	}

	public void SaveToServer(int playback_time, int duration) {
		String url = Constant.BASE_URL + "program/play";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("app_key", Constant.APPKEY);// required string
												// 申请应用时分配的AppKey。
		params.put("prod_id", prod_id);// required string
										// 视频id
		params.put("prod_name", prod_name);// required
		// string 视频名字
		if (mCurrentPlayData != null && mCurrentPlayData.prod_type != 1) {
			params.put("prod_subname",
					Integer.toString(mCurrentPlayData.CurrentIndex + 1));// required
		}

		// string
		// 视频的集数
		params.put("prod_type", mCurrentPlayData.prod_type);// required int 视频类别
		// 1：电影，2：电视剧，3：综艺，4：视频
		params.put("playback_time", playback_time);// _time required int
													// 上次播放时间，单位：秒
		params.put("duration", duration);// required int 视频时长， 单位：秒
		params.put("play_type", "1");// required string
		// 播放的类别 1: 视频地址播放
		// 2:webview播放
		params.put("video_url", prod_url);// required
		// string
		// 视频url
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.SetHeader(app.getHeaders());
		cb.params(params).url(url).type(JSONObject.class)
				.weakHandler(this, "CallProgramPlayResult");
		aq.ajax(cb);

	}

	public void CallProgramPlayResult(String url, JSONObject json,
			AjaxStatus status) {
		/*
		 * 保存历史播放记录的回调函数 prod_id index 播放时间
		 */
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
		if (aq != null)
			aq.dismiss();
		unregisterReceiver(mReceiver);
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

	public void SaveRecordToService() {
		if (mCurrentPlayData != null) {
			switch (mCurrentPlayData.prod_type) {
			case 1:
				StatisticsUtils.StatisticsClicksShow(new AQuery(this), app,
						prod_id, prod_name,
						"", 1);
				break;
			case 131:
				StatisticsUtils.StatisticsClicksShow(new AQuery(this), app,
						prod_id, prod_name,
						mCurrentPlayData.CurrentIndex+"", 131);
				break;
			case 2:

				StatisticsUtils.StatisticsClicksShow(new AQuery(this), app,
						prod_id, prod_name,
						mCurrentPlayData.CurrentIndex+"", 2);

				break;
			case 3:

				StatisticsUtils.StatisticsClicksShow(new AQuery(this), app,
						prod_id, prod_name,
						mCurrentPlayData.CurrentIndex+"", 3);

				break;
			}
		}

	}

}
