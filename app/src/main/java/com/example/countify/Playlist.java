package com.example.countify;

import com.google.gson.annotations.SerializedName;

public class Playlist {
    private String href;
    private String uri;
    private String id;
    private String external_url;
    public String getHref() {
        return href;
    }
    public String getExternal_url(){
        return external_url;
    }

    public void setExternal_url(String external_url) {
        this.external_url = external_url;
    }

    public String getUri() {
        return uri;
    }

    public String getId() {
        return id;
    }
}

class ExternalUrl {
    @SerializedName("spotify")
    private String url;
    public String getUrl() {
        return url;
    }
}
