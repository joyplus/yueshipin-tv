package com.joyplus.tv.entity;

public enum PlayerSourceType {
	/**
	 *  p2p，wangpan，le_tv_fee，
	 * 	letv，sohu，fengxing，
	 * 	youku，sinahd，qiyi，
	 * 	m1905，	qq，	pptv，
	 * 	56，pps
	 */
	TYPE_UNKOWN		("unkown",100),//max 100
	TYPE_P2P   		("p2p",0),//0
	TYPE_WANGPAN 	("wangpan",4),//4
	TYPE_LE_TV_FEE 	("le_tv_fee",5),//5
	TYPE_LETV 		("letv",7),//7
	TYPE_SOHU 		("sohu",8),//8
	TYPE_FENGXING 	("fengxing",9),//9
	TYPE_YOUKU 		("youku",10),//10
	TYPE_SINAHD 	("sinahd",11),//11
	TYPE_QIYI 		("qiyi",12),//12
	TYPE_M1905 		("m1905",13),//13
	TYPE_QQ 		("qq",14),//14
	TYPE_PPTV 		("pptv",15),//15
	TYPE_56 		("56",16),//16
	TYPE_PPS		("pps",17),//17
	TYPE_KU6		("ku6",18),//18
	
	//20131105 by @yzg
	TYPE_BAIDU_WANGPAN		("baidu_wangpan",1),//1
	TYPE_LETV_V2			("le_v2",2),//2
	TYPE_LETV_V2_FEE		("le_v2_fee",3);//3

	private String sourceName = "unkown";
	private int priority     = 100;//默认值100，值越小，优先级越高，可自行设置
	private PlayerSourceType(String _sourceName,int _priority){
		this.sourceName = _sourceName;
		this.priority 	= _priority;
	}

	public int getPriority(){
		return this.priority;
	}
	
	public String toSourceName(){
		return sourceName;
	}
	
}
