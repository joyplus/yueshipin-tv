package com.joyplus.tv.entity;

public class URLS_INDEX {

	public int souces;// 来源 例如P2P 本地排序使用
	public int defination;// 清晰度优先级 本地排序使用
	public String url;// 播放地址
	public String bakUrl;//重试临时保存地址
	public String source_from;// 来源 例如P2P 网络获取
	public String defination_from_server;// 清晰度 例如HD2
	public String webUrl;//原始网页播放地址

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "souces:" + souces + 
				" defination:" + defination + 
				" url" + url+ 
				" bakUrl" + bakUrl+ 
				" source_from:" + source_from + 
				"  defination_from_server:"+ defination_from_server;
	}

}