package com.bss.arrahmanlyrics.adapter;

import android.content.Context;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bss.arrahmanlyrics.R;

import com.bss.arrahmanlyrics.models.albumModel;
import com.bss.arrahmanlyrics.utils.FirstLetterUpperCase;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohan on 6/15/17.
 */

public class albumAdapter2 extends RecyclerView.Adapter<albumAdapter2.MyViewHolder> {
    private List<albumModel> albumList;
    private Context Context;

    public albumAdapter2(Context Context, List<albumModel> albumList) {
        this.Context = Context;
        this.albumList = albumList;
    }

    @Override
    public albumAdapter2.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_view, parent, false);
        RecyclerView.ViewHolder holder = new MyViewHolder(itemView);

        return (MyViewHolder) holder;
    }

    @Override
    public void onBindViewHolder(final albumAdapter2.MyViewHolder holder, int position) {
        albumModel album = albumList.get(position);

//        final Typeface font = Typeface.createFromAsset(Context.getAssets(), "Timber.ttf");
      //  holder.title.setTypeface(font);
        holder.title.setText(FirstLetterUpperCase.convert(album.getMovietitle()));
        holder.count.setText(album.getNumOfSongs() + " songs");
        Glide.with(Context).load(album.getImageString()).into(holder.thumbnail);

    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, count;
        public ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.Title);
            count = (TextView) view.findViewById(R.id.TotalSongs);
            thumbnail = (ImageView) view.findViewById(R.id.albumimg);


        }
    }

    public void setFilter(List<albumModel> albumLists){
	   albumList = new ArrayList<>();
	    albumList.addAll(albumLists);
	    notifyDataSetChanged();
    }
}
