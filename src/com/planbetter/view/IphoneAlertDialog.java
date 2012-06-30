package com.planbetter.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.planbetter.activity.R;

public class IphoneAlertDialog {

	public static void showCustomMessage(Context context, String pTitle, final String pMsg) {
		final Dialog lDialog = new Dialog(context,
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
					}
				});
		lDialog.show();

	}
}
