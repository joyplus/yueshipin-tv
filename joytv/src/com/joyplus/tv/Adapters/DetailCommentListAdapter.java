package com.joyplus.tv.Adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.androidquery.AQuery;
import com.joyplus.tv.R;

/*
 * 分类导航详情的数据适配器
 * */
public class DetailCommentListAdapter extends ArrayAdapter {

	// listview的数据
	private Map viewMap;
	AQuery aq;

	// 构造函数
	public DetailCommentListAdapter(Activity activity, List list) {
		super(activity, 0, list);

		viewMap = new HashMap();
	}

	// 获取显示当前的view
	public View getView(int i, View view, ViewGroup viewgroup) {
		Integer integer = Integer.valueOf(i);
		View view1 = (View) viewMap.get(integer);

		if (view1 == null) {
			// 加载布局文件
			view1 = ((Activity) getContext()).getLayoutInflater().inflate(
					R.layout.detail_comment_list, null);
			aq = new AQuery(view1);

			// 获取当前数据项的数据
			DetailCommentListData m_DetailCommentListData = (DetailCommentListData) getItem(i);
			aq.id(R.id.TextView01).text(m_DetailCommentListData.Prod_title);
			aq.id(R.id.TextView02).text(m_DetailCommentListData.Prod_comments);

			if(aq != null)
				aq.dismiss();
			Integer integer1 = Integer.valueOf(i);
			Object obj = viewMap.put(integer1, view1);
			
		}
		return view1;
	}
}
