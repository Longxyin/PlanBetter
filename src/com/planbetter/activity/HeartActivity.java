package com.planbetter.activity;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.planbetter.bean.HeartMessage;
import com.planbetter.constant.MenuItemId;
import com.planbetter.dao.DatabaseUtil;
import com.planbetter.date.DateUtils;


public class HeartActivity extends Activity {
	private HeartAdapter chatHistoryAdapter;
	private List<HeartMessage> messages = new ArrayList<HeartMessage>();

	private int messageDirection = HeartMessage.MESSAGE_FROM;
	private ListView chatHistoryLv;
	private Button sendBtn;
	private EditText textEditor;
	private Cursor databaseCur;
	
	private TextView heartListViewEmptyTV;
	
	private void changeMessageDirection() {
		if(messageDirection == HeartMessage.MESSAGE_FROM) {
			messageDirection = HeartMessage.MESSAGE_TO;
		} else {
			messageDirection = HeartMessage.MESSAGE_FROM;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.heart_layout);
		chatHistoryLv = (ListView) findViewById(R.id.chatting_history_lv);
		setAdapterForThis();
		sendBtn = (Button) findViewById(R.id.send_button);
		textEditor = (EditText) findViewById(R.id.text_editor);

		sendBtn.setOnClickListener(l);
		
		chatHistoryLv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				//执行删除操作
				showCustomMessage("确认删除","确认删除本条心语",(int)id,position);
				return false;
			}
		});
	}

	// 设置adapter
	private void setAdapterForThis() {
		initMessages();
		chatHistoryAdapter = new HeartAdapter();
		chatHistoryLv.setAdapter(chatHistoryAdapter);
		heartListViewEmptyTV = new TextView(this);
		heartListViewEmptyTV.setText(R.string.heart_list_view_empty);
		heartListViewEmptyTV.setGravity(Gravity.CENTER);
		heartListViewEmptyTV.setTextSize(20);	//设置字体大小
		heartListViewEmptyTV.setTextColor(0xff000000);
		addContentView(heartListViewEmptyTV, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		chatHistoryLv.setEmptyView(heartListViewEmptyTV);
	}

	// 为listView添加数据
	private void initMessages() {
		//查询数据库
		databaseCur = DatabaseUtil.query(HeartActivity.this, HeartMessage.TABLE_NAME, 
				null, null, null, null, null, HeartMessage.ID+" ASC");
		for(databaseCur.moveToFirst(); !databaseCur.isAfterLast();databaseCur.moveToNext()) {
			HeartMessage hm = HeartMessage.generateHeartMessage(databaseCur);
			hm.setDirection(messageDirection);
			messages.add(hm);
			changeMessageDirection();
		}
		DatabaseUtil.closeDatabase();
	}

	/**
	 * 按键时间监听
	 */
	private View.OnClickListener l = new View.OnClickListener() {

		public void onClick(View v) {

			if (v.getId() == sendBtn.getId()) {
				String str = textEditor.getText().toString();
				String sendStr;
				if (str != null
						&& (sendStr = str.trim().replaceAll("\r", "").replaceAll("\t", "").replaceAll("\n", "")
								.replaceAll("\f", "")) != "") {
					sendMessage(sendStr, messageDirection);
					changeMessageDirection();
				}
				textEditor.setText("");
			}
		}

		// 模拟发送消息
		private void sendMessage(String sendStr, int flag) {
			ContentValues values = new ContentValues();
			String datetime = DateUtils.nowDetail();
			values.put(HeartMessage.DATE, datetime);
			values.put(HeartMessage.HEART_CONTENT, sendStr);
			long id = DatabaseUtil.insert(HeartActivity.this, HeartMessage.TABLE_NAME, HeartMessage.ID, values);
			if(id != -1) {
				HeartMessage hm = new HeartMessage((int)id, datetime, sendStr, flag);
				messages.add(hm);
				chatHistoryAdapter.notifyDataSetChanged();
			}
		}
	};
	
	private void showCustomMessage(String pTitle, final String pMsg, final int id, final int position) {
		final Dialog lDialog = new Dialog(HeartActivity.this,
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
						// write your code to do things after users clicks OK
						lDialog.dismiss();
						int rows = DatabaseUtil.delete(HeartActivity.this, HeartMessage.TABLE_NAME, HeartMessage.ID+"="+id, null);
						if(rows > 0) {
							//更新listview
							messages.remove(position);
							chatHistoryAdapter.notifyDataSetChanged();
							Toast.makeText(HeartActivity.this, "心语删除成功", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(HeartActivity.this, "数据库更新失败", Toast.LENGTH_SHORT).show();
						}
					}
				});
		lDialog.show();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId())
		{
		//活动
		case MenuItemId.MENU_ITEM_TASK:
			Intent ht_intent = new Intent(HeartActivity.this, TaskActivity.class);
			startActivity(ht_intent);
			break;
		//目标
		case MenuItemId.MENU_ITEM_GOAL:
			Intent tg_intent = new Intent(HeartActivity.this, GoalActivity.class);
			startActivity(tg_intent);
			break;
		//历史
		case MenuItemId.MENU_ITEM_HISTORY:
			Intent th_intent = new Intent(HeartActivity.this, HistoryActivity.class);
			startActivity(th_intent);
			break;
			//设置
		case MenuItemId.MENU_ITEM_SETUP:
			Intent ti_intent = new Intent(HeartActivity.this, SetupActivity.class);
			startActivity(ti_intent);
			break;
			//帮助
		case MenuItemId.MENU_ITEM_HELP:
			Intent tx_intent = new Intent(HeartActivity.this, HelpActivity.class);
			startActivity(tx_intent);
			break;
			//关于
		case MenuItemId.MENU_ITEM_ABOUT:
			Intent ty_intent = new Intent(HeartActivity.this, AboutActivity.class);
			startActivity(ty_intent);
			break;
		}
		return true;
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuItem menu_task = menu.add(0, MenuItemId.MENU_ITEM_TASK, 0,
				R.string.menu_task);
		menu_task.setIcon(android.R.drawable.ic_menu_agenda);

		MenuItem menu_goal = menu.add(0, MenuItemId.MENU_ITEM_GOAL, 0,
				R.string.menu_goal);
		menu_goal.setIcon(android.R.drawable.ic_menu_directions);

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
	
	public class HeartAdapter extends BaseAdapter {
		public HeartAdapter() {
		}

		public int getCount() {
			return messages.size();
		}

		public Object getItem(int position) {
			return messages.get(position);
		}

		public long getItemId(int position) {
			return messages.get(position).getHeartId();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			HeartMessage message = messages.get(position);
			if (convertView == null
					|| (holder = (ViewHolder) convertView.getTag()).flag != message
							.getDirection()) {

				holder = new ViewHolder();
				if (message.getDirection() == HeartMessage.MESSAGE_FROM) {
					holder.flag = HeartMessage.MESSAGE_FROM;

					convertView = LayoutInflater.from(HeartActivity.this).inflate(
							R.layout.heart_item_from, null);
				} else {
					holder.flag = HeartMessage.MESSAGE_TO;
					convertView = LayoutInflater.from(HeartActivity.this).inflate(
							R.layout.heart_item_to, null);
				}

				holder.text = (TextView) convertView
						.findViewById(R.id.heart_content_itv);
				convertView.setTag(holder);
			}
			holder.text.setText(message.getContent());

			return convertView;
		}

		// 优化listview的Adapter
		class ViewHolder {
			TextView text;
			int flag;
		}

	}
}