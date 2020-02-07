package com.example.countify;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.stream.Collectors;

public class Song {
    private String id;
    private String name;
    private int duration_ms;
    private List<Artist> artists;
    private Album album;
    private String uri;

    //Instantiate early for performance
    private String concatenatedArtists;
    private String imageUrl;

    public Song(String id, String name, int duration_ms, List<Artist> artists) {
        this.name = name;
        this.id = id;
        this.duration_ms = duration_ms;
        this.artists = artists;
        parseSong();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration_ms() {
        return duration_ms;
    }

    public String getConcatenatedArtists() {
        return concatenatedArtists;
    }

    private String concatenateArtists(List<Artist> artists) {
        return artists
                .stream()
                .map(Artist::getName)
                .collect(Collectors.joining(", "));
    }

    public void parseSong() {
        concatenatedArtists = concatenateArtists(artists);
        imageUrl = album.getIconSizeImage();

    }
    public List<Artist> getArtists() {
        return artists;
    }

    public String getUri() {
        return uri;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
