package com.example.showfloating.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.lib.FloatingViewManager;
import com.example.showfloating.R;
import com.example.showfloating.service.ChatHeadService;


/**
 * Fragment that will be the main screen of the FloatingView.。
 */
public class FloatingViewControlFragment extends Fragment {

    /**
     * Debugging Log Tags
     */
    private static final String TAG = "FloatingViewControl";

    /**
     * Permission Allow Code for Flows to Display Simple FloatingView
     */
    private static final int CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;

    /**
     * Permission Allow Code for Flows to Display Customized FloatingView
     */
    private static final int CUSTOM_OVERLAY_PERMISSION_REQUEST_CODE = 101;

    /**
     * Generate FloatingViewControlFragment。
     */
    public static FloatingViewControlFragment newInstance() {
        final FloatingViewControlFragment fragment = new FloatingViewControlFragment();
        return fragment;
    }

    /**
     * constructor
     */
    public FloatingViewControlFragment() {
        // Required empty public constructor
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_floating_view_control, container, false);
        // View demos
        rootView.findViewById(R.id.float_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFloatingView(getActivity(), true, false);
            }
        });

        // Displaying the Configuration Screen
        rootView.findViewById(R.id.show_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, FloatingViewSettingsFragment.newInstance());
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        return rootView;
    }

    /**
     * Process Overlay Display Permissions。
     */

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE) {
            showFloatingView(getActivity(), false, false);
        } else if (requestCode == CUSTOM_OVERLAY_PERMISSION_REQUEST_CODE) {
            showFloatingView(getActivity(), false, true);
        }
    }

    /**
     * View FloatingView
     *
     * @param context                 Context
     * @param isShowOverlayPermission Flag that displays screen permissions when it cannot be displayed
     */
    @SuppressLint("NewApi")
    private void showFloatingView(Context context, boolean isShowOverlayPermission, boolean isCustomFloatingView) {
        // Check for API22 or lower
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            startFloatingViewService(getActivity());
            return;
        }

        // Check if it can be displayed on top of other apps
        if (Settings.canDrawOverlays(context)) {
            startFloatingViewService(getActivity());
            return;
        }

        // View Overlay Permissions
        if (isShowOverlayPermission) {
            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
            startActivityForResult(intent, isCustomFloatingView ? CUSTOM_OVERLAY_PERMISSION_REQUEST_CODE : CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Start floating view service
     *
     * @param activity  {@link Activity}
     */
    private static void startFloatingViewService(Activity activity) {
        // *** You must follow these rules when obtain the cutout(FloatingViewManager.findCutoutSafeArea) ***

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 1. 'windowLayoutInDisplayCutoutMode' do not be set to 'never'
            if (activity.getWindow().getAttributes().layoutInDisplayCutoutMode == WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER) {
                throw new RuntimeException("'windowLayoutInDisplayCutoutMode' do not be set to 'never'");
            }
            // 2. Do not set Activity to landscape
            if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                throw new RuntimeException("Do not set Activity to landscape");
            }
        }

        // launch service
        final Class<? extends Service> service;
        final String key;
        service = ChatHeadService.class;
        key = ChatHeadService.EXTRA_CUTOUT_SAFE_AREA;

        final Intent intent = new Intent(activity, service);
        intent.putExtra(key, FloatingViewManager.findCutoutSafeArea(activity));
        ContextCompat.startForegroundService(activity, intent);
    }
}