package com.joyplus.sub_old_1;

public class JoyplusSubContentRestrictionFactory {
	  private static JoyplusSubContentRestriction sContentRestriction;

	    private JoyplusSubContentRestrictionFactory() {
	    }

	    public static JoyplusSubContentRestriction getContentRestriction() {
	        if (null == sContentRestriction) {
	            sContentRestriction = new JoyplusSubCarrierContentRestriction();
	        }
	        return sContentRestriction;
	    }
}
