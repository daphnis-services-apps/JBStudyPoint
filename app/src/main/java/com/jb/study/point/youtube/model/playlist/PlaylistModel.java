package com.jb.study.point.youtube.model.playlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaylistModel {
    @SerializedName("nextPageToken")
    @Expose
    private String nextPageToken;

    @SerializedName("items")
    @Expose
    private List<PlaylistYTVideo> items;

    public PlaylistModel() {
    }

    public PlaylistModel(String nextPageToken, List<PlaylistYTVideo> items) {
        this.nextPageToken = nextPageToken;
        this.items = items;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public List<PlaylistYTVideo> getItems() {
        return items;
    }

    public void setItems(List<PlaylistYTVideo> items) {
        this.items = items;
    }
}
