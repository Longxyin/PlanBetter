package com.planbetter.alarm;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.planbetter.activity.R;
import com.planbetter.activity.TaskActivity;
import com.planbetter.bean.TaskBean;
import com.planbetter.date.DateUtils;
import com.planbetter.receiver.PlanBetterReceiver;

public class Alarm {

	public static final String HOUR = "alarm_hour";
	public static final String MINUTE = "alarm_minute";
	public static final String ID = "alarm_id";
	public static final String DATETIME = "alarm_datetime";
	public static final String ALARM_REGISTRATION_DETAIL_ACTION = "com.planbetter.activity.ALARM_REGISTRATION_DETAIL";
	public static final String ALARM_REGISTRATION_SIMPLE_ACTION = "com.planbetter.activity.ALARM_REGISTRATION_SIMPLE";
	public static final String ALARM_CANCEL_ACTION = "com.planbetter.activity.ALARM_CANCEL";
	public static final String ALARM_REGISTRATION_BUNDLE_TAG = "alarm_registration";
	public static final String ALARM_CANCEL_BUNDLE_TAG = "alarm_cancel";
	
	public static final String ALARM_STATE_ICON_STORAGE = "alarm_state_icon";

	public static void enableAlarm(Context context, Intent intent, String action) {
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		if (action.equals(ALARM_REGISTRATION_DETAIL_ACTION)) {
			Bundle bundle = intent
					.getBundleExtra(ALARM_REGISTRATION_BUNDLE_TAG);
			Intent alarmIntent = new Intent(
					PlanBetterReceiver.ALARM_ALERT_ACTION);
			alarmIntent.putExtra(TaskBean.ID, bundle.getInt(Alarm.ID));
			Calendar alarmCalendar = Calendar.getInstance();
			alarmCalendar.setTimeInMillis(System.currentTimeMillis());
			alarmCalendar.set(Calendar.HOUR_OF_DAY, bundle.getInt(Alarm.HOUR));
			alarmCalendar.set(Calendar.MINUTE, bundle.getInt(Alarm.MINUTE));
			alarmCalendar.set(Calendar.SECOND, 0);
			alarmCalendar.set(Calendar.MILLISECOND, 0);
			PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
					context, bundle.getInt(Alarm.ID), alarmIntent, 0);
			alarmManager.set(AlarmManager.RTC_WAKEUP,
					alarmCalendar.getTimeInMillis(), alarmPendingIntent);
		} else if (action.equals(ALARM_REGISTRATION_SIMPLE_ACTION)) {
			Bundle bundle = intent
					.getBundleExtra(ALARM_REGISTRATION_BUNDLE_TAG);
			Intent alarmIntent = new Intent(
					PlanBetterReceiver.ALARM_ALERT_ACTION);
			alarmIntent.putExtra(TaskBean.ID, bundle.getInt(Alarm.ID));
			int[] val = DateUtils.getYearMonthDayHourAndMinuteByDateTime(bundle
					.getString(Alarm.DATETIME));
			Calendar alarmCalendar = Calendar.getInstance();
			alarmCalendar.setTimeInMillis(System.currentTimeMillis());
			alarmCalendar.set(Calendar.YEAR, val[0]);
			alarmCalendar.set(Calendar.MONTH, val[1] - 1);
			alarmCalendar.set(Calendar.DAY_OF_MONTH, val[2]);
			alarmCalendar.set(Calendar.HOUR_OF_DAY, val[3]);
			alarmCalendar.set(Calendar.MINUTE, val[4]);
			alarmCalendar.set(Calendar.SECOND, 0);
			alarmCalendar.set(Calendar.MILLISECOND, 0);
			PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
					context, bundle.getInt(Alarm.ID), alarmIntent, 0);
			alarmManager.set(AlarmManager.RTC_WAKEUP,
					alarmCalendar.getTimeInMillis(), alarmPendingIntent);
			
		}
		increaseAlarmIconSize(context);
		setStatusBarIcon(context,true);
	}
	
	private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
	
	public static void resetAlarmIconSize(Context context) {
		Editor editor = getSharedPreferences(context).edit();
        editor.putInt(ALARM_STATE_ICON_STORAGE, 0);
        editor.commit();
	}
	
	private static void increaseAlarmIconSize(Context context) {
		int alarmSize = getSharedPreferences(context).getInt(ALARM_STATE_ICON_STORAGE, 0);
		alarmSize ++;
		Editor editor = getSharedPreferences(context).edit();
        editor.putInt(ALARM_STATE_ICON_STORAGE, alarmSize);
        editor.commit();
	}
	
	private static void decreaseAlarmIconSize(Context context) {
		int alarmSize = getSharedPreferences(context).getInt(ALARM_STATE_ICON_STORAGE, 0);
		alarmSize --;
		Editor editor = getSharedPreferences(context).edit();
        editor.putInt(ALARM_STATE_ICON_STORAGE, alarmSize);
        editor.commit();
	}
	
	private static boolean checkAlarmIconAvailable(Context context) {
		return (getSharedPreferences(context).getInt(ALARM_STATE_ICON_STORAGE, 0)>0);
	}
	
	private static void setAlarmIcon(Context context) {
		setStatusBarIcon(context, checkAlarmIconAvailable(context));
	}
	
	public static void setStatusBarIcon(Context context, boolean enabled) {
    	Notification n = new Notification();
    	Intent viewAlarm = new Intent(context, TaskActivity.class);
    	PendingIntent intent = PendingIntent.getActivity(context,0, viewAlarm, 0);
    	n.icon=R.drawable.ic_clock_alarm_selected;
		n.setLatestEventInfo(context, "",context.getString(R.string.alarm_notify_text),intent);
		n.flags |= Notification.FLAG_SHOW_LIGHTS| Notification.FLAG_ONGOING_EVENT;
		n.defaults |= Notification.DEFAULT_LIGHTS;

		NotificationManager nm =(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		if(enabled)
			nm.notify(1216, n);
		else
			nm.cancel(1216);
  }

	public static void disableAlarm(Context context, Intent intent) {
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		int taskId = intent.getIntExtra(Alarm.ID, 0);
		Log.d("debug","taskId = "+taskId);
		PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context,
				taskId, new Intent(
						PlanBetterReceiver.ALARM_ALERT_ACTION), 0);
		alarmManager.cancel(alarmPendingIntent);
		
		decreaseAlarmIconSize(context);
		setAlarmIcon(context);
	}
}
