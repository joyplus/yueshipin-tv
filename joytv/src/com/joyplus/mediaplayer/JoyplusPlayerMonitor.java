package com.joyplus.mediaplayer;


import com.joyplus.tv.R;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/*define by Jas@20130723 for monitor player state*/
public class JoyplusPlayerMonitor{

		private boolean Debug = false;
	    private String  TAG   = "JoyplusPlayerMonitor";
	    private static Handler mHandler;
	    private VideoViewInterface  mPlayer;
	    private static  int DELAY = 800;
	    private boolean Flog = false;
	    public  static final int MSG_STATEUPDATE       = 1;
	    public  static final int MSG_NOPROCESSCOMMEND  = 2;
	    public JoyplusPlayerMonitor(Context context,VideoViewInterface player){
	    	  mPlayer  = player;
	    	  setUpdateTime(Integer.parseInt(context.getString(R.string.defaultUpdateTime)));
	    }

		public void setUpdateTime(int time){
			if(Debug)Log.d(TAG,"setUpdateTime("+time+")");
			if(time>=500 && time<=1000){
				DELAY  = time;
			}
		}

	  
		private void notityState(){
			if(Debug)Log.d(TAG,"notityState()mHandler="+(mHandler == null)+" mPlayer="+(mPlayer == null));
			if(mHandler == null || mPlayer == null)return;
			Message m = Message.obtain(mHandler, MSG_STATEUPDATE,"MSG_STATEUPDATE");
			m.obj     = mPlayer.getMediaInfo();
			mHandler.removeCallbacksAndMessages("MSG_STATEUPDATE");
			mHandler.sendMessage(m);
		}
	    public void stopMonitor(){
	    	if(Debug)Log.d(TAG,"stopMonitor()");
	    	Flog = false;
	    	mRunnable = null;
			mHandler.removeCallbacksAndMessages("MSG_STATEUPDATE");
			mHandler = null;
	    }
	    public void startMonitor(Handler handler){
	    	if(Debug)Log.d(TAG,"startMonitor()");
	    	mHandler = handler;
	    	if(mRunnable==null){	
	    	  Flog = true;
	    	  mRunnable = new MediaPlayerMonitor();
	    	  mRunnable.start();
	    	}
	    }
	    private MediaPlayerMonitor mRunnable;
	    private class MediaPlayerMonitor extends Thread{
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(Flog){
					try {
						Thread.sleep(DELAY);
						notityState();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace(); 
					}
				}
			}
	    };
}
