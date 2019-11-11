package com.example.floattest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

public class PopupWindow extends Activity {

    TextView txtText;
    private ViewPager  mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.window_popup);

        //UI 객체생성
        txtText = (TextView)findViewById(R.id.name);

        //데이터 가져오기
        /*
        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        txtText.setText(data);
         */
        //크기조절
        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        int width = (int)(display.getWidth()*0.95);
        int height = (int) (display.getHeight() * 0.6);
        getWindow().getAttributes().width = width;
        getWindow().getAttributes().height = height;

        Button bt = (Button)findViewById(R.id.close_bt);
        bt.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                finish();
            }
        });

        // 뷰페이저
        mViewPager = (ViewPager) findViewById(R.id.container);


    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }
/*
    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }

 */
}

