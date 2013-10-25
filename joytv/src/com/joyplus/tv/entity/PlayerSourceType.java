package com.joyplus.tv.entity;

public enum PlayerSourceType {
	/**
	 *  p2p，wangpan，le_tv_fee，
	 * 	letv，sohu，fengxing，
	 * 	youku，sinahd，qiyi，
	 * 	m1905，	qq，	pptv，
	 * 	56，pps
	 */
	TYPE_UNKOWN		("unkown"),
	TYPE_P2P   		("p2p"),
	TYPE_WANGPAN 	("wangpan"),
	TYPE_LE_TV_FEE 	("le_tv_fee"),
	TYPE_LETV 		("letv"),
	TYPE_SOHU 		("sohu"),
	TYPE_FENGXING 	("fengxing"),
	TYPE_YOUKU 		("youku"),
	TYPE_SINAHD 	("sinahd"),
	TYPE_QIYI 		("qiyi"),
	TYPE_M1905 		("m1905"),
	TYPE_QQ 		("qq"),
	TYPE_PPTV 		("pptv"),
	TYPE_56 		("56"),
	TYPE_PPS		("pps");

	private String sourceName = "unkown";
	private PlayerSourceType(String _sourceName){
		this.sourceName = _sourceName;
	}
	
	//值越小，优先级越高
	//可自行设置
	private static final int PRIORITY_P2P 		= 0;
	private static final int PRIORITY_WANGPAN 	= 1;
	private static final int PRIORITY_LE_TV_FEE  	= 2;
	private static final int PRIORITY_LETV  		= 3;
	private static final int PRIORITY_SOHU 		= 4;
	private static final int PRIORITY_FENGXING 	= 5;
	private static final int PRIORITY_YOUKU  		= 6;
	private static final int PRIORITY_SINAHD 		= 7;
	private static final int PRIORITY_QIYI 		= 8;
	private static final int PRIORITY_M1905 		= 9;
	private static final int PRIORITY_QQ  		= 10;
	private static final int PRIORITY_PPTV 		= 11;
	private static final int PRIORITY_56  		= 12;
	private static final int PRIORITY_PPS 		= 13;
	private static final int PRIORITY_UNKOWN 		= 100;
	

	public int getPriority(){
		if("p2p".equalsIgnoreCase(sourceName))
			return PRIORITY_P2P;
		else if("wangpan".equalsIgnoreCase(sourceName))
			return PRIORITY_WANGPAN;
		else if("le_tv_fee".equalsIgnoreCase(sourceName))
			return PRIORITY_LE_TV_FEE;
		else if("letv".equalsIgnoreCase(sourceName))
			return PRIORITY_LETV;
		else if("sohu".equalsIgnoreCase(sourceName))
			return PRIORITY_SOHU;
		else if("fengxing".equalsIgnoreCase(sourceName))
			return PRIORITY_FENGXING;
		else if("youku".equalsIgnoreCase(sourceName))
			return PRIORITY_YOUKU;
		else if("sinahd".equalsIgnoreCase(sourceName))
			return PRIORITY_SINAHD;
		else if("qiyi".equalsIgnoreCase(sourceName))
			return PRIORITY_QIYI;
		else if("m1905".equalsIgnoreCase(sourceName))
			return PRIORITY_M1905;
		else if("qq".equalsIgnoreCase(sourceName))
			return PRIORITY_QQ;
		else if("pptv".equalsIgnoreCase(sourceName))
			return PRIORITY_PPTV;
		else if("56".equalsIgnoreCase(sourceName))
			return PRIORITY_56;
		else if("pps".equalsIgnoreCase(sourceName))
			return PRIORITY_PPS;
		else return PRIORITY_UNKOWN;
	}
	
	public String toStringValue(){
		return sourceName;
	}
	
}
