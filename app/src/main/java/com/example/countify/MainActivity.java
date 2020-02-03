package com.example.countify;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = Shared.COUNTIFY + "MainActivity";
    private Parcelable recyclerViewState;
    private RecyclerView recyclerView;
    private List<Song> allSongs = new ArrayList<>();
    private List<Song> playlist = new ArrayList<>();
    private Context context;
    private SongService songService;
    private Toolbar toolbar;
    private void initRecyclerView() {
        recyclerView = findViewById(R.id.playlist_recyclerview);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SnackbarCallback snackbarCallback = message -> Snackbar.make(toolbar, message, Snackbar.LENGTH_LONG).show();
        PlaylistAdapter adapter = new PlaylistAdapter(this, allSongs, playlist, snackbarCallback);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback swipeRemoveCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Song toRemove = playlist.get(viewHolder.getAdapterPosition()-1);
                Song replacement = songService.replaceSong(toRemove, playlist, allSongs);
                if (replacement != null) {
                Log.d(TAG, "Attempting to replace song " + toRemove.getName() + " " +
                        toRemove.getDuration_ms() + " with " + replacement.getName() + " " + replacement.getDuration_ms() );
                    playlist.set(viewHolder.getAdapterPosition() - 1, replacement);
                    adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                }
                else {
                    adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    Snackbar.make(toolbar, "Failed to find replacement song", Snackbar.LENGTH_SHORT).show();
                    Log.d(TAG, "Failed to find replacement song");
                }
            }
        };
        new ItemTouchHelper(swipeRemoveCallback).attachToRecyclerView(recyclerView);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        Snackbar.make(toolbar, "Successfully logged in!", Snackbar.LENGTH_LONG).show();
        context = this;
        songService = new SongService(this);

        songService.get(songs -> {
            allSongs.addAll(songs);
            ProgressBar progressBar = findViewById(R.id.getallsongs_progressbar);
            progressBar.setVisibility(View.GONE);
            Snackbar.make(toolbar, "Successfully grabbed all saved songs!", Snackbar.LENGTH_LONG).show();

            setSupportActionBar(toolbar);
            initRecyclerView();
        });

    }
}
