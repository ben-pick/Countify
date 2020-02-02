package com.example.countify;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Song> songList = new ArrayList<>();
    private List<Song> playlistRef = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        Snackbar.make(toolbar, "Successfully logged in!", Snackbar.LENGTH_LONG).show();

        SongService songService = new SongService(this);

        songService.get(songs -> {
            ProgressBar progressBar = findViewById(R.id.getallsongs_progressbar);
            progressBar.setVisibility(View.GONE);
            Snackbar.make(toolbar, "Successfully grabbed all saved songs!", Snackbar.LENGTH_LONG).show();
            songList = songs;

            EditText timeEntry = findViewById(R.id.time_edittext);
            timeEntry.setVisibility(View.VISIBLE);
            setSupportActionBar(toolbar);

            RecyclerView recyclerView = findViewById(R.id.playlist_recyclerview);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            PlaylistAdapter adapter = new PlaylistAdapter(this, playlistRef);
            recyclerView.setAdapter(adapter);

            Button generateBtn = findViewById(R.id.generate_btn);
            generateBtn.setVisibility(View.VISIBLE);

            generateBtn.setOnClickListener(v -> {
                String timeString = timeEntry.getText().toString();
                if (timeString.matches("-?\\d+")) {
                    try {
                        songService.findClosestSongs(playlist -> {
                            adapter.updateData(new ArrayList<>(playlist));
                        }, Integer.parseInt(timeEntry.getText().toString()) * 60 * 1000, songList);
                    } catch (NumberFormatException e) {
                        Snackbar.make(toolbar, "Please enter a valid time in minutes", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(toolbar, "Please enter a valid time in minutes", Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }
}
