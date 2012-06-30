package com.planbetter.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;


public class AlertService extends Service {

	private Vibrator mVibrator;
	private TelephonyManager mTelephonyManager;
	private int mInitialCallState;
	private static final long[] sVibratePattern = new long[] { 500, 500 };
	private static final int ALARM_TIMEOUT_SECONDS = 10 * 60;
	private static final int KILLER = 1000;
	private boolean mPlaying = false;

	private LocalBinder myBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		public AlertService getService() {
			return AlertService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return myBinder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mPhoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);
//		AlarmAlertWakeLock.acquireCpuWakeLock(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if(intent == null) {
			stopSelf();
			return START_NOT_STICKY;
		}
		playAlarm();
		mInitialCallState = mTelephonyManager.getCallState();
		return START_STICKY;
	}
	
	private void playAlarm() {
		mPlaying = true;
		mVibrator.vibrate(sVibratePattern, 0);
		enableKiller();
	}
	
	private void enableKiller() {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(KILLER),
                1000 * ALARM_TIMEOUT_SECONDS);
    }
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what) {
			case KILLER:
				stopSelf();
				break;
			}
		}
		
	};

	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String ignored) {
			if (state != TelephonyManager.CALL_STATE_IDLE
					&& state != mInitialCallState) {
				stopSelf();
			}
		}
	};
	
	@Override
    public void onDestroy() {
        stop();
        mTelephonyManager.listen(mPhoneStateListener, 0);
//        AlarmAlertWakeLock.releaseCpuLock();
    }
	
	private void stop() {
		if(mPlaying) {
			mPlaying = false;
			mVibrator.cancel();
		}
		disableKiller();
	}
	
	private void disableKiller() {
	    mHandler.removeMessages(KILLER);
	}
}
