package com.planbetter.bean;

import android.database.Cursor;

import com.planbetter.date.DateUtils;

public class HeartMessage {

	public static String TABLE_NAME = "heart";
	public static String ID = "heart_id";
	public static String DATE = "heart_date";
	public static String HEART_CONTENT = "heart_content";
	
	public static final int MESSAGE_FROM = 0;
	public static final int MESSAGE_TO = 1;

	private int heartId;
	private String heartDate;
	private int direction;

	public HeartMessage() {

	}
	
	public static HeartMessage generateHeartMessage(Cursor cursor) {
		HeartMessage hm = new HeartMessage();
		hm.setDirection(MESSAGE_FROM);
		hm.setHeartContent(cursor.getString(cursor.getColumnIndex(HEART_CONTENT)));
		hm.setHeartDate(cursor.getString(cursor.getColumnIndex(DATE)));
		hm.setHeartId(cursor.getInt(cursor.getColumnIndex(ID)));
		return hm;
	}

	public HeartMessage(int heartId, String heartDate, String heartContent, int direction) {
		this.heartId = heartId;
		this.heartDate = heartDate;
		this.heartContent = heartContent;
		this.direction = direction;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	private String heartContent;

	public int getHeartId() {
		return heartId;
	}

	public void setHeartId(int heartId) {
		this.heartId = heartId;
	}

	public String getHeartDate() {
		return heartDate;
	}

	public void setHeartDate(String heartDate) {
		this.heartDate = heartDate;
	}

	public String getHeartContent() {
		return heartContent;
	}

	public void setHeartContent(String heartContent) {
		this.heartContent = heartContent;
	}
	
	public String getContent() {
		return DateUtils.getHeartMessageTimeByDateTime(heartDate)+"\n"+heartContent;
	}
}
