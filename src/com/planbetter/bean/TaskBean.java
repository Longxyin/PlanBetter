package com.planbetter.bean;

import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;

public class TaskBean {

	public static final String TABLE_NAME = "task";
	public static final String ID = "task_id";
	public static final String DATETIME = "task_datetime";
	public static final String TASK_NAME = "task_name";
	public static final String POSITION_NAME = "position_name";
	public static final String TIME_ALERT_FLAG = "time_alert";
	public static final String PRIORITY = "task_priority";
	public static final String IF_COMPLETE = "if_complete";
	public static final String REPEAT_DAYS = "repeat_days";
	public static final String IF_FUTURE = "if_future";
	public static final String PARENT = "parent";
	
	public static final String CURVIEWID = "curviewid";

	private int taskId;
	private String taskDateTime;
	private String taskName;
	private String positionName;
	private int timeAlertFlag;
	private int taskPriority;
	private int ifComplete;
	private int repeatDays;
	private int parent;
	private int ifFuture;
	
	public int getIfFuture() {
		return ifFuture;
	}

	public void setIfFuture(int ifFuture) {
		this.ifFuture = ifFuture;
	}

	public int getParent() {
		return parent;
	}

	public void setParent(int parent) {
		this.parent = parent;
	}

	public TaskBean() {
		
	}
	
	public static Map<String, Object> generateTask(Cursor cursor) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		int id = cursor.getInt(cursor.getColumnIndex(ID));
		map.put(ID, id+"");
		String taskName = cursor.getString(cursor.getColumnIndex(TASK_NAME));
		map.put(TASK_NAME, taskName);
		String datetime = cursor.getString(cursor.getColumnIndex(DATETIME));
		map.put(DATETIME, datetime);
		String positionName = cursor.getString(cursor.getColumnIndex(POSITION_NAME));
		map.put(POSITION_NAME, positionName);
		int priority = cursor.getInt(cursor.getColumnIndex(PRIORITY));
		map.put(PRIORITY, priority+"");
		int timeAlertFlag = cursor.getInt(cursor.getColumnIndex(TIME_ALERT_FLAG));
		map.put(TIME_ALERT_FLAG, timeAlertFlag+"");
		int ifComplete = cursor.getInt(cursor.getColumnIndex(IF_COMPLETE));
		map.put(IF_COMPLETE, ifComplete+"");
		int repeatDays = cursor.getInt(cursor.getColumnIndex(REPEAT_DAYS));
		map.put(REPEAT_DAYS, repeatDays+"");
		int parent = cursor.getInt(cursor.getColumnIndex(PARENT));
		map.put(PARENT, parent+"");
		int ifFuture = cursor.getInt(cursor.getColumnIndex(IF_FUTURE));
		map.put(IF_FUTURE, ifFuture+"");
		
		return map;
	}

	public TaskBean(int taskId, String taskDateTime, String taskName,
			String positionName, int timeAlertFlag,
			int taskPriority, int ifComplete, int repeatDays, int parent, int ifFuture) {
		this.taskId = taskId;
		this.taskDateTime = taskDateTime;
		this.taskName = taskName;
		this.positionName = positionName;
		this.timeAlertFlag = timeAlertFlag;
		this.taskPriority = taskPriority;
		this.ifComplete = ifComplete;
		this.repeatDays = repeatDays;
		this.parent = parent;
		this.ifFuture = ifFuture;
	}

	public int getRepeatDays() {
		return repeatDays;
	}

	public void setRepeatDays(int repeatDays) {
		this.repeatDays = repeatDays;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public String getTaskDateTime() {
		return taskDateTime;
	}

	public void setTaskDateTime(String taskDateTime) {
		this.taskDateTime = taskDateTime;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getPositionName() {
		return positionName;
	}

	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}

	public int getTimeAlertFlag() {
		return timeAlertFlag;
	}

	public void setTimeAlertFlag(int timeAlertFlag) {
		this.timeAlertFlag = timeAlertFlag;
	}

	public int getTaskPriority() {
		return taskPriority;
	}

	public void setTaskPriority(int taskPriority) {
		this.taskPriority = taskPriority;
	}

	public int getIfComplete() {
		return ifComplete;
	}

	public void setIfComplete(int ifComplete) {
		this.ifComplete = ifComplete;
	}

}
