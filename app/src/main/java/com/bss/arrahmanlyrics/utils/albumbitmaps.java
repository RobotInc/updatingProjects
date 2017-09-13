package com.bss.arrahmanlyrics.utils;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by mohan on 9/14/17.
 */

public class albumbitmaps {

	static HashMap<String,Bitmap> cover;

	public static void setCovers(HashMap<String,Bitmap> map){
		cover = map;
	}

	public static Bitmap getBitmap(String name){
		return cover.get(name);
	}

}
