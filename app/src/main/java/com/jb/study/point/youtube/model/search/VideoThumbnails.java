package com.jb.study.point.youtube.model.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VideoThumbnails {
    @SerializedName("high")
    @Expose
    private HighThumb high;

    public VideoThumbnails() {
    }

    public VideoThumbnails(HighThumb high) {
        this.high = high;
    }

    public HighThumb getHigh() {
        return high;
    }

    public void setHigh(HighThumb high) {
        this.high = high;
    }

    public static class HighThumb {
        @SerializedName("url")
        @Expose
        private String url;

        public HighThumb() {
        }

        public HighThumb(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
