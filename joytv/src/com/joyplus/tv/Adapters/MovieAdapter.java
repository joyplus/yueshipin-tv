package com.joyplus.tv.Adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.joyplus.tv.R;
import com.joyplus.tv.entity.GridViewItemHodler;
import com.joyplus.tv.entity.MovieItemData;
import com.joyplus.tv.utils.JieMianConstant;

public class MovieAdapter extends BaseAdapter implements JieMianConstant{
	private int popWidth,popHeight;
	private List<MovieItemData> movieList = new ArrayList<MovieItemData>();

	private Context context;
	
	public MovieAdapter(Context context) {
		
		this.context = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (movieList.size() <= 0) {

			return DEFAULT_ITEM_NUM;
		}
		return movieList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (movieList.size() <= 0) {

			return null;
		}
		return movieList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		if (movieList.size() <= 0) {

			return DEFAULT_ITEM_NUM;
		}
		return movieList.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		GridViewItemHodler viewItemHodler = null;

		int width = parent.getWidth() / 5;
		int height = (int) (width / 1.0f / STANDARD_PIC_WIDTH * STANDARD_PIC_HEIGHT);

		if (convertView == null) {
			viewItemHodler = new GridViewItemHodler();
			convertView = ((Activity)context).getLayoutInflater().inflate(
					R.layout.show_item_layout_dianying, null);
			viewItemHodler.nameTv = (TextView) convertView
					.findViewById(R.id.tv_item_layout_name);
			viewItemHodler.scoreTv = (TextView) convertView
					.findViewById(R.id.tv_item_layout_score);
			viewItemHodler.otherInfo = (TextView) convertView
					.findViewById(R.id.tv_item_layout_other_info);
			viewItemHodler.haibaoIv = (ImageView) convertView
					.findViewById(R.id.iv_item_active_layout_haibao);
			convertView.setTag(viewItemHodler);

		} else {

			viewItemHodler = (GridViewItemHodler) convertView.getTag();
		}

		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
				width, height);
		convertView.setLayoutParams(params);
		convertView.setPadding(GRIDVIEW_ITEM_PADDING_LEFT,
				GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING_LEFT,
				GRIDVIEW_ITEM_PADDING);

		if (width != 0) {

			popWidth = width;
			popHeight = height;
		}

		if (movieList.size() <= 0) {

			return convertView;
		}

		viewItemHodler.nameTv.setText(movieList.get(position)
				.getMovieName());
		viewItemHodler.scoreTv.setText(movieList.get(position)
				.getMovieScore());

		String duration = movieList.get(position).getMovieDuration();
		if (duration != null && !duration.equals("")) {

			viewItemHodler.otherInfo.setText(movieList.get(position)
					.getMovieDuration());
		}

		AQuery aq = new AQuery(convertView);
		aq.id(R.id.iv_item_layout_haibao).image(
				movieList.get(position).getMoviePicUrl(), true, true, 0,
				R.drawable.post_normal);
		return convertView;
	}
	
	public int getHeight() {
		return popHeight;
	}

	public int getWidth() {
		return popWidth;
	}

	public void setList(List<MovieItemData> list) {
		this.movieList = list;
	}
	
	public List<MovieItemData> getMovieList() {
		return movieList;
	}

}
