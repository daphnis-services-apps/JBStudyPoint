package com.jb.study.point.youtube.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jb.study.point.R;
import com.jb.study.point.youtube.model.playlist.PlaylistYTVideo;
import com.jb.study.point.youtube.model.search.SearchYTVideo;
import com.squareup.picasso.Picasso;

public abstract class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private RecyclerViewClickListener itemListener;
    private Context context;

    public RecyclerViewAdapter(Context context, RecyclerViewClickListener itemListener) {
        this.context = context;
        this.itemListener = itemListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_playlist_video,parent,false);
        return new YouTubeHolder(view);
    }

    public abstract void setOnLoadMoreListener(PlaylistAdapter.OnLoadMoreListener onLoadMoreListener);

    public abstract void setLoaded();

    class YouTubeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView thumbnail;
        TextView title,desc;

        public YouTubeHolder(@NonNull View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.video_thumbnail);
            title = itemView.findViewById(R.id.video_title);
            desc = itemView.findViewById(R.id.video_desc);

            itemView.setOnClickListener(this);

        }

        public void setData(SearchYTVideo searchYtVideo) {
            String title = searchYtVideo.getSnippet().getTitle();
            String desc = searchYtVideo.getSnippet().getPublishedAt();
            String thumbnail = searchYtVideo.getSnippet().getThumbnails().getHigh().getUrl();

            this.title.setText(title);
            this.desc.setText(desc);
            Picasso.with(context)
                    .load(thumbnail)
                    .placeholder(R.drawable.loading)
                    .fit()
                    .centerCrop()
                    .into(this.thumbnail);

        }

        @Override
        public void onClick(View v) {
            itemListener.recyclerViewListClicked(v, this.getLayoutPosition());
        }

        public void setData(PlaylistYTVideo playlistYtVideo) {
            String title = playlistYtVideo.getSnippet().getTitle();
            String desc = playlistYtVideo.getSnippet().getPublishedAt();
            String thumbnail = playlistYtVideo.getSnippet().getThumbnails().getHigh().getUrl();

            this.title.setText(title);
            this.desc.setText(desc);
            Picasso.with(context)
                    .load(thumbnail)
                    .placeholder(R.drawable.loading)
                    .fit()
                    .centerCrop()
                    .into(this.thumbnail);
        }
    }

    public interface RecyclerViewClickListener {
        public void recyclerViewListClicked(View v, int position);
    }
    

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
