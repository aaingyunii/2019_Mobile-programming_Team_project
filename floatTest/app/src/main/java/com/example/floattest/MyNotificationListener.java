package com.example.floattest;

import android.app.Notification;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;



public class MyNotificationListener extends NotificationListenerService {
    MyDBHandler myDBHandler;

    public final static String TAG = "MyNotificationListener";

    @Override
    public void onCreate() {
        super.onCreate();
        //서비스실행할때 최초로 생성(바꿔줘야할지 고민해야 한다)
        myDBHandler = new MyDBHandler(this, "chatlog");
    }
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        Log.d(TAG, "onNotificationRemoved ~ " +
                " packageName: " + sbn.getPackageName() +
                " id: " + sbn.getId());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        Notification notification = sbn.getNotification();
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
        Icon smallIcon = notification.getSmallIcon();
        Icon largeIcon = notification.getLargeIcon();


        String subTextS= "";
        if(text == null)
            text = "";
        if(title == null)
            title = "";
        if(subText==null)
            subText="";
        subTextS = subText.toString();

        String packNmae = sbn.getPackageName().replaceAll("\\.", "");
        if(title.length()!=0){
            myDBHandler.createTable(packNmae);
            if(title.length()!=0 && subText.length() !=0){
                String temp = title;
                title =subTextS;
                subTextS = temp;
            }
            myDBHandler.insert(packNmae,sbn.getId(),sbn.getPostTime(),title,text.toString(),subTextS);
            Log.d(TAG, "onNotificationPosted ~ " +
                    " packageName: " + sbn.getPackageName() +
                    " id: " + sbn.getId() +
                    " postTime: " + sbn.getPostTime() +
                    " title: " + title +
                    " text : " + text +
                    " subText: " + subTextS);

            //여기서 리스트어댑터와 프래그먼트에 접근하여 화면을 새로고침한다.
            sendMessage(packNmae,title);
        }
    }
    private void sendMessage(String packageName,String title) {
        Intent intent = new Intent("message_to_Activity");
        intent.putExtra("message", packageName);
        intent.putExtra("title",title);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }



}