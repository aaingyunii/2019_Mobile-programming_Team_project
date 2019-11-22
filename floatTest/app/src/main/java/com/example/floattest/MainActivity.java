package com.example.floattest;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.library.FloatingViewManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab;
    private Boolean isPopupOpen = false;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1;
    private int numofM ;
    private ArrayList<FloatingActionButton> floatList = new ArrayList();
    private ArrayList<Integer> layoutlist = new ArrayList();

    static List<PackageInfo> packInfoList = null;


    /**
     * Permission Allow Code for Flows to Display Simple FloatingView
     */
    private static final int CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;

    /**
     * Permission Allow Code for Flows to Display Customized FloatingView
     */
    private static final int CUSTOM_OVERLAY_PERMISSION_REQUEST_CODE = 101;




    private static final String SETTINGS_PLAYER_JSON = "settings_item_json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create default notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String channelId = getString(R.string.default_floatingview_channel_id);
            final String channelName = getString(R.string.default_floatingview_channel_name);
            final NotificationChannel defaultChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN);
            final NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(defaultChannel);
            }
        }


        //버튼달기
        Button start_button = (Button)findViewById(R.id.start_button);
        Button setting_button = (Button)findViewById(R.id.setting_button);

        start_button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(packInfoList == null|| packInfoList.size()==0){
                    Toast.makeText(MainActivity.this, "empty, go to setting page.", Toast.LENGTH_SHORT).show();
                }else {
                    showFloatingView(MainActivity.this,true,false);
                }
            }
        });

        setting_button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,MessengerListActivity.class);
                startActivityForResult(intent,111);
            }
        });





        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        fab = (FloatingActionButton) findViewById(R.id.fab);


        //환경설정을통해서 floatList 내용 정하는 부분 구현(예: facebook 체크, kakao 체크), 메소드 만들어주기
        //환경설정은 아이콘 꾹누르면 체크할 수 있도록
        numofM = 1;
        //최대 플롯버튼수는 정해두기 일단 list로 넣어두고 나중에 동적으로 변경
        layoutlist.add(R.id.fab1);
        layoutlist.add(R.id.fab2);
        layoutlist.add(R.id.fab3);
        for(int i =0;i< numofM; i++){
            FloatingActionButton bt;
            floatList.add((FloatingActionButton) findViewById(layoutlist.get(i)));
            // 이미지랑 이름은 어플에서 가져와야 함.
        }

        //클릭 리스너 설정
        fab.setOnClickListener(this);
        for(int i =0;i< numofM; i++){
            floatList.get(i).setOnClickListener(this);
        }

        //notification listener 생성
        Intent intent = new Intent(MainActivity.this,MyNotificationListener.class);
        startService(intent);



    }

    //플로팅 뷰 켜기
    private void showFloatingView(Context context, boolean isShowOverlayPermission, boolean isCustomFloatingView) {
        // Check for API22 or lower
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            startFloatingViewService(MainActivity.this);
            return;
        }

        // Check if it can be displayed on top of other apps
        if (Settings.canDrawOverlays(context)) {
            startFloatingViewService(MainActivity.this);
            return;
        }

        // View Overlay Permissions
        if (isShowOverlayPermission) {
            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
            startActivityForResult(intent, isCustomFloatingView ? CUSTOM_OVERLAY_PERMISSION_REQUEST_CODE : CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    //플로팅뷰 서비스
    private static void startFloatingViewService(Activity activity) {
        // *** You must follow these rules when obtain the cutout(FloatingViewManager.findCutoutSafeArea) ***

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 1. 'windowLayoutInDisplayCutoutMode' do not be set to 'never'
            if (activity.getWindow().getAttributes().layoutInDisplayCutoutMode == WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER) {
                throw new RuntimeException("'windowLayoutInDisplayCutoutMode' do not be set to 'never'");
            }
            // 2. Do not set Activity to landscape
            if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                throw new RuntimeException("Do not set Activity to landscape");
            }
        }

        // launch service
        final Class<? extends Service> service;
        final String key;
        service = ChatHeadService.class;
        key = ChatHeadService.EXTRA_CUTOUT_SAFE_AREA;

        final Intent intent = new Intent(activity,service);
        intent.putExtra(key, FloatingViewManager.findCutoutSafeArea(activity));
        ContextCompat.startForegroundService(activity, intent);
    }


    //브로드케스트로부터 메세지 받기
    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("message_to_Activity"));
    }

    //브로드케스트 메세지 리시버
    //이걸로 최신 리스트를 유지해야 한다
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");

            ArrayList<String> DBlist = Array2String.getStringArrayPref(context,SETTINGS_PLAYER_JSON);
            if(DBlist==null)
                DBlist = new ArrayList();
            if(!DBlist.contains(message)){
                DBlist.add(message);
            }
            else{
                DBlist.remove(message);
                DBlist.add(message);
            }
            Array2String.setStringArrayPref(context,SETTINGS_PLAYER_JSON,DBlist);
        }
    };
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                anim();
                Toast.makeText(this, "Floating Action Button", Toast.LENGTH_SHORT).show();
                checkPermission();
                //permission check for notification
                boolean isPermissionAllowed = isNotiPermissionAllowed();
                if(!isPermissionAllowed){
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivity(intent);
                }
                break;
            case R.id.fab1:
                ArrayList<String> DBlist = Array2String.getStringArrayPref(this,SETTINGS_PLAYER_JSON);
                if(DBlist.contains("comkakaotalk")){
                    showPopup();
                }else{
                    Toast.makeText(this, "no message", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.fab2:
                showPopup();
                break;
            case R.id.fab3:
                showPopup();
                break;
        }


    }

    public void anim() {

        if (isFabOpen) {
            for(int i=0;i<numofM;i++){
                floatList.get(i).startAnimation(fab_close);
                floatList.get(i).setClickable(false);
            }
            if(isPopupOpen==true)
                showPopup();
            isFabOpen = false;
        } else {

            for(int i=0;i<numofM;i++){
                floatList.get(i).startAnimation(fab_open);
                floatList.get(i).setClickable(true);
            }

            isFabOpen = true;
        }
    }
    //check permission
    public void checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 체크
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }
    }


    //show popup window
    public void showPopup(){

        Intent intent = new Intent(MainActivity.this,PopupWindow.class);
        //intent.putExtra("name",)
        if(isPopupOpen == false){
            startActivity(intent);
            anim();
            //isPopupOpen = true;
        }else{
            isPopupOpen = false;
        }

    }
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // TODO 동의를 얻지 못했을 경우의 처리
            }
            else {
                startService(new Intent(MainActivity.this, PopupWindow.class));

            }
        }
    }
    //permision check
    private boolean isNotiPermissionAllowed() {
        Set<String> notiListenerSet = NotificationManagerCompat.getEnabledListenerPackages(this);
        String myPackageName = getPackageName();

        for(String packageName : notiListenerSet) {
            if(packageName == null) {
                continue;
            }
            if(packageName.equals(myPackageName)) {
                return true;
            }
        }

        return false;
    }




}