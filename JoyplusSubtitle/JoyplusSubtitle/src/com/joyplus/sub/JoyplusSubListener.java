package com.joyplus.sub;

public interface JoyplusSubListener {
    
	/*Interface of useable Sub change*/
	void onSubChange(boolean useable);
	
	/*Interface of Sub Instance state*/
	void onSubInstance(boolean Instanceing);
	
	/*Interface of Sub switch state*/
	void onSubSwitch(boolean Switching);
}
