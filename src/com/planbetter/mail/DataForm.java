package com.planbetter.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;

import com.planbetter.bean.GoalBean;
import com.planbetter.bean.HeartMessage;
import com.planbetter.bean.TaskBean;
import com.planbetter.constant.GoalConstant;
import com.planbetter.constant.TaskConstant;
import com.planbetter.dao.DatabaseUtil;
import com.planbetter.date.DateUtils;

public class DataForm {
	private static List<Map<String, Object>>  ItemList;
	
	private static final int SEND_TODAY_TASK = 1;
	private static final int SEND_HISTORY_TASK = 2;
	private static final int SEND_FUTURE_TASK = 3;
	private static final int SEND_GOAL = 4;
	private static final int SEND_HEART_MESSAGE = 5;
	
	public static String getSubject(int flag)
	{
		if(flag == SEND_TODAY_TASK)
		{
			return "���͵�����������";
		}
		else if(flag == SEND_HISTORY_TASK)
		{
			return "������ʷ��������";
		}
		else if(flag == SEND_FUTURE_TASK)
		{
			return "���͵�����������";
		}
		else if(flag == SEND_GOAL)
			return "����Ŀ������";
		else if(flag == SEND_HEART_MESSAGE)
			return "������������";
		else 
			return "ò�Ƴ����ˡ���";
			
	}
	
	public static String getContent(Context context, int flag)
	{
		
		if(flag == SEND_TODAY_TASK)
		{
			initTodayTaskListItem(context);
			return getTaskMessage();
		}
		else if(flag == SEND_HISTORY_TASK)
		{
			initHistoryTaskListItem(context);
			return getTaskMessage();
		}
		else if(flag == SEND_FUTURE_TASK)
		{
			initFutureTaskListItem(context);
			return getTaskMessage();
		}
		else if(flag == SEND_GOAL)
			return getGoalMessage(context);
		else if(flag == SEND_HEART_MESSAGE)
			return getHeartMessage(context);
		else 
			return "ò�Ƴ����ˡ���";
			
	}
	
	private static void initTodayTaskListItem(Context context) {
		ItemList = new ArrayList<Map<String, Object>>();
		// ��ѯ���ݿ�,��ȡ����������Ϣ
		Cursor todayTaskCur = DatabaseUtil.query(context,
				TaskBean.TABLE_NAME, null, TaskBean.DATETIME + " LIKE ?",
				new String[] { DateUtils.now() + "%" }, null, null, TaskBean.ID
						+ " ASC");

		// ���������������Ϣ
		
		for (todayTaskCur.moveToFirst(); !todayTaskCur.isAfterLast(); todayTaskCur
				.moveToNext()) {
			Map<String, Object> map = TaskBean.generateTask(todayTaskCur);
			ItemList.add(map);
		}
		DatabaseUtil.closeDatabase();
	}
	
	private static void initHistoryTaskListItem(Context context) {
		ItemList = new ArrayList<Map<String, Object>>();
		// ��ѯ���ݿ�,��ȡ����������Ϣ
		Cursor historyTaskCur = DatabaseUtil.query(context,
				TaskBean.TABLE_NAME, null, TaskBean.DATETIME + " NOT LIKE ? AND "+TaskBean.IF_FUTURE+" = ?",
				new String[] { DateUtils.now() + "%", TaskConstant.NOT_FUTURE + "" }, null, null, TaskBean.ID
						+ " ASC");
		
		for (historyTaskCur.moveToFirst(); !historyTaskCur.isAfterLast(); historyTaskCur
				.moveToNext()) {
			Map<String, Object> map = TaskBean.generateTask(historyTaskCur);
			ItemList.add(map);
		}
		DatabaseUtil.closeDatabase();
	}
	
	private static void initFutureTaskListItem(Context context) {
		ItemList = new ArrayList<Map<String, Object>>();
		// ��ѯ���ݿ�,��ȡ����������Ϣ
		Cursor futureTaskCur = DatabaseUtil.query(context,
				TaskBean.TABLE_NAME, null, TaskBean.IF_FUTURE + " = ? AND "+TaskBean.PARENT+" = ?",
				new String[] { TaskConstant.IS_FUTURE+"",TaskConstant.IS_PARENT+"" }, null, null, TaskBean.ID
						+ " ASC");
		for (futureTaskCur.moveToFirst(); !futureTaskCur.isAfterLast(); futureTaskCur
				.moveToNext()) {
			Map<String, Object> map = TaskBean.generateTask(futureTaskCur);
			ItemList.add(map);
		}
		DatabaseUtil.closeDatabase();
	}
	
	private static String getTaskMessage()
	{
	    String content = "";
		for(int i=1; i<=ItemList.size(); i++)
		{
			Map<String, Object> map = ItemList.get(i-1);
			String taskname = (String) map.get(TaskBean.TASK_NAME);
			String time = (String) map.get(TaskBean.DATETIME);
			String position = (String)map.get(TaskBean.POSITION_NAME);
			int flag = Integer.parseInt(map.get(TaskBean.IF_COMPLETE).toString());
			
			String tmp = null;
			if(flag == TaskConstant.TASK_COMPLETE)
				tmp =  i + "�������ƣ�"+taskname+"\tʱ�䣺"+time+"\tλ�ã�"+(position.equals("")?"δ֪":position)+"\t�����\n";
			else if(flag == TaskConstant.TASK_NOT_COMPLETE)
				tmp = i + " �������ƣ�"+taskname+"\tʱ�䣺"+time+"\tλ�ã�"+(position.equals("")?"δ֪":position)+"\tδ���\n";
			
			content = content + tmp;
		}
		if(content.equals(""))
			content = "û������";
		return content;
	}
	
	private static String getHeartMessage(Context context)
	{
		List<HeartMessage> messages = new ArrayList<HeartMessage>();
		Cursor databaseCur = DatabaseUtil.query(context, HeartMessage.TABLE_NAME, 
				null, null, null, null, null, HeartMessage.ID+" ASC");
		for(databaseCur.moveToFirst(); !databaseCur.isAfterLast();databaseCur.moveToNext()) {
			HeartMessage hm = HeartMessage.generateHeartMessage(databaseCur);	
			messages.add(hm);
		}
		DatabaseUtil.closeDatabase();
		
		String content = "";
		for(int i=1; i<=messages.size(); i++)
		{
			HeartMessage map = messages.get(i-1);
			String time = (String) map.getHeartDate();
			String heartcontent = (String) map.getHeartContent();
	
			String tmp =  i + " ʱ�䣺"+time+ "\t�������ݣ�"+heartcontent+"\n";	
			content = content + tmp;
		}
		if(content.equals(""))
			content = "û������";
		return content;
	}
	
	
	private static String getGoalMessage(Context context)
	{
		ItemList = new ArrayList<Map<String, Object>>();
		String firstGoal = "";
		String secondGoal = "";
		String thirdGoal = "";
		
		Cursor goalCur = DatabaseUtil.query(context , GoalBean.TABLE_NAME, new String[]{GoalBean.GOAL_CONTENT,GoalBean.GOAL_FLAG}, 
				GoalBean.GOAL_FLAG+"!="+GoalConstant.FORMER_GOAL, null, null, null, GoalBean.GOAL_FLAG+" ASC");
		
		int x = 0;
		for(goalCur.moveToFirst();!goalCur.isAfterLast();goalCur.moveToNext()) {
			
			switch(goalCur.getInt(goalCur.getColumnIndex(GoalBean.GOAL_FLAG))) {
			case GoalConstant.RANK_FIRST:
				firstGoal = goalCur.getString(goalCur.getColumnIndex(GoalBean.GOAL_CONTENT));
				break;
			case GoalConstant.RANK_SECOND:
				secondGoal = goalCur.getString(goalCur.getColumnIndex(GoalBean.GOAL_CONTENT));
				break;
			case GoalConstant.RANK_THIRD:
				thirdGoal = goalCur.getString(goalCur.getColumnIndex(GoalBean.GOAL_CONTENT));
				break;
			case GoalConstant.FORMER_GOAL:
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(GoalBean.GOAL_CONTENT,
						goalCur.getString(goalCur.getColumnIndex(GoalBean.GOAL_CONTENT)));
				map.put(GoalBean.DATE, goalCur.getString(goalCur.getColumnIndex(GoalBean.DATE)));
				map.put(GoalBean.ID, goalCur.getInt(goalCur.getColumnIndex(GoalBean.ID)));
				map.put(GoalBean.GOAL_FLAG,
						goalCur.getInt(goalCur.getColumnIndex(GoalBean.GOAL_FLAG)));
				ItemList.add(x, map);
				x++;
			}
		}
		DatabaseUtil.closeDatabase();
		
		String content = "��һĿ�꣺"+(firstGoal.equals("")?"û������":firstGoal)+"\n�ڶ�Ŀ�꣺"
			+(secondGoal.equals("")?"û������":secondGoal)+"\n����Ŀ�꣺"+(thirdGoal.equals("")?"û������":thirdGoal)+"\n��ʷĿ�꣺";
		for(int i=1; i<=ItemList.size(); i++)
		{
			Map<String, Object> map = ItemList.get(i);
            String goalcontent = (String)map.get(GoalBean.GOAL_CONTENT);
            String date = (String)map.get(GoalBean.DATE);
	
			String tmp =  i + " ʱ�䣺"+date+ "\t"+goalcontent+"\n";	
			content = content + tmp;
		}
		if(ItemList.size() == 0)
			content += "û������";
		return content;
	}
}
