package com.bss.arrahmanlyrics.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bss.arrahmanlyrics.models.songModel;

import java.util.ArrayList;
import java.util.List;

import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.utils.FirstLetterUpperCase;
import com.bumptech.glide.Glide;

/**
 * Created by mohan on 5/20/17.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder>{

	private Context mContext;
	private List<songModel> songlist;


	public SongAdapter(Context context, List<songModel> songlist) {
		this.mContext = context;
		this.songlist = songlist;

		//QuickAction.setDefaultColor(ResourcesCompat.getColor(s.getResources(), R.color.white, null));
		//QuickAction.setDefaultTextColor(Color.BLACK);


	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.song_list_view, parent, false);
		RecyclerView.ViewHolder holder = new MyViewHolder(itemView);

		return (MyViewHolder) holder;
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		songModel actualsong = songlist.get(position);


		holder.name.setText(FirstLetterUpperCase.convert(actualsong.getSongTitle()));
		//holder.name.setText(actualsong.getSongTitle());
		Glide.with(mContext).load(actualsong.getImages()).into(holder.imageView);

		holder.lyricist.setText(FirstLetterUpperCase.convert("Lyricist: " + actualsong.getLyricistName()));
		holder.movietitle.setText(FirstLetterUpperCase.convert("Movie: " + actualsong.getMovietitle()));

	}

	@Override
	public int getItemCount() {
		return songlist.size();
	}


	public class MyViewHolder extends RecyclerView.ViewHolder {
		public TextView name, lyricist, movietitle;
		ImageView imageView;


		public MyViewHolder(View view) {
			super(view);
			name = (TextView) view.findViewById(R.id.Songtitle);
			lyricist = (TextView) view.findViewById(R.id.MovieTitle);
			movietitle = (TextView) view.findViewById(R.id.Songlyricist);
			imageView = (ImageView) view.findViewById(R.id.songCover);


			//albumCover = (ImageView) view.findViewById(R.id.album_artwork);
		}






		public void setFilter(List<songModel> songlists) {
			songlist = new ArrayList<>();
			songlist.addAll(songlists);
			notifyDataSetChanged();
		}

	}
}
