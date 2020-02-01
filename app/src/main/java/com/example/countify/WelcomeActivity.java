package com.example.countify;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG = Shared.TAG + "WelcomeActivity";
    private static final String CLIENT_ID = "ca0af4041b944e459c991c15b9a93d9c";
    private static final String REDIRECT_URI = "com.example.countify://callback";
    private static final int REQUEST_CODE = 1337;
    private static final String SCOPES = "user-read-recently-played,user-library-modify,user-read-email,user-read-private";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Button loginButton = findViewById(R.id.spotify_login_btn);
        loginButton.setOnClickListener(v -> authenticateSpotify());
    }
    private void encryptToken(String token) {
        try {
            final KeyGenerator keyGenerator = KeyGenerator
                    .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            final KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(Shared.TOKEN,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build();
            keyGenerator.init(keyGenParameterSpec);
            final SecretKey secretKey = keyGenerator.generateKey();

            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] iv = cipher.getIV();
            Shared.saveInPersistence(this, Shared.TOKENIV, Base64.encodeToString(iv, Base64.DEFAULT));

            byte[] encryption = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
            Shared.saveInPersistence(this, Shared.TOKEN, Base64.encodeToString(encryption, Base64.DEFAULT));
            Log.d(TAG, "Finished encrypting");

        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }

    }
    private void authenticateSpotify() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{SCOPES});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        Log.d(TAG, "Opening login activity");

    }
    private void saveUserInfo() {
        UserService userService = new UserService(this);
        userService.get(user -> {
            Shared.saveInPersistenceNow(this, "userId", user.id);
            startActivity(new Intent(this, MainActivity.class));
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(TAG, "Request code" + requestCode);

        // Check if result comes from the spotify activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            Log.d(TAG, "Response type :" + response.getType());

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    //Save in persistent storage
                    Log.d(TAG, "token : " + response.getAccessToken());
                    encryptToken(response.getAccessToken());
                    saveUserInfo();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

}
