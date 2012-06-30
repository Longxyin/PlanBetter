package com.planbetter.constant;

import java.util.*;

public class DayStyle {
	// methods

	private static String[] getWeekDayNames() {
		String[] vec = new String[10];
		vec[Calendar.SUNDAY] = "日";
		vec[Calendar.MONDAY] = "一";
		vec[Calendar.TUESDAY] = "二";
		vec[Calendar.WEDNESDAY] = "三";
		vec[Calendar.THURSDAY] = "四";
		vec[Calendar.FRIDAY] = "五";
		vec[Calendar.SATURDAY] = "六";

		return vec;
	}

	public static String getWeekDayName(int iDay) {
		return vecStrWeekDayNames[iDay];
	}

	// fields
	private final static String[] vecStrWeekDayNames = getWeekDayNames();

	// fields
	/*public final static int iColorFrameHeader = 0xff666666;
	public final static int iColorFrameHeaderHoliday = 0xff707070;
	public final static int iColorTextHeader = 0xffcccccc;
	public final static int iColorTextHeaderHoliday = 0xffd0d0d0;

	public final static int iColorText = 0xffdddddd;
	public final static int iColorBkg = 0xff888888;
	public final static int iColorTextHoliday = 0xfff0f0f0;
	public final static int iColorBkgHoliday = 0xffaaaaaa;

	public final static int iColorTextToday = 0xff002200;
	public final static int iColorBkgToday = 0xff88bb88;

	public final static int iColorTextSelected = 0xff001122;
	public final static int iColorBkgSelectedLight = 0xffbbddff;
	public final static int iColorBkgSelectedDark = 0xff225599;

	public final static int iColorTextFocused = 0xff221100;
	public final static int iColorBkgFocusLight = 0xffffddbb;
	public final static int iColorBkgFocusDark = 0xffaa5500;*/
	
	public final static int iColorFrameHeader = 0xffff00;//0xff4169E1;/*M-F的color 0xff4169E1*/
	public final static int iColorFrameHeaderHoliday = 0xffff00;/*S_S color*/
	public final static int iColorTextHeader = 0xff4169E1;
	public final static int iColorTextHeaderHoliday = 0xff4169E1;

	public final static int iColorText = 0xff000000;
	public final static int iColorBkg = 0x111111;// 0xff00BFFF;//蓝色
	public final static int iColorTextHoliday = 0xff000000;
	public final static int iColorBkgHoliday = 0x111111;//0xff9933FF;//紫色

	public final static int iColorTextToday = 0xffE066FF;
	public final static int iColorBkgToday = 0xffE0EEE0;//0xffff0000;//大红色

	public final static int iColorTextSelected = 0xff4169E1;
	public final static int iColorBkgSelectedLight = 0xffFFE4B5;//0xffff0099;//粉色
	public final static int iColorBkgSelectedDark = 0xffFFE4B5;//0xffff33CC;//浅粉色

	public final static int iColorTextFocused = 0xff221100;
	public final static int iColorBkgFocusLight = 0x111111;//0xff00ff00;//绿色
	public final static int iColorBkgFocusDark = 0x111111;//0xff66ff66;//浅绿色

	
	// methods
	public static int getColorFrameHeader(boolean bHoliday) {
		if (bHoliday)
			return iColorFrameHeaderHoliday;
		return iColorFrameHeader;
	}
	

	public static int getColorTextHeader(boolean bHoliday) {
		if (bHoliday)
			return iColorTextHeaderHoliday;
		return iColorTextHeader;
	}

	public static int getColorText(boolean bHoliday, boolean bToday) {
		if (bToday)
			return iColorTextToday;
		if (bHoliday)
			return iColorTextHoliday;
		return iColorText;
	}

	public static int getColorBkg(boolean bHoliday, boolean bToday) {
		if (bToday)
			return iColorBkgToday;
		if (bHoliday)
			return iColorBkgHoliday;
		return iColorBkg;
	}

	public static int getWeekDay(int index, int iFirstDayOfWeek) {
		int iWeekDay = -1;

		if (iFirstDayOfWeek == Calendar.MONDAY) {
			iWeekDay = index + Calendar.MONDAY;
			if (iWeekDay > Calendar.SATURDAY)
				iWeekDay = Calendar.SUNDAY;
		}

		if (iFirstDayOfWeek == Calendar.SUNDAY) {
			iWeekDay = index + Calendar.SUNDAY;
		}

		return iWeekDay;
	}

}
