/* Copyright (c) 2009 Christoph Studer <chstuder@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.planbetter.mail;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PrefStore {
    public static final String PREF_LOGIN_USER = "login_user";
    public static final String PREF_LOGIN_PASSWORD = "login_password";

    static final String PREF_REFERENECE_UID = "reference_uid";  
    public static final String PREF_IMAP_FOLDER = "imap_folder";
    
    static final String PREF_INCOMING_TIMEOUT_SECONDS = "incoming_timeout_seconds";
    static final String PREF_REGULAR_TIMEOUT_SECONDS = "regular_timeout_seconds";
    
    static final String PREF_LAST_SYNC = "last_sync";
    
    public static final String PREF_MAX_ITEMS_PER_SYNC = "max_items_per_sync";
    static final String PREF_MARK_AS_READ = "mark_as_read";

    static final String DEFAULT_IMAP_FOLDER = "PlanBetter";
    
 
    /** Default value for {@link PrefStore#PREF_INCOMING_TIMEOUT_SECONDS}. */
    static final int DEFAULT_INCOMING_TIMEOUT_SECONDS = 20;
    
    /** Default value for {@link PrefStore#PREF_REGULAR_TIMEOUT_SECONDS}. */
    static final int DEFAULT_REGULAR_TIMEOUT_SECONDS = 30 * 60; // 30 minutes
    
    /** Default value for {@link #PREF_LAST_SYNC}. */
    public static final long DEFAULT_LAST_SYNC = -1;

    /** Default value for {@link #PREF_MAX_ITEMS_PER_SYNC}. */
    static final String DEFAULT_MAX_ITEMS_PER_SYNC = "100";

    /** Default value for {@link #PREF_MARK_AS_READ}. */
    static final boolean DEFAULT_MARK_AS_READ = false;
    
    public static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
    

    public static String getLoginUsername(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_LOGIN_USER, null);
    }
    
    public static String getLoginPassword(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_LOGIN_PASSWORD, null);
    }
    
    public static boolean isLoginUsernameSet(Context ctx) {
        return getLoginUsername(ctx) != null;
    }
    
    public static boolean isLoginInformationSet(Context ctx) {
        return isLoginUsernameSet(ctx) && getLoginPassword(ctx) != null;
    }
    
    public static String getReferenceUid(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_REFERENECE_UID, null);
    }
    
    public static void setReferenceUid(Context ctx, String referenceUid) {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_REFERENECE_UID, referenceUid);
        editor.commit();
    }
    
    public static String getImapFolder(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_IMAP_FOLDER, DEFAULT_IMAP_FOLDER);
    }
    
    public static boolean isImapFolderSet(Context ctx) {
        return getSharedPreferences(ctx).contains(PREF_IMAP_FOLDER);
    }
    
    public static int getMaxItemsPerSync(Context ctx) {
        String str = getSharedPreferences(ctx).getString(PREF_MAX_ITEMS_PER_SYNC,
                DEFAULT_MAX_ITEMS_PER_SYNC);
        return Integer.valueOf(str);
    }
    
    /**
     * Returns whether an IMAP folder is valid. This is the case if the name
     * only contains unaccented latin letters <code>[a-zA-Z]</code>.
     */
    public static boolean isValidImapFolder(String imapFolder) {
        for (int i = 0; i < imapFolder.length(); i++) {
            char currChar = imapFolder.charAt(i);
            if (!((currChar >= 'a' && currChar <= 'z')
                    || (currChar >= 'A' && currChar <= 'Z'))) {
                return false;
            }
        }
        return true;
    }
    
    public static void setImapFolder(Context ctx, String imapFolder) {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_IMAP_FOLDER, imapFolder);
        editor.commit();
    }
    
    
    public static int getIncomingTimeoutSecs(Context ctx) {
       return getSharedPreferences(ctx).getInt(PREF_INCOMING_TIMEOUT_SECONDS,
               DEFAULT_INCOMING_TIMEOUT_SECONDS);
    }
    
    public static int getRegularTimeoutSecs(Context ctx) {
        return getSharedPreferences(ctx).getInt(PREF_REGULAR_TIMEOUT_SECONDS,
                DEFAULT_REGULAR_TIMEOUT_SECONDS); 
    }
    
    public static long getLastSync(Context ctx) {
        return getSharedPreferences(ctx).getLong(PREF_LAST_SYNC, DEFAULT_LAST_SYNC);
    }
    
    public static void setLastSync(Context ctx) {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putLong(PREF_LAST_SYNC, System.currentTimeMillis());
        editor.commit();
    }
    
    public static boolean getMarkAsRead(Context ctx) {
        return getSharedPreferences(ctx).getBoolean(PREF_MARK_AS_READ, DEFAULT_MARK_AS_READ);
    }
    
    public static void setMarkAsRead(Context ctx, boolean markAsRead) {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(PREF_MARK_AS_READ, markAsRead);
        editor.commit();
    }
    
    
    public static void clearSyncData(Context ctx) {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(PREF_LOGIN_PASSWORD);
        editor.remove(PREF_LAST_SYNC);
        editor.commit();
    }

}
