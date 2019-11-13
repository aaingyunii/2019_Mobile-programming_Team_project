package com.example.showfloating.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.showfloating.R;

/**
 * Floating view 클릭 시 채팅앱 아이콘이 뜨는 것.
 */

public class FloatingActionService extends Service {

    private ChatHeadService chatHeadService;
    private String TAG = "FloatingViewService";
    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private Boolean isPopupOpen = false;


    @Override
    public void onCreate() {
        super.onCreate();

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        //화면의 해상도를 구한다.
        final DisplayMetrics metrics = new DisplayMetrics();
        //윈도우의 최상위 뷰를 가져온다.
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        //Xml의 객체들을 view로 가져오는 역할
        final LayoutInflater inflater = LayoutInflater.from(this);
        //xml파일을 view로 만들어서 화면위에 띄운다.
        final ImageView floatView = (ImageView) inflater.inflate(R.layout.widget_floating,null,false);
        final ImageView floatView2 = (ImageView) inflater.inflate(R.layout.widget_floating2,null,false);

        int type=0;
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.O){
            type=WindowManager.LayoutParams.TYPE_PHONE;
        }else{
            type =WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS|
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.BOTTOM | Gravity.END;
        windowManager.addView(floatView,params);
        windowManager.addView(floatView2,params);

        floatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }


    /**
     * Configuration floating action buttion action
     * 플로팅 뷰를 클릭 시 나머지 창들이 생기는 액션
     */


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(chatHeadService!=null){
            return START_STICKY;
        }

        return START_REDELIVER_INTENT;

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
    }


    @Override
    public IBinder onBind(Intent intent) { return null;}

//    @Override
//    public void onDestroy() {
//            if(chatHeadService!=null){
//
//            }
//            super.onDestroy();
//    }
}
