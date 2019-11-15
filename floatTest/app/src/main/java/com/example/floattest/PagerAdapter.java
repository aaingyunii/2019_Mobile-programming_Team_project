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
    ChatFragment tab1, tab2, tab3;

    public PagerAdapter(FragmentManager fm, int NumOfTabs, Context c) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        mContext = c;

    }


    @Override
    public Fragment getItem(int position) {
        Log.i("지금인가?","ㅇㅇ");
        switch (position) {
            case 0:
                tab1 = new ChatFragment();
                return tab1;
            case 1:
                tab2 = new ChatFragment();
                return tab2;
            case 2:
                tab3 = new ChatFragment();
                return tab3;
            default:
                return null;
        }


    }
    //업데이트하는부분 요소들 동적으로 변경해야함
    public void updateFragment(String packName){
        if(packName.equals("comkakaotalk")){
            tab1.listUdpate();}
        else{
            //tab2.listUdpate();
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }


}