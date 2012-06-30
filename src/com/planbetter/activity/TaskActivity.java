package com.planbetter.activity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.planbetter.alarm.Alarm;
import com.planbetter.bean.TaskBean;
import com.planbetter.constant.MenuItemId;
import com.planbetter.constant.MotionEventConstant;
import com.planbetter.constant.TaskConstant;
import com.planbetter.dao.DatabaseUtil;
import com.planbetter.date.DateUtils;
import com.planbetter.receiver.PlanBetterReceiver;
import com.planbetter.widget.TodayTaskWidget;
import com.planbetter.widget.TomorrowTaskWidgetService;

public class TaskActivity extends Activity implements OnGestureListener {
	private ViewFlipper flipper = null;
	private GestureDetector gestureDetector = null;

	private View todayView = null; // ������ͼ
	private View yesterdayView = null; // ������ͼ
	private View tomorrowView = null; // ������ͼ

	private Button todayAddTaskBtn = null; // �����������ť
	private Button tomorrowAddTaskBtn = null; // �����������ť

	private TextView todayTaskTV = null;

	/* �����������Ϣ���б���ͼ���б���ͼadapter */
	private List<Map<String, Object>> todayTaskItemList = null; // List�������������Ϣ
	private ListView todayTaskList = null; // ����������Ϣ�б���ͼ
	private static TodayTaskListViewAdapter todayAdapter = null; // �����б���ͼadapter

	/* δ����������Ϣ���б���ͼ���б���ͼadapter */
	private List<Map<String, Object>> tomorrowTaskItemList = null; // List����δ��������Ϣ
	private ListView tomorrowTaskList = null; // δ��������Ϣ�б���ͼ
	private static TomorrowTaskListViewAdapter tomorrowAdapter = null; // δ���б���ͼadapter
	private TextView tomorrowTaskName = null;
	private TextView tomorrowTaskDate = null;
	private TextView tomorrowTaskLeftDay = null;
	private String tomorrowTaskMessageID = null;

	/* �����������Ϣ���б���ͼ���б���ͼadapter */
	private List<Map<String, Object>> yesterdayTaskItemList = null; // List��������������Ϣ
	private ListView yesterdayTaskList = null; // ����������Ϣ�б���ͼ
	private static YesterdayTaskListViewAdapter yesterdayAdapter = null; // �����б���ͼadapter

	private int index = START_INDEX; // ��ǰview������
	private static final int START_INDEX = 0; // ��һ��view������
	private static final int MIDDLE_INDEX = 1;
	private static final int END_INDEX = 2; // ���һ��view������

	private Cursor todayTaskCur;
	private Cursor tomorrowTaskCur;
	private Cursor yesterdayTaskCur;

	private int priority = TaskConstant.RANK_FIRST;
	private int timeAlertValue = TaskConstant.NO_TIME_ALERT;
	private String time = "";
	private int repeatDays = TaskConstant.INIT_REPEAT_DAYS;

	private TextView repeatDateTimeTV = null;
	private TextView repeatDaysTV = null;

	private NotificationManager notificationManager;
	private Notification notification;
	private Intent notifyIntent;
	private PendingIntent notifyPendingIntent;

	private int alarmHour;
	private int alarmMinute;
	private int initYear;
	private int initMonth;
	private int initDay;

	private TextView todayListEmptyView;
	private TextView tomorrowListEmptyView;
	private TextView yesterdayListEmptyView;

	private static final int REFRESH_TASKACTIVITY = 1;
	private static final int REFRESH_FUTURE = 2;
	private static final int INIT_TASKCTIVITY = 3;

	private boolean hasInit = false;

	private int todayClickViewId = 0;
	private int todayClickPosition = 0;

	private int todayClickCompleteState = TaskConstant.TASK_NOT_COMPLETE;

	// private Typeface typeFace;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// ��ʼ��TaskActivity�Ľ���
		initUI();

		// typeFace =
		// Typeface.createFromAsset(getAssets(),"fonts/vavont-bolder.ttf");

		setMyDateChangedAlarm();

		// ��ʾ
		notifyIntent = new Intent(TaskActivity.this, TaskNotifyActivity.class);
		notifyPendingIntent = PendingIntent.getActivity(TaskActivity.this, 0,
				notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification = new Notification();
		notification.icon = R.drawable.icon;
		notification.tickerText = "���һ������л���ͼ";
		notification.defaults = Notification.DEFAULT_LIGHTS;
		notification.setLatestEventInfo(TaskActivity.this, "PlanBetter",
				"���ͼ���������л�", notifyPendingIntent);

		Timer timer = new Timer();
		timer.schedule(new NofifyTimeTask(), 1000);
		// ������Ϣ
		Message mes = new Message();
		mes.what = INIT_TASKCTIVITY;
		handler.sendMessageDelayed(mes, 1000);
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
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
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

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case REFRESH_TASKACTIVITY:
				initUI();
				flipper.showNext();
				index++;
				// �������γ�Ч��
				if (yesterdayTaskItemList.size() == 0) {
					yesterdayListEmptyView.setVisibility(View.INVISIBLE);
				}
				if (todayTaskItemList.size() == 0) {
					todayListEmptyView.setVisibility(View.VISIBLE);
				}
				break;
			case REFRESH_FUTURE:
				refreshFuture();
				break;
			case INIT_TASKCTIVITY:
				flipper.setInAnimation(AnimationUtils.loadAnimation(
						TaskActivity.this, R.anim.push_left_in_slowly));
				flipper.setOutAnimation(AnimationUtils.loadAnimation(
						TaskActivity.this, R.anim.push_left_out_slowly));
				flipper.showNext();
				index++;

				// �������γ�Ч��
				if (yesterdayTaskItemList.size() == 0) {
					yesterdayListEmptyView.startAnimation(AnimationUtils
							.loadAnimation(TaskActivity.this,
									R.anim.push_left_out_slowly));
					yesterdayListEmptyView.setVisibility(View.INVISIBLE);
				}
				if (todayTaskItemList.size() == 0) {
					todayListEmptyView.startAnimation(AnimationUtils
							.loadAnimation(TaskActivity.this,
									R.anim.push_left_in_slowly));
					todayListEmptyView.setVisibility(View.VISIBLE);
				}
				break;
			}
		}

	};

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (hasInit) {
			Message mes = new Message();
			mes.what = REFRESH_TASKACTIVITY;
			handler.sendMessage(mes);
		}
		hasInit = true;
	}

	private void refreshFuture() {
		tomorrowTaskItemList = new ArrayList<Map<String, Object>>();
		initTomorrowListItem();
		tomorrowAdapter.notifyDataSetChanged();
	}

	private void initUI() {

		setContentView(R.layout.task_main_layout);

		index = START_INDEX;

		initViews();

		// �����������ť
		todayAddTaskBtn = (Button) todayView
				.findViewById(R.id.today_add_task_btn);
		todayAddTaskBtn.setOnClickListener(new AddTaskListener());
		todayTaskTV = (TextView) todayView
				.findViewById(R.id.textView_today_date);
		todayTaskTV.setText(DateUtils.timeDetail(DateUtils.TODAY));

		// �����������ť
		tomorrowAddTaskBtn = (Button) tomorrowView.findViewById(R.id.addTask);
		tomorrowAddTaskBtn.setOnClickListener(new AddTaskListener());

		flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		flipper.addView(yesterdayView);
		flipper.addView(todayView);
		flipper.addView(tomorrowView);

		gestureDetector = new GestureDetector(this);

	}

	private class NofifyTimeTask extends TimerTask {
		@Override
		public void run() {
			notificationManager.notify(0, notification);
		}
	}

	private void registerAlarm(int id, int hourOfDay, int minute) {
		// �ж�ʱ���Ƿ�Ϸ�
		if (DateUtils.checkTimeAlertable(hourOfDay, minute)) {
			Intent intent = new Intent(Alarm.ALARM_REGISTRATION_DETAIL_ACTION);
			Bundle bundle = new Bundle();
			bundle.putInt(Alarm.ID, id);
			bundle.putInt(Alarm.HOUR, hourOfDay);
			bundle.putInt(Alarm.MINUTE, minute);
			intent.putExtra(Alarm.ALARM_REGISTRATION_BUNDLE_TAG, bundle);
			sendBroadcast(intent);
		}
	}

	private void registerAlarm(int id, String datetime) throws ParseException {
		if (DateUtils.checkTimeAlertable(datetime)) {
			Intent intent = new Intent(Alarm.ALARM_REGISTRATION_SIMPLE_ACTION);
			Bundle bundle = new Bundle();
			bundle.putInt(Alarm.ID, id);
			bundle.putString(Alarm.DATETIME, datetime);
			intent.putExtra(Alarm.ALARM_REGISTRATION_BUNDLE_TAG, bundle);
			sendBroadcast(intent);
		}
	}

	private void cancelAlarm(int id) {
		Intent intent = new Intent(Alarm.ALARM_CANCEL_ACTION);
		intent.putExtra(Alarm.ID, id);
		sendBroadcast(intent);
	}

	/**
	 * ������������
	 * 
	 * @author Kelvin
	 * 
	 */
	private class AddTaskListener implements OnClickListener {
		private int differDays = 1;

		public void onClick(View v) {
			priority = TaskConstant.RANK_FIRST;
			int[] nowHourAndMinute = DateUtils.getNowHourAndMinute();
			alarmHour = nowHourAndMinute[0];
			alarmMinute = nowHourAndMinute[1];
			time = DateUtils.formatTime(alarmHour, alarmMinute);
			timeAlertValue = TaskConstant.NO_TIME_ALERT;
			repeatDays = TaskConstant.INIT_REPEAT_DAYS;

			switch (index) {
			// ����ǽ���
			case MIDDLE_INDEX:
				int[] nowYearMonthDay = DateUtils.getNowDate();
				initYear = nowYearMonthDay[0];
				initMonth = nowYearMonthDay[1];
				initDay = nowYearMonthDay[2];
				// ����view
				final View addTaskView = getViewById(R.layout.add_task_dialog_layout);
				final EditText taskContent = (EditText) addTaskView
						.findViewById(R.id.et_task_info);
				final EditText taskPosition = (EditText) addTaskView
						.findViewById(R.id.et_task_position);
				TimePicker timePicker = (TimePicker) addTaskView
						.findViewById(R.id.tp_task_time);
				timePicker.setCurrentHour(alarmHour);
				timePicker.setCurrentMinute(alarmMinute);
				timePicker
						.setOnTimeChangedListener(new OnTimeChangedListener() {

							@Override
							public void onTimeChanged(TimePicker view,
									int hourOfDay, int minute) {
								time = DateUtils.formatTime(hourOfDay, minute);
								alarmHour = hourOfDay;
								alarmMinute = minute;
							}
						});
				RadioGroup rankGroup = (RadioGroup) addTaskView
						.findViewById(R.id.task_rank_group);
				final RadioButton rankFirst = (RadioButton) addTaskView
						.findViewById(R.id.task_rank_first);
				final RadioButton rankSecond = (RadioButton) addTaskView
						.findViewById(R.id.task_rank_second);
				final RadioButton rankThird = (RadioButton) addTaskView
						.findViewById(R.id.task_rank_third);
				final RadioButton rankFourth = (RadioButton) addTaskView
						.findViewById(R.id.task_rank_fourth);

				rankGroup
						.setOnCheckedChangeListener(new OnCheckedChangeListener() {

							@Override
							public void onCheckedChanged(RadioGroup group,
									int checkedId) {
								if (checkedId == rankFirst.getId()) {
									priority = TaskConstant.RANK_FIRST;
								} else if (checkedId == rankSecond.getId()) {
									priority = TaskConstant.RANK_SECOND;
								} else if (checkedId == rankThird.getId()) {
									priority = TaskConstant.RANK_THIRD;
								} else if (checkedId == rankFourth.getId()) {
									priority = TaskConstant.RANK_FOURTH;
								}
							}
						});

				final CheckBox timeAlertCB = (CheckBox) addTaskView
						.findViewById(R.id.cb_time_alert);
				timeAlertCB
						.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {
								if (timeAlertCB.isChecked()) {
									timeAlertValue = TaskConstant.TIME_ALERT;
								} else {
									timeAlertValue = TaskConstant.NO_TIME_ALERT;
								}
							}
						});

				repeatDateTimeTV = (TextView) addTaskView
						.findViewById(R.id.task_repeat_days_date);
				repeatDaysTV = (TextView) addTaskView
						.findViewById(R.id.task_repeat_days_total);
				refreshRepeatDaysTextView();

				Button setRepeatDaysBtn = (Button) addTaskView
						.findViewById(R.id.task_repeat_days_button);
				setRepeatDaysBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						int[] dayInfo = DateUtils
								.getDayInfoByRepeatDays(repeatDays);
						// �������ڶԻ���
						DatePickerDialog dlg = new DatePickerDialog(
								TaskActivity.this, dateListener, dayInfo[0],
								dayInfo[1], dayInfo[2]);
						dlg.setTitle(R.string.task_time_repeat_dialog_title);
						dlg.show();
					}
				});

				// �½��Ի���
				new AlertDialog.Builder(TaskActivity.this)
						.setTitle(R.string.add_task_dialog_title)
						.setIcon(R.drawable.add_task_dialog_icon)
						.setView(addTaskView)
						.setPositiveButton(R.string.save_task_dialog_text,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										String taskContentStr = taskContent
												.getText().toString().trim();
										String taskPositionStr = taskPosition
												.getText().toString().trim();
										if (taskContentStr.equals("")) {
											Toast.makeText(TaskActivity.this,
													"����Ʋ���Ϊ��",
													Toast.LENGTH_SHORT).show();
										} else {
											String datetime = DateUtils.now()
													+ " " + time;
											ContentValues values = new ContentValues();

											values.put(TaskBean.TASK_NAME,
													taskContentStr);
											values.put(TaskBean.DATETIME,
													datetime);
											values.put(
													TaskBean.IF_COMPLETE,
													TaskConstant.TASK_NOT_COMPLETE);
											values.put(TaskBean.POSITION_NAME,
													taskPositionStr);
											values.put(
													TaskBean.TIME_ALERT_FLAG,
													timeAlertValue);
											values.put(TaskBean.PRIORITY,
													priority);
											values.put(TaskBean.REPEAT_DAYS,
													repeatDays);
											values.put(TaskBean.IF_FUTURE,
													TaskConstant.NOT_FUTURE);
											values.put(TaskBean.PARENT,
													TaskConstant.IS_PARENT);

											// DatabaseHelper helper = new
											// DatabaseHelper(TaskActivity.this);
											// SQLiteDatabase db =
											// helper.getWritableDatabase();
											// long id =
											// db.insert(TaskBean.TABLE_NAME,TaskBean.ID,values);

											long id = DatabaseUtil.insert(
													TaskActivity.this,
													TaskBean.TABLE_NAME,
													TaskBean.ID, values);
											if (id == -1) {
												Toast.makeText(
														TaskActivity.this,
														"���ݿ��������ʧ��",
														Toast.LENGTH_SHORT)
														.show();
											} else {

												if (repeatDays > 1) {
													// �����㲥
													Intent insertData = new Intent(
															PlanBetterReceiver.INSERT_DATA_ACTION);
													Bundle bundle = new Bundle();
													bundle.putString(
															TaskBean.TASK_NAME,
															taskContentStr);
													bundle.putString(
															TaskBean.POSITION_NAME,
															taskPositionStr);
													bundle.putInt(
															TaskBean.TIME_ALERT_FLAG,
															timeAlertValue);
													bundle.putInt(
															TaskBean.PRIORITY,
															priority);
													bundle.putString(
															TaskBean.DATETIME,
															datetime);
													bundle.putInt(
															TaskBean.REPEAT_DAYS,
															repeatDays);
													bundle.putInt(
															TaskBean.PARENT,
															(int) id);
													insertData
															.putExtra(
																	PlanBetterReceiver.INSERT_DATA_INTENT_BUNDLE_TAG,
																	bundle);
													sendBroadcast(insertData);
												}

												Map<String, Object> map = new HashMap<String, Object>();
												map.put(TaskBean.TASK_NAME,
														taskContentStr);
												map.put(TaskBean.DATETIME,
														datetime);
												map.put(TaskBean.POSITION_NAME,
														taskPositionStr);
												map.put(TaskBean.PRIORITY,
														priority + "");
												map.put(TaskBean.TIME_ALERT_FLAG,
														timeAlertValue + "");
												map.put(TaskBean.IF_COMPLETE,
														TaskConstant.TASK_NOT_COMPLETE
																+ "");
												map.put(TaskBean.ID, id + "");
												map.put(TaskBean.REPEAT_DAYS,
														repeatDays + "");
												map.put(TaskBean.PARENT,
														TaskConstant.IS_PARENT);

												refreshTodayTaskAfterInsert(map);

												// ע������
												if (timeAlertValue == TaskConstant.TIME_ALERT)
													registerAlarm((int) id,
															alarmHour,
															alarmMinute);
												sendBroadcastToTodayWidget();
											}
										}
									}
								})
						.setNegativeButton(R.string.cancel_dialog_text,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {

									}

								}).create().show();

				break;
			// �����δ��
			case END_INDEX:
				int[] tomorrowYearMonthDay = DateUtils.getTomorrowDate();
				initYear = tomorrowYearMonthDay[0];
				initMonth = tomorrowYearMonthDay[1];
				initDay = tomorrowYearMonthDay[2];
				final View addTomorrowTaskView = getViewById(R.layout.edit_tomorrow_task_dialog_layout);
				final EditText tomorrowtaskContent = (EditText) addTomorrowTaskView
						.findViewById(R.id.et_tomorrow_task_info);

				DatePicker datePicker = (DatePicker) addTomorrowTaskView
						.findViewById(R.id.dp_tomorrow_task_date);
				datePicker.init(initYear, initMonth - 1, initDay,
						new DatePicker.OnDateChangedListener() {

							@Override
							public void onDateChanged(DatePicker view,
									int year,

									int monthOfYear, int dayOfMonth) {

								initYear = year;
								initMonth = monthOfYear + 1;
								initDay = dayOfMonth;

								differDays = DateUtils.getDifferDays(year,
										monthOfYear + 1, dayOfMonth);
								if (differDays < 0)
									Toast.makeText(TaskActivity.this,
											"�����óɽ����Ժ��ʱ��~��", Toast.LENGTH_SHORT)
											.show();

							}

						});

				TimePicker timePicker2 = (TimePicker) addTomorrowTaskView
						.findViewById(R.id.tp_tomorrow_task_time);
				timePicker2.setCurrentHour(alarmHour);
				timePicker2.setCurrentMinute(alarmMinute);

				timePicker2
						.setOnTimeChangedListener(new OnTimeChangedListener() {

							@Override
							public void onTimeChanged(TimePicker view,
									int hourOfDay, int minute) {
								time = DateUtils.formatTime(hourOfDay, minute);
								alarmHour = hourOfDay;
								alarmMinute = minute;
							}
						});

				final CheckBox timeAlertCB2 = (CheckBox) addTomorrowTaskView
						.findViewById(R.id.cb_tomorrow_time_alert);

				timeAlertCB2
						.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {
								if (timeAlertCB2.isChecked()) {
									timeAlertValue = TaskConstant.TIME_ALERT;
									alarmMinute = 0;
									alarmHour = 0;

								} else {
									timeAlertValue = TaskConstant.NO_TIME_ALERT;
									alarmMinute = 0;
									alarmHour = 0;
									time = DateUtils.formatTime(alarmHour,
											alarmMinute);
								}
							}
						});

				// �½��Ի���
				new AlertDialog.Builder(TaskActivity.this)
						.setTitle(R.string.edit_task_dialog_title)
						.setIcon(android.R.drawable.ic_menu_edit)
						.setView(addTomorrowTaskView)
						.setPositiveButton(R.string.save_task_dialog_text,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										String taskContentStr = tomorrowtaskContent
												.getText().toString().trim();

										if (taskContentStr.equals("")) {
											Toast.makeText(TaskActivity.this,
													"����Ʋ���Ϊ��",
													Toast.LENGTH_SHORT).show();
										} else if (differDays < 0) {
											Toast.makeText(TaskActivity.this,
													"�ʱ�����ò���������δ�洢",
													Toast.LENGTH_SHORT).show();
										} else {
											String datetime = DateUtils
													.formatDate(initYear,
															initMonth, initDay)
													+ " " + time;
											ContentValues values = new ContentValues();

											values.put(TaskBean.TASK_NAME,
													taskContentStr);
											values.put(TaskBean.DATETIME,
													datetime);
											values.put(
													TaskBean.IF_COMPLETE,
													TaskConstant.TASK_NOT_COMPLETE);
											values.put(TaskBean.POSITION_NAME,
													"");
											values.put(
													TaskBean.TIME_ALERT_FLAG,
													timeAlertValue);
											values.put(TaskBean.PRIORITY,
													TaskConstant.RANK_FOURTH);
											values.put(
													TaskBean.REPEAT_DAYS,
													TaskConstant.INIT_REPEAT_DAYS);
											values.put(TaskBean.IF_FUTURE,
													TaskConstant.IS_FUTURE);
											values.put(TaskBean.PARENT,
													TaskConstant.IS_PARENT);

											long id = DatabaseUtil.insert(
													TaskActivity.this,
													TaskBean.TABLE_NAME,
													TaskBean.ID, values);
											if (id == -1) {
												Toast.makeText(
														TaskActivity.this,
														"���ݿ��������ʧ��",
														Toast.LENGTH_SHORT)
														.show();
											} else {

												Map<String, Object> map = new HashMap<String, Object>();
												map.put(TaskBean.TASK_NAME,
														taskContentStr);
												map.put(TaskBean.DATETIME,
														datetime);
												map.put(TaskBean.POSITION_NAME,
														"");
												map.put(TaskBean.PRIORITY,
														TaskConstant.RANK_FOURTH
																+ "");
												map.put(TaskBean.TIME_ALERT_FLAG,
														timeAlertValue + "");
												map.put(TaskBean.IF_COMPLETE,
														TaskConstant.TASK_NOT_COMPLETE
																+ "");
												map.put(TaskBean.ID, id + "");
												map.put(TaskBean.REPEAT_DAYS,
														TaskConstant.INIT_REPEAT_DAYS
																+ "");

												// refreshYesterdayTaskAfterInsert(map);
												refreshTomorrowTaskAfterInsert(map);
												String taskid = id + "";
												refreshTomorrowMessageSendMessage(
														taskid, taskContentStr,
														datetime);
												startServiceToFreshTomorrowWidget();
											}
										}
									}
								})

						.setNegativeButton(R.string.cancel_dialog_text,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {

									}

								}).create().show();
				break;
			}
		}

	}

	private void refreshTomorrowMessageSendMessage(String id, String taskname,
			String date) {
		Message mes = new Message();
		mes.what = REFRESH_TOMORROW_MESSAGE;
		Bundle bundle = new Bundle();
		bundle.putString(TaskBean.ID, id);
		bundle.putString(TaskBean.TASK_NAME, taskname);
		bundle.putString(TaskBean.DATETIME, date);
		mes.setData(bundle);
		refreshHandler.sendMessage(mes);
	}

	private void refreshTomorrowMessageDelete() {
		String id = null;
		String taskName = null;
		String dateStr = null;
		int x = Integer.MAX_VALUE;
		int size = tomorrowTaskItemList.size();
		/* Ѱ��map�о������������Ǹ����� */
		for (int i = 0; i < size; i++) {
			Map<String, Object> map = tomorrowTaskItemList.get(i);
			String date = (String) map.get(TaskBean.DATETIME);
			int[] yearMonthDay = DateUtils
					.getYearMonthDayHourAndMinuteByDateTime(date);
			int taskYear = yearMonthDay[0];
			int taskMonth = yearMonthDay[1];
			int taskDay = yearMonthDay[2];

			String datetime = DateUtils
					.formatDate(taskYear, taskMonth, taskDay);

			int differ = x + 1;
			try {
				differ = DateUtils.getDifferDays(datetime);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			if (differ <= x) {
				taskName = (String) map.get(TaskBean.TASK_NAME);
				dateStr = (String) map.get(TaskBean.DATETIME);
				id = (String) map.get(TaskBean.ID);
				x = differ;
			}
		}
		refreshTomorrowMessageSendMessage(id, taskName, dateStr);
	}

	private void refreshRepeatDaysTextView() {
		repeatDateTimeTV.setText(DateUtils.getDateByRepeatDays(repeatDays));
		repeatDaysTV.setText(DateUtils.getDayByRepeatDays(repeatDays));
	}

	private OnDateSetListener dateListener = new OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			try {
				repeatDays = DateUtils.getDifferDays(year + "-"
						+ (monthOfYear + 1) + "-" + dayOfMonth);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (repeatDays < -1) {
				Toast.makeText(getApplicationContext(), "��ֹ���ڲ��������ڽ���֮ǰ",
						Toast.LENGTH_SHORT).show();
				repeatDays = 1;
			} else if (repeatDays == -1) {
				repeatDays = 1;
			} else {
				repeatDays++;
			}

			refreshRepeatDaysTextView();
		}
	};

	private void deleteAfterTodayInfo(int position, int id) {
		int parent = Integer.parseInt(todayTaskItemList.get(position)
				.get(TaskBean.PARENT).toString());

		String delSql = "";

		// ɾ��parent����curViewId����parent����parent,������С����ǰ���ڵĻ
		if (parent == TaskConstant.IS_PARENT) {
			delSql = "DELETE FROM " + TaskBean.TABLE_NAME + " WHERE "
					+ TaskBean.PARENT + "=" + id;
		} else {
			delSql = "DELETE FROM "
					+ TaskBean.TABLE_NAME
					+ " WHERE "
					+ TaskBean.PARENT
					+ "="
					+ parent
					+ " AND "
					+ TaskBean.REPEAT_DAYS
					+ "<"
					+ todayTaskItemList.get(position).get(TaskBean.REPEAT_DAYS)
							.toString();
		}

		DatabaseUtil.delete(TaskActivity.this, delSql);
	}

	public class PullDateListener implements OnDateSetListener {

		private String formerDate;
		private int id;
		private int position;

		public PullDateListener(String date, int id, int position) {
			this.formerDate = date;
			this.id = id;
			this.position = position;
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			int pullDays = 0;
			try {
				pullDays = DateUtils.getDifferDays(year + "-"
						+ (monthOfYear + 1) + "-" + dayOfMonth);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (pullDays <= -1) {
				Toast.makeText(getApplicationContext(), "�Ƴ����ڲ��������ڽ���֮ǰ",
						Toast.LENGTH_SHORT).show();
			} else {
				// �滻�µ�����
				String newDate = year + "-" + (monthOfYear + 1) + "-"
						+ dayOfMonth;
				String newDateTime = newDate + " " + formerDate.split(" ")[1];
				deleteAfterTodayInfo(position, id);
				ContentValues values = new ContentValues();
				values.put(TaskBean.DATETIME, newDateTime);
				values.put(TaskBean.REPEAT_DAYS, TaskConstant.INIT_REPEAT_DAYS);
				values.put(TaskBean.IF_FUTURE, TaskConstant.IS_FUTURE);
				values.put(TaskBean.PARENT, TaskConstant.IS_PARENT);
				int rows = DatabaseUtil.update(TaskActivity.this,
						TaskBean.TABLE_NAME, values, TaskBean.ID + "=" + id,
						null);
				if (rows > 0) {

					// ���ݿ���³ɹ�
					Log.d("debug", "�ʱ���Ƴٵ�" + newDateTime);
					Toast.makeText(TaskActivity.this,
							"�Ƴٻ�ɹ�,�����Զ���Ϊ1��,����δ����ͼ�н��в鿴", Toast.LENGTH_SHORT)
							.show();
					// ȡ������
					cancelAlarm(id);
					todayTaskItemList.remove(position);
					todayAdapter.notifyDataSetChanged();

					todayTaskCur = DatabaseUtil.query(TaskActivity.this,
							TaskBean.TABLE_NAME, null, TaskBean.ID + "=" + id,
							null, null, null, null);

					// �õ����������Ϣ��map��
					for (todayTaskCur.moveToFirst(); !todayTaskCur
							.isAfterLast(); todayTaskCur.moveToNext()) {
						Map<String, Object> map = TaskBean
								.generateTask(todayTaskCur);
						refreshTomorrowTaskAfterInsert(map);
					}
					DatabaseUtil.closeDatabase();
					startServiceToFreshTomorrowWidget();
					sendBroadcastToTodayWidget();

				} else {
					Toast.makeText(TaskActivity.this, "���ݿ����ʧ��",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case MenuItemId.TASK_ITEM_LONG_CLICK_MENU_EDIT:
			// ����༭��¼�
			todayTaskCur = DatabaseUtil.query(TaskActivity.this,
					TaskBean.TABLE_NAME, null, TaskBean.ID + "="
							+ todayClickViewId, null, null, null, null);
			Map<String, Object> map = null;
			for (todayTaskCur.moveToFirst(); !todayTaskCur.isAfterLast(); todayTaskCur
					.moveToNext()) {
				map = TaskBean.generateTask(todayTaskCur);
			}

			DatabaseUtil.closeDatabase();

			final View editTaskView = getViewById(R.layout.add_task_dialog_layout);
			final EditText taskContent = (EditText) editTaskView
					.findViewById(R.id.et_task_info);
			taskContent.setText(map.get(TaskBean.TASK_NAME).toString());
			final EditText taskPosition = (EditText) editTaskView
					.findViewById(R.id.et_task_position);
			taskPosition.setText(map.get(TaskBean.POSITION_NAME).toString());

			int[] minuteAndHour = DateUtils.getHourAndMinuteByDateTime(map.get(
					TaskBean.DATETIME).toString());

			TimePicker timePicker = (TimePicker) editTaskView
					.findViewById(R.id.tp_task_time);
			timePicker.setCurrentHour(minuteAndHour[0]);
			timePicker.setCurrentMinute(minuteAndHour[1]);
			alarmHour = minuteAndHour[0];
			alarmMinute = minuteAndHour[1];
			timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {

				@Override
				public void onTimeChanged(TimePicker view, int hourOfDay,
						int minute) {
					// TODO Auto-generated method stub
					time = DateUtils.formatTime(hourOfDay, minute);
					// alarmHour = hourOfDay;
					// alarmMinute = minute;
				}
			});

			RadioGroup rankGroup = (RadioGroup) editTaskView
					.findViewById(R.id.task_rank_group);
			final RadioButton rankFirst = (RadioButton) editTaskView
					.findViewById(R.id.task_rank_first);
			final RadioButton rankSecond = (RadioButton) editTaskView
					.findViewById(R.id.task_rank_second);
			final RadioButton rankThird = (RadioButton) editTaskView
					.findViewById(R.id.task_rank_third);
			final RadioButton rankFourth = (RadioButton) editTaskView
					.findViewById(R.id.task_rank_fourth);

			priority = Integer.parseInt(map.get(TaskBean.PRIORITY).toString());
			switch (priority) {
			case TaskConstant.RANK_FIRST:
				rankFirst.setChecked(true);
				break;
			case TaskConstant.RANK_SECOND:
				rankSecond.setChecked(true);
				break;
			case TaskConstant.RANK_THIRD:
				rankThird.setChecked(true);
				break;
			case TaskConstant.RANK_FOURTH:
				rankFourth.setChecked(true);
				break;
			}

			rankGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					// TODO Auto-generated method stub
					if (checkedId == rankFirst.getId()) {
						priority = TaskConstant.RANK_FIRST;
					} else if (checkedId == rankSecond.getId()) {
						priority = TaskConstant.RANK_SECOND;
					} else if (checkedId == rankThird.getId()) {
						priority = TaskConstant.RANK_THIRD;
					} else if (checkedId == rankFourth.getId()) {
						priority = TaskConstant.RANK_FOURTH;
					}
				}
			});

			final CheckBox timeAlertCB = (CheckBox) editTaskView
					.findViewById(R.id.cb_time_alert);

			timeAlertValue = Integer.parseInt(map.get(TaskBean.TIME_ALERT_FLAG)
					.toString());
			if (timeAlertValue == TaskConstant.TIME_ALERT) {
				timeAlertCB.setChecked(true);
			} else {
				timeAlertCB.setChecked(false);
			}

			timeAlertCB
					.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							// TODO Auto-generated method stub
							if (timeAlertCB.isChecked()) {
								timeAlertValue = TaskConstant.TIME_ALERT;
							} else {
								timeAlertValue = TaskConstant.NO_TIME_ALERT;
							}
						}
					});

			repeatDays = Integer.parseInt(map.get(TaskBean.REPEAT_DAYS)
					.toString());

			repeatDateTimeTV = (TextView) editTaskView
					.findViewById(R.id.task_repeat_days_date);
			repeatDaysTV = (TextView) editTaskView
					.findViewById(R.id.task_repeat_days_total);
			refreshRepeatDaysTextView();

			Button setRepeatDaysBtn = (Button) editTaskView
					.findViewById(R.id.task_repeat_days_button);
			setRepeatDaysBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int[] dayInfo = DateUtils
							.getDayInfoByRepeatDays(repeatDays);
					// �������ڶԻ���
					DatePickerDialog dlg = new DatePickerDialog(
							TaskActivity.this, dateListener, dayInfo[0],
							dayInfo[1], dayInfo[2]);
					dlg.setTitle(R.string.task_time_repeat_dialog_title);
					dlg.show();
				}
			});

			// �½��Ի���
			new AlertDialog.Builder(TaskActivity.this)
					.setTitle(R.string.edit_task_dialog_title)
					.setIcon(android.R.drawable.ic_menu_edit)
					.setView(editTaskView)
					.setPositiveButton(R.string.save_task_dialog_text,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									String taskContentStr = taskContent
											.getText().toString().trim();
									String taskPositionStr = taskPosition
											.getText().toString().trim();
									if (taskContentStr.equals("")) {
										Toast.makeText(TaskActivity.this,
												"����Ʋ���Ϊ��", Toast.LENGTH_SHORT)
												.show();
									} else {
										String datetime = DateUtils.now() + " "
												+ time;
										ContentValues values = new ContentValues();

										values.put(TaskBean.TASK_NAME,
												taskContentStr);
										values.put(TaskBean.DATETIME, datetime);
										values.put(TaskBean.IF_COMPLETE,
												TaskConstant.TASK_NOT_COMPLETE);
										values.put(TaskBean.POSITION_NAME,
												taskPositionStr);
										values.put(TaskBean.TIME_ALERT_FLAG,
												timeAlertValue);
										values.put(TaskBean.PRIORITY, priority);
										values.put(TaskBean.REPEAT_DAYS,
												repeatDays);

										deleteAfterTodayInfo(
												todayClickPosition,
												todayClickViewId);
										int rows = DatabaseUtil.update(
												TaskActivity.this,
												TaskBean.TABLE_NAME, values,
												TaskBean.ID + "="
														+ todayClickViewId,
												null);

										if (rows > 0) {
											int parent = Integer
													.parseInt(todayTaskItemList
															.get(todayClickPosition)
															.get(TaskBean.PARENT)
															.toString());
											// ���͹㲥֪ͨ��̨�޸����ݿ�
											if (repeatDays > 1) {
												// �����㲥
												Intent insertData = new Intent(
														PlanBetterReceiver.INSERT_DATA_AFTER_MODIFY);
												Bundle bundle = new Bundle();
												bundle.putString(
														TaskBean.TASK_NAME,
														taskContentStr);
												bundle.putString(
														TaskBean.POSITION_NAME,
														taskPositionStr);
												bundle.putInt(
														TaskBean.TIME_ALERT_FLAG,
														timeAlertValue);
												bundle.putInt(
														TaskBean.PRIORITY,
														priority);
												bundle.putString(
														TaskBean.DATETIME,
														datetime);
												bundle.putInt(
														TaskBean.REPEAT_DAYS,
														repeatDays);
												bundle.putInt(TaskBean.PARENT,
														(int) parent);
												bundle.putInt(
														TaskBean.CURVIEWID,
														todayClickViewId);
												insertData
														.putExtra(
																PlanBetterReceiver.INSERT_DATA_AFTER_MODIFY_INTENT_BUNDLE_TAG,
																bundle);
												sendBroadcast(insertData);
											}

											Map<String, Object> map = new HashMap<String, Object>();
											map.put(TaskBean.TASK_NAME,
													taskContentStr);
											map.put(TaskBean.DATETIME, datetime);
											map.put(TaskBean.POSITION_NAME,
													taskPositionStr);
											map.put(TaskBean.PRIORITY, priority
													+ "");
											map.put(TaskBean.TIME_ALERT_FLAG,
													timeAlertValue + "");
											map.put(TaskBean.IF_COMPLETE,
													TaskConstant.TASK_NOT_COMPLETE
															+ "");
											map.put(TaskBean.ID,
													todayClickViewId + "");
											map.put(TaskBean.REPEAT_DAYS,
													repeatDays + "");
											map.put(TaskBean.PARENT, parent
													+ "");

											refreshTodayTaskAfterUpdate(
													todayClickPosition, map);
											Toast.makeText(TaskActivity.this,
													"�޸Ļ�ɹ�",
													Toast.LENGTH_SHORT).show();
											cancelAlarm(todayClickViewId);
											if (timeAlertValue == TaskConstant.TIME_ALERT) {
												try {
													registerAlarm(
															todayClickViewId,
															datetime);
												} catch (ParseException e) {
													// TODO Auto-generated catch
													// block
													e.printStackTrace();
												}
											}
											sendBroadcastToTodayWidget();
										} else {
											Toast.makeText(TaskActivity.this,
													"�޸Ļʧ��",
													Toast.LENGTH_SHORT).show();
										}
									}
								}
							})
					.setNegativeButton(R.string.cancel_dialog_text,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}

							}).create().show();

			return true;
		case MenuItemId.TASK_ITEM_LONG_CLICK_MENU_DELETE:
			// ����ɾ����¼�
			deleteTodayTaskItem("ȷ��ɾ��", "ȷ��ɾ�����");
			return true;
		case MenuItemId.TASK_ITEM_LONG_CLICK_MENU_PULL:
			// �����Ƴٻ�¼�
			if (todayClickCompleteState == TaskConstant.TASK_NOT_COMPLETE) {
				// ����Ƴ�
				// ��ѯʱ��
				todayTaskCur = DatabaseUtil.query(TaskActivity.this,
						TaskBean.TABLE_NAME,
						new String[] { TaskBean.DATETIME }, TaskBean.ID + "="
								+ todayClickViewId, null, null, null, null);
				String datetime = "";
				for (todayTaskCur.moveToFirst(); !todayTaskCur.isAfterLast(); todayTaskCur
						.moveToNext()) {
					datetime = todayTaskCur.getString(todayTaskCur
							.getColumnIndex(TaskBean.DATETIME));
				}
				DatabaseUtil.closeDatabase();
				int[] yearMonthAndDay = DateUtils
						.getYearMonthAndDayByDateTime(datetime);
				DatePickerDialog dlg = new DatePickerDialog(TaskActivity.this,
						new PullDateListener(datetime, todayClickViewId,
								todayClickPosition), yearMonthAndDay[0],
						yearMonthAndDay[1] - 1, yearMonthAndDay[2]);
				dlg.setTitle(R.string.task_time_pull_dialog_title);
				dlg.show();
			} else {
				Toast.makeText(TaskActivity.this, "�������޷��Ƴ�",
						Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void deleteTodayTaskItem(String pTitle, final String pMsg) {
		final Dialog lDialog = new Dialog(TaskActivity.this,
				android.R.style.Theme_Translucent_NoTitleBar);
		lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		lDialog.setContentView(R.layout.iphone_alert_dialog_layout);
		((TextView) lDialog.findViewById(R.id.dialog_title)).setText(pTitle);
		((TextView) lDialog.findViewById(R.id.dialog_message)).setText(pMsg);
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
						Log.d("debug", "delete curViewId=" + todayClickViewId);

						deleteAfterTodayInfo(todayClickPosition,
								todayClickViewId);
						DatabaseUtil.delete(TaskActivity.this,
								TaskBean.TABLE_NAME, TaskBean.ID + "="
										+ todayClickViewId, null);

						Toast.makeText(TaskActivity.this, "�ɾ���ɹ�",
								Toast.LENGTH_SHORT).show();
						// ˢ���б�
						cancelAlarm(todayClickViewId);
						todayTaskItemList.remove(todayClickPosition);
						todayAdapter.notifyDataSetChanged();
						sendBroadcastToTodayWidget();
					}
				});
		lDialog.show();
	}

	private void initViews() {
		// ��ʼ��������ͼ
		todayView = LayoutInflater.from(TaskActivity.this).inflate(
				R.layout.today_task_layout, null);
		initTodayAdapter();
		todayTaskList = (ListView) todayView
				.findViewById(R.id.today_task_listview);
		todayTaskList.setAdapter(todayAdapter);
		todayListEmptyView = new TextView(this);
		todayListEmptyView.setText(R.string.task_list_view_empty);
		todayListEmptyView.setGravity(Gravity.CENTER);
		todayListEmptyView.setTextSize(20); // ���������С
		todayListEmptyView.setTextColor(0xff000000);
		addContentView(todayListEmptyView, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		todayTaskList.setEmptyView(todayListEmptyView);

		todayTaskList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				ContentValues values = new ContentValues();
				if (Integer.parseInt(todayTaskItemList.get(position)
						.get(TaskBean.IF_COMPLETE).toString()) == TaskConstant.TASK_NOT_COMPLETE) {
					values.put(TaskBean.IF_COMPLETE, TaskConstant.TASK_COMPLETE);
					int rows = DatabaseUtil.update(TaskActivity.this,
							TaskBean.TABLE_NAME, values,
							TaskBean.ID + "=" + id, null);
					if (rows > 0) {
						// ���³ɹ�
						Map<String, Object> map = todayTaskItemList
								.get(position);
						map.put(TaskBean.IF_COMPLETE,
								TaskConstant.TASK_COMPLETE + "");
						todayTaskItemList.set(position, map);
						todayAdapter.notifyDataSetChanged();
						// ȡ������
						cancelAlarm((int) id);
						Toast.makeText(TaskActivity.this, "��Ѿ����",
								Toast.LENGTH_SHORT).show();
					} else {
						// ����ʧ��
						Toast.makeText(TaskActivity.this, "���ݿ����ʧ��",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					values.put(TaskBean.IF_COMPLETE,
							TaskConstant.TASK_NOT_COMPLETE);
					int rows = DatabaseUtil.update(TaskActivity.this,
							TaskBean.TABLE_NAME, values,
							TaskBean.ID + "=" + id, null);
					if (rows > 0) {
						// ���³ɹ�
						Map<String, Object> map = todayTaskItemList
								.get(position);
						map.put(TaskBean.IF_COMPLETE,
								TaskConstant.TASK_NOT_COMPLETE + "");
						todayTaskItemList.set(position, map);
						todayAdapter.notifyDataSetChanged();
						if (Integer.parseInt(todayTaskItemList.get(position)
								.get(TaskBean.TIME_ALERT_FLAG).toString()) == TaskConstant.TIME_ALERT) {
							String datetime = todayTaskItemList.get(position)
									.get(TaskBean.DATETIME).toString();
							int[] val = DateUtils
									.getHourAndMinuteByDateTime(datetime);
							registerAlarm((int) id, val[0], val[1]);
						}
						Toast.makeText(TaskActivity.this, "���δ���",
								Toast.LENGTH_SHORT).show();
					} else {
						// ����ʧ��
						Toast.makeText(TaskActivity.this, "���ݿ����ʧ��",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		todayTaskList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				todayClickViewId = (int) id;
				todayClickPosition = position;
				todayClickCompleteState = Integer.parseInt(todayTaskItemList
						.get(position).get(TaskBean.IF_COMPLETE).toString());
				return false;
			}
		});

		todayTaskList
				.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

					@Override
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						// TODO Auto-generated method stub
						menu.setHeaderIcon(android.R.drawable.ic_menu_more);
						menu.setHeaderTitle(R.string.task_long_click_menu_title);
						menu.add(0, MenuItemId.TASK_ITEM_LONG_CLICK_MENU_EDIT,
								0, R.string.task_long_click_menu_edit);
						menu.add(0,
								MenuItemId.TASK_ITEM_LONG_CLICK_MENU_DELETE, 0,
								R.string.task_long_click_menu_delete);
						menu.add(0, MenuItemId.TASK_ITEM_LONG_CLICK_MENU_PULL,
								0, R.string.task_long_click_menu_pull);
					}
				});

		// ��ʼ��������ͼ
		tomorrowView = LayoutInflater.from(TaskActivity.this).inflate(
				R.layout.tomorrow_task_layout, null);
		tomorrowTaskName = (TextView) tomorrowView
				.findViewById(R.id.textView_datematter);
		tomorrowTaskDate = (TextView) tomorrowView
				.findViewById(R.id.textView_datemattermessage);
		tomorrowTaskLeftDay = (TextView) tomorrowView
				.findViewById(R.id.textView_daysleft);

		initTomorrowAdapter();

		tomorrowTaskList = (ListView) tomorrowView
				.findViewById(R.id.tomorrow_task_listview);
		tomorrowTaskList.setAdapter(tomorrowAdapter);
		tomorrowTaskList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, final long id) {
				tomorrowTaskCur = DatabaseUtil.query(TaskActivity.this,
						TaskBean.TABLE_NAME, null, TaskBean.ID + "=" + id,
						null, null, null, null);
				Map<String, Object> map = null;
				for (tomorrowTaskCur.moveToFirst(); !tomorrowTaskCur
						.isAfterLast(); tomorrowTaskCur.moveToNext()) {
					map = TaskBean.generateTask(tomorrowTaskCur);
				}
				DatabaseUtil.closeDatabase();
				String taskName = (String) map.get(TaskBean.TASK_NAME);
				String dateStr = (String) map.get(TaskBean.DATETIME);
				refreshTomorrowMessageSendMessage(id + "", taskName, dateStr);
			}

		});
		tomorrowTaskList
				.setOnItemLongClickListener(new OnItemLongClickListener() {
					String tPosition;
					String tpriority;
					String trepeatDays;
					int tViewPoition;
					int tyear;
					int tmonth;
					int tday;
					int thour;
					int tminute;
					int differDays = -1;

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, final long id) {

						tViewPoition = position;

						tomorrowTaskCur = DatabaseUtil.query(TaskActivity.this,
								TaskBean.TABLE_NAME, null, TaskBean.ID + "="
										+ id, null, null, null, null);
						Map<String, Object> map = null;
						for (tomorrowTaskCur.moveToFirst(); !tomorrowTaskCur
								.isAfterLast(); tomorrowTaskCur.moveToNext()) {
							map = TaskBean.generateTask(tomorrowTaskCur);
						}

						DatabaseUtil.closeDatabase();

						tPosition = (String) map.get(TaskBean.POSITION_NAME);
						trepeatDays = (String) map.get(TaskBean.REPEAT_DAYS);
						final View editTaskView = getViewById(R.layout.edit_tomorrow_task_dialog_layout);
						final EditText taskContent = (EditText) editTaskView
								.findViewById(R.id.et_tomorrow_task_info);
						taskContent.setText(map.get(TaskBean.TASK_NAME)
								.toString());

						int[] yearMonthDay = DateUtils
								.getYearMonthDayHourAndMinuteByDateTime(map
										.get(TaskBean.DATETIME).toString());
						tyear = yearMonthDay[0];
						tmonth = yearMonthDay[1];
						tday = yearMonthDay[2];
						tminute = yearMonthDay[4];
						thour = yearMonthDay[3];
						DatePicker datePicker = (DatePicker) editTaskView
								.findViewById(R.id.dp_tomorrow_task_date);
						datePicker.init(tyear, tmonth - 1, tday,
								new DatePicker.OnDateChangedListener() {

									@Override
									public void onDateChanged(DatePicker view,
											int year,

											int monthOfYear, int dayOfMonth) {

										tyear = year;
										tmonth = monthOfYear + 1;
										tday = dayOfMonth;

										differDays = DateUtils.getDifferDays(
												year, monthOfYear + 1,
												dayOfMonth);

										if (differDays < 0)
											Toast.makeText(TaskActivity.this,
													"�����óɽ����Ժ��ʱ�䣡",
													Toast.LENGTH_SHORT).show();
									}

								});

						TimePicker timePicker = (TimePicker) editTaskView
								.findViewById(R.id.tp_tomorrow_task_time);
						timePicker.setCurrentHour(thour);
						timePicker.setCurrentMinute(tminute);

						timePicker
								.setOnTimeChangedListener(new OnTimeChangedListener() {

									@Override
									public void onTimeChanged(TimePicker view,
											int hourOfDay, int minute) {
										time = DateUtils.formatTime(hourOfDay,
												minute);
										thour = hourOfDay;
										tminute = minute;
									}
								});

						final CheckBox timeAlertCB = (CheckBox) editTaskView
								.findViewById(R.id.cb_tomorrow_time_alert);

						timeAlertValue = Integer.parseInt(map.get(
								TaskBean.TIME_ALERT_FLAG).toString());
						if (timeAlertValue == TaskConstant.TIME_ALERT) {
							timeAlertCB.setChecked(true);
						} else {
							timeAlertCB.setChecked(false);
						}

						timeAlertCB
								.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

									@Override
									public void onCheckedChanged(
											CompoundButton buttonView,
											boolean isChecked) {
										if (timeAlertCB.isChecked()) {
											timeAlertValue = TaskConstant.TIME_ALERT;
											tminute = 0;
											thour = 0;
										} else {
											timeAlertValue = TaskConstant.NO_TIME_ALERT;
										}
									}
								});

						// �½��Ի���
						new AlertDialog.Builder(TaskActivity.this)
								.setTitle(R.string.edit_task_dialog_title)
								.setIcon(android.R.drawable.ic_menu_edit)
								.setView(editTaskView)
								.setPositiveButton(
										R.string.save_task_dialog_text,
										new DialogInterface.OnClickListener() {

											public void onClick(
													DialogInterface dialog,
													int which) {
												String taskContentStr = taskContent
														.getText().toString()
														.trim();

												if (taskContentStr.equals("")) {
													Toast.makeText(
															TaskActivity.this,
															"����Ʋ���Ϊ��",
															Toast.LENGTH_SHORT)
															.show();
												} else if (differDays < 0) {
													Toast.makeText(
															TaskActivity.this,
															"�������ò���������δ�洢",
															Toast.LENGTH_SHORT)
															.show();
												} else {
													String datetime = DateUtils
															.formatDate(tyear,
																	tmonth,
																	tday)
															+ " " + time;
													ContentValues values = new ContentValues();

													values.put(
															TaskBean.TASK_NAME,
															taskContentStr);
													values.put(
															TaskBean.DATETIME,
															datetime);
													values.put(
															TaskBean.IF_COMPLETE,
															TaskConstant.TASK_NOT_COMPLETE);
													values.put(
															TaskBean.POSITION_NAME,
															tPosition);
													values.put(
															TaskBean.TIME_ALERT_FLAG,
															timeAlertValue);

													int rows = DatabaseUtil
															.update(TaskActivity.this,
																	TaskBean.TABLE_NAME,
																	values,
																	TaskBean.ID
																			+ "="
																			+ id,
																	null);

													if (rows > 0) {
														Map<String, Object> map = new HashMap<String, Object>();
														map.put(TaskBean.TASK_NAME,
																taskContentStr);
														map.put(TaskBean.DATETIME,
																datetime);
														map.put(TaskBean.POSITION_NAME,
																tPosition);
														map.put(TaskBean.PRIORITY,
																tpriority + "");
														map.put(TaskBean.TIME_ALERT_FLAG,
																timeAlertValue
																		+ "");
														map.put(TaskBean.IF_COMPLETE,
																TaskConstant.TASK_NOT_COMPLETE
																		+ "");
														map.put(TaskBean.ID, id
																+ "");
														map.put(TaskBean.REPEAT_DAYS,
																trepeatDays
																		+ "");

														refreshTomorrowTaskAfterUpdate(
																tViewPoition,
																map);
														Toast.makeText(
																TaskActivity.this,
																"�޸Ļ�ɹ�",
																Toast.LENGTH_SHORT)
																.show();

														refreshTomorrowMessageSendMessage(
																id + "",
																taskContentStr,
																datetime);
														startServiceToFreshTomorrowWidget();
													} else {
														Toast.makeText(
																TaskActivity.this,
																"�޸Ļʧ��",
																Toast.LENGTH_SHORT)
																.show();
													}
												}
											}
										})
								.setNeutralButton(R.string.delete_dialog_text,
										new DialogInterface.OnClickListener() {

											public void onClick(
													DialogInterface dialog,
													int which) {
												final Dialog lDialog = new Dialog(
														TaskActivity.this,
														android.R.style.Theme_Translucent_NoTitleBar);
												lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
												lDialog.setContentView(R.layout.iphone_alert_dialog_layout);
												((TextView) lDialog
														.findViewById(R.id.dialog_title))
														.setText("ȷ��ɾ���˻");
												((TextView) lDialog
														.findViewById(R.id.dialog_message))
														.setText("ɾ���ɹ�");
												((Button) lDialog
														.findViewById(R.id.cancel))
														.setOnClickListener(new OnClickListener() {

															public void onClick(
																	View v) {
																lDialog.dismiss();
															}
														});
												((Button) lDialog
														.findViewById(R.id.ok))
														.setOnClickListener(new OnClickListener() {

															public void onClick(
																	View v) {

																lDialog.dismiss();
																// ɾ���

																int rows = DatabaseUtil
																		.delete(TaskActivity.this,
																				TaskBean.TABLE_NAME,
																				TaskBean.ID
																						+ "="
																						+ id,
																				null);
																if (rows > 0) {
																	Toast.makeText(
																			TaskActivity.this,
																			"�ɾ���ɹ�",
																			Toast.LENGTH_SHORT)
																			.show();
																	// ˢ���б�

																	tomorrowTaskItemList
																			.remove(tViewPoition);
																	tomorrowAdapter
																			.notifyDataSetChanged();
																	String taskid = id
																			+ "";
																	if (tomorrowTaskMessageID
																			.equals(taskid))
																		refreshTomorrowMessageDelete();
																	startServiceToFreshTomorrowWidget();
																}
															}
														});
												lDialog.show();
											}

										})
								.setNegativeButton(R.string.cancel_dialog_text,
										new DialogInterface.OnClickListener() {

											public void onClick(
													DialogInterface dialog,
													int which) {

											}

										}).create().show();
						return false;
					}
				});

		tomorrowListEmptyView = new TextView(this);
		tomorrowListEmptyView.setText(R.string.tomorrow_task_list_view_empty);
		tomorrowListEmptyView.setGravity(Gravity.CENTER);
		tomorrowListEmptyView.setTextSize(20); // ���������С
		tomorrowListEmptyView.setTextColor(0xff000000);
		addContentView(tomorrowListEmptyView, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		tomorrowTaskList.setEmptyView(tomorrowListEmptyView);
		// if(tomorrowTaskItemList.size() == 0) {
		// tomorrowListEmptyView.setVisibility(View.INVISIBLE);
		// }

		// ��ʼ��������ͼ
		yesterdayView = LayoutInflater.from(TaskActivity.this).inflate(
				R.layout.yesterday_task_layout, null);
		initYesterdayAdapter();
		yesterdayTaskList = (ListView) yesterdayView
				.findViewById(R.id.yesterday_task_listview);
		yesterdayTaskList.setAdapter(yesterdayAdapter);
		TextView yesterdayDate = (TextView) yesterdayView
				.findViewById(R.id.textView_yesterday_date);
		yesterdayDate.setText(DateUtils.timeDetail("yesterday"));
		yesterdayListEmptyView = new TextView(this);
		yesterdayListEmptyView.setText(R.string.yesterday_task_list_view_empty);
		yesterdayListEmptyView.setGravity(Gravity.CENTER);
		yesterdayListEmptyView.setTextSize(20); // ���������С
		yesterdayListEmptyView.setTextColor(0xff000000);
		addContentView(yesterdayListEmptyView, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		yesterdayTaskList.setEmptyView(yesterdayListEmptyView);

		// �ж�
		if (tomorrowTaskItemList.size() == 0) {
			tomorrowListEmptyView.setVisibility(View.INVISIBLE);
		}
		if (todayTaskItemList.size() == 0) {
			todayListEmptyView.setVisibility(View.INVISIBLE);
		}
	}

	// ��ʼ������������Ϣ��List��
	private void initTodayListItem() {
		// ��ѯ���ݿ�,��ȡ����������Ϣ
		todayTaskCur = DatabaseUtil.query(TaskActivity.this,
				TaskBean.TABLE_NAME, null, TaskBean.DATETIME + " LIKE ?",
				new String[] { DateUtils.now() + "%" }, null, null, TaskBean.ID
						+ " ASC");

		// ���������������Ϣ

		for (todayTaskCur.moveToFirst(); !todayTaskCur.isAfterLast(); todayTaskCur
				.moveToNext()) {
			Map<String, Object> map = TaskBean.generateTask(todayTaskCur);
			todayTaskItemList.add(map);
		}

		DatabaseUtil.closeDatabase();
	}

	// ��ʼ������Adapter
	private void initTodayAdapter() {
		todayTaskItemList = new ArrayList<Map<String, Object>>();
		initTodayListItem();
		todayAdapter = new TodayTaskListViewAdapter();
	}

	private void initTomorrowListItem() {
		tomorrowTaskCur = DatabaseUtil.query(TaskActivity.this,
				TaskBean.TABLE_NAME, null, TaskBean.IF_FUTURE + "="
						+ TaskConstant.IS_FUTURE + " AND " + TaskBean.PARENT
						+ "=" + TaskConstant.IS_PARENT, null, null, null,
				TaskBean.ID + " ASC");
		int x = Integer.MAX_VALUE;
		String taskName = null;
		String dateStr = null;
		String id = null;
		// ����δ����������Ϣ
		for (tomorrowTaskCur.moveToFirst(); !tomorrowTaskCur.isAfterLast(); tomorrowTaskCur
				.moveToNext()) {
			Map<String, Object> map = TaskBean.generateTask(tomorrowTaskCur);
			tomorrowTaskItemList.add(map);
			Log.d("debug", "future map: ����δ����������Ϣ");

			String date = (String) map.get(TaskBean.DATETIME);
			int[] yearMonthDay = DateUtils
					.getYearMonthDayHourAndMinuteByDateTime(date);
			int taskYear = yearMonthDay[0];
			int taskMonth = yearMonthDay[1];
			int taskDay = yearMonthDay[2];

			String datetime = DateUtils
					.formatDate(taskYear, taskMonth, taskDay);

			int differ = x + 1;
			try {
				differ = DateUtils.getDifferDays(datetime);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			if (differ <= x) {
				taskName = (String) map.get(TaskBean.TASK_NAME);
				dateStr = (String) map.get(TaskBean.DATETIME);
				id = (String) map.get(TaskBean.ID);
				x = differ;
			}
		}
		Log.d("debug", "initTomorrowListItem");
		DatabaseUtil.closeDatabase();

		initTomorrowMessage(id, taskName, dateStr);
	}

	private static final int REFRESH_TOMORROW_MESSAGE = 0;

	private Handler refreshHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//
			switch (msg.what) {
			case REFRESH_TOMORROW_MESSAGE:
				Bundle bundle = msg.getData();
				initTomorrowMessage(bundle.getString(TaskBean.ID),
						bundle.getString(TaskBean.TASK_NAME),
						bundle.getString(TaskBean.DATETIME));
				break;
			}
		}

	};

	private void initTomorrowMessage(String id, String name, String date) {
		tomorrowTaskMessageID = id;
		int taskYear;
		int taskMonth;
		int taskDay;
		if (name == null) {
			tomorrowTaskName.setText("���롭������");
			tomorrowTaskDate.setText(DateUtils.timeDetail("TOMORROW"));
			tomorrowTaskLeftDay.setText("X");
		} else {
			int[] yearMonthDay = DateUtils
					.getYearMonthDayHourAndMinuteByDateTime(date);

			taskYear = yearMonthDay[0];
			taskMonth = yearMonthDay[1];
			taskDay = yearMonthDay[2];

			String taskcontent = "����" + name + "��ʣ";

			String datetime = DateUtils
					.formatDate(taskYear, taskMonth, taskDay);
			String weekDay = DateUtils.getWeekDay(datetime);
			String daysmatter = "�����գ�" + datetime + " " + weekDay;

			int differ = 0;
			try {
				differ = DateUtils.getDifferDays(datetime);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			tomorrowTaskName.setText(taskcontent);
			tomorrowTaskDate.setText(daysmatter);
			tomorrowTaskLeftDay.setText(differ + "");
		}
	}

	private void initTomorrowAdapter() {
		tomorrowTaskItemList = new ArrayList<Map<String, Object>>();
		initTomorrowListItem();
		tomorrowAdapter = new TomorrowTaskListViewAdapter();
	}

	private void refreshTodayTaskAfterInsert(Map<String, Object> map) {
		// ˢ�������б�
		todayTaskItemList.add(map);
		todayAdapter.notifyDataSetChanged();
	}

	private void refreshTomorrowTaskAfterInsert(Map<String, Object> map) {
		// ˢ�������б�
		tomorrowTaskItemList.add(map);
		tomorrowAdapter.notifyDataSetChanged();
	}

	// private void refreshYesterdayTaskAfterInsert(Map<String, Object> map) {
	// // ˢ�������б�
	// yesterdayTaskItemList.add(map);
	// yesterdayAdapter.notifyDataSetChanged();
	// }

	private void refreshTodayTaskAfterUpdate(int position,
			Map<String, Object> map) {
		//
		todayTaskItemList.set(position, map);
		todayAdapter.notifyDataSetChanged();
	}

	private void refreshTomorrowTaskAfterUpdate(int position,
			Map<String, Object> map) {
		//
		tomorrowTaskItemList.set(position, map);
		tomorrowAdapter.notifyDataSetChanged();
	}

	private void initYesterdayAdapter() {
		yesterdayTaskItemList = new ArrayList<Map<String, Object>>();
		initYesterdayListItem();
		yesterdayAdapter = new YesterdayTaskListViewAdapter();
	}

	private void initYesterdayListItem() {
		// ��ѯ���ݿ�,��ȡ����������Ϣ
		yesterdayTaskCur = DatabaseUtil.query(TaskActivity.this,
				TaskBean.TABLE_NAME, null, TaskBean.DATETIME + " LIKE ?",
				new String[] { DateUtils.yesterday() + "%" }, null, null,
				TaskBean.ID + " ASC");

		// ���������������Ϣ

		for (yesterdayTaskCur.moveToFirst(); !yesterdayTaskCur.isAfterLast(); yesterdayTaskCur
				.moveToNext()) {
			Map<String, Object> map = TaskBean.generateTask(yesterdayTaskCur);
			yesterdayTaskItemList.add(map);

		}

		DatabaseUtil.closeDatabase();
	}

	// ��ȡview
	private View getViewById(int resourceId) {
		LayoutInflater inflater = LayoutInflater.from(this);
		return inflater.inflate(resourceId, null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		// Ŀ��
		case MenuItemId.MENU_ITEM_GOAL:
			Intent tg_intent = new Intent(TaskActivity.this, GoalActivity.class);
			startActivity(tg_intent);
			break;
		// ��ʷ
		case MenuItemId.MENU_ITEM_HISTORY:
			Intent th_intent = new Intent(TaskActivity.this,
					HistoryActivity.class);
			startActivity(th_intent);
			break;
		// ����
		case MenuItemId.MENU_ITEM_TIPS:
			Intent tt_intent = new Intent(TaskActivity.this,
					HeartActivity.class);
			startActivity(tt_intent);
			break;
		// ����
		case MenuItemId.MENU_ITEM_SETUP:
			Intent ti_intent = new Intent(TaskActivity.this,
					SetupActivity.class);
			startActivity(ti_intent);
			break;
		// ����
		case MenuItemId.MENU_ITEM_HELP:
			Intent tx_intent = new Intent(TaskActivity.this, HelpActivity.class);
			startActivity(tx_intent);
			break;
		// ����
		case MenuItemId.MENU_ITEM_ABOUT:
			Intent ty_intent = new Intent();
			ty_intent.setClass(TaskActivity.this, AboutActivity.class);
			startActivity(ty_intent);
			break;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuItem menu_goal = menu.add(0, MenuItemId.MENU_ITEM_GOAL, 0,
				R.string.menu_goal);
		menu_goal.setIcon(android.R.drawable.ic_menu_directions);

		MenuItem menu_tips = menu.add(0, MenuItemId.MENU_ITEM_TIPS, 0,
				R.string.menu_tips);
		menu_tips.setIcon(android.R.drawable.ic_menu_compass);

		MenuItem menu_history = menu.add(0, MenuItemId.MENU_ITEM_HISTORY, 0,
				R.string.menu_history);
		menu_history.setIcon(android.R.drawable.ic_menu_recent_history);

		MenuItem menu_setup = menu.add(0, MenuItemId.MENU_ITEM_SETUP, 0,
				R.string.menu_setup);
		menu_setup.setIcon(android.R.drawable.ic_menu_preferences);

		MenuItem menu_help = menu.add(0, MenuItemId.MENU_ITEM_HELP, 0,
				R.string.menu_help);
		menu_help.setIcon(android.R.drawable.ic_menu_help);

		MenuItem menu_about = menu.add(0, MenuItemId.MENU_ITEM_ABOUT, 0,
				R.string.menu_about);
		menu_about.setIcon(android.R.drawable.ic_menu_info_details);

		return true;
	}

	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		if (e1.getX() - e2.getX() > MotionEventConstant.getFlingMinDistance()
				&& Math.abs(velocityX) > MotionEventConstant
						.getFlingMinVelocity()) {

			if (index != END_INDEX) {
				// playSound(Sound.OPEN_SOUND);
				this.flipper.setInAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_left_in));
				this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_left_out));
				this.flipper.showNext();
				index++;

				if (index != MIDDLE_INDEX) {
					if (todayTaskItemList.size() == 0) {
						todayListEmptyView.startAnimation(AnimationUtils
								.loadAnimation(this, R.anim.push_left_out));
						todayListEmptyView.setVisibility(View.INVISIBLE);
					}
				} else {
					if (yesterdayTaskItemList.size() == 0) {
						yesterdayListEmptyView.startAnimation(AnimationUtils
								.loadAnimation(this, R.anim.push_left_out));
						yesterdayListEmptyView.setVisibility(View.INVISIBLE);
					}
					if (todayTaskItemList.size() == 0) {
						todayListEmptyView.startAnimation(AnimationUtils
								.loadAnimation(this, R.anim.push_left_in));
						todayListEmptyView.setVisibility(View.VISIBLE);
					}
				}
				if (index == END_INDEX) {
					if (tomorrowTaskItemList.size() == 0) {
						tomorrowListEmptyView.startAnimation(AnimationUtils
								.loadAnimation(this, R.anim.push_left_in));
						tomorrowListEmptyView.setVisibility(View.VISIBLE);
					}
				}

				return true;
			}
		} else if (e2.getX() - e1.getX() > MotionEventConstant
				.getFlingMinDistance()
				&& Math.abs(velocityX) > MotionEventConstant
						.getFlingMinVelocity()) {
			if (index != START_INDEX) {
				// playSound(Sound.OPEN_SOUND);
				this.flipper.setInAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_right_in));
				this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_right_out));
				this.flipper.showPrevious();
				index--;
				if (index != MIDDLE_INDEX) {
					if (todayTaskItemList.size() == 0) {
						todayListEmptyView.startAnimation(AnimationUtils
								.loadAnimation(this, R.anim.push_right_out));
						todayListEmptyView.setVisibility(View.INVISIBLE);
					}
					if (yesterdayTaskItemList.size() == 0) {
						yesterdayListEmptyView.startAnimation(AnimationUtils
								.loadAnimation(this, R.anim.push_right_in));
						yesterdayListEmptyView.setVisibility(View.VISIBLE);
					}
				} else {
					if (todayTaskItemList.size() == 0) {
						todayListEmptyView.startAnimation(AnimationUtils
								.loadAnimation(this, R.anim.push_right_in));
						todayListEmptyView.setVisibility(View.VISIBLE);
					}
					if (tomorrowTaskItemList.size() == 0) {
						tomorrowListEmptyView.startAnimation(AnimationUtils
								.loadAnimation(this, R.anim.push_right_out));
						tomorrowListEmptyView.setVisibility(View.INVISIBLE);
					}
				}
				return true;
			}
		}
		return false;
	}

	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	private class TodayTaskListViewAdapter extends BaseAdapter {

		final class TaskListItemView {
			public TextView taskInfo; // ��������
			public ImageView timeAlert; // ʱ������ͼƬ
			public TextView taskTimeAndPositionInfo; // �������ʱ��͵ص���Ϣ
			public ImageView taskcompleteState; // ����״̬ͼƬ
			public ImageView taskStarRank; // �������ȼ�ͼƬ
			public TextView taskRepeatDays; // ��������
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return todayTaskItemList.size();
		}

		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return todayTaskItemList.get(arg0);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return Integer.parseInt(todayTaskItemList.get(position)
					.get(TaskBean.ID).toString());
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			TaskListItemView listItemView = null;
			if (convertView == null) {
				listItemView = new TaskListItemView();
				convertView = LayoutInflater.from(TaskActivity.this).inflate(
						R.layout.today_task_item, null);
				// ��ȡ�ؼ�����
				listItemView.taskInfo = (TextView) convertView
						.findViewById(R.id.today_task_info);
				listItemView.timeAlert = (ImageView) convertView
						.findViewById(R.id.today_time_alert);
				listItemView.taskTimeAndPositionInfo = (TextView) convertView
						.findViewById(R.id.today_task_time_position_textview);
				listItemView.taskcompleteState = (ImageView) convertView
						.findViewById(R.id.today_task_complete_state);
				listItemView.taskStarRank = (ImageView) convertView
						.findViewById(R.id.today_star_rank);
				listItemView.taskRepeatDays = (TextView) convertView
						.findViewById(R.id.today_repeat_days);
				// ���ÿؼ�����convertView
				convertView.setTag(listItemView);
			} else {
				listItemView = (TaskListItemView) convertView.getTag();
			}

			// ������������
			listItemView.taskInfo.setText((String) todayTaskItemList.get(
					position).get(TaskBean.TASK_NAME));

			// ������������
			listItemView.taskRepeatDays.setText((String) todayTaskItemList.get(
					position).get(TaskBean.REPEAT_DAYS));

			// �����������ʱ��͵ص���Ϣ
			String positionName = (String) todayTaskItemList.get(position).get(
					TaskBean.POSITION_NAME);
			String timeAndPosition = "ʱ��:"
					+ DateUtils.getTaskTime((String) todayTaskItemList.get(
							position).get(TaskBean.DATETIME));
			if (!positionName.equals("")) {
				timeAndPosition += ("  �ص�:" + positionName);
			}
			listItemView.taskTimeAndPositionInfo.setText(timeAndPosition);

			Log.d("debug",
					"position = "
							+ position
							+ " timeAlertFlag = "
							+ (String) todayTaskItemList.get(position).get(
									TaskBean.TIME_ALERT_FLAG));

			// ����ʱ������ͼƬ�Ƿ���ʾ timeAlert
			if (Integer.parseInt(((String) todayTaskItemList.get(position).get(
					TaskBean.TIME_ALERT_FLAG))) == TaskConstant.NO_TIME_ALERT) {
				listItemView.timeAlert.setVisibility(View.INVISIBLE);
				Log.d("debug", "ʱ�����Ѱ�ť����ʾ");
			} else {
				listItemView.timeAlert.setVisibility(View.VISIBLE);
			}

			// �������ȼ�ͼƬ taskStarRank
			switch (Integer.parseInt(todayTaskItemList.get(position)
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
					.parseInt(todayTaskItemList.get(position)
							.get(TaskBean.IF_COMPLETE).toString())) {
				listItemView.taskcompleteState
						.setBackgroundResource(R.drawable.theme_checked);
				listItemView.taskTimeAndPositionInfo.setTextColor(0xff000000);
				listItemView.taskInfo.setTextColor(0xff000000);
			} else {
				listItemView.taskcompleteState
						.setBackgroundResource(R.drawable.theme_checked_disable);
				listItemView.taskTimeAndPositionInfo.setTextColor(0xff808080);
				listItemView.taskInfo.setTextColor(0xff808080);
			}

			return convertView;
		}
	}

	private class TomorrowTaskListViewAdapter extends BaseAdapter {

		private LayoutInflater layoutInflater;

		final class TaskListItemView {
			public TextView taskInfo; // ��������
			public TextView leftday;
		}

		public TomorrowTaskListViewAdapter() {
			this.layoutInflater = LayoutInflater.from(TaskActivity.this);
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return tomorrowTaskItemList.size();
		}

		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return tomorrowTaskItemList.get(arg0);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return Integer.parseInt(tomorrowTaskItemList.get(position)
					.get(TaskBean.ID).toString());
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			TaskListItemView listItemView = null;
			if (convertView == null) {
				listItemView = new TaskListItemView();
				convertView = layoutInflater.inflate(
						R.layout.tomorrow_task_item, null);
				// ��ȡ�ؼ�����
				listItemView.taskInfo = (TextView) convertView
						.findViewById(R.id.tomorrow_item_textView_datematter);
				listItemView.leftday = (TextView) convertView
						.findViewById(R.id.tomorrow_item_textView_daysleft);
				// ���ÿؼ�����convertView
				convertView.setTag(listItemView);
			} else {
				listItemView = (TaskListItemView) convertView.getTag();
			}

			// ����������������
			String x = "����"
					+ (String) tomorrowTaskItemList.get(position).get(
							TaskBean.TASK_NAME) + "��ʣ";
			listItemView.taskInfo.setText(x);
			// ��������ʣ�������

			String datetime = (String) tomorrowTaskItemList.get(position).get(
					TaskBean.DATETIME);
			String[] spliteOnce = datetime.split(" ", 2);
			String yearmd = spliteOnce[0];
			int differdays = 0;
			try {
				differdays = DateUtils.getDifferDays(yearmd);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String differ = differdays + "";
			listItemView.leftday.setText(differ);

			Log.d("debug",
					"tomorrowposition = "
							+ position
							+ " timeAlertFlag = "
							+ (String) tomorrowTaskItemList.get(position).get(
									TaskBean.TIME_ALERT_FLAG));

			return convertView;
		}
	}

	private class YesterdayTaskListViewAdapter extends BaseAdapter {

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
			return yesterdayTaskItemList.size();
		}

		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return yesterdayTaskItemList.get(arg0);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return Integer.parseInt(yesterdayTaskItemList.get(position)
					.get(TaskBean.ID).toString());
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			TaskListItemView listItemView = null;
			if (convertView == null) {
				listItemView = new TaskListItemView();
				convertView = LayoutInflater.from(TaskActivity.this).inflate(
						R.layout.yesterday_task_item, null);
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
			listItemView.taskInfo.setText((String) yesterdayTaskItemList.get(
					position).get(TaskBean.TASK_NAME));

			// �����������ʱ��͵ص���Ϣ
			String positionName = (String) yesterdayTaskItemList.get(position)
					.get(TaskBean.POSITION_NAME);
			String timeAndPosition = "ʱ��:"
					+ DateUtils.getTaskTime((String) yesterdayTaskItemList.get(
							position).get(TaskBean.DATETIME));
			if (!positionName.equals("")) {
				timeAndPosition += ("  �ص�:" + positionName);
			}
			listItemView.taskTimeAndPositionInfo.setText(timeAndPosition);

			Log.d("debug",
					"position = "
							+ position
							+ " timeAlertFlag = "
							+ (String) yesterdayTaskItemList.get(position).get(
									TaskBean.TIME_ALERT_FLAG));

			// �������ȼ�ͼƬ taskStarRank
			switch (Integer.parseInt(yesterdayTaskItemList.get(position)
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
					.parseInt(yesterdayTaskItemList.get(position)
							.get(TaskBean.IF_COMPLETE).toString())) {
				listItemView.taskTimeAndPositionInfo.setTextColor(0xff000000);
				listItemView.taskInfo.setTextColor(0xff000000);
			} else {
				listItemView.taskTimeAndPositionInfo.setTextColor(0xff808080);
				listItemView.taskInfo.setTextColor(0xff808080);
			}

			// ��������
			// listItemView.taskInfo.setTypeface(typeFace);

			listItemView.taskEditBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					// ɾ������
					// ��ȡid
					taskId = Integer.parseInt(yesterdayTaskItemList
							.get(position).get(TaskBean.ID).toString());
					curPosition = position;
					// ��������
					showCustomMessage("ȷ��ɾ��", "ȷ��ɾ���˻��");
				}
			});
			return convertView;
		}

		private void showCustomMessage(String pTitle, final String pMsg) {
			final Dialog lDialog = new Dialog(TaskActivity.this,
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
							DatabaseUtil.delete(TaskActivity.this,
									TaskBean.TABLE_NAME, TaskBean.ID + "="
											+ taskId, null);

							Toast.makeText(TaskActivity.this, "�ɾ���ɹ�",
									Toast.LENGTH_SHORT).show();
							// ˢ���б�
							yesterdayTaskItemList.remove(curPosition);
							yesterdayAdapter.notifyDataSetChanged();
						}
					});
			lDialog.show();

		}
	}
}
