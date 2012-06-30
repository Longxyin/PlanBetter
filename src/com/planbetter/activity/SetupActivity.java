package com.planbetter.activity;

import java.util.Date;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.planbetter.constant.GMailConsts;
import com.planbetter.mail.GMailService.GMailSyncState;
import com.planbetter.mail.PrefStore;
import com.planbetter.mail.GMailService;

public class SetupActivity extends PreferenceActivity implements OnPreferenceChangeListener{
	private static final int DIALOG_MISSING_CREDENTIALS = 1;
//    private static final int DIALOG_FIRST_SYNC = 2;  
    private static final int DIALOG_SYNC_DATA_RESET = 3;    
    private static final int DIALOG_INVALID_IMAP_FOLDER = 4;
    private static final int DIALOG_NEED_FIRST_MANUAL_SYNC = 5;

	private StatusPreference mStatusPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.main_screen);
		PreferenceManager prefMgr = getPreferenceManager();

		mStatusPref = new StatusPreference(this);
		mStatusPref.setSelectable(false);

		int sdkLevel = 1;
        try {
            sdkLevel = Integer.parseInt(Build.VERSION.SDK);
        } catch (NumberFormatException nfe) {
            // ignore (assume sdkLevel == 1)
        }

        if (sdkLevel < 3) {
            // Older versions don't show the title bar for PreferenceActivity
            PreferenceCategory cat = new PreferenceCategory(this);
            cat.setOrder(0);
            getPreferenceScreen().addPreference(cat);
            cat.setTitle(R.string.ui_status_label);
            cat.addPreference(mStatusPref);
        } else {
            // Newer SDK version show the title bar for PreferenceActivity
            mStatusPref.setOrder(0);
            getPreferenceScreen().addPreference(mStatusPref);
        }
        
	    Preference pref = prefMgr.findPreference(PrefStore.PREF_LOGIN_USER);
	    pref.setOnPreferenceChangeListener(this);
	        
	    pref = prefMgr.findPreference(PrefStore.PREF_IMAP_FOLDER);
	    pref.setOnPreferenceChangeListener(this);

	    pref = prefMgr.findPreference(PrefStore.PREF_LOGIN_PASSWORD);
	    pref.setOnPreferenceChangeListener(this);
	        
	    pref = prefMgr.findPreference(PrefStore.PREF_MAX_ITEMS_PER_SYNC);
	    pref.setOnPreferenceChangeListener(this);

	}

	@Override
    protected void onPause() {
        super.onPause();
        GMailService.unsetStateChangeListener();
    }
	
	@Override
	protected void onResume() {
		 super.onResume();
		 GMailService.setStateChangeListener(mStatusPref);
	     updateUsernameLabelFromPref();
	     updateImapFolderLabelFromPref();
         updateMaxItemsPerSync(null);
	}

	/*更新用戶名*/
	private void updateUsernameLabelFromPref() {
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        String username = prefs.getString(PrefStore.PREF_LOGIN_USER,
                getString(R.string.ui_login_label));
        Preference pref = getPreferenceManager().findPreference(PrefStore.PREF_LOGIN_USER);
        pref.setTitle(username);
    }
    
	/*更新發送時的文件夾名*/
    private void updateImapFolderLabelFromPref() {
        String imapFolder = PrefStore.getImapFolder(this);
        Preference pref = getPreferenceManager().findPreference(PrefStore.PREF_IMAP_FOLDER);
        pref.setTitle(imapFolder);
    }
    
   
	/*是否內容初始化完毕*/
	private boolean initiateSync() {
        if (!PrefStore.isLoginInformationSet(this)) {
            showDialog(DIALOG_MISSING_CREDENTIALS);
            return false;
        } else {
            startSync();
            return true;
        }
    }
    
	/*开始同步*/
    private void startSync() {
        Intent intent = new Intent(this, GMailService.class);
        startService(intent);
    }

    private class StatusPreference extends Preference implements
            GMailService.StateChangeListener, OnClickListener {
    	
        private View mView;
        private Button mSyncButton;
        private ImageView mStatusIcon;        
        private TextView mStatusLabel;
        private View mSyncDetails;      
        private TextView mErrorDetails;       
        private TextView mSyncDetailsLabel;        
        private ProgressBar mProgressBar;        
        private ProgressBar mProgressBarIndet;
        
        public StatusPreference(Context context) {
            super(context);
        }

        public void update() {
            stateChanged(GMailService.getState(), GMailService.getState());
        }

        @Override
        public void stateChanged(final GMailSyncState oldState,final GMailSyncState newState) {
            if (mView != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int STATUS_IDLE = 0;
                        int STATUS_WORKING = 1;
                        int STATUS_DONE = 2;
                        int STATUS_ERROR = 3;
                        int status = -1;
                        
                        CharSequence statusLabel = null;
                        String statusDetails = null;
                        boolean progressIndeterminate = false;
                        int progressMax = 1;
                        int progressVal = 0;
                        
                        switch (newState) {
                            case AUTH_FAILED:
                                statusLabel = getText(R.string.status_auth_failure);
                                statusDetails = getString(R.string.status_auth_failure_details);
                                status = STATUS_ERROR;
                                break;
                            case CALC:
                                statusLabel = getText(R.string.status_calc);
                                statusDetails = getString(R.string.status_calc_details);
                                progressIndeterminate = true;
                                status = STATUS_WORKING;
                                break;
                            case IDLE:
                                if (oldState == GMailSyncState.SYNC
                                        || oldState == GMailSyncState.CALC) {
                                    statusLabel = getText(R.string.status_done);
                                    int backedUpCount = GMailService.getCurrentSyncedItems();
                                    progressMax = GMailService.getItemsToSyncCount();
                                    progressVal = backedUpCount;
                                    if (backedUpCount ==
                                            PrefStore.getMaxItemsPerSync(SetupActivity.this)) {
                                        // Maximum msg per sync reached.
                                        statusDetails = getResources().getString(
                                                R.string.status_done_details_max_per_sync,
                                                backedUpCount);
                                    } else if (backedUpCount > 0) {
                                        statusDetails = getResources().getQuantityString(
                                                R.plurals.status_done_details, backedUpCount,
                                                backedUpCount);
                                    } else {
                                        statusDetails = getString(
                                                R.string.status_done_details_noitems);
                                        progressMax = 1;
                                        progressVal = 1;
                                    }
                                    
                                    progressIndeterminate = false;
                                    
                                    status = STATUS_DONE;
                                } else {
                                    statusLabel = getText(R.string.status_idle);
                                    long lastSync = PrefStore.getLastSync(SetupActivity.this);
                                    String lastSyncStr;
                                    if (lastSync == PrefStore.DEFAULT_LAST_SYNC) {
                                        lastSyncStr = 
                                            getString(R.string.status_idle_details_never);
                                    } else {
                                        lastSyncStr = new Date(lastSync).toLocaleString();
                                    }
                                    statusDetails = getString(R.string.status_idle_details,
                                            lastSyncStr);
                                    status = STATUS_IDLE;
                                }
                                break;
                            case LOGIN:
                                statusLabel = getText(R.string.status_login);
                                statusDetails = getString(R.string.status_login_details);
                                progressIndeterminate = true;
                                status = STATUS_WORKING;
                                break;
                            case SYNC:
                                statusLabel = getText(R.string.status_sync);
                                statusDetails = getString(R.string.status_sync_details,
                                        GMailService.getCurrentSyncedItems(),
                                        GMailService.getItemsToSyncCount());
                                progressMax = GMailService.getItemsToSyncCount();
                                progressVal = GMailService.getCurrentSyncedItems();
                                status = STATUS_WORKING;
                                break;
                            case GENERAL_ERROR:
                                statusLabel = getString(R.string.status_unknown_error);
                                statusDetails = getString(R.string.status_unknown_error_details,
                                        GMailService.getErrorDescription());
                                status = STATUS_ERROR;
                                break;
                            case CANCELED:
                                statusLabel = getString(R.string.status_canceled);
                                statusDetails = getString(R.string.status_canceled_details,
                                        GMailService.getCurrentSyncedItems(),
                                        GMailService.getItemsToSyncCount());
                                status = STATUS_IDLE;
                        } // switch (newStatus) { ... }

                        
                        int color;
                        TextView detailTextView;
                        int syncButtonText;
                        int icon;
                        
                        if (status == STATUS_IDLE) {
                            color = R.color.status_idle;
                            detailTextView = mSyncDetailsLabel;
                            syncButtonText = R.string.ui_sync_button_label_idle;
                            icon = R.drawable.ic_idle;
                        } else if (status == STATUS_WORKING) {
                            color = R.color.status_sync;
                            detailTextView = mSyncDetailsLabel;
                            syncButtonText = R.string.ui_sync_button_label_syncing;
                            icon = R.drawable.ic_syncing;
                        } else if (status == STATUS_DONE) {
                            color = R.color.status_done;
                            detailTextView = mSyncDetailsLabel;
                            syncButtonText = R.string.ui_sync_button_label_done;
                            icon = R.drawable.ic_done;
                        } else if (status == STATUS_ERROR) {
                            color = R.color.status_error;
                            detailTextView = mErrorDetails;
                            syncButtonText = R.string.ui_sync_button_label_error;
                            icon = R.drawable.ic_error;
                        } else {
                            Log.w(GMailConsts.TAG, "Illegal state: Unknown status.");
                            return;
                        }
                        
                        if (status != STATUS_ERROR) {
                            mSyncDetails.setVisibility(View.VISIBLE);
                            mErrorDetails.setVisibility(View.INVISIBLE);
                            if (progressIndeterminate) {
                                mProgressBarIndet.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(View.GONE);
                            } else {
                                mProgressBar.setVisibility(View.VISIBLE);
                                mProgressBarIndet.setVisibility(View.GONE);
                                mProgressBar.setIndeterminate(progressIndeterminate);
                                mProgressBar.setMax(progressMax);
                                mProgressBar.setProgress(progressVal); 
                            }
                            
                        } else {
                            mErrorDetails.setVisibility(View.VISIBLE);
                            mSyncDetails.setVisibility(View.INVISIBLE);
                        }
                        
                        mStatusLabel.setText(statusLabel);
                        mStatusLabel.setTextColor(getResources().getColor(color));
                        mSyncButton.setText(syncButtonText);
                        mSyncButton.setEnabled(true);
                        detailTextView.setText(statusDetails);
                        mStatusIcon.setImageResource(icon);
                        
                    } 
                });
            } 
        }

        @Override
        public void onClick(View v) {
            if (v == mSyncButton) {
                if (!GMailService.isWorking()) {
                    initiateSync();
                } else {
                    GMailService.cancel();
                    // Sync button will be restored on next status update.
                    mSyncButton.setText(R.string.ui_sync_button_label_canceling);
                    mSyncButton.setEnabled(false);
                }
            }
        }

        @Override
        public View getView(View convertView, ViewGroup parent) {
            if (mView == null) {
                mView = getLayoutInflater().inflate(R.layout.status, parent, false);
                mSyncButton = (Button) mView.findViewById(R.id.sync_button);
                mSyncButton.setOnClickListener(this);
                mStatusIcon = (ImageView) mView.findViewById(R.id.status_icon);
                mStatusLabel = (TextView) mView.findViewById(R.id.status_label);
                mSyncDetails = mView.findViewById(R.id.details_sync);
                mSyncDetailsLabel = (TextView) mSyncDetails.findViewById(R.id.details_sync_label);
                mProgressBar = (ProgressBar) mSyncDetails.findViewById(R.id.details_sync_progress);
                mProgressBarIndet =
                    (ProgressBar) mSyncDetails.findViewById(R.id.details_sync_progress_indet);
                mErrorDetails = (TextView) mView.findViewById(R.id.details_error);
                update();
            }
            return mView;
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        String title;
        String msg;
        Builder builder;
        switch (id) {
            case DIALOG_MISSING_CREDENTIALS:
                title = getString(R.string.ui_dialog_missing_credentials_title);
                msg = getString(R.string.ui_dialog_missing_credentials_msg);
                break;
            case DIALOG_SYNC_DATA_RESET:
                title = getString(R.string.ui_dialog_sync_data_reset_title);
                msg = getString(R.string.ui_dialog_sync_data_reset_msg);
                break;
            case DIALOG_INVALID_IMAP_FOLDER:
                title = getString(R.string.ui_dialog_invalid_imap_folder_title);
                msg = getString(R.string.ui_dialog_invalid_imap_folder_msg);
                break;
            case DIALOG_NEED_FIRST_MANUAL_SYNC:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // BUTTON1 == BUTTON_POSITIVE == "Yes"
                        if (which == DialogInterface.BUTTON1) {
                        	 startSync();
                        }
                    }
                };

                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.ui_dialog_need_first_manual_sync_title);
                builder.setMessage(R.string.ui_dialog_need_first_manual_sync_msg);
                builder.setPositiveButton(android.R.string.yes, dialogClickListener);
                builder.setNegativeButton(android.R.string.no, dialogClickListener);
                builder.setCancelable(false);
                return builder.create();
           
            default:
                return null;
        }

        return createMessageDialog(id, title, msg);
    }

    private Dialog createMessageDialog(final int id, String title, String msg) {
        Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismissDialog(id);
            }
        });
        return builder.create();
    }
    
   

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (PrefStore.PREF_LOGIN_USER.equals(preference.getKey())) {
            preference.setTitle(newValue.toString());
            SharedPreferences prefs = preference.getSharedPreferences();
            final String oldValue = prefs.getString(PrefStore.PREF_LOGIN_USER, null);
            if (!newValue.equals(oldValue)) {
                // We need to post the reset of sync state such that we do not interfere
                // with the current transaction of the SharedPreference.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PrefStore.clearSyncData(SetupActivity.this);
                        if (oldValue != null) {
                            showDialog(DIALOG_SYNC_DATA_RESET);
                        }
                    }
                });
            }
        } else if (PrefStore.PREF_IMAP_FOLDER.equals(preference.getKey())) {
            String imapFolder = newValue.toString();
            if (PrefStore.isValidImapFolder(imapFolder)) {
                preference.setTitle(imapFolder);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showDialog(DIALOG_INVALID_IMAP_FOLDER);
                    }
                });
                return false;
            }
        } else if (PrefStore.PREF_LOGIN_PASSWORD.equals(preference.getKey())) {
            if (PrefStore.isLoginUsernameSet(this)) {
                showDialog(DIALOG_NEED_FIRST_MANUAL_SYNC);
            }
        } else if (PrefStore.PREF_MAX_ITEMS_PER_SYNC.equals(preference.getKey())) {
            updateMaxItemsPerSync((String) newValue);
            if(newValue.equals("1"))
            	preference.setSummary("发送当天任务数据");
            else if(newValue.equals("2"))
            	preference.setSummary("发送历史任务数据");
            else if(newValue.equals("3"))
            	preference.setSummary("发送倒数任务数据");
            else if(newValue.equals("4"))
            	preference.setSummary("发送目标数据");
            else if(newValue.equals("5"))
            	preference.setSummary("发送心语数据");
            else
            	preference.setSummary("设置备份选项");
            
        }
        return true;
    }

    private void updateMaxItemsPerSync(String newValue) {
//        Preference pref = getPreferenceManager().findPreference(PrefStore.PREF_MAX_ITEMS_PER_SYNC);
        if (newValue == null) {
            newValue = String.valueOf(PrefStore.getMaxItemsPerSync(this));
        }
    }
}
