package com.planbetter.receiver;

import java.text.ParseException;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.planbetter.activity.TaskAlertActivity;
import com.planbetter.bean.TaskBean;
import com.planbetter.constant.TaskConstant;
import com.planbetter.dao.DatabaseUtil;
import com.planbetter.date.DateUtils;
import com.planbetter.service.PlanBetterInit;

public class PlanBetterReceiver extends BroadcastReceiver {

	public static final String ALARM_ALERT_ACTION = "com.planbetter.activity.ALARM_ALERT";
	public static final String INSERT_DATA_ACTION = "com.planbetter.activity.INSERT_DATA";
	public static final String DATE_CHANGED = "android.intent.action.DATE_CHANGED";
	public static final String BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";
	public static final String TIME_SET_CHANGED = "android.intent.action.TIME_SET";
	public static final String INSERT_DATA_AFTER_MODIFY = "com.planbetter.activity.INSERT_DATA_AFTER_MODIFY";
	public static final String INSERT_DATA_INTENT_BUNDLE_TAG = "insert_data";
	public static final String INSERT_DATA_AFTER_MODIFY_INTENT_BUNDLE_TAG = "insert_data_after_modify";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action != null) {
			if (action.equals(ALARM_ALERT_ACTION)) {
				Intent at_intent = new Intent(context, TaskAlertActivity.class);
				at_intent.putExtra(TaskBean.ID,
						intent.getIntExtra(TaskBean.ID, 1));
				at_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(at_intent);
			} else if (action.equals(INSERT_DATA_ACTION)) {
				Bundle bundle = intent
						.getBundleExtra(INSERT_DATA_INTENT_BUNDLE_TAG);
				int repeatDays = bundle.getInt(TaskBean.REPEAT_DAYS);
				String taskContentStr = bundle.getString(TaskBean.TASK_NAME);
				String taskPositionStr = bundle
						.getString(TaskBean.POSITION_NAME);
				int timeAlertValue = bundle.getInt(TaskBean.TIME_ALERT_FLAG);
				int priority = bundle.getInt(TaskBean.PRIORITY);
				String datetime = bundle.getString(TaskBean.DATETIME);
				int id = bundle.getInt(TaskBean.PARENT);
				// 循环插入剩余周期活动
				for (int n = 1; n < repeatDays; n++) {
					// 计算时间
					try {
						ContentValues values = new ContentValues();

						values.put(TaskBean.TASK_NAME, taskContentStr);
						values.put(TaskBean.IF_COMPLETE,
								TaskConstant.TASK_NOT_COMPLETE);
						values.put(TaskBean.POSITION_NAME, taskPositionStr);
						values.put(TaskBean.TIME_ALERT_FLAG, timeAlertValue);
						values.put(TaskBean.PRIORITY, priority);

						values.put(TaskBean.IF_FUTURE, TaskConstant.IS_FUTURE);

						values.put(TaskBean.DATETIME,
								DateUtils.calcDatetime(datetime, n));

						values.put(TaskBean.REPEAT_DAYS, repeatDays - n);

						values.put(TaskBean.PARENT, id);

						DatabaseUtil.insert(context, TaskBean.TABLE_NAME,
								TaskBean.ID, values);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else if (action.equals(DATE_CHANGED)
					|| action.equals(BOOT_COMPLETE) || action.equals(TIME_SET_CHANGED)) {
				// 日期发生变化
				// 启动初始化服务,刷新widget
				Log.d("debug", "启动初始化服务");
				Intent init = new Intent(context, PlanBetterInit.class);
				context.startService(init);
			} else if (action.equals(INSERT_DATA_AFTER_MODIFY)) {
				Bundle bundle = intent
						.getBundleExtra(INSERT_DATA_AFTER_MODIFY_INTENT_BUNDLE_TAG);
				int repeatDays = bundle.getInt(TaskBean.REPEAT_DAYS);
				String taskContentStr = bundle.getString(TaskBean.TASK_NAME);
				String taskPositionStr = bundle
						.getString(TaskBean.POSITION_NAME);
				int timeAlertValue = bundle.getInt(TaskBean.TIME_ALERT_FLAG);
				int priority = bundle.getInt(TaskBean.PRIORITY);
				String datetime = bundle.getString(TaskBean.DATETIME);
				int parent = bundle.getInt(TaskBean.PARENT);
				int curViewId = bundle.getInt(TaskBean.CURVIEWID);
				// 插入新活动
				for (int n = 1; n < repeatDays; n++) {
					// 计算时间
					try {

						ContentValues tmpValues = new ContentValues();

						tmpValues.put(TaskBean.TASK_NAME, taskContentStr);
						tmpValues.put(TaskBean.IF_COMPLETE,
								TaskConstant.TASK_NOT_COMPLETE);
						tmpValues.put(TaskBean.POSITION_NAME, taskPositionStr);
						tmpValues.put(TaskBean.TIME_ALERT_FLAG, timeAlertValue);
						tmpValues.put(TaskBean.PRIORITY, priority);
						tmpValues.put(TaskBean.IF_FUTURE, TaskConstant.IS_FUTURE);

						tmpValues.put(TaskBean.DATETIME,
								DateUtils.calcDatetime(datetime, n));
						tmpValues.put(TaskBean.REPEAT_DAYS, repeatDays - n);
						if (parent == TaskConstant.IS_PARENT) {
							tmpValues.put(TaskBean.PARENT, curViewId);
						} else {
							tmpValues.put(TaskBean.PARENT, parent);
						}

						DatabaseUtil.insert(context,
								TaskBean.TABLE_NAME, TaskBean.ID, tmpValues);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

}
