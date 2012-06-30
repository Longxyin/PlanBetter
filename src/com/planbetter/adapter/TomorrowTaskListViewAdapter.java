package com.planbetter.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.planbetter.activity.R;
import com.planbetter.view.AlwaysMarqueeTextView;
import com.planbetter.view.IphoneAlertDialog;

public class TomorrowTaskListViewAdapter extends BaseAdapter {

	private Context context;
	private List<Map<String,Object>> taskListItems; //任务列表信息
	private LayoutInflater layoutInflater;
	
	static final class TaskListItemView {
		public AlwaysMarqueeTextView taskInfo; //任务内容
		public ImageView timeAlert; //时间提醒图片
		public ImageView positionAlert; //地点提醒图片
		public AlwaysMarqueeTextView taskTimeAndPositionInfo; //任务完成时间和地点信息
		public Button taskEditBtn; //任务编辑按钮
		public Button taskDeleteBtn; //任务删除按钮
		public Button taskPullBtn; //任务提前按钮
		public ImageView taskStarRank; //任务优先级图片
	}
	
	public TomorrowTaskListViewAdapter(Context context, List<Map<String,Object>> taskListItems) {
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);
		this.taskListItems = taskListItems;
	}
	
	public int getCount() {
		// TODO Auto-generated method stub
		return taskListItems.size();
	}

	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return taskListItems.get(arg0);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		TaskListItemView listItemView = null;
		if (convertView == null) {
			listItemView = new TaskListItemView();
			convertView = layoutInflater.inflate(R.layout.tomorrow_task_item, null);
			// 获取控件对象
//			listItemView.taskInfo = (AlwaysMarqueeTextView) convertView
//					.findViewById(R.id.tomorrow_task_info);
//			listItemView.timeAlert = (ImageView) convertView
//					.findViewById(R.id.tomorrow_time_alert);
//			listItemView.positionAlert = (ImageView) convertView
//					.findViewById(R.id.tomorrow_position_alert);
//			listItemView.taskTimeAndPositionInfo = (AlwaysMarqueeTextView) convertView
//					.findViewById(R.id.tomorrow_task_time_position_textview);
//			listItemView.taskEditBtn = (Button) convertView
//					.findViewById(R.id.tomorrow_task_edit_btn);
//			listItemView.taskDeleteBtn = (Button) convertView
//					.findViewById(R.id.tomorrow_task_delete_btn);
//			listItemView.taskPullBtn = (Button) convertView
//					.findViewById(R.id.tomorrow_task_pull_btn);
//			listItemView.taskStarRank = (ImageView) convertView
//					.findViewById(R.id.tomorrow_star_rank);
			// 设置控件集到convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (TaskListItemView) convertView.getTag();
		}

		// 设置任务名称
		listItemView.taskInfo.setText((String)taskListItems.get(position).get("taskInfo"));
		
		// 设置任务完成时间和地点信息
		String timeAndPosition = "时间："+(String)taskListItems.get(position).get("time")+" 地点："+(String)taskListItems.get(position).get("position");
		listItemView.taskTimeAndPositionInfo.setText(timeAndPosition);
		
		// 设置时间提醒图片是否显示  timeAlert
		
		// 设置地点提醒图片是否显示  positionAlert
		
		// 设置优先级图片  taskStarRank
		switch(Integer.parseInt(taskListItems.get(position).get("taskRank").toString())) {
		case 1:
			listItemView.taskStarRank.setBackgroundResource(R.drawable.star1);
			break;
		case 2:
			listItemView.taskStarRank.setBackgroundResource(R.drawable.star2);
			break;
		case 3:
			listItemView.taskStarRank.setBackgroundResource(R.drawable.star3);
			break;
		case 4:
			listItemView.taskStarRank.setBackgroundResource(R.drawable.star4);
			break;
		}

		//任务编辑按钮
		listItemView.taskEditBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(context, "您按下了编辑按钮", Toast.LENGTH_SHORT).show();
			}
		});
		
		//任务删除按钮
		listItemView.taskDeleteBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Toast.makeText(context, "您按下了删除按钮", Toast.LENGTH_SHORT).show();
				IphoneAlertDialog.showCustomMessage(context, "确认删除", "确认删除此任务？");
			}
		});
		
		//任务提前按钮
		listItemView.taskPullBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(context, "您按下了提前按钮", Toast.LENGTH_SHORT).show();
				
			}
		});
		
		return convertView;
	}

}
