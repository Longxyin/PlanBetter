package com.planbetter.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

import com.planbetter.bean.TaskBean;
import com.planbetter.constant.TaskConstant;
import com.planbetter.dao.DatabaseUtil;
import com.planbetter.date.DateUtils;

public class HistoryOnedayActivity extends Activity implements ViewFactory,
		OnItemSelectedListener {
	private View historyView = null; // ������ͼ
	private List<Map<String, Object>> historyTaskItemList = null; // List��������������Ϣ
	private ListView historyTaskList = null; // ����������Ϣ�б���ͼ
	private static HistoryTaskListViewAdapter historyAdapter = null; // �����б���ͼadapter
	private Cursor historyTaskCur;
	private TextView listEmptyView;
	private TextView historyDate;
	
	private static final int REFRESH_LAYOUT = 0;
	
	//ImageSwitcher mSwitcher;
	Resources r;
	int today;
	int tyear;
	int tmonth;
	int tday;

	private Integer[] HistoryOnebgIds = { R.drawable.h1, R.drawable.h2,
			R.drawable.h3, R.drawable.h4, R.drawable.h5, R.drawable.h6,
			R.drawable.h7 };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		historyView = LayoutInflater.from(HistoryOnedayActivity.this).inflate(
				R.layout.history_oneday, null);		
		setContentView(historyView);
		
		setTitle("ImageShowActivity");
		Intent intent = getIntent();		
		today = intent.getIntExtra("today", 0);
		tyear = today / 10000;
		tmonth = today / 100 - tyear * 100;
		tday = today % 100;
		initViews(today);
		
		Gallery g = (Gallery) findViewById(R.id.history_gallery);
		// Ϊ����ͼ�����ָ��һ��������
		g.setAdapter(new ImageAdapter(this));
		// ��Ӧ ������ͼ�б���ѡ��ĳ������ͼ��� �¼�
		g.setSelection(10);
		g.setOnItemSelectedListener(this);
		r = this.getResources();
		
	}
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.arg1) {
			case REFRESH_LAYOUT:
				refreshHistoryLayout(msg.arg2);
				break;
			}
		}
		
	};
	

	private void initViews(int date) {
		int year = date / 10000;
		int month = date / 100 - year * 100;
		int day = date % 100;
		String currentDate = DateUtils.formatDate(year, month, day);
		
		String dateStr = getWeekDayFormat(date);// �õ�Format��yyyymmdd���������
		int weekday = getIntWeekDay(dateStr);// �õ��ܼ�������
		String WeekDay = getStringWeekDay(weekday);// �õ��ܼ����ַ���
		String dateString = getStringday(date) + " " + WeekDay;
		
		/*�õ�һ�������պ����ڵ�ʱ��*/
		historyDate = (TextView) historyView
		.findViewById(R.id.textView_history_date);
		historyDate.setText(dateString);
		
		// ��ʼ��������ͼ
		historyTaskList = (ListView) historyView
				.findViewById(R.id. history_task_listview);
		initHistoryAdapter(currentDate);
		
		historyTaskList.setAdapter(historyAdapter);
		
		listEmptyView=new TextView(this);
		listEmptyView.setText(R.string.history_task_list_view_empty);
		listEmptyView.setGravity(Gravity.CENTER);
		listEmptyView.setTextSize(20);	//���������С
		listEmptyView.setTextColor(0xffffffff);
		addContentView(listEmptyView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		historyTaskList.setEmptyView(listEmptyView);
	}
	
	private void refreshHistoryLayout(int date) {
		int year = date / 10000;
		int month = date / 100 - year * 100;
		int day = date % 100;
		String currentDate = DateUtils.formatDate(year, month, day);
		
		String dateStr = getWeekDayFormat(date);// �õ�Format��yyyymmdd���������
		int weekday = getIntWeekDay(dateStr);// �õ��ܼ�������
		String WeekDay = getStringWeekDay(weekday);// �õ��ܼ����ַ���
		String dateString = getStringday(date) + " " + WeekDay;
		
		historyDate.setText(dateString);
		
		historyTaskItemList.clear();
		initHistoryListItem(currentDate);
		historyAdapter.notifyDataSetChanged();
	}
	
	private void initHistoryAdapter(String date){
		historyTaskItemList = new ArrayList<Map<String, Object>>();
		initHistoryListItem(date);
		historyAdapter = new HistoryTaskListViewAdapter();
	}
	
	private void initHistoryListItem(String date) {
		// ��ѯ���ݿ�,��ȡ����������Ϣ
		historyTaskCur = DatabaseUtil.query(HistoryOnedayActivity.this,
				TaskBean.TABLE_NAME, null, TaskBean.DATETIME + " LIKE ?",
				new String[] {date + "%" }, null, null, TaskBean.ID
						+ " ASC");

		// ���������������Ϣ
		
		for (historyTaskCur.moveToFirst(); !historyTaskCur.isAfterLast(); historyTaskCur
				.moveToNext()) {
			Map<String, Object> map = TaskBean.generateTask(historyTaskCur);
			historyTaskItemList.add(map);
		}

		DatabaseUtil.closeDatabase();
	}
	
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
		int curdate = Countday(today, position);
		Message mes = new Message();
		mes.arg1 = REFRESH_LAYOUT;
		mes.arg2 = curdate;
		handler.sendMessage(mes);
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}

	public View makeView() {
		ImageView i = new ImageView(this);
		i.setBackgroundColor(0xFF000000);
		i.setScaleType(ImageView.ScaleType.FIT_CENTER);
		i.setLayoutParams(new ImageSwitcher.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		return i;
	}

	public class ImageAdapter extends BaseAdapter {
		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			return 21;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		// getView������̬����һ��ImageView,Ȼ������setLayoutParams��setImageResource��
		// setBackgroundResource�ֱ��趨ͼƬ��С��ͼƬԴ�ļ���ͼƬ��������ͼƬ����ʾ����ǰ
		// ��Ļ��ʱ����������ͻᱻ�Զ��ص����ṩҪ��ʾ��ImageView
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(mContext);

			Bitmap newb = Bitmap.createBitmap(120, 70, Config.ARGB_8888);

			Canvas canvasTemp = new Canvas(newb);
			Paint pt = new Paint();
			canvasTemp.drawColor(Color.TRANSPARENT);

			pt.setColor(Color.WHITE);
			pt.setTypeface(null);
			pt.setAntiAlias(true);
			pt.setShader(null);
			pt.setFakeBoldText(true);
			pt.setTextSize(20);

			int curdate = Countday(today, position);
			String dateStr = getWeekDayFormat(curdate);// �õ�Format��yyyymmdd���������
			int weekday = getIntWeekDay(dateStr);// �õ��ܼ�������
			String WeekDay = getStringWeekDay(weekday);// �õ��ܼ����ַ���
			
			canvasTemp.drawText(WeekDay, 30, 30, pt);
			pt.setTextSize(14);

			String date = getStringday(curdate);
			canvasTemp.drawText(date, 5, 50, pt);

			i.setImageBitmap(newb);

			i.setAdjustViewBounds(true);
			i.setLayoutParams(new Gallery.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			// i.setBackgroundResource(R.drawable.bg);
			i.setBackgroundResource(HistoryOnebgIds[weekday - 1]);// �����ܼ���������ʾ��ͬ�ı���
			return i;
		}
		
		
		

	}

	public int Countday(int today, int position) {
		String DateStr = getWeekDayFormat(today);
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");

		Date date = null;
		int selday = 0;
		try {
			date = f.parse(DateStr);// ��String ת��Ϊ���ϸ�ʽ������
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DATE, position - 10);

			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			selday = year * 10000 + month * 100 + day;
			System.out.println(selday);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return selday;
	}

	/* �������ε����ڵõ��ַ���������������� */
	public String getStringday(int today) {
		int year = today / 10000;
		int month = today / 100 - year * 100;
		int day = today % 100;

		String date = year + "��" + month + "��" + day + "��";

		return date;
	}

	/* �õ���ʽ�����ַ��� */
	public String getWeekDayFormat(int today) {
		int year = today / 10000;
		int month = today / 100 - year * 100;
		int day = today % 100;
		String dateStr;

		String xmonth;
		String xday;
		if (month < 10)
			xmonth = "0" + month;
		else
			xmonth = month + "";
		if (day < 10)
			xday = "0" + day;
		else
			xday = day + "";

		dateStr = year + xmonth + xday;

		return dateStr;
	}

	/* �õ���ʽ�����ַ��� */
	public String getWeekDayFormat2(int today) {
		int year = today / 10000;
		int month = today / 100 - year * 100;
		int day = today % 100;
		String dateStr;

		String xmonth;
		String xday;
		if (month < 10)
			xmonth = "0" + month;
		else
			xmonth = month + "";
		if (day < 10)
			xday = "0" + day;
		else
			xday = day + "";

		dateStr = year + "-"+xmonth +"-"+ xday;

		return dateStr;
	}
	
	/* ����һ������������ܼ� */
	public String getStringWeekDay(int weekday) {
		String weekday1 = "";
		if (weekday == 1)
			weekday1 = "����";
		else if (weekday == 2)
			weekday1 = "��һ";
		else if (weekday == 3)
			weekday1 = "�ܶ�";
		else if (weekday == 4)
			weekday1 = "����";
		else if (weekday == 5)
			weekday1 = "����";
		else if (weekday == 6)
			weekday1 = "����";
		else if (weekday == 7)
			weekday1 = "����";

		return weekday1;
	}

	/* �õ����ܼ������� */
	public int getIntWeekDay(String DateStr) {
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");

		Date date = null;
		int weekDay = 0;
		try {
			date = f.parse(DateStr);// ��String ת��Ϊ���ϸ�ʽ������
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			weekDay = calendar.get(Calendar.DAY_OF_WEEK);
			System.out.println(weekDay);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println("����:"+DateStr+" �� "+weekDay);
		return weekDay;
	}

	private class HistoryTaskListViewAdapter extends BaseAdapter{
		
		private int taskId = 0;
		private int curPosition = 0;
		
		final class TaskListItemView {
			public TextView taskInfo; // ��������
			public TextView taskTimeAndPositionInfo; // �������ʱ��͵ص���Ϣ
			public Button taskEditBtn; // ����༭��ť
			public ImageView taskStarRank; // �������ȼ�ͼƬ
		}
		
		public int getCount() {
			// TODO Auto-generated method stub
			return historyTaskItemList.size();
		}

		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return historyTaskItemList.get(arg0);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return Integer.parseInt(historyTaskItemList.get(position).get(TaskBean.ID).toString());			
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			TaskListItemView listItemView = null;
			if (convertView == null) {
				listItemView = new TaskListItemView();
				convertView = LayoutInflater.from(HistoryOnedayActivity.this).inflate(R.layout.yesterday_task_item,
						null);
				// ��ȡ�ؼ�����
				listItemView.taskInfo = (TextView) convertView
						.findViewById(R.id.yesterday_task_info);
				listItemView.taskTimeAndPositionInfo = (TextView) convertView
						.findViewById(R.id.yesterday_task_time_position_textview);
				listItemView.taskEditBtn = (Button) convertView
						.findViewById(R.id.yesterday_task_edit_btn);
				listItemView.taskStarRank = (ImageView) convertView
						.findViewById(R.id.yesterday_star_rank);
				// ���ÿؼ�����convertView
				convertView.setTag(listItemView);
			} else {
				listItemView = (TaskListItemView) convertView.getTag();
			}

			// ������������
			listItemView.taskInfo.setText((String) historyTaskItemList.get(
					position).get(TaskBean.TASK_NAME));

			// �����������ʱ��͵ص���Ϣ
			String positionName = (String) historyTaskItemList.get(position).get(
					TaskBean.POSITION_NAME);
			String timeAndPosition = "ʱ��:"
					+ DateUtils.getTaskTime((String) historyTaskItemList.get(
							position).get(TaskBean.DATETIME));
			if (!positionName.equals("")) {
				timeAndPosition += ("  �ص�:" + positionName);
			}
			listItemView.taskTimeAndPositionInfo.setText(timeAndPosition);

			Log.d("debug",
					"position = "
							+ position
							+ " timeAlertFlag = "
							+ (String) historyTaskItemList.get(position).get(
									TaskBean.TIME_ALERT_FLAG));

			

			// �������ȼ�ͼƬ taskStarRank
			switch (Integer.parseInt(historyTaskItemList.get(position)
					.get(TaskBean.PRIORITY).toString())) {
			case TaskConstant.RANK_FOURTH:
				listItemView.taskStarRank
						.setBackgroundResource(R.drawable.star1);
				break;
			case TaskConstant.RANK_THIRD:
				listItemView.taskStarRank
						.setBackgroundResource(R.drawable.star2);
				break;
			case TaskConstant.RANK_SECOND:
				listItemView.taskStarRank
						.setBackgroundResource(R.drawable.star3);
				break;
			case TaskConstant.RANK_FIRST:
				listItemView.taskStarRank
						.setBackgroundResource(R.drawable.star4);
				break;
			}

			if (TaskConstant.TASK_NOT_COMPLETE == Integer
					.parseInt(historyTaskItemList.get(position)
							.get(TaskBean.IF_COMPLETE).toString())) {
				listItemView.taskTimeAndPositionInfo.setTextColor(0xff000000);
				listItemView.taskInfo.setTextColor(0xff000000);
			} else {
				listItemView.taskTimeAndPositionInfo.setTextColor(0xff808080);
				listItemView.taskInfo.setTextColor(0xff808080);
			}

			listItemView.taskEditBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					taskId = Integer.parseInt(historyTaskItemList
							.get(position).get(TaskBean.ID).toString());
					curPosition = position;
					//��������
					showCustomMessage("ȷ��ɾ��", "ȷ��ɾ���˻��");
					
				}
			});
			return convertView;
		}
		
		private void showCustomMessage(String pTitle, final String pMsg) {
			final Dialog lDialog = new Dialog(HistoryOnedayActivity.this,
					android.R.style.Theme_Translucent_NoTitleBar);
			lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			lDialog.setContentView(R.layout.iphone_alert_dialog_layout);
			((TextView) lDialog.findViewById(R.id.dialog_title))
					.setText(pTitle);
			((TextView) lDialog.findViewById(R.id.dialog_message))
					.setText(pMsg);
			((Button) lDialog.findViewById(R.id.cancel))
					.setOnClickListener(new OnClickListener() {

						public void onClick(View v) {
							// write your code to do things after users clicks
							// CANCEL
							lDialog.dismiss();
						}
					});
			((Button) lDialog.findViewById(R.id.ok))
					.setOnClickListener(new OnClickListener() {

						public void onClick(View v) {
							// write your code to do things after users clicks
							// OK
							lDialog.dismiss();
							// ɾ���
							DatabaseUtil.delete(HistoryOnedayActivity.this, TaskBean.TABLE_NAME, TaskBean.ID+"="+taskId, null);
							
							Toast.makeText(HistoryOnedayActivity.this, "�ɾ���ɹ�",
									Toast.LENGTH_SHORT).show();
							// ˢ���б�
							historyTaskItemList.remove(curPosition);
							historyAdapter.notifyDataSetChanged();
						}
					});
			lDialog.show();

		}
	}

}
