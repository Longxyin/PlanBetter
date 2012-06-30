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

	private static int DATABASE_VERSION = 1;  //���ݿ�汾
	
	private static String DATABASE_NAME = "planbetter"; //���ݿ�����
	
	//���������
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
	
	//ɾ�������
	private static String DROP_TASK_TABLE_SQL = "DROP TABLE IF EXISTS " + TaskBean.TABLE_NAME;
	
	//���������ܽ��
	private static String CREATE_SUMMARY_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + SummaryBean.TABLE_NAME + "("
		+ SummaryBean.ID + " INTEGER PRIMARY KEY,"
		+ SummaryBean.DATE + " TEXT,"
		+ SummaryBean.SCORE_RANK + " INTEGER,"
		+ SummaryBean.MOOD + " TEXT)";
	
	//ɾ�������ܽ��
	private static String DROP_SUMMARY_TABLE_SQL = "DROP TABLE IF EXISTS " + SummaryBean.TABLE_NAME;
	
	//����Ŀ���
	private static String CREATE_GOAL_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + GoalBean.TABLE_NAME + "("
		+ GoalBean.ID + " INTEGER PRIMARY KEY,"
		+ GoalBean.DATE + " TEXT,"
		+ GoalBean.GOAL_CONTENT + " TEXT,"
		+ GoalBean.GOAL_FLAG + " INTEGER)";
	
	//ɾ��Ŀ���
	private static String DROP_GOAL_TABLE_SQL = "DROP TABLE IF EXISTS " + GoalBean.TABLE_NAME;
	
	//���������
	private static String CREATE_HEART_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + HeartMessage.TABLE_NAME + "("
		+ HeartMessage.ID + " INTEGER PRIMARY KEY,"
		+ HeartMessage.DATE + " TEXT,"
		+ HeartMessage.HEART_CONTENT + " TEXT)";
	
	//ɾ�������
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
		db.execSQL(CREATE_TASK_TABLE_SQL); //���������
		db.execSQL(CREATE_GOAL_TABLE_SQL); //����Ŀ���
		db.execSQL(CREATE_HEART_TABLE_SQL); //���������
		db.execSQL(CREATE_SUMMARY_TABLE_SQL); //���������ܽ��
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL(DROP_TASK_TABLE_SQL); //ɾ�������
		db.execSQL(DROP_SUMMARY_TABLE_SQL); //ɾ�������ܽ��
		db.execSQL(DROP_GOAL_TABLE_SQL); //ɾ��Ŀ���
		db.execSQL(DROP_HEART_TABLE_SQL); //ɾ�������
		onCreate(db);
	}

}
