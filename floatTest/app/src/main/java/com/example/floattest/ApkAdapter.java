package com.example.floattest;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

public class ApkAdapter extends BaseAdapter {
    List<PackageInfo> msList;
    Activity context;
    PackageManager packageManager;

    public ApkAdapter(Activity context, List<PackageInfo> msList, PackageManager packageManager) {
        super();
        this.context = context;
        this.msList = msList;
        this.packageManager = packageManager;
    }


    private class ViewHolder {
        TextView apkName;
    }

    public int getCount() {
        return msList.size();
    }

    public Object getItem(int position) {
        return msList.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = context.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.apklist_item, null);
            holder = new ViewHolder();

            holder.apkName = (TextView) convertView.findViewById(R.id.appname);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final PackageInfo packageInfo = (PackageInfo) getItem(position);

        //Add imported app icon
        Drawable appIcon = packageManager.getApplicationIcon(packageInfo.applicationInfo);
        //Add imported app name
        String appName = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
        //Fit size
        appIcon.setBounds(0, 0, 80, 80);
        //Insert to list
        holder.apkName.setCompoundDrawables(appIcon, null, null, null);
        holder.apkName.setCompoundDrawablePadding(5);
        holder.apkName.setText(appName);

        Switch switchButton = (Switch) convertView.findViewById(R.id.msSwitch);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b == true) {
                    if (!MessengerListActivity.result.contains(packageInfo))
                        MessengerListActivity.result.add(packageInfo);
                } else {
                    if (MessengerListActivity.result.contains(packageInfo))
                        MessengerListActivity.result.remove(packageInfo);
                }
            }
        });

        return convertView;
    }
}
