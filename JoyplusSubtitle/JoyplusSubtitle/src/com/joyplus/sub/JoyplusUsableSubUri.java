package com.joyplus.sub;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class JoyplusUsableSubUri extends JoyplusURIList implements ISubModelChangedObserver{
	private Context mContext;
	private JoyplusSub mSub = null;
	private JoyplusSubStateTrack mStateTrack;
	private boolean Switching   = false;
	private final static int MSG_SWITCH_SUBURI   = 0;
	
	public JoyplusUsableSubUri(Context context){
		mContext    = context;
		mStateTrack = new JoyplusSubStateTrack();
	}
	public void registerListener(JoyplusSubListener listener){
		mStateTrack.registerListener(listener);
	}
	private boolean setSub(JoyplusSub sub){
		if(sub.getElements().size()<2)return false;
		mSub = sub;
		mStateTrack.notifySubChange((sub!=null?true:false));	
		return true;
	}
	public JoyplusSub getSub(){
		return mSub;
	}
	public boolean CheckSubAviable(){
    	if(mSub != null && mSub.getElements().size()>2)return true;
    	return false;
    }
	private void setSwitchState(boolean switching){
	    Switching   = switching;
	    mStateTrack.notifySubSwitchState(switching);
	}
    public void SwitchSub(int index){
    	if(get(index)==null || index>=size() || Switching ||!get(index).Instanced) return;
    	SwitchSub(get(index));
    }
    private synchronized void SwitchSub(SubURI suburi){
		if(Switching || suburi==null)return;
		if(mSub!=null && mSub.getUri().getUrl().equals(suburi.getUrl()))return;
		Message m = new Message();
		m.what = MSG_SWITCH_SUBURI;
		m.obj  = suburi;
		mHandler.sendMessage(m);
	}
    private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case MSG_SWITCH_SUBURI:
				final SubURI subswitch = (SubURI) msg.obj;
				if(subswitch != null && !Switching && subswitch.Instanced){
					new Thread(new Runnable() {						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							setSwitchState(true);
							JoyplusSub subtemp1 =InstanceSub(subswitch);
							if(subtemp1 != null){
								setSub(subtemp1);//switch ok 
							}else if(mSub == null){								
								for(SubURI sub:JoyplusUsableSubUri.this){
									if(!sub.Instanced)continue;
									JoyplusSub subtemp2 =InstanceSub(subswitch);
									if(subtemp2 != null){
										setSub(subtemp2);//switch ok 
										break;
									}
								}
							}
							setSwitchState(false);
						}
					}).start();
				}
			    break;
			}
		}    	
    };
    private synchronized JoyplusSub InstanceSub(SubURI uri){
    	JoyplusSub  sub = null;
    	JoyplusSubInstance mSubInstance = new JoyplusSubInstance(mContext);
    	if(mSubInstance.InstanceSub(uri) && mSubInstance.IsSubAviable()){
	    	sub = mSubInstance.getJoyplusSub();
    	} 	
    	if(sub!=null && sub.elements.size()>2){
    		uri.Instanced = true;
    		return sub;
    	}
    	remove(uri);
    	return null;
    }
	@Override
	public void onSubModelChanged(SubModel model, boolean dataChanged) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInstance(JoyplusSubInstance subInstance) {
		// TODO Auto-generated method stub
		Log.d("Sub","+++++Usable onInstance()");
		if(!subInstance.IsSubAviable())return;
		if(!contains(subInstance.getSubURI())){
			Log.d("Sub","------Usable onInstance() size="+size());
			if(mSub == null)setSub(subInstance.getJoyplusSub());
			add(subInstance.getSubURI());
		}
		Log.d("Sub","+++++Usable onInstance() size="+size());
	}
    
}
