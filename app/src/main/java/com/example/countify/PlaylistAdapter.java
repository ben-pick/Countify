package com.example.countify;

import android.content.Context;
import android.os.Parcelable;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = Shared.TAG + "PlaylistAdapter";
    private List<Song> playlist;
    private List<Song> allSongs;
    private LayoutInflater mInflater;
    private Context context;
    private RefreshCallback refreshStateCallback;
    private Editable timeBefore;

    PlaylistAdapter(Context context, List<Song> allsongs, List<Song> playlist, RefreshCallback refreshStateCallback) {
        this.mInflater = LayoutInflater.from(context);
        this.allSongs = allsongs;
        this.playlist = playlist;
        this.context = context;
        this.refreshStateCallback = refreshStateCallback;
    }

    private final int GENERATE = 0;
    private final int SONG = 1;
    // inflates the row layout from xml when needed
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case GENERATE :
                return new GenerateViewHolder(mInflater.inflate(R.layout.generate_actions, parent, false));
            case SONG :
                return new SongViewHolder(mInflater.inflate(R.layout.container_song, parent, false));
            default: return new SongViewHolder(mInflater.inflate(R.layout.container_song, parent, false));
        }
    }

    //Only first position needs to be the generate actions, otherwise, display the songs
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return GENERATE;
        }
        else {
            return SONG;
        }
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case GENERATE :
                GenerateViewHolder generateViewHolder = (GenerateViewHolder) holder;
                if (timeBefore == null) {
                    timeBefore = generateViewHolder.time.getText();
                }
                else {
                    generateViewHolder.time.setText(timeBefore);
                }
                generateViewHolder.generateBtn.setOnClickListener(v -> {
                    Log.d(TAG, "Clicked generate button");
                    timeBefore = generateViewHolder.time.getText();
                    SongService songService = new SongService(context);
                    String timeString = generateViewHolder.time.getText().toString();
                    if (timeString.matches("-?\\d+")) {
                        try {
                            songService.findClosestSongs(playlist -> {
                                updateData(new ArrayList<>(playlist));
                            }, Integer.parseInt(timeString) * 60 * 1000, allSongs);
                        } catch (NumberFormatException e) {
//                            Snackbar.make(toolbar, "Please enter a valid time in minutes", Snackbar.LENGTH_LONG).show();
                        }
                    } else {
//                        Snackbar.make(toolbar, "Please enter a valid time in minutes", Snackbar.LENGTH_LONG).show();
                    }
                });

        break;
            case SONG:
                SongViewHolder songViewHolder = (SongViewHolder) holder;
                Song song = playlist.get(position-1);
                songViewHolder.artistName.setText(song.getConcatenatedArtists());
                songViewHolder.songName.setText(song.getName());
                Picasso.get().load(song.getImageUrl()).into(songViewHolder.albumArt);
                songViewHolder.albumArt.setMaxWidth(songViewHolder.albumArt.getMaxHeight());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + holder.getItemViewType());
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return playlist.size() + 1;
    }

    public class GenerateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        EditText time;
        Button generateBtn;

        public GenerateViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time_edittext);
            generateBtn = itemView.findViewById(R.id.generate_btn);
        }

        @Override
        public void onClick(View v) {

        }
    }
    // stores and recycles views as they are scrolled off screen
    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView songName;
        TextView artistName;
        ImageView albumArt;

        SongViewHolder(View itemView) {
            super(itemView);
            artistName = itemView.findViewById(R.id.songartist_textview);
            songName = itemView.findViewById(R.id.songname_textview);
            albumArt = itemView.findViewById(R.id.albumart_imageview);
        }

        @Override
        public void onClick(View v) {

        }
    }
    public void updateData(List<Song> data) {
        refreshStateCallback.onBefore();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(data, playlist));
        playlist.clear();
        playlist.addAll(data);
        diffResult.dispatchUpdatesTo(this);
        refreshStateCallback.onAfter();

    }
}
class DiffCallback extends DiffUtil.Callback{

    private List<Song> oldPlaylist;
    private List<Song> newPlaylist;

    DiffCallback(List<Song> newPlaylist, List<Song> oldPlaylist) {
        this.newPlaylist = newPlaylist;
        this.oldPlaylist = oldPlaylist;
    }

    @Override
    public int getOldListSize() {
        return oldPlaylist.size();
    }

    @Override
    public int getNewListSize() {
        return newPlaylist.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldPlaylist.get(oldItemPosition).getId().equals(newPlaylist.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldPlaylist.get(oldItemPosition).equals(newPlaylist.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
