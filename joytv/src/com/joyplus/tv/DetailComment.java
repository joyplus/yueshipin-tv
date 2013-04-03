package com.joyplus.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.tv.R;
import com.joyplus.tv.Adapters.DetailCommentListData;
import com.joyplus.tv.Service.Return.ReturnProgramReviews;
import com.parse.Parse;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

public class DetailComment extends Activity implements
		android.widget.AdapterView.OnItemClickListener{
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
	private boolean isDetailComment = false;
	private int CurrentIndex = 0;
	private ScrollView scrollViewItemDetail;
	private int CurrentDetailComment = 0;
	private int totalDetailCommentHeight = 0;

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
		
		ItemsListView.setOnKeyListener(new View.OnKeyListener() {
				
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					int action = event.getAction();
					if (action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_NUMPAD_5) {
						aq.id(R.id.scrollViewItemDetail).invisible();
						aq.id(R.id.listView1).visible ();
						aq.id(R.id.listView1).getView().requestFocus();
					}
					return false;
				}
			});
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
		
		prod_id = "6524";
		prod_name = "那年冬天,风在吹";
		prod_dou = "3.5";
		prod_url = "http://img3.douban.com/view/photo/photo/public/p1869602430.jpg";
		
		aq.id(R.id.textView_dou).text(prod_dou);
		aq.id(R.id.textView1).text(prod_name);
		aq.id(R.id.imageViewBarCode).image(prod_url, true,
				true, 0, R.drawable.movie_pic);

		scrollViewItemDetail =  (ScrollView) findViewById(R.id.scrollViewItemDetail);
		if (prod_id != null)
			CheckSaveData();
		if(Float.parseFloat(prod_dou) >0)
			InitDou();
		// MobclickAgent.setDebugMode(true);
	}
	public void InitDou() {
		String m_j = null;
		int i = 0;
		Float f_dou = Float.parseFloat(prod_dou);
		
		for (i = 0; i < f_dou/1; i++) {
			m_j = Integer.toString(i + 2);
			ImageView m_ImageView = (ImageView) this.findViewById(getResources()
					.getIdentifier("imageView" + m_j, "id", getPackageName()));
			m_ImageView.setImageResource(R.drawable.star_on);
		}
		if(f_dou%i > 0){
			m_j = Integer.toString(i+1);
			ImageView m_ImageView = (ImageView) this.findViewById(getResources()
					.getIdentifier("imageView" + m_j, "id", getPackageName()));
			m_ImageView.setImageResource(R.drawable.star_half);
		}
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
		
		aq.id(R.id.scrollViewItemDetail).invisible();
		aq.id(R.id.listView1).visible();
		aq.id(R.id.listView1).getView().requestFocus();
		
		
		if (isLastisNext > 1) {
			for (int i = 0; i < m_ReturnProgramReviews.reviews.length; i++) {
				DetailCommentListData m_DetailCommentListData = new DetailCommentListData();
				m_DetailCommentListData.Prod_ID = m_ReturnProgramReviews.reviews[i].review_id;
				m_DetailCommentListData.Prod_title = m_ReturnProgramReviews.reviews[i].title;
				m_DetailCommentListData.Prod_comments = m_ReturnProgramReviews.reviews[i].comments.replace("\r", "\r\n");
				dataStruct.add(m_DetailCommentListData);
				DetailCommentAdapter.notifyDataSetChanged();
			}
			return;
			
		} else
		{
			DetailCommentAdapter.notifyDataSetChanged();
		}
		
		for (int i = 0; i < m_ReturnProgramReviews.reviews.length; i++) {
			DetailCommentListData m_DetailCommentListData = new DetailCommentListData();
			m_DetailCommentListData.Prod_ID = m_ReturnProgramReviews.reviews[i].review_id;
			m_DetailCommentListData.Prod_title = m_ReturnProgramReviews.reviews[i].title;
			m_DetailCommentListData.Prod_comments = m_ReturnProgramReviews.reviews[i].comments.replace("\r", "\r\n");
			dataStruct.add(m_DetailCommentListData);
		}
		DetailCommentAdapter.notifyDataSetChanged();
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
//			aq.id(R.id.ProgressText).invisible();
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
			if (dataStruct.size() > 0) {
				aq.id(R.id.textView3).text(
						"1/" + Integer.toString(dataStruct.size()));
				aq.id(R.id.textView3).visible();
			}
//			aq.id(R.id.ProgressText).invisible();
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

	// listview的点击事件接口函数
	@Override
	public void onItemClick(AdapterView adapterview, View view, int i, long l) {
		CurrentIndex = i;
		DetailCommentListData m_DetailCommentListData = (DetailCommentListData) ItemsListView
				.getItemAtPosition(i);
		if (m_ReturnProgramReviews != null) {
			isDetailComment = true;
			aq.id(R.id.listViewItemTitle).text(m_DetailCommentListData.Prod_title);
			aq.id(R.id.listViewItemDetail).text(m_DetailCommentListData.Prod_comments);
			aq.id(R.id.listView1).invisible();
			aq.id(R.id.scrollViewItemDetail).visible();
			aq.id(R.id.scrollViewItemDetail).getView().requestFocus();
			
			scrollViewItemDetail.fullScroll(ScrollView.FOCUS_UP);
			
			CurrentDetailComment = 1;
			
			totalDetailCommentHeight = aq.id(R.id.listViewItemDetail).getTextView().getHeight()/scrollViewItemDetail.getMeasuredHeight();
//			aq.id(R.id.listViewItemDetail).getTextView().get
			aq.id(R.id.textView3).text(
					 "1/" + Integer.toString(totalDetailCommentHeight));

		} else {
			app.MyToast(this, "ReturnProgramReviews is empty.");
		}

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (isDetailComment) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getRepeatCount() == 0
						&& scrollViewItemDetail.arrowScroll(View.FOCUS_DOWN)) {
					scrollViewItemDetail.pageScroll(View.FOCUS_DOWN);
					CurrentDetailComment++;
					aq.id(R.id.textView3).text(
							Integer.toString(CurrentDetailComment) + "/" + Integer.toString(totalDetailCommentHeight));
					return true;
				}
			}else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getRepeatCount() == 0
						&& scrollViewItemDetail.arrowScroll(View.FOCUS_UP)){
					
					scrollViewItemDetail.pageScroll(View.FOCUS_UP);
					CurrentDetailComment--;
					aq.id(R.id.textView3).text(
							Integer.toString(CurrentDetailComment) + "/" + Integer.toString(totalDetailCommentHeight));
					
					return true;
				}
			}
		}

		return super.dispatchKeyEvent(event);
	}

	private void CheckSaveData() {
		String SaveData = null;
		ObjectMapper mapper = new ObjectMapper();
		SaveData = app.GetServiceData("DetailComment" +prod_id +Integer.toString(isLastisNext));
		
		dataStruct = new ArrayList();
		DetailCommentAdapter = new DetailCommentListAdapter();
		ItemsListView.setAdapter(DetailCommentAdapter);
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
		String url = Constant.BASE_URL + "program/reviews" +"?prod_id=" + prod_id +"&page_num="
				+ Integer.toString(index) + "&page_size=6";

		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, "InitListData");

		cb.SetHeader(app.getHeaders());

//		aq.id(R.id.ProgressText).visible();
		aq.ajax(cb);

	}
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
           if (keyCode == KeyEvent.KEYCODE_BACK && isDetailComment){                    
        	   	aq.id(R.id.scrollViewItemDetail).invisible();
   				aq.id(R.id.listView1).visible ();
   				aq.id(R.id.listView1).getView().requestFocus();
   				DetailCommentAdapter.notifyDataSetChanged();
   				aq.id(R.id.textView3).text(
   						Integer.toString(CurrentIndex) + "/" + Integer.toString(dataStruct.size()));
   				isDetailComment = false;
                 return true;
           }
           return super.onKeyDown(keyCode, event);
    }
	static class ViewHolder {
		ImageView imageView1;
		TextView textView01;
		TextView textView02;
	}
	public class DetailCommentListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return dataStruct.size();
		}

		@Override
		public DetailCommentListData getItem(int position) {
			return (DetailCommentListData) dataStruct.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			
			DetailCommentListData m_DetailCommentListData = (DetailCommentListData) getItem(position);
			ViewHolder holder;
			
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.detail_comment_list, viewGroup, false);

				holder = new ViewHolder();
				
				holder.imageView1 = (ImageView) convertView
						.findViewById(R.id.imageView1);
				
				holder.textView01 = (TextView) convertView
						.findViewById(R.id.TextView01);
				
				holder.textView02 = (TextView) convertView
						.findViewById(R.id.TextView02);

				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (CurrentIndex == position) {
				holder.imageView1.setVisibility(View.VISIBLE);
				convertView.setBackgroundResource(R.drawable.bg_teat_repeat);
			} else {
				holder.imageView1.setVisibility(View.INVISIBLE);
				convertView.setBackgroundResource(0);
			}

			holder.textView01.setText(m_DetailCommentListData.Prod_title);
			holder.textView02.setText(m_DetailCommentListData.Prod_comments);

			return convertView;
		}
	}
	
}
