package com.joyplus.tv.ui;

import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.joyplus.tv.R;

public class KeyBoardView extends LinearLayout implements android.view.View.OnClickListener{
	
	private EditText mEditText;
	private OnKeyBoardResultListener listener;

	public KeyBoardView(Context context, EditText editView, OnKeyBoardResultListener listener) {
		super(context);
		// TODO Auto-generated constructor stub
		this.mEditText = editView;
		this.listener = listener;
		View rootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_keybard, null);
		
		Button btn_0 = (Button) rootView.findViewById(R.id.key_0);
		Button btn_1 = (Button) rootView.findViewById(R.id.key_1);
		Button btn_2 = (Button) rootView.findViewById(R.id.key_2);
		Button btn_3 = (Button) rootView.findViewById(R.id.key_3);
		Button btn_4 = (Button) rootView.findViewById(R.id.key_4);
		Button btn_5 = (Button) rootView.findViewById(R.id.key_5);
		Button btn_6 = (Button) rootView.findViewById(R.id.key_6);
		Button btn_7 = (Button) rootView.findViewById(R.id.key_7);
		Button btn_8 = (Button) rootView.findViewById(R.id.key_8);
		Button btn_9 = (Button) rootView.findViewById(R.id.key_9);
		
		Button btn_a = (Button) rootView.findViewById(R.id.key_a);
		Button btn_b = (Button) rootView.findViewById(R.id.key_b);
		Button btn_c = (Button) rootView.findViewById(R.id.key_c);
		Button btn_d = (Button) rootView.findViewById(R.id.key_d);
		Button btn_e = (Button) rootView.findViewById(R.id.key_e);
		Button btn_f = (Button) rootView.findViewById(R.id.key_f);
		Button btn_g = (Button) rootView.findViewById(R.id.key_g);
		Button btn_h = (Button) rootView.findViewById(R.id.key_h);
		Button btn_i = (Button) rootView.findViewById(R.id.key_i);
		Button btn_j = (Button) rootView.findViewById(R.id.key_j);
		Button btn_k = (Button) rootView.findViewById(R.id.key_k);
		Button btn_l = (Button) rootView.findViewById(R.id.key_l);
		Button btn_m = (Button) rootView.findViewById(R.id.key_m);
		Button btn_n = (Button) rootView.findViewById(R.id.key_n);
		Button btn_o = (Button) rootView.findViewById(R.id.key_o);
		Button btn_p = (Button) rootView.findViewById(R.id.key_p);
		Button btn_q = (Button) rootView.findViewById(R.id.key_q);
		Button btn_r = (Button) rootView.findViewById(R.id.key_r);
		Button btn_s = (Button) rootView.findViewById(R.id.key_s);
		Button btn_t = (Button) rootView.findViewById(R.id.key_t);
		Button btn_u = (Button) rootView.findViewById(R.id.key_u);
		Button btn_v = (Button) rootView.findViewById(R.id.key_v);
		Button btn_w = (Button) rootView.findViewById(R.id.key_w);
		Button btn_x = (Button) rootView.findViewById(R.id.key_x);
		Button btn_y = (Button) rootView.findViewById(R.id.key_y);
		Button btn_z = (Button) rootView.findViewById(R.id.key_z);

		Button btn_doute = (Button) rootView.findViewById(R.id.key_doute);
		Button btn_backspace = (Button) rootView.findViewById(R.id.key_backspace);
		Button btn_canle = (Button) rootView.findViewById(R.id.key_cancle);
		Button btn_search = (Button) rootView.findViewById(R.id.key_search);
		
		btn_0.setOnClickListener(this);
		btn_1.setOnClickListener(this);
		btn_2.setOnClickListener(this);
		btn_3.setOnClickListener(this);
		btn_4.setOnClickListener(this);
		btn_5.setOnClickListener(this);
		btn_6.setOnClickListener(this);
		btn_7.setOnClickListener(this);
		btn_8.setOnClickListener(this);
		btn_9.setOnClickListener(this);
		
		btn_a.setOnClickListener(this);
		btn_b.setOnClickListener(this);
		btn_c.setOnClickListener(this);
		btn_d.setOnClickListener(this);
		btn_e.setOnClickListener(this);
		btn_f.setOnClickListener(this);
		btn_g.setOnClickListener(this);
		btn_h.setOnClickListener(this);
		btn_i.setOnClickListener(this);
		btn_j.setOnClickListener(this);
		btn_k.setOnClickListener(this);
		btn_l.setOnClickListener(this);
		btn_m.setOnClickListener(this);
		btn_n.setOnClickListener(this);
		btn_o.setOnClickListener(this);
		btn_p.setOnClickListener(this);
		btn_q.setOnClickListener(this);
		btn_r.setOnClickListener(this);
		btn_s.setOnClickListener(this);
		btn_t.setOnClickListener(this);
		btn_u.setOnClickListener(this);
		btn_v.setOnClickListener(this);
		btn_w.setOnClickListener(this);
		btn_x.setOnClickListener(this);
		btn_y.setOnClickListener(this);
		btn_z.setOnClickListener(this);
		
		btn_doute.setOnClickListener(this);
		btn_backspace.setOnClickListener(this);
		btn_canle.setOnClickListener(this);
		btn_search.setOnClickListener(this);
		
		rootView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		addView(rootView);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Editable editable = mEditText.getText();
		int start = mEditText.getSelectionStart();
		switch (v.getId()) {
		case R.id.key_backspace:
			if (editable != null && editable.length() > 0) {
				if (start > 0) {
					editable.delete(start - 1, start);
				}
			}
			break;
		case R.id.key_search:
			listener.onResult(true);
			break;
		case R.id.key_cancle:
			listener.onResult(false);
			break;
		default:
			editable.insert(start, ((Button)v).getText());
			break;
		}
	}
	
	public interface OnKeyBoardResultListener{
		abstract void onResult(boolean isSearch);
	}

}
