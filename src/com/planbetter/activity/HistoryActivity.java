package com.planbetter.activity;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.planbetter.constant.DayStyle;
import com.planbetter.constant.MenuItemId;
import com.planbetter.constant.MotionEventConstant;
import com.planbetter.view.DateWidgetDayCell;
import com.planbetter.view.DateWidgetDayHeader;
import com.planbetter.view.SymbolButton;

public class HistoryActivity extends Activity implements OnGestureListener {
	private ArrayList<DateWidgetDayCell> days = new ArrayList<DateWidgetDayCell>();
	// private SimpleDateFormat dateMonth = new SimpleDateFormat("MMMM yyyy");
	private Calendar calStartDate = Calendar.getInstance();
	private Calendar calToday = Calendar.getInstance();
	private Calendar calCalendar = Calendar.getInstance();
	private Calendar calSelected = Calendar.getInstance();
	LinearLayout layContent = null;

	Button btnPrev = null;
	Button btnToday = null;
	Button btnNext = null;

	private int iFirstDayOfWeek = Calendar.MONDAY;
	private int iMonthViewCurrentMonth = 0;
	private int iMonthViewCurrentYear = 0;
	public static final int SELECT_DATE_REQUEST = 111;
	private int iDayCellSize = 42;// 原值是38
	private int iDayHeaderHeight = 24;// 原值是24
	private int cellpadding = 8;
	private int iTotalWidth = (iDayCellSize * 7);

//	private int mYear = 0;
//	private int mMonth = 0;
//	private int mDay = 0;

	private GestureDetector mGestureDetector;
	
	@Override
	public void onCreate(Bundle icicle) {
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels; // 屏幕宽度（像素）
//		int height = metric.heightPixels; // 屏幕高度（像素）

		iDayCellSize = (width - 30) / 7;
		iTotalWidth = (iDayCellSize * 7);
		iDayHeaderHeight = iDayCellSize;

		super.onCreate(icicle);
		iFirstDayOfWeek = Calendar.MONDAY;
//		mYear = calSelected.get(Calendar.YEAR);
//		mMonth = calSelected.get(Calendar.MONTH);
//		mDay = calSelected.get(Calendar.DAY_OF_MONTH);
		setContentView(generateContentView());
		calStartDate = getCalendarStartDate();
		DateWidgetDayCell daySelected = updateCalendar();
		if (daySelected != null)
			daySelected.requestFocus();
		
		mGestureDetector = new GestureDetector(this);  

	}

	@Override
	public void onStart() {
		super.onStart();

	}

	private LinearLayout createLayout(int iOrientation) {
		LinearLayout lay = new LinearLayout(this);
		lay.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		lay.setOrientation(iOrientation);
		return lay;
	}

	private Button createButton(String sText, int iWidth, int iHeight) {
		Button btn = new Button(this);
		btn.setText(sText);
		btn.setLayoutParams(new LayoutParams(iWidth, iHeight));
		return btn;
	}

	/* 产生了上面的button群，采用了andriod的自带系统图标 */
	private void generateTopButtons(LinearLayout layTopControls) {
		final int iHorPadding = 24;
		final int iSmallButtonWidth = 60;
		btnToday = createButton("", iTotalWidth - iSmallButtonWidth
				- iSmallButtonWidth,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		btnToday.setPadding(iHorPadding, btnToday.getPaddingTop(), iHorPadding,
				btnToday.getPaddingBottom());
		btnToday.setBackgroundResource(android.R.drawable.btn_default_small);

		SymbolButton btnPrev = new SymbolButton(this,
				SymbolButton.symbol.arrowLeft);
		btnPrev.setLayoutParams(new LayoutParams(iSmallButtonWidth,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		btnPrev.setBackgroundResource(android.R.drawable.btn_default_small);

		SymbolButton btnNext = new SymbolButton(this,
				SymbolButton.symbol.arrowRight);
		btnNext.setLayoutParams(new LayoutParams(iSmallButtonWidth,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		btnNext.setBackgroundResource(android.R.drawable.btn_default_small);

		// set events
		btnPrev.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				setPrevViewItem();/* 上一个月的显示内容 */
			}
		});
		btnToday.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {

				setTodayViewItem();/* 显示今天的相关信息 */
				String s = calToday.get(Calendar.YEAR) + "年"
						+ (calToday.get(Calendar.MONTH) + 1) + "月";
				btnToday.setText(s);
			}
		});
		btnNext.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				setNextViewItem();/* 下一个月的显示内容 */
			}
		});

		/* 指定控件的基本位置，水平方向的中央 */
		layTopControls.setGravity(Gravity.CENTER_HORIZONTAL);
		layTopControls.addView(btnPrev);
		layTopControls.addView(btnToday);
		layTopControls.addView(btnNext);
	}

	/* 产生整个日历的视图 */
	private View generateContentView() {
		LinearLayout layMain = createLayout(LinearLayout.VERTICAL);
		layMain.setPadding(cellpadding, cellpadding, cellpadding, cellpadding);
		layMain.setBackgroundResource(R.drawable.calendar_bg);
		LinearLayout layTopControls = createLayout(LinearLayout.HORIZONTAL);

		layContent = createLayout(LinearLayout.VERTICAL);
		layContent.setPadding(5, 0, 5, 0);/* 20,0,20,0 */
		generateTopButtons(layTopControls);
		generateCalendar(layContent);

		layMain.addView(layTopControls);/* 日历的头部日期选择相关 */
		layMain.addView(layContent);/* 日历的网格内容部分 */

		return layMain;
	}

	/* 产生日历的一行显示的内容 */
	private View generateCalendarRow() {
		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
		for (int iDay = 0; iDay < 7; iDay++) {
			DateWidgetDayCell dayCell = new DateWidgetDayCell(this,
					iDayCellSize, iDayCellSize + 10);// 这里修改过！
			dayCell.setItemClick(mOnDayCellClick);
			days.add(dayCell);
			layRow.addView(dayCell);
		}
		return layRow;
	}

	/* 产生日历的头部，如周一神马的 */
	private View generateCalendarHeader() {
		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
		for (int iDay = 0; iDay < 7; iDay++) {
			DateWidgetDayHeader day = new DateWidgetDayHeader(this,
					iDayCellSize, iDayHeaderHeight);
			final int iWeekDay = DayStyle.getWeekDay(iDay, iFirstDayOfWeek);
			day.setData(iWeekDay);
			layRow.addView(day);
		}
		return layRow;
	}

	/* 组合产生日历的说 */
	private void generateCalendar(LinearLayout layContent) {
		layContent.addView(generateCalendarHeader());
		days.clear();
		for (int iRow = 0; iRow < 6; iRow++) {
			layContent.addView(generateCalendarRow());
		}
	}

	/* 得到日历的起始时间 */
	private Calendar getCalendarStartDate() {
		calToday.setTimeInMillis(System.currentTimeMillis());
		calToday.setFirstDayOfWeek(iFirstDayOfWeek);

		if (calSelected.getTimeInMillis() == 0) {
			calStartDate.setTimeInMillis(System.currentTimeMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		} else {
			calStartDate.setTimeInMillis(calSelected.getTimeInMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		}

		UpdateStartDateForMonth();

		return calStartDate;
	}

	private DateWidgetDayCell updateCalendar() {
		DateWidgetDayCell daySelected = null;
		boolean bSelected = false;
		final boolean bIsSelection = (calSelected.getTimeInMillis() != 0);
		final int iSelectedYear = calSelected.get(Calendar.YEAR);
		final int iSelectedMonth = calSelected.get(Calendar.MONTH);
		final int iSelectedDay = calSelected.get(Calendar.DAY_OF_MONTH);
		calCalendar.setTimeInMillis(calStartDate.getTimeInMillis());
		for (int i = 0; i < days.size(); i++) {
			final int iYear = calCalendar.get(Calendar.YEAR);
			final int iMonth = calCalendar.get(Calendar.MONTH);
			final int iDay = calCalendar.get(Calendar.DAY_OF_MONTH);
			final int iDayOfWeek = calCalendar.get(Calendar.DAY_OF_WEEK);
			DateWidgetDayCell dayCell = days.get(i);
			// check today
			boolean bToday = false;
			if (calToday.get(Calendar.YEAR) == iYear)
				if (calToday.get(Calendar.MONTH) == iMonth)
					if (calToday.get(Calendar.DAY_OF_MONTH) == iDay)
						bToday = true;
			// check holiday
			boolean bHoliday = false;
			if ((iDayOfWeek == Calendar.SATURDAY)
					|| (iDayOfWeek == Calendar.SUNDAY))
				bHoliday = true;
			if ((iMonth == Calendar.JANUARY) && (iDay == 1))
				bHoliday = true;

			dayCell.setData(iYear, iMonth, iDay, bToday, bHoliday,
					iMonthViewCurrentMonth);
			bSelected = false;
			if (bIsSelection)
				if ((iSelectedDay == iDay) && (iSelectedMonth == iMonth)
						&& (iSelectedYear == iYear)) {
					bSelected = true;
				}
			dayCell.setSelected(bSelected);
			if (bSelected)
				daySelected = dayCell;
			calCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		layContent.invalidate();
		return daySelected;
	}

	private void UpdateStartDateForMonth() {
		iMonthViewCurrentMonth = calStartDate.get(Calendar.MONTH);
		iMonthViewCurrentYear = calStartDate.get(Calendar.YEAR);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		UpdateCurrentMonthDisplay();
		// update days for week
		int iDay = 0;
		int iStartDay = iFirstDayOfWeek;
		if (iStartDay == Calendar.MONDAY) {
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
			if (iDay < 0)
				iDay = 6;
		}
		if (iStartDay == Calendar.SUNDAY) {
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
			if (iDay < 0)
				iDay = 6;
		}
		calStartDate.add(Calendar.DAY_OF_WEEK, -iDay);
	}

	private void UpdateCurrentMonthDisplay() {
		String s = calStartDate.get(Calendar.YEAR) + "年"
				+ (calStartDate.get(Calendar.MONTH) + 1) + "月";// dateMonth.format(calCalendar.getTime());
		btnToday.setText(s);
//		mYear = calCalendar.get(Calendar.YEAR);
	}

	private void setPrevViewItem() {
		iMonthViewCurrentMonth--;
		if (iMonthViewCurrentMonth == -1) {
			iMonthViewCurrentMonth = 11;
			iMonthViewCurrentYear--;
		}
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
		UpdateStartDateForMonth();
		updateCalendar();

	}
	
	public boolean onTouchEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	private void setTodayViewItem() {
		calToday.setTimeInMillis(System.currentTimeMillis());
		calToday.setFirstDayOfWeek(iFirstDayOfWeek);
		calStartDate.setTimeInMillis(calToday.getTimeInMillis());
		calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		UpdateStartDateForMonth();
		updateCalendar();
	}

	private void setNextViewItem() {
		iMonthViewCurrentMonth++;
		if (iMonthViewCurrentMonth == 12) {
			iMonthViewCurrentMonth = 0;
			iMonthViewCurrentYear++;
		}
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
		UpdateStartDateForMonth();
		updateCalendar();

	}

	private DateWidgetDayCell.OnItemClick mOnDayCellClick = new DateWidgetDayCell.OnItemClick() {
		public void OnClick(DateWidgetDayCell item) {
			calSelected.setTimeInMillis(item.getDate().getTimeInMillis());

			item.setSelected(true);
			updateCalendar();

			int year = calSelected.get(Calendar.YEAR);
			int month = calSelected.get(Calendar.MONTH) + 2;
			int day = calSelected.get(Calendar.DAY_OF_MONTH);
			int x = year * 10000 + month * 100 + day;

			Intent intent = new Intent(HistoryActivity.this,
					HistoryOnedayActivity.class);
			intent.putExtra("today", x);
			startActivity(intent);
		}
	};

	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// 向右滑动
		if (e2.getX() - e1.getX() > MotionEventConstant.getFlingMinDistance()
				&& Math.abs(velocityX) > MotionEventConstant
						.getFlingMinVelocity()) {
			Message mes = new Message();
			mes.what = SET_PREV_VIEW;
			handler.sendMessage(mes);
		}
		// 向左滑动
		else if (e1.getX() - e2.getX() > MotionEventConstant
				.getFlingMinDistance()
				&& Math.abs(velocityX) > MotionEventConstant
						.getFlingMinVelocity()) {
			Message mes = new Message();
			mes.what = SET_NETX_VIEW;
			handler.sendMessage(mes);
		}

		return true;
	}
	
	public static final int SET_NETX_VIEW = 1;
	public static final int SET_PREV_VIEW = 2;
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what) {
			case SET_NETX_VIEW:
				setNextViewItem();/* 下一个月的显示内容 */
				break;
			case SET_PREV_VIEW:
				setPrevViewItem();/* 上一个月的显示内容 */
				break;
			}
		}
		
	};

	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case MenuItemId.MENU_ITEM_TASK:
			Intent ht_intent = new Intent(HistoryActivity.this,
					TaskActivity.class);
			startActivity(ht_intent);
			break;
		// 目标
		case MenuItemId.MENU_ITEM_GOAL:
			Intent tg_intent = new Intent(HistoryActivity.this,
					GoalActivity.class);
			startActivity(tg_intent);
			break;
		// 心语
		case MenuItemId.MENU_ITEM_TIPS:
			Intent tt_intent = new Intent(HistoryActivity.this,
					HeartActivity.class);
			startActivity(tt_intent);
			break;
		// 设置
		case MenuItemId.MENU_ITEM_SETUP:
			Intent ti_intent = new Intent(HistoryActivity.this,
					SetupActivity.class);
			startActivity(ti_intent);
			break;
		// 帮助
		case MenuItemId.MENU_ITEM_HELP:
			Intent tx_intent = new Intent(HistoryActivity.this,
					HelpActivity.class);
			startActivity(tx_intent);
			break;
		// 关于
		case MenuItemId.MENU_ITEM_ABOUT:
			Intent ty_intent = new Intent(HistoryActivity.this,
					AboutActivity.class);
			startActivity(ty_intent);
			break;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuItem menu_task = menu.add(0, MenuItemId.MENU_ITEM_TASK, 0,
				R.string.menu_task);
		menu_task.setIcon(android.R.drawable.ic_menu_agenda);

		MenuItem menu_goal = menu.add(0, MenuItemId.MENU_ITEM_GOAL, 0,
				R.string.menu_goal);
		menu_goal.setIcon(android.R.drawable.ic_menu_directions);

		MenuItem menu_tips = menu.add(0, MenuItemId.MENU_ITEM_TIPS, 0,
				R.string.menu_tips);
		menu_tips.setIcon(android.R.drawable.ic_menu_compass);

		MenuItem menu_setup = menu.add(0, MenuItemId.MENU_ITEM_SETUP, 0,
				R.string.menu_setup);
		menu_setup.setIcon(android.R.drawable.ic_menu_preferences);

		MenuItem menu_help = menu.add(0, MenuItemId.MENU_ITEM_HELP, 0,
				R.string.menu_help);
		menu_help.setIcon(android.R.drawable.ic_menu_help);

		MenuItem menu_about = menu.add(0, MenuItemId.MENU_ITEM_ABOUT, 0,
				R.string.menu_about);
		menu_about.setIcon(android.R.drawable.ic_menu_info_details);

		return true;
	}

}
