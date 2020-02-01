package com.example.countify;

import android.content.Context;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class UserService extends Service<User> {
    private final String TAG = Shared.TAG + "UserService";
    private static final String ENDPOINT = "https://api.spotify.com/v1/me";

    UserService(Context context) {
        super(context);
    }

    @Override
    public void get(VolleyCallBack<User> callback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(ENDPOINT, null, response -> {
            Gson gson = new Gson();
            User user = gson.fromJson(response.toString(), User.class);
            callback.onSuccess(user);
        }, error -> get((user) -> {

        })) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = decryptToken();
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        VolleySingleton.sharedInstance(context).getRequestQueue().add(jsonObjectRequest);

    }

    @Override
    String getTag() {
        return TAG;
    }


}
