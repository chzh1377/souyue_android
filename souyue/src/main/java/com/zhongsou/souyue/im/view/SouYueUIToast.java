package com.zhongsou.souyue.im.view;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.zhongsou.souyue.R;

public class SouYueUIToast {
	private static Toast toast;
	public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
	public static final int LENGTH_LONG = Toast.LENGTH_LONG;
	
	public static SouYueUIToast makeText(Context context, int resId, int duration) {
		return makeText(context, context.getResources().getString(resId), duration);
	}

	public static SouYueUIToast makeText(Context context, CharSequence text, int duration) {
		return makeText(context, text, duration,Gravity.CENTER,0, 0);
	}
	public static SouYueUIToast makeText(Context context, CharSequence text, int duration,int gravity,int xOffset, int yOffset) {
		if(toast==null){//防止多个toast同时显示影响体验
			toast = new Toast(context);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View root = inflater.inflate(R.layout.souyue_im_toast, null);
			toast.setView(root);
			toast.setGravity(gravity, xOffset, yOffset);
		}
		View root = toast.getView();
		TextView toastTv = (TextView) root.findViewById(R.id.toast_tv);
		toastTv.setText(text);
		toast.setDuration(duration);
		return new SouYueUIToast();
	}

	public void show() {
		if (toast != null)
			toast.show();
	}

}
