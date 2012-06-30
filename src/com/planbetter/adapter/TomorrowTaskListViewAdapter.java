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
	private List<Map<String,Object>> taskListItems; //�����б���Ϣ
	private LayoutInflater layoutInflater;
	
	static final class TaskListItemView {
		public AlwaysMarqueeTextView taskInfo; //��������
		public ImageView timeAlert; //ʱ������ͼƬ
		public ImageView positionAlert; //�ص�����ͼƬ
		public AlwaysMarqueeTextView taskTimeAndPositionInfo; //�������ʱ��͵ص���Ϣ
		public Button taskEditBtn; //����༭��ť
		public Button taskDeleteBtn; //����ɾ����ť
		public Button taskPullBtn; //������ǰ��ť
		public ImageView taskStarRank; //�������ȼ�ͼƬ
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
			// ��ȡ�ؼ�����
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
			// ���ÿؼ�����convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (TaskListItemView) convertView.getTag();
		}

		// ������������
		listItemView.taskInfo.setText((String)taskListItems.get(position).get("taskInfo"));
		
		// �����������ʱ��͵ص���Ϣ
		String timeAndPosition = "ʱ�䣺"+(String)taskListItems.get(position).get("time")+" �ص㣺"+(String)taskListItems.get(position).get("position");
		listItemView.taskTimeAndPositionInfo.setText(timeAndPosition);
		
		// ����ʱ������ͼƬ�Ƿ���ʾ  timeAlert
		
		// ���õص�����ͼƬ�Ƿ���ʾ  positionAlert
		
		// �������ȼ�ͼƬ  taskStarRank
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

		//����༭��ť
		listItemView.taskEditBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(context, "�������˱༭��ť", Toast.LENGTH_SHORT).show();
			}
		});
		
		//����ɾ����ť
		listItemView.taskDeleteBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Toast.makeText(context, "��������ɾ����ť", Toast.LENGTH_SHORT).show();
				IphoneAlertDialog.showCustomMessage(context, "ȷ��ɾ��", "ȷ��ɾ��������");
			}
		});
		
		//������ǰ��ť
		listItemView.taskPullBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(context, "����������ǰ��ť", Toast.LENGTH_SHORT).show();
				
			}
		});
		
		return convertView;
	}

}
