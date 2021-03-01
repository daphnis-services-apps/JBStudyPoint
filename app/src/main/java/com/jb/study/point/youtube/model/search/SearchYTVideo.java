package com.jb.study.point.youtube.model.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SearchYTVideo {

    @SerializedName("id")
    @Expose
    private VideoID id;

    @SerializedName("snippet")
    @Expose
    private SnippetYT snippet;

    public SearchYTVideo() {
    }

    public SearchYTVideo(VideoID id, SnippetYT snippet) {
        this.id = id;
        this.snippet = snippet;
    }

    public VideoID getId() {
        return id;
    }

    public void setId(VideoID id) {
        this.id = id;
    }

    public SnippetYT getSnippet() {
        return snippet;
    }

    public void setSnippet(SnippetYT snippet) {
        this.snippet = snippet;
    }
}
