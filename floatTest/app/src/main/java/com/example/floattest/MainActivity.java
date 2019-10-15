package com.example.floattest;

import android.os.Bundle;
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
                break;
            case R.id.fab1:
                Toast.makeText(this, "facebook", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fab2:
                Toast.makeText(this, "kakao", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fab3:
                Toast.makeText(this,"instagram",Toast.LENGTH_SHORT).show();
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
}