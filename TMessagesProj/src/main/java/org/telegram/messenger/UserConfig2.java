/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLRPC;

import java.io.File;

public class UserConfig2 {

    private static TLRPC.User currentUser;
    private final static Object sync = new Object();


    public static int getClientUserId() {
        synchronized (sync) {
            return currentUser != null ? currentUser.id : 0;
        }
    }

    public static TLRPC.User getCurrentUser(String userTag) {
        synchronized (sync) {
            UserConfig2.loadConfig(userTag);
            return currentUser;
        }
    }

    public static void setCurrentUser(TLRPC.User user) {
        synchronized (sync) {
            currentUser = user;
        }
    }

    public static void loadConfig(String userTag) {
        synchronized (sync) {
          try {
            final File configFile = new File(ApplicationLoader.getFilesDirFixed()+ userTag, "user.dat");
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfing"+ userTag, Context.MODE_PRIVATE);
            String string = preferences.getString("user", null);
            if (string != null) {
                byte[] bytes = Base64.decode(string, Base64.DEFAULT);
                if (bytes != null) {
                    SerializedData data = new SerializedData(bytes);
                    currentUser = TLRPC.User.TLdeserialize(data, data.readInt32(false), false);
                    data.cleanup();
                }
            }
          } catch (Exception e) {
              Log.i("TGM", "loadConfig: " + e.toString());
          }
        }
    }
}
