package com.bss.arrahmanlyrics.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mohan on 5/20/17.
 */

public class albumsongs implements Parcelable {
    String songName;
    String trackNo;
    String lyricistNames;


    public albumsongs(String songName, String trackNo, String lyricistNames) {
        this.songName = songName;
        this.trackNo = trackNo;
        this.lyricistNames = lyricistNames;

    }

    protected albumsongs(Parcel in) {
        songName = in.readString();
        trackNo = in.readString();
        lyricistNames = in.readString();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(songName);
        dest.writeString(trackNo);
        dest.writeString(lyricistNames);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<albumsongs> CREATOR = new Creator<albumsongs>() {
        @Override
        public albumsongs createFromParcel(Parcel in) {
            return new albumsongs(in);
        }

        @Override
        public albumsongs[] newArray(int size) {
            return new albumsongs[size];
        }
    };

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
