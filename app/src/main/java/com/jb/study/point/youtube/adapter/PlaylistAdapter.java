package com.jb.study.point.youtube.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jb.study.point.R;
import com.jb.study.point.youtube.model.playlist.PlaylistYTVideo;

import java.util.List;

public class PlaylistAdapter extends RecyclerViewAdapter {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private final Context context;
    private final List<PlaylistYTVideo> videoList;
    private final RecyclerViewClickListener itemListener;
    private final int visibleThreshold = 5;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean isLoading;
    private int lastVisibleItem, totalItemCount;

    public PlaylistAdapter(Context context, List<PlaylistYTVideo> videoList, RecyclerViewClickListener itemListener, RecyclerView recyclerView) {
        super(context, itemListener);
        this.context = context;
        this.videoList = videoList;
        this.itemListener = itemListener;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }


    /*public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener;
    }*/

    @Override
    public int getItemViewType(int position) {
        return videoList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public void setLoaded() {
        isLoading = false;
    }

    public void setInterface(OnLoadMoreListener mOnLoadMoreListener){
        this.onLoadMoreListener = mOnLoadMoreListener;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof YouTubeHolder) {
            PlaylistYTVideo playlistYtVideo = videoList.get(position);
            YouTubeHolder youTubeHolder = (YouTubeHolder) holder;
            youTubeHolder.setData(playlistYtVideo);
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

   @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            //super.onCreateViewHolder(parent,viewType);
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.item_playlist_video,parent,false);
            return new YouTubeHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    @Override
    public int getItemCount() {
        return videoList == null ? 0 : videoList.size();
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progressBar1);
        }
    }
}
