package com.example.countify;

import android.content.Context;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = Shared.TAG + "PlaylistAdapter";
    private LayoutInflater mInflater;
    private Context context;
    private SnackbarCallback snackbarCallback;
    private List<Song> allSongs;
    private List<Song> playlist;
    private FragmentTransactionCallback fragmentTransactionCallback;
    private Editable timeBefore;
    private SongViewModel model;
    PlaylistAdapter(Context context, List<Song> allSongs, List<Song> playlist, SnackbarCallback snackbarCallback,FragmentTransactionCallback fragmentTransactionCallback) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.snackbarCallback = snackbarCallback;
        this.fragmentTransactionCallback = fragmentTransactionCallback;
        model = new ViewModelProvider((PlaylistActivity) context,new SongViewModelFactory(context)).get(SongViewModel.class);
        this.allSongs = allSongs;
        this.playlist = playlist;

    }

    private final int GENERATE = 0;
    private final int SONG = 1;
    private final int CONFIRM = 2;
    // inflates the row layout from xml when needed
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case GENERATE :
                return new GenerateViewHolder(mInflater.inflate(R.layout.generate_actions, parent, false));
            case CONFIRM :
                return new ConfirmViewHolder(mInflater.inflate(R.layout.confirm_actions, parent, false));
            case SONG :
                return new SongViewHolder(mInflater.inflate(R.layout.container_song, parent, false));
            default: return new SongViewHolder(mInflater.inflate(R.layout.container_song, parent, false));
        }
    }

    //Only first position needs to be the generate actions, otherwise, display the songs
    @Override
    public int getItemViewType(int position) {
        if (playlist.size() > 0) {
            if (position == 0) {
                return GENERATE;
            } else if (position == playlist.size() + 1) {
                return CONFIRM;
            } else {
                return SONG;
            }
        } else {
            if (position == 0) {
                return GENERATE;
            } else {
                return SONG;
            }
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
                    String timeString = generateViewHolder.time.getText().toString();
                    if (timeString.matches("-?\\d+")) {
                        try {
                            int timeInMs = Math.multiplyExact(Integer.parseInt(timeString), 60 * 1000);
                            model.findClosestSongs(timeInMs);
                        } catch (NumberFormatException | ArithmeticException e) {
                            snackbarCallback.showSnackbar("Please enter a valid time in minutes.");
                        }
                    } else {
                        snackbarCallback.showSnackbar("Please enter a valid time in minutes.");
                    }
                });

        break;
            case CONFIRM:
                ConfirmViewHolder confirmViewHolder = (ConfirmViewHolder) holder;
                confirmViewHolder.confirmBtn.setOnClickListener(v -> {
                    fragmentTransactionCallback.onSwitch();
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
//        //Dont display confirm button
        if (playlist.size() == 0) {
            return playlist.size() + 1;
        }
        //Display the confirm button
        else {
            return playlist.size() + 2;
        }
//        return playlist.size() + 2;
    }
    public class ConfirmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Button confirmBtn;

        public ConfirmViewHolder(View itemView) {
            super(itemView);
            confirmBtn = itemView.findViewById(R.id.confirm_btn);
        }

        @Override
        public void onClick(View v) {

        }
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
        Log.d("Ben-testing", "new " + data.size() + " old " + playlist.size() );
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(data, playlist));
        playlist.clear();
        playlist.addAll(data);
        ListUpdateCallback listUpdateCallback = new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.d("Ben-testing", "inserting" + playlist.get(position).getName()+ " at " + position + " for count " + count);
                notifyItemRangeInserted(position + 1, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.d("Ben-testing", "removing" + playlist.get(position).getName()+ " at " + position + " for count " + count);
                notifyItemRangeRemoved(position + 1, count);

            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.d("Ben-testing", "removing at " + fromPosition + " for count " + toPosition);

                notifyItemMoved(fromPosition + 1, toPosition + 1);

            }

            @Override
            public void onChanged(int position, int count, @Nullable Object payload) {
                Log.d("Ben-testing", "changing at " + position + " for count " + count);

                notifyItemRangeChanged(position +1, count, payload);
            }
        };
//        int size = playlist.size();
//        playlist.clear();
////        notifyDataSetChanged();
//        notifyItemRangeRemoved(1,size+1);
//        playlist.addAll(data);
////        notifyDataSetChanged();
//        notifyItemRangeInserted(1,data.size()+1);

        diffResult.dispatchUpdatesTo(listUpdateCallback);
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
        boolean same =  ((oldPlaylist.get(oldItemPosition).getId()).equals(newPlaylist.get(newItemPosition).getId()));
        Log.d("Ben-testing", "old " + oldPlaylist.get(oldItemPosition).getId() + " new " + newPlaylist.get(newItemPosition).getId() + " are " + same + " equal");
        return oldPlaylist.get(oldItemPosition).getId().equals(newPlaylist.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        boolean same = oldPlaylist.get(oldItemPosition).equals(newPlaylist.get(newItemPosition));
        Log.d("Ben-testing", "old " + oldPlaylist.get(oldItemPosition).getId() + " new " + newPlaylist.get(newItemPosition).getId() + " are " + same + " equal");

        return oldPlaylist.get(oldItemPosition).equals(newPlaylist.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
