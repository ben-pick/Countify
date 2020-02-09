package com.example.countify;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

public class ConfirmationFragment extends Fragment {
    private SongViewModel model;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirmation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Confirm");
        EditText description = getActivity().findViewById(R.id.edittext_description);
        EditText name = getActivity().findViewById(R.id.edittext_name);
        Switch isPublic = getActivity().findViewById(R.id.switch_scope);
        model = new ViewModelProvider(requireActivity(), new SongViewModelFactory(getContext())).get(SongViewModel.class);
        model.getGeneratedPlaylist().observe(getViewLifecycleOwner(), playlist -> {
            if (getViewLifecycleOwner().getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
                Log.d("Ben-testing", playlist.toString());
                Snackbar.make(toolbar, "Playlist generated!", Snackbar.LENGTH_LONG).show();
                PackageManager pm = getActivity().getPackageManager();
                try {
                    pm.getPackageInfo("com.spotify.music", 0);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(playlist.getUri()));
                    intent.putExtra(Intent.EXTRA_REFERRER,
                            Uri.parse("android-app://" + getContext().getPackageName()));
                    startActivity(intent);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.d("Ben-testing", "Spotify not installed");
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(playlist.getExternal_url()));
                    startActivity(browserIntent);
                }

            }
        });

        getActivity().findViewById(R.id.button_confirmation).setOnClickListener(v -> {
            Log.d("Ben-testing", "Clicking");
            model.generatePlaylist(name.getText().toString(), description.getText().toString(), isPublic.isChecked());
        });


    }

}