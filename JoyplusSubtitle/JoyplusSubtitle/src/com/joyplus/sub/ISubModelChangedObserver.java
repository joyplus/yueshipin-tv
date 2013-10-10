package com.joyplus.sub;


public interface ISubModelChangedObserver {
    
	void onSubModelChanged(SubModel model, boolean dataChanged);
	
	void onInstance(JoyplusSubInstance subInstance);
	
}
