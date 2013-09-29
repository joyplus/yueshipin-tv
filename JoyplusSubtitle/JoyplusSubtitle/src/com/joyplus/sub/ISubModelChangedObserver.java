package com.joyplus.sub;


public interface ISubModelChangedObserver {
    
	void onSubModelChanged(SubModel model, boolean dataChanged);
	
	void onInstance(SubURI sub,JoyplusSubInstance subInstance);
}
