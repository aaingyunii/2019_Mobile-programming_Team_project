package com.example.floattest;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ChatFragment tab1 = new ChatFragment();
                return tab1;
            case 1:
                ChatFragment tab2 = new ChatFragment();
                return tab2;
            case 2:
                ChatFragment tab3 = new ChatFragment();
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