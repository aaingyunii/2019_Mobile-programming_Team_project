package com.example.showfloating.fragment;


import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.showfloating.R;

/**
 * Configure FloatingView Settings。
 */
public class FloatingViewSettingsFragment extends PreferenceFragmentCompat {

    /**
     * Generate FloatingViewSettingsFragment。
     *
     * @return FloatingViewSettingsFragment
     */
    public static FloatingViewSettingsFragment newInstance() {
        final FloatingViewSettingsFragment fragment = new FloatingViewSettingsFragment();
        return fragment;
    }

    /**
     * construct
     */
    public FloatingViewSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_floatingview, null);
    }
}
