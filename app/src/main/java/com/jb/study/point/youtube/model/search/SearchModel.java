package com.jb.study.point.youtube.model.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchModel {
    @SerializedName("nextPageToken")
    @Expose
    private String nextPageToken;

    @SerializedName("items")
    @Expose
    private List<SearchYTVideo> items;

    public SearchModel() {
    }

    public SearchModel(String nextPageToken, List<SearchYTVideo> items) {
        this.nextPageToken = nextPageToken;
        this.items = items;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public List<SearchYTVideo> getItems() {
        return items;
    }

    public void setItems(List<SearchYTVideo> items) {
        this.items = items;
    }
}
