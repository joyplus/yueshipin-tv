package com.joyplus;

import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import com.joyplus.mediaplayer.JoyplusMediaPlayerManager;
import com.joyplus.mediaplayer.JoyplusVideoView;
import com.joyplus.mediaplayer.MediaInfo;
import com.joyplus.mediaplayer.VideoViewInterface;
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
	public  MediaInfo                  CurrentMediaInfo;
	public  MediaInfo                  PreMediaInfo;
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
			PreMediaInfo = CurrentMediaInfo;
			CurrentMediaInfo = ((MediaInfo) msg.obj).CreateMediaInfo();
			return true;
		}
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
    		VideoView.hideView();
    	}
	}
	@Override
	public int JoyplusgetLayout() {
		// TODO Auto-generated method stub
		return LAYOUT_VIDEOVIEW;
	}
}
