package com.jb.study.point.youtube.model.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SnippetYT {
    @SerializedName("publishedAt")
    @Expose
    private String publishedAt;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("description")
    @Expose
    private String description;

    @SerializedName("thumbnails")
    @Expose
    private VideoThumbnails thumbnails;

    public SnippetYT() {
    }

    public SnippetYT(String publishedAt, String title, String description, VideoThumbnails thumbnails) {
        this.publishedAt = publishedAt;
        this.title = title;
        this.description = description;
        this.thumbnails = thumbnails;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public VideoThumbnails getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(VideoThumbnails thumbnails) {
        this.thumbnails = thumbnails;
    }
}
