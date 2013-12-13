package com.joyplus.sub;


public class JoyplusSubContentRestrictionFactory {
	   private static JoyplusSubContentRestriction sContentRestriction;

	    private JoyplusSubContentRestrictionFactory() {
	    }

	    public synchronized static JoyplusSubContentRestriction getContentRestriction() {
	        if (null == sContentRestriction) {
	            sContentRestriction = new JoyplusSubCarrierContentRestriction();
	        }
	        return sContentRestriction;
	    }
}
