package com.jb.study.point.youtube.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jb.study.point.youtube.model.search.SearchYTVideo;

import java.util.List;

public class SearchAdapter extends RecyclerViewAdapter{
    private Context context;
    private List<SearchYTVideo> videoList;

    public SearchAdapter(Context context, List<SearchYTVideo> videoList , RecyclerViewClickListener itemListener) {
        super(context, itemListener);
        this.context = context;
        this.videoList = videoList;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SearchYTVideo searchYtVideo = videoList.get(position);
        YouTubeHolder youTubeHolder = (YouTubeHolder) holder;
        youTubeHolder.setData(searchYtVideo);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    @Override
    public void setOnLoadMoreListener(PlaylistAdapter.OnLoadMoreListener onLoadMoreListener) {

    }

    @Override
    public void setLoaded() {

    }
}
