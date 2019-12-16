package com.example.floattest;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessengerListActivity extends AppCompatActivity {
    PackageManager packageManager;
    ListView apkList;
    static List<PackageInfo> msList;
    static List<PackageInfo> result;
    static HashMap<String,Integer> colorMap = new HashMap();
    //Apps to be used for this service
    static String[] packNameList = {"com.kakao.talk","com.facebook.orca","com.instagram.android","com.Slack"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //delete status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_messenger_list);

        packageManager = getPackageManager();
        List<PackageInfo> packageList = packageManager
                .getInstalledPackages(PackageManager.GET_PERMISSIONS);

        msList = new ArrayList<PackageInfo>();
        result = new ArrayList<PackageInfo>();

        String filter = packNameList[0];
        String filter2 = packNameList[1];
        String filter3 = packNameList[2];
        String filter4 = packNameList[3];

        //color hashmap
        colorMap.put(filter.replaceAll("\\.", ""), ContextCompat.getColor(this,R.color.comkakaotalk));
        colorMap.put(filter2.replaceAll("\\.", ""),ContextCompat.getColor(this,R.color.comfacebookorca));
        colorMap.put(filter3.replaceAll("\\.", ""),ContextCompat.getColor(this,R.color.cominstagramandroid));
        colorMap.put(filter4.replaceAll("\\.", ""),ContextCompat.getColor(this,R.color.comSlack));

        //Put the package address of Kakao Talk, Instagram, Petok, and Slag directly.
        //Filter and insert only these into the msList from the entire PackageInfo.
        for (PackageInfo pi : packageList) {
            boolean b = isSystemPackage(pi);
            if (!b) {
                if (pi.packageName.equals(filter))
                    msList.add(pi);
                else if (pi.packageName.equals(filter2))
                    msList.add(pi);
                else if (pi.packageName.equals(filter3))
                    msList.add(pi);
                else if (pi.packageName.equals(filter4))
                    msList.add(pi);
            }
        }

        apkList = (ListView) findViewById(R.id.applist);
        apkList.setAdapter(new ApkAdapter(this, msList, packageManager));

    }

    /**
     * Return whether the given PackgeInfo represents a system package or not.
     * User-installed packages (Market or otherwise) should not be denoted as
     * system packages.
     *
     * @param pkgInfo
     * @return boolean
     */

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true : false;
    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.packInfoList = result;
    }

}
