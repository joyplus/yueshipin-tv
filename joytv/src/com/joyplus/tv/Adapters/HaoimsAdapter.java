package com.joyplus.tv.Adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.joyplus.tv.R;
import com.joyplus.tv.entity.ISourceData;

public class HaoimsAdapter extends BaseAdapter {
	
	private ArrayList<ISourceData> list;
	private Activity context;
	public HaoimsAdapter(Activity _context){
		this.context = _context;
	}

	public void setList(ArrayList<ISourceData> _list){
		this.list = _list;
	}
	public ArrayList<ISourceData> getList(){
		return this.list;
	}

	public int getCount() {
		return list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView ==null){
			convertView = context.getLayoutInflater().inflate(R.layout.item_history_list,null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.img = (ImageView) convertView.findViewById(R.id.image);
			holder.directors = (TextView) convertView.findViewById(R.id.directors);
			holder.stars = (TextView) convertView.findViewById(R.id.stars);
			holder.directors_notice = (TextView) convertView.findViewById(R.id.directors_notice);
			holder.stars_notice = (TextView) convertView.findViewById(R.id.stars_notice);
			holder.content = (TextView) convertView.findViewById(R.id.content);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		convertView.setBackgroundResource(R.drawable.historty_listitem_drawable_selector);
		holder.title.setText(list.get(position).getProd_name());
		return convertView;
	}

	class ViewHolder{
		TextView title;
		TextView stars;
		TextView stars_notice;
		TextView directors;
		TextView directors_notice;
		TextView content;
		ImageView img;
	}
}
