package com.bss.arrahmanlyrics.models;

import android.graphics.Bitmap;
import android.os.Parcel;

/**
 * Created by mohan on 6/30/17.
 */

public class albumModel {
	String Movietitle;
	int numOfSongs;
	byte[] imageString;
	private Bitmap image;

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	public albumModel(String movietitle, int numOfSongs, byte[] imageString, Bitmap image) {
		this.Movietitle = movietitle;

		this.numOfSongs = numOfSongs;

		this.imageString = imageString;

		this.image = image;

	}





	public String getMovietitle() {
		return Movietitle;
	}

	public void setMovietitle(String movietitle) {
		Movietitle = movietitle;
	}



	public byte[] getImageString() {
		return imageString;
	}

	public void setImageString(byte[] imageString) {
		this.imageString = imageString;
	}


	public int getNumOfSongs() {
		return numOfSongs;
	}

	public void setNumOfSongs(int numOfSongs) {
		this.numOfSongs = numOfSongs;
	}
}
