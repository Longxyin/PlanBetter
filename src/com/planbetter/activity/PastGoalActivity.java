package com.planbetter.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.planbetter.bean.GoalBean;
import com.planbetter.constant.GoalConstant;
import com.planbetter.dao.DatabaseUtil;
import com.planbetter.view.HistoryGoalGalleryFlow;

/**
 * 历史目标
 * 
 * @author Kelvin
 * 
 */
public class PastGoalActivity extends Activity {
	private ImageView popupView;
	private ImageView popupButton;
	HistoryGoalGalleryFlow galleryFlow;
	private PopupWindow popup;
	private List<Map<String, Object>> pastGoalItemList = null;
	private int imagesize = 0;
	private HistoryGoalAdapter adapter;
	private static final int REFRESH_PASTACITVITY = 1;
	private static final int REFRESH_ADAPTER = 2;
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what) {
			case REFRESH_PASTACITVITY:
				init();
				break;
			case REFRESH_ADAPTER:
				Map<String, Object> map = pastGoalItemList.get(msg.arg1);
				String id = map.get(GoalBean.ID).toString();
				Log.d("debug", "delete curViewId=" + id);
				DatabaseUtil.delete(PastGoalActivity.this, GoalBean.TABLE_NAME, GoalBean.ID+"="+id, null);
				Toast.makeText(PastGoalActivity.this, "目标删除成功",
						Toast.LENGTH_SHORT).show();
				pastGoalItemList.remove(msg.arg1);
				adapter.notifyDataSetChanged();
				break;
			}
		}
		
	};

	// 图片数组
	private Integer[] images = new Integer[] { R.drawable.img0002,
			R.drawable.img0003, R.drawable.img0004, R.drawable.img0005,
			R.drawable.img0006, R.drawable.img0007, R.drawable.img0008 };

	private void init() {
		setContentView(R.layout.past_goal_layout);
		imagesize = images.length;
		pastGoalItemList = new ArrayList<Map<String, Object>>();
		initPopupWindow();
		initPastGoalItemList();
		
		adapter = new HistoryGoalAdapter(this, images);
		adapter.createReflectedImages(); // 映射图片

		galleryFlow = (HistoryGoalGalleryFlow) findViewById(R.id.HistoryGoalGallery);
		galleryFlow.setAdapter(adapter);
		galleryFlow.setOnItemClickListener(new ItemClickListener()); // 添加点击事件
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	private void initPastGoalItemList() {
		
		Cursor pastGoalCur = DatabaseUtil.query(PastGoalActivity.this,
				GoalBean.TABLE_NAME, null, GoalBean.GOAL_FLAG + "="
						+ GoalConstant.FORMER_GOAL, null, null, null,
				GoalBean.ID + " ASC");

		// 遍历今天的任务信息

		int i = 0;
		for (pastGoalCur.moveToFirst(); !pastGoalCur.isAfterLast(); pastGoalCur
				.moveToNext()) {

			Map<String, Object> map1 = new HashMap<String, Object>();
			map1.put(GoalBean.GOAL_CONTENT,
					pastGoalCur.getString(pastGoalCur.getColumnIndex(GoalBean.GOAL_CONTENT)));
			map1.put(GoalBean.DATE, pastGoalCur.getString(pastGoalCur.getColumnIndex(GoalBean.DATE)));
			map1.put(GoalBean.ID, pastGoalCur.getInt(pastGoalCur.getColumnIndex(GoalBean.ID)));
			map1.put(GoalBean.GOAL_FLAG,
					pastGoalCur.getInt(pastGoalCur.getColumnIndex(GoalBean.GOAL_FLAG)));
			pastGoalItemList.add(i, map1);
			i++;
		}

		DatabaseUtil.closeDatabase();
	}

	private void initPopupWindow() {
		View view = LayoutInflater.from(PastGoalActivity.this).inflate(
				R.layout.past_goal_popwindow, null);
		popup = new PopupWindow(view,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		popup.setOutsideTouchable(true);
		
		popupView = (ImageView) view.findViewById(R.id.past_goal_popwindow_imageview);
		popupButton = (ImageView) view.findViewById(R.id.past_goal_popwindow_button);
	}

	@Override
	protected void onPause() {
		if (popup.isShowing()) {
			popup.dismiss();
		}

		super.onPause();
	}

	@Override
	protected void onStop() {
		if (popup.isShowing()) {
			popup.dismiss();
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (popup.isShowing()) {
			popup.dismiss();
		}
		super.onDestroy();
	}

	class ItemClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, final int position,
				long id) {
			Log.d("historygoal", "this one is on item long clicked!" + position);

			Bitmap newb = Bitmap.createBitmap(120, 200, Config.ARGB_8888);
			Canvas canvasTemp = new Canvas(newb);

			Paint pt = new Paint();
			canvasTemp.drawColor(Color.TRANSPARENT);
			pt.setColor(Color.WHITE);
			pt.setAntiAlias(true);

			Map<String, Object> map = pastGoalItemList.get(position);
            String content = (String)map.get(GoalBean.GOAL_CONTENT);
            String date = (String)map.get(GoalBean.DATE);
		
            pt.setTextSize(10);
			canvasTemp.drawText(date, 10, 30, pt);
			
			pt.setTextSize(20);
			int contentLength = content.length();
			int x = contentLength/5 + 1;
			for(int i=0; i<x; i++)
			{
				String s1;
				if(contentLength < i*5+5)
					s1 = content.substring(i*5, contentLength);
				else
					s1 = content.substring(i*5, i*5+5);
				
				canvasTemp.drawText(s1, 10, 60+i*40, pt);
			}

			popupView.setImageBitmap(newb);
		
			popupView.setBackgroundResource((images[position%imagesize]));
			popupView.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					if (popup.isShowing()) {
						popup.dismiss();
					}
				}
			});
			
			popupButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					if (popup.isShowing()) {
						popup.dismiss();
					}
					showCustomMessage("删除目标", "确认删除此目标?", position);
				}
			});
			
			popup.showAtLocation(galleryFlow, Gravity.CENTER, 0, 0);
		}
	}
	
	private void showCustomMessage(String pTitle, final String pMsg, int position) {
		final int ps = position;
		final Dialog lDialog = new Dialog(PastGoalActivity.this,
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
						
						/*popwindow消失*/
						lDialog.dismiss();
						// 删除活动
						Message mes = new Message();
						mes.what = REFRESH_ADAPTER;
						mes.arg1 = ps;
						handler.sendMessage(mes);
					}
				});
		lDialog.show();
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Message mes = new Message();
		mes.what = REFRESH_PASTACITVITY;
		handler.sendMessage(mes);
	}

	private class HistoryGoalAdapter extends BaseAdapter {
		private Context mContext;
		private Integer[] mImageIds;
		private Bitmap[] mImages;

		public HistoryGoalAdapter(Context c, Integer[] ImageIds) {
			mContext = c;
			mImageIds = ImageIds;
			mImages = new Bitmap[mImageIds.length];
		}

		public boolean createReflectedImages() {
			final int reflectionGap = 4;
			int index = 0;

			for (int imageId : mImageIds) {
				Bitmap originalImage = BitmapFactory.decodeResource(
						mContext.getResources(), imageId);
				int width = originalImage.getWidth();
				int height = originalImage.getHeight();

				Matrix matrix = new Matrix();
				matrix.preScale(1, -1);

				Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
						height / 2, width, height / 2, matrix, false);

				Bitmap bitmapWithReflection = Bitmap.createBitmap(width / 2,
						height / 2, Config.ARGB_8888);

				Canvas canvas = new Canvas(bitmapWithReflection);

				canvas.drawBitmap(originalImage, 0, 0, null);

				Paint deafaultPaint = new Paint();
				canvas.drawRect(0, height, width, height + reflectionGap,
						deafaultPaint);

				canvas.drawBitmap(reflectionImage, 0, height + reflectionGap,
						null);

				Paint paint = new Paint();
				LinearGradient shader = new LinearGradient(0,
						originalImage.getHeight(), 0,
						bitmapWithReflection.getHeight() + reflectionGap,
						0x70ffffff, 0x00ffffff, TileMode.CLAMP);

				paint.setShader(shader);

				paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

				canvas.drawRect(0, height, width,
						bitmapWithReflection.getHeight() + reflectionGap, paint);

				mImages[index++] = bitmapWithReflection;
				
				
			}
			return true;
		}

		public int getCount() {
			return pastGoalItemList.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return Integer.parseInt(pastGoalItemList.get(position).get(GoalBean.ID).toString());
		}

		public View getView(int position, View convertView, ViewGroup parent) {				
			ImageView iv = new ImageView(mContext);
			Bitmap newb = mImages[position%imagesize];

			Canvas canvasTemp = new Canvas(newb);
			Paint pt = new Paint();
			canvasTemp.drawColor(Color.TRANSPARENT);

			pt.setColor(Color.WHITE);
			pt.setTypeface(null);
			pt.setAntiAlias(true);
			pt.setShader(null);
			pt.setFakeBoldText(true);
		
			Map<String, Object> map = pastGoalItemList.get(position);
            String content = (String)map.get(GoalBean.GOAL_CONTENT);
            String date = (String)map.get(GoalBean.DATE);
		
            pt.setTextSize(30);
			canvasTemp.drawText(date, 30, 60, pt);
			
			pt.setTextSize(40);
			int contentLength = content.length();
			int x = contentLength/5 + 1;
			for(int i=0; i<x; i++)
			{
				String s1;
				if(contentLength < i*5+5)
					s1 = content.substring(i*5, contentLength);
				else
					s1 = content.substring(i*5, i*5+5);
				
				canvasTemp.drawText(s1, 30, 100+i*50, pt);
			}
			
			iv.setImageBitmap(newb);

			iv.setAdjustViewBounds(true);

			iv.setLayoutParams(new HistoryGoalGalleryFlow.LayoutParams(
							180, 240));
			return iv;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			if (popup.isShowing()) {
				popup.dismiss();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
