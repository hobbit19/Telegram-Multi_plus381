package org.telegram.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import org.telegram.messenger.ApplicationLoader2;
import org.telegram.messenger.ChangeUserHelper;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig2;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Adapters.UserItemsAdapter;
import org.telegram.ui.Components.UserItems;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//import org.telegram.ui.ActionBar.AlertDialog;

public class ChangeUserActivity extends Activity implements AdapterView.OnItemClickListener {

    ListView lvUserList = null;
    UserItemsAdapter adapter = null;
    private ArrayList<Object> itemList;
    private UserItems userItems ;
    static ProgressDialog prepareProgress;
    static Context ctx ;
    public int currentUser = 0;

    @Override
    public void onBackPressed()
    {
        System.gc();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user);
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("fromIntro", false)) backToLastUser();
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getText(R.string.Change_another_user));
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_ab_back);
        }

        try {
            ctx = this;
            itemList = new ArrayList<Object>();
            lvUserList = (ListView) findViewById(R.id.users_listview);
            lvUserList.setOnItemClickListener(this);
            lvUserList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                    // TODO Auto-generated method stub
                    // TODO deleting user on random position
                    if (position > 0)
                        showAlertDeleteUser(position);
                    else Toast.makeText(ChangeUserActivity.this, getText(R.string.DeleteFirstUser), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            prepareArrayList();
            Log.i("TGM", "onCreate: prepareArray");
            prepareProgress.dismiss();
            Thread prepareThread = new Thread(
                    new Runnable() {
                        public void run() {
                            prepareArrayList();
                            runOnUiThread(new Runnable() {
                                public void run() {
//                                    prepareProgress.dismiss();
                                }
                            });
                        }
                    }
            );
//            prepareThread.start();

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//            fab.setColorNormal(Theme.getColor(Theme.key_chats_actionBackground));
//            fab.setColorPressed(Theme.getColor(Theme.key_chats_actionPressedBackground));
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (lvUserList.getCount() <= 9)
                        addUser();
                    else
                        Toast.makeText(ChangeUserActivity.this, getText(R.string.MaxUsersCount), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Throwable th) {
            Log.i("TGM", "onCreate: " + th.toString());
        }
    }


    public static void showPrepareDialog(Context ctx, String title) {
        prepareProgress= new ProgressDialog(ctx);
        prepareProgress.setMessage(title);
        prepareProgress.setIndeterminate(false);
        prepareProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prepareProgress.setCancelable(false);
        prepareProgress.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
         lvUserList = null;
         adapter = null;
         itemList = null;
         userItems = null;
         System.gc();
    }

    private void deleteUser(int position) {
        int toDelete = -1;
        if(getUsersEnabled().size() > position) toDelete = getUsersEnabled().get(position); else return;

        SharedPreferences sharedPref = getSharedPreferences("userID", Context.MODE_PRIVATE);
        sharedPref.edit().putInt("state_user_" + toDelete, 1).commit();
        sharedPref.edit().apply();
        deleteDir(getApplicationInfo().dataDir + "/files_user_" + String.valueOf(toDelete));
        if(currentUser == position) setUser(0); else prepareArrayList();
    }

    private void showAlertDeleteUser(final int position) {
        String title = getText(R.string.DialogSure).toString();
        String message = getText(R.string.DialogDeleteUser).toString();
        String button1String = getText(R.string.DialogYes).toString();
        String button2String = getText(R.string.DialogNo).toString();

        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle(title);
        ad.setMessage(message);
        ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                deleteUser(position);
            }
        });
        ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        ad.show();
    }

    private void showAlertAddUser() {
        String title = getText(R.string.DialogAddSure).toString();
        String message = getText(R.string.DialogAddUser).toString();
        String button1String = getText(R.string.DialogAddYes).toString();
        String button2String = getText(R.string.DialogAddNo).toString();

        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle(title);
        ad.setMessage(message);
        ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                restart();
            }
        });
        ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        ad.show();
    }

    public void deleteDir(String folder) {
        File dir = new File(folder);
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
            dir.delete();
        }
    }

    public void addUser() {
        int firstDisabledUser = getUsersDisabled();
        Log.i("TGM", "addUser: firstDisabledUser" + firstDisabledUser);
        SharedPreferences sharedPref = getSharedPreferences("userID", Context.MODE_PRIVATE);
        sharedPref.edit().putInt("lastID", ChangeUserHelper.getID()).commit();
        Log.i("userTAG", "lastUser: tag changed to " + ChangeUserHelper.getID());
        ChangeUserHelper.setUserTag(firstDisabledUser);
        sharedPref.edit().putInt("userID", firstDisabledUser).commit();
        sharedPref.edit().putInt("state_user_"+firstDisabledUser, 0).commit();
        sharedPref.edit().putInt("addedUser", firstDisabledUser).commit();
        sharedPref.edit().apply();
        Log.i("userTAG", "addUser: tag changed to " + ChangeUserHelper.getUserTag());
        showAlertAddUser();
    }

    public void backToLastUser() {
        SharedPreferences sharedPref = getSharedPreferences("userID", Context.MODE_PRIVATE);
        sharedPref.edit().putInt("state_user_" + sharedPref.getInt("addedUser",10), 1).commit();
        sharedPref.edit().putInt("userID", sharedPref.getInt("lastID",0)).commit();
        sharedPref.edit().apply();
        deleteUser(sharedPref.getInt("addedUser",10));
        Log.i("userTAG", "backToLastUser: tag changed to _" + sharedPref.getInt("lastID",0));
        restart();
    }

    public int getUsersDisabled() {
        int count = 0;
        Log.i("TGM", "getUsersDisabled: called");
        SharedPreferences userDisabled = getSharedPreferences("userID", Context.MODE_PRIVATE);
        for (int i = 0; i < 9; i++) {
            if(userDisabled.getInt("state_user_"+i,1) == 1) return i;
        }
        return -1;
    }

    public List<Integer> getUsersEnabled() {
        List<Integer> users = new ArrayList<Integer>();
        Log.i("TGM", "getUsersEnabled: called");
        SharedPreferences userDisabled = getSharedPreferences("userID", Context.MODE_PRIVATE);
        for (int i = 0; i < 9; i++) {
            if(userDisabled.getInt("state_user_"+i,1) == 0) {
                users.add(i);
            }
        }
        return users;
    }

    public void setUser(int position) {
        int toSetUser = -1;
        if(getUsersEnabled().size() > position) toSetUser = getUsersEnabled().get(position); else return;
        ChangeUserHelper.setUserTag(toSetUser);
        SharedPreferences sharedPref = getSharedPreferences("userID", Context.MODE_PRIVATE);
        sharedPref.edit().putInt("userID", ChangeUserHelper.getID()).commit();
        sharedPref.edit().apply();
        Log.i("userTAG", "setUser: tag changed to " + ChangeUserHelper.getUserTag());
        restart();
    }

    public void restart() {
//        Intent launchIntent = new Intent(getApplicationContext(), org.telegram.ui.LaunchActivity.class);
//        PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0, launchIntent , 0);
//        AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        manager.set(AlarmManager.RTC, System.currentTimeMillis() + 1500, intent);
//        Log.i("userTAG", "restarting... " + ChangeUserHelper.getUserTag());
//        System.exit(2);
        Intent mStartActivity = new Intent(getApplicationContext(), org.telegram.ui.LaunchActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, mPendingIntent);
//        System.exit(0);
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        UserItems userItems = (UserItems) adapter.getItem(position);
        if(currentUser == position) Toast.makeText(ctx, getText(R.string.IsCurrentUser).toString(), Toast.LENGTH_SHORT).show(); else setUser(position);
    }

    public void prepareArrayList()
    {   itemList = new ArrayList<>();
        for (int i = 0; i < getUsersEnabled().size() ; i++) {
            int k = getUsersEnabled().get(i);
            Log.i("TGM", "prepareArrayLits: " + k);
            Log.i("TGM", "prepareArrayList: getUsersEnabled().size " + getUsersEnabled().size());
            Log.i("TGM", "prepareArrayList: getUsersEnabled().get(i) " + k + " i = " + i);
            String first_name = "null";
            if (getUserByTag("_user_" + k).last_name == null) first_name = getUserByTag("_user_" + k).first_name;
            else first_name = getUserByTag("_user_" + k).first_name + " " + getUserByTag("_user_" + k).last_name;
            String phone = getUserByTag("_user_" + k).phone;
            Bitmap photo = getBitmap(getUserByTag("_user_" + k));
            if (ChangeUserHelper.getID() == k) AddObjectToList(photo, first_name, phone, i);
            else AddObjectToList(photo, first_name, phone);
        }
        adapter = new UserItemsAdapter(this, itemList);
        lvUserList.setAdapter(adapter);
        Log.i("TGM", "prepareArrayLits: setAdapters");
    }

    private TLRPC.User getUserByTag(String tag) {
        ApplicationLoader2.convertConfig2(tag);
        TLRPC.User user = UserConfig2.getCurrentUser(tag);
        Log.i("TGM", "getUserByTag: called " + tag.toString());
        return user;
    }

    public Bitmap getBitmap(TLRPC.User user) {
        Bitmap icon;
        Log.i("TGM", "getBitmap: called");
        if (user.photo != null && user.photo.photo_small != null) {
            Log.i("TGM", "getBitmap: photo != null");
            icon = createRoundBitmap(FileLoader.getPathToAttach(user.photo.photo_small, true));
            return icon;
        }
        return drawableToBitmap(R.drawable.tab_user);
    }

    public void AddObjectToList(Bitmap image, String title, String desc)
    {
        Log.i("TGM", "AddObjectToList: called");
        userItems = new UserItems();
        userItems.setPhone(desc);
        userItems.setPhoto(image);
        userItems.setName(title);
        itemList.add(userItems);
    }

    public void AddObjectToList(Bitmap image, String title, String desc, int pos)
    {
        Log.i("TGM", "AddObjectToList with setCurrent: called " + pos);
        userItems = new UserItems();
        userItems.setPhone(desc);
        userItems.setPhoto(image);
        userItems.setCurrent(pos);
        currentUser = pos;
        userItems.setName(title);
        itemList.add(userItems);
    }

    public Bitmap drawableToBitmap (int drawable) {
        Log.i("TGM", "drawableToBitmap: called");
        Bitmap b = null;
        Drawable d = getResources().getDrawable(drawable);
        Drawable currentState = d.getCurrent();
        if(currentState instanceof BitmapDrawable)
            b = ((BitmapDrawable)currentState).getBitmap();
        return b;
    }

    public Bitmap createRoundBitmap(File path) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path.toString());
            if (bitmap != null) {
                Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

                BitmapShader shader = new BitmapShader (bitmap,  Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Paint paint = new Paint();
                paint.setShader(shader);
                paint.setAntiAlias(true);
                Canvas c = new Canvas(circleBitmap);
                c.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth()/2, paint);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                        circleBitmap, (int) convertDpToPixel(50),(int) convertDpToPixel(50), false);
                return resizedBitmap;
            }
        } catch (Throwable e) {
            Log.e("TGM",e.toString());
        }
        return null;
    }

    public static float convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
