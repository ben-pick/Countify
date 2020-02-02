package com.example.countify;

public class Song {
    private String id;
    private String name;
    private int duration_ms;

    public Song(String id, String name) {
        this.name = name;
        this.id = id;
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

}
