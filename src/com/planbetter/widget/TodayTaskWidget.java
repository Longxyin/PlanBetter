package com.planbetter.widget;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.View;
import android.widget.RemoteViews;

import com.planbetter.activity.R;
import com.planbetter.activity.TaskActivity;
import com.planbetter.bean.TaskBean;
import com.planbetter.dao.DatabaseUtil;
import com.planbetter.date.DateUtils;

/** Mister Widget appears on your home screen to provide helpful tips. */
public class TodayTaskWidget extends AppWidgetProvider {
    public static final String ACTION_NEXT_TIP = "com.android.misterwidget.NEXT_TIP";
    public static final String ACTION_POKE = "com.android.misterwidget.HEE_HEE";

    public static final String EXTRA_TIMES = "times";

    public static final String PREFS_NAME = "Protips";
    public static final String PREFS_TIP_NUMBER = "widget_tip";

    private static final Pattern sNewlineRegex = Pattern.compile(" *\\n *");
//    private static final Pattern sDrawableRegex = Pattern.compile(" *@(drawable/[a-z0-9_]+) *");

    // initial appearance: eyes closed, no bubble
    private int mIconRes = R.drawable.droidman_open;
    private int mMessage = 0;
    
    private AppWidgetManager mWidgetManager = null;
    private int[] mWidgetIds;
    private Context mContext;

    private CharSequence[] mTips;
    private Cursor todayTaskCur;
    
    private void getTaskArray(Context context) {
    	int size = 0;
    	List<String> taskInfo = new ArrayList<String>();
    	//查询今天任务
    	// 查询数据库,获取今天任务信息
		todayTaskCur = DatabaseUtil.query(context,
				TaskBean.TABLE_NAME, new String[]{TaskBean.DATETIME, TaskBean.POSITION_NAME, TaskBean.TASK_NAME}, TaskBean.DATETIME + " LIKE ?",
				new String[] { DateUtils.now() + "%" }, null, null, TaskBean.ID
						+ " ASC");

		// 遍历今天的任务信息
		for (todayTaskCur.moveToFirst(); !todayTaskCur.isAfterLast(); todayTaskCur
				.moveToNext()) {
			String taskTime = DateUtils.getTaskTime(todayTaskCur.getString(0));
			String positionName = todayTaskCur.getString(1);
			String taskName = "活动名称:"+todayTaskCur.getString(2);
			String temp = taskTime + " " + (positionName.equals("")?"地点未知":"在"+positionName) + "\n" + taskName;
			taskInfo.add(size, temp);
			size ++;
		}
		DatabaseUtil.closeDatabase();
		if(size == 0) {
			mTips = new CharSequence[1];
			mTips[0] = "今天您还没有添加任何活动";
		} else {
			mTips = new CharSequence[size];
			for(int n=0; n<size; n++) {
				mTips[n] = taskInfo.get(n);
			}
		}
    }

    private void setup(Context context) {
        mContext = context;
        mWidgetManager = AppWidgetManager.getInstance(context);
        mWidgetIds = mWidgetManager.getAppWidgetIds(new ComponentName(context, TodayTaskWidget.class));

        SharedPreferences pref = context.getSharedPreferences(PREFS_NAME, 0);
        mMessage = pref.getInt(PREFS_TIP_NUMBER, 0);

        getTaskArray(context);

        if (mTips != null) {
            if (mMessage >= mTips.length) mMessage = 0;
        } else {
            mMessage = -1;
        }

    }

    public void goodmorning() {
        mMessage = -1;
        try {
            setIcon(R.drawable.droidman_down_closed);
            Thread.sleep(500);
            setIcon(R.drawable.droidman_down_open);
            Thread.sleep(200);
            setIcon(R.drawable.droidman_down_closed);
            Thread.sleep(100);
            setIcon(R.drawable.droidman_down_open);
            Thread.sleep(600);
        } catch (InterruptedException ex) {
        }
        mMessage = 0;
        mIconRes = R.drawable.droidman_open;
        refresh();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        setup(context);

        if (intent.getAction().equals(ACTION_NEXT_TIP)) {
            mMessage = getNextMessageIndex();
            SharedPreferences.Editor pref = context.getSharedPreferences(PREFS_NAME, 0).edit();
            pref.putInt(PREFS_TIP_NUMBER, mMessage);
            pref.commit();
            refresh();
        } else if (intent.getAction().equals(ACTION_POKE)) {
            blink(intent.getIntExtra(EXTRA_TIMES, 1));
            Intent tt_intent = new Intent(context, TaskActivity.class);
            tt_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(tt_intent);
        } else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {
            goodmorning();
        } else {
            mIconRes = R.drawable.droidman_open;
            refresh();
        }
    }

    private void refresh() {
        RemoteViews rv = buildUpdate(mContext);
        for (int i : mWidgetIds) {
            mWidgetManager.updateAppWidget(i, rv);
        }
    }

    private void setIcon(int resId) {
        mIconRes = resId;
        refresh();
    }

    private int getNextMessageIndex() {
        return (mMessage + 1) % mTips.length;
    }

    private void blink(int blinks) {
        // don't blink if no bubble showing or if goodmorning() is happening
        if (mMessage < 0) return;

        setIcon(R.drawable.droidman_closed);
        try {
            Thread.sleep(100);
            while (0<--blinks) {
                setIcon(R.drawable.droidman_open);
                Thread.sleep(200);
                setIcon(R.drawable.droidman_closed);
                Thread.sleep(100);
            }
        } catch (InterruptedException ex) { }
        setIcon(R.drawable.droidman_open);
    }

    public RemoteViews buildUpdate(Context context) {
        RemoteViews updateViews = new RemoteViews(
            context.getPackageName(), R.layout.today_task_widget_layout);

        // Action for tap on bubble
        Intent bcast = new Intent(context, TodayTaskWidget.class);
        bcast.setAction(ACTION_NEXT_TIP);
        PendingIntent pending = PendingIntent.getBroadcast(
            context, 0, bcast, PendingIntent.FLAG_UPDATE_CURRENT);
        updateViews.setOnClickPendingIntent(R.id.tip_bubble, pending);

        // Action for tap on android
        bcast = new Intent(context, TodayTaskWidget.class);
        bcast.setAction(ACTION_POKE);
        bcast.putExtra(EXTRA_TIMES, 1);
        pending = PendingIntent.getBroadcast(
            context, 0, bcast, PendingIntent.FLAG_UPDATE_CURRENT);
        updateViews.setOnClickPendingIntent(R.id.bugdroid, pending);

        // Tip bubble text
        if (mMessage >= 0) {
            String[] parts = sNewlineRegex.split(mTips[mMessage], 2);
            String title = parts[0];
            String text = parts.length > 1 ? parts[1] : "";

            // Look for a callout graphic referenced in the text
//            Matcher m = sDrawableRegex.matcher(text);
//            if (m.find()) {
//                String imageName = m.group(1);
//                int resId = context.getResources().getIdentifier(
//
//                    imageName, null, context.getPackageName());
//                updateViews.setImageViewResource(R.id.tip_callout, R.drawable.icon);
//                updateViews.setViewVisibility(R.id.tip_callout, View.VISIBLE);
//                text = m.replaceFirst("");
//            } else {
//                updateViews.setImageViewResource(R.id.tip_callout, R.drawable.icon);
//                updateViews.setViewVisibility(R.id.tip_callout, View.GONE);
//            }

            updateViews.setTextViewText(R.id.tip_message, 
                text);
            updateViews.setTextViewText(R.id.tip_header,
                title);
            updateViews.setTextViewText(R.id.tip_footer, 
                context.getResources().getString(
                    R.string.pager_footer,
                    (1+mMessage), mTips.length));
            updateViews.setViewVisibility(R.id.tip_bubble, View.VISIBLE);
        } else {
            updateViews.setViewVisibility(R.id.tip_bubble, View.INVISIBLE);
        }

        updateViews.setImageViewResource(R.id.bugdroid, mIconRes);

        return updateViews;
    }
}
