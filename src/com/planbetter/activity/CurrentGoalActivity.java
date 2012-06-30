package com.planbetter.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.planbetter.bean.GoalBean;
import com.planbetter.constant.GoalConstant;
import com.planbetter.dao.DatabaseUtil;
import com.planbetter.date.DateUtils;

/**
 * 当前目标
 * 
 * @author Kelvin
 * 
 */
public class CurrentGoalActivity extends Activity {

	private ListView goalListView = null;
	private List<Map<String, Object>> goalItemList = null;
	private GoalListViewAdapter goalAdapter = null;
	private Cursor goalCur;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.current_goal_layout);

		initGoalAdapter();
		goalListView = (ListView) findViewById(R.id.current_goal_listview);
		goalListView.setAdapter(goalAdapter);

	}

	private void initGoalAdapter() {
		initGoalItemList();
		goalAdapter = new GoalListViewAdapter();
	}

	private void initGoalItemList() {
		goalItemList = new ArrayList<Map<String, Object>>();

		String firstGoal = "";
		String secondGoal = "";
		String thirdGoal = "";
		
		goalCur = DatabaseUtil.query(CurrentGoalActivity.this, GoalBean.TABLE_NAME, new String[]{GoalBean.GOAL_CONTENT,GoalBean.GOAL_FLAG}, 
				GoalBean.GOAL_FLAG+"!="+GoalConstant.FORMER_GOAL, null, null, null, GoalBean.GOAL_FLAG+" ASC");
		
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
			}
			
		}
		DatabaseUtil.closeDatabase();
		
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put(GoalBean.GOAL_CONTENT, firstGoal);
		map1.put(GoalBean.GOAL_FLAG, GoalConstant.RANK_FIRST);
		goalItemList.add(0, map1);
		
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put(GoalBean.GOAL_CONTENT, secondGoal);
		map2.put(GoalBean.GOAL_FLAG, GoalConstant.RANK_SECOND);
		goalItemList.add(1, map2);
		
		Map<String, Object> map3 = new HashMap<String, Object>();
		map3.put(GoalBean.GOAL_CONTENT, thirdGoal);
		map3.put(GoalBean.GOAL_FLAG, GoalConstant.RANK_THIRD);
		goalItemList.add(2, map3);
	}

	public class GoalListViewAdapter extends BaseAdapter {	

		private View popupView;
		private PopupWindow popup;

		private Button goalEditBtn;
		private Button goalClearBtn;

		private int goalFlag = GoalConstant.FORMER_GOAL;
		private int curPosition = 0;
		private boolean isEmpty = true;

		final class GoalListItemView {
			public ImageView goalRankImg; // 目标级别图片
			public TextView goalContent; // 目标内容描述
			public ImageView myCursor; // 箭头图片
		}

		public GoalListViewAdapter() {
			initPopupWindow();
		}

		private void initPopupWindow() {
			popupView = LayoutInflater.from(CurrentGoalActivity.this).inflate(
					R.layout.goal_edit_popup_window, null);
			popup = new PopupWindow(popupView,
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			popup.setOutsideTouchable(true);
			goalEditBtn = (Button) popupView.findViewById(R.id.goal_edit_btn);
			goalClearBtn = (Button) popupView.findViewById(R.id.goal_clear_btn);
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return goalItemList.size();
		}

		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return goalItemList.get(arg0);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return Integer.parseInt(goalItemList.get(position).get(GoalBean.GOAL_FLAG).toString());
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			GoalListItemView listItemView = null;
			if (convertView == null) {
				listItemView = new GoalListItemView();
				convertView = LayoutInflater.from(CurrentGoalActivity.this)
						.inflate(R.layout.goal_item, null);
				// 获取控件对象
				listItemView.goalContent = (TextView) convertView
						.findViewById(R.id.goal_content_textview);
				listItemView.goalRankImg = (ImageView) convertView
						.findViewById(R.id.goal_rank_img);
				listItemView.myCursor = (ImageView) convertView
						.findViewById(R.id.myCursor);
				// 设置控件集到convertView
				convertView.setTag(listItemView);
			} else {
				listItemView = (GoalListItemView) convertView.getTag();
			}

			// 设置文字
			listItemView.goalContent.setText(goalItemList.get(position)
					.get(GoalBean.GOAL_CONTENT).toString());

			// 设置优先级图片 goalRank
			switch (Integer.parseInt(goalItemList.get(position)
					.get(GoalBean.GOAL_FLAG).toString())) {
			case GoalConstant.RANK_FIRST:
				listItemView.goalRankImg
						.setBackgroundResource(R.drawable.goal1);
				break;
			case GoalConstant.RANK_SECOND:
				listItemView.goalRankImg
						.setBackgroundResource(R.drawable.goal2);
				break;
			case GoalConstant.RANK_THIRD:
				listItemView.goalRankImg
						.setBackgroundResource(R.drawable.goal3);
				break;
			}

			listItemView.myCursor.setOnClickListener(new OnClickListener() {
				
				private View editGoalDialogView;
				private EditText goalEditText;
				
				@Override
				public void onClick(View view) {
					// TODO Auto-generated method stub
					curPosition = position;
					goalFlag = position + 1;
					final String goalContent = goalItemList.get(position).get(GoalBean.GOAL_CONTENT).toString();
					isEmpty = goalContent.equals("") ? true : false;
					Log.d("debug", "curPosition="+position);
					
					if (popup.isShowing()) {
						popup.dismiss();
					} else {
						if(isEmpty) {
							goalClearBtn.setBackgroundResource(R.drawable.goal_empty);
						} else {
							goalClearBtn.setBackgroundResource(R.drawable.goal_full);
						}
						((LinearLayout) (popupView
								.findViewById(R.id.goal_edit_popup_view)))
								.setBackgroundResource(R.drawable.popupview_down);
						popup.setAnimationStyle(R.anim.popup_window_bottom);
						popup.update();
						popup.showAsDropDown(view);
						goalEditBtn.startAnimation(AnimationUtils.loadAnimation(
								CurrentGoalActivity.this, R.anim.my_scale_action));
						goalClearBtn.startAnimation(AnimationUtils.loadAnimation(
								CurrentGoalActivity.this, R.anim.my_scale_action));

						goalEditBtn.setOnClickListener(new OnClickListener() {

							public void onClick(View v) {
								// TODO Auto-generated method stub
								popup.dismiss();
								editGoalDialogView = LayoutInflater.from(
										CurrentGoalActivity.this).inflate(
										R.layout.edit_goal_dialog, null);
								goalEditText = (EditText) editGoalDialogView
										.findViewById(R.id.edit_goal_dialog_et);
								if(!isEmpty) {
									goalEditText.setText(goalContent);
								}
								new AlertDialog.Builder(CurrentGoalActivity.this)
										.setTitle(R.string.edit_goal_dialog_title)
										.setIcon(android.R.drawable.ic_menu_edit)
										.setView(editGoalDialogView)
										.setPositiveButton(
												R.string.edit_goal_dialog_ok,
												new DialogInterface.OnClickListener() {

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														// TODO Auto-generated
														// method stub
														// 点击保存按钮
														// 取出内容
														String content = goalEditText
																.getText()
																.toString()
																.trim()
																.replaceAll("\r",
																		"")
																.replaceAll("\t",
																		"")
																.replaceAll("\n",
																		"")
																.replaceAll("\f",
																		"");
														if (content.equals("")) {
															Toast.makeText(CurrentGoalActivity.this, "目标不能为空", Toast.LENGTH_SHORT).show();
														} else {
															//保存内容
															ContentValues values = new ContentValues();
															values.put(GoalBean.GOAL_CONTENT,content);
															values.put(GoalBean.DATE,DateUtils.now());
															values.put(GoalBean.GOAL_FLAG,goalFlag);
															Log.d("debug","goalFlag="+goalFlag+" curPosition="+curPosition);
															if(isEmpty) {
																//插入到数据库
																long id = DatabaseUtil.insert(CurrentGoalActivity.this, GoalBean.TABLE_NAME, GoalBean.ID, values);
																if(id != -1) {
																	//更新listview
																	Map<String, Object> map = goalItemList.get(curPosition);
																	map.put(GoalBean.GOAL_CONTENT, content);
																	goalItemList.set(curPosition, map);
																	goalAdapter.notifyDataSetChanged();
																	Toast.makeText(CurrentGoalActivity.this, "您的目标已经确定了,要好好努力哦", Toast.LENGTH_SHORT).show();
																} else {
																	Toast.makeText(CurrentGoalActivity.this, "数据库插入数据失败", Toast.LENGTH_SHORT).show();
																}
															} else {
																//修改并更新
																ContentValues former = new ContentValues();
																former.put(GoalBean.GOAL_FLAG, GoalConstant.FORMER_GOAL);
																int rows = DatabaseUtil.update(CurrentGoalActivity.this, GoalBean.TABLE_NAME, former, 
																		GoalBean.GOAL_FLAG+"="+goalFlag, null);
																long id = DatabaseUtil.insert(CurrentGoalActivity.this, GoalBean.TABLE_NAME, GoalBean.ID, values);
																if(id != -1 && rows > 0) {
																	Map<String, Object> map = goalItemList.get(curPosition);
																	map.put(GoalBean.GOAL_CONTENT, content);
																	goalItemList.set(curPosition, map);
																	goalAdapter.notifyDataSetChanged();
																	Toast.makeText(CurrentGoalActivity.this, "您的目标已经重新确立了,先前的目标可以在历史目标中查看", Toast.LENGTH_SHORT).show();
																} else {
																	Toast.makeText(CurrentGoalActivity.this, "数据库更新数据失败", Toast.LENGTH_SHORT).show();
																}
															}
														}
													}
												})
										.setNegativeButton(
												R.string.edit_goal_dialog_cancel,
												new DialogInterface.OnClickListener() {

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														// TODO Auto-generated
														// method stub

													}
												}).create().show();

							}
						});

						goalClearBtn.setOnClickListener(new OnClickListener() {

							public void onClick(View v) {
								// TODO Auto-generated method stub
								popup.dismiss();
								showCustomMessage("确认删除","确认删除该目标?");
							}
						});
					}
				}
			});

			return convertView;
		}
		
		private void showCustomMessage(String pTitle, final String pMsg) {
			final Dialog lDialog = new Dialog(CurrentGoalActivity.this,
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
							//清空目标
							if(isEmpty) {
								//如果为空
								Toast.makeText(CurrentGoalActivity.this, "目标为空无法删除", Toast.LENGTH_SHORT).show();
							} else {
								//如果不为空
								ContentValues values = new ContentValues();
								values.put(GoalBean.GOAL_FLAG, GoalConstant.FORMER_GOAL);
								int rows = DatabaseUtil.update(CurrentGoalActivity.this,GoalBean.TABLE_NAME,values,GoalBean.GOAL_FLAG+"="+goalFlag,null);
								if(rows > 0) {
									//刷新listview
									Map<String, Object> map = goalItemList.get(curPosition);
									map.put(GoalBean.GOAL_CONTENT, "");
									goalItemList.set(curPosition, map);
									goalAdapter.notifyDataSetChanged();
									Toast.makeText(CurrentGoalActivity.this, "目标删除成功,可在历史目标中查看", Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(CurrentGoalActivity.this, "数据库更新字段失败", Toast.LENGTH_SHORT).show();
								}
							}
						}
					});
			lDialog.show();

		}

	}
	
}
