package com.example.countify;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public abstract class Service<T> {
    Context context;
    public Service(Context context) {
        this.context = context;
    }
    public abstract void get(VolleyCallBack<T> volleyCallBack);
    abstract String getTag();
    String decryptToken() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(Shared.TOKEN, null);
            final SecretKey secretKey = secretKeyEntry.getSecretKey();
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            final String ivString = Shared.getFromPersistence(context, Shared.TOKENIV);

            final GCMParameterSpec spec = new GCMParameterSpec(128, Base64.decode(ivString, Base64.DEFAULT));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            final String tokenString = Shared.getFromPersistence(context, Shared.TOKEN);

            final byte[] decodedData = cipher.doFinal(Base64.decode(tokenString, Base64.DEFAULT));
            return new String(decodedData, StandardCharsets.UTF_8);

        } catch (Exception e) {
            Log.e(getTag(), Objects.requireNonNull(e.getMessage()));
            return null;
        }
    }

}
