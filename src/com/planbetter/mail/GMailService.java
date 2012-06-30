package com.planbetter.mail;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.util.Log;

import com.android.email.mail.Folder;
import com.android.email.mail.Folder.FolderType;
import com.android.email.mail.Folder.OpenMode;
import com.android.email.mail.Address;
import com.android.email.mail.Message;
import com.android.email.mail.Message.RecipientType;
import com.android.email.mail.MessagingException;
import com.android.email.mail.internet.MimeMessage;
import com.android.email.mail.internet.TextBody;
import com.android.email.mail.store.ImapStore;
import com.planbetter.activity.R;
import com.planbetter.constant.GMailConsts;

public class GMailService extends Service {
	private static boolean sIsRunning = false;

	private static GMailSyncState sState = GMailSyncState.IDLE;

	private static int sItemsToSync;
	private static int sCurrentSyncedItems;
	private static String sLastError;
	private static StateChangeListener sStateChangeListener;
	private static WakeLock sWakeLock;
	private static WifiLock sWifiLock;
	public static boolean sCanceled;

	public enum GMailSyncState {
		IDLE, CALC, LOGIN, SYNC, AUTH_FAILED, GENERAL_ERROR, CANCELED;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public static void acquireWakeLock(Context ctx) {
		if (sWakeLock == null) {
			PowerManager pMgr = (PowerManager) ctx
					.getSystemService(POWER_SERVICE);
			sWakeLock = pMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					"SmsSyncService.sync() wakelock.");

			WifiManager wMgr = (WifiManager) ctx.getSystemService(WIFI_SERVICE);
			sWifiLock = wMgr.createWifiLock("GMail Backup");
		}
		sWakeLock.acquire();
		sWifiLock.acquire();
	}

	public static void releaseWakeLock(Context ctx) {
		sWakeLock.release();
		sWifiLock.release();
	}

	@Override
	// TODO(chstuder): Clean this flow up a bit and split it into multiple
	// methods. Make clean distinction between onStart(...) and backup(...).
	public void onStart(final Intent intent, int startId) {
		super.onStart(intent, startId);

		synchronized (this.getClass()) {
			// Only start a sync if there's no other sync going on at this time.
			if (!sIsRunning) {
				acquireWakeLock(this);
				sIsRunning = true;
				// Start sync in new thread.
				new Thread() {
					public void run() {

						Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
						try {

							backup();

						} catch (GeneralErrorException e) {
							Log.i(GMailConsts.TAG, "", e);
							sLastError = e.getLocalizedMessage();
							updateState(GMailSyncState.GENERAL_ERROR);
						} catch (AuthenticationErrorException e) {
							Log.i(GMailConsts.TAG, "", e);
							sLastError = e.getLocalizedMessage();
							updateState(GMailSyncState.AUTH_FAILED);
						} finally {
							stopSelf();

							sIsRunning = false;
							releaseWakeLock(GMailService.this);
						}
					}
				}.start();
			} else {
				Log.d(GMailConsts.TAG,
						"SmsSyncService.onStart(): Already running.");
			}
		}
	}

	public void backup() throws GeneralErrorException,
			AuthenticationErrorException {
		Log.i(GMailConsts.TAG, "Starting backup...");
		sCanceled = false;

		if (!PrefStore.isLoginInformationSet(this)) {
			throw new GeneralErrorException(this,
					R.string.err_sync_requires_login_info, null);
		}

		String username = PrefStore.getLoginUsername(this);
		String password = PrefStore.getLoginPassword(this);
		int flag = PrefStore.getMaxItemsPerSync(this);

		updateState(GMailSyncState.CALC);

		sItemsToSync = 1;
		sCurrentSyncedItems = 0;

		Log.d(GMailConsts.TAG, "Total messages to backup: " + sItemsToSync);

		updateState(GMailSyncState.LOGIN);

		/* 登陆邮箱，构建文件夹 */
		ImapStore imapStore;
		Folder folder;
		boolean folderExists;
		String label = PrefStore.getImapFolder(this);
		try {
			imapStore = new ImapStore(String.format(GMailConsts.IMAP_URI,
					URLEncoder.encode(username), URLEncoder.encode(password)
							.replace("+", "%20")));
			folder = imapStore.getFolder(label);
			folderExists = folder.exists();
			if (!folderExists) {
				Log.i(GMailConsts.TAG, "Label '" + label
						+ "' does not exist yet. Creating.");
				folder.create(FolderType.HOLDS_MESSAGES);
			}
			folder.open(OpenMode.READ_WRITE);// 确认要发送至这个文件夹下
		} catch (MessagingException e) {
			throw new AuthenticationErrorException(e);
		}

		List<Message> messages = new ArrayList<Message>();
	
		try {
			while (true) {
				// Cancel sync if requested by the user.
				if (sCanceled) {
					Log.i(GMailConsts.TAG, "Backup canceled by user.");
					updateState(GMailSyncState.CANCELED);
					break;
				}
				updateState(GMailSyncState.SYNC);

			   if (sCurrentSyncedItems >= sItemsToSync) {
	                    Log.i(GMailConsts.TAG, "Sync done: " + sCurrentSyncedItems + " items uploaded.");
	                    PrefStore.setLastSync(GMailService.this);
	                    updateState(GMailSyncState.IDLE);
	                    folder.close(true);
	                    break;
	                }
				Message msg = new MimeMessage();

				String sub = DataForm.getSubject(flag);
				String body = DataForm.getContent(this, flag);
				msg.setSubject("PlanBetter backup:" + sub);
				TextBody tbody = new TextBody(body);
				Address mUserAddress = new Address(username);
				msg.setFrom(mUserAddress);
				msg.setRecipient(RecipientType.TO, mUserAddress);
				msg.setSentDate(new Date());
				msg.setBody(tbody);
				messages.add(msg);

				folder.appendMessages(messages.toArray(new Message[messages
						.size()]));
				updateState(GMailSyncState.SYNC);
				sCurrentSyncedItems += 1;

				Log.d(GMailConsts.TAG, "Sending " + body.length()
						+ " messages to server.");
			}
		} catch (MessagingException e) {
			throw new GeneralErrorException(this,
					R.string.err_communication_error, e);
		} finally {

		}
	}

	public static void cancel() {
		if (GMailService.sIsRunning) {
			GMailService.sCanceled = true;
		}
	}

	// Statistics accessible from other classes.

	/**
	 * Returns whether there is currently a backup going on or not.
	 * 
	 */
	public static boolean isWorking() {
		return sIsRunning;
	}

	/**
	 * Returns the current state of the service. Also see
	 * {@link #setStateChangeListener(StateChangeListener)} to get notified when
	 * the state changes.
	 */
	public static GMailSyncState getState() {
		return sState;
	}

	/**
	 * Returns a description of the last error. Only valid if
	 * <code>{@link #getState()} == {@link GMailSyncState#GENERAL_ERROR}</code>.
	 */
	public static String getErrorDescription() {
		return (sState == GMailSyncState.GENERAL_ERROR) ? sLastError : null;
	}

	/**
	 * Returns the number of messages that require sync during the current
	 * cycle.
	 */
	public static int getItemsToSyncCount() {
		return sItemsToSync;
	}

	/**
	 * Registers a {@link StateChangeListener} that is notified whenever the
	 * state of the service changes. Note that at most one listener can be
	 * registered and you need to call {@link #unsetStateChangeListener()} in
	 * between calls to this method.
	 * 
	 * @see #getState()
	 * @see #unsetStateChangeListener()
	 */
	public static void setStateChangeListener(StateChangeListener listener) {
		if (sStateChangeListener != null) {
			throw new IllegalStateException(
					"setStateChangeListener(...) called when there"
							+ " was still some other listener "
							+ "registered. Use unsetStateChangeListener() first.");
		}
		sStateChangeListener = listener;
	}

	/**
	 * Unregisters the currently registered {@link StateChangeListener}.
	 * 
	 * @see #setStateChangeListener(StateChangeListener)
	 */
	public static void unsetStateChangeListener() {
		sStateChangeListener = null;
	}

	/**
	 * Internal method that needs to be called whenever the state of the service
	 * changes.
	 */
	public static void updateState(GMailSyncState newState) {
		GMailSyncState old = sState;
		sState = newState;
		if (sStateChangeListener != null) {
			sStateChangeListener.stateChanged(old, newState);
		}
	}

	/**
	 * A state change listener interface that provides a callback that is called
	 * whenever the state of the {@link SmsSyncService} changes.
	 * 
	 * @see SmsSyncService#setStateChangeListener(StateChangeListener)
	 */
	public interface StateChangeListener {
		/**
		 * Called whenever the sync state of the service changed.
		 */
		public void stateChanged(GMailSyncState oldState,
				GMailSyncState newState);
	}

	/**
	 * Exception indicating an error while synchronizing.
	 */
	public static class GeneralErrorException extends Exception {
		private static final long serialVersionUID = 1L;

		public GeneralErrorException(String msg, Throwable t) {
			super(msg, t);
		}

		public GeneralErrorException(Context ctx, int msgId, Throwable t) {
			super(ctx.getString(msgId), t);
		}
	}

	public static class AuthenticationErrorException extends Exception {
		private static final long serialVersionUID = 1L;

		public AuthenticationErrorException(Throwable t) {
			super(t.getLocalizedMessage(), t);
		}
	}

	public static int getCurrentSyncedItems() {
		// TODO Auto-generated method stub
		return sCurrentSyncedItems;
	}
}
