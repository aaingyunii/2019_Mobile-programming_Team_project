package com.example.floattest;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.HashMap;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    private Context mContext = null ;
    ChatFragment tab;
    ArrayList tab_list ;
    HashMap<Integer,ChatFragment> chatFragments = new HashMap();

    public PagerAdapter(FragmentManager fm, int NumOfTabs, Context c) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        mContext = c;

    }
    public void getPositionList(ArrayList arr){
        this.tab_list = arr;
    }


    @Override
    public Fragment getItem(int position) {
        tab = new ChatFragment();
        tab.getPosition(position);
        chatFragments.put(position,tab);
        return tab;
        /*
        switch (position) {
            case 0:
                tab1 = new ChatFragment();
                tab1.getPosition(position);
                return tab1;
            case 1:
                tab2 = new ChatFragment();
                tab1.getPosition(position);
                return tab2;
            case 2:
                tab3 = new ChatFragment();
                tab1.getPosition(position);
                return tab3;
            default:
                return null;
        }*/


    }

    //업데이트하는부분 요소들 동적으로 변경해야함

    public void updateFragment(String packName,int position){
        chatFragments.get(position).listUdpate(packName,tab_list.get(position).toString());


    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }


}