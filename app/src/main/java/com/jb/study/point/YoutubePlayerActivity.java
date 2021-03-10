package com.jb.study.point;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jb.study.point.youtube.adapter.PlaylistAdapter;
import com.jb.study.point.youtube.adapter.RecyclerViewAdapter;
import com.jb.study.point.youtube.adapter.SearchAdapter;
import com.jb.study.point.youtube.helper.FullScreenHelper;
import com.jb.study.point.youtube.model.playlist.PlaylistModel;
import com.jb.study.point.youtube.model.playlist.PlaylistYTVideo;
import com.jb.study.point.youtube.model.search.SearchModel;
import com.jb.study.point.youtube.model.search.SearchYTVideo;
import com.jb.study.point.youtube.network.YoutubeAPI;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerUtils;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YoutubePlayerActivity extends AppCompatActivity implements RecyclerViewAdapter.RecyclerViewClickListener {

    private final FullScreenHelper fullScreenHelper = new FullScreenHelper(this);
    private final List<SearchYTVideo> searchVideoList = new ArrayList<>();
    private final List<PlaylistYTVideo> playlistVideoList = new ArrayList<>();
    private RecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView recyclerView;
    private YouTubePlayer youTubePlaye;
    private YouTubePlayerView youTubePlayerView;
    private String nextToken;
    private ProgressBar progressBar;
    private float second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);
        initViews();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        if (getIntent().getStringExtra("type").equals("search"))
            if (getIntent().getStringExtra("section").equals("all"))
                getAllVideos();
            else
                getNewestVideoList();
        else if (getIntent().getStringExtra("type").equals("playlist")) {
            if (getIntent().getStringExtra("playlist") != null)
                getVideoPlayList(getIntent().getStringExtra("playlist"));
            else
                getAllPlaylists();
        }

        recyclerViewAdapter.setOnLoadMoreListener(new PlaylistAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                playlistVideoList.add(null);
                recyclerViewAdapter.notifyItemInserted(playlistVideoList.size() - 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playlistVideoList.remove(playlistVideoList.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(playlistVideoList.size());

                        //Generating more data
                        getMoreVideoPlayList(getIntent().getStringExtra("playlist"));
                    }
                }, 100);
            }
        });

        getLifecycle().addObserver(youTubePlayerView);
        //initPictureInPicture(youTubePlayerView);
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlaye = youTubePlayer;
                YouTubePlayerUtils.loadOrCueVideo(
                        youTubePlaye, getLifecycle(),
                        getIntent().getStringExtra("videoId") != null ? getIntent().getStringExtra("videoId") : "", 0f
                );
                addFullScreenListenerToPlayer();
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCurrentSecond(@NotNull YouTubePlayer youTubePlayer, float second) {
                super.onCurrentSecond(youTubePlayer, second);
                YoutubePlayerActivity.this.second = second;
            }


        });

    }

    private void getAllVideos() {
        progressBar.setIndeterminate(true);
        recyclerViewAdapter = new SearchAdapter(this, searchVideoList, this);
        String url = YoutubeAPI.BASE_URL + YoutubeAPI.SEARCH + YoutubeAPI.PART + YoutubeAPI.CHANNEL + YoutubeAPI.CHANNEL_ID + YoutubeAPI.ORDER + YoutubeAPI.MAX_RESULTS_50 + YoutubeAPI.API + YoutubeAPI.API_KEY;
        Call<SearchModel> call = YoutubeAPI.getSearchVideo().getSearchVideos(url);
        call.enqueue(new Callback<SearchModel>() {
            @Override
            public void onResponse(@NonNull Call<SearchModel> call, Response<SearchModel> response) {
                if (response.errorBody() != null) {
                    Log.e("MainActivity", "" + response.errorBody());
                } else {
                    SearchModel searchModel = response.body();
                    nextToken = searchModel.getNextPageToken();
                    searchVideoList.addAll(searchModel.getItems());
                    recyclerViewAdapter.notifyDataSetChanged();
                    recyclerView.scheduleLayoutAnimation();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SearchModel> call, Throwable t) {
                Log.e("MainActivity", "onFailure" + t);
            }
        });
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void getAllPlaylists() {
        progressBar.setIndeterminate(true);
        recyclerViewAdapter = new PlaylistAdapter(this, playlistVideoList, this, recyclerView);
        String url = YoutubeAPI.BASE_URL + YoutubeAPI.PLAYLISTS + YoutubeAPI.PART + YoutubeAPI.CHANNEL + YoutubeAPI.CHANNEL_ID + YoutubeAPI.ORDER + YoutubeAPI.MAX_RESULTS_50 + YoutubeAPI.API + YoutubeAPI.API_KEY;
        Call<PlaylistModel> call = YoutubeAPI.getPlaylistVideo().getPlaylistVideos(url);
        call.enqueue(new Callback<PlaylistModel>() {
            @Override
            public void onResponse(@NonNull Call<PlaylistModel> call, Response<PlaylistModel> response) {
                if (response.errorBody() != null) {
                    Log.e("MainActivity", "" + response.errorBody());
                } else {
                    PlaylistModel playlistModel = response.body();
                    nextToken = playlistModel.getNextPageToken();
                    playlistVideoList.addAll(playlistModel.getItems());
                    recyclerViewAdapter.notifyDataSetChanged();
                    recyclerView.scheduleLayoutAnimation();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlaylistModel> call, Throwable t) {
                Log.e("MainActivity", "onFailure" + t);
            }
        });
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void getNewestVideoList() {
        progressBar.setIndeterminate(true);
        recyclerViewAdapter = new SearchAdapter(this, searchVideoList, this);
        String url = YoutubeAPI.BASE_URL + YoutubeAPI.SEARCH + YoutubeAPI.PART + YoutubeAPI.CHANNEL + YoutubeAPI.CHANNEL_ID + YoutubeAPI.MAX_RESULTS_10 + YoutubeAPI.ORDER + YoutubeAPI.API + YoutubeAPI.API_KEY;
        Call<SearchModel> call = YoutubeAPI.getSearchVideo().getSearchVideos(url);
        call.enqueue(new Callback<SearchModel>() {
            @Override
            public void onResponse(@NonNull Call<SearchModel> call, Response<SearchModel> response) {
                if (response.errorBody() != null) {
                    Log.e("MainActivity", "" + response.errorBody());
                } else {
                    SearchModel searchModel = response.body();
                    searchVideoList.addAll(searchModel.getItems());
                    recyclerViewAdapter.notifyDataSetChanged();
                    recyclerView.scheduleLayoutAnimation();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SearchModel> call, Throwable t) {
                Log.e("MainActivity", "onFailure" + t);
            }
        });
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void getVideoPlayList(String playlist) {
        progressBar.setIndeterminate(true);
        recyclerViewAdapter = new PlaylistAdapter(this, playlistVideoList, this, recyclerView);
        String url = YoutubeAPI.BASE_URL + YoutubeAPI.PLAYLIST_ITEMS + YoutubeAPI.PART + YoutubeAPI.PLAYLIST + playlist + YoutubeAPI.API + YoutubeAPI.API_KEY;
        Call<PlaylistModel> call = YoutubeAPI.getPlaylistVideo().getPlaylistVideos(url);
        call.enqueue(new Callback<PlaylistModel>() {
            @Override
            public void onResponse(@NonNull Call<PlaylistModel> call, Response<PlaylistModel> response) {
                if (response.errorBody() != null) {
                    Log.e("MainActivity", "" + response.errorBody());
                } else {
                    PlaylistModel playlistModel = response.body();
                    nextToken = playlistModel.getNextPageToken();
                    playlistVideoList.addAll(playlistModel.getItems());
                    recyclerViewAdapter.notifyDataSetChanged();
                    recyclerView.scheduleLayoutAnimation();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlaylistModel> call, Throwable t) {
                Log.e("MainActivity", "onFailure" + t);
            }
        });
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void getMoreVideoPlayList(String playlist) {
        String url = YoutubeAPI.BASE_URL + YoutubeAPI.PLAYLIST_ITEMS + YoutubeAPI.PART + YoutubeAPI.NEXT_TOKEN + nextToken + YoutubeAPI.PLAYLIST + playlist + YoutubeAPI.MAX_RESULTS_10 + YoutubeAPI.API + YoutubeAPI.API_KEY;
        Call<PlaylistModel> call = YoutubeAPI.getPlaylistVideo().getPlaylistVideos(url);
        call.enqueue(new Callback<PlaylistModel>() {
            @Override
            public void onResponse(@NonNull Call<PlaylistModel> call, @NotNull Response<PlaylistModel> response) {
                if (response.errorBody() != null) {
                    Log.e("MainActivity", "" + response.errorBody());
                } else {
                    PlaylistModel playlistModel = response.body();
                    nextToken = playlistModel.getNextPageToken();
                    playlistVideoList.addAll(playlistModel.getItems());
                    recyclerViewAdapter.notifyDataSetChanged();
                    recyclerView.scheduleLayoutAnimation();
                    recyclerViewAdapter.setLoaded();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlaylistModel> call, Throwable t) {
                Log.e("MainActivity", "onFailure" + t);
            }
        });
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.playlist_progressBar);
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void initPictureInPicture(YouTubePlayerView youTubePlayerView) {
        ImageView pictureInPictureIcon = new ImageView(this);
        pictureInPictureIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_picture_in_picture_24dp));

        pictureInPictureIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPIP();
            }
        });

        youTubePlayerView.getPlayerUiController().addView(pictureInPictureIcon);
    }

    private void getPIP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            boolean supportsPIP = YoutubePlayerActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
            if (supportsPIP)
                YoutubePlayerActivity.this.enterPictureInPictureMode();
        } else {
            new AlertDialog.Builder(YoutubePlayerActivity.this)
                    .setTitle("Can't enter picture in picture mode")
                    .setMessage("In order to enter picture in picture mode you need a SDK version >= N.")
                    .show();
        }
    }


    private void addFullScreenListenerToPlayer() {
        addCustomActionsToPlayer();
        youTubePlayerView.addFullScreenListener(new YouTubePlayerFullScreenListener() {
            @Override
            public void onYouTubePlayerEnterFullScreen() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                fullScreenHelper.enterFullScreen();
                hideUi();
            }

            @Override
            public void onYouTubePlayerExitFullScreen() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                fullScreenHelper.exitFullScreen();
                hideUi();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (youTubePlayerView.isFullScreen()) {
            youTubePlayerView.exitFullScreen();
        } else {
            //getPIP();
            finish();
        }
    }

    private void addCustomActionsToPlayer() {
        Drawable customAction1Icon = ContextCompat.getDrawable(this, R.drawable.ic_fast_rewind_white_24dp);
        Drawable customAction2Icon = ContextCompat.getDrawable(this, R.drawable.ic_fast_forward_white_24dp);
        assert customAction1Icon != null;
        assert customAction2Icon != null;

        youTubePlayerView.getPlayerUiController().setCustomAction1(customAction1Icon, view -> youTubePlaye.seekTo(second-5));

        youTubePlayerView.getPlayerUiController().setCustomAction2(customAction2Icon, view -> youTubePlaye.seekTo(second + 5));
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);

        if (isInPictureInPictureMode) {
            youTubePlayerView.enterFullScreen();
        } else {
            youTubePlayerView.exitFullScreen();
        }
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        if (recyclerViewAdapter instanceof PlaylistAdapter) {
            PlaylistYTVideo playlistYTVideo = playlistVideoList.get(position);
            YouTubePlayerUtils.loadOrCueVideo(
                    youTubePlaye, getLifecycle(),
                    playlistYTVideo.getSnippet().getResourceID().getVideoId(), 0f
            );
        } else {
            SearchYTVideo searchYtVideo = searchVideoList.get(position);
            YouTubePlayerUtils.loadOrCueVideo(
                    youTubePlaye, getLifecycle(),
                    searchYtVideo.getId().getVideoId(), 0f
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideUi();
    }


    private void hideUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            view.setSystemUiVisibility(uiOptions);
            /*int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            this.getWindow().setStatusBarColor(Color.WHITE);*/
        }
    }
}