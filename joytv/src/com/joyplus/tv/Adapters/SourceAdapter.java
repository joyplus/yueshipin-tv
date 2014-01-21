package com.joyplus.tv.Adapters;

import java.util.List;

import com.joyplus.tv.R;
import com.joyplus.tv.VideoPlayerJPActivity;
import com.joyplus.tv.entity.PlayerSourceType;
import com.joyplus.utils.Utils;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SourceAdapter extends BaseAdapter {

	private Context c;
	private List<String> date;
	
	public SourceAdapter(Context c, List<String> date){
		this.c = c;
		this.date = date;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return date.size();
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
		String source = date.get(position);
		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
//			LinearLayout layout_contentView = new LinearLayout(c);
			LayoutParams layoutParams = new LayoutParams(Math.round(Utils.getStandardValue(c, 175)),
					Math.round(Utils.getStandardValue(c, 175)));
//			layout_contentView.setLayoutParams(layoutParams);
//			layout_contentView.setPadding(Math.round(Utils.getStandardValue(c, 15)), 
//					Math.round(Utils.getStandardValue(c, 0)), 
//					Math.round(Utils.getStandardValue(c, 15)), 
//					Math.round(Utils.getStandardValue(c, 0)));
			
			LinearLayout layout = new LinearLayout(c);
			layout.setLayoutParams(layoutParams);
			layout.setFocusable(true);
			layout.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.bg_choose_source_color_selector));
			layout.setGravity(Gravity.CENTER);
			layout.setPadding(Math.round(Utils.getStandardValue(c, 10)), 
					Math.round(Utils.getStandardValue(c, 10)), 
					Math.round(Utils.getStandardValue(c, 10)), 
					Math.round(Utils.getStandardValue(c, 10)));
			TextView tv = new TextView(c);
			
//			tv.setId(1);
			tv.setTextColor(Color.WHITE);
			tv.setGravity(Gravity.CENTER);
			tv.setTextSize(30);
			tv.setBackgroundColor(c.getResources().getColor(R.color.movie_bg));
			
			layout.addView(tv, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
//			layout_contentView.addView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
//					LayoutParams.MATCH_PARENT));
			holder.tv = tv;
			convertView = layout;
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		String strSrc = "";
		if (source.equalsIgnoreCase(VideoPlayerJPActivity.SOURCE_LETV)){
			strSrc = c.getString(R.string.videoPlayerJPActivity_source_letv);
		} else if (source.equalsIgnoreCase(PlayerSourceType.TYPE_FENGXING.toSourceName())) {
			strSrc = c.getString(R.string.videoPlayerJPActivity_source_fengxing);
		} else if (source.equalsIgnoreCase(PlayerSourceType.TYPE_QIYI.toSourceName())) {
			strSrc = c.getString(R.string.videoPlayerJPActivity_source_qiyi);
		} else if (source.equalsIgnoreCase(PlayerSourceType.TYPE_YOUKU.toSourceName())) {
			strSrc = c.getString(R.string.videoPlayerJPActivity_source_youku);
		} else if (source.equalsIgnoreCase(PlayerSourceType.TYPE_SINAHD.toSourceName())) {
			strSrc = c.getString(R.string.videoPlayerJPActivity_source_sina);
		} else if (source.equalsIgnoreCase(PlayerSourceType.TYPE_SOHU.toSourceName())) {
			strSrc = c.getString(R.string.videoPlayerJPActivity_source_sohu);
		} else if (source.equalsIgnoreCase(PlayerSourceType.TYPE_QQ.toSourceName())) {
			strSrc = c.getString(R.string.videoPlayerJPActivity_source_qq);
		} else if (source.equalsIgnoreCase(PlayerSourceType.TYPE_PPTV.toSourceName())) {
			strSrc = c.getString(R.string.videoPlayerJPActivity_source_pptv);
		} else if (source.equalsIgnoreCase(PlayerSourceType.TYPE_M1905.toSourceName())) {
			strSrc = c.getString(R.string.videoPlayerJPActivity_source_m1905);
		} else if (source.equalsIgnoreCase(PlayerSourceType.TYPE_P2P.toSourceName())) {
			strSrc = c.getString(R.string.videoPlayerJPActivity_source_p2p);
		}else {//wangpan baidu_wangpan
			strSrc = c.getString(R.string.videoPlayerJPActivity_source_pptv);
		}
		holder.tv.setText(strSrc);
		return convertView;
	}
	
	class ViewHolder{
		TextView tv;
	}

}
