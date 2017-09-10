package com.bss.arrahmanlyrics.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mohan on 5/20/17.
 */

public class albumsongs{
    String songName;
    String trackNo;
    String lyricistNames;
    String ulr;
    String year;


    public albumsongs(String songName, String trackNo, String lyricistNames,String ulr,String year) {
        this.songName = songName;
        this.trackNo = trackNo;
        this.lyricistNames = lyricistNames;
        this.ulr = ulr;
        this.year = year;

    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getUlr() {
        return ulr;
    }

    public void setUlr(String ulr) {
        this.ulr = ulr;
    }

    protected albumsongs(Parcel in) {
        songName = in.readString();
        trackNo = in.readString();
        lyricistNames = in.readString();

    }


    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getTrackNo() {
        return trackNo;
    }

    public void setTrackNo(String trackNo) {
        this.trackNo = trackNo;
    }

    public String getLyricistNames() {
        return lyricistNames;
    }

    public void setLyricistNames(String lyricistNames) {
        this.lyricistNames = lyricistNames;
    }






}
