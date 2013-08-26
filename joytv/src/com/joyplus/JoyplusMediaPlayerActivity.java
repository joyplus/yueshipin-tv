package com.joyplus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.mediaplayer.JoyplusMediaPlayerListener;
import com.joyplus.mediaplayer.JoyplusMediaPlayerManager;
import com.joyplus.mediaplayer.MediaInfo;
import com.joyplus.mediaplayer.VideoViewInterface;
import com.joyplus.mediaplayer.VideoViewInterface.STATE;
import com.joyplus.tv.App;
import com.joyplus.tv.Constant;
import com.joyplus.tv.R;
import com.joyplus.tv.Service.Return.ReturnFengxingSecondView;
import com.joyplus.tv.Service.Return.ReturnFirstFengxingUrlView;
import com.joyplus.tv.Service.Return.ReturnProgramView;
import com.joyplus.tv.Service.Return.ReturnReGetVideoView;
import com.joyplus.tv.database.TvDatabaseHelper;
import com.joyplus.tv.entity.CurrentPlayDetailData;
import com.joyplus.tv.entity.HotItemInfo;
import com.joyplus.tv.entity.URLS_INDEX;
import com.joyplus.tv.utils.BangDanConstant;
import com.joyplus.tv.utils.DBUtils;
import com.joyplus.tv.utils.DefinationComparatorIndex;
import com.joyplus.tv.utils.JieMianConstant;
import com.joyplus.tv.utils.SouceComparatorIndex1;
import com.joyplus.tv.utils.URLUtils;
import com.joyplus.tv.utils.UtilTools;
import com.joyplus.tv.utils.DataBaseItems.UserHistory;
import com.joyplus.tv.utils.DataBaseItems.UserShouCang;
import com.umeng.analytics.MobclickAgent;

import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore.Video;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;


public class JoyplusMediaPlayerActivity extends Activity implements JoyplusMediaPlayerListener{
	
	private boolean Debug = true;
	private String  TAG   = "JoyplusMediaPlayerActivity";
	/*videoview layout      msg 100-199  level 3*/
	public JoyplusMediaPlayerVideoView     mVideoView;
	/*middle control layout msg 200-299  level 2*/
	public JoyplusMediaPlayerMiddleControl mMiddleControl;
	/*Top and bottom layout msg 300-399  level 1*/
	public JoyplusMediaPlayerBar           mTopBottomController;
	//private Handler mHandler = new Handler(){};
	/*msg 0-99*/
	public static final int   MSG_MEDIAINFO         = 0;
	public static final int   DELAY_SHOWVIEW        = 10*1000; //10s
	public static final int   MSG_UPDATEPLAYERINFO  = 1;
	public static final int   MSG_REQUSETPLAYERINFO = 2;
	
	public static final int   MSG_REQUESTPLAY       = 3;
	public static final int   MSG_REQUESTPAUSE      = 4;
	public static final int   MSG_REQUESTFORWARD    = 5;
	public static final int   MSG_REQUESTBACKWARD   = 6;
	
	public static final int   MSG_UPDATECURRENTINFO = 7;
	enum URLTYPE{
		UNKNOW (0), NETWORK (1), LOCAL (2);
		private int type;
		URLTYPE(int Type){
			type = Type;
		}
		public int toInt(){
			return type;
		}
	}
	
	public  static CurrentPlayerInfo mInfo;

	private boolean StateOk = false;//flog of player in loading or others
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.joyplus.tv.R.layout.joyplusvideoview);
		InitResource();
		initFromIntent(getIntent());
	}
	private class JoyplusFinish implements Runnable{
		@Override 
        public void run() {
			finish();
			JoyplusMediaPlayerManager.getInstance().unregisterListener(JoyplusMediaPlayerActivity.this);
			mTopBottomController.JoyplussetVisible(false, 0);
        	mMiddleControl.JoyplussetVisible(false, 0);
        	mVideoView.JoyplussetVisible(false, 0);
        }
	};
	private void finishActivity(){
		new JoyplusFinish().run();
	}
	private Handler MiniHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case JoyplusMediaPlayerMiddleControlMini.MSG_PAUSEPLAY:
				mVideoView.getPlayer().PauseVideo();
				return;
			case JoyplusMediaPlayerMiddleControlMini.MSG_KEYDOWN_CENTER:
				finishActivity();
				break;
			case JoyplusMediaPlayerMiddleControlMini.MSG_KEYDOWN_LEFT:
				if(mInfo.mType == URLTYPE.NETWORK && mInfo.getHavePre()){
					InitUI();
					if (mProd_type == 3) 
					    mEpisodeIndex +=1;
					else
						mEpisodeIndex -=1;
					mHandler.sendEmptyMessage(MESSAGE_RETURN_DATE_OK);
				}
				break;
			case JoyplusMediaPlayerMiddleControlMini.MSG_KEYDOWN_TOP:
				if(mVideoView.getPlayer() != null){
					mVideoView.getPlayer().StartVideo();
				}
				mMiddleControl.JoyplussetVisible(false, JoyplusMediaPlayerMiddleControl.LAYOUT_MINI);
				break;
			case JoyplusMediaPlayerMiddleControlMini.MSG_KEYDOWN_RIGHT:
				if(mInfo.mType == URLTYPE.NETWORK && mInfo.getHaveNext()){
					InitUI();
					if (mProd_type == 3)
						mEpisodeIndex -= 1;
					else
						mEpisodeIndex += 1;
					mHandler.sendEmptyMessage(MESSAGE_RETURN_DATE_OK);
				}
				break;
			case JoyplusMediaPlayerMiddleControlMini.MSG_KEYDOWN_BOTTOM:
				Log.d(TAG,"eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
				if(mInfo.mCollection!=0)
				    mInfo.setCollection(false);
				else 
					mInfo.setCollection(true);
				if(mVideoView.getPlayer() != null){
					mVideoView.getPlayer().StartVideo();
				}
				mMiddleControl.JoyplussetVisible(false, JoyplusMediaPlayerMiddleControl.LAYOUT_MINI);
				break;
			case JoyplusMediaPlayerMiddleControlMini.MSG_KEYDOWN_PAUSEPLAY:
				if(mVideoView.getPlayer() != null){
					mVideoView.getPlayer().StartVideo();
				}
				mMiddleControl.JoyplussetVisible(false, JoyplusMediaPlayerMiddleControl.LAYOUT_MINI);
				break;
			}			
		}
	};
    @Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		mHandler.removeCallbacksAndMessages(null);
		m_ReturnProgramView = null;
		initFromIntent(intent);
	}

	private void InitResource() {
		// TODO Auto-generated method stub
    	if(Debug)Log.d(TAG,"InitResource()");
    	mInfo  = new CurrentPlayerInfo();
    	JoyplusMediaPlayerMiddleControlMini.setHandler(MiniHandler);
    	try {
			JoyplusMediaPlayerManager.Init(this);
			JoyplusMediaPlayerManager.getInstance().registerListener(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	mVideoView           = new JoyplusMediaPlayerVideoView(this);
    	mMiddleControl       = (JoyplusMediaPlayerMiddleControl) this.findViewById(R.id.JoyplusMediaPlayerMiddleControl);
    	mTopBottomController = new JoyplusMediaPlayerBar(this);
    	registerReceiver(mReceiver, new IntentFilter(Constant.VIDEOPLAYERCMD));
	}
	private void InitUI(){
		StateOk = false;
		mVideoView.Init();
    	mTopBottomController.Init();
    	mMiddleControl.Init();
    	CurrentPlayerInfo Info  = new CurrentPlayerInfo(mInfo);
    	mInfo = null;
    	mInfo = new CurrentPlayerInfo();
    	mInfo.mType = Info.mType;
    	mInfo.NotifyPlayerInfo(); 
    	if(mVideoView.getPlayer() == null){
    		finishActivity();
    		return;
    	}
	}
    private void initFromIntent(Intent intent){
    	InitUI();
    	if(intent != null && intent.getData() != null){
    		mInfo.mType = URLTYPE.LOCAL;
    		CreateLocal(intent.getData());
    	}else{
    		mInfo.mType = URLTYPE.NETWORK;
    		Create();
    	}
    }
    /*Interface of get the videoview of which used*/
    public VideoViewInterface getPlayer(){
    	return mVideoView.getPlayer();
    }
	private boolean JoyplusdispatchMessage(Message msg){
		//if(Debug)Log.d(TAG,"JoyplusdispatchMessage()");
		if(!mMiddleControl.JoyplusdispatchMessage(msg))
			if(!mTopBottomController.JoyplusdispatchMessage(msg))
				if(!mVideoView.JoyplusdispatchMessage(msg))
				       return false;
		return true;
	}
	private boolean JoyplusonKeyDown(int keyCode, KeyEvent event){
		if(Debug)Log.d(TAG,"JoyplusonKeyDown() "+keyCode);
		if(!mMiddleControl.JoyplusonKeyDown(keyCode, event))
		    if(!mTopBottomController.JoyplusonKeyDown(keyCode, event))
			   return false;
		return true;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(!StateOk){
			//when loading ,we only can finish it.
			if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == 111)finishActivity();;
			return true;
		}
		if(JoyplusonKeyDown(keyCode,event))return true;
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			if(mProd_type == 1 || mInfo.mType == URLTYPE.LOCAL){//movie
				finishActivity();return true; 
			}
			JoyplusMediaPlayerMiddleControlMini.setLayout(JoyplusMediaPlayerMiddleControlMini.LAYOUT_SWITCH);
			mMiddleControl.JoyplussetVisible(true, JoyplusMediaPlayerMiddleControl.LAYOUT_MINI);
			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			mTopBottomController.JoyplussetVisible(true, 0);
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_VOLUME_MUTE:
			mMiddleControl.JoyplussetVisible(true, JoyplusMediaPlayerMiddleControl.LAYOUT_AUDIO);
			if(mMiddleControl.JoyplusonKeyDown(keyCode, event))return true;
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			JoyplusMediaPlayerMiddleControlMini.setLayout(JoyplusMediaPlayerMiddleControlMini.LAYOUT_PAUSEPLAY);
			mMiddleControl.JoyplussetVisible(true, JoyplusMediaPlayerMiddleControl.LAYOUT_MINI);
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	private Handler VIDEOPLAYERCMD_Handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case MSG_REQUESTPLAY:
				break;
			case MSG_REQUESTPAUSE:
				break;
			case MSG_REQUESTFORWARD:
				break;
			case MSG_REQUESTBACKWARD:
				finishActivity();
				break;
			}
		}
	};
	
	@Override
	public void MediaInfo(MediaInfo info) {
		// TODO Auto-generated method stub
		if((info.getState().toInt()>=STATE.MEDIA_STATE_INITED.toInt()&&!StateOk)){ 
			mVideoView.getPlayer().StartVideo();
			if(mVideoView.hasMediaInfoChange()){
				mMiddleControl.JoyplussetVisible(false, 0);
				mTopBottomController.JoyplussetVisible(true, 0);
				StateOk = true;//now we can dispatch keydown or others operation
			}
		}
		mInfo.mState = info.CreateMediaInfo().getState();
		//if(mInfo.mState == STATE.MEDIA_STATE_FINISH)MediaCompletion();
		Message m = new Message();
		m.what    = MSG_MEDIAINFO;
		m.obj     = info;
		JoyplusdispatchMessage(m);
	}
	@Override
	public void MediaCompletion() {
		// TODO Auto-generated method stub
		if(mInfo.mType == URLTYPE.NETWORK){
			autoPlayNext();
		}else{//local media should be exit
			finishActivity();
		}
	}
	@Override
	public void ErrorInfo() {
		// TODO Auto-generated method stub
		Log.e(TAG,"+++++++++++++++++ERROR+++++++++++++++");
		finishActivity();
	}
	@Override
	public void NoProcess(String commend) {
		// TODO Auto-generated method stub
		Log.i(TAG,"Commend ("+commend+") is no disptch !!!");
	} 
	/* follow was use to handle the local resource
	 * 
	 * */
	private void CreateLocal( Uri uri){
		String scheme = uri.getScheme();
        if (scheme.equals("content")) {
			try {
				initFromContentUri(uri);
				mVideoView.getPlayer().SetVideoPaths(uri.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				onCreateDialog(0);//the uri was bad we can finish this.
				e.printStackTrace();
			}
        } else if (uri.getScheme().equals("file")) {
        	mInfo.mPlayerName = uri.getPath();
        	mVideoView.getPlayer().SetVideoPaths(uri.toString());
        }
        mInfo.NotifyPlayerInfo();
	}
	
	private String getMediaType(String string){
		String extension = MimeTypeMap.getFileExtensionFromUrl(string);
        if (TextUtils.isEmpty(extension)) {
            // getMimeTypeFromExtension() doesn't handle spaces in filenames nor can it handle
            // urlEncoded strings. Let's try one last time at finding the extension.
            int dotPos = string.lastIndexOf('.');
            if (0 <= dotPos) {
                extension = string.substring(dotPos + 1);
            }
        }
        return extension;
	}
	 private void initFromContentUri(Uri uri) throws Exception {
	        ContentResolver cr = this.getContentResolver();
	        Cursor c = cr.query(uri, new String[] {Video.Media.DATA}, null, null, null);
	        if (c != null) {
	            try {
	                if (c.moveToFirst()) {
	                    String path;
	                    try {
	                        // Local videos will have a data column
	                        path = c.getString(0);
	                    } catch (IllegalArgumentException e) {
	                        // For non-local videos, the path is the uri
	                        path = uri.toString();
	                    }
	                    mInfo.mPlayerName = path.substring(path.lastIndexOf('/') + 1);
	                } else {
	                    throw new Exception("Nothing found: " + uri);
	                }
	            } finally {
	                c.close();
	            }
	        } else {
	            throw new Exception("Bad URI: " + uri);
	        }
	    }

	/*
	 * this class was use to interface the player param before (VideoPlayerJPActivity)
	 * 
	 */
	public class CurrentPlayerInfo  implements Parcelable{
		public String  mPlayerUri  = "";             //the uri which was use to play
		public String  mPlayerName = "";             //the name of media
		public String  mQua        = "Unknow";       //the qua of madia 720P/1080P/Unknow
		public URLTYPE mType       = URLTYPE.UNKNOW; //the flog of this resource from
		public int     mCollection = 0 ;             //false =0 else true
		public STATE   mState      = STATE.MEDIA_STATE_UNKNOW;//current state
	    
		public int     mHaveNext   = 0 ;             //false =0 else true
		public int     mHavePre    = 0 ;             //false =0 else true
		public int     mLastTime   = 0 ;
		public String  mFrom       = "";
		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}
		public CurrentPlayerInfo(){
		}
		public CurrentPlayerInfo(CurrentPlayerInfo info){
			if(info != null){
				mPlayerUri  = info.mPlayerUri;
				mPlayerName = info.mPlayerName;
				mQua        = info.mQua;
				mType       = info.mType;
				mCollection = info.mCollection;
				mState      = info.mState;
				mHaveNext   = info.mHaveNext;
				mHavePre    = info.mHavePre;
				mLastTime   = info.mLastTime;
				mFrom       = info.mFrom;
			}
		}
		public CurrentPlayerInfo CreateInfo(){
			return new CurrentPlayerInfo(this);
		}
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			// TODO Auto-generated method stub
			dest.writeString(mPlayerUri);
			dest.writeString(mPlayerName);
			dest.writeString(mQua);
			dest.writeInt(mType.toInt());
			dest.writeInt(mCollection);
			dest.writeInt(mState.toInt());
			dest.writeInt(mHaveNext);
			dest.writeInt(mHavePre);
			dest.writeInt(mLastTime);
			dest.writeString(mFrom);
		}
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			StringBuffer sb = new StringBuffer();
	        sb.append("CurrentPlayerInfo{ mPlayerUri: ").append(mPlayerUri).
	           append(", mPlayerName: ").append(mPlayerName).
	           append(", mQua: ").append(mQua).
	           append(", mType: ").append(mType.toInt()).
	           append(", mCollection: ").append((mCollection==0)?false:true).
	           append(", mLastTime: ").append(mLastTime).
	           append(", mFrom: ").append(mFrom).
	           append("} ");
	        return sb.toString();
		}
		public boolean getHaveNext(){
			if(m_ReturnProgramView ==null )return false;
			if (mProd_type == 3){
				if (mEpisodeIndex > 0&&m_ReturnProgramView.show.episodes[mEpisodeIndex-1].down_urls!=null) {
					return true;
				}
			}else if(mProd_type == 2 || mProd_type == 131){
				if (mEpisodeIndex < (m_ReturnProgramView.tv.episodes.length - 1)&&m_ReturnProgramView.tv.episodes[mEpisodeIndex+1].down_urls!=null) {
					return true;
				}
			}
			return false;
		}
		public boolean getHavePre(){
			if (mProd_type == 3){
				if (mEpisodeIndex < (m_ReturnProgramView.show.episodes.length - 1)&&m_ReturnProgramView.show.episodes[mEpisodeIndex+1].down_urls!=null) {
					return true;
				}
			}else if(mProd_type == 2 || mProd_type == 131){
				if (mEpisodeIndex > 0&&m_ReturnProgramView.tv.episodes[mEpisodeIndex-1].down_urls!=null) {
					return true;
				}
			}
			return false;
		}
		public void setCollection(boolean collection){
			Log.d(TAG,"1111111111111111111111111111 "+collection);
			String url = "";
			if(collection)
				url = Constant.BASE_URL + "program/favority";
			else
				url = Constant.BASE_URL + "program/unfavority";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("prod_id", mProd_id);
			AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
			cb.SetHeader(app.getHeaders());
			if(collection)
			    cb.params(params).url(url).type(JSONObject.class).weakHandler(JoyplusMediaPlayerActivity.this, "favorityResult");
			else 
				cb.params(params).url(url).type(JSONObject.class).weakHandler(JoyplusMediaPlayerActivity.this, "unfavorityResult");
			aq.ajax(url,JSONObject.class,cb);
		}
		private void NotifyPlayerInfo(){
			Message m = new Message();
			m.what    = MSG_UPDATEPLAYERINFO;
			m.obj     = new CurrentPlayerInfo(this);
			JoyplusdispatchMessage(m);
		}
	}
	
	
	
	
	
	/***********************************************************
	 * follow was copy from VideoPlayerJPActivity
	 * it don't want to change 
	 * ************************************************************/
	private static final int MESSAGE_RETURN_DATE_OK = 0;
	private static final int MESSAGE_URLS_READY = MESSAGE_RETURN_DATE_OK + 1;
	private static final int MESSAGE_PALY_URL_OK = MESSAGE_URLS_READY + 1;
	private static final int MESSAGE_URL_NEXT = MESSAGE_PALY_URL_OK + 1;
	private static final int MESSAGE_UPDATE_PROGRESS = MESSAGE_URL_NEXT + 1;
	private static final int MESSAGE_HIDE_PROGRESSBAR = MESSAGE_UPDATE_PROGRESS + 1;
	private static final int MESSAGE_HIDE_VOICE = MESSAGE_HIDE_PROGRESSBAR + 1;
	
	private String mProd_id;
	private String mProd_name;
	private int    mProd_type;
	private String mProd_src;// 来源
	
	private String url_temp; //首次url备份
	private int    mDefination = 0; // 清晰度 6为尝鲜，7为普清，8为高清
	private String mProd_sub_name = null;
	private int    mEpisodeIndex = -1; // 当前集数对应的index
	private long   lastTime = 0;

	
	private boolean isShoucang = false;// 默认为没有收藏

	/**
	 * 网络数据
	 */
	private int               currentPlayIndex;
	private String            currentPlayUrl;
	private ReturnProgramView m_ReturnProgramView = null;
	private List<URLS_INDEX>  playUrls = new ArrayList<URLS_INDEX>();
	private AQuery            aq;
	private App               app;
	private boolean           isOnlyExistFengXing = false;
	private boolean           isOnlyExistLetv = false;
    private String            sourceFromUrl = null;//当前集的原始播放地址
	
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
					VIDEOPLAYERCMD_Handler.sendEmptyMessage(MSG_REQUESTPLAY);
					break;
				case 405:
					VIDEOPLAYERCMD_Handler.sendEmptyMessage(MSG_REQUESTPAUSE);
					break;
				case 407:
					VIDEOPLAYERCMD_Handler.sendEmptyMessage(MSG_REQUESTFORWARD);
				case 409:
					VIDEOPLAYERCMD_Handler.sendEmptyMessage(MSG_REQUESTBACKWARD);
					break;
				}
			} 
		}
	};

	
	private void Create() {
		// TODO Auto-generated method stub
		aq = new AQuery(this);
		app = (App) getApplication();
		m_ReturnProgramView = app.get_ReturnProgramView();
		initVedioDate();
		// 获取是否收藏
		getIsShoucangData();
	}

	private void initVedioDate() {
		// 点击某部影片播放时，会全局设置CurrentPlayData
		CurrentPlayDetailData playDate = app.getmCurrentPlayDetailData();
		if (playDate == null) {// 如果不设置就不播放
			//change by Jas@20130816
			finishActivity();
			return;
		}
		// 初始化基本播放数据
		mProd_id       = playDate.prod_id;
		mProd_type     = playDate.prod_type;
		mProd_name     = playDate.prod_name;
		mProd_sub_name = playDate.prod_sub_name;
		currentPlayUrl = playDate.prod_url;
		url_temp       = playDate.prod_url;
		mDefination    = playDate.prod_qua;
		lastTime       = (int) playDate.prod_time;
		mProd_src      = playDate.prod_src;
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
		Log.d(TAG, "defination----->" + mDefination);
		if(lastTime<=0){
			String lastTimeStr = DBUtils.getDuartion4HistoryDB(
					getApplicationContext(),
					UtilTools.getCurrentUserId(getApplicationContext()), mProd_id,mProd_sub_name);
			Log.i(TAG, "DBUtils.getDuartion4HistoryDB-->lastTimeStr:" + lastTimeStr);
			if (lastTimeStr != null && !lastTimeStr.equals("")) {
				try {
					long tempTime = Integer.valueOf(lastTimeStr);
					Log.i(TAG, "DBUtils.getDuartion4HistoryDB-->time:" + tempTime);
					if (tempTime != 0) {
						lastTime = tempTime * 1000;
						//add by Jas
						mInfo.mLastTime = (int) lastTime;
						//end add by Jas
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
				if(playUrls.size()<=0)return;
				currentPlayIndex = 0;
				currentPlayUrl = playUrls.get(currentPlayIndex).url;
				mProd_src = playUrls.get(currentPlayIndex).source_from;
				Log.i(TAG, "MESSAGE_URLS_READY--->" + currentPlayUrl + " isOnlyExistFengXing--->" + isOnlyExistFengXing
						+" sourceFromUrl--->" + sourceFromUrl);
				if (currentPlayUrl != null
						&& URLUtil.isNetworkUrl(currentPlayUrl)) {					
					if(isOnlyExistFengXing && sourceFromUrl != null){//						
						String url = URLUtils.getFexingParseUrlURL(Constant.FENGXING_REGET_FIRST_URL, sourceFromUrl);
						getFengxingParseServiceData(url);						
					}else {//只存在风行的地址						
						// 地址跳转相关。。。
						new Thread(new UrlRedirectTask()).start();
//						mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
						// 要根据不同的节目做相应的处理。这里仅仅是为了验证上下集
					}
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
							Log.d(TAG, currentPlayUrl);
							Log.d(TAG, mProd_src);
							new Thread(new UrlRedirectTask()).start();
//							mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
						}
					} else {
						// 所有的片源都不能播放
						Log.e(TAG, "no url can play!--->");						
						if(isOnlyExistLetv && m_ReturnProgramView != null
								&& sourceFromUrl != null){
							String url = URLUtils.
									getParseUrlURL(Constant.LETV_PARSE_URL_URL, sourceFromUrl, mProd_id, mProd_sub_name);
							Log.i(TAG, "sourceUrl--->" + sourceFromUrl + " url---->" + url);
							getLetvParseServiceData(url);
						}else {							
							noUrlCanPlay();
						}
					}
				}
				break;
			case MESSAGE_PALY_URL_OK:
				updateName();
				updateSourceAndTime();
				//add by Jas@20130816 for player
				mInfo.mPlayerUri = currentPlayUrl;
				mVideoView.getPlayer().SetVideoPaths(currentPlayUrl);
				if(lastTime>0){
					mVideoView.getPlayer().SeekVideo((int)lastTime);
				}
				//mVideoView.getPlayer().StartVideo();
				break;
			default:
				break;
			}
		}
	};
	
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
		if (json == null || json.equals("")){
			mHandler.sendEmptyMessage(MESSAGE_PALY_URL_OK);
			return;
		}
		Log.d(TAG, "initFengxingParseServiceData= " + json.toString());		
		ObjectMapper mapper = new ObjectMapper();
		try {
			ReturnFirstFengxingUrlView returnFirstFengxingUrlView = mapper.readValue(json.toString(),
					ReturnFirstFengxingUrlView.class);			
			if(returnFirstFengxingUrlView != null){
				if(returnFirstFengxingUrlView.error != null && 
						returnFirstFengxingUrlView.error.equals("false")){
					String sourceQua = defintionToType(mDefination);
					if(returnFirstFengxingUrlView.video_infos != null &&
							returnFirstFengxingUrlView.video_infos.length > 0){						
						for(int i=0;i<returnFirstFengxingUrlView.video_infos.length;i++){							
							if(returnFirstFengxingUrlView.video_infos[i]!= null 
									&& returnFirstFengxingUrlView.video_infos[i].type != null){								
								if(sourceQua.equals(returnFirstFengxingUrlView.video_infos[i].type)){									
									String tempUrl = returnFirstFengxingUrlView.video_infos[i].request_url;
									Log.i(TAG, "tempUrl--->" + tempUrl);
									getFenxingNetServiceData(tempUrl);
									return;
								}
							}							
						}
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
	
	private int typeToTypedefintion(String type){		
		int defintion = 8;		
		if(type != null && !type.equals("")){			
			if(type.equals("hd2")){				
				return 8;
			}else if(type.equals("mp4")){				
				return 7;
			}else if(type.equals("flv")){				
				return 6;
			}
		}		
		return defintion;
	}
	
	private void noUrlCanPlay(){		
		if(!JoyplusMediaPlayerActivity.this.isFinishing()){			
			//所有url不能播放，向服务器传递-1
			saveToServer(-1, 0);
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
		if (json == null || json.equals("")){
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
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, interfaceName);
		Map<String, Object> params = new HashMap<String, Object>();
		try {
			HttpEntity entity = new StringEntity(json.toString());
			params.put(AQuery.POST_ENTITY, entity);
			cb.params(params);
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "application/json");
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
		if (json == null || json.equals("")){
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
						urls_INDEX.source_from = Constant.video_index[3];
						urls_INDEX.defination_from_server = type;
						urls_INDEX.url = returnFengxingSecondView.urls[i];
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
			if(reGetVideoView != null && reGetVideoView.down_urls != null
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
						Log.i(TAG, "urls_INDEX--->" + urls_INDEX.toString());
						playUrls.add(urls_INDEX);
					}
				}
				if(maxQuality != -1 && isOnlyExistLetv){
					if(maxQuality == mDefination){
						mDefination = 8;
					}
				}
				sequenceList();
				for(int i=0;i<playUrls.size();i++){
					Log.i(TAG, "playUrls--->" + playUrls.get(i).defination_from_server);
				}
				// url list 准备完成
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

	private void updateName() {
		switch (mProd_type) {
		case -1:
		case 1:
			//mVideoNameText.setText(mProd_name);
			mInfo.mPlayerName = mProd_name;
			break;
		case 2:
		case 131:
			//mVideoNameText.setText(mProd_name + " 第" + mProd_sub_name + "集");
			mInfo.mPlayerName = mProd_name+" 第" + mProd_sub_name + "集";
			break;
		case 3:
			//mVideoNameText.setText(mProd_name + " " + mProd_sub_name);
			mInfo.mPlayerName = mProd_name+" "+ mProd_sub_name;
			break;
		default:
			mInfo.mPlayerName = "UnKnow ";
			break;
		}
		mInfo.NotifyPlayerInfo();
	}
		
	private void autoPlayNext(){
		switch (mProd_type) {
		case 1:
			finishActivity();
			break;
		case 2:
		case 131:
			if(mEpisodeIndex<m_ReturnProgramView.tv.episodes.length-1){
				playNext();
			}else{
				setResult2Xiangqing();//返回集数和是否收藏
				finishActivity();
			}
			break;
		case 3:
			if(mEpisodeIndex>0){
				playNext();
			}else{
				setResult2Xiangqing();//返回集数和是否收藏
				finishActivity();
			}
			break;
		default:
			finishActivity();
			break;
		}
	}
	private void playNext(){
		InitUI();
		lastTime = 0;
		if (mProd_type == 3) {
			mEpisodeIndex -= 1;
		} else {
			mEpisodeIndex += 1;
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
		String url = Constant.BASE_URL + "program/view" + "?prod_id="+ mProd_id;
		getServiceData(url, "initMovieDate");
	}

	public void initMovieDate(String url, JSONObject json, AjaxStatus status) {
		if (status.getCode() == AjaxStatus.NETWORK_ERROR || json == null) {
			app.MyToast(aq.getContext(),getResources().getString(R.string.networknotwork));
			return;
		}
		if (json == null || json.equals(""))
			return;
		Log.d(TAG, "data = " + json.toString());
		ObjectMapper mapper = new ObjectMapper();
		try {
			m_ReturnProgramView = null;
			m_ReturnProgramView = mapper.readValue(json.toString(),ReturnProgramView.class);
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
			//mResourceTextView.setText("");
			//add by Jas
			mInfo.mFrom = "";
		} else {
			String strSrc = "";
			if (mProd_src.equalsIgnoreCase("wangpan")) {
				strSrc = "PPTV";
			} else if (mProd_src.equalsIgnoreCase("le_tv_fee")) {
				strSrc = "乐  视";
			} else if (mProd_src.equalsIgnoreCase("letv")) {
				strSrc = "乐  视";
			} else if (mProd_src.equalsIgnoreCase("fengxing")) {
				strSrc = "风  行";
			} else if (mProd_src.equalsIgnoreCase("qiyi")) {
				strSrc = "爱  奇  艺";
			} else if (mProd_src.equalsIgnoreCase("youku")) {
				strSrc = "优  酷";
			} else if (mProd_src.equalsIgnoreCase("sinahd")) {
				strSrc = "新  浪  视  频";
			} else if (mProd_src.equalsIgnoreCase("sohu")) {
				strSrc = "搜  狐  视  频";
			} else if (mProd_src.equalsIgnoreCase("qq")) {
				strSrc = "腾  讯  视  频";
			} else if (mProd_src.equalsIgnoreCase("pptv")) {
				strSrc = "PPTV";
			} else if (mProd_src.equalsIgnoreCase("m1905")) {
				strSrc = "电  影  网";
			} else {
				strSrc = "PPTV";
			}
			//add by Jas
			mInfo.mFrom = strSrc;
			//mResourceTextView.setText(strSrc);
		}
		//add by Jas
		mInfo.mLastTime = (int) lastTime;
		//end add by Jas
		if(playUrls.size()>0&&currentPlayIndex<=playUrls.size()-1){
			Log.d(TAG, "type---->" + playUrls.get(currentPlayIndex).defination_from_server);
			//mDefinationIcon.setVisibility(View.VISIBLE);
			if(Constant.player_quality_index[0].equalsIgnoreCase(playUrls.get(currentPlayIndex).defination_from_server)){
				//mDefinationIcon.setImageResource(R.drawable.player_1080p);
				mInfo.mQua = "1080p";
			}else if(Constant.player_quality_index[1].equalsIgnoreCase(playUrls.get(currentPlayIndex).defination_from_server)){
				//mDefinationIcon.setImageResource(R.drawable.player_720p);
				mInfo.mQua = "720p";
			}else{
				//mDefinationIcon.setVisibility(View.INVISIBLE);
				mInfo.mQua = "Unknow";
			}
		}
		mInfo.NotifyPlayerInfo();
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
				
				if(m_ReturnProgramView.movie != null) {
					
					mProd_name = m_ReturnProgramView.movie.name;
					
					if(m_ReturnProgramView.movie.episodes != null 
							&& m_ReturnProgramView.movie.episodes.length > 0 
							&& m_ReturnProgramView.movie.episodes[0].down_urls != null) {
							
							if(m_ReturnProgramView.movie.episodes[0].video_urls != null
									&& m_ReturnProgramView.movie.episodes[0].video_urls.length ==1){
								if(m_ReturnProgramView.movie.episodes[0].video_urls[0]!= null){
									
									if(m_ReturnProgramView.movie.episodes[0].video_urls[0].source != null){
										
										if(m_ReturnProgramView.movie.episodes[0].video_urls[0].source.
												equals(Constant.video_index[1])||
												m_ReturnProgramView.movie.episodes[0].video_urls[0].source.
												equals(Constant.video_index[2])){
											
											isOnlyExistLetv = true;
										}else if(m_ReturnProgramView.movie.episodes[0].video_urls[0].source.
												equals(Constant.video_index[3])){
											
											isOnlyExistFengXing = true;
										}
									}
									
									if(isOnlyExistFengXing || isOnlyExistLetv){
										
										sourceFromUrl = m_ReturnProgramView.movie.episodes[0].video_urls[0].url;
									}
								}
							}
						
						for (int i = 0; i < m_ReturnProgramView.movie.episodes[0].down_urls.length; i++) {
							
							if(m_ReturnProgramView.movie.episodes[0].down_urls[i] != null) {
								
								String souces = m_ReturnProgramView.movie.episodes[0].down_urls[i].source;
								
								if(m_ReturnProgramView.movie.episodes[0].down_urls[i].urls != null) {
									
									for (int j = 0; j < m_ReturnProgramView.movie.episodes[0].down_urls[i].urls.length; j++) {
										
										if(m_ReturnProgramView.movie.episodes[0].down_urls[i].urls[j] != null) {
											
											URLS_INDEX url = new URLS_INDEX();
											url.source_from = souces;
											url.defination_from_server = m_ReturnProgramView.movie.episodes[0].down_urls[i].urls[j].type;
											url.url = m_ReturnProgramView.movie.episodes[0].down_urls[i].urls[j].url;
											playUrls.add(url);
										}

									}
								}
							}
						}
					}

				}
				
				break;
			case 2:
			case 131:
				
				if(m_ReturnProgramView.tv != null) {
					
					mProd_name = m_ReturnProgramView.tv.name;
					
					if(m_ReturnProgramView.tv.episodes != null) {
						
						if (mEpisodeIndex == -1) {
							for (int i = 0; i < m_ReturnProgramView.tv.episodes.length; i++) {
								if (m_ReturnProgramView.tv.episodes[i] != null 
										&& mProd_sub_name
										.equals(m_ReturnProgramView.tv.episodes[i].name)) {
									mEpisodeIndex = i;
									if(m_ReturnProgramView.tv.episodes[i].down_urls == null){
										mHandler.sendEmptyMessage(MESSAGE_URLS_READY);
										return; 
									}
									
									if(m_ReturnProgramView.tv.episodes[i].video_urls != null
											&& m_ReturnProgramView.tv.episodes[i].video_urls.length ==1){
										if(m_ReturnProgramView.tv.episodes[i].video_urls[0]!= null){
											
											if(m_ReturnProgramView.tv.episodes[i].video_urls[0].source != null){
												
												if(m_ReturnProgramView.tv.episodes[i].video_urls[0].source.
														equals(Constant.video_index[1])||
														m_ReturnProgramView.tv.episodes[i].video_urls[0].source.
														equals(Constant.video_index[2])){
													
													isOnlyExistLetv = true;
												}else if(m_ReturnProgramView.tv.episodes[i].video_urls[0].source.
														equals(Constant.video_index[3])){
													
													isOnlyExistFengXing = true;
												}
											}
											
											if(isOnlyExistFengXing || isOnlyExistLetv){
												
												sourceFromUrl = m_ReturnProgramView.tv.episodes[i].video_urls[0].url;
											}
										}
									}
									
									for (int j = 0; j < m_ReturnProgramView.tv.episodes[i].down_urls.length; j++) {
										
										if(m_ReturnProgramView.tv.episodes[i].down_urls[j] != null) {
											
											String souces = m_ReturnProgramView.tv.episodes[i].down_urls[j].source;
											
											if(m_ReturnProgramView.tv.episodes[i].down_urls[j].urls != null) {
												
												for (int k = 0; k < m_ReturnProgramView.tv.episodes[i].down_urls[j].urls.length; k++) {
													
													if(m_ReturnProgramView.tv.episodes[i].down_urls[j].urls[k] != null) {
														
														URLS_INDEX url = new URLS_INDEX();
														url.source_from = souces;
														url.defination_from_server = m_ReturnProgramView.tv.episodes[i].down_urls[j].urls[k].type;
														url.url = m_ReturnProgramView.tv.episodes[i].down_urls[j].urls[k].url;
														playUrls.add(url);
													}
												}
											}
										}

									}
								}
							}
						} else {
							
							if(m_ReturnProgramView.tv.episodes.length > mEpisodeIndex
									&& m_ReturnProgramView.tv.episodes[mEpisodeIndex] != null) {
								
								if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls != null
										&& m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls.length ==1){
									if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls[0]!= null){
										
										if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls[0].source != null){
											
											if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls[0].source.
													equals(Constant.video_index[1])||
													m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls[0].source.
													equals(Constant.video_index[2])){
												
												isOnlyExistLetv = true;
											}else if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls[0].source.
													equals(Constant.video_index[3])){
												
												isOnlyExistFengXing = true;
											}
										}
										
										if(isOnlyExistFengXing || isOnlyExistLetv){
											
											sourceFromUrl = m_ReturnProgramView.tv.episodes[mEpisodeIndex].video_urls[0].url;
										}
									}
								}
								
								if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls != null) {
									
									for (int j = 0; j < m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls.length; j++) {
										
										if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j] != null) {
											
											String souces = m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].source;
											
											if( m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls != null) {
												
												for (int k = 0; k < m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls.length; k++) {
													
													if(m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls[k] != null) {
														
														URLS_INDEX url = new URLS_INDEX();
														url.source_from = souces;
														url.defination_from_server = m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls[k].type;
														url.url = m_ReturnProgramView.tv.episodes[mEpisodeIndex].down_urls[j].urls[k].url;
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
				
				break;
			case 3:
				
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
										
										if(m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls != null
												&& m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls.length ==1){
											if(m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[0]!= null){
												
												if(m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[0].source != null){
													
													if(m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[0].source.
															equals(Constant.video_index[1])||
															m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[0].source.
															equals(Constant.video_index[2])){
														
														isOnlyExistLetv = true;
													}else if(m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[0].source.
															equals(Constant.video_index[3])){
														
														isOnlyExistFengXing = true;
													}
												}
												
												if(isOnlyExistFengXing || isOnlyExistLetv){
													
													sourceFromUrl = m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[0].url;
												}
											}
										}
										
										for (int j = 0; j < m_ReturnProgramView.show.episodes[i].down_urls.length; j++) {
											
											
											if(m_ReturnProgramView.show.episodes[i].down_urls[j] != null) {
												
												String souces = m_ReturnProgramView.show.episodes[i].down_urls[j].source;
												
												if(m_ReturnProgramView.show.episodes[i].down_urls[j].urls != null) {
													
													for (int k = 0; k < m_ReturnProgramView.show.episodes[i].down_urls[j].urls.length; k++) {
														
														if(m_ReturnProgramView.show.episodes[i].down_urls[j].urls[k] != null) {
															
															URLS_INDEX url = new URLS_INDEX();
															url.source_from = souces;
															url.defination_from_server = m_ReturnProgramView.show.episodes[i].down_urls[j].urls[k].type;
															url.url = m_ReturnProgramView.show.episodes[i].down_urls[j].urls[k].url;
															playUrls.add(url);
														}
													}
												}
											}
										}
									}
								}
							}
						} else {
							
							if(m_ReturnProgramView.show.episodes.length > mEpisodeIndex ) {
								
								if(m_ReturnProgramView.show.episodes[mEpisodeIndex] != null 
										&& m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls != null) {
									
									if(m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls != null
											&& m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls.length ==1){
										if(m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[0]!= null){
											
											if(m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[0].source != null){
												
												if(m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[0].source.
														equals(Constant.video_index[1])||
														m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[0].source.
														equals(Constant.video_index[2])){
													
													isOnlyExistLetv = true;
												}else if(m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[0].source.
														equals(Constant.video_index[3])){
													
													isOnlyExistFengXing = true;
												}
											}
											
											if(isOnlyExistFengXing || isOnlyExistLetv){
												
												sourceFromUrl = m_ReturnProgramView.show.episodes[mEpisodeIndex].video_urls[0].url;
											}
										}
									}
									
									for (int j = 0; j < m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls.length; j++) {
										
										if(m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j] != null) {
											
											String souces = m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].source;
											
											if(m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls != null) {
												
												for (int k = 0; k < m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls.length; k++) {
													
													if(m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls[k] != null) {
														
														URLS_INDEX url = new URLS_INDEX();
														url.source_from = souces;
														url.defination_from_server = m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls[k].type;
														url.url = m_ReturnProgramView.show.episodes[mEpisodeIndex].down_urls[j].urls[k].url;
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
			sequenceList();
			// url list 准备完成
			mHandler.sendEmptyMessage(MESSAGE_URLS_READY);
		}
	}
	
	private int maxQuality = -1;//最高清晰度
	
	private void sequenceList(){		
		Log.d(TAG, "playUrls size ------->" + playUrls.size() + " maxQuality--->" + maxQuality);		
		for (int i = 0; i < playUrls.size(); i++) {
			URLS_INDEX url_index = playUrls.get(i);
			if (url_index.source_from.trim().equalsIgnoreCase(
					Constant.video_index[0])) {
				url_index.souces = 0;
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					Constant.video_index[1])) {//le_tv_fee
				url_index.souces = 1;				
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					Constant.video_index[2])) {//letv
				url_index.souces = 2;
			} else if (url_index.source_from.trim().equalsIgnoreCase(
					Constant.video_index[3])) {//fengxing
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
			if (url_index.source_from
					.equalsIgnoreCase(Constant.BAIDU_WANGPAN)) {
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
			}
		}
		if (playUrls.size() > 1) {
			Collections.sort(playUrls, new SouceComparatorIndex1());
			Collections.sort(playUrls, new DefinationComparatorIndex());
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		MobclickAgent.onResume(this);
		super.onResume();
	}

	@Override
	protected void onPause() {		
		Log.i(TAG, "onPause--->");		
		// TODO Auto-generated method stub
		MobclickAgent.onPause(this);
		if (mProd_type > 0 && mVideoView.CurrentMediaInfo != null) {
			MediaInfo info       = mVideoView.CurrentMediaInfo.CreateMediaInfo();
			long duration        = info.getTotleTime();
			long curretnPosition = info.getCurrentTime();
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
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onDestroy--->");		
		unregisterReceiver(mReceiver);	
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
			app.MyToast(aq.getContext(),getResources().getString(R.string.networknotwork));
			return;
		}
		if (json == null || json.equals(""))return;
		Log.d(TAG, "data = " + json.toString());
		String flag = json.toString();
		if (!flag.equals("")) {
			if (flag.contains("true")) {
				isShoucang = true;
				mInfo.mCollection = 1;
			} else {
				isShoucang = false;
				mInfo.mCollection = 0;
			}
		} else {
			isShoucang = true;
			mInfo.mCollection = 1;
		}
	}

	public void favorityResult(String url, JSONObject json,AjaxStatus status) {
		Log.d(TAG,"222222222222favorityResult");
		if (json != null) {
			try {
				// woof is "00000",now "20024",by yyc
				if (json.getString("res_code").trim().equalsIgnoreCase("00000")) {
					app.MyToast(this, "收藏成功!");					
					isShoucang = true;	
					mInfo.mCollection = 1;
				} else {					
					isShoucang = true;
					mInfo.mCollection = 1;
					//mBottomButton.setBackgroundResource(R.drawable.player_btn_unfav);
					app.MyToast(this, "已收藏!");
				}					
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// ajax error, show error code
			if (status.getCode() == AjaxStatus.NETWORK_ERROR)
				app.MyToast(aq.getContext(),getResources().getString(R.string.networknotwork));
		}

	}

	public void unfavorityResult(String url, JSONObject json, AjaxStatus status) {
		Log.d(TAG," 2222222unfavorityResult ");
		if (json != null) {
			try {
				if (json.getString("res_code").trim().equalsIgnoreCase("00000")) {
					app.MyToast(this, "取消收藏成功!");
					//mBottomButton.setBackgroundResource(R.drawable.player_btn_fav);
					isShoucang = false;
					mInfo.mCollection = 0;
//					setResult(JieMianConstant.SHOUCANG_CANCEL);
				} else {					
					app.MyToast(this, "取消收藏失败!");
					isShoucang = true;
					mInfo.mCollection = 1;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// ajax error, show error code
			if (status.getCode() == AjaxStatus.NETWORK_ERROR)
				app.MyToast(this,getResources().getString(R.string.networknotwork));
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		 Dialog alertDialog = new AlertDialog.Builder(this). 
	                setTitle("提示"). 
	                setMessage("该视频无法播放"). 
	                setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							finishActivity();
						}
					}).create();
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
//			URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(),null);
//			HttpGet mHttpGet = new HttpGet(uri);
			HttpGet mHttpGet = new HttpGet(url.toURI());
			HttpResponse response = mAndroidHttpClient.execute(mHttpGet);
			StatusLine statusLine = response.getStatusLine();			
			int status = statusLine.getStatusCode();
			Log.i(TAG, "HTTP STATUS : " + status);			
			if (status == HttpStatus.SC_OK) {
				Log.i(TAG, "HttpStatus.SC_OK--->" + urlStr);
				// 正确的话直接返回，不进行下面的步骤
				mAndroidHttpClient.close();
				list.add(urlStr);				
				return;//后面不执行
			} else {				
				Log.i(TAG, "NOT HttpStatus.SC_OK--->" + urlStr);				
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
					mAndroidHttpClient.close();
					list.add(null);					
					return;//后面不执行
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block 
			e.printStackTrace();
		}	
	}
	
	
}
