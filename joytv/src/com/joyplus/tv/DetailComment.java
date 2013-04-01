package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.tv.Adapters.DetailCommentListAdapter;
import com.joyplus.tv.Adapters.DetailCommentListData;
import com.joyplus.tv.Service.Return.ReturnProgramReviews;
import com.parse.Parse;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

public class DetailComment extends Activity implements
		android.widget.AdapterView.OnItemClickListener {
	private String TAG = "DetailComment";
	private AQuery aq;
	private App app;
	private ReturnProgramReviews m_ReturnProgramReviews = null;

	private String prod_id = null;
	private String prod_name = null;
	private String prod_dou = null;
	private String prod_url = null;
	
	private ArrayList dataStruct;
	private ListView ItemsListView;
	private DetailCommentListAdapter DetailCommentAdapter;
	private int isLastisNext = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_comment);
		app = (App) getApplication();
		aq = new AQuery(this);
		dataStruct = new ArrayList();
		
		// 获取listview对象
		ItemsListView = (ListView) findViewById(R.id.listView1);
		// 设置listview的点击事件监听器
		ItemsListView.setOnItemClickListener(this);
		ItemsListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				// 当不滚动时
				case OnScrollListener.SCROLL_STATE_IDLE:
					// 判断滚动到底部
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
						isLastisNext++;
						GetServiceData(isLastisNext);
					}
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});
//		Intent intent = getIntent();
//		prod_id = intent.getStringExtra("prod_id");
//		prod_name = intent.getStringExtra("prod_name");
		
		prod_id = "1004572";
		prod_name = "上位";
		prod_dou = "3";
		prod_url = "http://img.funshion.com/pictures/159/820/7/1598207.jpg";
		
		if (prod_id != null)
			CheckSaveData();
		// MobclickAgent.setDebugMode(true);
	}

	public void OnClickTab1TopLeft(View v) {

	}

	public void OnClickDownloadTopRight(View v) {

	}

	public void OnClickTab1TopRight(View v) {
	}

	@Override
	protected void onDestroy() {
		if (aq != null)
			aq.dismiss();
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	public void GetReviews() {
		String m_j = null;

		if (m_ReturnProgramReviews.reviews == null)
			return;
		if (isLastisNext > 1) {
			for (int i = 0; i < m_ReturnProgramReviews.reviews.length; i++) {
				DetailCommentListData m_DetailCommentListData = new DetailCommentListData();
				m_DetailCommentListData.Prod_ID = m_ReturnProgramReviews.reviews[i].review_id;
				m_DetailCommentListData.Prod_title = m_ReturnProgramReviews.reviews[i].title;
				m_DetailCommentListData.Prod_comments = m_ReturnProgramReviews.reviews[i].comments;
				dataStruct.add(m_DetailCommentListData);
				DetailCommentAdapter.notifyDataSetChanged();
			}
			return;
			
		} else
		{
			NotifyDataAnalysisFinished();
		}
		
		for (int i = 0; i < m_ReturnProgramReviews.reviews.length; i++) {
			DetailCommentListData m_DetailCommentListData = new DetailCommentListData();
			m_DetailCommentListData.Prod_ID = m_ReturnProgramReviews.reviews[i].review_id;
			m_DetailCommentListData.Prod_title = m_ReturnProgramReviews.reviews[i].title;
			m_DetailCommentListData.Prod_comments = m_ReturnProgramReviews.reviews[i].comments;
			dataStruct.add(m_DetailCommentListData);
		}

	}

	public void OnClickImageView(View v) {
		/*
		 * Intent intent = new Intent(this, BuChongGeRenZhiLiao.class);
		 * intent.putExtra("prod_id", m_prod_id); intent.putExtra("prod_type",
		 * m_prod_type); try { startActivity(intent); } catch
		 * (ActivityNotFoundException ex) { Log.e(TAG,
		 * "OnClickImageView failed", ex); }
		 */
	}

	// 初始化list数据函数
	public void InitListData(String url, JSONObject json, AjaxStatus status) {
		
		if (status.getCode() == AjaxStatus.NETWORK_ERROR)  {
//			aq.id(R.id.ProgressText).gone();
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			return;
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			if (isLastisNext > 1)
				m_ReturnProgramReviews = null;
			m_ReturnProgramReviews = mapper.readValue(json.toString(), ReturnProgramReviews.class);
			if (m_ReturnProgramReviews.reviews.length > 0)
				app.SaveServiceData("tops" + prod_id + Integer.toString(isLastisNext),
						json.toString());
			// 创建数据源对象
			GetReviews();
//			aq.id(R.id.ProgressText).gone();
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// 数据更新
	public void NotifyDataAnalysisFinished() {
		if (dataStruct != null && ItemsListView != null) {
			DetailCommentListAdapter listviewdetailadapter = getAdapter();
			ItemsListView.setAdapter(listviewdetailadapter);
		} else {
			app.MyToast(this, "ItemsListView empty.");
		}
	}

	private DetailCommentListAdapter getAdapter() {
		if (DetailCommentAdapter == null) {
			ArrayList arraylist = dataStruct;
			DetailCommentListAdapter listviewdetailadapter = new DetailCommentListAdapter(this,
					arraylist);
			DetailCommentAdapter = listviewdetailadapter;
		} else {
			ArrayList arraylist1 = dataStruct;
			DetailCommentListAdapter listviewdetailadapter1 = new DetailCommentListAdapter(this,
					arraylist1);
			DetailCommentAdapter = listviewdetailadapter1;
		}
		return DetailCommentAdapter;
	}

	// listview的点击事件接口函数
	@Override
	public void onItemClick(AdapterView adapterview, View view, int i, long l) {
		DetailCommentListData m_DetailCommentListData = (DetailCommentListData) ItemsListView
				.getItemAtPosition(i);
		if (m_ReturnProgramReviews != null) {
			// app.MyToast(this, m_DetailCommentListData.Pic_name, Toast.LENGTH_LONG)
			// .show();
//			Intent intent = new Intent(this, Detail_BangDan.class);
//			intent.putExtra("BangDan_id", m_DetailCommentListData.Pic_ID);
//			intent.putExtra("BangDan_name", m_DetailCommentListData.Pic_name);
//			try {
//				startActivity(intent);
//			} catch (ActivityNotFoundException ex) {
//				Log.e(TAG, "Call Detail_BangDan failed", ex);
//			}
		} else {
			app.MyToast(this, "ReturnProgramReviews is empty.");
		}

	}

	private void CheckSaveData() {
		String SaveData = null;
		ObjectMapper mapper = new ObjectMapper();
		SaveData = app.GetServiceData("DetailComment" +prod_id +Integer.toString(isLastisNext));
		if (SaveData == null) {
			GetServiceData(isLastisNext);
		} else {
			try {
				m_ReturnProgramReviews = mapper.readValue(SaveData, ReturnProgramReviews.class);
				// 创建数据源对象
				GetReviews();
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						// execute the task
						dataStruct = null;
						dataStruct = new ArrayList();
						GetServiceData(isLastisNext);
					}
				}, 100000);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void GetServiceData(int index) {
		String url = Constant.BASE_URL + "program/reviews" +"?prod_id=" + prod_id +"page_num="
				+ Integer.toString(index) + "&page_size=10";

		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, "InitListData");

		cb.SetHeader(app.getHeaders());

//		aq.id(R.id.ProgressText).visible();
		aq.ajax(cb);

	}
}
