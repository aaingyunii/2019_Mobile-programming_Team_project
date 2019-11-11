package com.example.showfloating.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.showfloating.PopupWindow;
import com.example.showfloating.R;

import java.util.ArrayList;

/**
 * Floating view 클릭 시 채팅앱 아이콘이 뜨는 것.
 */

public class FloatingActionService extends Service {

    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;

    private Boolean isPopupOpen = false;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1;
    private int numofM = 0;
    private ArrayList<ImageView> floatList = new ArrayList();
    private ArrayList<Integer> layoutlist = new ArrayList();

    /**
     * Configuration floating action buttion action
     * 플로팅 뷰를 클릭 시 나머지 창들이 생기는 액션
     */

    public void onCreate() {
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        //환경설정을통해서 floatList 내용 정하는 부분 구현(예: facebook 체크, kakao 체크), 메소드 만들어주기
        //환경설정은 아이콘 꾹누르면 체크할 수 있도록
        numofM = 3;
        //최대 플롯버튼수는 정해두기 일단 list로 넣어두고 나중에 동적으로 변경
        layoutlist.add(R.id.fab1);
//        layoutlist.add(R.id.fab2);
//        layoutlist.add(R.id.fab3);
//
//        for (int i = 0; i < numofM; i++) {
//            floatList.add((ImageView) layoutlist.get(i));
//            // 이미지랑 이름은 어플에서 가져와야 함.
//        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final DisplayMetrics metrics = new DisplayMetrics();
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        final LayoutInflater inflater = LayoutInflater.from(this);
        final FrameLayout floatVIew = (FrameLayout) inflater.inflate(R.layout.widget_floating, null, false);


        floatVIew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
            }
        });

        return START_REDELIVER_INTENT;
    }

//    public void onClick(View v) {
////        Log.d(TAG, getString(R.string.chathead_click_message));
//        int id = v.getId();
//        switch (id) {
//            case R.id.fab1:
//                showPopup();
//                break;
//            case R.id.fab2:
//                showPopup();
//                break;
//            case R.id.fab3:
//                showPopup();
//                break;
//        }
//    }

    public void anim() {

        if (isFabOpen) {
            for (int i = 0; i < numofM; i++) {
                fab1.startAnimation(fab_close);
                fab1.setClickable(false);
//                floatList.get(i).startAnimation(fab_close);
//                floatList.get(i).setClickable(false);
            }
            showPopup();
            isFabOpen = false;
        } else{
            fab1.startAnimation(fab_open);
            fab1.setClickable(true);
//            for (int i = 0; i < numofM; i++) {
//                floatList.get(i).startAnimation(fab_open);
//                floatList.get(i).setClickable(true);
//            }

            isFabOpen = true;
        }
    }

    //show popup window
    public void showPopup() {
        Intent intent = new Intent(this, PopupWindow.class);
        //intent.putExtra("name",)
        if (isPopupOpen == false) {



            startService(intent);
            isPopupOpen = true;
        } else {
            stopService(new Intent(this, PopupWindow.class));
            isPopupOpen = false;
        }

    }

    @Override
    public IBinder onBind(Intent intent) { return null;}

}
