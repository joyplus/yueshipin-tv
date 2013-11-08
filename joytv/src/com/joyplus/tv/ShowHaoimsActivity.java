package com.joyplus.tv;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.joyplus.manager.HaoimsDataSourceManager;
import com.joyplus.manager.RequestAQueryManager;
import com.joyplus.tv.Adapters.HaoimsAdapter;
import com.joyplus.tv.HistoryActivity.HistortyAdapter;
import com.joyplus.tv.entity.HaoimsSourceType;
import com.joyplus.tv.entity.ISourceData;
import com.joyplus.tv.entity.REQUEST_URL;
import com.joyplus.tv.ui.WaitingDialog;
import com.joyplus.tv.utils.UtilTools;
import com.umeng.analytics.MobclickAgent;

public class ShowHaoimsActivity extends Activity {
	
	private static final String TAG = "ShowHaoimsActivity";
	private static final int DIALOG_WAITING = 0;
	
	private App 	 app;
	private AQuery  aQuery;
	private LeftButtonsController buttonsController;
	
	private ListView   		listView;
	private HaoimsAdapter  mAdapter;   
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_haoims);
		AjaxCallback.setAgent(Constant.USER_AGENT_FIRFOX);
		this.initSource();
		this.initListener();
		this.buttonsController.getList4Network();
	}
	
	private void initSource(){
		this.app 	= (App) getApplication();
		this.aQuery  = new AQuery(this);
		HaoimsDataSourceManager.getInstance().initManager(this);
		this.buttonsController = new LeftButtonsController(this);
		this.buttonsController.setHandler(mHandler);
		this.mAdapter = new HaoimsAdapter(this);
		this.listView = (ListView) findViewById(R.id.lv_haoims_source);
		this.mAdapter.setList(HaoimsDataSourceManager.getInstance().getCurrentSourceType().getSourceDatalist());
		this.listView.setAdapter(this.mAdapter);
	}
	
	private void initListener(){
		this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				HaoimsSourceType sourceType = HaoimsDataSourceManager.getInstance().getCurrentSourceType();
				if(sourceType.getSourceDatalist().size() > position){
					ISourceData sourceDdata = sourceType.getSourceDatalist().get(position);
					String prod_type = sourceDdata.getProd_type();
					if(!TextUtils.isEmpty(prod_type)){
						Intent intent = new Intent();
						intent.setClass(ShowHaoimsActivity.this, ShowDetailHaoimsActivity.class);
						intent.putExtra("ID", sourceDdata.getProd_id());

						intent.putExtra("prod_url", sourceDdata.getProd_pic_url());
						intent.putExtra("prod_name",sourceDdata.getProd_name());
						intent.putExtra("stars", sourceDdata.getStar());
						intent.putExtra("directors", sourceDdata.getDirector());
						intent.putExtra("summary", sourceDdata.getProd_sumary());
						intent.putExtra("support_num",sourceDdata.getSupport_num());
						intent.putExtra("favority_num",sourceDdata.getFavority_num());
//						intent.putExtra("definition", sourceDdata.getD);
						intent.putExtra("score", sourceDdata.getScore());
						RequestAQueryManager.getInstance().setcurrentRequest_URL(REQUEST_URL.HAOIMS);
						startActivity(intent);
					}
					
				}
			}
		});
		
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			
			int tempfirstVisibleItem;
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:
					Log.i(TAG, "playGv--->SCROLL_STATE_IDLE" + " tempfirstVisibleItem--->" + tempfirstVisibleItem);
					getMoreData();
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					Log.i(TAG, "playGv--->SCROLL_STATE_TOUCH_SCROLL");
					break;
				case OnScrollListener.SCROLL_STATE_FLING:
					Log.i(TAG, "playGv--->SCROLL_STATE_FLING");
					break;
				default:
					break;
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				tempfirstVisibleItem = firstVisibleItem;
			}
		});
		listView.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				getMoreData();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}
	
	private void getMoreData(){
		HaoimsSourceType sourceType = HaoimsDataSourceManager.getInstance().getCurrentSourceType();
		if(sourceType.getSourceDatalist().size()-listView.getLastVisiblePosition()
				==HaoimsSourceType.DEFAULT_PAGE_SIZE/2){
			Log.d(TAG, "onItemSelected-->getMore");
			buttonsController.getList4Network();
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (this.aQuery != null)
			this.aQuery.dismiss();
		RequestAQueryManager.getInstance().setcurrentRequest_URL(
				RequestAQueryManager.DEAULT_REQUEST_URL);
		super.onDestroy();
	}
	
	protected void onResume() {
		super.onResume();

		MobclickAgent.onResume(this);

		if (app.getUserInfo() != null) {
			aQuery.id(R.id.iv_head_user_icon).image(
					app.getUserInfo().getUserAvatarUrl(), false, true, 0,
					R.drawable.avatar_defult);
			updateUserName();
		}
	}
	
	private void updateUserName(){
		if(VIPLoginActivity.isLogin(this))
			aQuery.id(R.id.tv_head_user_name).text(UtilTools.getVIP_NickName(this));
		else
			aQuery.id(R.id.tv_head_user_name).text(app.getUserInfo().getUserName());
	}
	
	protected void onPause() {
		super.onPause();

		MobclickAgent.onPause(this);
	}
	
	public App getApp(){
		return this.app;
	}
	
	public AQuery getAQuery(){
		return this.aQuery;
	}
	
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_WAITING:
			WaitingDialog dlg = new WaitingDialog(this);
			dlg.show();
			dlg.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			});
			dlg.setDialogWindowStyle();
			return dlg;
		default:
			return super.onCreateDialog(id);
		}
	}
	
	public static final int MESSAGE_LOADING_DATA 		= 100;
	public static final int MESSAGE_REFRESH_LISTVIEW 	= 101;
	public static final int MESSAGE_ADAPTER_SETLIST 	= 102;
	public static final int MESSAGE_NETWORK_ERROR     = 103;
	
	private Handler mHandler = new Handler(){

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_LOADING_DATA:
				loadingData();
				break;
			case MESSAGE_REFRESH_LISTVIEW:
				refershListView();
				break;
			case MESSAGE_ADAPTER_SETLIST:
				adapterSetList();
				break;
			case MESSAGE_NETWORK_ERROR:
				networkError();
				break;
			default:
				break;
			}
		}
	};
	
	private void loadingData(){
		showDialog(DIALOG_WAITING);
	}
	private void refershListView(){
		mAdapter.notifyDataSetChanged();
		removeDialog(DIALOG_WAITING);
	}
	private void adapterSetList(){
		Log.i(TAG, "list size--->" + 
				HaoimsDataSourceManager.getInstance().getCurrentSourceType().getSourceDatalist().size());
		mAdapter.setList(HaoimsDataSourceManager.getInstance().getCurrentSourceType().getSourceDatalist());
		mAdapter.notifyDataSetChanged();
		removeDialog(DIALOG_WAITING);
		listView.setSelection(0);
		listView.requestFocus();
	}
	private void networkError(){
		app.MyToast(ShowHaoimsActivity.this,
				getResources().getString(R.string.networknotwork));
	}
}
