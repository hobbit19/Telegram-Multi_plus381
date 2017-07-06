package org.telegram.messenger;

import android.util.Log;

/**
 * Created by oleg.svs on 21.05.2017.
 */

public class ChangeUserHelper {
    private static final String userTag = "_user_";
    private static int userID = 0;

    static public void setUserTag(int id) {
        if(id != -1)
            userID = id;
        Log.i("userTAG", "setUserTag: " + userTag + userID);
    }

    static public String getUserTag() {
        return String.valueOf(userTag + userID);
    }

    static public int getID() {
        return userID;
    }

}
