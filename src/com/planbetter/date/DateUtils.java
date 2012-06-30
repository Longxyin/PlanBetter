package com.planbetter.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {

	public static final String TODAY = "today";
	public static final String YESTERDAY = "yesterday";
	public static final String TOMORROW = "tomorrow";

	public static String MIDDLE = "12:00";
	
	public static boolean checkTimePassOrNot(String datetime) throws ParseException {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = f.parse(datetime);
		Date now = new Date(System.currentTimeMillis());
		if(date.before(now) || sdf.format(date).equals(sdf.format(now))) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean checkTimeAlertable(int hourOfDay, int minute) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		int curHour = calendar.get(Calendar.HOUR_OF_DAY);
		int curMinute = calendar.get(Calendar.MINUTE);
		if(hourOfDay < curHour || (hourOfDay == curHour && curMinute>= minute)) {
			return false;
		}
		return true;
	}
	
	public static boolean checkTimeAlertable(String datetime) throws ParseException {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = f.parse(datetime);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());
		int curHour = now.get(Calendar.HOUR_OF_DAY);
		int curMinute = now.get(Calendar.MINUTE);
		if(hourOfDay < curHour || (hourOfDay == curHour && curMinute>= minute)) {
			return false;
		}
		return true;
	}
	
	public static String yesterday() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		return formatDate(year, month, day);
	}
	
	public static String calcDatetime(String datetime, int days) throws ParseException {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = f.parse(datetime);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, days);
		date = calendar.getTime();
		return f.format(date);
	}
	
	public static String getWeekDay(String DateStr) {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");

		Date date = null;
		int weekDay = 0;
		try {
			date = f.parse(DateStr);// 将String 转换为符合格式的日期
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			weekDay = calendar.get(Calendar.DAY_OF_WEEK);
			System.out.println(weekDay);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// System.out.println("日期:"+DateStr+" ： "+weekDay);
		String weekday1 = "";
		if (weekDay == 1)
			weekday1 = "周日";
		else if (weekDay == 2)
			weekday1 = "周一";
		else if (weekDay == 3)
			weekday1 = "周二";
		else if (weekDay == 4)
			weekday1 = "周三";
		else if (weekDay == 5)
			weekday1 = "周四";
		else if (weekDay == 6)
			weekday1 = "周五";
		else if (weekDay == 7)
			weekday1 = "周六";

		return weekday1;
	}
	
	public static String formatTime(int hourOfDay, int minute) {
		String time = "";
		if (hourOfDay < 10) {
			time += ("0" + hourOfDay);
		} else {
			time += hourOfDay;
		}
		if (minute < 10) {
			time += (":0" + minute);
		} else {
			time += (":" + minute);
		}
		return time;
	}
	
	public static String formatDate(int year, int month, int day) {
		String date = year + "-";
		if (month < 10) {
			date += ("0" + month);
		} else {
			date += month;
		}
		if (day < 10) {
			date += ("-0" + day);
		} else {
			date += ("-" + day);
		}
		return date;
	}
	
	public static String getHeartMessageTimeByDateTime(String datetime) {
		int[] yearMonthDayHourAndMinute = getYearMonthDayHourMinuteAndSecondByDateTime(datetime);
		StringBuilder sb = new StringBuilder();
		sb.append(yearMonthDayHourAndMinute[0]+"年");
		sb.append(yearMonthDayHourAndMinute[1]>=10?yearMonthDayHourAndMinute[1]+"月":"0"+yearMonthDayHourAndMinute[1]+"月");
		sb.append(yearMonthDayHourAndMinute[2]>=10?yearMonthDayHourAndMinute[2]+"日":"0"+yearMonthDayHourAndMinute[2]+"日");
		sb.append(yearMonthDayHourAndMinute[3]>=10?yearMonthDayHourAndMinute[3]+":":"0"+yearMonthDayHourAndMinute[3]+":");
		sb.append(yearMonthDayHourAndMinute[4]>=10?yearMonthDayHourAndMinute[4]+":":"0"+yearMonthDayHourAndMinute[4]+":");
		sb.append(yearMonthDayHourAndMinute[5]>=10?yearMonthDayHourAndMinute[5]:"0"+yearMonthDayHourAndMinute[5]);
		return sb.toString();
	}
	
	public static int[] getYearMonthDayHourMinuteAndSecondByDateTime(String datetime) {
		int[] yearMonthDayHourAndMinute = new int[6];
		String[] spliteOnce = datetime.split(" ", 2);
		String[] spliteDate = spliteOnce[0].split("\\-");
		String[] spliteTime = spliteOnce[1].split("\\:");
		yearMonthDayHourAndMinute[0] = Integer.parseInt(spliteDate[0]);
		yearMonthDayHourAndMinute[1] = Integer.parseInt(spliteDate[1]);
		yearMonthDayHourAndMinute[2] = Integer.parseInt(spliteDate[2]);
		yearMonthDayHourAndMinute[3] = Integer.parseInt(spliteTime[0]);
		yearMonthDayHourAndMinute[4] = Integer.parseInt(spliteTime[1]);
		yearMonthDayHourAndMinute[5] = Integer.parseInt(spliteTime[2]);
		return yearMonthDayHourAndMinute;
	}
	
	public static int[] getYearMonthDayHourAndMinuteByDateTime(String datetime) {
		int[] yearMonthDayHourAndMinute = new int[5];
		String[] spliteOnce = datetime.split(" ", 2);
		String[] spliteDate = spliteOnce[0].split("\\-");
		String[] spliteTime = spliteOnce[1].split("\\:");
		yearMonthDayHourAndMinute[0] = Integer.parseInt(spliteDate[0]);
		yearMonthDayHourAndMinute[1] = Integer.parseInt(spliteDate[1]);
		yearMonthDayHourAndMinute[2] = Integer.parseInt(spliteDate[2]);
		yearMonthDayHourAndMinute[3] = Integer.parseInt(spliteTime[0]);
		yearMonthDayHourAndMinute[4] = Integer.parseInt(spliteTime[1]);
		return yearMonthDayHourAndMinute;
	}
	
	public static int[] getYearMonthAndDayByDateTime(String datetime) {
		String[] split = datetime.split(" ", 2)[0].split("\\-");
		int[] yearMonthAndDay = new int[3];
		yearMonthAndDay[0] = Integer.parseInt(split[0]);
		yearMonthAndDay[1] = Integer.parseInt(split[1]);
		yearMonthAndDay[2] = Integer.parseInt(split[2]);
		return yearMonthAndDay;
	}
	
	public static int[] getHourAndMinuteByDateTime(String datetime) {
		String[] split = datetime.split(" ", 2)[1].split("\\:");
		int[] hourAndMinute = new int[2];
		hourAndMinute[0] = Integer.parseInt(split[0]);
		hourAndMinute[1] = Integer.parseInt(split[1]);
		return hourAndMinute;
	}
	
	public static int[] getNowHourAndMinute() {
		int[] nowHourAndMinute = new int[2];
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		nowHourAndMinute[0] = calendar.get(Calendar.HOUR_OF_DAY);
		nowHourAndMinute[1] = calendar.get(Calendar.MINUTE);
		return nowHourAndMinute;
	}

	public static String now() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date now = new Date(System.currentTimeMillis());
		return format.format(now);
	}
	
	public static String nowDetail() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date(System.currentTimeMillis());
		return format.format(now);
	}

	public static String getDateByRepeatDays(int days) {
		String now = "至";
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, days - 1);
		int year = calendar.get(Calendar.YEAR);
		now += (year + "年");
		int month = calendar.get(Calendar.MONTH) + 1;
		now += (month + "月");
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		now += (day + "日");
		return now;
	}
	
	public static int[] getNowDate(){
		int[] nowYearMonthDay = new int[3];
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		nowYearMonthDay[0] = calendar.get(Calendar.YEAR);
		nowYearMonthDay[1] = calendar.get(Calendar.MONTH)+1;
		nowYearMonthDay[2] = calendar.get(Calendar.DAY_OF_MONTH);
		return nowYearMonthDay;
	}
	
	public static int[] getTomorrowDate(){
		int[] tomorrowYearMonthDay = new int[3];
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		tomorrowYearMonthDay[0] = calendar.get(Calendar.YEAR);
		tomorrowYearMonthDay[1] = calendar.get(Calendar.MONTH)+1;
		tomorrowYearMonthDay[2] = calendar.get(Calendar.DAY_OF_MONTH);
		return tomorrowYearMonthDay;
	}
	
	public static int getDifferDays(int year, int month, int days)
	{

		String datetime = formatDate(year, month, days);
		
		int differ = 0;
		try {
			differ = DateUtils.getDifferDays(datetime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return differ;
	}

	public static int[] getDayInfoByRepeatDays(int days) {
		int[] dayInfo = new int[3];

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, days - 1);
		int year = calendar.get(Calendar.YEAR);
		dayInfo[0] = year;
		int month = calendar.get(Calendar.MONTH);
		dayInfo[1] = month;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		dayInfo[2] = day;

		return dayInfo;
	}

	public static String getDayByRepeatDays(int days) {
		return "共" + days + "天";
	}

	public static int getDifferDays(String endDateStr) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date endDate = sdf.parse(endDateStr);
		GregorianCalendar start = new GregorianCalendar();
		GregorianCalendar end = new GregorianCalendar();
		start.setTime(new Date());
		end.setTime(endDate);
		return getDays(start, end);
	}

	private static int getDays(GregorianCalendar g1, GregorianCalendar g2) {
		int elapsed = 0;
		boolean flag = true;
		GregorianCalendar gc1, gc2;

		g1.clear(Calendar.MILLISECOND);
		g1.clear(Calendar.SECOND);
		g1.clear(Calendar.MINUTE);
		g1.clear(Calendar.HOUR_OF_DAY);
		g2.clear(Calendar.MILLISECOND);
		g2.clear(Calendar.SECOND);
		g2.clear(Calendar.MINUTE);
		g2.clear(Calendar.HOUR_OF_DAY);

		if (g2.after(g1)) {
			gc2 = (GregorianCalendar) g2.clone();
			gc1 = (GregorianCalendar) g1.clone();
		} else {
			flag = false;
			gc2 = (GregorianCalendar) g1.clone();
			gc1 = (GregorianCalendar) g2.clone();
		}
		while (gc1.before(gc2)) {
			gc1.add(Calendar.DATE, 1);
			elapsed++;
		}
		return flag ? elapsed : (-elapsed);
	}
	
	public static String getTomorrowWidgetTimeDetail(int days) {
		String time = "";
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, days);
		int year = calendar.get(Calendar.YEAR);
		time += (year + "/");
		int month = calendar.get(Calendar.MONTH) + 1;
		time += (month + "/");
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		time += (day + "");
		int weekday = calendar.get(Calendar.DAY_OF_WEEK);
		switch (weekday) {
		case Calendar.MONDAY:
			time += ",周一";
			break;
		case Calendar.TUESDAY:
			time += ",周二";
			break;
		case Calendar.WEDNESDAY:
			time += ",周三";
			break;
		case Calendar.THURSDAY:
			time += ",周四";
			break;
		case Calendar.FRIDAY:
			time += ",周五";
			break;
		case Calendar.SATURDAY:
			time += ",周六";
			break;
		case Calendar.SUNDAY:
			time += ",周日";
			break;
		}
		return time;
	}

	public static String timeDetail(String time) {
		String now = "";
		Calendar calendar = Calendar.getInstance();
		if (time.equals(TOMORROW)) {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		} else if (time.equals(YESTERDAY)) {
			calendar.add(Calendar.DAY_OF_MONTH, -1);
		}
		int year = calendar.get(Calendar.YEAR);
		now += (year + "年");
		int month = calendar.get(Calendar.MONTH) + 1;
		now += (month + "月");
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		now += (day + "日");
		int weekday = calendar.get(Calendar.DAY_OF_WEEK);
		switch (weekday) {
		case Calendar.MONDAY:
			now += "星期一";
			break;
		case Calendar.TUESDAY:
			now += "星期二";
			break;
		case Calendar.WEDNESDAY:
			now += "星期三";
			break;
		case Calendar.THURSDAY:
			now += "星期四";
			break;
		case Calendar.FRIDAY:
			now += "星期五";
			break;
		case Calendar.SATURDAY:
			now += "星期六";
			break;
		case Calendar.SUNDAY:
			now += "星期日";
			break;
		}
		return now;
	}

	public static String getTaskTime(String datetime) {
		String[] splitStr = datetime.split(" ", 2);
		if (splitStr[1].equals(MIDDLE)) {
			return "中午12:00";
		} else {
			String[] time = splitStr[1].split(":");
			if (Integer.parseInt(time[0]) >= 12) {
				return "下午" + splitStr[1];
			} else {
				return "上午" + splitStr[1];
			}
		}
	}
}
