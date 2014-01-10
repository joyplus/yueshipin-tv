package com.joyplus.tv;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.adkey.Ad;
import com.joyplus.adkey.AdListener;
import com.joyplus.adkey.banner.AdView;
import com.joyplus.manager.RequestAQueryManager;
import com.joyplus.sub_old_1.JoyplusSubManager;
import com.joyplus.tv.Service.Return.ReturnFengxingSecondView;
import com.joyplus.tv.Service.Return.ReturnFirstFengxingUrlView;
import com.joyplus.tv.Service.Return.ReturnProgramView;
import com.joyplus.tv.Service.Return.ReturnReGetVideoView;
import com.joyplus.tv.database.TvDatabaseHelper;
import com.joyplus.tv.entity.CurrentPlayDetailData;
import com.joyplus.tv.entity.HotItemInfo;
import com.joyplus.tv.entity.PlayerSourceType;
import com.joyplus.tv.entity.REQUEST_URL;
import com.joyplus.tv.entity.URLS_INDEX;
import com.joyplus.tv.ui.ArcView;
import com.joyplus.tv.ui.SubTitleView;
import com.joyplus.tv.utils.BangDanConstant;
import com.joyplus.tv.utils.DBUtils;
import com.joyplus.tv.utils.DataBaseItems.UserHistory;
import com.joyplus.tv.utils.DataBaseItems.UserShouCang;
import com.joyplus.tv.utils.DefinationComparatorIndex;
import com.joyplus.tv.utils.JieMianConstant;
import com.joyplus.tv.utils.SouceComparatorIndex1;
import com.joyplus.tv.utils.URLUtils;
import com.joyplus.tv.utils.UtilTools;
import com.joyplus.utils.DesUtils;
import com.joyplus.utils.Log;
import com.joyplus.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class VideoPlayerJPActivity extends Activity implements
		MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
		MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener,
		MediaPlayer.OnInfoListener, MediaPlayer.OnSeekCompleteListener,
		MediaPlayer.OnVideoSizeChangedListener, OnSeekBarChangeListener,
		OnClickListener, AdListener {

	private static final String TAG = "VideoPlayerActivity";
	
	private static final boolean DEBUG = false;

	private static final int MESSAGE_RETURN_DATE_OK = 0;
	private static final int MESSAGE_URLS_READY = MESSAGE_RETURN_DATE_OK + 1;
	private static final int MESSAGE_PALY_URL_OK = MESSAGE_URLS_READY + 1;
	private static final int MESSAGE_URL_NEXT = MESSAGE_PALY_URL_OK + 1;
	private static final int MESSAGE_UPDATE_PROGRESS = MESSAGE_URL_NEXT + 1;
	private static final int MESSAGE_HIDE_PROGRESSBAR = MESSAGE_UPDATE_PROGRESS + 1;
	private static final int MESSAGE_HIDE_VOICE = MESSAGE_HIDE_PROGRESSBAR + 1;
	private static final int MESSAGE_DATALOADING_UPDATE_NETSPEED = MESSAGE_HIDE_VOICE + 1;
	
	/**
	 * 数据加载
	 */
	private static final int STATUE_LOADING = 0;
	/**
	 * 播放
	 */
	private static final int STATUE_PLAYING = STATUE_LOADING + 1;
	/**
	 * 暂停
	 */
	private static final int STATUE_PAUSE = STATUE_PLAYING + 1;
	/**
	 * 快退、快进
	 */
	private static final int STATUE_FAST_DRAG = STATUE_PAUSE + 1;
	
	private static final int SEEKBAR_REFRESH_TIME = 200;//refresh time

	private int OFFSET = 33;
	private int seekBarWidthOffset = 40;

	private TextView mVideoNameText; // 名字
	private ImageView mDefinationIcon;// 清晰度icon
	private SeekBar mSeekBar; // 进度条
	private RelativeLayout mTimeLayout; // 时间提示块
	private TextView mCurrentTimeTextView; // 当前播放时间
	private TextView mTotalTimeTextView; // 总时长
	private RelativeLayout mFastIcon; // 快进（退）标识图标
	private TextView mFastTextView; // 快进（退）标识提示

	private TextView mLastTimeTextView;// 上次播放时间
	private TextView mResourceTextView;// 视频来源
	private TextView mSpeedTextView;// 网速
	private TextView mPercentTextView;// 完成百分比

	private ImageButton mPreButton;// 上一集
	private ImageButton mNextButton;// 下一集
	private ImageButton mTopButton;// 上面的（继续）按钮
	private ImageButton mBottomButton;// 上面的（收藏）按钮
	private ImageButton mCenterButton;// 中间的按钮

	private ImageButton mContinueButton;// 继续

	private ArcView mVoiceProgress; // 声音大小显示
	
	private TextView mDataLoadingSpeedText; //缓冲速度
	private long mCurrentLoadingbytes;

	/**
	 * 预加载层
	 */
	private RelativeLayout mPreLoadLayout;
	/**
	 * 播放提示相关层
	 */
	private RelativeLayout mNoticeLayout;
	/**
	 * 上下集控制层
	 */
	private LinearLayout mControlLayout;
	/**
	 * 声音相关层
	 */
	private LinearLayout mVocieLayout;

	/**
	 * 暂停继续层
	 */
	private LinearLayout mContinueLayout;
	/**
	 * 缓冲速度
	 */
	private LinearLayout mDateLoadingLayout;
	/**
	 * subtitle
	 */
	private SubTitleView mSubTitleView;

	/**
	 * 基本播放参数
	 */
	private String mProd_id;
	private String mProd_name;
	private int mProd_type;
	private String mProd_src;// 来源
	
	private String url_temp;//首次url备份
	private int mDefination = 0; // 清晰度 6为尝鲜，7为普清，8为高清
	private String mProd_sub_name = null;
	private int mEpisodeIndex = -1; // 当前集数对应的index
	private long lastTime = 0;

	/**
	 * 收藏
	 */
	private boolean isShoucang = false;// 默认为没有收藏

	/**
	 * 网络数据
	 */
	private int currentPlayIndex = -1;
	private String currentPlayUrl;
	private ReturnProgramView m_ReturnProgramView = null;
	private List<URLS_INDEX> playUrls = new ArrayList<URLS_INDEX>();

	private AQuery aq;
	private App app;

	private long mStartRX = 0;
	private long rxByteslast = 0;
	private long mLoadingPreparedPercent = 0;

	private int mStatue = 0;

	private int mTimeJumpSpeed = 0;
	private int mFastJumpTime = 0;
	int[] mTimes = { 1000, 333, 40 };

	/**
	 * android本身VideoView
	 */
	private VideoView mVideoView;

	private AudioManager mAudioManager;

	/** 最大声音 */
	private int mMaxVolume;
	/** 当前声音 */
	private int mVolume = -1;
	
	private Animation mAlphaDispear;
	private boolean isSeekBarIntoch = false;
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * blew is adkey varies
	 * @author yyc
	 */
	private RelativeLayout layout;
	private AdView mAdView;
	
	private boolean getFlvTypeFengXing=false;
	private boolean isOnlyExistFengXing = false;
	private boolean isOnlyExistLetv = false;//包括le_tv_fee、letv
	private boolean hasP2p = false;
	private boolean isRetry = false;
	private int	 reloadLetvCount = 0;
	
	private String sourceFromUrl = null;//当前集的原始播放地址
	
	private List<URLS_INDEX> playUrls_hd2 = new ArrayList<URLS_INDEX>();//超清
//	private List<URLS_INDEX> playUrls_hd = new ArrayList<URLS_INDEX>();//
	private List<URLS_INDEX> playUrls_mp4 = new ArrayList<URLS_INDEX>();//高清
	private List<URLS_INDEX> playUrls_flv = new ArrayList<URLS_INDEX>();//标清
	ArrayList<Integer> definationStrings = new ArrayList<Integer>();//清晰度选择
	ArrayList<Integer> zimuStrings = new ArrayList<Integer>();
	
	private JoyplusSubManager mJoyplusSubManager;
	
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(Constant.VIDEOPLAYERCMD)) {
				int mCMD = intent.getIntExtra("cmd", 0);
				Log.d(TAG, "onReceive------>" + mCMD);
				String mContent = intent.getStringExtra("content");
				String mProd_url = intent.getStringExtra("prod_url");
				if (!mProd_url.equalsIgnoreCase(url_temp)){
					Log.d(TAG, "mProd_url != url_temp");
					return ;
				}
				
				
				/*
				 * “403”：视频推送后，手机发送播放指令。 “405”：视频推送后，手机发送暂停指令。
				 * “407”：视频推送后，手机发送快进指令。 “409”：视频推送后，手机发送后退指令。
				 */
				switch (mCMD) {
				case 403:
					if (!mVideoView.isPlaying()) {
						mStatue = STATUE_PLAYING;
						mSeekBar.setEnabled(true);
						mVideoView.start();
						mContinueLayout.setVisibility(View.GONE);
						mControlLayout.setVisibility(View.GONE);
						mHandler.sendEmptyMessageDelayed(
								MESSAGE_HIDE_PROGRESSBAR, 2500);
					}
					break;
				case 405:
					if (mVideoView.isPlaying()) {
						mVideoView.pause();
						mStatue = STATUE_PAUSE;
						mSeekBar.setEnabled(false);
						mNoticeLayout.setVisibility(View.VISIBLE);
						mContinueLayout.setVisibility(View.VISIBLE);
						mContinueButton.requestFocus();
					}
					break;
				case 407:
					if (Integer.parseInt(mContent) <= mVideoView.getDuration()) {
						int destination = Integer.parseInt(mContent);
						if (destination < mVideoView.getDuration()) {
							mVideoView.seekTo(destination);
						}
						mNoticeLayout.setVisibility(View.VISIBLE);
						mHandler.sendEmptyMessageDelayed(
								MESSAGE_HIDE_PROGRESSBAR, 2500);
						// mVideoView.seekTo(c)
						// if (mPlayer.getDuration() -
						// Integer.parseInt(mContent) < 10000
						// && mCurrentPlayData.prod_type != 1)// 下一集
						// mPlayer.OnContinueVideoPlay();
						// else
						// mPlayer.onSeekMove(Integer.parseInt(mContent));
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
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Log.i(TAG, "onCreate--->");
		setContentView(R.layout.video_player_main);
		aq = new AQuery(this);
		app = (App) getApplication();
		mAlphaDispear = AnimationUtils.loadAnimation(this, R.anim.alpha_disappear);
		//广告位初始化
		layout = (RelativeLayout)findViewById(R.id.adsdkContent);
		
		if (mAdView != null) {
			removeBanner();
			if (layout != null)
				layout.setVisibility(View.GONE);
		}
		
		initViews();
		mSeekBar.setEnabled(false);
		m_ReturnProgramView = app.get_ReturnProgramView();
		resetSubManager();
		initVedioDate();

		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		winParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
		// winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		win.setAttributes(winParams);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.VIDEOPLAYERCMD);
		registerReceiver(mReceiver, intentFilter);

		// 获取是否收藏
		getIsShoucangData();
		
		OFFSET = (int) Utils.getStandardValue(getApplicationContext(), OFFSET);
		seekBarWidthOffset = (int) Utils.getStandardValue(getApplicationContext(), seekBarWidthOffset);
	}
	
	private void dismissView(View v){
		v.setVisibility(View.GONE);
		v.startAnimation(mAlphaDispear);
	}

	private void initVedioDate() {
		
		reInitDefinationPartAndSubTitle();
		
		mStatue = STATUE_LOADING;
		mSeekBar.setEnabled(false);
		mSeekBar.setProgress(0);
		mTotalTimeTextView.setText("--:--");
		mPreLoadLayout.setVisibility(View.VISIBLE);
		mNoticeLayout.setVisibility(View.VISIBLE);
		mContinueLayout.setVisibility(View.GONE);
		mControlLayout.setVisibility(View.GONE);
		mDateLoadingLayout.setVisibility(View.GONE);
		mStartRX = TrafficStats.getTotalRxBytes();// 获取网络速度
		if (mStartRX == TrafficStats.UNSUPPORTED) {
			mSpeedTextView
					.setText("Your device does not support traffic stat monitoring.");
		} else {
			mHandler.postDelayed(mLoadingRunnable, 500);
		}
		// 点击某部影片播放时，会全局设置CurrentPlayData
		CurrentPlayDetailData playDate = app.getmCurrentPlayDetailData();
		if (playDate == null) {// 如果不设置就不播放
			Log.e(TAG, "playDate----->null");
			finish();
			return;
		}
		// 初始化基本播放数据
		mProd_id = playDate.prod_id;
		mProd_type = playDate.prod_type;
		mProd_name = playDate.prod_name;
		mProd_sub_name = playDate.prod_sub_name;
		currentPlayUrl = playDate.prod_url;
		url_temp = playDate.prod_url;
		mDefination = playDate.prod_qua;
		lastTime = (int) playDate.prod_time;
		mProd_src = playDate.prod_src;

		Log.d(TAG, "name ----->" + mProd_name);
		Log.d(TAG, "currentPlayUrl ----->" + currentPlayUrl);
		Log.d(TAG,"sourceFromUrl------->"+sourceFromUrl);
		
		if(mDefination == 0){
			mDefination = 8;
		}
		
		//记录点击次数
		if(mProd_id != null && !mProd_id.equals("")
				&& mProd_name != null && !mProd_name.equals("")){
			
			switch (mProd_type) {
			case 1:
				UtilTools.StatisticsClicksShow(aq, app, mProd_id, mProd_name, "", mProd_type);
				break;
			case 2:
			case 3:
			case 131:
				if(mProd_sub_name != null && !mProd_sub_name.equals("")){
					
					UtilTools.StatisticsClicksShow(aq, app, mProd_id, mProd_name, mProd_sub_name, mProd_type);
				}
				
				break;

			default:
				break;
			}
		}
		
		// 更新播放来源和上次播放时间
		updateSourceAndTime();
		updateName();
		if(lastTime<=0){
			lastTime = getPlayTimeFromLocal();
		}
		if (currentPlayUrl != null && URLUtil.isNetworkUrl(currentPlayUrl)) {
			if (mProd_type<0) {
				new Thread(new UrlRedirectTask()).start();
			} else {
				if (m_ReturnProgramView != null) {// 如果不为空，获取服务器返回的详细数据

					mHandler.sendEmptyMessage(MESSAGE_RETURN_DATE_OK);
				} else {// 如果为空，就重新获取

					getProgramViewDetailServiceData();
				}
			}
		} else {
			if (m_ReturnProgramView != null) {// 如果不为空，获取服务器返回的详细数据

				m_ReturnProgramView = app.get_ReturnProgramView();
				mHandler.sendEmptyMessage(MESSAGE_RETURN_DATE_OK);
			} else {// 如果为空，就重新获取

				getProgramViewDetailServiceData();
			}
		}
	}
	
	private long getPlayTimeFromLocal(){
		String lastTimeStr = DBUtils.getDuartion4HistoryDB(
				getApplicationContext(),
				UtilTools.getCurrentUserId(getApplicationContext()), mProd_id,mProd_sub_name);
		Log.i(TAG, "DBUtils.getDuartion4HistoryDB-->lastTimeStr:" + lastTimeStr);

		if (lastTimeStr != null && !lastTimeStr.equals("")) {

			try {
				long tempTime = Integer.valueOf(lastTimeStr);
				Log.i(TAG, "DBUtils.getDuartion4HistoryDB-->time:" + tempTime);
				if (tempTime != 0) {

					return tempTime * 1000;
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		return 0;
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_RETURN_DATE_OK:
				new Thread(new PrepareTask()).start();
				break;
			case MESSAGE_URLS_READY:// url 准备好了
				if(playUrls.size()<=0){
					if(!VideoPlayerJPActivity.this.isFinishing()){
						showDialog(0);
					}
					return;
				}
				currentPlayIndex = 0;
				currentPlayUrl = playUrls.get(currentPlayIndex).url;
				mProd_src = playUrls.get(currentPlayIndex).source_from;
				Log.i(TAG, "MESSAGE_URLS_READY--->" + currentPlayUrl + " isOnlyExistFengXing--->" + isOnlyExistFengXing
						+" sourceFromUrl--->" + sourceFromUrl);
				if (currentPlayUrl != null&& URLUtil.isNetworkUrl(currentPlayUrl)) {
					
					if(isOnlyExistFengXing && sourceFromUrl != null){//只存在风行的地址
						
						getTypeFengXing();
						if(getFlvTypeFengXing){
							String url = URLUtils.getFexingParseUrlURL(Constant.FENGXING_REGET_FIRST_URL, sourceFromUrl, mProd_id, mProd_sub_name);
							Log.d(TAG,"FENGXING-URL"+mProd_id+"mProd_sub_name"+ mProd_sub_name + "url = " + url);
							getFengxingParseServiceData(url);
						}else{
							new Thread(new UrlRedirectTask()).start();
						}
					}else {
						// 地址跳转相关。。。
						new Thread(new UrlRedirectTask()).start();
						// 要根据不同的节目做相应的处理。这里仅仅是为了验证上下集
					}

				}else {
					mHandler.sendEmptyMessage(MESSAGE_URL_NEXT);
				}
				break;
			case MESSAGE_URL_NEXT:
				if (playUrls.size() <= 0) {
					if (app.get_ReturnProgramView() != null) {
						m_ReturnProgramView = app.get_ReturnProgramView();
						mHandler.sendEmptyMessage(MESSAGE_RETURN_DATE_OK);
					} else {
						if (mProd_type > 0 && !"-1".equals(mProd_id)
								&& mProd_id != null) {
							getProgramViewDetailServiceData();
						}
					}
				} else {
					if (currentPlayIndex < playUrls.size() - 1) {
						currentPlayIndex += 1;
						currentPlayUrl = playUrls.get(currentPlayIndex).url;
						mProd_src = playUrls.get(currentPlayIndex).source_from;
						if (currentPlayUrl != null
								&& URLUtil.isNetworkUrl(currentPlayUrl)) {
							// 地址跳转相关。。。
							Log.d(TAG, "currentPlayUrl:" + currentPlayUrl + "  mProd_src-->" + mProd_src);
							new Thread(new UrlRedirectTask()).start();
						}else {
							mHandler.sendEmptyMessage(MESSAGE_URL_NEXT);
						}
					} else {
						// 所有的片源都不能播放
						Log.e(TAG, "no url can play!--->");
						
//						if(isOnlyExistLetv && m_ReturnProgramView != null
//								&& sourceFromUrl != null){
//							String url = URLUtils.
//									getParseUrlURL(Constant.LETV_PARSE_URL_URL, sourceFromUrl, mProd_id, mProd_sub_name);
//							Log.i(TAG, "sourceUrl--->" + sourceFromUrl + " url---->" + url);
//							getLetvParseServiceData(url);
//						}else {
							
							noUrlCanPlay();
//						}
					}
				}
				break;
			case MESSAGE_PALY_URL_OK:
				updateName();
				updateSourceAndTime();
				Log.d(TAG,"------------->url_ok currentPlayUrl"+currentPlayUrl);
				mVideoView.setVideoURI(Uri.parse(currentPlayUrl));
				
				if (lastTime > 0) {
					mVideoView.seekTo((int) lastTime);
				}
				mVideoView.start();
				
				if(reloadLetvCount == 0)
				postDelayed(new Runnable() {
					public void run() {
						if(mStatue != STATUE_LOADING) return;
						if(currentPlayIndex>=0&& currentPlayIndex<playUrls.size()){
							if(PlayerSourceType.TYPE_LETV.toSourceName().
									equals(playUrls.get(currentPlayIndex).source_from)
									||PlayerSourceType.TYPE_LE_TV_FEE.toSourceName().
									equals(playUrls.get(currentPlayIndex).source_from)){
								Log.i(TAG, "postDelayed--->" + playUrls.get(currentPlayIndex).source_from);
								mVideoView.stopPlayback();
							//	findViewById(R.id.tv_preload_source_reload).setVisibility(View.VISIBLE);
								reloadLetvCount ++;
							}
						}
					}
				}, 3 * 1000);
				break;
			case MESSAGE_UPDATE_PROGRESS:
				updateSeekBar();
				break;
			case MESSAGE_HIDE_PROGRESSBAR:
				dismissView(mNoticeLayout);
				break;
			case MESSAGE_HIDE_VOICE:
				dismissView(mVocieLayout);
				break;
			case MESSAGE_DATALOADING_UPDATE_NETSPEED:
				updateDataLoadingSpeed();
				break;
			default:
				break;
			}
		}
	};
	
	public long getPlayerCurrentPosition(){
		if(mVideoView == null) return 0;
		return mVideoView.getCurrentPosition();
	}
	
	
	private void getTypeFengXing(){
		String sourceQua = "flv";
		switch (mProd_type) {
		case 1:
			for (int i = 0; i < m_ReturnProgramView.movie.episodes[0].down_urls.length; i++) {
				for (int j = 0; j < m_ReturnProgramView.movie.episodes[0].down_urls[i].urls.length; j++) {
						if(m_ReturnProgramView.movie.episodes[0].down_urls[i].urls[j].type!= null) {
								if(sourceQua.equals(m_ReturnProgramView.movie.episodes[0].down_urls[i].urls[j].type)){
									getFlvTypeFengXing=true;	
								}
						}
					}
				}
			break;
		case 2:
		case 131:
			if(mEpisodeIndex>=0&&mEpisodeIndex<m_ReturnProgramView.tv.episodes.length){
				if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls != null) {
					for (int j = 0; j < m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls.length; j++) {
						if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j] != null) {
							String sources = m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].source;
							if( m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls != null) {
								for (int k = 0; k < m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls.length; k++) {
									if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls[k] != null) {
										if(sourceQua.equals(m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls[k].type)){
											getFlvTypeFengXing=true;	
										}
									}
								}
							}
						}
					}
				}
			}
			break;
		case 3:
			if(mEpisodeIndex>=0&&mEpisodeIndex<m_ReturnProgramView.show.episodes.length){
				for (int j = 0; j < m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls.length; j++) {
					if(m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j] != null) {
						String sources = m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].source;
						if(m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls != null) {
							for (int k = 0; k < m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls.length; k++) {
								if(m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls[k] != null) {
									if(sourceQua.equals(m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls[k].type)){
										getFlvTypeFengXing=true;	
									}
								}
							}
						}
					}

				}
			}
			break;
		}
		
		
		
	}
	
	private void getFengxingParseServiceData(String url){
		
		getParseServiceData(url, "initFengxingParseServiceData");
	}
	
	public void initFengxingParseServiceData(String url, JSONObject json, AjaxStatus status) {

		if (status.getCode() == AjaxStatus.NETWORK_ERROR || json == null) {
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));

			mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
			return;
		}

		if (json == null || json.toString().equals("")){
			mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
			return;
		}
		Log.d(TAG, "initFengxingParseServiceData= " + json.toString());
		ObjectMapper mapper = new ObjectMapper();
		try {
			ReturnFirstFengxingUrlView returnFirstFengxingUrlView = mapper.readValue(json.toString(),
					ReturnFirstFengxingUrlView.class);
			Log.d(TAG,"returnFirstFengxingUrlView"+returnFirstFengxingUrlView.down_urls);
			if(returnFirstFengxingUrlView != null){
				if(returnFirstFengxingUrlView.error != null && 
						returnFirstFengxingUrlView.error.equals("false")){
					String sourceQua = "";
					switch (mDefination) {
					case 8:
						sourceQua = "hd2";
						break;
					case 7:
						sourceQua = "mp4";
						break;
					case 6:
						sourceQua = "flv";
						break;
					}
					Log.i(TAG, "mDefination--->" + mDefination);
					
					
					
//					if(returnFirstFengxingUrlView.video_infos != null &&
//							returnFirstFengxingUrlView.video_infos.length > 0){
//						for(int i=0;i<returnFirstFengxingUrlView.video_infos.length;i++){
//							if(returnFirstFengxingUrlView.video_infos[i]!= null 
//									&& returnFirstFengxingUrlView.video_infos[i].type != null){
//								Log.i(TAG, "sourceQua--->" + sourceQua + " type:" + returnFirstFengxingUrlView.video_infos[i].type);
//								if(sourceQua.equals(returnFirstFengxingUrlView.video_infos[i].type)){
//									
//									String tempUrl = returnFirstFengxingUrlView.video_infos[i].request_url;
//									Log.i(TAG, "tempUrl--->" + tempUrl);
//									getFenxingNetServiceData(tempUrl);
//									return;
//								}
//							}
//						}
//					Log.d(TAG,"invideo_infos");
//					
//					}
					if(returnFirstFengxingUrlView != null && returnFirstFengxingUrlView.error!= null
							&& returnFirstFengxingUrlView.error.equals("false")){
		//				Log.d(TAG,"invideo_infos_length"+returnFirstFengxingUrlView.video_infos.length+"urls"+returnFirstFengxingUrlView.video_infos);
						if(returnFirstFengxingUrlView.down_urls != null && returnFirstFengxingUrlView.down_urls.urls.length > 0){
							
							String type = defintionToType(mDefination);
							if(playUrls != null){
								
								playUrls.clear();
							}
							Log.d(TAG,"playurls"+playUrls+"type"+type);
							for(int i=0;i<returnFirstFengxingUrlView.down_urls.urls.length;i++){
								URLS_INDEX urls_INDEX = new URLS_INDEX();
								urls_INDEX.source_from = PlayerSourceType.TYPE_FENGXING.toSourceName();
								urls_INDEX.defination_from_server = type;
								urls_INDEX.url = returnFirstFengxingUrlView.down_urls.urls[i].url;
								urls_INDEX.webUrl = sourceFromUrl;
								Log.i(TAG, "urls_INDEX--->" + urls_INDEX.toString());
								playUrls.add(urls_INDEX);
							//	Log.d(TAG,"playurls.urls_index"+urls_INDEX);
							}
						}
					//	Log.d(TAG,"invideo_infos");
					}
				//	Log.d(TAG,"OUTVIDEO_INFOS");

				}
				
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
			mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
	}
	
	private String defintionToType(int defintion){
		String sourceQua = "";
		switch (defintion) {
		case 8:
			sourceQua = "hd2";
			break;
		case 7:
			sourceQua = "mp4";
			break;
		case 6:
			sourceQua = "flv";
			break;
		}
		return sourceQua;
	}
	
	private void noUrlCanPlay(){
		
		if(hasP2p && !isRetry){
			isRetry = true;
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
//					urlArrange();??
					p2pPreArrange();
					
					playUrlsPreArrange();
					initDefinitionUrls();
					sortList();
					mHandler.sendEmptyMessage(MESSAGE_URLS_READY);
				}
			}).start();
		}else{
			if(!VideoPlayerJPActivity.this.isFinishing()){
				showDialog(0);
				
				//所有url不能播放，向服务器传递-1
				saveToServer(-1, 0);
			}
		}
	}
	
	private void getFenxingNetServiceData(String url){
		
		getParseServiceData(url, "initFenxingNetServiceData");
	}
	
	public void initFenxingNetServiceData(String url, JSONObject json, AjaxStatus status) {

		if (status.getCode() == AjaxStatus.NETWORK_ERROR || json == null) {
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
			return;
		}

		if (json == null || json.toString().equals("")){
			mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
			return;
		}

		Log.d(TAG, "initFenxingNetServiceData = " + json.toString());
		getFengxingSecondServiceData(Constant.FENGXING_REGET_SECOND_URL, json);
		
	}
	
	private void getFengxingSecondServiceData(String url,JSONObject json){
		
		getParseServiceData(url,json, "initFengxingSecondServiceData");
	}
	
	protected void getParseServiceData(String url,JSONObject json, String interfaceName) {
		// TODO Auto-generated method stub

		Log.d(TAG,"--------->json"+json);
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, interfaceName);
		Map<String, Object> params = new HashMap<String, Object>();
		try {
			HttpEntity entity = new StringEntity(json.toString());
			params.put(AQuery.POST_ENTITY, entity);

			cb.params(params);
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "application/json");
//			headers.put("app_key", Constant.APPKEY);
			headers.putAll(app.getHeaders());
			cb.SetHeader(headers);
			aq.ajax(cb);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
;
	}
	
	public void initFengxingSecondServiceData(String url, JSONObject json, AjaxStatus status) {

		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
			return;
		}
		
		if (json == null || json.toString().equals("")){
			Log.d(TAG, "initFengxingSecondServiceData = ");
			mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
			return;
		}

		Log.d(TAG, "initFengxingSecondServiceData = " + json.toString());
		ObjectMapper mapper = new ObjectMapper();
		try {
			ReturnFengxingSecondView returnFengxingSecondView = mapper.readValue(json.toString(),
					ReturnFengxingSecondView.class);
			
			if(returnFengxingSecondView != null && returnFengxingSecondView.error!= null
					&& returnFengxingSecondView.error.equals("false")){
				
				if(returnFengxingSecondView.urls != null && returnFengxingSecondView.urls.length > 0){
					
					String type = defintionToType(mDefination);
					if(playUrls != null){
						
						playUrls.clear();
					}
					for(int i=0;i<returnFengxingSecondView.urls.length;i++){
						
						URLS_INDEX urls_INDEX = new URLS_INDEX();
						urls_INDEX.source_from = PlayerSourceType.TYPE_FENGXING.toSourceName();
						urls_INDEX.defination_from_server = type;
						urls_INDEX.url = returnFengxingSecondView.urls[i];
						urls_INDEX.webUrl = sourceFromUrl;
						Log.i(TAG, "urls_INDEX--->" + urls_INDEX.toString());
						playUrls.add(urls_INDEX);
					}
				}
			}
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
			
			mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
	}
	
	protected void getParseServiceData(String url, String interfaceName) {
		// TODO Auto-generated method stub

		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, interfaceName);

		if(!"initFenxingNetServiceData".equals(interfaceName)){
			
//			Map<String, String> headers = new HashMap<String, String>();
//			headers.put("app_key", Constant.APPKEY);
			cb.SetHeader(app.getHeaders());
		}
		aq.ajax(cb);
	}
	
	private void getLetvParseServiceData(String url){
		
		getParseServiceData(url, "initLetvParseServiceData");
	}
	
	public void initLetvParseServiceData(String url, JSONObject json, AjaxStatus status) {

		if (status.getCode() == AjaxStatus.NETWORK_ERROR || json == null) {
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			noUrlCanPlay();
			return;
		}

		if (json == null || json.equals("")){
			
			noUrlCanPlay();
			return;
		}


		Log.d(TAG, "initLetvParseServiceData = " + json.toString());
		ObjectMapper mapper = new ObjectMapper();
		try {
			ReturnReGetVideoView reGetVideoView = mapper.readValue(json.toString(),
					ReturnReGetVideoView.class);
			
			
			if(reGetVideoView != null && "false".equals(reGetVideoView.error)
					&&reGetVideoView.down_urls != null
					&& reGetVideoView.down_urls.urls != null
					&& reGetVideoView.down_urls.urls.length > 0){
				if(playUrls != null){
					
					playUrls.clear();
				}
				for(int i=0;i<reGetVideoView.down_urls.urls.length ;i++){
					
					if(reGetVideoView.down_urls.urls != null){
						
						URLS_INDEX urls_INDEX = new URLS_INDEX(); 
						urls_INDEX.source_from = reGetVideoView.down_urls.source;
						urls_INDEX.defination_from_server = reGetVideoView.down_urls.urls[i].type;
						urls_INDEX.url = reGetVideoView.down_urls.urls[i].url;
						urls_INDEX.webUrl = sourceFromUrl;
						Log.i(TAG, "urls_INDEX--->" + urls_INDEX.toString());
						playUrls.add(urls_INDEX);
					}
				}
				
				if(maxQuality != -1 && isOnlyExistLetv){
					
					if(maxQuality == mDefination){
						
						mDefination = 8;
					}
				}
				
//				urlArrange();??
				playUrlsPreArrange();
				initDefinitionUrls();
				sortList();
				
				for(int i=0;i<playUrls.size();i++){
					
					Log.i(TAG, "playUrls--->" + playUrls.get(i).defination_from_server);
				}
				// url list 准备完成
				sourceFromUrl = null;
				mHandler.sendEmptyMessage(MESSAGE_URLS_READY);
				
				return;
				
			}
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
		
		noUrlCanPlay();
	}
	
	private void updateDataLoadingSpeed(){
		long speed = getLoadingData() - mCurrentLoadingbytes;
		mDataLoadingSpeedText.setText("（" + speed + "kb/s）");
		mCurrentLoadingbytes += speed;
		mHandler.sendEmptyMessageDelayed(MESSAGE_DATALOADING_UPDATE_NETSPEED, 1000);
	}
	
	private long getLoadingData(){
		ApplicationInfo ai = getApplicationInfo();
//		return TrafficStats.getUidRxBytes(ai.uid) == TrafficStats.UNSUPPORTED ? 0
//				: (TrafficStats.getTotalRxBytes() / 1024);
		return TrafficStats.getTotalRxBytes() / 1024;
	}

	private void updateName() {
		switch (mProd_type) {
		case -1:
		case 1:
			mVideoNameText.setText(mProd_name);
			break;
		case 2:
		case 131:
			mVideoNameText.setText(getString(R.string.utils_prodname_prodSubName, mProd_name,mProd_sub_name));
			break;
		case 3:
			mVideoNameText.setText(mProd_name + " " + mProd_sub_name);
			break;
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		
		Log.i(TAG, "onStart--->");
		
		super.onStart();
		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		mMaxVolume = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}

	private void onVolumeSlide(int index) {
		if (index > mMaxVolume)
			index = mMaxVolume;
		else if (index < 0)
			index = 0;

		// 变更声音
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
		int mAngle = index * 360 / mMaxVolume;
		// 变更进度条
		if (index == 0)
			mVoiceProgress.setBackgroundResource(R.drawable.player_volume_mute);
		else {
			mVoiceProgress.setBackgroundResource(R.drawable.player_volume);

		}
		mVoiceProgress.SetAngle(mAngle);

	}

	private void initViews() {
		mVideoNameText = (TextView) findViewById(R.id.tv_play_name);
		mDefinationIcon = (ImageView) findViewById(R.id.iv_1080_720);
		mSeekBar = (SeekBar) findViewById(R.id.sb_seekbar);
		mTimeLayout = (RelativeLayout) findViewById(R.id.rl_popup_time);
		mCurrentTimeTextView = (TextView) findViewById(R.id.tv_popup_time_current_time);
		mTotalTimeTextView = (TextView) findViewById(R.id.tv_total_time);
		mFastIcon = (RelativeLayout) findViewById(R.id.rl_popup_time_fast);
		mFastTextView = (TextView) findViewById(R.id.tv_popup_time_fast);

		mLastTimeTextView = (TextView) findViewById(R.id.tv_preload_bofang_record);
		mResourceTextView = (TextView) findViewById(R.id.tv_preload_source_laizi);// 视频来源
		mSpeedTextView = (TextView) findViewById(R.id.tv_preload_network_kb);
		mPercentTextView = (TextView) findViewById(R.id.tv_preload_network_accomplish);

		mPreButton = (ImageButton) findViewById(R.id.ib_control_left);
		mNextButton = (ImageButton) findViewById(R.id.ib_control_right);
		mTopButton = (ImageButton) findViewById(R.id.ib_control_top);
		mBottomButton = (ImageButton) findViewById(R.id.ib_control_bottom);
		mCenterButton = (ImageButton) findViewById(R.id.ib_control_center);
		mContinueButton = (ImageButton) findViewById(R.id.btn_continue);
		
		mSubTitleView = (SubTitleView) findViewById(R.id.tv_subtitle);
		
		mSubTitleView.Init(this);
		
		mDataLoadingSpeedText = (TextView) findViewById(R.id.tv_dataloading_network_kb);

		mPreButton.setOnClickListener(this);
		mNextButton.setOnClickListener(this);
		mTopButton.setOnClickListener(this);
		mBottomButton.setOnClickListener(this);
		mCenterButton.setOnClickListener(this);
		mContinueButton.setOnClickListener(this);

		mVoiceProgress = (ArcView) findViewById(R.id.av_volume);

		mPreLoadLayout = (RelativeLayout) findViewById(R.id.rl_preload);
		BitmapFactory.Options opt = new BitmapFactory.Options();
		// opt.inPreferredConfig = Bitmap.Config.RGB_565; // Each pixel is
		// stored 2 bytes
		// opt.inPreferredConfig = Bitmap.Config.ARGB_8888; //Each pixel is
		// stored 4 bytes

		opt.inTempStorage = new byte[16 * 1024];
		opt.inPurgeable = true;
		opt.inInputShareable = true;

		try {
			mPreLoadLayout.setBackgroundDrawable(new BitmapDrawable(BitmapFactory.decodeResource(
					getResources(), R.drawable.player_bg, opt)));
		} catch (OutOfMemoryError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		mNoticeLayout = (RelativeLayout) findViewById(R.id.rl_titile_seekbar);
		mControlLayout = (LinearLayout) findViewById(R.id.ll_control_buttons);
		mVocieLayout = (LinearLayout) findViewById(R.id.ll_volume);
		mContinueLayout = (LinearLayout) findViewById(R.id.ll_continue);
		mDateLoadingLayout = (LinearLayout) findViewById(R.id.ll_data_loading);
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVideoView.setOnErrorListener(this);
		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnPreparedListener(this);
//		mVideoView.setOnInfoListener(this);
		mVideoView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(mStatue == STATUE_PLAYING){
					mHandler.removeMessages(MESSAGE_HIDE_PROGRESSBAR);
					mNoticeLayout.setVisibility(View.VISIBLE);
					mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_PROGRESSBAR, 2500);	
				}
				return false;
			}
		});
	}
	
	private void reloadLetv(){
		findViewById(R.id.tv_preload_source_reload).setVisibility(View.GONE);
		//1.获取原始地址
		String webUrl = playUrls.get(currentPlayIndex).webUrl;
		String url = URLUtils.getReloadLetv(Constant.CLICK_LETV_PARSE_URL_URL,webUrl,mProd_id,mProd_sub_name);
		Log.i(TAG, "reloadLetv url--->" + url);
//		//2.服务器请求获取新地址
		RequestAQueryManager.getInstance().getRequest(VideoPlayerJPActivity.this, url, app.getHeaders(), aq, "initOnlineParseLetvData");
//		//3.清掉letv播放地址
	}
	
	public void initOnlineParseLetvData(String url, JSONObject json,
			AjaxStatus status) {
		if (status.getCode() == AjaxStatus.NETWORK_ERROR 
				|| json == null || json.toString().equals("")) {
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
			return;
		}
		Log.d(TAG, "initOnlineParseLetvData--->" + json.toString());
		try {
			if(json.has("error") && !json.getBoolean("error")){
				String type = defintionToType(mDefination);
				if(json.has("down_urls")){
					JSONObject downUrls = json.getJSONObject("down_urls");
					if(downUrls != null && downUrls.has("urls")){
						JSONArray urlArray = downUrls.getJSONArray("urls");
						if(urlArray != null && urlArray.length() > 0){
							for(int i=0;i<urlArray.length();i++){
								JSONObject urlObj = urlArray.getJSONObject(i);
								if(urlObj != null && urlObj.has("type")
										&& type.equals(urlObj.get("type"))){
									String requestUrl = urlObj.getString("url");
									if(!TextUtils.isEmpty(requestUrl)){
										RequestAQueryManager.getInstance().getRequest(VideoPlayerJPActivity.this, requestUrl, null, aq, "initRequestLetvData");
										return;
									}
									
								}
							}
						}
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
	}
	
	public void initRequestLetvData(String url, JSONObject json,
			AjaxStatus status) {
		if (status.getCode() == AjaxStatus.NETWORK_ERROR 
				|| json == null || json.toString().equals("")) {
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
			return;
		}
		Log.d(TAG, "initRequestLetvData--->" + json.toString());
		if(json.has("location")){
			try {
				String location = json.getString("location");
				Log.i(TAG, "initRequestLetvData--->location:" + location);
				if(!TextUtils.isEmpty(location)){
					if(currentPlayIndex >= 0&& currentPlayIndex< playUrls.size()){
						playUrls.get(currentPlayIndex).url = location;
						currentPlayUrl = location;
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.d(TAG, "keycode ---------->" + keyCode);
		Log.d(TAG, "mStatue ---------->" + mStatue);
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_ESCAPE:
			switch (mStatue) {
			case STATUE_LOADING:
				finish();
				return true;
			case STATUE_PLAYING:
				if (mProd_type == 2 || mProd_type == 131 || mProd_type == 3) {
					mDateLoadingLayout.setVisibility(View.GONE);
					showControlLayout();
					return true;
				} else {
					// mVideoView.stopPlayback();
					setResult2Xiangqing();
					finish();
				}
				break;
			case STATUE_PAUSE:
				return true;
			case STATUE_FAST_DRAG:
				mTimeJumpSpeed = 0;
				upDateFastTimeBar();
//				mHandler.removeMessages(MESSAGE_UPDATE_PROGRESS);
//				mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, 1000);
				endUpdateSeekBar();
				startUpdateSeekBar(SEEKBAR_REFRESH_TIME);
				mStatue = STATUE_PLAYING;
				mSeekBar.setProgress(mVideoView.getCurrentPosition());
				mSeekBar.setEnabled(true);
				return true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			switch (mStatue) {
			case STATUE_LOADING:
				if(reloadLetvCount == 1){
					reloadLetvCount ++;
				//	reloadLetv();
				}
				break;
			case STATUE_PLAYING:
				/*
				 * 显示banner
				 */
				showBanner();
				
				layout.setVisibility(View.VISIBLE);
				if(!(mDateLoadingLayout.getVisibility()==View.VISIBLE)){
					mVocieLayout.setVisibility(View.GONE);
					mHandler.removeMessages(MESSAGE_HIDE_VOICE);
					mStatue = STATUE_PAUSE;
					mSeekBar.setEnabled(false);
					mVideoView.pause();
					mHandler.removeMessages(MESSAGE_HIDE_PROGRESSBAR);
					mContinueLayout.setVisibility(View.VISIBLE);
					mNoticeLayout.setVisibility(View.VISIBLE);
					mContinueButton.requestFocus();
				}
				break;
			case STATUE_FAST_DRAG:
				if (mFastJumpTime < mVideoView.getDuration()) {
					mVideoView.seekTo(mFastJumpTime);
					mSeekBar.setProgress(mFastJumpTime);
				}else{
					mSeekBar.setProgress(mVideoView.getCurrentPosition());
				}
				mTimeJumpSpeed = 0;
				upDateFastTimeBar();
//				mHandler.removeMessages(MESSAGE_UPDATE_PROGRESS);
//				mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, 1000);
				endUpdateSeekBar();
				startUpdateSeekBar(SEEKBAR_REFRESH_TIME);
				mStatue = STATUE_PLAYING;
				mSeekBar.setEnabled(true);
				break;
			}
			break;
		case KeyEvent.KEYCODE_MENU:
			if(mDateLoadingLayout.getVisibility()!=View.VISIBLE && mStatue == STATUE_PLAYING){
				try{
					final Dialog dialog = new AlertDialog.Builder(this).create();
					dialog.show();
					LayoutInflater inflater = LayoutInflater.from(this);
					View view = inflater.inflate(R.layout.video_choose_defination, null);
					Button btn_ok = (Button) view.findViewById(R.id.btn_ok_def);
					Button btn_cancel = (Button) view.findViewById(R.id.btn_cancle_def);
					final Gallery gallery = (Gallery) view.findViewById(R.id.gallery_def);
					final Gallery gallery_zm = (Gallery) view.findViewById(R.id.gallery_zimu);
					
					definationStrings.clear();
					zimuStrings.clear();
					
					if(mJoyplusSubManager.getSubList().size() == 0){
						zimuStrings.add(-1);//暂无字幕
					}else{
						for(int i=0; i<=mJoyplusSubManager.getSubList().size(); i++)
							zimuStrings.add(i);
					}
					
					if(playUrls_hd2.size()>0){
						definationStrings.add(Constant.DEFINATION_HD2);
					}
					
					if(playUrls_mp4.size()>0){
						definationStrings.add(Constant.DEFINATION_MP4);
					}
					if(playUrls_flv.size()>0){
						definationStrings.add(Constant.DEFINATION_FLV);
					}
					
					gallery.setAdapter(new DefinationAdapter(this, definationStrings));
					gallery.setSelection(definationStrings.indexOf(mDefination));
					gallery_zm.setAdapter(new ZimuAdapter(this, zimuStrings));
					
					if(!mJoyplusSubManager.CheckSubAviable()){
						gallery_zm.setSelection(0);
					}else{
						if(zimuStrings.size()==1&&zimuStrings.get(0)==-1){
							gallery_zm.setSelection(0);
						}else{
							if(mSubTitleView.getVisibility() == View.INVISIBLE){
								gallery_zm.setSelection(0);
							}else{
								gallery_zm.setSelection(mJoyplusSubManager.getCurrentSubIndex() + 1);
							}
						}
					}
					
					gallery.requestFocus();
					btn_ok.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							dialog.dismiss();
							if(gallery_zm.getChildCount()>1){
								if(gallery_zm.getSelectedItemPosition()==0){
									mSubTitleView.hiddenSubtitle();
								}else{
									if((gallery_zm.getSelectedItemPosition()!=0 && mSubTitleView.getVisibility() == View.INVISIBLE)||
											mJoyplusSubManager.getCurrentSubIndex() + 1 !=  gallery_zm.getSelectedItemPosition()){
										final int selection = gallery_zm.getSelectedItemPosition();
										new Thread(new Runnable() {
											
											@Override
											public void run() {
												// TODO Auto-generated method stub
												mJoyplusSubManager.SwitchSub(selection -1);
												mSubTitleView.displaySubtitle();
											}
										}).start();
									}
								}
							}
							changeDefination(definationStrings.get(gallery.getSelectedItemPosition()));
						}
					});
					btn_cancel.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					});
					dialog.setContentView(view);
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}	
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (mStatue == STATUE_PLAYING) {
				mHandler.removeMessages(MESSAGE_HIDE_VOICE);
				mVolume = mAudioManager
						.getStreamVolume(AudioManager.STREAM_MUSIC);
				if (mVolume < 0) {
					mVolume = 0;
				}
				mVocieLayout.setVisibility(View.VISIBLE);
				mVolume++;
				onVolumeSlide(mVolume);
				mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_VOICE, 2500);
			}
			return true;
		case KeyEvent.KEYCODE_DPAD_UP:
			if (mStatue == STATUE_PLAYING) {
				mHandler.removeMessages(MESSAGE_HIDE_VOICE);
				mVolume = mAudioManager
						.getStreamVolume(AudioManager.STREAM_MUSIC);
				if (mVolume < 0) {
					mVolume = 0;
				}
				mVocieLayout.setVisibility(View.VISIBLE);
				mVolume++;
				onVolumeSlide(mVolume);
				mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_VOICE, 2500);
				return true;
			}
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (mStatue == STATUE_PLAYING) {
				mHandler.removeMessages(MESSAGE_HIDE_VOICE);
				mVolume = mAudioManager
						.getStreamVolume(AudioManager.STREAM_MUSIC);
				if (mVolume < 0) {
					mVolume = 0;
				}
				mVocieLayout.setVisibility(View.VISIBLE);
				mVolume--;
				onVolumeSlide(mVolume);
				mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_VOICE, 2500);
			}
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if (mStatue == STATUE_PLAYING) {
				mHandler.removeMessages(MESSAGE_HIDE_VOICE);
				mVolume = mAudioManager
						.getStreamVolume(AudioManager.STREAM_MUSIC);
				if (mVolume < 0) {
					mVolume = 0;
				}
				mVocieLayout.setVisibility(View.VISIBLE);
				mVolume--;
				onVolumeSlide(mVolume);
				mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_VOICE, 2500);
				return true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (mStatue == STATUE_PLAYING) {
				mStatue = STATUE_FAST_DRAG;
				mSeekBar.setEnabled(false);
				mTimeJumpSpeed = -1;
				mFastJumpTime = (int) mVideoView.getCurrentPosition();
				upDateFastTimeBar();
				return true;
			} else if (mStatue == STATUE_FAST_DRAG) {
				switch (mTimeJumpSpeed) {
				case -1:
				case -2:
					mTimeJumpSpeed -= 1;
					break;
				case 1:
					mTimeJumpSpeed = -1;
					break;
				case 2:
				case 3:
					mTimeJumpSpeed = 1;
				}
				upDateFastTimeBar();
				return true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (mStatue == STATUE_PLAYING) {
				mStatue = STATUE_FAST_DRAG;
				mSeekBar.setEnabled(false);
				mTimeJumpSpeed = 1;
				mFastJumpTime = (int) mVideoView.getCurrentPosition();
				upDateFastTimeBar();
				return true;
			} else if (mStatue == STATUE_FAST_DRAG) {
				switch (mTimeJumpSpeed) {
				case 1:
				case 2:
					mTimeJumpSpeed += 1;
					break;
				case -1:
					mTimeJumpSpeed = 1;
					break;
				case -2:
				case -3:
					mTimeJumpSpeed = -1;
				}
				upDateFastTimeBar();
				return true;
			}
			break;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void startUpdateSeekBar(long time){
		mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, time);
	}
	
	private void endUpdateSeekBar(){
		mHandler.removeMessages(MESSAGE_UPDATE_PROGRESS);
	}
	
	private void changeDefination(int defination){
		if(mDefination == defination){
			return ;
		}
		lastTime = mVideoView.getCurrentPosition();
		rxByteslast = 0;
		mLoadingPreparedPercent = 0;
		reloadLetvCount = 0;
		mEpisodeIndex = -1;
		mPercentTextView.setText(
				getString(R.string.utils_percent_network,mLoadingPreparedPercent / 100 + ""));
		
		mDefination = defination;
		mVideoView.stopPlayback();
		mStatue = STATUE_LOADING;
		mDateLoadingLayout.setVisibility(View.GONE);
		mSeekBar.setEnabled(false);
		mSeekBar.setProgress(0);
		mTotalTimeTextView.setText("--:--");
		mPreLoadLayout.setVisibility(View.VISIBLE);
		mNoticeLayout.setVisibility(View.VISIBLE);
		mContinueLayout.setVisibility(View.GONE);
		mControlLayout.setVisibility(View.GONE);
		mStartRX = TrafficStats.getTotalRxBytes();// 获取网络速度
		if (mStartRX == TrafficStats.UNSUPPORTED) {
			mSpeedTextView
					.setText("Your device does not support traffic stat monitoring.");
		} else {

			mHandler.postDelayed(mLoadingRunnable, 500);
		}
		sortPushUrls(mDefination);
		mHandler.sendEmptyMessage(MESSAGE_URLS_READY);
	}
	
	private void sortPushUrls(int defination){
		
		for(URLS_INDEX url_index_info:playUrls){
			switch (defination) {
			case Constant.DEFINATION_HD2:
				if("hd2".equalsIgnoreCase(url_index_info.defination_from_server)){
					url_index_info.defination = 0;
				}else if("hd".equalsIgnoreCase(url_index_info.defination_from_server)){
					url_index_info.defination = 1;
				}else if("mp4".equalsIgnoreCase(url_index_info.defination_from_server)){
					url_index_info.defination = 2;
				}else if("flv".equalsIgnoreCase(url_index_info.defination_from_server)){
					url_index_info.defination = 3;
				}else{
					url_index_info.defination = 4;
				}
				break;
			case Constant.DEFINATION_MP4:
				if("hd2".equalsIgnoreCase(url_index_info.defination_from_server)){
					url_index_info.defination = 1;
				}else if("hd".equalsIgnoreCase(url_index_info.defination_from_server)){
					url_index_info.defination = 2;
				}else if("mp4".equalsIgnoreCase(url_index_info.defination_from_server)){
					url_index_info.defination = 0;
				}else if("flv".equalsIgnoreCase(url_index_info.defination_from_server)){
					url_index_info.defination = 3;
				}else{
					url_index_info.defination = 4;
				}
				break;
			case Constant.DEFINATION_FLV:
				if("hd2".equalsIgnoreCase(url_index_info.defination_from_server)){
					url_index_info.defination = 1;
				}else if("hd".equalsIgnoreCase(url_index_info.defination_from_server)){
					url_index_info.defination = 2;
				}else if("mp4".equalsIgnoreCase(url_index_info.defination_from_server)){
					url_index_info.defination = 3;
				}else if("flv".equalsIgnoreCase(url_index_info.defination_from_server)){
					url_index_info.defination = 0;
				}else{
					url_index_info.defination = 4;
				}
				break;
			default:
				break;
			}
		}
		if(playUrls.size()>1){
			Collections.sort(playUrls, new DefinationComparatorIndex());
		}
	}

	private void showControlLayout() {
		// 判断上下集能不能用
		Log.d(TAG, "mEpisodeIndex----->" + mEpisodeIndex);
		if (mProd_type == 3) {
			if (mEpisodeIndex > 0&&m_ReturnProgramView.show.episodes[mEpisodeIndex-1].down_urls!=null) {
				mNextButton.setEnabled(true);
				mNextButton.setFocusable(true);
			} else {
				mNextButton.setEnabled(false);
				mNextButton.setFocusable(false);
			}

			if (mEpisodeIndex < (m_ReturnProgramView.show.episodes.length - 1)&&m_ReturnProgramView.show.episodes[mEpisodeIndex+1].down_urls!=null) {
				mPreButton.setEnabled(true);
				mPreButton.setFocusable(true);
			} else {
				mPreButton.setEnabled(false);
				mPreButton.setFocusable(false);
			}

		} else {
			if (mEpisodeIndex > 0&&m_ReturnProgramView.tv.episodes[mEpisodeIndex-1].down_urls!=null) {
				mPreButton.setEnabled(true);
				mPreButton.setFocusable(true);
			} else {
				mPreButton.setEnabled(false);
				mPreButton.setFocusable(false);
			}

			if (mEpisodeIndex < (m_ReturnProgramView.tv.episodes.length - 1)&&m_ReturnProgramView.tv.episodes[mEpisodeIndex+1].down_urls!=null) {
				mNextButton.setEnabled(true);
				mNextButton.setFocusable(true);
			} else {
				mNextButton.setEnabled(false);
				mNextButton.setFocusable(false);
			}
		}

		if (isShoucang) {

			mBottomButton.setBackgroundResource(R.drawable.player_btn_unfav);
		} else {

			mBottomButton.setBackgroundResource(R.drawable.player_btn_fav);
		}
		
		/*
		 * 显示banner
		 */
		showBanner();
		layout.setVisibility(View.VISIBLE);
		
		mVocieLayout.setVisibility(View.GONE);
		mHandler.removeMessages(MESSAGE_HIDE_VOICE);
		mStatue = STATUE_PAUSE;
		mSeekBar.setEnabled(false);
		mVideoView.pause();
		mHandler.removeMessages(MESSAGE_HIDE_PROGRESSBAR);
		mControlLayout.setVisibility(View.VISIBLE);
		mNoticeLayout.setVisibility(View.VISIBLE);
		mCenterButton.requestFocus();
	}

	private void upDateFastTimeBar() {
		if (mTimeJumpSpeed > 0) {
			mFastIcon.setVisibility(View.VISIBLE);
			mFastIcon.setBackgroundResource(R.drawable.play_time_right);
			mFastTextView.setText("x" + Math.abs(mTimeJumpSpeed));
			mHandler.removeMessages(MESSAGE_HIDE_PROGRESSBAR);
			mNoticeLayout.setVisibility(View.VISIBLE);
		} else if (mTimeJumpSpeed < 0) {
			mFastIcon.setVisibility(View.VISIBLE);
			mFastIcon.setBackgroundResource(R.drawable.play_time_left);
			mFastTextView.setText("x" + Math.abs(mTimeJumpSpeed));
			mHandler.removeMessages(MESSAGE_HIDE_PROGRESSBAR);
			mNoticeLayout.setVisibility(View.VISIBLE);
		} else if (mTimeJumpSpeed == 0) {
			mFastIcon.setVisibility(View.GONE);
			mFastTextView.setText("");
			mHandler.removeMessages(MESSAGE_HIDE_PROGRESSBAR);
			mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_PROGRESSBAR, 2500);
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		// 播放有问题 选下一个地址
		mHandler.sendEmptyMessage(MESSAGE_URL_NEXT);
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		// 播放完成
		autoPlayNext();
	}
	
	private void autoPlayNext(){
		switch (mProd_type) {
		case 1:
			finish();
			break;
		case 2:
		case 131:
			if(mEpisodeIndex<m_ReturnProgramView.tv.episodes.length-1){
				playNext();
			}else{
				setResult2Xiangqing();//返回集数和是否收藏
				finish();
			}
			break;
		case 3:
			if(mEpisodeIndex>0){
				playNext();
			}else{
				setResult2Xiangqing();//返回集数和是否收藏
				finish();
			}
			break;
		default:
			finish();
			break;
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		// 准备好了
		mTotalTimeTextView.setText(Utils.formatDuration(mVideoView
				.getDuration()));
		mSeekBar.setMax((int) mVideoView.getDuration());
		mSeekBar.setOnSeekBarChangeListener(VideoPlayerJPActivity.this);
		mSeekBar.setProgress((int) lastTime);
//		mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, 1000);
		startUpdateSeekBar(SEEKBAR_REFRESH_TIME);
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
		// 快进好了（拖动） 系统不支持？
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onInfo-------->what = " + what);
		Log.d(TAG, "onInfo-------->extra = " + extra);
		switch (what) {
		case 701:
			if(mStatue == STATUE_FAST_DRAG||mStatue == STATUE_PLAYING){
				mDateLoadingLayout.setVisibility(View.VISIBLE);
				mCurrentLoadingbytes = getLoadingData();
				if (mStartRX == TrafficStats.UNSUPPORTED) {
					mDataLoadingSpeedText.setText("");
				} else {
					mDataLoadingSpeedText.setText("（0kb/s）");
					mHandler.sendEmptyMessageDelayed(MESSAGE_DATALOADING_UPDATE_NETSPEED, 1000);
				}
			}
			//showDialog(1);
			break;
		case 702:
			//removeDialog(1);
			mDateLoadingLayout.setVisibility(View.GONE);
			mHandler.removeMessages(MESSAGE_DATALOADING_UPDATE_NETSPEED);
			break;

		default:
			break;
		}
		return true;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		// 缓冲进度
	}

	private void updateSeekBar() {
		switch (mStatue) {
		case STATUE_LOADING:
			long current = mVideoView.getCurrentPosition();// 当前进度
			long lastProgress = mSeekBar.getProgress();
//			Log.d(TAG, "loading --->" + current);
			// updateTimeNoticeView(mSeekBar.getProgress());
			if(current>lastProgress){
				hidePreLoad(); 
			}else{
				mSeekBar.setProgress((int) current);
				startUpdateSeekBar(SEEKBAR_REFRESH_TIME);
//				mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, 1000);
			}
			break;
		case STATUE_PLAYING:
			if(!isSeekBarIntoch){
				long current1 = mVideoView.getCurrentPosition();// 当前进度
				mSeekBar.setProgress((int) current1);
				// updateTimeNoticeView(mSeekBar.getProgress());
			}
//			mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, 1000);
			startUpdateSeekBar(SEEKBAR_REFRESH_TIME);
			break;
		case STATUE_FAST_DRAG:
			if (mTimeJumpSpeed > 0) {
				mFastJumpTime = (int) (mFastJumpTime + (mVideoView
						.getDuration() / 500));
			} else if (mTimeJumpSpeed < 0) {
				mFastJumpTime = (int) (mFastJumpTime - (mVideoView
						.getDuration() / 500));
			}

			if (mFastJumpTime > mVideoView.getDuration()) {
				mFastJumpTime = (int) mVideoView.getDuration();
			}
			if (mFastJumpTime < 0) {
				mFastJumpTime = 0;
			}
			mSeekBar.setProgress(mFastJumpTime);
			// updateTimeNoticeView(mSeekBar.getProgress());
//			mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS,
//					mTimes[Math.abs(mTimeJumpSpeed) - 1]);
			startUpdateSeekBar(mTimes[Math.abs(mTimeJumpSpeed) - 1]);
			break;
		default:
//			mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, 1000);
			startUpdateSeekBar(SEEKBAR_REFRESH_TIME);
			break;
		}
	}

	private void updateTimeNoticeView(int progress) {
		RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		parms.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

		double mLeft = (double) progress / mVideoView.getDuration()
				* (mSeekBar.getMeasuredWidth() - seekBarWidthOffset) + OFFSET;

		if (progress > 0)
			parms.leftMargin = (int) mLeft;
		else
			parms.leftMargin = OFFSET;
		parms.bottomMargin = (int) Utils.getStandardValue(getApplicationContext(), 30);
		mTimeLayout.setLayoutParams(parms);

		mCurrentTimeTextView.setText(Utils.formatDuration(progress));
		mCurrentTimeTextView.setVisibility(View.VISIBLE);
	}

	private final Runnable mLoadingRunnable = new Runnable() {
		long beginTimeMillis, timeTakenMillis, m_bitrate;

		public void run() {

			// long txBytes = TrafficStats.getTotalTxBytes()- mStartTX;
			// TX.setText(Long.toString(txBytes));
			long rxBytes = TrafficStats.getTotalRxBytes() - mStartRX;

			timeTakenMillis = System.currentTimeMillis() - beginTimeMillis;
			beginTimeMillis = System.currentTimeMillis();
			if(timeTakenMillis!=0){
				m_bitrate = ((rxBytes - rxByteslast) * 8 * 1000 / timeTakenMillis) / 8000;
				rxByteslast = rxBytes;

				mSpeedTextView.setText("（" + Long.toString(m_bitrate) + "kb/s）");
				mLoadingPreparedPercent = mLoadingPreparedPercent + m_bitrate;
				if (mLoadingPreparedPercent >= 100
						&& mLoadingPreparedPercent / 100 < 100)
					mPercentTextView.setText(
							getString(R.string.utils_percent_network,mLoadingPreparedPercent / 100 + ""));
			}
			mHandler.postDelayed(mLoadingRunnable, 500);
		}
	};

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		updateTimeNoticeView(mSeekBar.getProgress());
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		isSeekBarIntoch = true;
		mHandler.removeMessages(MESSAGE_HIDE_PROGRESSBAR);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		isSeekBarIntoch = false;
		mVideoView.seekTo(mSeekBar.getProgress());
		mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_PROGRESSBAR, 2500);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ib_control_top:
			dismissView(mControlLayout);
			mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_PROGRESSBAR, 2500);
			mStatue = STATUE_PLAYING;
			mSeekBar.setEnabled(true);
			mVideoView.requestFocus();
			mVideoView.start();
		
			removeBanner();
			if (layout != null)
				layout.setVisibility(View.GONE);

			break;
		case R.id.btn_continue:
			dismissView(mContinueLayout);
			mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_PROGRESSBAR, 2500);
			mStatue = STATUE_PLAYING;
			mSeekBar.setEnabled(true);
			mVideoView.requestFocus();
			mVideoView.start();
			removeBanner();
			if(layout!=null)
				layout.setVisibility(View.GONE);
			break;
		case R.id.ib_control_center:
			setResult2Xiangqing();
			finish();
			break;
		case R.id.ib_control_left:
			playPrevious();
			break;
		case R.id.ib_control_right:
			playNext();
			break;
		case R.id.ib_control_bottom:
			if (!isShoucang) {
				String url = Constant.BASE_URL + "program/favority";

				Map<String, Object> params = new HashMap<String, Object>();
				params.put("prod_id", mProd_id);

				AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
				cb.SetHeader(app.getHeaders());

				cb.params(params).url(url).type(JSONObject.class)
						.weakHandler(this, "favorityResult");
				aq.ajax(cb);
			} else {// 取消收藏
				String url = Constant.BASE_URL + "program/unfavority";

				Map<String, Object> params = new HashMap<String, Object>();
				params.put("prod_id", mProd_id);

				AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
				cb.SetHeader(app.getHeaders());

				cb.params(params).url(url).type(JSONObject.class)
						.weakHandler(this, "unfavorityResult");

				aq.ajax(cb);
			}
			break;
		default:
			break;
		}
	}

	private void showLoading() {
		removeBanner();
		if (layout != null)
			layout.setVisibility(View.GONE);
		
		mLoadingPreparedPercent = 0;
		rxByteslast = 0;
		mStartRX = TrafficStats.getTotalRxBytes();
		if (mStartRX == TrafficStats.UNSUPPORTED) {
			mSpeedTextView
					.setText("Your device does not support traffic stat monitoring.");
		} else {
			mHandler.postDelayed(mLoadingRunnable, 500);
		}
		mPercentTextView.setText(getString(R.string.utils_percent_network,"0"));
		mDateLoadingLayout.setVisibility(View.GONE);
		mPreLoadLayout.setVisibility(View.VISIBLE);
		mNoticeLayout.setVisibility(View.VISIBLE);
	}
// kong
	private void playNext() {
		// TODO Auto-generated method stub
		url_temp = null;
		isRetry = false;
		mStatue = STATUE_LOADING;
		mSeekBar.setProgress(0);
		mSeekBar.setEnabled(false);
		mTotalTimeTextView.setText("--:--");
		mHandler.removeCallbacksAndMessages(null);
		mControlLayout.setVisibility(View.GONE);
		lastTime = 0;
		reloadLetvCount = 0;
		mVideoView.stopPlayback();
		showLoading();
		if (mProd_type == 3) {
			mEpisodeIndex -= 1;
		} else {
			mEpisodeIndex += 1;
		}
		mHandler.sendEmptyMessage(MESSAGE_RETURN_DATE_OK);
		resetSubManager();
	}
	
	private void resetSubManager(){
		mJoyplusSubManager = new JoyplusSubManager(this);
		mSubTitleView.setSubManager(mJoyplusSubManager);
	}

	private void playPrevious() {
		// TODO Auto-generated method stub
		url_temp = null;
		isRetry = false;
		mStatue = STATUE_LOADING;
		mSeekBar.setProgress(0);
		mSeekBar.setEnabled(false);
		mTotalTimeTextView.setText("--:--");
		mHandler.removeCallbacksAndMessages(null);
		mControlLayout.setVisibility(View.GONE);
		reloadLetvCount = 0;
		lastTime = 0;
		mVideoView.stopPlayback();
		showLoading();
		if (mProd_type == 3) {
			mEpisodeIndex += 1;
		} else {
			mEpisodeIndex -= 1;
		}
		mHandler.sendEmptyMessage(MESSAGE_RETURN_DATE_OK);
	}

	protected void getServiceData(String url, String interfaceName) {
		// TODO Auto-generated method stub

		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, interfaceName);

		cb.SetHeader(app.getHeaders());

		Log.d(TAG, url);
		Log.d(TAG, "header appkey" + app.getHeaders().get("app_key"));

		aq.ajax(cb);
	}

	private void getProgramViewDetailServiceData() {
		// TODO Auto-generated method stub

		String url = Constant.BASE_URL + "program/view" + "?prod_id="
				+ mProd_id;
		getServiceData(url, "initMovieDate");
	}

	public void initMovieDate(String url, JSONObject json, AjaxStatus status) {

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
			m_ReturnProgramView = null;
			m_ReturnProgramView = mapper.readValue(json.toString(),
					ReturnProgramView.class);
			// 检测URL
			mHandler.sendEmptyMessage(MESSAGE_RETURN_DATE_OK);
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

	private void updateSourceAndTime() {
		Log.d(TAG, " ---- sre = " + mProd_src);
		if (mProd_src == null || mProd_src.length() == 1
				|| "null".equals(mProd_src)) {
			mResourceTextView.setText("");
		} else {
			String strSrc = "";
			if (mProd_src.equalsIgnoreCase(PlayerSourceType.TYPE_LE_TV_FEE.toSourceName()) 
					||mProd_src.equalsIgnoreCase(PlayerSourceType.TYPE_LETV.toSourceName())
					||mProd_src.equals(PlayerSourceType.TYPE_LETV_V2.toSourceName())
					||mProd_src.equals(PlayerSourceType.TYPE_LETV_V2_FEE.toSourceName())) {
				strSrc = getString(R.string.videoPlayerJPActivity_source_letv);
			} else if (mProd_src.equalsIgnoreCase(PlayerSourceType.TYPE_FENGXING.toSourceName())) {
				strSrc = getString(R.string.videoPlayerJPActivity_source_fengxing);
			} else if (mProd_src.equalsIgnoreCase(PlayerSourceType.TYPE_QIYI.toSourceName())) {
				strSrc = getString(R.string.videoPlayerJPActivity_source_qiyi);
			} else if (mProd_src.equalsIgnoreCase(PlayerSourceType.TYPE_YOUKU.toSourceName())) {
				strSrc = getString(R.string.videoPlayerJPActivity_source_youku);
			} else if (mProd_src.equalsIgnoreCase(PlayerSourceType.TYPE_SINAHD.toSourceName())) {
				strSrc = getString(R.string.videoPlayerJPActivity_source_sina);
			} else if (mProd_src.equalsIgnoreCase(PlayerSourceType.TYPE_SOHU.toSourceName())) {
				strSrc = getString(R.string.videoPlayerJPActivity_source_sohu);
			} else if (mProd_src.equalsIgnoreCase(PlayerSourceType.TYPE_QQ.toSourceName())) {
				strSrc = getString(R.string.videoPlayerJPActivity_source_qq);
			} else if (mProd_src.equalsIgnoreCase(PlayerSourceType.TYPE_PPTV.toSourceName())) {
				strSrc = getString(R.string.videoPlayerJPActivity_source_pptv);
			} else if (mProd_src.equalsIgnoreCase(PlayerSourceType.TYPE_M1905.toSourceName())) {
				strSrc = getString(R.string.videoPlayerJPActivity_source_m1905);
			} else if (mProd_src.equalsIgnoreCase(PlayerSourceType.TYPE_P2P.toSourceName())) {
				strSrc = getString(R.string.videoPlayerJPActivity_source_p2p);
			}else {//wangpan baidu_wangpan
				strSrc = getString(R.string.videoPlayerJPActivity_source_pptv);
			}
			
			mResourceTextView.setText(strSrc);
			mResourceTextView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(!mProd_src.equalsIgnoreCase(PlayerSourceType.TYPE_P2P.toSourceName())&&
							!mProd_src.equalsIgnoreCase(PlayerSourceType.TYPE_WANGPAN.toSourceName())){
						//点击logo跳网页
						String url = null;
						if(currentPlayIndex>=0&&currentPlayIndex<playUrls.size()){
							url = playUrls.get(currentPlayIndex).webUrl;
						}
//						Log.i(TAG, "OnClickListener weburl:" + url);
						if(url!=null){
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse(url));
							startActivity(intent);
						}
					}
				}
			});
		}
		if(lastTime>0){
			mLastTimeTextView.setVisibility(View.VISIBLE);
			mLastTimeTextView.setText(getString(R.string.videoPlayerJPActivity_lastTimeRecord,Utils.formatDuration(lastTime)));
		}else{
			mLastTimeTextView.setVisibility(View.GONE);
		}
		if(playUrls.size()>0&&currentPlayIndex<=playUrls.size()-1){
			Log.d(TAG, "type---->" + playUrls.get(currentPlayIndex).defination_from_server);
			mDefinationIcon.setVisibility(View.VISIBLE);
			if(Constant.player_quality_index[0].equalsIgnoreCase(playUrls.get(currentPlayIndex).defination_from_server)){
				mDefinationIcon.setImageResource(R.drawable.player_1080p);
			}else if(Constant.player_quality_index[1].equalsIgnoreCase(playUrls.get(currentPlayIndex).defination_from_server)){
				mDefinationIcon.setImageResource(R.drawable.player_720p);
			}else{
				mDefinationIcon.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	private void movieUrlPrepare(){
		if(m_ReturnProgramView.movie != null) {
			mProd_name = m_ReturnProgramView.movie.name;
			
			if(m_ReturnProgramView.movie.episodes != null 
					&& m_ReturnProgramView.movie.episodes.length > 0 
					&& m_ReturnProgramView.movie.episodes[0].down_urls != null) {
				Map<String,String> movieMap = new HashMap<String, String>();//来源和网页地址
				if(m_ReturnProgramView.movie.episodes[0].video_urls != null
						&& m_ReturnProgramView.movie.episodes[0].video_urls.length > 0){
					for(int i=0;i<m_ReturnProgramView.movie.episodes[0].video_urls.length;i++){
						if(m_ReturnProgramView.movie.episodes[0].video_urls[i]!= null
								&& m_ReturnProgramView.movie.episodes[0].video_urls[i].source != null){
							String source = m_ReturnProgramView.movie.episodes[0].video_urls[i].source;
							String webUrl = m_ReturnProgramView.movie.episodes[0].video_urls[i].url;
							movieMap.put(source, webUrl);
						}
					}
				}
				if(movieMap.size() == 1){//只存在一个来源
					if(!TextUtils.isEmpty(movieMap.get(PlayerSourceType.TYPE_LE_TV_FEE.toSourceName()))
							|| !TextUtils.isEmpty(movieMap.get(PlayerSourceType.TYPE_LETV.toSourceName()))){
						isOnlyExistLetv = true;
					}else if( !TextUtils.isEmpty(movieMap.get(PlayerSourceType.TYPE_FENGXING.toSourceName()))){
						isOnlyExistFengXing = true;
					}
					if(isOnlyExistFengXing || isOnlyExistLetv){
						sourceFromUrl = m_ReturnProgramView.movie.episodes[0].video_urls[0].url;
					}
				}
				
				for (int i = 0; i < m_ReturnProgramView.movie.episodes[0].down_urls.length; i++) {
					if(m_ReturnProgramView.movie.episodes[0].down_urls[i] != null) {
						String sources = m_ReturnProgramView.movie.episodes[0].down_urls[i].source;
						if(m_ReturnProgramView.movie.episodes[0].down_urls[i].urls != null) {
							for (int j = 0; j < m_ReturnProgramView.movie.episodes[0].down_urls[i].urls.length; j++) {
								if(m_ReturnProgramView.movie.episodes[0].down_urls[i].urls[j] != null) {
									URLS_INDEX url = new URLS_INDEX();
									url.source_from = sources;
									url.defination_from_server = m_ReturnProgramView.movie.episodes[0].down_urls[i].urls[j].type;
									url.url = m_ReturnProgramView.movie.episodes[0].down_urls[i].urls[j].url;
									url.bakUrl = url.url;
									url.webUrl = movieMap.get(sources);//网页播放来源
									playUrls.add(url);
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void tvAndAnimeUrlPrepare(){
		if(m_ReturnProgramView.tv != null) {
			mProd_name = m_ReturnProgramView.tv.name;
			if(m_ReturnProgramView.tv.episodes != null) {
				if (mEpisodeIndex == -1) {
					for (int i = 0; i < m_ReturnProgramView.tv.episodes.length; i++) {
						if (m_ReturnProgramView.tv.episodes[i] != null 
								&& mProd_sub_name.equals(m_ReturnProgramView.tv.episodes[i].name)) {
							mEpisodeIndex = i;
							if(m_ReturnProgramView.tv.episodes[i].down_urls == null){
								mHandler.sendEmptyMessage(MESSAGE_URLS_READY);
								return; 
							}
						}
					}
				} 
				if(mEpisodeIndex >= 0){//找到当前播放集数
					if(m_ReturnProgramView.tv.episodes.length > mEpisodeIndex
							&& m_ReturnProgramView.tv.episodes[mEpisodeIndex] != null) {
						Map<String,String> map = new HashMap<String, String>();//来源和网页地址
						if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls != null
								&& m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls.length > 0){
							for(int i=0;i<m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls.length;i++){
								if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls[i]!= null 
										&& m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls[i].source != null){
									String source = m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls[i].source;
									String webUrl = m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls[i].url;
									map.put(source, webUrl);
								}
							}
						}
						if(map.size() == 1){//只存在一个来源
							if(!TextUtils.isEmpty(map.get(PlayerSourceType.TYPE_LE_TV_FEE.toSourceName()))
									|| !TextUtils.isEmpty(map.get(PlayerSourceType.TYPE_LETV.toSourceName()))){
								isOnlyExistLetv = true;
							}else if( !TextUtils.isEmpty(map.get(PlayerSourceType.TYPE_FENGXING.toSourceName()))){
								isOnlyExistFengXing = true;
							}
							if(isOnlyExistFengXing || isOnlyExistLetv){
								sourceFromUrl = m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls[0].url;
							}
						}
						if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls != null) {
							for (int j = 0; j < m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls.length; j++) {
								if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j] != null) {
									String sources = m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].source;
									if( m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls != null) {
										for (int k = 0; k < m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls.length; k++) {
											if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls[k] != null) {
												URLS_INDEX url = new URLS_INDEX();
												url.source_from = sources;
												url.defination_from_server = m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls[k].type;
												url.url = m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls[k].url;
												url.bakUrl = url.url;
												url.webUrl = map.get(sources);//网页播放来源
												playUrls.add(url);
											}
										}
									}
								}
							}
						}
						mProd_sub_name = m_ReturnProgramView.tv.episodes[mEpisodeIndex].name;
					}
				}
			}
		}
	}
	
	private void varietyUrlParepare(){
		if(m_ReturnProgramView.show != null) {
			mProd_name = m_ReturnProgramView.show.name;
			if(m_ReturnProgramView.show.episodes != null) {
				if (mEpisodeIndex == -1) {
					for (int i = 0; i < m_ReturnProgramView.show.episodes.length; i++) {
						if(m_ReturnProgramView.show.episodes[i] != null) {
							if (UtilTools.isSame4Str(mProd_sub_name, m_ReturnProgramView.show.episodes[i].name)) {
								mEpisodeIndex = i;
								mProd_sub_name = m_ReturnProgramView.show.episodes[i].name;
								if(m_ReturnProgramView.show.episodes[i].down_urls==null){
									mHandler.sendEmptyMessage(MESSAGE_URLS_READY);
									return ;
								}
							}
						}
					}
				}
				if(mEpisodeIndex >= 0){
					if(m_ReturnProgramView.show.episodes.length > mEpisodeIndex ) {
						Map<String,String> map = new HashMap<String, String>();//来源和网页地址
						if(m_ReturnProgramView.show.episodes[mEpisodeIndex] != null 
								&& m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls != null) {
							if(m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls != null
									&& m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls.length > 0){
								for(int i=0;i<m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls.length;i++){
									if(m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[i]!= null
											&& m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[i].source != null){
										String source = m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[i].source;
										String webUrl = m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[i].url;
										map.put(source, webUrl);
									}
								}
							}
							if(map.size() == 1){//只存在一个来源
								if(!TextUtils.isEmpty(map.get(PlayerSourceType.TYPE_LE_TV_FEE.toSourceName()))
										|| !TextUtils.isEmpty(map.get(PlayerSourceType.TYPE_LETV.toSourceName()))){
									isOnlyExistLetv = true;
								}else if( !TextUtils.isEmpty(map.get(PlayerSourceType.TYPE_FENGXING.toSourceName()))){
									isOnlyExistFengXing = true;
								}
								if(isOnlyExistFengXing || isOnlyExistLetv){
									sourceFromUrl = m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[0].url;
								}
							}
							for (int j = 0; j < m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls.length; j++) {
								if(m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j] != null) {
									String sources = m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].source;
									if(m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls != null) {
										for (int k = 0; k < m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls.length; k++) {
											if(m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls[k] != null) {
												URLS_INDEX url = new URLS_INDEX();
												url.source_from = sources;
												url.defination_from_server = m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls[k].type;
												url.url = m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls[k].url;
												url.bakUrl = url.url;
												url.webUrl = map.get(sources);//网页播放来源
												playUrls.add(url);
											}
										}
									}
								}

							}
						}
						mProd_sub_name = m_ReturnProgramView.show.episodes[mEpisodeIndex].name;
					}
				}
			}
		}
	}

	/**
	 * 把m_ReturnProgramView中数据转化成基本数据
	 * 
	 * @author Administrator
	 * 
	 */
	class PrepareTask implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			playUrls.clear();
			switch (mProd_type) {
			case 1:
				movieUrlPrepare();
				break;
			case 2:
			case 131:
				tvAndAnimeUrlPrepare();
				break;
			case 3:
				varietyUrlParepare();
				break;
			}
			boolean hasChaoqing = false,hasGaoqing = false,hasPuqing = false;
			for(int i=0;i<playUrls.size();i++){
				URLS_INDEX url_index = playUrls.get(i);
				if(url_index.defination_from_server.equals(Constant.player_quality_index[0])){
					hasChaoqing = true;
				}else if(url_index.defination_from_server.equals(Constant.player_quality_index[1])){
					hasGaoqing = true;
				}else {
					hasPuqing = true;
				}
			}
			
			if(hasChaoqing){
				maxQuality = 8;
			}else {
				if(hasGaoqing){
					maxQuality = 7;
				}else {
					if(hasPuqing){
						maxQuality = 6;
					}
				}
			}

			urlArrange();
			// url list 准备完成
			mHandler.sendEmptyMessage(MESSAGE_URLS_READY);
		}
	}
	
	private int maxQuality = -1;//最高清晰度
	
	/**
	 * 把P2P源的磁力链和Bt地址转换成下载地址
	 * 此方法需要在非UI线程中调用
	 */
	private void p2pPreArrange(){
		UtilTools.setP2PMD5(getApplicationContext(), "");
		if("".equals(UtilTools.getP2PMD5(getApplicationContext()))){
			String md5 = null;
			if(Constant.TestEnv){
				md5 = MobclickAgent.getConfigParams(this, "TEST_P2P_TV_MD5");
			}else {
				md5 = MobclickAgent.getConfigParams(this, "P2P_TV_MD5");
			}
			Log.i(TAG, "md5--->" + md5);
			if(md5 != null && !"".equals(md5)){
				UtilTools.setP2PMD5(getApplicationContext(), md5);
			}
		}
		
		List<URLS_INDEX> tempP2PList = new ArrayList<URLS_INDEX>();
		List<URLS_INDEX> tempExP2PList = new ArrayList<URLS_INDEX>();
		
		for (int i = 0; i < playUrls.size(); i++) {
			URLS_INDEX tempUrls_INDEX = playUrls.get(i);
			if (PlayerSourceType.TYPE_P2P.toSourceName().equals(tempUrls_INDEX.source_from)) {
				if(tempUrls_INDEX.bakUrl != null){
					boolean hasSame = false;
					for(URLS_INDEX p2pUrlIndex: tempP2PList){
						if(tempUrls_INDEX.bakUrl.equals(p2pUrlIndex.bakUrl)){
							hasSame = true;
						}
					}
					if(!hasSame) tempP2PList.add(tempUrls_INDEX);
				}
			}else {
				tempExP2PList.add(tempUrls_INDEX);
			}
		}
		
		if(tempP2PList.size()>0){
			hasP2p = true;
		}
		
		for(int i=0;i< tempP2PList.size();i++){
			if(PlayerSourceType.TYPE_P2P.toSourceName().equals(tempP2PList.get(i).source_from)){
				String p2pUrl = tempP2PList.get(i).bakUrl;
				if(DEBUG)Log.i(TAG, "p2pUrl--->" + p2pUrl);
				if(p2pUrl != null && !p2pUrl.equals("")){
					if(!"".equals(UtilTools.getP2PMD5(getApplicationContext()))){
						String p2pStr = "";
						if(!isRetry){
							p2pStr = URLUtils.getXunLeiUrlURL(Constant.P2P_PARSE_URL_URL,p2pUrl,
									UtilTools.getP2PMD5(getApplicationContext()));
						}else{
							p2pStr = URLUtils.getXunLeiUrlURL(Constant.P2P_PARSE_URL_URL_RETRY,p2pUrl,
									UtilTools.getP2PMD5(getApplicationContext()));
						}
						
						Log.i(TAG, "p2pStr-->" + p2pStr);
						AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();           
						cb.url(p2pStr).type(JSONObject.class);             
//						Map<String, String> headers = new HashMap<String, String>();
//						headers.put("app_key", Constant.APPKEY);
						cb.SetHeader(app.getHeaders());        
						aq.sync(cb);
						JSONObject jo = cb.getResult();
						if(jo != null && jo.has("error")){
							try {
								if(!jo.getBoolean("error")){
									String downloadUrl = jo.getString("downurl");
									if(downloadUrl != null && !"".equals(downloadUrl)){
										String data = DesUtils.decode(Constant.DES_KEY, downloadUrl);
										Log.i(TAG, "data code-->" + data);
										String[] urls = data.split("\\{mType\\}");
										for(String str : urls){
											URLS_INDEX url_index_info = new URLS_INDEX();
											String[] p = str.split("\\{m\\}");
											if(p.length<2){
												continue;
											}
											url_index_info.source_from=PlayerSourceType.TYPE_P2P.toSourceName();
											url_index_info.bakUrl = p2pUrl;
											url_index_info.defination_from_server = p[0];
											if("hd".equals(p[0])){
												url_index_info.defination_from_server="mp4";
											}else if("hd2".equals(p[0])){
												url_index_info.defination_from_server="hd2";
											}else {
												url_index_info.defination_from_server="flv";
											}
											url_index_info.url = p[1];
											tempExP2PList.add(url_index_info);
										}
									}
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if(!mJoyplusSubManager.CheckSubAviable()){//获取字幕
							mJoyplusSubManager.setSubUri(
									mJoyplusSubManager.getNetworkSubURI(
											Constant.SUBTITLE_PARSE_URL_URL, 
											p2pUrl, UtilTools.getP2PMD5(VideoPlayerJPActivity.this),
											Constant.APPKEY, VideoPlayerJPActivity.this));
							mSubTitleView.hiddenSubtitle();
						}
					}
				}
			}
		}
		
		playUrls.clear();
		playUrls = tempExP2PList;
	}
	
	/**
	 * 把Letv V2的地址转换成下载地址
	 * 此方法需要在非UI线程中调用
	 * 包括LetvV2和Letv_v2_fee
	 */
	private void letvV2PreArrange(){
		List<URLS_INDEX> tempV2List 	= new ArrayList<URLS_INDEX>();
		List<URLS_INDEX> tempExV2List 	= new ArrayList<URLS_INDEX>();
		List<String> sourceList         = new ArrayList<String>();
		List<String> typeList			= new ArrayList<String>();
		for(URLS_INDEX tempUrls:playUrls){
			if (PlayerSourceType.TYPE_LETV_V2.toSourceName().equals(tempUrls.source_from)
					||PlayerSourceType.TYPE_LETV_V2_FEE.toSourceName().equals(tempUrls.source_from)) {
				tempV2List.add(tempUrls);
				if(sourceList.indexOf(tempUrls.webUrl) <0){
					sourceList.add(tempUrls.webUrl);
					typeList.add(tempUrls.source_from);
				}
			}else{
				tempExV2List.add(tempUrls);
			}
		}
		
		List<URLS_INDEX> tempList = getV2List4Server(tempV2List);
		
		if(tempList.size() <= 0){//需要主动去获取播放地址
			if(sourceList.size() > 0){
				for(int j=0;j<sourceList.size();j++){
					String sourceUrl = sourceList.get(j);
					String url = URLUtils.getReloadLetv(Constant.CLICK_LETV_PARSE_URL_URL,sourceUrl,mProd_id,mProd_sub_name);
					AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();           
					cb.url(url).type(JSONObject.class);             
//					Map<String, String> headers = new HashMap<String, String>();
//					headers.put("app_key", Constant.APPKEY);
					cb.SetHeader(app.getHeaders());        
					aq.sync(cb);
					JSONObject json = cb.getResult();
					if(json != null)Log.i(TAG, "letvV2PreArrange:" + json.toString());
					try {
						if(json != null && json.has("error") && !json.getBoolean("error")){
							if(json.has("down_urls")){
								JSONObject downUrls = json.getJSONObject("down_urls");
								if(downUrls != null && downUrls.has("urls")){
									JSONArray urlArray = downUrls.getJSONArray("urls");
									if(urlArray != null && urlArray.length() > 0){
										for(int i=0;i<urlArray.length();i++){
											JSONObject urlObj = urlArray.getJSONObject(i);
											if(urlObj != null)Log.i(TAG, "urlObj:" + urlObj.toString());
											if(urlObj != null && urlObj.has("type")){
												String requestUrl = urlObj.getString("url");
												String type = urlObj.getString("type");
												if(!TextUtils.isEmpty(requestUrl)){
													URLS_INDEX tempIndex = new URLS_INDEX();
													Log.i(TAG, "source_from:" + type);
													tempIndex.source_from = typeList.get(j);
													tempIndex.defination_from_server = type;
													tempIndex.url = requestUrl;
													tempIndex.webUrl = sourceUrl;
													tempList.add(tempIndex);
												}
											}
										}
									}
								}
							}
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			tempList = getV2List4Server(tempList);
		}
		
		tempExV2List.addAll(tempList);
		playUrls.clear();
		playUrls = tempExV2List;
	}
	
	private List<URLS_INDEX> getV2List4Server(List<URLS_INDEX> list){
		Iterator<URLS_INDEX> v2Iterator = list.iterator();
		List<URLS_INDEX> tempList = new ArrayList<URLS_INDEX>();
		while(v2Iterator.hasNext()){
			URLS_INDEX tempE = v2Iterator.next();
			String downUrl = tempE.url;
			AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();           
			cb.url(downUrl).type(JSONObject.class);             
//			Map<String, String> headers = new HashMap<String, String>();
//			headers.put("app_key", Constant.APPKEY);
//			cb.SetHeader(app.getHeaders());        
			aq.sync(cb);
			JSONObject json = cb.getResult();
			if(json != null)Log.i(TAG, "getV2List4Server:" + json.toString());
			if(json != null &&json.has("location")){
				try {
					String location = json.getString("location");
					if(!TextUtils.isEmpty(location) && URLUtil.isNetworkUrl(location)){
						tempE.url = location;
						tempList.add(tempE);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				break;
			}
		}
		return tempList;
	}
	
	/**
	 * 把百度网盘的原始转换成下载地址
	 * 此方法需要在非UI线程中调用
	 */
	private void baiduWangPanPreArrange(){
		for(int i=0;i<playUrls.size();i++){
			URLS_INDEX url_index = playUrls.get(i);
			if (url_index.source_from
					.equalsIgnoreCase(PlayerSourceType.TYPE_BAIDU_WANGPAN.toSourceName())) {
				boolean isLocalParse = false;
				if(UtilTools.getWpBaiduLocalParseInit(getApplicationContext())){
					
					isLocalParse = UtilTools.getWpBaiduLocalParse(getApplicationContext());
				}else {
					
					String localParse = MobclickAgent.getConfigParams(this, "WP_BAIDU_LOCAL_PARSE");
					Log.i(TAG, "localParse--->" + localParse);
					if(localParse != null && "true".equals(localParse)){
						
						isLocalParse = true;
						UtilTools.setWpBaiduLocalParse(getApplicationContext(), true);
					}else {
						
						isLocalParse = false;
						UtilTools.setWpBaiduLocalParse(getApplicationContext(), false);
					}
				}
				
				if(isLocalParse){
					
					Document doc = null;
					try {
						doc = Jsoup.connect(url_index.url).timeout(10000).get();
						// doc = Jsoup.connect(htmlStr).timeout(10000).get();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (doc != null) {
						Element e = doc.getElementById("fileDownload");
						if (e != null) {
							Log.d(TAG, "url = " + e.attr("href"));
							if (e.attr("href") != null
									&& e.attr("href").length() > 0) {
								url_index.url = e.attr("href");
							}
						}
					}
				} else {
					
					String wangPanUrl = URLUtils.
							getParseUrlURL(Constant.LETV_PARSE_URL_URL, url_index.url, mProd_id, mProd_sub_name);
					Log.d(TAG,"-------geturl"+url_index.url);
					AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();           
					cb.url(wangPanUrl).type(JSONObject.class);             
//					Map<String, String> headers = new HashMap<String, String>();
//					headers.put("app_key", Constant.APPKEY);
					cb.SetHeader(app.getHeaders());  
					
					aq.sync(cb);
//					AjaxStatus status = cb.getStatus();
					JSONObject jo = cb.getResult();
					if(jo != null){
						Log.i(TAG, "wangpan src url:" + url_index.url+ "  --wangpan-jsondata:" + jo.toString());
						if(jo.has("error")){
							try {
								if(!jo.getBoolean("error")){
									
									ObjectMapper mapper = new ObjectMapper();
									ReturnReGetVideoView reGetVideoView = mapper.readValue(jo.toString(),
											ReturnReGetVideoView.class);
									if(reGetVideoView != null && reGetVideoView.down_urls != null
											&& reGetVideoView.down_urls.urls != null
											&& reGetVideoView.down_urls.urls.length > 0){
										if(reGetVideoView.down_urls.urls[0] != null
												&& reGetVideoView.down_urls.urls[0].url != null){
											url_index.url = reGetVideoView.down_urls.urls[0].url;
										}
									}
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 *地址排序前的处理
	 */
	private void playUrlsPreArrange(){
		for (int i = 0; i < playUrls.size(); i++) {
			URLS_INDEX url_index = playUrls.get(i);

			if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_P2P.toSourceName())) {
				url_index.souces = PlayerSourceType.TYPE_P2P.getPriority();
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_WANGPAN.toSourceName())) {
				url_index.souces = PlayerSourceType.TYPE_WANGPAN.getPriority();
				
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_LE_TV_FEE.toSourceName())) {//le_tv_fee
				url_index.souces =PlayerSourceType.TYPE_LE_TV_FEE.getPriority();
				
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_LETV.toSourceName())) {//letv
				url_index.souces = PlayerSourceType.TYPE_LETV.getPriority();

			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_SOHU.toSourceName())) {//fengxing
				url_index.souces = PlayerSourceType.TYPE_SOHU.getPriority();
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_FENGXING.toSourceName())) {
				url_index.souces = PlayerSourceType.TYPE_FENGXING.getPriority();
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_YOUKU.toSourceName())) {
				url_index.souces = PlayerSourceType.TYPE_YOUKU.getPriority();
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_SINAHD.toSourceName())) {
				url_index.souces = PlayerSourceType.TYPE_SINAHD.getPriority();
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_QIYI.toString())) {
				url_index.souces = PlayerSourceType.TYPE_QIYI.getPriority();
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_M1905.toSourceName())) {
				url_index.souces = PlayerSourceType.TYPE_M1905.getPriority();
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_QQ.toSourceName())) {
				url_index.souces = PlayerSourceType.TYPE_P2P.getPriority();
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_PPTV.toSourceName())) {
				url_index.souces = PlayerSourceType.TYPE_PPTV.getPriority();
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_56.toSourceName())) {
				url_index.souces = PlayerSourceType.TYPE_56.getPriority();
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_PPS.toSourceName())) {
				url_index.souces = PlayerSourceType.TYPE_PPS.getPriority();
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_BAIDU_WANGPAN.toSourceName())) {
				url_index.souces = PlayerSourceType.TYPE_BAIDU_WANGPAN.getPriority();
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_LETV_V2.toSourceName())) {
				url_index.souces = PlayerSourceType.TYPE_LETV_V2.getPriority();
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					PlayerSourceType.TYPE_LETV_V2_FEE.toSourceName())) {
				url_index.souces = PlayerSourceType.TYPE_LETV_V2_FEE.getPriority();
			}else {
				url_index.souces = PlayerSourceType.TYPE_UNKOWN.getPriority();
			}
			switch (mDefination) {
			case BangDanConstant.GAOQING:// 高清
				if (url_index.defination_from_server.trim()
						.equalsIgnoreCase(Constant.player_quality_index[1])) {
					url_index.defination = 1;
				} else if (url_index.defination_from_server.trim()
						.equalsIgnoreCase(Constant.player_quality_index[0])) {
					url_index.defination = 2;
				} else if (url_index.defination_from_server.trim()
						.equalsIgnoreCase(Constant.player_quality_index[2])) {
					url_index.defination = 3;
				} else if (url_index.defination_from_server.trim()
						.equalsIgnoreCase(Constant.player_quality_index[3])) {
					url_index.defination = 4;
				} else {
					url_index.defination = 5;
				}
				break;
			case BangDanConstant.CHAOQING:// 超清
				if (url_index.defination_from_server.trim()
						.equalsIgnoreCase(Constant.player_quality_index[0])) {
					url_index.defination = 1;
				} else if (url_index.defination_from_server.trim()
						.equalsIgnoreCase(Constant.player_quality_index[1])) {
					url_index.defination = 2;
				} else if (url_index.defination_from_server.trim()
						.equalsIgnoreCase(Constant.player_quality_index[2])) {
					url_index.defination = 3;
				} else if (url_index.defination_from_server.trim()
						.equalsIgnoreCase(Constant.player_quality_index[3])) {
					url_index.defination = 4;
				} else {
					url_index.defination = 5;
				}
				break;
			case BangDanConstant.CHANGXIAN:// 标清
				if (url_index.defination_from_server.trim()
						.equalsIgnoreCase(Constant.player_quality_index[2])) {
					url_index.defination = 1;
				} else if (url_index.defination_from_server.trim()
						.equalsIgnoreCase(Constant.player_quality_index[3])) {
					url_index.defination = 2;
				} else if (url_index.defination_from_server.trim()
						.equalsIgnoreCase(Constant.player_quality_index[1])) {
					url_index.defination = 3;
				} else if (url_index.defination_from_server.trim()
						.equalsIgnoreCase(Constant.player_quality_index[0])) {
					url_index.defination = 4;
				} else {
					url_index.defination = 5;
				}
				break;
			}
		}
	}
	
	private void initDefinitionUrls(){
		reInitDefinationPartAndSubTitle();
		for (int i = 0; i < playUrls.size(); i++) {

			if ("hd2".equals(playUrls.get(i).defination_from_server)) {
				playUrls_hd2.add(playUrls.get(i));
			} else if ("mp4".equals(playUrls.get(i).defination_from_server)) {
				playUrls_mp4.add(playUrls.get(i));
			} else if ("flv".equals(playUrls.get(i).defination_from_server)) {
				playUrls_flv.add(playUrls.get(i));
			}
		}
	}
	
	//地址排序
	private void sortList(){
		if (playUrls.size() > 1) {
			Collections.sort(playUrls, new SouceComparatorIndex1());
			Collections.sort(playUrls, new DefinationComparatorIndex());
		}
	}
	
	private void urlArrange(){
		Log.d(TAG, "playUrls size ------->" + playUrls.size() + " maxQuality--->" + maxQuality);
		
		p2pPreArrange();
		letvV2PreArrange();
		baiduWangPanPreArrange();
		playUrlsPreArrange();
		initDefinitionUrls();
		sortList();
	}
	
	private void reInitDefinationPartAndSubTitle(){
		
		playUrls_flv.clear();
		playUrls_hd2.clear();
		playUrls_mp4.clear();
	}
	
	
	protected void onResume() {
		MobclickAgent.onResume(this);
		super.onResume();
	}

	protected void onPause() {
		
		Log.i(TAG, "onPause--->");
		
		MobclickAgent.onPause(this);
		if (mProd_type > 0&&mStatue!=STATUE_LOADING) {
			long duration = mVideoView.getDuration();
			long curretnPosition = mVideoView.getCurrentPosition();
			Log.d(TAG, "duration ->" + duration);
			Log.d(TAG, "curretnPosition ->" + curretnPosition);
			if(duration-curretnPosition<10*1000){
				saveToServer(duration / 1000, (duration / 1000) -10);
			}else{
				saveToServer(duration / 1000, curretnPosition / 1000);
			}
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		
		Log.i(TAG, "onStop--->");
		// TODO Auto-generated method stub
		if(!isFinishing()){
			finish();
		}
		super.onStop();
	}

	public void saveToServer(long duration, long playBackTime) {
		String url = Constant.BASE_URL + "program/play";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("app_key", Constant.APPKEY);// required string
		params.put("prod_id", mProd_id);
		params.put("prod_name", mProd_name);// required
		params.put("prod_subname", mProd_sub_name);
		params.put("prod_type", mProd_type);// required int 视频类别
		params.put("play_type", "1");
		params.put("playback_time", playBackTime);// _time required int
		params.put("duration", duration);// required int 视频时长， 单位：秒
		params.put("video_url", currentPlayUrl);// required
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.SetHeader(app.getHeaders());
		cb.params(params).url(url).type(JSONObject.class)
				.weakHandler(this, "CallProgramPlayResult");
		
		aq.ajax(cb);

		// DB操作，把存储到服务器的数据保存到数据库
		TvDatabaseHelper helper = TvDatabaseHelper
				.newTvDatabaseHelper(getApplicationContext());
		SQLiteDatabase database = helper.getWritableDatabase();// 获取写db

		String selection = UserShouCang.USER_ID + "=? and "
				+ UserHistory.PRO_ID + "=?";// 通过用户id，找到相应信息
		String[] selectionArgs = {
				UtilTools.getCurrentUserId(getApplicationContext()), mProd_id };

		database.delete(TvDatabaseHelper.HISTORY_TABLE_NAME, selection,
				selectionArgs);

		HotItemInfo info = new HotItemInfo();
		info.prod_type = mProd_type + "";
		info.prod_name = mProd_name;

		info.prod_subname = mProd_sub_name;
		info.prod_id = mProd_id;
		info.play_type = "1";
		info.playback_time = playBackTime + "";
		info.video_url = currentPlayUrl;
		info.duration = duration + "";

		DBUtils.insertHotItemInfo2DB_History(getApplicationContext(), info,
				UtilTools.getCurrentUserId(getApplicationContext()), database);

		helper.closeDatabase();
		
		REQUEST_URL request_URL = RequestAQueryManager.getInstance().getcurrentRequest_URL();
		
		if(request_URL == REQUEST_URL.HAOIMS) return;
		//发送更新最新记录广播
		app.set_ReturnProgramView(m_ReturnProgramView);
		Intent historyIntent  = new Intent(UtilTools.ACTION_PLAY_END_HISTORY);
		historyIntent.putExtra("prod_id", mProd_id);
		historyIntent.putExtra("prod_sub_name", mProd_sub_name);
		historyIntent.putExtra("prod_type", mProd_type);
		historyIntent.putExtra("time", playBackTime);
		sendBroadcast(historyIntent);
		
		Intent mainIntent  = new Intent(UtilTools.ACTION_PLAY_END_MAIN);
		mainIntent.putExtra("prod_id", mProd_id);
		mainIntent.putExtra("prod_sub_name", mProd_sub_name);
		mainIntent.putExtra("prod_type", mProd_type);
		mainIntent.putExtra("time", playBackTime);
		sendBroadcast(mainIntent);
		
	}
	
	private void setResult2Xiangqing() {
		
		Intent dataIntent = getIntent();
		dataIntent.putExtra("prod_subname", mProd_sub_name);
		
		if(isShoucang) {
			
			setResult(JieMianConstant.SHOUCANG_ADD,dataIntent);
		} else {
			
			setResult(JieMianConstant.SHOUCANG_CANCEL,dataIntent);
		}
	}

	public void CallProgramPlayResult(String url, JSONObject json,
			AjaxStatus status) {
		if (json != null) {
			Log.d(TAG, json.toString());
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "--------on new Intent--------------");
		super.onNewIntent(intent);
		mHandler.removeCallbacksAndMessages(null);
		m_ReturnProgramView = null;
		if (mVideoView!=null) { 
			mVideoView.stopPlayback();
			mVideoView.resume();
		}
		lastTime = 0;
		rxByteslast = 0;
		mLoadingPreparedPercent = 0;
		mEpisodeIndex = -1;
		reloadLetvCount = 0;
		mPercentTextView.setText(getString(R.string.utils_percent_network, 
				Long.toString(mLoadingPreparedPercent / 100)));
		initVedioDate();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		Log.i(TAG, "onDestroy--->");
		
		unregisterReceiver(mReceiver);
		if (mVideoView != null) {
			mVideoView.stopPlayback();
		}
		
		if(mPreLoadLayout.getBackground() != null) {
			
			UtilTools.recycleBitmap(((BitmapDrawable)mPreLoadLayout.getBackground()).getBitmap());
		}
		
		mHandler.removeCallbacksAndMessages(null);
		
		super.onDestroy();
	}

	private void getIsShoucangData() {
		String url = Constant.BASE_URL + "program/is_favority";
		// +"?prod_id=" + prod_id;
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		Map<String, String> params = new HashMap<String, String>();
		params.put("prod_id", mProd_id);
		cb.params(params).url(url).type(JSONObject.class)
				.weakHandler(this, "initIsShoucangData");
		cb.SetHeader(app.getHeaders());
		aq.ajax(cb);
	}

	public void initIsShoucangData(String url, JSONObject json,
			AjaxStatus status) {

		if (status.getCode() == AjaxStatus.NETWORK_ERROR || json == null) {
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}

		if (json == null || json.equals(""))
			return;

		Log.d(TAG, "data = " + json.toString());

		String flag = json.toString();

		if (!flag.equals("")) {

			if (flag.contains("true")) {

				isShoucang = true;
			} else {

				isShoucang = false;
			}
		} else {

			isShoucang = true;
		}
	}

	public void favorityResult(String url, JSONObject json,
			AjaxStatus status) {

		if (json != null) {
			try {
				// woof is "00000",now "20024",by yyc
				if (json.getString("res_code").trim().equalsIgnoreCase("00000")) {
					app.MyToast(this, getString(R.string.videoPlayerJPActivity_toast_add_favority_success));
					
					isShoucang = true;
					mBottomButton.setBackgroundResource(R.drawable.player_btn_unfav);
					
					
				} else {
					
					isShoucang = true;
					mBottomButton.setBackgroundResource(R.drawable.player_btn_unfav);
					app.MyToast(this,getString(R.string.videoPlayerJPActivity_toast_add_favority_already));
				}
					
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

	public void unfavorityResult(String url, JSONObject json, AjaxStatus status) {
		if (json != null) {
			try {
				if (json.getString("res_code").trim().equalsIgnoreCase("00000")) {
					app.MyToast(this,getString(R.string.videoPlayerJPActivity_toast_cancel_favority_succes));

					mBottomButton
					.setBackgroundResource(R.drawable.player_btn_fav);
					isShoucang = false;
//					setResult(JieMianConstant.SHOUCANG_CANCEL);
				} else {
					
					app.MyToast(this,getString(R.string.videoPlayerJPActivity_toast_cancel_favority_fail));
					isShoucang = true;
				}

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
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		 Dialog alertDialog = new AlertDialog.Builder(this). 
	                setTitle(getString(R.string.videoPlayerJPActivity_dailog_tip_title)). 
	                setMessage(getString(R.string.videoPlayerJPActivity_dailog_canot_play)). 
	                setPositiveButton(getString(R.string.videoPlayerJPActivity_dailog_OK), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							finish();
						}

					}).
	                create();
		 	alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					finish();
				}
			});
	        alertDialog.show(); 
		return super.onCreateDialog(id);
	}
	
	/**
	 * 地址跳转
	 */
	
	class  UrlRedirectTask implements Runnable{

		@Override
		public void run() {
			
			// TODO Auto-generated method stub
			
			Log.i(TAG, "UrlRedirectTask-->" + currentPlayUrl);
			
			if(currentPlayUrl != null && !currentPlayUrl.equals("")) {
				
				if(currentPlayUrl.indexOf(("{now_date}")) != -1) {
					
					currentPlayUrl = currentPlayUrl.replace("{now_date}", System.currentTimeMillis()/1000 + "");
				}
			}
			
			String str = getRedirectUrl();
			
			if(str!=null){
				currentPlayUrl = str;
				mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
			}else{
				mHandler.sendEmptyMessage(MESSAGE_URL_NEXT);
			}
		}
		
	}
	
	private String getRedirectUrl(){
		String urlStr = null;
			
		List<String> list = new ArrayList<String>();
		
		try {
			urlRedirect(currentPlayUrl,list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//超时异常
		}
		if(list.size() > 0) {
			 urlStr = list.get(list.size() -1);
		}
		return urlStr;
	}
	
	private void urlRedirect(String urlStr,List<String> list) {
		
		// 模拟火狐ios发用请求 使用userAgent
		AndroidHttpClient mAndroidHttpClient = AndroidHttpClient
				.newInstance(Constant.USER_AGENT_IOS);

		HttpParams httpParams = mAndroidHttpClient.getParams();
		// 连接时间最长5秒，可以更改
		HttpConnectionParams.setConnectionTimeout(httpParams, 5000 * 1);
		
		URL url;
		try {
			url = new URL(urlStr);
			HttpGet mHttpGet = new HttpGet(url.toURI());
			HttpResponse response = mAndroidHttpClient.execute(mHttpGet);
			StatusLine statusLine = response.getStatusLine();
			
			int status = statusLine.getStatusCode();
			
			if (status == HttpStatus.SC_OK) {
				// 正确的话直接返回，不进行下面的步骤
				mAndroidHttpClient.close();
				list.add(urlStr);
				return;//后面不执行
			} else {
				if (status == HttpStatus.SC_MOVED_PERMANENTLY || // 网址被永久移除
						status == HttpStatus.SC_MOVED_TEMPORARILY || // 网址暂时性移除
						status == HttpStatus.SC_SEE_OTHER || // 重新定位资源
						status == HttpStatus.SC_TEMPORARY_REDIRECT) {// 暂时定向
					Header header = response.getFirstHeader("Location");// 拿到重新定位后的header
					if(header != null) {
						String location = header.getValue();// 从header重新取出信息
						Log.i(TAG, "Location: " + location);
						if(location != null && !location.equals("")) {
							urlRedirect(location, list);
							mAndroidHttpClient.close();// 关闭此次连接
							return;//后面不执行
						}
					}
					list.add(null);
					mAndroidHttpClient.close();
					return;
				} else {//地址真的不存在
					Log.d(TAG, "status="+ status +"\n + url->" + urlStr);
					mAndroidHttpClient.close();
					list.add(null);
					return;//后面不执行
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mAndroidHttpClient.close();
			list.add(null);
		} 
	}
	
	
	private void hidePreLoad(){
		Log.d(TAG, "hidePreLoad----------->");
		mPreLoadLayout.setVisibility(View.GONE);
		mHandler.removeCallbacks(mLoadingRunnable);
		mStatue = STATUE_PLAYING;
		mSeekBar.setEnabled(true);
//		mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, 1000);
		startUpdateSeekBar(SEEKBAR_REFRESH_TIME);
		mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_PROGRESSBAR, 5000);
	}
	
	/*
	 * adkey player picture in picture
	 * author yyc
	 */
	private void showBanner(){
		if (mAdView != null) {
			removeBanner();
		}
		mAdView = new AdView(this, Constant.PLAYER_ADV_PUBLISHERID,Constant.ANIMATION);
		mAdView.setAdListener(this);
		layout.addView(mAdView);
	}
	
	private void removeBanner(){
		if(mAdView!=null){
			layout.removeView(mAdView);
			mAdView = null;
		}
	}
	
	@Override
	public void adClicked()
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void adClosed(Ad ad, boolean completed)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void adLoadSucceeded(Ad ad)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void adShown(Ad ad, boolean succeeded)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void noAdFound()
	{
		// TODO Auto-generated method stub
		
	}
	
	class DefinationAdapter extends BaseAdapter{

		List<Integer> list;
		Context c;
		
		public DefinationAdapter(Context c, List<Integer> list){
			this.c = c;
			this.list = list;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView tv = new TextView(c);
			tv.setBackgroundResource(R.drawable.bg_choose_defination_selector);
			tv.setTextColor(Color.WHITE);
			tv.setTextSize(25);
			switch (list.get(position)) {
			case Constant.DEFINATION_HD2:
				tv.setText(getString(R.string.videoPlayerJPActivity_density_SUPERD));
				break;
			case Constant.DEFINATION_MP4:
				tv.setText(getString(R.string.videoPlayerJPActivity_density_HD));
				
				break;
			case Constant.DEFINATION_FLV:
				tv.setText(getString(R.string.videoPlayerJPActivity_density_STANDARDD));
				break;
			}
			Gallery.LayoutParams param = new Gallery.LayoutParams((int)Utils.getStandardValue(getApplicationContext(), 165),
					(int)Utils.getStandardValue(getApplicationContext(), 40));
			tv.setGravity(Gravity.CENTER);
			tv.setLayoutParams(param);
			return tv;
		}

	}
	
	class ZimuAdapter extends BaseAdapter{

		List<Integer> list;
		Context c;
		
		public ZimuAdapter(Context c, List<Integer> list){
			Log.i(TAG, "ZimuAdapter list--->" + list.size());
			this.c = c;
			this.list = list;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			Log.i(TAG, "ZimuAdapter getCount--->" + list.size());
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView tv = new TextView(c);
			tv.setTextColor(Color.WHITE);
			tv.setBackgroundResource(R.drawable.bg_choose_defination_selector);
			tv.setTextSize(25);
			Log.i(TAG, "ZimuAdapter position--->" + position);
			if(position>=0&&position<list.size()){
				switch (list.get(position)) {
				case -1://无字幕
					tv.setText(getString(R.string.videoPlayerJPActivity_no_subtitle));
					break;
				case 0://字幕关
					tv.setText(getString(R.string.videoPlayerJPActivity_subtitle_close));
					break;
					
				default:
					tv.setText(getString(R.string.videoPlayerJPActivity_subtitle_num,list.get(position).toString()));
					break;
//				case 1://字幕开
//					tv.setText("");
//					break;
				}
			}
			Gallery.LayoutParams param = new Gallery.LayoutParams((int)Utils.getStandardValue(VideoPlayerJPActivity.this,165),
					(int)Utils.getStandardValue(VideoPlayerJPActivity.this,40));
			tv.setGravity(Gravity.CENTER);
			tv.setLayoutParams(param);
			return tv;
		}

	}
}
