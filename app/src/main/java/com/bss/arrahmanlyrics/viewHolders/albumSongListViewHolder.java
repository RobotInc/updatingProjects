package com.bss.arrahmanlyrics.viewHolders;

import android.view.View;
import android.widget.TextView;

import com.bss.arrahmanlyrics.R;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

public class albumSongListViewHolder extends ChildViewHolder {

  public TextView trackNo,lyricist,songtitle;


  public albumSongListViewHolder(View itemView) {
    super(itemView);
    trackNo = (TextView) itemView.findViewById(R.id.trackNo);
    lyricist= (TextView) itemView.findViewById(R.id.Songlyricist);
    songtitle = (TextView) itemView.findViewById(R.id.Songtitle);


  }


}