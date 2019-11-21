package com.example.notificationbadge;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    NotificationBadge mBadge;
    private int mCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBadge = (NotificationBadge) findViewById(R.id.badge);

        Button increase = (Button) findViewById(R.id.increase);
        increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBadge.setNumber(++mCount);
            }
        });

        Button many = (Button) findViewById(R.id.many);
        many.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCount = 98;
                mBadge.setNumber(mCount);
            }
        });

        Button clear = (Button) findViewById(R.id.clear);
        clear .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCount = 0;
                mBadge.setNumber(mCount);
            }
        });
    }
}