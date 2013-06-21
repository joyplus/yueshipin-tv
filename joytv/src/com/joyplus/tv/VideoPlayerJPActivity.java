package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
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
import com.joyplus.tv.Service.Return.ReturnProgramView;
import com.joyplus.tv.entity.CurrentPlayDetailData;
import com.joyplus.tv.entity.URLS_INDEX;
import com.joyplus.tv.ui.ArcView;
import com.joyplus.tv.utils.BangDanConstant;
import com.joyplus.tv.utils.DefinationComparatorIndex;
import com.joyplus.tv.utils.Log;
import com.joyplus.tv.utils.SouceComparatorIndex1;
import com.joyplus.tv.utils.UtilTools;

public class VideoPlayerJPActivity extends Activity implements
		MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
		MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener,
		MediaPlayer.OnInfoListener, MediaPlayer.OnSeekCompleteListener,
		MediaPlayer.OnVideoSizeChangedListener, OnSeekBarChangeListener,
		OnClickListener {

	private static final String TAG = "VideoPlayerActivity";

	private static final int MESSAGE_RETURN_DATE_OK = 0;
	private static final int MESSAGE_URLS_READY = MESSAGE_RETURN_DATE_OK + 1;
	private static final int MESSAGE_PALY_URL_OK = MESSAGE_URLS_READY + 1;
	private static final int MESSAGE_URL_NEXT = MESSAGE_PALY_URL_OK + 1;
	private static final int MESSAGE_UPDATE_PROGRESS = MESSAGE_URL_NEXT + 1;
	private static final int MESSAGE_HIDE_PROGRESSBAR = MESSAGE_UPDATE_PROGRESS + 1;
	private static final int MESSAGE_HIDE_VOICE = MESSAGE_HIDE_PROGRESSBAR + 1;

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

	private static final int OFFSET = 33;
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
	 * 基本播放参数
	 */
	private String mProd_id;
	private String mProd_name;
	private int mProd_type;
	private String mProd_src;// 来源
	private int mDefination = 0; // 清晰度 6为尝鲜，7为普清，8为高清
	private String mProd_sub_name = null;
	private int mEpisodeIndex = -1; // 当前集数对应的index
	private long lastTime = 0;

	/**
	 * 网络数据
	 */
	private int currentPlayIndex;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.video_player_main);

		aq = new AQuery(this);
		app = (App) getApplication();

		mStatue = STATUE_LOADING;// 进入播放器为加载状态

		initViews();

		// mProd_id = getIntent().getIntExtra("prod_id", 0);
		// mProd_type = getIntent().getIntExtra("prod_type", 0);
		// mCurrentEpisodeIndex = getIntent().getStringExtra("sub_name");

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
			finish();
			return;
		}

		// 初始化基本播放数据
		mProd_id = playDate.prod_id;
		mProd_type = playDate.prod_type;
		mProd_name = playDate.prod_name;
		mProd_sub_name = playDate.prod_sub_name;
		currentPlayUrl = playDate.prod_url;
		mDefination = playDate.prod_qua;
		lastTime = (int) playDate.prod_time;
		mProd_src = playDate.prod_src;

		// 更新播放来源和上次播放时间
		updateSourceAndTime();

		if (currentPlayUrl != null && URLUtil.isNetworkUrl(currentPlayUrl)
				&& mProd_type != 2 && mProd_type != 3 && mProd_type != 131) {// 如果是电影，地址正确就直接播放

			mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
		} else {// 如果不是电影

			if (app.get_ReturnProgramView() != null) {// 如果不为空，获取服务器返回的详细数据

				m_ReturnProgramView = app.get_ReturnProgramView();
				mHandler.sendEmptyMessage(MESSAGE_RETURN_DATE_OK);
			} else {// 如果为空，就重新获取

				getProgramViewDetailServiceData();
			}
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MESSAGE_RETURN_DATE_OK:
				new Thread(new PrepareTask()).start();
				break;
			case MESSAGE_URLS_READY:// url 准备好了
				currentPlayIndex = 0;
				currentPlayUrl = playUrls.get(currentPlayIndex).url;
				mProd_src = playUrls.get(currentPlayIndex).source_from;
				if (currentPlayUrl != null
						&& URLUtil.isNetworkUrl(currentPlayUrl)) {
					// 地址跳转相关。。。
					mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
					mVideoNameText.setText(mProd_name + (mEpisodeIndex + 1));// 这句话不对，
																				// 要根据不同的节目做相应的处理。这里仅仅是为了验证上下集
				}
				break;
			case MESSAGE_URL_NEXT:
				if (playUrls.size() <= 0) {
					if (app.get_ReturnProgramView() != null) {
						m_ReturnProgramView = app.get_ReturnProgramView();
						mHandler.sendEmptyMessage(MESSAGE_RETURN_DATE_OK);
					} else {
						if(mProd_type>0&&!"-1".equals(mProd_id)&&mProd_id!=null){
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
							mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
						}
					} else {
						// 所有的片源都不能播放
						Log.e(TAG, "no url can play!");
					}
				}
				break;
			case MESSAGE_PALY_URL_OK:
				updateSourceAndTime();
				mVideoView.setVideoURI(Uri.parse(currentPlayUrl));
				mVideoView.seekTo((int) lastTime);
				mVideoView.start();
				break;
			case MESSAGE_UPDATE_PROGRESS:
				updateSeekBar();
				break;
			case MESSAGE_HIDE_PROGRESSBAR:
				mNoticeLayout.setVisibility(View.GONE);
				break;
			case MESSAGE_HIDE_VOICE:
				mVocieLayout.setVisibility(View.GONE);
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
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

		mPreButton.setOnClickListener(this);
		mNextButton.setOnClickListener(this);
		mTopButton.setOnClickListener(this);
		mBottomButton.setOnClickListener(this);
		mCenterButton.setOnClickListener(this);
		mContinueButton.setOnClickListener(this);

		mVoiceProgress = (ArcView) findViewById(R.id.av_volume);

		mPreLoadLayout = (RelativeLayout) findViewById(R.id.rl_preload);
		mNoticeLayout = (RelativeLayout) findViewById(R.id.rl_titile_seekbar);
		mControlLayout = (LinearLayout) findViewById(R.id.ll_control_buttons);
		mVocieLayout = (LinearLayout) findViewById(R.id.ll_volume);
		mContinueLayout = (LinearLayout) findViewById(R.id.ll_continue);
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVideoView.setOnErrorListener(this);
		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnPreparedListener(this);
		mVideoView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				mHandler.removeMessages(MESSAGE_HIDE_PROGRESSBAR);
				mNoticeLayout.setVisibility(View.VISIBLE);
				mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_PROGRESSBAR, 2500);
				return false;
			}
		});
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
					mVocieLayout.setVisibility(View.GONE);
					mHandler.removeMessages(MESSAGE_HIDE_VOICE);
					mStatue = STATUE_PAUSE;
					mVideoView.pause();
					mHandler.removeMessages(MESSAGE_HIDE_PROGRESSBAR);
					mControlLayout.setVisibility(View.VISIBLE);
					mNoticeLayout.setVisibility(View.VISIBLE);
					mTopButton.requestFocus();
					// if( getCurrentFocus().getId() != mSeekBar.getId()) {
					//
					// mSeekBar.requestFocus();
					// }
					//
					// Log.d(TAG,"FOUCED ID -->" + getCurrentFocus().getId());
					// mHandler.postDelayed(new Runnable() {
					//
					// @Override
					// public void run() {
					// // TODO Auto-generated method stub
					// mTopButton.requestFocus();
					// }
					// }, 200);
					return true;
				} else {
					mVideoView.stopPlayback();
					finish();
				}
				break;
			case STATUE_PAUSE:
				return true;
			case STATUE_FAST_DRAG:
				return true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			switch (mStatue) {
			case STATUE_PLAYING:
				mVocieLayout.setVisibility(View.GONE);
				mHandler.removeMessages(MESSAGE_HIDE_VOICE);
				mStatue = STATUE_PAUSE;
				mVideoView.pause();
				mHandler.removeMessages(MESSAGE_HIDE_PROGRESSBAR);
				mContinueLayout.setVisibility(View.VISIBLE);
				mNoticeLayout.setVisibility(View.VISIBLE);
				mContinueButton.requestFocus();
				break;
			case STATUE_FAST_DRAG:
				if(mFastJumpTime<mVideoView.getDuration()){
					mVideoView.seekTo(mFastJumpTime);
				}
				mTimeJumpSpeed = 0;
				upDateFastTimeBar();
				mHandler.removeMessages(MESSAGE_UPDATE_PROGRESS);
				mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, 1000);
				mStatue = STATUE_PLAYING;
				break;
			}
			break;
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
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		// 准备好了
		mPreLoadLayout.setVisibility(View.GONE);
		mHandler.removeCallbacks(mLoadingRunnable);
		mTotalTimeTextView.setText(UtilTools.formatDuration(mVideoView
				.getDuration()));
		mSeekBar.setMax((int) mVideoView.getDuration());
		mSeekBar.setOnSeekBarChangeListener(this);
		mStatue = STATUE_PLAYING;
		mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, 1000);
		mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_PROGRESSBAR, 5000);
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
		// 快进好了（拖动）
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		// 缓冲进度
	}

	private void updateSeekBar() {
		switch (mStatue) {
		case STATUE_PLAYING:
			long current = mVideoView.getCurrentPosition();// 当前进度
			mSeekBar.setProgress((int) current);
			// updateTimeNoticeView(mSeekBar.getProgress());
			mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, 1000);
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
			mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS,
					mTimes[Math.abs(mTimeJumpSpeed) - 1]);
			break;
		default:
			mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, 1000);
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
		parms.bottomMargin = 20 + 10;
		mTimeLayout.setLayoutParams(parms);

		mCurrentTimeTextView.setText(UtilTools.formatDuration(progress));
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
			// check how long there is until we reach the desired refresh rate
			m_bitrate = ((rxBytes - rxByteslast) * 8 * 1000 / timeTakenMillis) / 8000;
			rxByteslast = rxBytes;

			mSpeedTextView.setText("（" + Long.toString(m_bitrate) + "kb/s");
			mLoadingPreparedPercent = mLoadingPreparedPercent + m_bitrate;
			if (mLoadingPreparedPercent >= 100
					&& mLoadingPreparedPercent / 100 <= 100)
				mPercentTextView.setText("）,已完成"
						+ Long.toString(mLoadingPreparedPercent / 100) + "%");

			// Fun_downloadrate();
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

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ib_control_top:
			mControlLayout.setVisibility(View.GONE);
			mNoticeLayout.setVisibility(View.GONE);
			mStatue = STATUE_PLAYING;
			mVideoView.requestFocus();
			mVideoView.start();
			break;
		case R.id.btn_continue:
			mContinueLayout.setVisibility(View.GONE);
			mNoticeLayout.setVisibility(View.GONE);
			mStatue = STATUE_PLAYING;
			mVideoView.requestFocus();
			mVideoView.start();
			break;
		case R.id.ib_control_center:
			finish();
			break;
		case R.id.ib_control_left:
			playPrevious();
			mStatue = STATUE_LOADING;
			mSeekBar.setProgress(0);
			mHandler.removeCallbacksAndMessages(this);
			mControlLayout.setVisibility(View.GONE);
			break;
		case R.id.ib_control_right:
			playNext();
			mStatue = STATUE_LOADING;
			mSeekBar.setProgress(0);
			mHandler.removeCallbacksAndMessages(this);
			mControlLayout.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	private void showLoading() {
		mLoadingPreparedPercent = 0;
		rxByteslast = 0;
		mStartRX = TrafficStats.getTotalRxBytes();
		if (mStartRX == TrafficStats.UNSUPPORTED) {
			mSpeedTextView
					.setText("Your device does not support traffic stat monitoring.");
		} else {
			mHandler.postDelayed(mLoadingRunnable, 500);
		}
		mPercentTextView.setText("已完成0%");
		mPreLoadLayout.setVisibility(View.VISIBLE);
	}

	private void playNext() {
		// TODO Auto-generated method stub
		lastTime = 0;
		mVideoView.stopPlayback();
		mEpisodeIndex += 1;
		mHandler.sendEmptyMessage(MESSAGE_RETURN_DATE_OK);
	}

	private void playPrevious() {
		// TODO Auto-generated method stub
		lastTime = 0;
		mVideoView.stopPlayback();
		showLoading();
		mEpisodeIndex -= 1;
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
		if (mProd_src == null || mProd_src.length() == 1
				|| "null".equals(mProd_src)) {
			mResourceTextView.setText("PPTV");
		} else {
			String strSrc = "";
			if (mProd_src.equalsIgnoreCase("wangpan")) {
				strSrc = "PPTV";
			} else if (mProd_src.equalsIgnoreCase("le_tv_fee")) {
				strSrc = "乐视";
			} else if (mProd_src.equalsIgnoreCase("letv")) {
				strSrc = "乐视";
			} else if (mProd_src.equalsIgnoreCase("fengxing")) {
				strSrc = "风行";
			} else if (mProd_src.equalsIgnoreCase("qiyi")) {
				strSrc = "爱奇艺";
			} else if (mProd_src.equalsIgnoreCase("youku")) {
				strSrc = "优酷";
			} else if (mProd_src.equalsIgnoreCase("sinahd")) {
				strSrc = "新浪视频";
			} else if (mProd_src.equalsIgnoreCase("sohu")) {
				strSrc = "搜狐视频";
			} else if (mProd_src.equalsIgnoreCase("qq")) {
				strSrc = "腾讯视频";
			} else if (mProd_src.equalsIgnoreCase("pptv")) {
				strSrc = "PPTV";
			} else if (mProd_src.equalsIgnoreCase("m1905")) {
				strSrc = "电影网";
			} else {
				strSrc = "PPTV";
			}
			mResourceTextView.setText(strSrc);
		}
		mLastTimeTextView.setText(UtilTools.formatDuration(lastTime));
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
				for (int i = 0; i < m_ReturnProgramView.movie.episodes[0].down_urls.length; i++) {
					String souces = m_ReturnProgramView.movie.episodes[0].down_urls[i].source;
					for (int j = 0; j < m_ReturnProgramView.movie.episodes[0].down_urls[i].urls.length; j++) {
						URLS_INDEX url = new URLS_INDEX();
						url.source_from = souces;
						url.defination_from_server = m_ReturnProgramView.movie.episodes[0].down_urls[i].urls[j].type;
						url.url = m_ReturnProgramView.movie.episodes[0].down_urls[i].urls[j].url;
						playUrls.add(url);
					}
				}
				break;
			case 2:
			case 131:
				if (mEpisodeIndex == -1) {
					for (int i = 0; i < m_ReturnProgramView.tv.episodes.length; i++) {
						if (mProd_sub_name
								.equals(m_ReturnProgramView.tv.episodes[i].name)) {
							mEpisodeIndex = i;
							for (int j = 0; j < m_ReturnProgramView.tv.episodes[i].down_urls.length; j++) {
								String souces = m_ReturnProgramView.tv.episodes[i].down_urls[j].source;
								for (int k = 0; k < m_ReturnProgramView.tv.episodes[i].down_urls[j].urls.length; k++) {
									URLS_INDEX url = new URLS_INDEX();
									url.source_from = souces;
									url.defination_from_server = m_ReturnProgramView.tv.episodes[i].down_urls[j].urls[k].type;
									url.url = m_ReturnProgramView.tv.episodes[i].down_urls[j].urls[k].url;
									playUrls.add(url);
								}
							}
						}
					}
				} else {
					for (int j = 0; j < m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls.length; j++) {
						String souces = m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].source;
						for (int k = 0; k < m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls.length; k++) {
							URLS_INDEX url = new URLS_INDEX();
							url.source_from = souces;
							url.defination_from_server = m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls[k].type;
							url.url = m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls[k].url;
							playUrls.add(url);
						}
					}
					mProd_sub_name = m_ReturnProgramView.tv.episodes[mEpisodeIndex].name;
				}
				break;
			case 3:
				if (mEpisodeIndex == -1) {
					for (int i = 0; i < m_ReturnProgramView.show.episodes.length; i++) {
						if (mProd_sub_name
								.equals(m_ReturnProgramView.show.episodes[i].name)) {
							mEpisodeIndex = i;
							for (int j = 0; j < m_ReturnProgramView.show.episodes[i].down_urls.length; j++) {
								String souces = m_ReturnProgramView.show.episodes[i].down_urls[j].source;
								for (int k = 0; k < m_ReturnProgramView.show.episodes[i].down_urls[j].urls.length; k++) {
									URLS_INDEX url = new URLS_INDEX();
									url.source_from = souces;
									url.defination_from_server = m_ReturnProgramView.show.episodes[i].down_urls[j].urls[k].type;
									url.url = m_ReturnProgramView.show.episodes[i].down_urls[j].urls[k].url;
									playUrls.add(url);
								}
							}
						}
					}
				} else {
					for (int j = 0; j < m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls.length; j++) {
						String souces = m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].source;
						for (int k = 0; k < m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls.length; k++) {
							URLS_INDEX url = new URLS_INDEX();
							url.source_from = souces;
							url.defination_from_server = m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls[k].type;
							url.url = m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls[k].url;
							playUrls.add(url);
						}
					}
					mProd_sub_name = m_ReturnProgramView.show.episodes[mEpisodeIndex].name;
				}
				break;
			}
			Log.d(TAG, "playUrls size ------->" + playUrls.size());
			for (int i = 0; i < playUrls.size(); i++) {
				URLS_INDEX url_index = playUrls.get(i);
				if (url_index.source_from.trim().equalsIgnoreCase(
						Constant.video_index[0])) {
					url_index.souces = 0;
				} else if (url_index.source_from.trim().equalsIgnoreCase(
						Constant.video_index[1])) {
					url_index.souces = 1;
				} else if (url_index.source_from.trim().equalsIgnoreCase(
						Constant.video_index[2])) {
					url_index.souces = 2;
				} else if (url_index.source_from.trim().equalsIgnoreCase(
						Constant.video_index[3])) {
					url_index.souces = 3;
				} else if (url_index.source_from.trim().equalsIgnoreCase(
						Constant.video_index[4])) {
					url_index.souces = 4;
				} else if (url_index.source_from.trim().equalsIgnoreCase(
						Constant.video_index[5])) {
					url_index.souces = 5;
				} else if (url_index.source_from.trim().equalsIgnoreCase(
						Constant.video_index[6])) {
					url_index.souces = 6;
				} else if (url_index.source_from.trim().equalsIgnoreCase(
						Constant.video_index[7])) {
					url_index.souces = 7;
				} else if (url_index.source_from.trim().equalsIgnoreCase(
						Constant.video_index[8])) {
					url_index.souces = 8;
				} else if (url_index.source_from.trim().equalsIgnoreCase(
						Constant.video_index[9])) {
					url_index.souces = 9;
				} else if (url_index.source_from.trim().equalsIgnoreCase(
						Constant.video_index[10])) {
					url_index.souces = 10;
				} else if (url_index.source_from.trim().equalsIgnoreCase(
						Constant.video_index[11])) {
					url_index.souces = 11;
				} else {
					url_index.souces = 12;
				}
				switch (mDefination) {
				case BangDanConstant.CHAOQING:// 超清
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
				case BangDanConstant.GAOQING:// 高清
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
				if (url_index.source_from
						.equalsIgnoreCase(Constant.BADDU_WANGPAN)) {
					Document doc = null;
					try {
						doc = Jsoup.connect(url_index.url).timeout(3000).get();
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
				}
			}
			if (playUrls.size() > 2) {
				Collections.sort(playUrls, new SouceComparatorIndex1());
				Collections.sort(playUrls, new DefinationComparatorIndex());
			}
			// url list 准备完成
			mHandler.sendEmptyMessage(MESSAGE_URLS_READY);
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(mStatue != STATUE_LOADING){
			SaveToServer();
		}
		super.onDestroy();
	}
	
	public void SaveToServer() {
		String url = Constant.BASE_URL + "program/play";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("app_key", Constant.APPKEY);// required string
												// 申请应用时分配的AppKey。
		params.put("prod_id", mProd_id);// required string
										// 视频id
										// prod_name
		// String titleName = " 第"
		// + (mCurrentPlayData.CurrentIndex + 1)
		// + "集";
		// mCurrentPlayData.prod_name= name + title;
		params.put("prod_name", mProd_name);// required
		params.put("prod_subname", mProd_name);
		// string 视频名字
//		switch (mProd_type) {
//		case 1: {
////			params.put(
////					"prod_subname",
////					m_ReturnProgramView.movie.episodes[mCurrentPlayData.CurrentIndex].name);
//			params.put(
//			"prod_subname",
//			"");
//		}
//			break;
//		case 131:
//		case 2: {
//			params.put(
//					"prod_subname",
//					mProd_name);
//		}
//			break;
//		case 3: {
//			params.put(
//					"prod_subname",
//					m_ReturnProgramView.show.episodes[tempCurrentPlayData.CurrentIndex].name);
//
//		}
//			break;
//		}
//		if (mCurrentPlayData != null && mCurrentPlayData.prod_type != 1) {
//			params.put("prod_subname",
//					Integer.toString(mCurrentPlayData.CurrentIndex + 1));// required
//		}

		// string
		// 视频的集数
		params.put("prod_type", mProd_type);// required int 视频类别
		// 1：电影，2：电视剧，3：综艺，4：视频
		params.put("playback_time", mVideoView.getCurrentPosition());// _time required int
													// 上次播放时间，单位：秒
		params.put("duration", mVideoView.getDuration());// required int 视频时长， 单位：秒
		params.put("play_type", "1");// required string
		// 播放的类别 1: 视频地址播放
		// 2:webview播放
		params.put("video_url", currentPlayUrl);// required
		// string
		// 视频url
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.SetHeader(app.getHeaders());
		cb.params(params).url(url).type(JSONObject.class)
				.weakHandler(this, "CallProgramPlayResult");
		aq.ajax(cb);
		
		//DB操作，把存储到服务器的数据保存到数据库
//		TvDatabaseHelper helper = TvDatabaseHelper
//				.newTvDatabaseHelper(getApplicationContext());
//		SQLiteDatabase database = helper.getWritableDatabase();// 获取写db
//		
//		String selection = UserShouCang.USER_ID + "=? and " + UserHistory.PRO_ID + "=?";// 通过用户id，找到相应信息
//		String[] selectionArgs = { UtilTools.getCurrentUserId(getApplicationContext()),prod_id };
//		
//		database.delete(TvDatabaseHelper.HISTORY_TABLE_NAME, selection,
//				selectionArgs);
//		
//		HotItemInfo info = new HotItemInfo();
//		info.prod_type = mProd_type + "";
//		info.prod_name = mProd_name;
//		if (tempCurrentPlayData != null) {
//			
//			info.prod_subname = (tempCurrentPlayData.CurrentIndex + 1) + "";
//		}
//		info.prod_id = mProd_type;
//		info.play_type = "1";
//		info.playback_time = playback_time + "";
//		info.video_url = tempCurrentPlayData.prod_url;
//		info.duration = duration + "";
//				
//		DBUtils.insertHotItemInfo2DB_History(getApplicationContext(), info,
//				UtilTools.getCurrentUserId(getApplicationContext()), database);
//		
//		helper.closeDatabase();
	}
}
