package com.joyplus.sub;


import com.joyplus.common.Log;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class JoyplusTempSubUri extends JoyplusURIList {
    
    private Context mContext;
    private boolean Instanceing = false;    
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
    private class SubModelChanged implements ISubModelChangedObserver{
		@Override
		public void onSubModelChanged(SubModel model, boolean dataChanged) {
			// TODO Auto-generated method stub
			if(!Instanceing){
				CheckInstanceList();
			}
		}

		@Override
		public void onInstance(SubURI sub, JoyplusSubInstance subInstance) {
			// TODO Auto-generated method stub
			
		}

		 	
    }
    private synchronized void  InstanceSub(final SubURI uri){
    	if(Instanceing)return;
    	new Thread(new Runnable() {			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Log.i("Yzg", "JoyplusTempSubUri:InstanceSub Run--->");
				Instanceing = true;
		    	JoyplusSubInstance mSubInstance = new JoyplusSubInstance(mContext);
		    	if(mSubInstance.InstanceSub(uri) && mSubInstance.IsSubAviable()){
		    		notifyModelInstance(uri,mSubInstance);
		    	}
		    	remove(uri);    	
		    	Instanceing = false;
		    	CheckInstanceList();
			}
		}).start();
    }
	public JoyplusTempSubUri(Context context){
		Instanceing  = false;
		MAX = 2*JoyplusSubConfig.getInstance().getSubMax();
		registerModelChangedObserver(new SubModelChanged());
	}
    
	
	
	private synchronized void CheckInstanceList(){
		if(!Instanceing && size()>0){
			Message m = new Message();
			m.what = MSG_INSTANCE_SUBURI;
			m.obj  = get(0);
			mHandler.sendMessage(m);
		}
	}	
}
