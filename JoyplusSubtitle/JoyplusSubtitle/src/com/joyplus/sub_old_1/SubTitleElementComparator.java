package com.joyplus.sub_old_1;

import java.util.Comparator;


public class SubTitleElementComparator implements Comparator<Element> {
    
	private boolean   DELAY     = false;
    private final int DelayTime = 100;
    public SubTitleElementComparator(boolean delay){
    	DELAY = delay;
    }
	@Override
	public int compare(Element lhs, Element rhs) {
		// TODO Auto-generated method stub
		if(!DELAY ){
			long lhsStartTime = lhs.getStartTime().getTime();
			long rhsStartTime = rhs.getStartTime().getTime();			
			if(lhsStartTime - rhsStartTime > 0 ){
				return 1;
			}else if(lhsStartTime - rhsStartTime < 0 ){
				return -1;
			}			
			return 0;
		}else {
			if(SubFeature.SUBDELAY){				
				if(((lhs.getEndTime().getTime()+DelayTime)>= rhs.getStartTime().getTime())
					&&(rhs.getEndTime().getTime()-rhs.getStartTime().getTime()>2*DelayTime)){
					rhs.setStartTime(new JoyplusSubTime(rhs.getStartTime().getTime()+DelayTime));
				}
			}
		}
		return 0;
	}
}
