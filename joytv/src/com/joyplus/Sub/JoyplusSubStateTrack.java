package com.joyplus.Sub;

import java.util.ArrayList;
import java.util.List;

import com.joyplus.mediaplayer.JoyplusMediaPlayerListener;

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
	
}
