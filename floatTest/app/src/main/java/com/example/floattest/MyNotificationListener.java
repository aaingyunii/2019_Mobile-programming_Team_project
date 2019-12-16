package com.example.floattest;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.robj.notificationhelperlibrary.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.HashMap;

import models.Action;
import services.BaseNotificationListener;


public class MyNotificationListener extends BaseNotificationListener {
    MyDBHandler myDBHandler;
    private static final String SETTINGS_PLAYER_JSON = "settings_item_json";

    public final static String TAG = "MyNotificationListener";

    public static HashMap<String, Action> replyModel = new HashMap();

    static ArrayList<HashMap> tabCountCover;

    @Override
    public void onCreate() {
        super.onCreate();
        //count
        tabCountCover = new ArrayList();

        //First created when running the service
        myDBHandler = new MyDBHandler(this, "chatlog");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);


        String firstStart = prefs.getString("firstStart", null);
        if (firstStart == null) {
            Log.i("reset", "ok");
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.putString("firstStart", "ok");
            editor.apply();
            String[] packName = MessengerListActivity.packNameList;
            for (int i = 0; i < packName.length; i++)
                myDBHandler.deleteAll(packName[i]);
            ArrayList<String> DBlist = new ArrayList();
            Array2String.setStringArrayPref(this, SETTINGS_PLAYER_JSON, DBlist);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        Log.d(TAG, "onNotificationRemoved ~ " +
                " packageName: " + sbn.getPackageName() +
                " id: " + sbn.getId());
    }

    @Override
    protected boolean shouldAppBeAnnounced(StatusBarNotification sbn) {


        Notification notification = sbn.getNotification();
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
        Icon smallIcon = notification.getSmallIcon();
        Icon largeIcon = notification.getLargeIcon();
        int color = notification.color;

        String subTextS = "";
        if (text == null)
            text = "";
        if (title == null)
            title = "";
        if (subText == null)
            subText = "";
        subTextS = subText.toString();
        String packNmae = sbn.getPackageName().replaceAll("\\.", "");


        if (title.length() != 0 && !packNmae.contains("com.android")) {
            if ((packNmae.contains("insta") && text.toString().contains(":") && !text.toString().substring(0, text.toString().indexOf(":")).equals(title)) || sbn.getId() == 1863136066) {


            } else {
                myDBHandler.createTable(packNmae);
                if (title.length() != 0 && subText.length() != 0) {
                    String temp = title;
                    title = subTextS;
                    subTextS = temp;
                }

                //slack, processing exception
                if (packNmae.contains("Slack") && title.contains(")")) {
                    title = title.substring(title.indexOf(")") + 1, title.length());
                }

                myDBHandler.insert(packNmae, sbn.getId(), sbn.getPostTime(), title, text.toString(), subTextS);
                Log.d(TAG, "onNotificationPosted ~ " +
                        " packageName: " + sbn.getPackageName() +
                        " id: " + sbn.getId() +
                        " postTime: " + sbn.getPostTime() +
                        " title: " + title +
                        " text : " + text +
                        " subText: " + subTextS);

                //From here, access the list adapter and fragments and refresh the screen.
                sendMessage(packNmae, title);

                String key = packNmae + title;
                if (!replyModel.containsKey(key)) {
                    replyModel.put(key, NotificationUtils.getQuickReplyAction(notification, sbn.getPackageName()));
                }
            }
        }


        return false;
    }

    @Override
    protected void onNotificationPosted(StatusBarNotification statusBarNotification, String s) {

    }

    private void sendMessage(String packageName, String title) {
        Intent intent = new Intent("message_to_Activity");
        intent.putExtra("message", packageName);
        intent.putExtra("title", title);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}