package com.example.floattest;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.library.FloatingViewListener;
import com.example.library.FloatingViewManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * ChatHead Service
 */
public class ChatHeadService extends Service implements FloatingViewListener, View.OnClickListener {

    /**
     * Debugging Log Tags
     */
    private static final String TAG = "ChatHeadService";

    /**
     * Intent key (Cutout safe area)
     */
    public static final String EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area";

    /**
     * Notification ID
     */
    private static final int NOTIFICATION_ID = 9083150;

    /**
     * FloatingViewManager
     */
    private FloatingViewManager mFloatingViewManager;


    /**
     * {@inheritDoc}
     */
    private RelativeLayout iconView;
    private ArrayList<View> floatList;
    private ArrayList<NotificationBadge> badgeList;
    private ArrayList<Integer> countList;

    private static final String SETTINGS_PLAYER_JSON = "settings_item_json";
    private Context context;

    static List<PackageInfo> packInfoList = null;
    private ArrayList<String> packOrder;
    private HashMap<String, String> hashMap;
    NotificationBadge mBadge, sBdage;
    private WindowManager windowManager;

    @Override
    public void onCreate() {
        context = this;
        //Receive message from BroadcastReceiver
        packInfoList = MainActivity.packInfoList;
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("message_to_Activity"));
        LocalBroadcastManager.getInstance(this).registerReceiver(popupRecevier,
                new IntentFilter("visibility"));

        //Save application name
        hashMap = new HashMap();
        countList = new ArrayList();
        packOrder = new ArrayList();
        for (int i = 0; i < packInfoList.size(); i++) {
            packOrder.add(packInfoList.get(i).packageName.replaceAll("\\.", ""));
            countList.add(0);
            HashMap<String, Integer> tabCount = new HashMap();
            MyNotificationListener.tabCountCover.add(tabCount);
        }

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        MainActivity.serviceOn = 1;
        // Do nothing if the Manager already exists
        if (mFloatingViewManager != null) {
            return START_STICKY;
        }
        Toast.makeText(this, "Start MessageHub", Toast.LENGTH_SHORT).show();
        //Get a resolution of the screen.
        final DisplayMetrics metrics = new DisplayMetrics();
        //Gets the top view of the window.
        final LayoutInflater inflater = LayoutInflater.from(this);
        iconView = (RelativeLayout) inflater.inflate(R.layout.widget_chathead, null, false);
        iconView.setOnClickListener(this);
        mBadge = (NotificationBadge) iconView.findViewById(R.id.badge);

        //Use FloatingViewManager to add and delete iconView to window.
        mFloatingViewManager = new FloatingViewManager(this, this);
        mFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed);
        mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
        mFloatingViewManager.setSafeInsetRect((Rect) intent.getParcelableExtra(EXTRA_CUTOUT_SAFE_AREA));
        final FloatingViewManager.Options options = new FloatingViewManager.Options();
        options.overMargin = (int) (16 * metrics.density);
        options.floatingViewWidth = 170;
        options.floatingViewHeight = 170;

        mFloatingViewManager.addViewToWindow(iconView, options);
        mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);


        // resident start
        startForeground(NOTIFICATION_ID, createNotification(this));

        return START_REDELIVER_INTENT;
    }

    public void childService() {
        //Get a resolution of the screen.
        final DisplayMetrics metrics = new DisplayMetrics();
        //Gets the top view of the window.
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        final LayoutInflater inflater = LayoutInflater.from(this);

        //Static positioning of each view using parameters.
        int type = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        } else {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);

        floatList = new ArrayList();
        badgeList = new ArrayList();

        int y_loc = -400;
        for (int i = 0; i < packInfoList.size(); i++) {
            String name = packInfoList.get(i).packageName.replaceAll("\\.", "");
            params.gravity = Gravity.RIGHT | Gravity.CENTER;
            params.y = y_loc;
            RelativeLayout floatView = (RelativeLayout) inflater.inflate(R.layout.widget_floating, null, false);
            floatList.add(floatView);
            sBdage = (NotificationBadge) floatView.findViewById(R.id.badge);
            badgeList.add(sBdage);

            floatView.setTag(name);
            hashMap.put(name, getPackageManager().getApplicationLabel(packInfoList.get(i).applicationInfo).toString());
            hashMap.put(name + "position", i + "");
            int number = 0;
            Iterator<String> keys = MyNotificationListener.tabCountCover.get(i).keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                number += (int) MyNotificationListener.tabCountCover.get(i).get(key);
            }
            badgeList.get(i).setNumber(number);

            floatView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<String> DBlist = Array2String.getStringArrayPref(context, SETTINGS_PLAYER_JSON);
                    String tag = view.getTag().toString();
                    if (DBlist.contains(tag)) {
                        Intent intent = new Intent(context, PopupWindow.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("packname", tag);
                        intent.putExtra("appname", hashMap.get(tag));
                        intent.putExtra("packposition", hashMap.get(tag + "position"));
                        startActivity(intent);

                        //delete view
                        for (int i = 0; i < floatList.size(); i++) {
                            windowManager.removeViewImmediate(floatList.get(i));

                        }
                    } else {
                        Toast.makeText(context, "no message", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Drawable icon = getPackageManager().getApplicationIcon(packInfoList.get(i).applicationInfo);

            ImageView imageView = (ImageView) floatView.findViewById(R.id.floatView);
            imageView.setImageDrawable(icon);
            //imageView.setLayoutParams(new RelativeLayout.LayoutParams(150,150));
            params.height = 150;
            params.width = 150;

            windowManager.addView(floatView, params);

            y_loc += 200;
        }
        RelativeLayout iconHome = (RelativeLayout) inflater.inflate(R.layout.widget_chatheadhome, null, false);
        floatList.add(iconHome);

        params.y = y_loc;
        params.height = 155;
        params.width = 155;
        windowManager.addView(iconHome, params);

        ImageView homView = (ImageView) iconHome.findViewById(R.id.fab_home);

        homView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //notification badge
                int count = 0;
                for (int i = 0; i < packInfoList.size(); i++) {
                    count += countList.get(i);
                }
                iconView.setVisibility(View.VISIBLE);
                iconView.setClickable(true);
                mBadge.setNumber(count);
                for (int i = 0; i < floatList.size(); i++) {
                    windowManager.removeViewImmediate(floatList.get(i));

                }
            }
        });


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        Toast.makeText(this, "Terminate all services", Toast.LENGTH_SHORT).show();
        MainActivity.serviceOn = 0;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFinishFloatingView() {
        stopSelf();
        Log.d(TAG, getString(R.string.finish_deleted));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTouchFinished(boolean isFinishing, int x, int y) {
        if (isFinishing) {
            Log.d(TAG, getString(R.string.deleted_soon));
        } else {
            Log.d(TAG, getString(R.string.touch_finished_position, x, y));
        }
    }


    /**
     * Displays notifications.
     * Describe click action.
     */

    @Override
    public void onClick(View v) {
        if (v.getId() == iconView.getId()) {
            iconView.setVisibility(View.INVISIBLE);
            iconView.setClickable(false);
            childService();
        }

    }


    //Broadcast message receiver
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("message");
            String title = intent.getStringExtra("title");
            Log.i("what meesage?", message);
            if (packOrder.contains(message)) {
                if (MyNotificationListener.tabCountCover.get(packOrder.indexOf(message)).containsKey(title)) {
                    //have
                    MyNotificationListener.tabCountCover.get(packOrder.indexOf(message)).put(title, (Integer) MyNotificationListener.tabCountCover.get(packOrder.indexOf(message)).get(title) + 1);
                } else {
                    //no
                    MyNotificationListener.tabCountCover.get(packOrder.indexOf(message)).put(title, 1);
                }
            }


            ArrayList<String> DBlist = Array2String.getStringArrayPref(context, SETTINGS_PLAYER_JSON);
            if (DBlist == null)
                DBlist = new ArrayList();
            if (!DBlist.contains(message)) {
                DBlist.add(message);
            } else {
                DBlist.remove(message);
                DBlist.add(message);
            }

            Array2String.setStringArrayPref(context, SETTINGS_PLAYER_JSON, DBlist);
            for (int i = 0; i < packInfoList.size(); i++) {
                if (packInfoList.get(i).packageName.replaceAll("\\.", "").equals(message)) {

                    int number = 0;
                    Iterator<String> keys = MyNotificationListener.tabCountCover.get(i).keySet().iterator();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        number += (int) MyNotificationListener.tabCountCover.get(i).get(key);
                    }

                    countList.set(i, number);
                    try {
                        badgeList.get(i).setNumber(countList.get(i));
                    } catch (Exception e) {

                    }
                }
            }
            if (iconView.isClickable()) {
                int count = 0;
                for (int i = 0; i < packInfoList.size(); i++) {
                    count += countList.get(i);
                }
                mBadge.setNumber(count);
            }

        }
    };
    private BroadcastReceiver popupRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            for (int i = 0; i < packInfoList.size(); i++) {
                if (packInfoList.get(i).packageName.replaceAll("\\.", "").equals(intent.getStringExtra("packname"))) {
                    int number = 0;
                    Iterator<String> keys = MyNotificationListener.tabCountCover.get(i).keySet().iterator();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        number += (int) MyNotificationListener.tabCountCover.get(i).get(key);
                    }
                    countList.set(i, number);
                    badgeList.get(i).setNumber(countList.get(i));
                }
            }

            //call view
            childService();

        }
    };


    private static Notification createNotification(Context context) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.default_floatingview_channel_id));
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.mh_launcher);
        builder.setContentTitle("MessageHub is running now.");
        //builder.setContentText(context.getString(R.string.content_text));
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);

        return builder.build();
    }


}
