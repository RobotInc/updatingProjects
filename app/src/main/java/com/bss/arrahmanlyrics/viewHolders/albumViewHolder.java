package com.bss.arrahmanlyrics.viewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bss.arrahmanlyrics.R;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

public class albumViewHolder extends GroupViewHolder {

	public TextView title, count;
	public ImageView thumbnail;

	public albumViewHolder(View view) {
		super(view);
		title = (TextView) view.findViewById(R.id.Title);
		count = (TextView) view.findViewById(R.id.TotalSongs);
		thumbnail = (ImageView) view.findViewById(R.id.albumimg);

	}

}