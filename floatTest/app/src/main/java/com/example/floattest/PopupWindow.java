package com.example.floattest;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class PopupWindow extends AppCompatActivity {

    TextView txtText;
    ConstraintLayout constraintLayout;
    //채팅방 수 임시로 고정
    private ViewPager viewPager;
    private PagerAdapter adapter;
    String packagNmae = "";
    public MyDBHandler myDBHandler ;
    public Context context;
    ArrayList tab_list ;
    public TabLayout tabLayout;
    private static final String SETTINGS_PLAYER_JSON = "settings_item_json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //packName 가져오기
        Intent intent = getIntent();
        this.packagNmae = intent.getStringExtra("packname");

        //콘텍스트 저장
        context = this;
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.window_popup);

        //UI 객체생성
        txtText = (TextView)findViewById(R.id.name);
        constraintLayout = (ConstraintLayout)findViewById(R.id.popup_element);
        //색가져오기
        int color = MessengerListActivity.colorMap.get(packagNmae);

        Log.i("색가져오기",packagNmae+" : "+color);
        //컬러 적용시키기
        Drawable background = this.getResources().getDrawable(R.drawable.round_kakao);
        background.setColorFilter(color,PorterDuff.Mode.SRC_OVER);
        String chatName = intent.getStringExtra("appname");
        txtText.setText(chatName);
        txtText.setTextColor(Color.BLACK);
        constraintLayout.setBackground(background);



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

        tab_list = tabUpdate();

        tabSetting();

    }
    //tab_list 갱신
    ArrayList tabUpdate(){
        //데이터베이스에서 조사해서 탭 수 정하기
        MyDBHandler myDBHandler = MyDBHandler.open(this,"chatlog");
        Cursor cursor = myDBHandler.tabNum(packagNmae);

        tab_list = new ArrayList();
        while (cursor.moveToNext()) {
            if (cursor.getString(0) != null) {
                tab_list.add(cursor.getString(0)) ;
                Log.i("알려줘",cursor.getString(0));
            }
        }
        return tab_list;
    }
    //tab setting
    public void tabSetting(){
        // 프래그먼트 탭
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        for(int i=0;i<tab_list.size();i++){
            tabLayout.addTab(tabLayout.newTab().setText(tab_list.get(i).toString()));
        }

        tabLayout.setTabTextColors(Color.LTGRAY,Color.BLACK);
        tabLayout.setTabGravity(tabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.pager);

        adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), this);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        adapter.setPositionList(tab_list);
        adapter.setPackageName(packagNmae);

        //스와이프 막기

        viewPager.setAdapter(adapter);
        //셀랙트 리스너

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                adapter.updateFragment(packagNmae,viewPager.getCurrentItem());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        //길게 눌렀을때
        final LinearLayout tabStrip = (LinearLayout)tabLayout.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    deleteShow(viewPager.getCurrentItem());
                    adapter.notifyDataSetChanged();
                    return false;
                }
            });
        }
    }
    //삭제 다이얼로그
    void deleteShow(final int position)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete history");
        builder.setMessage("Are you sure you want to clear this chat history?");
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        myDBHandler = MyDBHandler.open(context,"chatlog");
                        MyNotificationListener.replyModel.remove(packagNmae+tab_list.get(position).toString());
                        myDBHandler.delete(packagNmae,tab_list.get(position).toString());
                        myDBHandler.close();
                        tabLayout.removeTabAt(position);
                        tab_list.remove(position);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(),"history is deleted.",Toast.LENGTH_LONG).show();
                        if(tab_list.size()==0){
                            finish();
                            ArrayList<String> DBlist = Array2String.getStringArrayPref(context,SETTINGS_PLAYER_JSON);
                            DBlist.remove(packagNmae);
                            Array2String.setStringArrayPref(context,SETTINGS_PLAYER_JSON,DBlist);
                            myDBHandler.deleteTable(packagNmae);
                        }
                        if(position ==0){
                            viewPager.setCurrentItem(position);
                            adapter.updateFragment(packagNmae,viewPager.getCurrentItem());

                        }
                    }
                });
        builder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    //서비스로부터 브로드케스트 메세지를 받기위한 onResume()
    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("message_to_Activity"));
    }

    //브로드케스트 메세지 리시버
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            String tabName = intent.getStringExtra("title");

                if(tab_list.get(viewPager.getCurrentItem()).equals(tabName)){
                    adapter.updateFragment(message,viewPager.getCurrentItem());
                }else if(tabName.length()!=0){
                    if(!tab_list.contains(tabName)){
                        int position = viewPager.getCurrentItem();
                        tab_list = tabUpdate();

                        tabLayout.removeAllTabs();
                        tabSetting();
                        adapter.notifyDataSetChanged();
                        viewPager.setCurrentItem(position);
                    }

            }


        }
    };
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }
    @Override
    public void onStop(){
        super.onStop();
        Intent intent = new Intent("visibility");
        intent.putExtra("packname",packagNmae);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
/*
    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }

 */
}

