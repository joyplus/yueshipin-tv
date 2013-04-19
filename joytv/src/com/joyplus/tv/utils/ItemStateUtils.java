package com.joyplus.tv.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.joyplus.tv.R;
import com.joyplus.tv.StatisticsUtils;

public class ItemStateUtils implements JieMianConstant{
	
	public static void linearLayoutToPTState(Context context,LinearLayout linearLayout) {

		Button tempButton = (Button) linearLayout.getChildAt(0);
		linearLayout.setBackgroundResource(R.drawable.text_drawable_selector);
		tempButton.setTextColor(context.getResources().getColorStateList(
				R.color.text_color_selector));
		tempButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources()
				.getDrawable(R.drawable.side_hot_normal), null, null, null);
	}

	public static void buttonToPTState(Context context,Button button) {

		button.setBackgroundResource(R.drawable.text_drawable_selector);
		button.setTextColor(context.getResources().getColorStateList(
				R.color.text_color_selector));
	}

	public static void linearLayoutToActiveState(Context context,LinearLayout linearLayout) {

		Button tempButton = (Button) linearLayout.getChildAt(0);
		linearLayout.setBackgroundResource(R.drawable.menubg);
		linearLayout.setPadding(0, 0, WENZI_PADDING_RIGHT, 0);
		tempButton.setTextColor(context.getResources().getColor(R.color.text_active));
		tempButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources()
				.getDrawable(R.drawable.side_hot_active), null, null, null);
	}

	public static void buttonToActiveState(Context context,Button button) {

		button.setBackgroundResource(R.drawable.menubg);
		button.setPadding(0, 0, WENZI_PADDING_RIGHT, 0);
		button.setTextColor(context.getResources().getColor(R.color.text_active));
	}
	
	public static void beforeViewActiveStateBack(Context context,View activeView) {
		if (activeView instanceof LinearLayout) {

			LinearLayout tempLinearLayout = (LinearLayout) activeView;
			ItemStateUtils.linearLayoutToPTState(context,tempLinearLayout);
		} else if (activeView instanceof Button) {

			Button tempButton = (Button) activeView;
			ItemStateUtils.buttonToPTState(context,tempButton);
		}
	}
	
	public static void buttonToFocusState(Context context,Button button) {
		
		button.setTextColor(context.getResources().getColor(
				R.color.text_foucs));
		button.setBackgroundResource(R.drawable.text_drawable_selector);
	}
	
	public static void linearLayoutToFocusState(Context context,LinearLayout linearLayout) {
		
		Button button = (Button) linearLayout.getChildAt(0);

		button.setTextColor(context.getResources().getColor(
				R.color.text_foucs));
		button.setCompoundDrawablesWithIntrinsicBounds(
				context.getResources().getDrawable(
						R.drawable.side_hot_active), null, null,
				null);
		linearLayout.setBackgroundResource(R.drawable.text_drawable_selector);
	}
	
	public static void viewToFocusState(Context context,View v) {
		
		if (v instanceof LinearLayout) {
			LinearLayout linearLayout = (LinearLayout) v;
			ItemStateUtils.linearLayoutToFocusState(context, linearLayout);
		} else if (v instanceof Button) {
			Button button = (Button) v;
			ItemStateUtils.buttonToFocusState(context, button);
		}
	}
	
	public static void viewToOutFocusState(Context context, View v, View activeView) {
		
		if (v instanceof LinearLayout) {

			LinearLayout tempLinearLayout = (LinearLayout) v;
			if (v.getId() == activeView.getId()) {
				ItemStateUtils.linearLayoutToActiveState(context,tempLinearLayout);
			} else {
				ItemStateUtils.linearLayoutToPTState(context,tempLinearLayout);
			}
		} else if (v instanceof Button) {

			Button tempButton = (Button) v;
			if (v.getId() == activeView.getId()) {
				ItemStateUtils.buttonToActiveState(context,tempButton);
			} else {
				ItemStateUtils.buttonToPTState(context,tempButton);
			}
		}
	}
	
	public static View viewToActive(Context context, View v,View activeView) {
		
		if (v instanceof LinearLayout) {
			LinearLayout linearLayout = (LinearLayout) v;
			if (v.getId() != activeView.getId()) {
				ItemStateUtils.beforeViewActiveStateBack(context,activeView);
				ItemStateUtils.linearLayoutToActiveState(context,linearLayout);
				return v;
			}
		} else if (v instanceof Button) {
			Button button = (Button) v;
			if (v.getId() != activeView.getId()) {
				ItemStateUtils.beforeViewActiveStateBack(context,activeView);
				ItemStateUtils.buttonToActiveState(context,button);
				return v;
			}
		}
		
		return null;
	}
	
	public static void viewOutAnimation(Context context,View v) {
		
		ScaleAnimation outScaleAnimation = StatisticsUtils
				.getOutScaleAnimation();
		ImageView iv = (ImageView) v
				.findViewById(R.id.item_layout_dianying_reflact);
		iv.setVisibility(View.VISIBLE);
		v.setBackgroundColor(context.getResources()
				.getColor(android.R.color.transparent));
		v.setPadding(GRIDVIEW_ITEM_PADDING_LEFT,
				GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING_LEFT,
				GRIDVIEW_ITEM_PADDING);
		v.startAnimation(outScaleAnimation);
	}
	
	public static void viewInAnimation(Context context,View v) {
		
		ScaleAnimation inScaleAnimation = StatisticsUtils
				.getInScaleAnimation();
		ImageView iv = (ImageView) v
				.findViewById(R.id.item_layout_dianying_reflact);
		iv.setVisibility(View.GONE);
		v.setPadding(GRIDVIEW_ITEM_PADDING,
				GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING,
				GRIDVIEW_ITEM_PADDING);
		v.setBackgroundColor(context.getResources()
				.getColor(R.color.text_active));

		v.startAnimation(inScaleAnimation);
	}
	
	public static void floatViewOutAnimaiton(View v) {
		
//		ScaleAnimation outScaleAnimation = StatisticsUtils
//				.getOutScaleAnimation();
//		v.startAnimation(outScaleAnimation);
		v.setVisibility(View.GONE);
	}
	
	public static void floatViewInAnimaiton(Context context,View v) {
		
		v.setPadding(GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING,
				GRIDVIEW_ITEM_PADDING, GRIDVIEW_ITEM_PADDING);
		v.setBackgroundColor(context.getResources().getColor(
				R.color.text_active));
		ScaleAnimation inScaleAnimation = StatisticsUtils.getInScaleAnimation();

		v.startAnimation(inScaleAnimation);
	}
	
	public static void setItemPadding(View view) {
		
		view.setPadding(0, 0, WENZI_PADDING_RIGHT, 0);
	}
	
	public static boolean gridViewSmoothScrollCaculate(int position,int beforepostion,int[] beforeFirstAndLastVible,
			boolean isGridViewUp,int popHeight,GridView movieGv) {
		
		boolean isSameContent = position >= beforeFirstAndLastVible[0]
				&& position <= beforeFirstAndLastVible[1];
		if (position >= 5 && !isSameContent) {

			if (beforepostion >= beforeFirstAndLastVible[0]
					&& beforepostion <= beforeFirstAndLastVible[0] + 4) {

				if (isGridViewUp) {

					movieGv.smoothScrollBy(-popHeight, 1000);
					return  true;
				}
			} else {

				if (!isGridViewUp) {

					movieGv.smoothScrollBy(popHeight, 1000 * 2);
					return true;

				}
			}

		}
		return false;
	}
	
	/**
	 * 
	 * @param beforeFirstAndLastVible
	 * @param movieGv
	 * @param isSmoonthScroll
	 * @param top 顶部有一部分显示
	 * @return
	 */
	public static int[] reCaculateFirstAndLastVisbile(int[] beforeFirstAndLastVible,int[] firstAndLastVisible, 
			boolean isSmoonthScroll,boolean top) {
		
		if(!top) {
			
			if (!isSmoonthScroll) {

				beforeFirstAndLastVible[0] = firstAndLastVisible[0];
				beforeFirstAndLastVible[1] = firstAndLastVisible[0] + 9;
			} else {

				beforeFirstAndLastVible[0] = firstAndLastVisible[0] - 5;
				beforeFirstAndLastVible[1] = firstAndLastVisible[0] + 9 - 5;
			}
		} else {
			
			if (!isSmoonthScroll) {

				beforeFirstAndLastVible[0] = firstAndLastVisible[1] - 9;
				beforeFirstAndLastVible[1] = firstAndLastVisible[1];
			} else {

				beforeFirstAndLastVible[0] = firstAndLastVisible[1] - 9 + 5;
				beforeFirstAndLastVible[1] = firstAndLastVisible[1] + 5;
			}
		}
		

		
		return beforeFirstAndLastVible;
	}

}
