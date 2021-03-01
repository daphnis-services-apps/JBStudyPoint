package com.jb.study.point.youtube.network;

import com.jb.study.point.youtube.model.playlist.PlaylistModel;
import com.jb.study.point.youtube.model.search.SearchModel;

import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Url;

public class YoutubeAPI {
    public static final String BASE_URL = "https://www.googleapis.com/youtube/v3/";

    public static final String SEARCH = "search?";

    public static final String PART = "part=snippet";

    public static final String ORDER = "&order=date";

    public static final String API = "&key=";

    public static final String API_KEY = "AIzaSyAi_LI1VkH4NuzfBdewHumERkcavh4i_ho";

    public static final String PLAYLIST_ITEMS = "playlistItems?";

    public static final String PLAYLISTS = "playlists?";

    public static final String PLAYLIST = "&playlistId=";

    public static final String CHANNEL = "&channelId=";

    public static final String MAX_RESULTS_10 = "&maxResults=10";

    public static final String MAX_RESULTS_50 = "&maxResults=50";

    public static final String CHANNEL_ID = "UCbRC_a_H7fYIIJecxkHgXfA";

    public static final String NEXT_TOKEN = "&pageToken=";

    public interface SearchVideo{
        @GET
        Call<SearchModel> getSearchVideos(@Url String url);
    }

    public interface PlaylistVideo{
        @GET
        Call<PlaylistModel> getPlaylistVideos(@Url String url);
    }

    private static SearchVideo searchVideo = null;
    private static PlaylistVideo playlistVideo = null;

    public static SearchVideo getSearchVideo(){
        if(searchVideo == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            searchVideo = retrofit.create(SearchVideo.class);
        }
        return searchVideo;
    }

    public static PlaylistVideo getPlaylistVideo(){
        if(playlistVideo == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            playlistVideo = retrofit.create(PlaylistVideo.class);
        }
        return playlistVideo;
    }
}
