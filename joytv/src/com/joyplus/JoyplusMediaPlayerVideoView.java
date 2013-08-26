package com.joyplus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import com.joyplus.mediaplayer.JoyplusVideoView;
import com.joyplus.mediaplayer.MediaInfo;
import com.joyplus.mediaplayer.VideoViewInterface;
import com.joyplus.mediaplayer.VideoViewInterface.STATE;
import com.joyplus.tv.R;
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
	private RoundProcessDialog         mWaitingDialog;
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
    	mWaitingDialog   = new RoundProcessDialog(mActivity);
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
			PreMediaInfo     = CurrentMediaInfo.CreateMediaInfo();
			CurrentMediaInfo = ((MediaInfo) msg.obj).CreateMediaInfo();
			CheckMediaInfo(); 
			return true;
		}
		return false;
	}
	private void CheckMediaInfo(){
		if(CurrentMediaInfo.getState().toInt()>STATE.MEDIA_STATE_INITED.toInt()
		&& CurrentMediaInfo.getState().toInt()<STATE.MEDIA_STATE_FINISH.toInt()
		&& CurrentMediaInfo.getState().toInt() != STATE.MEDIA_STATE_PUSE.toInt()){
			if(CurrentMediaInfo.getCurrentTime() == PreMediaInfo.getCurrentTime() && CurrentMediaInfo.getCurrentTime()>1000)
				mWaitingDialog.setVisible(true);
			else
				mWaitingDialog.setVisible(false);
		}
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
    		VideoView.hideView();
    	}
	}
	@Override
	public int JoyplusgetLayout() {
		// TODO Auto-generated method stub
		return LAYOUT_VIDEOVIEW;
	}
	 
	public boolean hasMediaInfoChange(){
		long delay = Math.abs(CurrentMediaInfo.getCurrentTime()-PreMediaInfo.getCurrentTime());
		Log.e(TAG,"eeeeeeeeeeeeeeeee  "+delay +" eeeeeeeeeeee");
		return (delay<2000&&delay>=300);
	}
	
	private class RoundProcessDialog extends Dialog{
		private final static int MSG_SHOW = 1;
		private final static int MSG_HIDE = 2;
		private boolean   MSG_Visible = false;
		public RoundProcessDialog(Context context, int theme) {
			super(context, theme);
			// TODO Auto-generated constructor stub
			this.setContentView(R.layout.joyplusmediaplayer_loading_process);
		}
		protected RoundProcessDialog(Context context, boolean cancelable,
				OnCancelListener cancelListener) {
			super(context, cancelable, cancelListener);
			// TODO Auto-generated constructor stub
			this.setContentView(R.layout.joyplusmediaplayer_loading_process);
		}
		public RoundProcessDialog(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			this.setContentView(R.layout.joyplusmediaplayer_loading_process);
		}
		public void setVisible(boolean Visible){
			if(MSG_Visible == Visible)return;
			MSG_Visible = Visible;
			mHandler.removeCallbacksAndMessages(null);
			if(Visible)mHandler.sendEmptyMessageDelayed(MSG_SHOW, 1000);
			else       mHandler.sendEmptyMessage(MSG_HIDE);
		}
		private Handler mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch(msg.what){
				case MSG_SHOW:
					if(!RoundProcessDialog.this.isShowing())
						RoundProcessDialog.this.show();
					break;
				case MSG_HIDE:
					if(RoundProcessDialog.this.isShowing())
						RoundProcessDialog.this.dismiss();
					break;
				}
			}
		};
	}
}
