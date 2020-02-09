package com.example.countify;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment {

    private static final String TAG = Shared.COUNTIFY + "PlaylistFragment";
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private SongViewModel model;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_main, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Playlist");
        model = new ViewModelProvider(requireActivity(), new SongViewModelFactory(getContext())).get(SongViewModel.class);
        model.getSavedSongs().observe(getViewLifecycleOwner(), songs -> {
            if (getViewLifecycleOwner().getLifecycle().getCurrentState() == Lifecycle.State.STARTED) {
                ProgressBar progressBar = view.findViewById(R.id.getallsongs_progressbar);
                progressBar.setVisibility(View.GONE);
                initRecyclerView(view);
            }
            else if (getViewLifecycleOwner().getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
                Snackbar.make(toolbar, "Successfully grabbed all saved songs!", Snackbar.LENGTH_LONG).show();
                ProgressBar progressBar = view.findViewById(R.id.getallsongs_progressbar);
                progressBar.setVisibility(View.GONE);
                initRecyclerView(view);
            }
        });

        if (model.hasGotSavedSongs().getValue() != null && !model.hasGotSavedSongs().getValue()) {
            Snackbar.make(toolbar, "Successfully logged in!", Snackbar.LENGTH_LONG).show();
            model.setHasGotSavedSongs(true);
            model.getAllSongs();
        }
    }

    private void initRecyclerView(View view) {

        recyclerView = view.findViewById(R.id.playlist_recyclerview);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SnackbarCallback snackbarCallback = message -> Snackbar.make(toolbar, message, Snackbar.LENGTH_LONG).show();
        FragmentTransactionCallback fragmentTransactionCallback = () -> {
            ConfirmationFragment confirmationFragment =new ConfirmationFragment();
            getParentFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.flContainer, confirmationFragment)
                    .addToBackStack(null)
                    .commit();
        };
        PlaylistAdapter adapter = new PlaylistAdapter(getContext(), new ArrayList<>(), new ArrayList<>(), snackbarCallback, fragmentTransactionCallback);
        model.getCurrentPlaylist().observe(getViewLifecycleOwner(), playlist -> {
            if (playlist.size() == 0) {
                Log.d(TAG, "playlist null");
                snackbarCallback.showSnackbar("You don't have enough songs to fit that time.");
            }
            else {
                Log.d(TAG, "playlist not null");
                adapter.updateData(new ArrayList<>(playlist));
            }

        });

        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback swipeRemoveCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof PlaylistAdapter.SongViewHolder) {
                    return makeMovementFlags(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
                }
                else {
                    return ItemTouchHelper.ACTION_STATE_IDLE;
                }
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Song toRemove = model.getCurrentPlaylist().getValue().get(viewHolder.getAdapterPosition()-1);
                Song replacement = model.replaceSong(viewHolder.getAdapterPosition()-1);
                if (replacement != null) {
                    Log.d(TAG, "Attempting to replace song " + toRemove.getName() + " " +
                            toRemove.getDuration_ms() + " with " + replacement.getName() + " " + replacement.getDuration_ms() );
//                    adapter.notifyItemChanged(viewHolder.getAdapterPosition());
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

}
