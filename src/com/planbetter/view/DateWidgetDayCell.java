package com.planbetter.view;

import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout.LayoutParams;

import com.planbetter.activity.R;
import com.planbetter.bean.TaskBean;
import com.planbetter.constant.DayStyle;
import com.planbetter.constant.TaskConstant;
import com.planbetter.dao.DatabaseUtil;
import com.planbetter.date.DateUtils;


public class DateWidgetDayCell extends View {
	// types
	public interface OnItemClick {
		public void OnClick(DateWidgetDayCell item);
	}

	public static int ANIM_ALPHA_DURATION = 100;
	// fields
	private int fTextSize = 22;//从22修改至12
    private boolean dataflag = false;
	private int iDateYear = 0;
	private int iDateMonth = 0;
	private int iDateDay = 0;

	// fields
	private OnItemClick itemClick = null;
	private Paint pt = new Paint();
	private RectF rect = new RectF();
	private String sDate = "";

	// fields
	private boolean bSelected = false;
	private boolean bIsActiveMonth = false;
	private boolean bToday = false;
	private boolean bHoliday = false;
	private boolean bTouchedDown = false;
	private Context mcontext;

	// methods
	public DateWidgetDayCell(Context context, int iWidth, int iHeight) {
		super(context);
		setFocusable(true);
		setLayoutParams(new LayoutParams(iWidth, iHeight));
		
		mcontext = context;
		
		fTextSize = iWidth/2;
	}

	public boolean getSelected() {
		return this.bSelected;
	}

	public void setSelected(boolean bEnable) {
		if (this.bSelected != bEnable) {
			this.bSelected = bEnable;
			this.invalidate();
		}
	}

	public void setData(int iYear, int iMonth, int iDay, boolean bToday,
			boolean bHoliday, int iActiveMonth) {
		iDateYear = iYear;
		iDateMonth = iMonth;
		iDateDay = iDay;

		this.sDate = Integer.toString(iDateDay);
		this.bIsActiveMonth = (iDateMonth == iActiveMonth);
		this.bToday = bToday;
		this.bHoliday = bHoliday;
		this.dataflag = checkData(iYear, iMonth, iDay);
	}

	public void setItemClick(OnItemClick itemClick) {
		this.itemClick = itemClick;
	}

	private int getTextHeight() {
		return (int) (-pt.ascent() + pt.descent());
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean bResult = super.onKeyDown(keyCode, event);
		if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
				|| (keyCode == KeyEvent.KEYCODE_ENTER)) {
			doItemClick();
		}
		return bResult;
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean bResult = super.onKeyUp(keyCode, event);
		return bResult;
	}

	public void doItemClick() {
		if (itemClick != null)
			itemClick.OnClick(this);
	}

	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		invalidate();
	}

	public Calendar getDate() {
		Calendar calDate = Calendar.getInstance();
		calDate.clear();
		calDate.set(Calendar.YEAR, iDateYear);
		calDate.set(Calendar.MONTH, iDateMonth);
		calDate.set(Calendar.DAY_OF_MONTH, iDateDay);
		return calDate;
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// init rectangles
		rect.set(0, 0, this.getWidth(), this.getHeight());
		rect.inset(1, 1);

		// drawing
		final boolean bFocused = IsViewFocused();

		drawDayView(canvas, bFocused);
		drawDayNumber(canvas, bFocused);
	}

	private void drawDayView(Canvas canvas, boolean bFocused) {
		if(bIsActiveMonth) {
			if (bSelected || bFocused) {
				LinearGradient lGradBkg = null;
	
				if (bFocused) {
					lGradBkg = new LinearGradient(rect.left, 0, rect.right, 0,
							DayStyle.iColorBkgFocusDark,
							DayStyle.iColorBkgFocusLight, Shader.TileMode.CLAMP);
				}
	
				if (bSelected) {
					lGradBkg = new LinearGradient(rect.left, 0, rect.right, 0,
							DayStyle.iColorBkgSelectedDark,
							DayStyle.iColorBkgSelectedLight, Shader.TileMode.CLAMP);
				}
	
				if (lGradBkg != null) {
					pt.setShader(lGradBkg);
					canvas.drawRect(rect, pt);
				}
	
				pt.setShader(null);
	
			} else {
	
				pt.setColor(DayStyle.getColorBkg(bHoliday, bToday));
				
				/*if (!bIsActiveMonth)//如果不是当月
					pt.setAlpha(iAlphaInactiveMonth);*/
				canvas.drawRect(rect, pt);
			}
		} else {
			pt.setShader(null);
		}
	}

	public void drawDayNumber(Canvas canvas, boolean bFocused) {
		// draw day number
		pt.setTypeface(null);
		pt.setAntiAlias(true);
		pt.setShader(null);
		pt.setFakeBoldText(true);
		pt.setTextSize(fTextSize);

		pt.setUnderlineText(false);
		if (bToday)
			pt.setUnderlineText(true);

		int iTextPosX = (int) rect.right - (int) pt.measureText(sDate);
		int iTextPosY = (int) rect.bottom + (int) (-pt.ascent())
				- getTextHeight();

		iTextPosX -= ((int) rect.width() >> 1)
				- ((int) pt.measureText(sDate) >> 1);
		iTextPosY -= ((int) rect.height() >> 1) - (getTextHeight() >> 1);

		// draw text

		if (bIsActiveMonth)
		{		
			if (bSelected || bFocused) {
				if (bSelected)
					pt.setColor(DayStyle.iColorTextSelected);
				if (bFocused)
					pt.setColor(DayStyle.iColorTextFocused);
			} else {
				pt.setColor(DayStyle.getColorText(bHoliday, bToday));
			}
			if(dataflag)
			{
				Bitmap bitmap = BitmapFactory.decodeResource(
		    				mcontext.getResources(), R.drawable.history_flag);
				canvas.drawBitmap(bitmap, 4, 8, pt);
			}
		    canvas.drawText(sDate, iTextPosX, iTextPosY+8, pt);
		    pt.setUnderlineText(false);
		}
	}


	public boolean IsViewFocused() {
		return (this.isFocused() || bTouchedDown);
	}

	public boolean onTouchEvent(MotionEvent event) {
		boolean bHandled = false;
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			bHandled = true;
			bTouchedDown = true;
			invalidate();
			startAlphaAnimIn(DateWidgetDayCell.this);
		}
		if (event.getAction() == MotionEvent.ACTION_CANCEL) {
			bHandled = true;
			bTouchedDown = false;
			invalidate();
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			bHandled = true;
			bTouchedDown = false;
			invalidate();
			doItemClick();
		}
		return bHandled;
	}

	public static void startAlphaAnimIn(View view) {
		AlphaAnimation anim = new AlphaAnimation(0.5F, 1);
		anim.setDuration(ANIM_ALPHA_DURATION);
		anim.startNow();
		view.startAnimation(anim);
	}
	
	private boolean checkData(int year, int month, int day)
	{
		boolean hasData = false;
		String date = DateUtils.formatDate(year, month+1, day);
		
		Cursor todayTaskCur = DatabaseUtil.query(mcontext,
				TaskBean.TABLE_NAME, null, TaskBean.DATETIME + " LIKE ? AND "+TaskBean.IF_FUTURE+" = ?",
				new String[] { date + "%", TaskConstant.NOT_FUTURE+"" }, null, null, null);

		// 遍历今天的任务信息
		todayTaskCur.moveToFirst();
		hasData = !todayTaskCur.isAfterLast();
		DatabaseUtil.closeDatabase();
	
		Log.d("debug", date+hasData);
		return hasData;
	}

}
