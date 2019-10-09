package com.example.munmo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

/**
 * Delete action screen after starting from notification。
 */
public class DeleteActionActivity extends AppCompatActivity {

    /**
     * Configuration Fragment Tags
     */
    private static final String FRAGMENT_TAG_DELETE_ACTION = "delete_action";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_action);

        if (savedInstanceState == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.container, DeleteActionFragment.newInstance(), FRAGMENT_TAG_DELETE_ACTION);
            ft.commit();
        }

    }
}