package com.joyplus.Sub;

import java.util.ArrayList;


public class SubModel {
		
	protected ArrayList<ISubModelChangedObserver> mSubModelChangedObservers =
            new ArrayList<ISubModelChangedObserver>();

    public void registerModelChangedObserver(ISubModelChangedObserver observer) {
        if (!mSubModelChangedObservers.contains(observer)) {
        	mSubModelChangedObservers.add(observer);
        }
    }

    public void unregisterModelChangedObserver(ISubModelChangedObserver observer) {
    	mSubModelChangedObservers.remove(observer);
    }

    public void unregisterAllModelChangedObservers() {
    	mSubModelChangedObservers.clear();
    }

    protected void notifyModelChanged(boolean dataChanged) {
        for (ISubModelChangedObserver observer : mSubModelChangedObservers) {
        	observer.onSubModelChanged(this, dataChanged);
        }
    }
    protected void notifyModelInstance(SubURI sub , byte[] subtitle){
    	for (ISubModelChangedObserver observer : mSubModelChangedObservers) {
        	observer.onInstance(sub, subtitle);
        }
    }
}
