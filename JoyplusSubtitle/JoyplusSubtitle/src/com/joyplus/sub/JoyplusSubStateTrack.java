package com.joyplus.sub;

import java.util.ArrayList;
import java.util.List;

public class JoyplusSubStateTrack {
    private List<JoyplusSubListener> mListenerList=new ArrayList<JoyplusSubListener>();
	
	public void registerListener(JoyplusSubListener listener){
		if(listener != null){
			synchronized(mListenerList){
			    if(!mListenerList.contains(listener))mListenerList.add(listener);
			}
		}
	}
	public void unregisterListener(JoyplusSubListener listener){
		if(listener != null){
			synchronized(mListenerList){
				if(mListenerList.contains(listener))mListenerList.remove(listener);
			}
		}
	}
	public void unregisterAllListener(){
		synchronized(mListenerList){
			mListenerList = new ArrayList<JoyplusSubListener>();
		}
	}
	
	public void notifySubChange(final boolean useable){
		synchronized(mListenerList){
			new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					for(JoyplusSubListener listener:mListenerList){
						 listener.onSubChange(useable);
					}
				}				   
			}.run();
	   }
	}
	
	public void notifySubInstanceState(final boolean instanceing){
		synchronized(mListenerList){
			new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					for(JoyplusSubListener listener:mListenerList){
						 listener.onSubInstance(instanceing);
					}
				}				   
			}.run();
	   }
	}
	public void notifySubSwitchState(final boolean switching){
		synchronized(mListenerList){
			new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					for(JoyplusSubListener listener:mListenerList){
						 listener.onSubSwitch(switching);
					}
				}				   
			}.run();
	   }
	}
}
