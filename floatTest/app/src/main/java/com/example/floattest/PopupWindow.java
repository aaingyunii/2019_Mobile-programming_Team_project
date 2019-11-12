package com.example.floattest;


import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class PopupWindow extends AppCompatActivity {

    TextView txtText;
    //채팅방 수 임시로 고정
    private int chat_num = 2;


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
        //테두리 투명하게
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // 프래그먼트 탭
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        for(int i=0;i<chat_num;i++){
            tabLayout.addTab(tabLayout.newTab().setText(""+(i+1)));

        }
        tabLayout.setTabTextColors(Color.LTGRAY,Color.BLUE);
        tabLayout.setTabGravity(tabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        final PagerAdapter adpater = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), this);
        Log.i("gangmin","before");
        viewPager.setAdapter(adpater);
        Log.i("gangmin","after");
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        //셀랙트 리스너
        /*
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

         */



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

