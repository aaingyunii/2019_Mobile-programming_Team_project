package com.example.floattest;


import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    private Context mContext = null ;
    ChatFragment tab;
    ArrayList tab_list ;
    HashMap<Integer,ChatFragment> chatFragments = new HashMap();
    String packName;

    public PagerAdapter(FragmentManager fm, int NumOfTabs, Context c) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        mContext = c;

    }
    public void setPositionList(ArrayList arr){
        this.tab_list = arr;
    }
    public void setPackageName(String packName){
        this.packName = packName;
    }

    @Override
    public Fragment getItem(int position) {
        tab = new ChatFragment();
        tab.setInfo(position,tab_list,packName);

        chatFragments.put(position,tab);
        return tab;
    }

    //Components of the update must be dynamically changed
    public void updateFragment(String packName,int position){
        try {

            chatFragments.get(position).listUdpate(packName, tab_list.get(position).toString());
        }catch (Exception e){
            Log.i("error check", tab_list.get(position) + "");
        }
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }



}