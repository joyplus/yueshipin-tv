package com.joyplus.Sub;


public interface ISubModelChangedObserver {
    
	void onSubModelChanged(SubModel model, boolean dataChanged);
	
	void onInstance(SubURI sub,byte[] subtitle);
}
