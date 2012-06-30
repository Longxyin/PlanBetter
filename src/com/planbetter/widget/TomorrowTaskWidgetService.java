package com.planbetter.widget;

import java.text.ParseException;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.planbetter.activity.R;
import com.planbetter.activity.TaskActivity;
import com.planbetter.bean.TaskBean;
import com.planbetter.constant.TaskConstant;
import com.planbetter.dao.DatabaseUtil;
import com.planbetter.date.DateUtils;

public class TomorrowTaskWidgetService extends Service {
	
	private Cursor tomorrowTaskCur;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		
		AppWidgetManager manager = AppWidgetManager.getInstance(this);
		try {
			manager.updateAppWidget(new ComponentName(this, TomorrowTaskWidget.class), getRemoteView());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		stopSelf(startId);
	}
	
	private RemoteViews getRemoteView() throws ParseException {
		
		String datetime = "";
		String task = "";
		String date = "";
		String days = "";
		
		RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.tomorrow_task_widget_layout);
		views.setOnClickPendingIntent(R.id.tomorrow_widget_layout_inside, PendingIntent.getActivity(this, 0, new Intent(this,TaskActivity.class), 0));
		
		//获取倒计时活动
		tomorrowTaskCur = DatabaseUtil.query(this,
				TaskBean.TABLE_NAME, new String[]{TaskBean.DATETIME, TaskBean.TASK_NAME}, TaskBean.IF_FUTURE + "="+
				TaskConstant.IS_FUTURE+" AND "+TaskBean.PARENT+"="+TaskConstant.IS_PARENT, null, null, null, TaskBean.ID
						+ " ASC");
		tomorrowTaskCur.moveToFirst();
		if(!tomorrowTaskCur.isAfterLast()) {
			datetime = tomorrowTaskCur.getString(0);
			int day = DateUtils.getDifferDays(datetime.split(" ")[0]); 
			days = day+"";
			date = DateUtils.getTomorrowWidgetTimeDetail(day);
			task = tomorrowTaskCur.getString(1);
		}
		DatabaseUtil.closeDatabase();
		
		views.setTextViewText(R.id.tomorrow_widget_title, datetime.equals("")?"":"还有");
		views.setTextViewText(R.id.tomorrow_widget_days, days);
		views.setTextViewText(R.id.tomorrow_widget_date, date);
		views.setTextViewText(R.id.tomorrow_widget_event, task);
		return views;
	}

}
