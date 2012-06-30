package com.planbetter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.planbetter.alarm.Alarm;


public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if(action != null) {
			if(action.equals(Alarm.ALARM_REGISTRATION_DETAIL_ACTION)) {
				Alarm.enableAlarm(context, intent, action);
			} else if(action.equals(Alarm.ALARM_REGISTRATION_SIMPLE_ACTION)) {
				Alarm.enableAlarm(context, intent, action);
			} else if(action.equals(Alarm.ALARM_CANCEL_ACTION)) {
				Alarm.disableAlarm(context, intent);
			}
		}
	}

}
