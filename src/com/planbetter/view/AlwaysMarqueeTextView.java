package com.planbetter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class AlwaysMarqueeTextView extends TextView {

	public AlwaysMarqueeTextView(Context context) {
		super(context);
	}
	
	public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlwaysMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public boolean isFocused() {
//		Paint paint = new Paint();
//		paint.setTextSize(getTextSize());
//		if(paint.measureText(getText().toString()) > getTextSize()) {
//			return true;
//		}
		return true;
	}

}
