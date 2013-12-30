package com.joyplus.tv.Adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.joyplus.tv.Constant;
import com.joyplus.tv.R;
import com.joyplus.tv.entity.MovieItemData;
import com.joyplus.tv.utils.JieMianConstant;

public class SohuAdapter extends BaseAdapter implements JieMianConstant{

	private List<MovieItemData> dates;
	private Context context;
	private AQuery aq;
	
	public SohuAdapter(Context c, List<MovieItemData> lists){
		context = c;
		dates = lists;
		aq = new AQuery(c);
	}
	
	public List<MovieItemData> getDateList(){
		return dates;
	}
	
	public void setDateList(List<MovieItemData> dates){
		this.dates = dates;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return dates.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		int width = parent.getWidth() / 5;
		int height = (int) (width / 1.0f / STANDARD_PIC_WIDTH * STANDARD_PIC_HEIGHT);
		MovieItemData itemInfo = dates.get(position);
		if(convertView==null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.show_item_layout_dianying, null);
			holder.nameTv = (TextView) convertView.findViewById(R.id.tv_item_layout_name);
			holder.scoreTv = (TextView) convertView.findViewById(R.id.tv_item_layout_score);
			holder.otherInfo = (TextView) convertView.findViewById(R.id.tv_item_layout_other_info);
			holder.haibaoIv = (ImageView) convertView.findViewById(R.id.iv_item_layout_haibao);
			holder.definition = (ImageView) convertView.findViewById(R.id.iv_item_layout_gaoqing_logo);
			convertView.setTag(holder);
			convertView.setPadding(GRIDVIEW_ITEM_PADDING_LEFT,
					GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING_LEFT,
					GRIDVIEW_ITEM_PADDING);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.nameTv.setText(itemInfo.getMovieName());
		aq.id(holder.haibaoIv).image(itemInfo.getMoviePicUrl(), true, true);
		
//		String definition = itemInfo.getDefinition();
//		
//		if(definition != null && !definition.equals("")) {
//			holder.definition.setVisibility(View.VISIBLE);
//			switch (Integer.valueOf(definition)) {
//			case 8:
//				holder.definition.setImageResource(R.drawable.icon_bd);
//				break;
//			case 7:
//				holder.definition.setImageResource(R.drawable.icon_hd);
//				break;
//			case 6:
//				holder.definition.setImageResource(R.drawable.icon_ts);
//				break;
//			default:
//				holder.definition.setVisibility(View.GONE);
//				break;
//			}
//		}
		
		if(Constant.SO_HU_CP.equalsIgnoreCase(itemInfo.getSources())){
			holder.definition.setVisibility(View.VISIBLE);
			holder.definition.setImageResource(R.drawable.icon_sohu);
		}
		
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
				width, height);
		convertView.setLayoutParams(params);
		convertView.setVisibility(View.VISIBLE);
		return convertView;
	}

	
	class ViewHolder{
		TextView nameTv;
		TextView scoreTv;
		TextView otherInfo;
		ImageView haibaoIv;
		ImageView definition;
	}
}
