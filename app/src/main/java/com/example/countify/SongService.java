package com.example.countify;

import android.content.Context;
import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class SongService extends Service<List<Song>> {
    private final String TAG = Shared.TAG + "SongService";
    private static int limit = 50;
    private Set<Song> playlist = new HashSet<>();
    private static final String ENDPOINT = "https://api.spotify.com/v1/me/tracks?limit=" + limit;
    public SongService(Context context) {
        super(context);
    }

    private Boolean isValid(List<Song> songList, int time) {
        int totalTime = songList.stream()
                .map(Song::getDuration_ms)
                .reduce(0, Integer::sum);
        Log.d(TAG,"totalTime "+totalTime);
        Log.d(TAG,"enteredTime "+time);

        if (time > totalTime) {
            return false;
        }
        else {
            return true;
        }
    }
    //We need to recurse until we get a request with less than 50 songs
    private void recurse(VolleyCallBack<List<Song>> callback, ArrayList<Song> totalSongs, int offset) {
        Log.d("Ben-testing", "here");
        String token = decryptToken();
        getInner(songs -> {
            totalSongs.addAll(songs);
            if (songs.size() < limit) {
                callback.onSuccess(totalSongs);
            }
            else {
                recurse(callback, totalSongs, offset + limit);
            }
        }, ENDPOINT + "&offset=" + offset, token);
    }
    private void getInner(VolleyCallBack<List<Song>> volleyCallBack, String endpoint, String token) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(endpoint, null, response -> {
            Log.d(TAG, response.toString());
            volleyCallBack.onSuccess(convertToSongs(response));
        }, error -> get((user) -> {

        })) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        VolleySingleton.sharedInstance(context).getRequestQueue().add(jsonObjectRequest);
    }
    private List<Song> convertToSongs(JSONObject response) {
        ArrayList<Song> songs = new ArrayList<>();
        try {
            JSONArray itemsArray = response.optJSONArray("items");
            if (itemsArray != null) {
                for (int i = 0; i< itemsArray.length(); i++) {
                    JSONObject track = itemsArray.getJSONObject(i)
                            .getJSONObject("track");
                    Song song = new Gson().fromJson(track.toString(), Song.class);
                    song.parseSong();
                    songs.add(song);
                }
            }
            return songs;
        } catch (JSONException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            return new ArrayList<>();
        }
    }
    public void findClosestSongs(VolleyCallBack<Set<Song>> callback, int time,List<Song> songs) {
        sum_up(callback,songs, time);
    }
    //Returns replacement song
    //Enter real time in the future
    //Pass in playlist BEFORE removal
    public Song replaceSong(Song songToBeReplaced, List<Song> currentPlaylist, List<Song> allSongs) {
        HashSet<Song> playlistSet = new HashSet<>(currentPlaylist);
        int playlistTime = 0;
        for (Song song: playlistSet) {
            playlistTime += song.getDuration_ms();
        }
        int timeAfterRemoval = playlistTime - songToBeReplaced.getDuration_ms();
        ArrayList<Song> songArray = new ArrayList<>(allSongs);
        while (songArray.size() > 0) {
            Song n = songArray.get(new Random().nextInt(songArray.size()));
            songArray.remove(n);
            if (!playlistSet.contains(n)
                    &&  playlistTime <= timeAfterRemoval + n.getDuration_ms() + 5000
                    && playlistTime >= timeAfterRemoval + n.getDuration_ms() - 5000) {
                return n;
            }
        }
        return null;
    }
    void sum_up_recursive(VolleyCallBack<Set<Song>> callback,Set<Song> songSet, int target, Set<Song> partial) {
        int s = 0;
        for (Song x: partial) s += x.getDuration_ms();
        //5 second leighway
        //Only generate list of 10 songs
        if (s <= target + 5000 && s >= target - 5000) {
            playlist = new HashSet<>(partial);
            callback.onSuccess(partial);
        }
        if (s > target + 5000)
            return;
        while (songSet.size() > 0) {
            if (playlist.size() >=1) {
                return;
            }
            Song[] songArray = songSet.toArray(new Song[0]);
            Song n = songArray[new Random().nextInt(songSet.size())];

            songSet.remove(n);
            Set<Song> remaining = new HashSet<>(songSet);

            Set<Song> partial_rec = new HashSet<Song>(partial);
            partial_rec.add(n);
            sum_up_recursive(callback,remaining,target,partial_rec);
        }

    }
    void sum_up(VolleyCallBack<Set<Song>> callback,List<Song> numbers, int target) {
        if (isValid(numbers, target)) {
            sum_up_recursive(callback, new HashSet<>(numbers), target, new HashSet<>());
        }
        else {
            callback.onSuccess(null);
        }
        playlist.clear();
    }
    @Override
    public void get(VolleyCallBack<List<Song>> callback) {
        recurse(callback, new ArrayList<Song>(), 0);
    }


    @Override
    String getTag() {
        return TAG;
    }
}
