package com.example.floattest;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, facebook, kakao, instagram;
    private Boolean isPopupOpen = false;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        facebook = (FloatingActionButton) findViewById(R.id.fab1);
        kakao = (FloatingActionButton) findViewById(R.id.fab2);
        instagram = (FloatingActionButton) findViewById(R.id.fab3);

        fab.setOnClickListener(this);
        facebook.setOnClickListener(this);
        kakao.setOnClickListener(this);
        instagram.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                anim();
                Toast.makeText(this, "Floating Action Button", Toast.LENGTH_SHORT).show();
                checkPermission();
                break;
            case R.id.fab1:
                Toast.makeText(this, "facebook", Toast.LENGTH_SHORT).show();
                showPopup();
                break;
            case R.id.fab2:
                Toast.makeText(this, "kakao", Toast.LENGTH_SHORT).show();
                showPopup();
                break;
            case R.id.fab3:
                Toast.makeText(this,"instagram",Toast.LENGTH_SHORT).show();
                showPopup();
                break;
        }


    }

    public void anim() {

        if (isFabOpen) {
            facebook.startAnimation(fab_close);
            kakao.startAnimation(fab_close);
            instagram.startAnimation(fab_close);

            facebook.setClickable(false);
            kakao.setClickable(false);
            instagram.setClickable(false);

            isFabOpen = false;
        } else {
            facebook.startAnimation(fab_open);
            kakao.startAnimation(fab_open);
            instagram.startAnimation(fab_open);

            facebook.setClickable(true);
            kakao.setClickable(true);
            instagram.setClickable(true);

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
        if(isPopupOpen == false){
            startService(new Intent(MainActivity.this,PopupWindow.class));
            isPopupOpen = true;
        }else{
            stopService(new Intent(MainActivity.this,PopupWindow.class));
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

}