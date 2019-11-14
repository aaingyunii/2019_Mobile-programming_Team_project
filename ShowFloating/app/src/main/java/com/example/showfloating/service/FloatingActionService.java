package com.example.showfloating.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.Image;
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
import android.widget.Toast;

import com.example.showfloating.R;

/**
 * Floating view 클릭 시 채팅앱 아이콘이 뜨는 것.
 */

public class FloatingActionService extends Service implements View.OnClickListener{

    private ChatHeadService chatHeadService;
    private String TAG = "FloatingViewService";
    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private Boolean isPopupOpen = false;
    private ImageView floatView,floatView2,floatView3,floatView4;

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
        final ImageView iconView = (ImageView) inflater.inflate(R.layout.widget_chathead,null,false);
        floatView = (ImageView) inflater.inflate(R.layout.widget_floating,null,false);
        floatView2 = (ImageView) inflater.inflate(R.layout.widget_floating,null,false);
        floatView3 = (ImageView) inflater.inflate(R.layout.widget_floating,null,false);
        floatView4 = (ImageView) inflater.inflate(R.layout.widget_floating,null,false);

        int type=0;
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.O){
            type=WindowManager.LayoutParams.TYPE_PHONE;
        }else{
            type =WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        WindowManager.LayoutParams params1 = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS|
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);

        //파라미터를 이용해 각 뷰의 위치를 정적으로 고정시켜 놓음.
        params1.gravity = Gravity.RIGHT | Gravity.CENTER;
        params1.y=-400;
        windowManager.addView(floatView,params1);//위에서 1번째
        params1.y=-200;
        windowManager.addView(floatView2,params1);//위에서 2번째
        params1.y=0;
        windowManager.addView(floatView3,params1);//위에서 3번째
        params1.y=200;
        windowManager.addView(floatView4,params1);//4번째

        //미완성
        iconView.setOnClickListener(this);
//        floatView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(FloatingActionService.this,"Click Kakao",Toast.LENGTH_SHORT).show();
//            }
//        });
//        floatView2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(FloatingActionService.this,"Click Slack",Toast.LENGTH_SHORT).show();
//            }
//        });
//        floatView3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(FloatingActionService.this,"Click Facebook",Toast.LENGTH_SHORT).show();
//            }
//        });
//        floatView4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(FloatingActionService.this,"Click Instagram",Toast.LENGTH_SHORT).show();
//            }
//        });

        floatView.setOnClickListener(this);
        floatView2.setOnClickListener(this);
        floatView3.setOnClickListener(this);
        floatView4.setOnClickListener(this);
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

    //애니메이션 not working yet
    @Override
    public void onClick(View v) {
        int id =v.getId();
        switch (id){
            case R.layout.widget_chathead:
                anim();
                Toast.makeText(this, "Floating View", Toast.LENGTH_SHORT).show();
                break;
            case R.layout.widget_floating:
                anim();
                Toast.makeText(this, "Floating vv", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //애니메이션 not working yet
    public void anim() {
        if (isFabOpen) {
            floatView.startAnimation(fab_close);
            floatView2.startAnimation(fab_close);
            floatView3.startAnimation(fab_close);
            floatView4.startAnimation(fab_close);
            floatView.setClickable(false);
            floatView2.setClickable(false);
            floatView3.setClickable(false);
            floatView4.setClickable(false);

            isFabOpen = false;

        } else {
            floatView.startAnimation(fab_open);
            floatView2.startAnimation(fab_open);
            floatView3.startAnimation(fab_open);
            floatView4.startAnimation(fab_open);
            floatView.setClickable(true);
            floatView2.setClickable(true);
            floatView3.setClickable(true);
            floatView4.setClickable(true);

            isFabOpen = true;
        }
    }
//    @Override
//    public void onDestroy() {
//            if(chatHeadService!=null){
//
//            }
//            super.onDestroy();
//    }
}
