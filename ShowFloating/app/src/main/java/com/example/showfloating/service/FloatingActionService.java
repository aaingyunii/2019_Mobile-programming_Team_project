package com.example.showfloating.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.example.showfloating.R;

/**
 * Floating view 클릭 시 채팅앱 아이콘이 뜨는 것.
 */

public class FloatingActionService extends Service {

    private String TAG = "FloatingViewService";


    @Override
    public void onCreate() {
        super.onCreate();
        //화면의 해상도를 구한다.
        final DisplayMetrics metrics = new DisplayMetrics();
        //윈도우의 최상위 뷰를 가져온다.
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        //Xml의 객체들을 view로 가져오는 역할
        final LayoutInflater inflater = LayoutInflater.from(this);
        //xml파일을 view로 만들어서 화면위에 띄운다.
        final FrameLayout floatView = (FrameLayout) inflater.inflate(R.layout.widget_floating,null,false);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

    }


    /**
     * Configuration floating action buttion action
     * 플로팅 뷰를 클릭 시 나머지 창들이 생기는 액션
     */


//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        //화면의 해상도를 구한다.
//        final DisplayMetrics metrics = new DisplayMetrics();
//        //윈도우의 최상위 뷰를 가져온다.
//        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//        windowManager.getDefaultDisplay().getMetrics(metrics);
//
//        //Xml의 객체들을 view로 가져오는 역할
//        final LayoutInflater inflater = LayoutInflater.from(this);
//        //xml파일을 view로 만들어서 화면위에 띄운다.
//        final View floatView = (View) inflater.inflate(R.layout.widget_floating,null,false);
//
//        floatView.setVisibility(View.VISIBLE);
//
////        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
////                WindowManager.LayoutParams.WRAP_CONTENT,
////                WindowManager.LayoutParams.WRAP_CONTENT,
////                WindowManager.LayoutParams.TYPE_PHONE,
////                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
////                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
////                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
////                PixelFormat.TRANSLUCENT);
////
//        return flags;
//    }


    @Override
    public IBinder onBind(Intent intent) { return null;}

}
