package com.joyplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joyplus.mediaplayer.JoyplusVideoView;
import com.joyplus.mediaplayer.MediaInfo;
import com.joyplus.mediaplayer.VideoViewInterface;
import com.joyplus.mediaplayer.VideoViewInterface.STATE;
import com.joyplus.tv.R;
import com.joyplus.tv.entity.URLS_INDEX;
/*videoview layout
 * It use to displayer media
 * it have: Android system VideoView 
 *          vitamio VideoView
 *          
 **/
public class JoyplusMediaPlayerVideoView implements JoyplusMediaPlayerInterface{

	private boolean Debug = true;
	private String  TAG   = "JoyplusMediaPlayerVideoView";
	
	private JoyplusMediaPlayerActivity mActivity;
	private VideoViewInterface         Player = null;
	private JoyplusVideoView           VideoView;
	public  static MediaInfo           CurrentMediaInfo;
	public  MediaInfo                  PreMediaInfo;	
	private LoadingWindows             mWaitingWindows;
	private final static int MSG_BASE  = 100;
	public  final static int LAYOUT_VIDEOVIEW = MSG_BASE+1;

	public void Init(){
		update();
		CurrentMediaInfo = new MediaInfo();
		PreMediaInfo     = new MediaInfo();
	}
    public JoyplusMediaPlayerVideoView(JoyplusMediaPlayerActivity activity){
    	mActivity = activity;
    	InitResource();
    }
    private void InitResource() {
		// TODO Auto-generated method stub
    	VideoView = (JoyplusVideoView) mActivity.findViewById(R.id.JoyplusVideoView);
    	//mWaitingDialog   = new RoundProcessDialog(mActivity);
    	mWaitingWindows  = new LoadingWindows();
	}
 
	public VideoViewInterface getPlayer(){
    	return Player;
    }
    public void update(){
    	if(Debug)Log.d(TAG,"VideoViewControl update()");
    	VideoView.Update();
        Player = VideoView.getVideoView();
    }
    
	@Override
	public boolean JoyplusdispatchMessage(Message msg) {
		// TODO Auto-generated method stub
		switch(msg.what){
		case JoyplusMediaPlayerActivity.MSG_MEDIAINFO:
			PreMediaInfo     = new MediaInfo(CurrentMediaInfo);
			CurrentMediaInfo = ((MediaInfo) msg.obj).CreateMediaInfo();
			return CheckMediaInfo(); 
		}
		return false;
	}
	private boolean CheckMediaInfo(){
		if(CurrentMediaInfo.getState().toInt()>STATE.MEDIA_STATE_INITED.toInt()
		&& CurrentMediaInfo.getState().toInt()<STATE.MEDIA_STATE_FINISH.toInt()
		&& CurrentMediaInfo.getState().toInt() != STATE.MEDIA_STATE_PUSE.toInt()){
			if( CurrentMediaInfo.getINFO() == 701 //loading
					&& CurrentMediaInfo.getCurrentTime()>1000
					&& JoyplusMediaPlayerActivity.StateOk
					&& CurrentMediaInfo.getState() != STATE.MEDIA_STATE_PUSE
					){
				mWaitingWindows.setVisible(true);
			}else{
				mWaitingWindows.setVisible(false);
			}
		}
		if(CurrentMediaInfo.getPath()==null || "".equals(CurrentMediaInfo.getPath()))return true;
		if(PreMediaInfo.getPath()!=null && !PreMediaInfo.getPath().equals(CurrentMediaInfo.getPath()))return true;
		return false;
	}
	@Override
	public boolean JoyplusonKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub 
		return false;
	}
	@Override
	public void JoyplussetVisible(boolean visible,int layout) {
		// TODO Auto-generated method stub
		if(Debug)Log.d(TAG,"setVisibliable("+visible+")");
    	if(visible){
    		update();
    	}else{
    		mWaitingWindows.setVisible(false);
    		VideoView.hideView();
    	}
	}
	@Override
	public int JoyplusgetLayout() {
		// TODO Auto-generated method stub
		return LAYOUT_VIDEOVIEW;
	}
	public boolean setLayoutParams(LinearLayout.LayoutParams params){
		return VideoView.setLayoutParams(params);
	}  
	public boolean hasMediaInfoChange(){
		long delay = Math.abs(CurrentMediaInfo.getCurrentTime()-PreMediaInfo.getCurrentTime());
		Log.e(TAG,"eeeeeeeeeeeeeeeee  "+delay +" eeeeeeeeeeee");
		return (delay<2000&&delay>=300);
	}
	private class LoadingWindows {
		private final static int MSG_SHOW = 1;
		private final static int MSG_HIDE = 2;
		
	    private TextView       mInfo;
	    private TextView       mNotify;
		private RelativeLayout mLayout;
		
		private long mStartRX = 0;
		private long rxByteslast = 0;
		
	    private class Time{
	    	private long TIME = 0;
	    	public long getTime(){
	    		return TIME;
	    	}
	    	public Time(long time){
	    		TIME = time;
	    	}
	    }
		private List<Time> TimeRecord = new ArrayList<Time>();
		private boolean    ShowNotify = false;
		public LoadingWindows(){
			mInfo   = (TextView)       mActivity.findViewById(R.id.joyplus_videoview_buffer_info);
			mNotify = (TextView)       mActivity.findViewById(R.id.joyplus_videoview_buffer_notify);
			mLayout = (RelativeLayout) mActivity.findViewById(R.id.joyplus_videoview_buffer);
		}
		public void setVisible(boolean Visible){
			CheckNotify(Visible);
			if(Visible)mHandler.removeCallbacksAndMessages(null);
			if((mLayout.getVisibility()==View.VISIBLE) == Visible)return;
			mHandler.removeCallbacksAndMessages(null);
			if(Visible)mHandler.sendEmptyMessage(MSG_SHOW);
			else       mHandler.sendEmptyMessage(MSG_HIDE);
		}
		private Handler mHandler = new Handler(){
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what){
				case MSG_SHOW:
					if(mLayout.getVisibility()==View.VISIBLE)return;
					mLayout.setVisibility(View.VISIBLE);					
					mHandler.removeCallbacksAndMessages(null);
					StartTrafficStates();					
					break;
				case MSG_HIDE:
					rxByteslast = 0;
					if(!(mLayout.getVisibility()==View.VISIBLE))return;
					mLayout.setVisibility(View.GONE);
					mHandler.removeCallbacksAndMessages(null);
					break;
				}
			}
		};
		private void StartTrafficStates(){
			Log.d("Jas","=========StartTrafficStates===========");
			long mStartRX = TrafficStats.getTotalRxBytes();// ��ȡ�����ٶ�
			rxByteslast = 0;
			if (mStartRX == TrafficStats.UNSUPPORTED) {
			     
			} else {
				mHandler.removeCallbacks(UpdateTrafficStats);
				mHandler.postDelayed(UpdateTrafficStats, 500);
			}
		}
		protected void CheckNotify(boolean show) {
			// TODO Auto-generated method stub
			long current = System.currentTimeMillis();
			if(show){	
				Iterator<Time> it = TimeRecord.iterator();
				while(it.hasNext()){
					Time time = it.next();
					if((int)(current - time.getTime())>10*1000){
						it.remove();
					}
				}		
				if(TimeRecord.size()>5)ShowNotify = true;
				  else ShowNotify = false;
			}
			TimeRecord.add(new Time(current));
		}
		private Runnable UpdateTrafficStats = new Runnable(){
			long beginTimeMillis, timeTakenMillis, m_bitrate;
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Log.d("Jas","-------------run-------------");
				long rxBytes = TrafficStats.getTotalRxBytes() - mStartRX;
				timeTakenMillis = System.currentTimeMillis() - beginTimeMillis;
				beginTimeMillis = System.currentTimeMillis();
				if(timeTakenMillis>0){
					m_bitrate = (rxBytes - rxByteslast) / timeTakenMillis;
					rxByteslast = rxBytes;
					UpdateInfo(m_bitrate);
				}	
				mHandler.removeCallbacks(UpdateTrafficStats);
				mHandler.postDelayed(UpdateTrafficStats, 500);
			}
		};
		private void UpdateInfo(long speed){
			Log.d("Jas","============= speed="+speed+" =========================");
			if(!(mLayout.getVisibility()==View.VISIBLE))return;
			mInfo.setVisibility(View.VISIBLE);
			if(speed>=0)
				mInfo.setText(mActivity.getApplicationContext().getString(R.string.meidaplayer_loading_string_buffer,speed));
			else
				mInfo.setText(mActivity.getApplicationContext().getString(R.string.meidaplayer_loading_string_buffer_loading));
			if(ShowNotify)
				mNotify.setVisibility(View.VISIBLE);
			else 
				mNotify.setVisibility(View.GONE);				
		}
	}
	

	@Override
	public boolean JoyplusonKeyLongPress(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}
