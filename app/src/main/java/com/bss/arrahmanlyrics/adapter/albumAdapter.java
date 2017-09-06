package com.bss.arrahmanlyrics.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.models.Tracks;
import com.bss.arrahmanlyrics.models.albumModel;
import com.bss.arrahmanlyrics.models.albumsongs;
import com.bss.arrahmanlyrics.utils.FirstLetterUpperCase;
import com.bss.arrahmanlyrics.viewHolders.albumSongListViewHolder;
import com.bss.arrahmanlyrics.viewHolders.albumViewHolder;
import com.bumptech.glide.Glide;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class albumAdapter extends ExpandableRecyclerViewAdapter<albumViewHolder, albumSongListViewHolder> {
  List<albumModel> albums;
  Context context;
  public albumAdapter(List<? extends ExpandableGroup> groups,Context context,List<albumModel> albums) {
    super(groups);
    this.albums = albums;
    this.context = context;
  }

  @Override
  public albumViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_view, parent, false);
    return new albumViewHolder(view);
  }

  @Override
  public albumSongListViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_song_list_view, parent, false);
    return new albumSongListViewHolder(view);
  }



  @Override
  public void onBindChildViewHolder(albumSongListViewHolder holder, int flatPosition, ExpandableGroup group,
      int childIndex) {
    final albumsongs songs = (albumsongs) group.getItems().get(childIndex);
          holder.trackNo.setText(songs.getTrackNo());
          holder.lyricist.setText(FirstLetterUpperCase.convert(songs.getLyricistNames()));
          holder.songtitle.setText(FirstLetterUpperCase.convert(songs.getSongName()));

      }

  @Override
  public void onBindGroupViewHolder(albumViewHolder holder, int flatPosition,
      ExpandableGroup group) {
    albumModel album = albums.get(flatPosition);

//        final Typeface font = Typeface.createFromAsset(Context.getAssets(), "Timber.ttf");
    //  holder.title.setTypeface(font);
    holder.title.setText(FirstLetterUpperCase.convert(album.getMovietitle()));
    holder.count.setText(album.getNumOfSongs() + " songs");
    Glide.with(context).load(album.getImageString()).into(holder.thumbnail);
  }
}