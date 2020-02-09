package com.example.countify;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SongViewModel extends ViewModel {
    private MutableLiveData <List<Song>> _allSongs = new MutableLiveData<>();
    private MutableLiveData <Boolean> _allSongsFlag = new MutableLiveData<>();
    private MutableLiveData <List<Song>> _songList = new MutableLiveData<>();
    private MutableLiveData <Playlist> _playlist = new MutableLiveData<>();
    private MutableLiveData <Boolean> _playlistFlag = new MutableLiveData<>();

    private SongService songService;
    private PlaylistService playlistService;

    public SongViewModel(Context context) {
        songService = new SongService(context);
        playlistService = new PlaylistService(context);
        _allSongsFlag.setValue(false);
        _playlistFlag.setValue(false);

    }
    public LiveData<List<Song>> getCurrentPlaylist() {
        return _songList;
    }

    public LiveData<List<Song>> getSavedSongs() {
        return _allSongs;
    }

    public LiveData<Boolean> hasGotSavedSongs() {
        return _allSongsFlag;
    }

    public LiveData<Boolean> hasGeneratedPlaylist() {
        return _playlistFlag;
    }

    public LiveData<Playlist> getGeneratedPlaylist() {
        return _playlist;
    }

    public void setHasGotSavedSongs(boolean hasGotSavedSongs) {
        _allSongsFlag.setValue(hasGotSavedSongs);
    }

    public void setHasGeneratedPlaylist(boolean hasGeneratedPlaylist) {
        _playlistFlag.setValue(hasGeneratedPlaylist);
    }


    public Song replaceSong(int index) {
        List<Song> list = _songList.getValue();
        Song replacement = songService.replaceSong(_songList.getValue().get(index), _songList.getValue(), _allSongs.getValue());
        if (replacement != null) {
            list.set(index, replacement);
            _songList.setValue(list);
        }
        return replacement;
    }

    public void findClosestSongs(int time) {
        songService.findClosestSongs(songs ->  {
            if (songs == null) {
                _songList.setValue(new ArrayList<>());
            }
            else {
                _songList.setValue(new ArrayList<>(songs));
            }
        }, time, _allSongs.getValue());
    }

    public void getAllSongs() {
        songService.get(songs -> {
            _allSongs.setValue(songs);
        });
    }

    public void generatePlaylist(String name, String description, boolean isPublic) {
        playlistService.generatePlaylist(playlist -> {
            _playlist.setValue(playlist);
        }, name, description, isPublic, _songList.getValue());
    }

}
