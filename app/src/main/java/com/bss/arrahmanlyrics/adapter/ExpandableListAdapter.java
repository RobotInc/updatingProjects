package com.bss.arrahmanlyrics.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bss.arrahmanlyrics.MainActivity;
import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.models.albumModel;
import com.bss.arrahmanlyrics.models.albumsongs;
import com.bss.arrahmanlyrics.utils.Helper;

import static android.content.ContentValues.TAG;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

	private Context _context;
	private List<albumModel> _listDataHeader; // header titles
	MainActivity activity;
	// child data in format of header title, child title
	private HashMap<String, List<albumsongs>> _listDataChild;

	public ExpandableListAdapter(Context context, List<albumModel> listDataHeader,
	                             HashMap<String, List<albumsongs>> listChildData, MainActivity activity) {
		this._context = context;
		this._listDataHeader = listDataHeader;
		this._listDataChild = listChildData;
		this.activity = activity;
	}

	@Override
	public albumsongs getChild(int groupPosition, int childPosititon) {
		return this._listDataChild.get(this._listDataHeader.get(groupPosition).getMovietitle())
				.get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,
	                         boolean isLastChild, View convertView, ViewGroup parent) {

		albumsongs songs = getChild(groupPosition, childPosition);
		Log.i(TAG, "getChildView: " + String.valueOf(songs));

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.album_song_list_view, null);
		}
		TextView trackNo = (TextView) convertView.findViewById(R.id.trackNo);
		TextView lyricist = (TextView) convertView.findViewById(R.id.Songlyricist);
		TextView songtitle = (TextView) convertView.findViewById(R.id.Songtitle);
		trackNo.setText(songs.getTrackNo());
		lyricist.setText(Helper.FirstLetterCaps(songs.getLyricistNames()));
		songtitle.setText(Helper.FirstLetterCaps(songs.getSongName()));
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		Log.i(TAG, "getChildrenCount: " + _listDataChild);

		return this._listDataChild.get(this._listDataHeader.get(groupPosition).getMovietitle())
				.size();
	}

	@Override
	public albumModel getGroup(int groupPosition) {
		return this._listDataHeader.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return this._listDataHeader.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
	                         View convertView, ViewGroup parent) {
		albumModel album = getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.album_view, null);
		}

		TextView title = (TextView) convertView.findViewById(R.id.Title);
		TextView count = (TextView) convertView.findViewById(R.id.TotalSongs);
		ImageView thumbnail = (ImageView) convertView.findViewById(R.id.albumimg);
		title.setText(Helper.FirstLetterCaps(album.getMovietitle()));
		count.setText(album.getNumOfSongs() + " songs");
		//Glide.with(context).load(album.getImageString()).into(holder.thumbnail);
		thumbnail.setImageBitmap(activity.getImageBitmap(album.getMovietitle()));

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	/*public void setFilter(List<albumModel> list,HashMap<String,List<albumsongs>> map){
		this._listDataHeader.clear();
		this._listDataHeader = list;
		this._listDataChild.clear();
		this._listDataChild = map;
		notifyDataSetChanged();

	}*/

	public void filterData(List<albumModel> models,HashMap<String,List<albumsongs>> map, String query) {

		query = query.toLowerCase().trim();

		List<albumModel> dummy = new ArrayList<>();
		HashMap<String, List<albumsongs>> dummy2 = new HashMap<>();

		for (albumModel model : models) {

			String name = model.getMovietitle().toLowerCase();
			String songname = "";
			String lyrics="";
			String year="";
			List<albumsongs> songs = map.get(model.getMovietitle());
			for(albumsongs s : songs){
				songname = s.getSongName().toLowerCase();
				lyrics = s.getLyricistNames().toLowerCase();
				year = s.getYear();
				if(name.contains(query)||songname.contains(query)||lyrics.contains(query)||year.contains(query)){
					if(!dummy2.containsKey(model.getMovietitle())){
						dummy.add(model);
						dummy2.put(model.getMovietitle(), map.get(model.getMovietitle()));
					}

				}
			}



		}
		if(dummy.size()>0){
			_listDataHeader = new ArrayList<>();
			_listDataChild = new HashMap<>();
			_listDataHeader.addAll(dummy);
			_listDataChild.putAll(dummy2);
		}
		notifyDataSetChanged();
	}
	public HashMap<String,List<albumsongs>> get_listDataChild(){
		return _listDataChild;
	}
	public List<albumModel> get_listDataHeader(){
		return _listDataHeader;
	}

	public void setall(List<albumModel> models,HashMap<String,List<albumsongs>> map){
		if(models !=null && map != null) {
			_listDataHeader = new ArrayList<>();
			_listDataChild = new HashMap<>();
			_listDataHeader.addAll(models);
			_listDataChild.putAll(map);
			notifyDataSetChanged();
		}

	}

}

