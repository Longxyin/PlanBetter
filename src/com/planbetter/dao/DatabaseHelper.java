package com.planbetter.dao;

import com.planbetter.bean.GoalBean;
import com.planbetter.bean.HeartMessage;
import com.planbetter.bean.SummaryBean;
import com.planbetter.bean.TaskBean;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static int DATABASE_VERSION = 1;  //数据库版本
	
	private static String DATABASE_NAME = "planbetter"; //数据库名称
	
	//创建任务表
	private static String CREATE_TASK_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TaskBean.TABLE_NAME + "("
		+ TaskBean.ID + " INTEGER PRIMARY KEY,"
		+ TaskBean.DATETIME + " TEXT,"
		+ TaskBean.TASK_NAME + " TEXT,"
		+ TaskBean.POSITION_NAME + " TEXT,"
		+ TaskBean.TIME_ALERT_FLAG + " INTEGER,"
		+ TaskBean.PRIORITY + " INTEGER,"
		+ TaskBean.IF_COMPLETE + " INTEGER,"
		+ TaskBean.REPEAT_DAYS + " INTEGER,"
		+ TaskBean.IF_FUTURE+ " INTEGER,"
		+ TaskBean.PARENT +" INTEGER)";
	
	//删除任务表
	private static String DROP_TASK_TABLE_SQL = "DROP TABLE IF EXISTS " + TaskBean.TABLE_NAME;
	
	//创建任务总结表
	private static String CREATE_SUMMARY_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + SummaryBean.TABLE_NAME + "("
		+ SummaryBean.ID + " INTEGER PRIMARY KEY,"
		+ SummaryBean.DATE + " TEXT,"
		+ SummaryBean.SCORE_RANK + " INTEGER,"
		+ SummaryBean.MOOD + " TEXT)";
	
	//删除任务总结表
	private static String DROP_SUMMARY_TABLE_SQL = "DROP TABLE IF EXISTS " + SummaryBean.TABLE_NAME;
	
	//创建目标表
	private static String CREATE_GOAL_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + GoalBean.TABLE_NAME + "("
		+ GoalBean.ID + " INTEGER PRIMARY KEY,"
		+ GoalBean.DATE + " TEXT,"
		+ GoalBean.GOAL_CONTENT + " TEXT,"
		+ GoalBean.GOAL_FLAG + " INTEGER)";
	
	//删除目标表
	private static String DROP_GOAL_TABLE_SQL = "DROP TABLE IF EXISTS " + GoalBean.TABLE_NAME;
	
	//创建心语表
	private static String CREATE_HEART_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + HeartMessage.TABLE_NAME + "("
		+ HeartMessage.ID + " INTEGER PRIMARY KEY,"
		+ HeartMessage.DATE + " TEXT,"
		+ HeartMessage.HEART_CONTENT + " TEXT)";
	
	//删除心语表
	private static String DROP_HEART_TABLE_SQL = "DROP TABLE IF EXISTS " + HeartMessage.TABLE_NAME;
	
	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	
	public DatabaseHelper(Context context) {
		this(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_TASK_TABLE_SQL); //创建任务表
		db.execSQL(CREATE_GOAL_TABLE_SQL); //创建目标表
		db.execSQL(CREATE_HEART_TABLE_SQL); //创建心语表
		db.execSQL(CREATE_SUMMARY_TABLE_SQL); //创建任务总结表
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL(DROP_TASK_TABLE_SQL); //删除任务表
		db.execSQL(DROP_SUMMARY_TABLE_SQL); //删除任务总结表
		db.execSQL(DROP_GOAL_TABLE_SQL); //删除目标表
		db.execSQL(DROP_HEART_TABLE_SQL); //删除心语表
		onCreate(db);
	}

}
