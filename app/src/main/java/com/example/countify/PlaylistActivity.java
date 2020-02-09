package com.example.countify;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

public class PlaylistActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        PlaylistFragment firstFragment = new PlaylistFragment();

        // Add Fragment to FrameLayout (flContainer), using FragmentManager
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();// begin  FragmentTransaction
        ft.add(R.id.flContainer, firstFragment);                                // add    Fragment
        ft.commit();
    }

    public void setToolbar(String text) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(text);
        }
    }

}
