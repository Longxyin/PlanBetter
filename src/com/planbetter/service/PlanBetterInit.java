package com.planbetter.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.planbetter.alarm.Alarm;
import com.planbetter.bean.TaskBean;
import com.planbetter.constant.TaskConstant;
import com.planbetter.dao.DatabaseUtil;
import com.planbetter.date.DateUtils;
import com.planbetter.receiver.PlanBetterReceiver;
import com.planbetter.widget.TodayTaskWidget;
import com.planbetter.widget.TomorrowTaskWidgetService;

public class PlanBetterInit extends Service {
	
	private LocalBinder myBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		public PlanBetterInit getService() {
			return PlanBetterInit.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return myBinder;
	}	
	
	
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		//ˢ�����ݿ���Ϣ
		//�������ļ�¼����ѯ�Ƿ���Ҫ�޸�is_future�ֶ�
		//��ȡ��Ҫ�޸�is_future�ֶε�id��
		try {
			initTaskInfo();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			registerAlarm(this);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendBroadcastToTodayWidget();
		startServiceToFreshTomorrowWidget();
		//ע��
		setMyDateChangedAlarm();
		stopSelf(startId);
	}
	
	private void setMyDateChangedAlarm() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent intent = new Intent(PlanBetterReceiver.DATE_CHANGED);
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
	}

	private void startServiceToFreshTomorrowWidget() {
		Intent intent = new Intent(this, TomorrowTaskWidgetService.class);
		startService(intent);
	}
	
	private void sendBroadcastToTodayWidget() {
		Intent bcast = new Intent(this, TodayTaskWidget.class);
        bcast.setAction(TodayTaskWidget.ACTION_NEXT_TIP);
        sendBroadcast(bcast);
	}
	
	private void initTaskInfo() throws ParseException {
		List<Integer> taskId = getNeedModifyTaskId();
		Iterator<Integer> iterator = taskId.iterator();
		while(iterator.hasNext()) {
			int id = iterator.next();
			modifyTaskInfo(id);
		}
	}
	
	private void modifyTaskInfo(int taskId) {
		ContentValues values = new ContentValues();
		values.put(TaskBean.IF_FUTURE, TaskConstant.NOT_FUTURE);
		DatabaseUtil.update(this, TaskBean.TABLE_NAME, values, TaskBean.ID+"="+taskId, null);
	}

	private List<Integer> getNeedModifyTaskId() throws ParseException {
		Log.d("debug", "����getNeedModifyTaskId()����");
		List<Integer> taskId = new ArrayList<Integer>(); 
		Cursor cursor = DatabaseUtil.query(this, TaskBean.TABLE_NAME, new String[]{TaskBean.ID, TaskBean.DATETIME, TaskBean.IF_FUTURE}, 
				TaskBean.IF_FUTURE+"="+TaskConstant.IS_FUTURE, null, null, null, TaskBean.ID+" ASC");
		for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
			String datetime = cursor.getString(cursor.getColumnIndex(TaskBean.DATETIME));
			int ifFuture = cursor.getInt(cursor.getColumnIndex(TaskBean.IF_FUTURE));
			int id = cursor.getInt(cursor.getColumnIndex(TaskBean.ID));
			Log.d("debug","����idΪ"+id+" ifFuture="+ifFuture);
			//�Ƚ�����
			if(DateUtils.checkTimePassOrNot(datetime) && ifFuture == TaskConstant.IS_FUTURE) {
				Log.d("debug", "��Ҫ�޸ĵ�Id��"+id);
				//��Ҫ�޸�
				taskId.add(id);
			}
		}
		DatabaseUtil.closeDatabase();
		return taskId;
	}
	
	private void registerAlarm(Context context) throws ParseException {
		
		Alarm.resetAlarmIconSize(context);
		
		// ��ѯ���ݿ�,��ȡ����������Ϣ
		Cursor todayTaskCur = DatabaseUtil.query(context,
				TaskBean.TABLE_NAME, new String[]{TaskBean.ID, TaskBean.TIME_ALERT_FLAG, TaskBean.DATETIME}, TaskBean.DATETIME + " LIKE ?",
				new String[] { DateUtils.now() + "%" }, null, null, TaskBean.ID
						+ " ASC");

		// ���������������Ϣ
		
		for (todayTaskCur.moveToFirst(); !todayTaskCur.isAfterLast(); todayTaskCur
				.moveToNext()) {
			int taskId = todayTaskCur.getInt(0);
			int alarmTag = todayTaskCur.getInt(1);
			String datetime = todayTaskCur.getString(2);
			if(alarmTag == TaskConstant.TIME_ALERT) {
				if(DateUtils.checkTimeAlertable(datetime)) {
					registerAlarm(taskId, datetime);
				}
			}
		}
		DatabaseUtil.closeDatabase();
		
	}
	
	private void registerAlarm(int id, String datetime) throws ParseException {
		Intent intent = new Intent(Alarm.ALARM_REGISTRATION_SIMPLE_ACTION);
		Bundle bundle = new Bundle();
		bundle.putInt(Alarm.ID, id);
		bundle.putString(Alarm.DATETIME, datetime);
		intent.putExtra(Alarm.ALARM_REGISTRATION_BUNDLE_TAG, bundle);
		sendBroadcast(intent);
	}
}
