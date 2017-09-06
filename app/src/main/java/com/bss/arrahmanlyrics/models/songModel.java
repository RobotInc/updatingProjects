package com.bss.arrahmanlyrics.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mohan on 6/30/17.
 */

public class songModel{
	String Movietitle;
	String songTitle;
	String lyricistName;
	byte[] images;
	String ulr;

	public songModel(String movietitle, String songTitle, String lyricistName, byte[] images, String ulr) {
		Movietitle = movietitle;
		this.songTitle = songTitle;
		this.lyricistName = lyricistName;
		this.images = images;
		this.ulr = ulr;
	}




	public String getUlr() {
		return ulr;
	}

	public void setUlr(String ulr) {
		this.ulr = ulr;
	}

	public String getMovietitle() {
		return Movietitle;
	}

	public void setMovietitle(String movietitle) {
		Movietitle = movietitle;
	}

	public String getSongTitle() {
		return songTitle;
	}

	public void setSongTitle(String songTitle) {
		this.songTitle = songTitle;
	}

	public String getLyricistName() {
		return lyricistName;
	}

	public void setLyricistName(String lyricistName) {
		this.lyricistName = lyricistName;
	}

	public byte[] getImages() {
		return images;
	}

	public void setImages(byte[] images) {
		this.images = images;
	}


}
