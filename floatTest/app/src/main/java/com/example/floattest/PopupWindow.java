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
import java.util.Iterator;

public class PopupWindow extends AppCompatActivity {

    TextView txtText;
    ConstraintLayout constraintLayout;
    private ViewPager viewPager;
    private PagerAdapter adapter;
    String packagNmae = "";
    public MyDBHandler myDBHandler;
    public Context context;
    ArrayList tab_list;
    public TabLayout tabLayout;
    private static final String SETTINGS_PLAYER_JSON = "settings_item_json";
    private int position;
    private int check = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get packName
        Intent intent = getIntent();
        this.packagNmae = intent.getStringExtra("packname");

        //get position
        this.position = Integer.parseInt(intent.getStringExtra("packposition"));

        //save context
        context = this;
        //remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.window_popup);

        //create UI object
        txtText = (TextView) findViewById(R.id.name);
        constraintLayout = (ConstraintLayout) findViewById(R.id.popup_element);
        //get the color
        int color = MessengerListActivity.colorMap.get(packagNmae);

        Log.i("get color", packagNmae + " : " + color);
        //apply the color
        Drawable background = this.getResources().getDrawable(R.drawable.round_kakao);
        background.setColorFilter(color, PorterDuff.Mode.SRC_OVER);
        String chatName = intent.getStringExtra("appname");
        txtText.setText(chatName);
        txtText.setTextColor(Color.BLACK);
        constraintLayout.setBackground(background);


        //control size
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        int width = (int) (display.getWidth() * 0.95);
        int height = (int) (display.getHeight() * 0.6);
        getWindow().getAttributes().width = width;
        getWindow().getAttributes().height = height;

        Button bt = (Button) findViewById(R.id.close_bt);
        bt.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                countReset();
                finish();
            }
        });
        //make frame transparently
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        tab_list = tabUpdate();

        tabSetting();

    }

    //update tab_list
    ArrayList tabUpdate() {
        check = 1;

        //investigate in a database and
        //determine the number of tabs
        MyDBHandler myDBHandler = MyDBHandler.open(this, "chatlog");
        Cursor cursor = myDBHandler.tabNum(packagNmae);

        tab_list = new ArrayList();
        while (cursor.moveToNext()) {
            if (cursor.getString(0) != null) {
                tab_list.add(cursor.getString(0));
                Log.i("check", cursor.getString(0));
            }
        }
        return tab_list;
    }

    //count Reset
    public void countReset() {
        //before finish make count to 0
        Log.d("name?", tabLayout.getTabAt(0).getText().toString());
        String name = tabLayout.getTabAt(0).getText().toString();

        if (viewPager.getCurrentItem() == 0) {
            if (name.contains("(")) {
                name = name.substring(0, name.indexOf("("));
            }
            MyNotificationListener.tabCountCover.get(position).put(name, 0);

        }
    }

    //tab setting
    public void tabSetting() {
        //fragment tab
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        for (int i = 0; i < tab_list.size(); i++) {
            int count = (int) MyNotificationListener.tabCountCover.get(position).get(tab_list.get(i).toString());
            if (count == 0) {
                tabLayout.addTab(tabLayout.newTab().setText(tab_list.get(i).toString()));
            } else {
                tabLayout.addTab(tabLayout.newTab().setText(tab_list.get(i).toString() + "(" + count + ")"));
            }

        }

        tabLayout.setTabTextColors(Color.LTGRAY, Color.BLACK);
        tabLayout.setTabGravity(tabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.pager);

        adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), this);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        adapter.setPositionList(tab_list);
        adapter.setPackageName(packagNmae);

        //prevent swipe
        viewPager.setAdapter(adapter);

        //select listener
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int lposition = tab.getPosition();

                viewPager.setCurrentItem(lposition);

                Log.d("select tab?", lposition + "");
                if (lposition != 0) {
                    MyNotificationListener.tabCountCover.get(position).put(tab_list.get(lposition).toString(), 0);
                    tabLayout.getTabAt(lposition).setText(tab_list.get(lposition).toString());
                } else if (check != 1) {
                    MyNotificationListener.tabCountCover.get(position).put(tab_list.get(lposition).toString(), 0);
                    tabLayout.getTabAt(lposition).setText(tab_list.get(lposition).toString());

                }
                check = 0;
                adapter.updateFragment(packagNmae, viewPager.getCurrentItem());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }

        });

        //tab long pressed
        final LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);
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

    //delete dialogs
    void deleteShow(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete history");
        builder.setMessage("Are you sure you want to clear this chat history?");
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        myDBHandler = MyDBHandler.open(context, "chatlog");
                        MyNotificationListener.replyModel.remove(packagNmae + tab_list.get(position).toString());
                        myDBHandler.delete(packagNmae, tab_list.get(position).toString());
                        myDBHandler.close();
                        tabLayout.removeTabAt(position);
                        tab_list.remove(position);

                        if (tab_list.size() == 0) {
                            ArrayList<String> DBlist = Array2String.getStringArrayPref(context, SETTINGS_PLAYER_JSON);
                            DBlist.remove(packagNmae);
                            Array2String.setStringArrayPref(context, SETTINGS_PLAYER_JSON, DBlist);
                            myDBHandler.deleteTable(packagNmae);

                            countReset();

                            finish();
                            return;
                        }

                        int position = viewPager.getCurrentItem();
                        tab_list = tabUpdate();
                        tabLayout.removeAllTabs();
                        tabSetting();

                        adapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), "history is deleted.", Toast.LENGTH_LONG).show();
                        if (position == 0) {
                            viewPager.setCurrentItem(position);
                            adapter.updateFragment(packagNmae, viewPager.getCurrentItem());

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

    //To receive a broadcast message from the service, onResume()
    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("message_to_Activity"));
    }

    //Broadcast message receiver
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            String tabName = intent.getStringExtra("title");


            if (packagNmae.equals(packagNmae) && tab_list.get(viewPager.getCurrentItem()).equals(tabName)) {
                adapter.updateFragment(message, viewPager.getCurrentItem());
            } else if (packagNmae.equals(packagNmae) && tabName.length() != 0) {
                if (!tab_list.contains(tabName)) {
                    int position = viewPager.getCurrentItem();
                    tab_list = tabUpdate();

                    tabLayout.removeAllTabs();
                    tabSetting();
                    adapter.notifyDataSetChanged();
                    viewPager.setCurrentItem(position);
                } else {

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
        //Don't close, when click on the outer layer.
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        Intent intent = new Intent("visibility");
        intent.putExtra("packname", packagNmae);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}

