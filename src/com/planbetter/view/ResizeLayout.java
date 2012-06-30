package com.planbetter.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

public class ResizeLayout extends LinearLayout {

	int count = 0;
	int count1 = 0;
	int count2 = 0;
	//定义默认的软键盘最小高度，这是为了避免onSizeChanged在某些下特殊情况下出现的问题。
//	private static final int SOFTKEYPAD_MIN_HEIGHT = 50;
	private Handler uiHandler = new Handler();
	private static final String TAG = "ResizeLayout";

	public ResizeLayout(Context context) {
		super(context);
	}

	public ResizeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onSizeChanged(int w, final int h, int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		Log.i(TAG, "onSizeChanged " + count++ + "=>onResize called! w=" + w + ",h=" + h + ",oldw=" + oldw + ",oldh=" + oldh);

		uiHandler.post(new Runnable() {

			public void run() {
				
			}
		});
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		Log.e(TAG, "onLayout " + count1++ + "=>OnLayout called! l=" + l + ", t=" + t + ",r=" + r + ",b=" + b);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		Log.e(TAG, "onMeasure " + count2++ + "=>onMeasure called! widthMeasureSpec=" + widthMeasureSpec + ", heightMeasureSpec="
				+ heightMeasureSpec);

	}
}
