package com.jb.study.point.youtube.model.playlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlaylistYTVideo {

    @SerializedName("snippet")
    @Expose
    private SnippetYT snippet;

    @SerializedName("id")
    @Expose
    private String id;

    public PlaylistYTVideo() {
    }

    public PlaylistYTVideo(SnippetYT snippet, String id) {
        this.snippet = snippet;
        this.id = id;
    }

    public SnippetYT getSnippet() {
        return snippet;
    }

    public void setSnippet(SnippetYT snippet) {
        this.snippet = snippet;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
