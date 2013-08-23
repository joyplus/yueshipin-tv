package com.joyplus;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.joyplus.JoyplusMediaPlayerActivity.CurrentPlayerInfo;
import com.joyplus.mediaplayer.MediaInfo;
import com.joyplus.mediaplayer.VideoViewInterface.STATE;
import com.joyplus.tv.R;
import com.joyplus.tv.utils.UtilTools;

public class JoyplusMediaPlayerBar implements JoyplusMediaPlayerInterface{
    
	private boolean Debug = true;
	private String  TAG   = "JoyplusMediaPlayerBar";
	private JoyplusMediaPlayerActivity mActivity;
	private VideoViewController        mBottomBar;
	private VideoViewTopBar            mTopBar;
	
	private static final int MSG_BASE        = 300;
	private static final int MSG_SHOWVIEW    = MSG_BASE+1;
	private static final int MSG_HIDEVIEW    = MSG_BASE+2;
	private static final int MSG_UPDATETIME  = MSG_BASE+3;
	private static final int MSG_REQUESTSHOW = MSG_BASE+4;
	private static final int MSG_REQUESTHIDE = MSG_BASE+5;
	private static final int LAYOUT_BAR      = MSG_BASE+7;
	/*use to control seekbar*/
	enum SEEKTYPE{
		NORMAL , FORWARD , BACKWARD
	}
	enum SPEED{
		X0 ("x0"),X1 ("x1"), X2 ("x2"), X3 ("x3");
		private String speed;
		SPEED(String Speed){
			speed = Speed;
		}
		public String toString(){
			return speed;
		}
	}
	public int getIntSpeed(SPEED speed){
		if(speed == null)return 0;
		if(speed == SPEED.X3)return 3;
		if(speed == SPEED.X2)return 2;
		if(speed == SPEED.X1)return 1;
		if(speed == SPEED.X0)return 0;
		return 0;
	}
	public SPEED getNextSpeed(SPEED speed){
		if(speed == null)return null;
		if(speed == SPEED.X0){
			return SPEED.X1;
		}else if(speed == SPEED.X1){
			return SPEED.X2;
		}else if(speed == SPEED.X2){
			return SPEED.X3;
		}else if(speed == SPEED.X3){
			return SPEED.X3;
		}else{
			return SPEED.X0;
		}
	}
	public void Init(){
		mHandler.removeCallbacksAndMessages(null);
		mBottomBar.Init();
		mTopBar.Init();
	}
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case JoyplusMediaPlayerActivity.MSG_MEDIAINFO:
				mBottomBar.dispatchMessage(msg);
				break;
			case MSG_SHOWVIEW:
				mTopBar.setVisible(true);
				mBottomBar.setVisible(true);
				setVisible(false,JoyplusMediaPlayerActivity.DELAY_SHOWVIEW);
				break;
			case MSG_HIDEVIEW:
				mTopBar.setVisible(false);
				mBottomBar.setVisible(false);
				break;
			case MSG_REQUESTSHOW:
				setVisible(true,0);
				break;
			case MSG_REQUESTHIDE:
				setVisible(false,0);
				break;
			case JoyplusMediaPlayerActivity.MSG_UPDATEPLAYERINFO:
				mTopBar.UpdatePlayerInfo();
				break;
			}
		}
	};
	
	public JoyplusMediaPlayerBar(JoyplusMediaPlayerActivity context){
		mActivity  = context;
		mBottomBar = new VideoViewController();
		mTopBar    = new VideoViewTopBar();
		setVisible(true,0);
	}
	private void setVisible(boolean visible,int delay){
		Message m ;
		if(visible)
			m=Message.obtain(mHandler,MSG_SHOWVIEW,"MSG_SHOWVIEW");
		else
			m=Message.obtain(mHandler,MSG_HIDEVIEW,"MSG_HIDEVIEW");
		mHandler.removeCallbacksAndMessages("MSG_SHOWVIEW");
		mHandler.removeCallbacksAndMessages("MSG_HIDEVIEW");
		mHandler.sendMessageDelayed(m,delay);
	}
	
	/*add by Jas@20130812 for TopBar in JoyPlus VideoView
	 * it use to display Media name , media resolution .current time
	 * */
	private class VideoViewTopBar {
		
		private boolean     Debug = true;
		private String      TAG   = "VideoViewTopBar";
		
		private ImageView   MediaResolution;
		private TextView    MediaName;
		private TextView    Click;
		private RelativeLayout Layout; 
		public void Init(){
			Layout.setVisibility(View.VISIBLE);
		};
		public void UpdatePlayerInfo() {
			// TODO Auto-generated method stub
			if(Layout.getVisibility() == View.VISIBLE){
				 InitView();
			}
		}
		public VideoViewTopBar(){
			InitResource();
		}
		public void dispatchMessage(Message m){
			switch(m.what){
			case JoyplusMediaPlayerActivity.MSG_MEDIAINFO:
				 if(Layout.getVisibility() == View.VISIBLE){
					 MediaInfo info = ((MediaInfo) m.obj).CreateMediaInfo();
				 }
				 break;
			}
		}
		private Runnable UpdateTime = new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(Layout.getVisibility() == View.VISIBLE)InitView();
			}
		};
		public void setVisible(boolean Visiblility){
			if(Debug)Log.d(TAG,"setVisibility("+Visiblility+")");
			//if(((Layout.VISIBLE == View.VISIBLE)?true:false) == Visiblility)return;
			Layout.setVisibility(Visiblility?View.VISIBLE:View.GONE);
			if(Layout.getVisibility() == View.VISIBLE){
				InitView();
				mHandler.removeCallbacks(UpdateTime);
				mHandler.postDelayed(UpdateTime, 1000);
			}else
				mHandler.removeCallbacks(UpdateTime);
		}
		
		private void InitView() {
			// TODO Auto-generated method stub
			if(Layout.getVisibility() != View.VISIBLE)return;
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("H:mm");
			Click.setText(format.format(date));
			MediaName.setText(JoyplusMediaPlayerActivity.mInfo.mPlayerName);
			if("1080p".equals(JoyplusMediaPlayerActivity.mInfo.mQua)){
				MediaResolution.setImageResource(R.drawable.player_1080p);
				MediaResolution.setVisibility(View.VISIBLE);
			}else if("720p".equals(JoyplusMediaPlayerActivity.mInfo.mQua)){
				MediaResolution.setImageResource(R.drawable.player_720p);
				MediaResolution.setVisibility(View.VISIBLE);
			}else{
				MediaResolution.setVisibility(View.GONE);
			}
		}
		
		private void InitResource() {
			// TODO Auto-generated method stub
			Layout          = (RelativeLayout) mActivity.findViewById(R.id.mediacontroller_topbar);
			MediaName       = (TextView)       mActivity.findViewById(R.id.mediacontroller_topbar_playname);
			MediaResolution = (ImageView)      mActivity.findViewById(R.id.mediacontroller_topbar_resolution);
			Click           = (TextView)       mActivity.findViewById(R.id.mediacontroller_topbar_time);
			setVisible(true);
		}
	}
	
	/*Add by Jas@20130813 for add the BottomBar in JoyPlus VideoView 
	 * it depends on the MediaInfo which report from JoyPlusMediaPlayerStateTrack
	 * and depends on the layout of joyplusvideoview.xml 
	 * */
	private class VideoViewController {
        
		private boolean     Debug = true;
		private String      TAG   = "VideoViewController";
		
		//private ImageButton PlayOrPauseButton;
		private TextView    CurrentTimeView;
		private TextView    TotalTimeView;
		//private TextView    FileNameView;
		
		private SeekBar        SeekBar;
		private RelativeLayout Layout_Time;
		private RelativeLayout Layout_seek;
		private RelativeLayout Layout_Speed;
		private TextView       SpeedView;
	    private int            DefaultSpeedSpace = 60*100;//1min
	    private static final int OFFSET = 33;
		private int seekBarWidthOffset  = 40;
		public void Init(){
			mSeekBarType = SEEKTYPE.NORMAL;
			mSpeed       = SPEED.X0;
			setVisible(true);
			mHandler.removeCallbacks(QuickAdjustSeekBar);
			UpdateProgress(null);
			Layout_Time.setVisibility(View.VISIBLE);
		}
		
		private Runnable QuickAdjustSeekBar = new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(mSeekBarType == SEEKTYPE.NORMAL) return;
				int position =0;
				if(mSeekBarType == SEEKTYPE.FORWARD)
				    position  = SeekBar.getProgress()+DefaultSpeedSpace*getIntSpeed(mSpeed);
				else if(mSeekBarType == SEEKTYPE.BACKWARD)
					position  = SeekBar.getProgress()-DefaultSpeedSpace*getIntSpeed(mSpeed);
				if(position<0)position = 0;
				if(position>SeekBar.getMax())position = SeekBar.getMax();
				SeekBar.setProgress(position);
				UpdateProgress(null);
				mHandler.sendEmptyMessage(MSG_REQUESTSHOW);
				mHandler.removeCallbacks(QuickAdjustSeekBar);
				mHandler.postDelayed(QuickAdjustSeekBar, 200);
			}
		};
		/*it use to fast forward or fast backward*/
		private SEEKTYPE mSeekBarType;
		private SPEED    mSpeed;
		public void dispatchMessage(Message m){
			switch(m.what){
			case JoyplusMediaPlayerActivity.MSG_MEDIAINFO:
				 if(Layout_Time.getVisibility() == View.VISIBLE){
					 UpdateProgress(((MediaInfo) m.obj).CreateMediaInfo());
				 }
				 break;
			}
		}
		public boolean JoyplusonKeyDown(int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if(Layout_Time.getVisibility() == View.VISIBLE && (mActivity.getPlayer()!=null)){
				switch(keyCode){
				case KeyEvent.KEYCODE_DPAD_LEFT:
					if(mSeekBarType == SEEKTYPE.FORWARD)mSpeed = SPEED.X1;
					else if(mSpeed == SPEED.X3)
						return true;
					else mSpeed = getNextSpeed(mSpeed);
					mSeekBarType = SEEKTYPE.BACKWARD;					
					mHandler.removeCallbacks(QuickAdjustSeekBar);
					mHandler.postDelayed(QuickAdjustSeekBar, 200);
					return true;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					if(mSeekBarType == SEEKTYPE.BACKWARD)mSpeed = SPEED.X1;
					else if(mSpeed == SPEED.X3)
						return true;
					else mSpeed = getNextSpeed(mSpeed);
					mSeekBarType = SEEKTYPE.FORWARD;
					mHandler.removeCallbacks(QuickAdjustSeekBar);
					mHandler.postDelayed(QuickAdjustSeekBar, 200);
					return true;
				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_ENTER:
					if(mSeekBarType != SEEKTYPE.NORMAL){
						mSpeed   = SPEED.X0;
						mHandler.removeCallbacks(QuickAdjustSeekBar);
						mSeekBarType = SEEKTYPE.NORMAL;
						mActivity.getPlayer().SeekVideo(SeekBar.getProgress());
						return true;
					}
					break;
				case KeyEvent.KEYCODE_BACK:
				case 111://the keycode was be change to 111 ,but don't know where change
					mSeekBarType = SEEKTYPE.NORMAL;
					mSpeed       = SPEED.X0;
					mHandler.removeCallbacks(QuickAdjustSeekBar);
					mHandler.sendEmptyMessage(MSG_REQUESTHIDE);
					break;
				}
			} 
			return false;
		} 
		public VideoViewController(){
			InitResource();
		}
		public void setVisible(boolean Visiblility){
			if(Debug)Log.d(TAG,"setVisibility("+Visiblility+")");
			Layout_Time.setVisibility(Visiblility?View.VISIBLE:View.GONE);
			Layout_seek.setVisibility(Visiblility?View.VISIBLE:View.GONE);
		}
		private void InitResource(){
			if(Debug)Log.d(TAG,"VideoViewController InitResource()");
			Layout_Time       = (RelativeLayout)mActivity.findViewById(R.id.mediacontroller_bottombar);
			CurrentTimeView   = (TextView)      mActivity.findViewById(R.id.mediacontroller_bottombar_current_time);
			TotalTimeView     = (TextView)      mActivity.findViewById(R.id.mediacontroller_bottombar_total_time);
			SeekBar           = (SeekBar)       mActivity.findViewById(R.id.mediacontroller_bottombar_seekbar);
			Layout_seek       = (RelativeLayout)mActivity.findViewById(R.id.mediacontroller_bottombar_seek);
			Layout_Speed      = (RelativeLayout)mActivity.findViewById(R.id.mediacontroller_bottombar_time_fast);
			SpeedView         = (TextView)      mActivity.findViewById(R.id.mediacontroller_bottombar_time_fasttext);
		}
		public void UpdateProgress(MediaInfo info){
			//if(Debug)Log.d(TAG,"UpdateProgress()");
			if(mSeekBarType == SEEKTYPE.BACKWARD || mSeekBarType == SEEKTYPE.FORWARD){
				Layout_Speed.setVisibility(View.VISIBLE);
				if(mSeekBarType == SEEKTYPE.FORWARD)
					Layout_Speed.setBackgroundResource(R.drawable.play_time_right);
				else if(mSeekBarType == SEEKTYPE.BACKWARD)
					Layout_Speed.setBackgroundResource(R.drawable.play_time_left);
				CurrentTimeView.setText(getTimeString(SeekBar.getProgress()));
				SpeedView.setText(mSpeed.toString());
				updateSeekBar(null);
				return;
			}
			if(mSeekBarType != SEEKTYPE.NORMAL)return;
			Layout_Speed.setVisibility(View.GONE);
			if(info != null){Log.d(TAG,"UpdateProgress info="+info.toString());
				//if(info.getState().toInt()>=STATE.MEDIA_STATE_INITED.toInt()){
			    if(info.getState() == STATE.MEDIA_STATE_PUSE
						||info.getState() == STATE.MEDIA_STATE_PLAYING
						||info.getState() == STATE.MEDIA_STATE_INITED){
					CurrentTimeView.setText(getTimeString((int)info.getCurrentTime()));
					TotalTimeView.setText(getTimeString((int)info.getTotleTime()));
					updateSeekBar(info);
				}
			}else{
				CurrentTimeView.setText(getTimeString(0));
				TotalTimeView.setText(getTimeString(0));
				SeekBar.setMax(100);
				SeekBar.setProgress(0);
				updateSeekBar(null);
			}
		}
		private void updateSeekBar(MediaInfo info){		
			if (info != null && (info.getState().toInt()>=STATE.MEDIA_STATE_INITED.toInt()) && mSeekBarType == SEEKTYPE.NORMAL){
				SeekBar.setMax((int) info.getTotleTime());
				SeekBar.setProgress((int) info.getCurrentTime());
			}else if(info == null && mSeekBarType == SEEKTYPE.NORMAL){
				SeekBar.setMax(100);
				SeekBar.setProgress(0);
			}
			UpdateSeekTime();
		}
		private void UpdateSeekTime(){
			RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			parms.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);	
			if (SeekBar.getProgress()>0){
				double mLeft = (double) SeekBar.getProgress() / SeekBar.getMax()* (SeekBar.getMeasuredWidth() - seekBarWidthOffset) + OFFSET;
				parms.leftMargin = (int) mLeft;
			}else{
				parms.leftMargin = OFFSET;
			}
			parms.bottomMargin = 20 + 10;
			Layout_Time.setLayoutParams(parms);
		}
		private String getTimeString(int time){
			if(time<0)time = 0;
			StringBuffer sb = new StringBuffer();
			time/=1000;
	        sb.append(getString(time/(60*60)));
	        sb.append(":");
	        time%=(60*60);
	        sb.append(getString(time/60));
	        sb.append(":");
	        time%=60;
	        sb.append(getString(time));
	        return sb.toString();
		}
		private String getString(int time){
			StringBuffer sb = new StringBuffer();
			sb.append(time/10).append(time%10);
			return sb.toString();
		}
	}

	@Override
	public boolean JoyplusdispatchMessage(Message msg) {
		// TODO Auto-generated method stub
		mHandler.dispatchMessage(msg);
		return false;
	}
	@Override
	public boolean JoyplusonKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(mBottomBar.Layout_Time.getVisibility()== View.VISIBLE){
			if(mBottomBar.JoyplusonKeyDown(keyCode, event)){
				mHandler.sendEmptyMessage(MSG_REQUESTSHOW);
			}
			return true;
		}
		return false;
	}
	@Override
	public void JoyplussetVisible(boolean visible,int layout) {
		// TODO Auto-generated method stub
		if(Debug)Log.d(TAG,"setVisible("+visible+")");
		setVisible(visible,0);
	}
	@Override
	public int JoyplusgetLayout() {
		// TODO Auto-generated method stub
		return LAYOUT_BAR;
	}
}
