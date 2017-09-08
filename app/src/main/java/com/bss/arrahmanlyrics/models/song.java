package com.bss.arrahmanlyrics.models;

/**
 * Created by mohan on 9/7/17.
 */

public class song {
	String movieName;
	String songName;
	String url;


	public song(String movieName, String songName, String url) {
		this.movieName = movieName;
		this.songName = songName;
		this.url = url;
	}

	public String getMovieName() {
		return movieName;
	}

	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}

	public String getSongName() {
		return songName;
	}

	public void setSongName(String songName) {
		this.songName = songName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
