package com.joyplus.mediaplayer;


import com.joyplus.tv.R;
import com.joyplus.tv.utils.Log;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

/*define by Jas@20130723 for monitor player state*/
public class JoyplusPlayerMonitor{

		private boolean Debug = false;
	    private String  TAG   = "JoyplusPlayerMonitor";
	    public static Handler mHandler;
	    private VideoViewInterface  mPlayer;
	    private static  int DELAY = 500;
	    private boolean Flog = false;
	    public  static final int MSG_STATEUPDATE       = 1;
	    public  static final int MSG_NOPROCESSCOMMEND  = 2;
	    public JoyplusPlayerMonitor(Context context,VideoViewInterface player){
	    	  mPlayer  = player;
	    	  setUpdateTime(Integer.parseInt(context.getString(R.string.defaultUpdateTime)));
	    }

		public void setUpdateTime(int time){
			if(Debug)Log.d(TAG,"setUpdateTime("+time+")");
			if(time>=300 && time<=800){
				DELAY  = time;
			}
		}

	  
		private void notityState(){
			if(Debug)Log.d(TAG,"notityState()mHandler="+(mHandler == null)+" mPlayer="+(mPlayer == null));
			if(mHandler == null || mPlayer == null)return;
			mHandler.removeCallbacksAndMessages(null);
			Message m = new Message();
			m.what    = MSG_STATEUPDATE;
			//Message m = Message.obtain(mHandler, MSG_STATEUPDATE,"MSG_STATEUPDATE");
			m.obj     = mPlayer.getMediaInfo();			
			mHandler.sendMessage(m);
		}
	    public void stopMonitor(){
	    	if(Debug)Log.d(TAG,"stopMonitor()");
	    	Flog = false;
	    	mRunnable = null;
			mHandler.removeCallbacksAndMessages(null);
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
						System.gc();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace(); 
					}
				}
			}
	    };
}
