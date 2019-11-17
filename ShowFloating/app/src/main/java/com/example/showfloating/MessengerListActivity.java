package com.example.showfloating;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

public class MessengerListActivity extends AppCompatActivity {
    PackageManager packageManager;
    ListView apkList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger_list);

        packageManager = getPackageManager();
        List<PackageInfo> packageList = packageManager
                .getInstalledPackages(PackageManager.GET_PERMISSIONS);

        final List<PackageInfo> msList = new ArrayList<PackageInfo>();

        String filter = "com.kakao.talk";
        String filter2 = "com.facebook.orca";
        String filter3 = "com.instagram.android";
        String filter4 = "com.Slack";

        //카카오톡, 인스타그램, 페톡, 슬랙의 package 주소를 직접넣어
        //전체 PackageInfo에서 이들만을 필터링하여 msList에 삽입.
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

}
