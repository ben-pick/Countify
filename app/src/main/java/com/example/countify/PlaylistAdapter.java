package com.example.countify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private List<Song> playlist;
    private LayoutInflater mInflater;

    PlaylistAdapter(Context context, List<Song> data) {
        this.mInflater = LayoutInflater.from(context);
        this.playlist = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.container_song, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = playlist.get(position);
        holder.songName.setText(song.getName());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return playlist.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView songName;

        ViewHolder(View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.songname_textview);
        }

        @Override
        public void onClick(View v) {

        }
    }
    public void updateData(List<Song> data) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(data, playlist));
        playlist.clear();
        playlist.addAll(data);
        diffResult.dispatchUpdatesTo(this);
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
