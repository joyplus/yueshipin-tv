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

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.tv.App;
import com.joyplus.tv.Constant;
import com.joyplus.tv.R;
import com.joyplus.tv.Adapters.CurrentPlayData;
import com.joyplus.tv.Service.Return.ReturnProgramView;
import com.joyplus.tv.Service.Return.ReturnProgramView.DOWN_URLS;
import com.joyplus.tv.database.TvDatabaseHelper;
import com.joyplus.tv.entity.HotItemInfo;
import com.joyplus.tv.utils.BangDanConstant;
import com.joyplus.tv.utils.DataBaseItems.UserHistory;
import com.joyplus.tv.utils.DataBaseItems.UserShouCang;
import com.joyplus.tv.utils.DBUtils;
import com.joyplus.tv.utils.Log;
import com.joyplus.tv.utils.UtilTools;
import com.umeng.analytics.MobclickAgent;

/**
 * This activity plays a video from a specified URI.
 */
public class VideoPlayerActivity extends Activity {
	@SuppressWarnings("unused")
	private static final String TAG = "VideoPlayerActivity";
	private static final String MOVIE_PLAY = "电影播放";
	private static final String TV_PLAY = "电视剧播放";
	private static final String SHOW_PLAY = "综艺播放";
	
	private App app;
	private AQuery aq;

	private MoviePlayer mPlayer;
	private AudioManager mAudioManager;
	
	private boolean mFinishOnCompletion;
	

	private String prod_id = null;
	private int prod_type = 0;
	private String prod_name = null;
	private String prod_url = null;// 播放地址
	
//	private Uri mUri;
//	private String prod_src = null;// 来源
//	private int prod_qua = 0;// 清晰度 1080p 或720p
	
//	private CurrentPlayData mCurrentPlayData = null;
	private ReturnProgramView m_ReturnProgramView = null;

	
	private int mTime = 0;
	private String dbCurEpisode = "";//数据库中当前影片的更新集数
	
//	private List<Result> Result_list = new ArrayList<Result>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (App) getApplication();
		aq = new AQuery(this);

		getWindow().setFormat(PixelFormat.RGBA_8888);
		
		setContentView(R.layout.video_player);
		View rootView = findViewById(R.id.root);

		Intent intent = getIntent();
		prod_name = intent.getStringExtra("title");
		prod_url = intent.getStringExtra("prod_url");

		prod_name = "未命名";
		// prod_url =
		// "http://221.130.179.66/25/36/53/kingsoft/movie/47978987920B0079FF686B6370B4E039-xiyoupian.mp4?crypt=61740d1aa7f2e300&b=800&gn=132&nc=1&bf=30&p2p=1&video_type=mp4&check=0&tm=1364191200&key=af7b9ad0697560c682a0070cf225e65e&opck=1&lgn=letv&proxy=3702889363&cipi=2026698610&tsnp=1&tag=ios&tag=kingsoft&sign=coopdown&realext=.mp4test=m3u8";
		//

		m_ReturnProgramView = app.get_ReturnProgramView();
		
		if (m_ReturnProgramView == null) {// 如果为空，那就调用此方法

			loadReturnProgramView();

		}

		CurrentPlayData tempCurrentPlayData = app.getCurrentPlayData();
		if (tempCurrentPlayData != null) {

			prod_id = tempCurrentPlayData.prod_id;
			prod_name = tempCurrentPlayData.prod_name;
			prod_url = tempCurrentPlayData.prod_url;
//			prod_src = mCurrentPlayData.prod_src;
//			prod_qua = mCurrentPlayData.prod_qua;
			prod_type = tempCurrentPlayData.prod_type;
			mTime = (int) tempCurrentPlayData.prod_time;


			
//			if (m_ReturnProgramView != null) { //过滤不能播放的地址
//				String where = mCurrentPlayData.prod_id + "_"
//						+ mCurrentPlayData.prod_type + "_"
//						+ mCurrentPlayData.CurrentIndex + "_"
//						+ mCurrentPlayData.CurrentSource + "_"
//						+ mCurrentPlayData.CurrentQuality;
//				GetNextValURL(where);
//			}
			
//			if (prod_type == 2 && prod_type == 3) {
//				if (tempCurrentPlayData.CurrentIndex == 0)
//					aq.id(R.id.imageControl_r).gone();
//
//			}
			
			//判断当前影片是否为置顶影片，获取数据库中当前更新集数
			if(prod_type == Integer.valueOf(BangDanConstant.DONGMAN_TYPE) 
					||prod_type == Integer.valueOf(BangDanConstant.TV_TYPE)
					||prod_type == Integer.valueOf(BangDanConstant.ZONGYI_TYPE)) {
				
				dbCurEpisode = DBUtils.getTopPlayerCurEpisode(getApplicationContext(),
						UtilTools.getCurrentUserId(getApplicationContext()), prod_id);
				if(dbCurEpisode != null && !dbCurEpisode.equals("")) {
					
					if(prod_name.contains(dbCurEpisode)) {//如果名字中含有当前集数，那就播放过 取消置顶状态
						
						DBUtils.cancelAPlayTopState(getApplicationContext(),
								UtilTools.getCurrentUserId(getApplicationContext()), prod_id);
					}
				}
				
			}
			
			
			SaveRecordToService(tempCurrentPlayData);
			// else {
			// aq.id(R.id.imageControl_r).getView().setVisibility(View.)
			// aq.id(R.id.imageControl_t).v
			// }
		}
		if (prod_url == null || prod_url.length() == 0){
			finish();
			return;
		}
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

		Log.i(TAG, "url------->" + prod_url);

	}
	
	private void loadReturnProgramView() {

		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				getServiceDate();
				return null;
			}
		};
		task.execute();
	}

	private void getServiceDate() {
		String url = Constant.BASE_URL + "program/view" + "?prod_id=" + prod_id;
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, "initDate");
		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}

	public void initDate(String url, JSONObject json, AjaxStatus status) {
		if (status.getCode() == AjaxStatus.NETWORK_ERROR || json == null) {
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}

		if (json == null || json.equals(""))
			return;

		Log.d(TAG, "data = " + json.toString());
		ObjectMapper mapper = new ObjectMapper();
		try {

			m_ReturnProgramView = mapper.readValue(json.toString(),
					ReturnProgramView.class);
			
			CurrentPlayData tempCurrentPlayData = app.getCurrentPlayData();
			
			if(tempCurrentPlayData != null) {
				
				//如果是综艺，重新计算集数
				if(tempCurrentPlayData.prod_type == 3 &&
						tempCurrentPlayData.CurrentIndex == -1
						){
					for (int i = 0; i < m_ReturnProgramView.show.episodes.length; i++) {
						if(m_ReturnProgramView.show.episodes[i].name.equalsIgnoreCase(tempCurrentPlayData.prod_sub_name))
							tempCurrentPlayData.CurrentIndex = i;
					}
					
				}
				ReIndexURL(tempCurrentPlayData);
			}
			
			app.set_ReturnProgramView(m_ReturnProgramView);

		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void ReIndexURL(CurrentPlayData tempCurrentPlayData) {

		switch (tempCurrentPlayData.prod_type) {
		case 1: {
			if (m_ReturnProgramView.movie.episodes[0].down_urls != null) {
				videoSourceSort(m_ReturnProgramView.movie.episodes[0].down_urls);
			}

		}
			break;
		case 131:
		case 2: {
			if (m_ReturnProgramView.tv.episodes[tempCurrentPlayData.CurrentIndex].down_urls != null) {
				videoSourceSort(m_ReturnProgramView.tv.episodes[tempCurrentPlayData.CurrentIndex].down_urls);
				
			}

		}
			break;
		case 3: {
			if (m_ReturnProgramView.show.episodes[tempCurrentPlayData.CurrentIndex].down_urls != null) {
				videoSourceSort(m_ReturnProgramView.show.episodes[tempCurrentPlayData.CurrentIndex].down_urls);
			}

		}
			break;
		}

	}

	// 给片源赋权值
	@SuppressWarnings("unchecked")
	public void videoSourceSort(DOWN_URLS[] down_urls) {
		
		if (down_urls != null) {
			for (int j = 0; j < down_urls.length; j++) {
				if (down_urls[j].source.equalsIgnoreCase("letv")) {
					down_urls[j].index = 0;
				} else if (down_urls[j].source.equalsIgnoreCase("fengxing")) {
					down_urls[j].index = 1;
				} else if (down_urls[j].source.equalsIgnoreCase("qiyi")) {
					down_urls[j].index = 2;
				} else if (down_urls[j].source.equalsIgnoreCase("youku")) {
					down_urls[j].index = 3;
				} else if (down_urls[j].source.equalsIgnoreCase("sinahd")) {
					down_urls[j].index = 4;
				} else if (down_urls[j].source.equalsIgnoreCase("sohu")) {
					down_urls[j].index = 5;
				} else if (down_urls[j].source.equalsIgnoreCase("56")) {
					down_urls[j].index = 6;
				} else if (down_urls[j].source.equalsIgnoreCase("qq")) {
					down_urls[j].index = 7;
				} else if (down_urls[j].source.equalsIgnoreCase("pptv")) {
					down_urls[j].index = 8;
				} else if (down_urls[j].source.equalsIgnoreCase("m1905")) {
					down_urls[j].index = 9;
				}
			}
			if (down_urls.length > 1) {
				Arrays.sort(down_urls, new EComparatorIndex());
			}
		}
	}

	// 将片源排序
	@SuppressWarnings("rawtypes")
	class EComparatorIndex implements Comparator {

		@Override
		public int compare(Object first, Object second) {
			// TODO Auto-generated method stub
			int first_name = ((DOWN_URLS) first).index;
			int second_name = ((DOWN_URLS) second).index;
			if (first_name - second_name < 0) {
				return -1;
			} else {
				return 1;
			}
		}
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub

		Log.i(TAG, "event--->" + event.getKeyCode());
		return super.dispatchKeyEvent(event);
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

					CurrentPlayData tempCurrentPlayData = app.getCurrentPlayData();
					if(tempCurrentPlayData != null) {
						
						if (Integer.parseInt(mContent) <= mPlayer.getDuration()) {
							if (mPlayer.getDuration() - Integer.parseInt(mContent) < 10000
									&& tempCurrentPlayData.prod_type != 1)// 下一集
								mPlayer.OnContinueVideoPlay();
							else
								mPlayer.onSeekMove(Integer.parseInt(mContent));
						}
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
		CurrentPlayData tempCurrentPlayData = app.getCurrentPlayData();
//		m_ReturnProgramView = app.get_ReturnProgramView();
		int mTime = 0;
		if (tempCurrentPlayData != null) {
			prod_id = tempCurrentPlayData.prod_id;
			prod_name = tempCurrentPlayData.prod_name;
			prod_url = tempCurrentPlayData.prod_url;
//			prod_src = mCurrentPlayData.prod_src;
//			prod_qua = mCurrentPlayData.prod_qua;
			mTime = (int) tempCurrentPlayData.prod_time;
		}
		if (mPlayer != null && URLUtil.isNetworkUrl(prod_url)) {
			mPlayer.setVideoURI(Uri.parse(prod_url), mTime);
		}
		super.onNewIntent(intent);

	}

	public void OnClickPause(View v) {
		Log.i(TAG, "OnClickPause---->");
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
		Log.i(TAG, "OnClickPre---->");
		if (mPlayer != null) {
			mPlayer.exitReturnMode();
			mPlayer.OnPreVideoPlay();
			
			cancelAPlayerTopState();//符合条件，因为播放 取消置顶状态
		}
	}

	public void OnClickContinue(View v) {
		Log.i(TAG, "OnClickContinue---->");
		if (mPlayer.isPause()) {
			mPlayer.exitReturnMode();
			mPlayer.playTVVideo();
		}
	}

	public void OnClickNext(View v) {
		Log.i(TAG, "OnClickNext---->");

		if (mPlayer != null) {
			mPlayer.exitReturnMode();
			mPlayer.OnContinueVideoPlay();
			
			cancelAPlayerTopState();//符合条件，因为播放 取消置顶状态
		}
	}
	
	private void cancelAPlayerTopState() {
		
		CurrentPlayData currentPlayData = app.getCurrentPlayData();
		
		if(currentPlayData != null) {
			
			int tempType = currentPlayData.CurrentIndex;
			String tempProName = currentPlayData.prod_name;
			
			//判断当前影片是否为置顶影片，获取数据库中当前更新集数
			if(tempType == Integer.valueOf(BangDanConstant.DONGMAN_TYPE) 
					||tempType == Integer.valueOf(BangDanConstant.TV_TYPE)
					||tempType == Integer.valueOf(BangDanConstant.ZONGYI_TYPE)) {
				
//				dbCurEpisode = StatisticsUtils.getTopPlayerCurEpisode(getApplicationContext(),
//						StatisticsUtils.getCurrentUserId(getApplicationContext()), prod_id);
				if(dbCurEpisode != null && !dbCurEpisode.equals("")) {
					
					if(tempProName.contains(dbCurEpisode)) {//如果名字中含有当前集数，那就播放过 取消置顶状态
						
						DBUtils.cancelAPlayTopState(getApplicationContext(),
								UtilTools.getCurrentUserId(getApplicationContext()), prod_id);
					}
				}
				
			}
		}
	}

	public void OnClickFav(View v) {
		Log.i(TAG, "OnClickFav---->");
//		mPlayer.exitReturnMode();
		
		if (mPlayer.isPause()) {//收藏或者取消收藏后
			mPlayer.exitReturnMode();
			mPlayer.playTVVideo();
		}
		
		CurrentPlayData tempCurrentPlayData = app.getCurrentPlayData();
		
		if(tempCurrentPlayData != null) {
			
			if (!tempCurrentPlayData.prod_favority) {
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
				
				CurrentPlayData tempCurrentPlayData = app.getCurrentPlayData();
				
				if(tempCurrentPlayData != null) {
					
					tempCurrentPlayData.prod_favority = true;
					app.setCurrentPlayData(tempCurrentPlayData);
				}
				
				findViewById(R.id.imageControl_b).setBackgroundResource(R.drawable.player_btn_unfav);
//				setResult(101);
//				finish();
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
				CurrentPlayData tempCurrentPlayData = app.getCurrentPlayData();
				
				if(tempCurrentPlayData != null) {
					
					tempCurrentPlayData.prod_favority = false;
					app.setCurrentPlayData(tempCurrentPlayData);
				}
				findViewById(R.id.imageControl_b).setBackgroundResource(R.drawable.player_btn_fav);
//				setResult(102);
//				finish();
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

		super.onPause();
		
		MobclickAgent.onPause(this);
	}

	public void SaveToServer(CurrentPlayData tempCurrentPlayData,int playback_time, int duration) {
		String url = Constant.BASE_URL + "program/play";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("app_key", Constant.APPKEY);// required string
												// 申请应用时分配的AppKey。
		params.put("prod_id", prod_id);// required string
										// 视频id
										// prod_name
		// String titleName = " 第"
		// + (mCurrentPlayData.CurrentIndex + 1)
		// + "集";
		// mCurrentPlayData.prod_name= name + title;
		params.put("prod_name", tempCurrentPlayData.prod_name);// required
		// string 视频名字
		switch (tempCurrentPlayData.prod_type) {
		case 1: {
//			params.put(
//					"prod_subname",
//					m_ReturnProgramView.movie.episodes[mCurrentPlayData.CurrentIndex].name);
			params.put(
			"prod_subname",
			"");
		}
			break;
		case 131:
		case 2: {
			params.put(
					"prod_subname",
					m_ReturnProgramView.tv.episodes[tempCurrentPlayData.CurrentIndex].name);

		}
			break;
		case 3: {
			params.put(
					"prod_subname",
					m_ReturnProgramView.show.episodes[tempCurrentPlayData.CurrentIndex].name);

		}
			break;
		}
//		if (mCurrentPlayData != null && mCurrentPlayData.prod_type != 1) {
//			params.put("prod_subname",
//					Integer.toString(mCurrentPlayData.CurrentIndex + 1));// required
//		}

		// string
		// 视频的集数
		params.put("prod_type", tempCurrentPlayData.prod_type);// required int 视频类别
		// 1：电影，2：电视剧，3：综艺，4：视频
		params.put("playback_time", playback_time);// _time required int
													// 上次播放时间，单位：秒
		params.put("duration", duration);// required int 视频时长， 单位：秒
		params.put("play_type", "1");// required string
		// 播放的类别 1: 视频地址播放
		// 2:webview播放
		params.put("video_url", tempCurrentPlayData.prod_url);// required
		// string
		// 视频url
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.SetHeader(app.getHeaders());
		cb.params(params).url(url).type(JSONObject.class)
				.weakHandler(this, "CallProgramPlayResult");
		aq.ajax(cb);
		
		//DB操作，把存储到服务器的数据保存到数据库
		TvDatabaseHelper helper = TvDatabaseHelper
				.newTvDatabaseHelper(getApplicationContext());
		SQLiteDatabase database = helper.getWritableDatabase();// 获取写db
		
		String selection = UserShouCang.USER_ID + "=? and " + UserHistory.PRO_ID + "=?";// 通过用户id，找到相应信息
		String[] selectionArgs = { UtilTools.getCurrentUserId(getApplicationContext()),prod_id };
		
		database.delete(TvDatabaseHelper.HISTORY_TABLE_NAME, selection,
				selectionArgs);
		
		HotItemInfo info = new HotItemInfo();
		info.prod_type = prod_type + "";
		info.prod_name = prod_name;
		if (tempCurrentPlayData != null) {
			
			info.prod_subname = (tempCurrentPlayData.CurrentIndex + 1) + "";
		}
		info.prod_id = prod_id;
		info.play_type = "1";
		info.playback_time = playback_time + "";
		info.video_url = tempCurrentPlayData.prod_url;
		info.duration = duration + "";
				
		DBUtils.insertHotItemInfo2DB_History(getApplicationContext(), info,
				UtilTools.getCurrentUserId(getApplicationContext()), database);
		
		helper.closeDatabase();

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
		
		MobclickAgent.onResume(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mPlayer.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy() {
		
		MobclickAgent.onEventEnd(this, MOVIE_PLAY);
		MobclickAgent.onEventEnd(this, TV_PLAY);
		MobclickAgent.onEventEnd(this, SHOW_PLAY);
		MobclickAgent.onPause(this);
		
		CurrentPlayData tempCurrentPlayData = app.getCurrentPlayData();
		
		if (mPlayer != null && tempCurrentPlayData != null) {
			/*
			 * 获取当前播放时间和总时间,将播放时间和总时间放在服务器上
			 */
			
			if(m_ReturnProgramView != null
					&& tempCurrentPlayData.CurrentIndex != -1)
				SaveToServer(tempCurrentPlayData,mPlayer.getCurrentPositon() / 1000,
					mPlayer.getDuration() / 1000);

		}
		if (aq != null)
			aq.dismiss();
		if(mPlayer != null){
			mPlayer.onDestroy();
			unregisterReceiver(mReceiver);
		}
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

	public void SaveRecordToService(CurrentPlayData tempCurrentPlayData) {
		if (tempCurrentPlayData != null) {
			switch (tempCurrentPlayData.prod_type) {
			case 1:
				UtilTools.StatisticsClicksShow(new AQuery(this), app,
						prod_id, prod_name, "", 1);
				break;
			case 131:
				UtilTools.StatisticsClicksShow(new AQuery(this), app,
						prod_id, prod_name, tempCurrentPlayData.CurrentIndex + "",
						131);
				break;
			case 2:

				UtilTools.StatisticsClicksShow(new AQuery(this), app,
						prod_id, prod_name, tempCurrentPlayData.CurrentIndex + "",
						2);

				break;
			case 3:

				UtilTools.StatisticsClicksShow(new AQuery(this), app,
						prod_id, prod_name, tempCurrentPlayData.CurrentIndex + "",
						3);

				break;
			}
		}

	}
	
//	private void GetNextValURL(String where) {
//	prod_url = null;
//
//	switch (mCurrentPlayData.prod_type) {
//	case 1: {
//		while (app.GetPlayData(where) != null) {
//			if (mCurrentPlayData.CurrentQuality < m_ReturnProgramView.movie.episodes[mCurrentPlayData.CurrentIndex].down_urls[mCurrentPlayData.CurrentSource].urls.length) {
//				mCurrentPlayData.CurrentQuality += 1;
//			}
//			else {
//				mCurrentPlayData.CurrentSource += 1;
//				mCurrentPlayData.CurrentQuality = 0;
//			}
//			where = mCurrentPlayData.prod_id + "_"
//					+ mCurrentPlayData.prod_type + "_"
//					+ mCurrentPlayData.CurrentIndex + "_"
//					+ mCurrentPlayData.CurrentSource + "_"
//					+ mCurrentPlayData.CurrentQuality;
//			
//		}
//
//		app.setCurrentPlayData(mCurrentPlayData);
//		try{
//			prod_url = m_ReturnProgramView.movie.episodes[0].
//					down_urls[mCurrentPlayData.CurrentSource].
//					urls[mCurrentPlayData.CurrentQuality].url;
//		}catch (Exception e) {
//			// TODO: url is null
//		}
//	}
//		break;
//	case 131:
//	case 2: {
//		while (app.GetPlayData(where) != null) {
//			if (mCurrentPlayData.CurrentQuality < m_ReturnProgramView.tv.episodes[mCurrentPlayData.CurrentIndex].down_urls[mCurrentPlayData.CurrentSource].urls.length) {
//				mCurrentPlayData.CurrentQuality += 1;
//			}
//			else {
//				mCurrentPlayData.CurrentSource += 1;
//				mCurrentPlayData.CurrentQuality = 0;
//			}
//			where = mCurrentPlayData.prod_id + "_"
//					+ mCurrentPlayData.prod_type + "_"
//					+ mCurrentPlayData.CurrentIndex + "_"
//					+ mCurrentPlayData.CurrentSource + "_"
//					+ mCurrentPlayData.CurrentQuality;
//			
//		}
//
//		app.setCurrentPlayData(mCurrentPlayData);
//		try{
//			prod_url = m_ReturnProgramView.tv.episodes[mCurrentPlayData.CurrentIndex].
//					down_urls[mCurrentPlayData.CurrentSource].
//					urls[mCurrentPlayData.CurrentQuality].url;
//		}catch (Exception e) {
//			// TODO: url is null
//		}
//	}
//
//		break;
//	case 3: {
//		while (app.GetPlayData(where) != null) {
//			if (mCurrentPlayData.CurrentQuality < m_ReturnProgramView.show.episodes[mCurrentPlayData.CurrentIndex].down_urls[mCurrentPlayData.CurrentSource].urls.length) {
//				mCurrentPlayData.CurrentQuality += 1;
//			}
//			else {
//				mCurrentPlayData.CurrentSource += 1;
//				mCurrentPlayData.CurrentQuality = 0;
//			}
//			where = mCurrentPlayData.prod_id + "_"
//					+ mCurrentPlayData.prod_type + "_"
//					+ mCurrentPlayData.CurrentIndex + "_"
//					+ mCurrentPlayData.CurrentSource + "_"
//					+ mCurrentPlayData.CurrentQuality;
//			
//		}
//
//		app.setCurrentPlayData(mCurrentPlayData);
//		try{
//			prod_url = m_ReturnProgramView.show.episodes[mCurrentPlayData.CurrentIndex].
//					down_urls[mCurrentPlayData.CurrentSource].
//					urls[mCurrentPlayData.CurrentQuality].url;
//		}catch (Exception e) {
//			// TODO: url is null
//		}
//	}
//
//		break;
//	}
//	
//
//}
	
	public class Result {

		public String url;
		public AjaxStatus status;
	}

}
