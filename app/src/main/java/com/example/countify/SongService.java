package com.example.countify;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.RequestQueue;

import java.util.ArrayList;
import java.util.List;

public class SongService extends Service<List<Song>> {
    private ArrayList<Song> songs = new ArrayList<>();

    public SongService(Context context) {
        super(context);
    }

    public List<Song> getItem() {
        return songs;
    }

    //Possibly use strategy pattern
    public void get(VolleyCallBack volleyCallBack) {

    }

    @Override
    String getTag() {
        return null;
    }
}
