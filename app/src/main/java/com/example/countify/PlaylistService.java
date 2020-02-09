package com.example.countify;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlaylistService extends Service<Playlist> {
    private static final String TAG = Shared.TAG + "PlaylistService";
    private static String ENDPOINT_USERS = "https://api.spotify.com/v1/users/";
    private static String ENDPOINT_PLAYLISTS = "https://api.spotify.com/v1/playlists/";

    private String userId;
    public PlaylistService(Context context) {
        super(context);
        userId = context.getSharedPreferences(Shared.COUNTIFY, 0).getString(Shared.USERID, "");
    }
    private String getEditUrl(String playlistId) {
        return ENDPOINT_PLAYLISTS + playlistId;
    }
    private String getCreateUrl(String userId) {
        return ENDPOINT_USERS + userId + "/playlists";
    }
    private String getAddUrl(String playlistId) {
        return ENDPOINT_PLAYLISTS + playlistId + "/tracks";
    }

    private void accumulateTracks(VolleyCallBack<Void> callback, Playlist playlist, int position, List<Song> songs) {
        Log.d(TAG, "Size of playlist " + songs.size());
        if (position >= songs.size()-1) {
            callback.onSuccess(null);
            return;
        }
        int requestSongSize = Math.min(songs.size()-position, 100);
        ArrayList<Song> partial = new ArrayList<>();
        for (int i = position; i < position + requestSongSize; i++) {
            partial.add(songs.get(i));
        }
        addTracks(aVoid -> {
            accumulateTracks(callback, playlist, position + requestSongSize, songs);
        }, playlist, position, partial);

//        addTracks(position -> {
//            if (position >= songs.size()-1) {
//                callback.onSuccess(null);
//            }
//            else {
//                accumulateTracks(callback, playlist, position, );
//            }
//        }, playlist, position1, partial);
    }
    //Add error handling here
    private void addTracks(VolleyCallBack<Void> callback, Playlist playlist, int position, ArrayList<Song> partial) {
        Log.d(TAG, "Adding " + partial.size() + " tracks " + " at position " + position);
        JSONObject body;
        JSONArray uriArray = new JSONArray();
        for (Song song: partial) {
            uriArray.put(song.getUri());
        }

        try {
            body = new JSONObject()
                    .putOpt("uris", uriArray)
                    .put("position", position);
        }
        catch (JSONException e) {
            Log.d(TAG, e.getMessage());
            body = null;
        }
        Log.d(TAG, body.toString());
        Log.d(TAG, playlist.getId());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getAddUrl(playlist.getId()), body, response -> {
            callback.onSuccess(null);
        }, error -> get((user) -> {

        })) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String auth = "Bearer " + decryptToken();
                headers.put("Authorization", auth);
                headers.put("Content-type", "application/json");
                return headers;
            }
        };
        VolleySingleton.sharedInstance(context).getRequestQueue().add(jsonObjectRequest);

    }
    private void createPlaylist(VolleyCallBack<Playlist> callback, String name, String description, boolean isPublic) {
        JSONObject body = null;
        try {
            body = new JSONObject()
                        .put("name", name)
                        .put("description", description)
                        .put("public", Boolean.toString(isPublic));
        } catch (JSONException e) {
            callback.onSuccess(null);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getCreateUrl(userId), body, response -> {
            Log.d(TAG, response.toString());

            Playlist playlist = new Gson().fromJson(response.toString(), Playlist.class);
            try {
                playlist.setExternal_url(response.getJSONObject("external_urls").getString("spotify"));
            } catch (JSONException e) {
                Log.d(TAG, e.getMessage());
            }
            callback.onSuccess(new Gson().fromJson(response.toString(), Playlist.class));
        }, error -> Log.d(TAG, "" +error.networkResponse.statusCode)) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String auth = "Bearer " + decryptToken();
                headers.put("Authorization", auth);
                return headers;
            }
        };
        VolleySingleton.sharedInstance(context).getRequestQueue().add(jsonObjectRequest);
    }

    public void editPlaylist(VolleyCallBack<Void> callback,String playlistId ,String name, String description, boolean isPublic) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, getEditUrl(playlistId), null, response -> {
            callback.onSuccess(null);
        }, error -> get((user) -> {

        })) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String auth = "Bearer " + decryptToken();
                headers.put("Authorization", auth);
                return headers;
            }
        };
        VolleySingleton.sharedInstance(context).getRequestQueue().add(jsonObjectRequest);

    }
    public void generatePlaylist(VolleyCallBack<Playlist> callback, String name, String description, boolean isPublic, List<Song> playlistSongs) {
        createPlaylist(playlist -> {
            accumulateTracks(aVoid -> {
                callback.onSuccess(playlist);
            }, playlist, 0, playlistSongs);
        }, name, description, isPublic);

    }
    @Override
    public void get(VolleyCallBack<Playlist> callback) {
//        createPlaylist(playlist -> {
//            accumulateTracks((VolleyCallBack<Void>) aVoid -> {
//                callback.onSuccess(playlist);
//            }, playlist, 0, );
//        }, );
    }

    @Override
    String getTag() {
        return null;
    }
}
