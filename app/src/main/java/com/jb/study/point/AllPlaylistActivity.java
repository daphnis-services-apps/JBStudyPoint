package com.jb.study.point;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jb.study.point.youtube.adapter.PlaylistAdapter;
import com.jb.study.point.youtube.adapter.RecyclerViewAdapter;
import com.jb.study.point.youtube.model.playlist.PlaylistModel;
import com.jb.study.point.youtube.model.playlist.PlaylistYTVideo;
import com.jb.study.point.youtube.network.YoutubeAPI;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllPlaylistActivity extends AppCompatActivity implements RecyclerViewAdapter.RecyclerViewClickListener {
    private final List<PlaylistYTVideo> playlistVideoList = new ArrayList<>();
    private ProgressBar progressBar;
    private RecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_playlist);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = getWindow().getDecorView();
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            this.getWindow().setStatusBarColor(Color.WHITE);
        }
        progressBar = findViewById(R.id.playlist_progressBar);
        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        if (getIntent().getStringExtra("type").equals("demo"))
            getDemoPlaylists();
        else
            getPremiumPlaylists();
    }

    private void getPremiumPlaylists() {
        progressBar.setIndeterminate(true);
        recyclerViewAdapter = new PlaylistAdapter(this, playlistVideoList, this, recyclerView);
        String playlist =
                "&id=PLNosGDY8Dh1RD58QcX-9Us1f_2ipMViKL,PLNosGDY8Dh1R9H3459tHddeQOrMgH3cKe,PLNosGDY8Dh1SpHIc_yLTT4HlaU2xRYxIz,PLNosGDY8Dh1TIPAv1NvTkOp-Ae-Nxqx50, PLNosGDY8Dh1TqPZwqQjWrGDJ-UYPIHznv, PLNosGDY8Dh1Qx6E_7hAMdorOq6ZZyAbPp, PLNosGDY8Dh1T-OqShYYWwD7wTZHc4ae23, PLNosGDY8Dh1RXNkbcaSmEbYBRiNbT3l54, PLNosGDY8Dh1RhUs1WMu3dHF2V0IicpihY";

            String url = YoutubeAPI.BASE_URL + YoutubeAPI.PLAYLISTS + YoutubeAPI.PART + playlist + YoutubeAPI.API + YoutubeAPI.API_KEY;
            Call<PlaylistModel> call = YoutubeAPI.getPlaylistVideo().getPlaylistVideos(url);
            call.enqueue(new Callback<PlaylistModel>() {
                @Override
                public void onResponse(@NonNull Call<PlaylistModel> call, Response<PlaylistModel> response) {
                    if (response.errorBody() != null) {
                        Log.e("MainActivity", "" + response.errorBody());
                    } else {
                        PlaylistModel playlistModel = response.body();
                        assert playlistModel != null;
                        playlistVideoList.addAll(playlistModel.getItems());
                        recyclerViewAdapter.notifyDataSetChanged();
                        recyclerView.scheduleLayoutAnimation();
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PlaylistModel> call, Throwable t) {
                    Log.e("MainActivity", "onFailure" + t);
                }
            });
            recyclerView.setAdapter(recyclerViewAdapter);

    }

    private void getDemoPlaylists() {
        progressBar.setIndeterminate(true);
        recyclerViewAdapter = new PlaylistAdapter(this, playlistVideoList, this, recyclerView);
        String url = YoutubeAPI.BASE_URL + YoutubeAPI.PLAYLISTS + YoutubeAPI.PART + YoutubeAPI.CHANNEL + YoutubeAPI.CHANNEL_ID + YoutubeAPI.ORDER + YoutubeAPI.MAX_RESULTS_50 + YoutubeAPI.API + YoutubeAPI.API_KEY;
        Call<PlaylistModel> call = YoutubeAPI.getPlaylistVideo().getPlaylistVideos(url);
        call.enqueue(new Callback<PlaylistModel>() {
            @Override
            public void onResponse(@NonNull Call<PlaylistModel> call, @NotNull Response<PlaylistModel> response) {
                if (response.errorBody() != null) {
                    Log.e("MainActivity", "" + response.errorBody());
                } else {
                    PlaylistModel playlistModel = response.body();
                    assert playlistModel != null;
                    playlistVideoList.addAll(playlistModel.getItems());
                    recyclerViewAdapter.notifyDataSetChanged();
                    recyclerView.scheduleLayoutAnimation();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlaylistModel> call, @NotNull Throwable t) {
                Log.e("MainActivity", "onFailure" + t);
            }
        });
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        PlaylistYTVideo playlistYTVideo = playlistVideoList.get(position);
        startActivity(new Intent(AllPlaylistActivity.this, YoutubePlayerActivity.class).putExtra("type", "playlist").putExtra("playlist", playlistYTVideo.getId()));
    }
}