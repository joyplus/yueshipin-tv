package com.joyplus.sub;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class JoyplusSubListInatance extends JoyplusURIList{
	private   Context mContext;
	protected boolean Instanceing   = false;    
	private   boolean InstanceStop  = true;
	private   InstanceThread mInstanceThread;
    private final static int MSG_INSTANCE_SUBURI = 0;
    private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case MSG_INSTANCE_SUBURI:		    	
				SubURI sub = (SubURI) msg.obj;
				if(sub != null && Instanceing!=true){
					InstanceSub(sub);
				}
				break;
			}
		}    	
    };
    
    @Override
	public void clear() {
		// TODO Auto-generated method stub 	
		SetInstanceState(null,true);
		super.clear();
	}
	private synchronized void  InstanceSub(final SubURI uri){
    	if(Instanceing)return;
    	if(mInstanceThread != null){
    		mInstanceThread = null;
    	}
    	mInstanceThread = new InstanceThread(mContext,uri);
    	mInstanceThread.start();
    }
    private synchronized void CheckInstanceList(){
		if(!Instanceing && size()>0 && !InstanceStop  && mContext != null){
			Message m = new Message();
			m.what = MSG_INSTANCE_SUBURI;
			m.obj  = getNextInstance();
			mHandler.sendMessage(m);
		}
	}	
    public void SetInstanceState(Context context,boolean stop){
    	InstanceStop  = stop;
    	if(stop && mInstanceThread != null){
    	    mInstanceThread = null;
    	}else{
    		mContext  = context;
        	CheckInstanceList();
    	}
    }
    private class InstanceThread extends Thread{
        private Context mContext;
        private SubURI  mSubURI;
        public InstanceThread(Context context,SubURI uri){
        	mSubURI  = uri;
        	mContext = context;
        }
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			if(Instanceing || mContext==null || mSubURI==null)return;
			Instanceing = true;
	    	JoyplusSubInstance mSubInstance = new JoyplusSubInstance(mContext);
	    	if(mSubInstance.InstanceSub(mSubURI) && mSubInstance.IsSubAviable()){
	    		Log.d("Sub","SubListInstance InstanceOK");
	    		notifyModelInstance(mSubInstance);
	    	}	    	    	
	    	Instanceing = false;
	    	remove(mSubURI);
		}
    };
}
