package com.joyplus.tv.Adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.fasterxml.jackson.databind.ser.std.StdArraySerializers.IntArraySerializer;
import com.joyplus.tv.R;
import com.joyplus.tv.entity.HotItemInfo;

public class MainHotItemAdapter extends BaseAdapter {

	private List<HotItemInfo> hot_list;
	private AQuery aq;
	private Context c;
	private int displayWith;
	private android.widget.Gallery.LayoutParams layoutParam;
	
	public MainHotItemAdapter(Context c,List<HotItemInfo> list){
		super();
		this.hot_list = list;
		this.c = c;
		displayWith = ((Activity)c).getWindowManager().getDefaultDisplay().getWidth();
		layoutParam = new android.widget.Gallery.LayoutParams((displayWith-40)/6,2*displayWith/9);
		aq = new AQuery(c);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return hot_list.size();
//		if(hot_list.size()!=0){
//			return 2;
//		}else{
//			return hot_list.size();
//		}
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if(convertView == null){
			convertView = LayoutInflater.from(c).inflate(R.layout.item_layout_gallery, null);
			holder = new ViewHolder();
			holder.firstTitle = (TextView) convertView.findViewById(R.id.first_title);
			holder.secondTitle = (TextView) convertView.findViewById(R.id.second_title);
			holder.content = (TextView) convertView.findViewById(R.id.content);
			holder.score = (TextView) convertView.findViewById(R.id.score);
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			holder.definition = (ImageView) convertView.findViewById(R.id.definition);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
//		aq = new AQuery(convertView);
		holder.image.setTag(hot_list.get(position).prod_pic_url);
//		holder.image.setImageResource(R.drawable.test1);
		aq.id(holder.image).image(hot_list.get(position).prod_pic_url,true,true);
		
		holder.secondTitle.setText(hot_list.get(position).prod_name);
		holder.score.setText(hot_list.get(position).score);
		switch (Integer.valueOf(hot_list.get(position).definition)) {
		case 5:
			holder.definition.setImageResource(R.drawable.icon_bd);
			break;
		case 4:
			holder.definition.setImageResource(R.drawable.icon_hd);
			break;
		case 3:
			holder.definition.setImageResource(R.drawable.icon_ts);
			break;
		default:
			holder.definition.setVisibility(View.GONE);
			break;
		}
		convertView.setPadding(15, 10, 15, 10);
		convertView.setLayoutParams(layoutParam);
		
//		ImageView img = (ImageView) view.findViewById(R.id.image);
//		
//		img.setImageResource(resouces[position]);
//		Log.d(TAG, hot_list.get(position).prod_pic_url);
//		aq = new AQuery(view);
//		aq.id(R.id.image).image(hot_list.get(position).prod_pic_url);
		return convertView;
	}
	
	class ViewHolder{
		TextView firstTitle;
		TextView secondTitle;
		TextView content;
		TextView score;
		ImageView image;
		ImageView definition;
	}
}


