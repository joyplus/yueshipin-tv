package com.joyplus.tv;

import org.json.JSONObject;

import com.androidquery.callback.AjaxStatus;

public abstract class AbstractShowAddShouCangActivity extends AbstractShowActivity{
	
	protected abstract void initShoucangView(int size);

	protected abstract void getShoucangData(String url);// 获取收藏

	public abstract void initShoucangServiceData(String url, JSONObject json,
			AjaxStatus status);// 推荐的10部影片服务
}
