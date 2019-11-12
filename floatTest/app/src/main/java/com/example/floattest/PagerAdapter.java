package com.example.floattest;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    private Context mContext = null ;

    public PagerAdapter(FragmentManager fm, int NumOfTabs, Context c) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        mContext = c;

    }


    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ChatFragment tab1 = new ChatFragment();
                Log.i("gangmin","fragment1");
                return tab1;
            case 1:
                ChatFragment tab2 = new ChatFragment();
                Log.i("gangmin","fragment2");
                return tab2;
            case 2:
                ChatFragment tab3 = new ChatFragment();
                Log.i("gangmin","fragment3");
                return tab3;
            default:
                return null;
        }


    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }


}