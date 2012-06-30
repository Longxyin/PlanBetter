package com.planbetter.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.planbetter.constant.MenuItemId;

public class GoalActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.goal_layout);
		setTitle(R.string.goal_title);
		
		Resources res = getResources();  
        TabHost tabHost = getTabHost();   
        TabHost.TabSpec spec;   
        Intent intent;   
        
        intent = new Intent().setClass(this, CurrentGoalActivity.class);

        spec = tabHost.newTabSpec("当前目标").setIndicator("当前目标",
                          res.getDrawable(android.R.drawable.ic_menu_myplaces))
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, PastGoalActivity.class);
        spec = tabHost.newTabSpec("历史目标").setIndicator("历史目标",
                          res.getDrawable(android.R.drawable.ic_menu_agenda))
                      .setContent(intent);
        tabHost.addTab(spec);

        View view1 = this.getTabWidget().getChildAt(0);   
        ((TextView)view1.findViewById(android.R.id.title)).setTextSize(12);//设置字体   
        ((ImageView)view1.findViewById(android.R.id.icon)).setPadding(0, -5, 0, 0);//设置补白系数 
        
        View view2 = this.getTabWidget().getChildAt(1);   
        ((TextView)view2.findViewById(android.R.id.title)).setTextSize(12); 
        ((ImageView)view2.findViewById(android.R.id.icon)).setPadding(0, -5, 0, 0); 
        
        tabHost.setCurrentTab(0);
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId())
		{
		//目标
		case MenuItemId.MENU_ITEM_TASK:
			Intent tg_intent = new Intent();
			tg_intent.setClass(GoalActivity.this, TaskActivity.class);
			startActivity(tg_intent);
			break;
		//历史
		case MenuItemId.MENU_ITEM_HISTORY:
			Intent th_intent = new Intent();
			th_intent.setClass(GoalActivity.this, HistoryActivity.class);
			startActivity(th_intent);
			break;
		//心语
		case MenuItemId.MENU_ITEM_TIPS:
			Intent tt_intent = new Intent();
			tt_intent.setClass(GoalActivity.this, HeartActivity.class);
			startActivity(tt_intent);
			break;
			//设置
		case MenuItemId.MENU_ITEM_SETUP:
			Intent ti_intent = new Intent();
			ti_intent.setClass(GoalActivity.this, SetupActivity.class);
			startActivity(ti_intent);
			break;
			//帮助
		case MenuItemId.MENU_ITEM_HELP:
			Intent tx_intent = new Intent();
			tx_intent.setClass(GoalActivity.this, HelpActivity.class);
			startActivity(tx_intent);
			break;
			//关于
		case MenuItemId.MENU_ITEM_ABOUT:
			Intent ty_intent = new Intent();
			ty_intent.setClass(GoalActivity.this, AboutActivity.class);
			startActivity(ty_intent);
			break;
			//退出
		}
		return true;
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuItem menu_task = menu.add(0, MenuItemId.MENU_ITEM_TASK, 0,
				R.string.menu_task);
		menu_task.setIcon(android.R.drawable.ic_menu_agenda);

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
}
