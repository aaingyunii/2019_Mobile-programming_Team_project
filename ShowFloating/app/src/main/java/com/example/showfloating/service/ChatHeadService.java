package com.example.showfloating.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.lib.FloatingViewListener;
import com.example.lib.FloatingViewManager;
import com.example.showfloating.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


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

    private ChatHeadService mChatheadService;

    private Boolean isFabOpen = false;
    private ImageView iconView, floatView, floatView2, floatView3, floatView4, iconViewSub;
    private int numOfView = 5;
    private ArrayList<ImageView> floatList = new ArrayList();


    /**
     * {@inheritDoc}
     */


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Do nothing if the Manager already exists
        if (mFloatingViewManager != null) {
            return START_STICKY;
        }

        //화면의 해상도를 구한다.
        final DisplayMetrics metrics = new DisplayMetrics();
        //윈도우의 최상위 뷰를 가져온다.
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        final LayoutInflater inflater = LayoutInflater.from(this);

        iconView = (ImageView) inflater.inflate(R.layout.widget_chathead, null, false);
        floatView = (ImageView) inflater.inflate(R.layout.widget_floating, null, false);
        floatView2 = (ImageView) inflater.inflate(R.layout.widget_floating, null, false);
        floatView3 = (ImageView) inflater.inflate(R.layout.widget_floating, null, false);
        floatView4 = (ImageView) inflater.inflate(R.layout.widget_floating, null, false);
        //처음 iconView 클릭시 iconView는 사라진 상태에서 다시 iconView를 불러내기 위한 버튼 홈버튼과 같은 존재
        iconViewSub = (ImageView) inflater.inflate(R.layout.widget_chatheadsub, null, false);


        //동적을 위해 리스트에 각 뷰를 저장
        floatList.add(floatView);
        floatList.add(floatView2);
        floatList.add(floatView3);
        floatList.add(floatView4);
        floatList.add(iconViewSub);

        //각 뷰에 해당하는 버튼 클릭 이벤트 생성
        iconView.setOnClickListener(this);
        iconViewSub.setOnClickListener(this);
        floatView.setOnClickListener(this);
        floatView2.setOnClickListener(this);
        floatView3.setOnClickListener(this);
        floatView4.setOnClickListener(this);


        //파라미터를 이용해 각 뷰의 위치를 정적으로 고정시켜 놓음.
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

        //각 floatView의 위치 넣기
        params.gravity = Gravity.RIGHT | Gravity.CENTER;
        params.y = -400;
        windowManager.addView(floatView, params);//위에서 1번째
        params.y = -200;
        windowManager.addView(floatView2, params);// 2번째
        params.y = 0;
        windowManager.addView(floatView3, params);// 3번째
        params.y = 200;
        windowManager.addView(floatView4, params);// 4번째
        params.y = 400;
        windowManager.addView(iconViewSub, params);

        Drawable icon = null;
        Drawable icon2 = null;
        Drawable icon3 = null;
        Drawable icon4 = null;

        try {
            icon = getPackageManager().getApplicationIcon("com.kakao.talk");
            icon2 = getPackageManager().getApplicationIcon("com.Slack");
            icon3 = getPackageManager().getApplicationIcon("com.facebook.orca");
            icon4 = getPackageManager().getApplicationIcon("com.instagram.android");

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        floatView.setImageDrawable(icon);
        floatView2.setImageDrawable(icon2);
        floatView3.setImageDrawable(icon3);
        floatView4.setImageDrawable(icon4);



        //FloatingViewManager를 이용해 iconView를 윈도우에 추가 및 삭제 액티비티 삽입.
        mFloatingViewManager = new FloatingViewManager(this, this);
        mFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed);
        mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
        mFloatingViewManager.setSafeInsetRect((Rect) intent.getParcelableExtra(EXTRA_CUTOUT_SAFE_AREA));
        final FloatingViewManager.Options options = new FloatingViewManager.Options();
        options.overMargin = (int) (16 * metrics.density);
        mFloatingViewManager.addViewToWindow(iconView, options);
        mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);


        // resident start
        startForeground(NOTIFICATION_ID, createNotification(this));

        return START_REDELIVER_INTENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        destroy();
        Log.d(TAG, "모든 서비스 종료");
        Toast.makeText(this, "모든 서비스 종료", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    /**
     * Remove View.
     */
    private void destroy() {
        if (mFloatingViewManager != null) {
            mFloatingViewManager.removeAllViewToWindow();
            for (int i = 0; i < numOfView; i++)
                ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(floatList.get(i));
            mFloatingViewManager = null;
        }
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
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                anim();
                Toast.makeText(this, "Icon Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fab_sub:
                anim();
                Toast.makeText(this, "Sub Clicked", Toast.LENGTH_SHORT).show();
                break;
        }
        if (floatList.get(0).equals(v)) {
            Toast.makeText(this, "1 Clicked", Toast.LENGTH_SHORT).show();

        } else if (floatList.get(1).equals(v)) {
            Toast.makeText(this, "2 Clicked", Toast.LENGTH_SHORT).show();

        } else if (floatList.get(2).equals(v)) {
            Toast.makeText(this, "3 Clicked", Toast.LENGTH_SHORT).show();

        } else if (floatList.get(3).equals(v)) {
            Toast.makeText(this, "4 Clicked", Toast.LENGTH_SHORT).show();

        }

    }

    public void anim() {
        if (isFabOpen) {
            iconView.setVisibility(View.VISIBLE);
            iconView.setOnClickListener(this);
            iconView.setClickable(true);
            for (int i = 0; i < numOfView; i++) {
                floatList.get(i).setVisibility(View.INVISIBLE);
                floatList.get(i).setClickable(false);
            }

            isFabOpen = false;

        } else {
            iconView.setVisibility(View.INVISIBLE);
            iconView.setOnClickListener(null);
            iconView.setClickable(false);
            for (int i = 0; i < numOfView; i++) {
                floatList.get(i).setVisibility(View.VISIBLE);
                floatList.get(i).setClickable(true);
            }
            isFabOpen = true;
        }

    }

    private static Notification createNotification(Context context) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.default_floatingview_channel_id));
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(context.getString(R.string.chathead_content_title));
        builder.setContentText(context.getString(R.string.content_text));
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);

        return builder.build();
    }


}
