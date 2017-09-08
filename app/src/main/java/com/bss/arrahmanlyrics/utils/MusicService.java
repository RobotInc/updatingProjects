package com.bss.arrahmanlyrics.utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bss.arrahmanlyrics.MainActivity;
import com.bss.arrahmanlyrics.application;
import com.bss.arrahmanlyrics.models.song;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.ContentValues.TAG;

/**
 * Created by Belal on 12/30/2016.
 */

public class MusicService extends Service implements MediaPlayer.OnCompletionListener,
		MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
		MediaPlayer.OnBufferingUpdateListener, CacheListener, AudioManager.OnAudioFocusChangeListener {
	//creating a mediaplayer object
	public MediaPlayer mediaPlayer;
	private final IBinder iBinder = new LocalBinder();
	ArrayList<song> playlist = new ArrayList<>();
	song activeSong;
	int audioIndex;
	boolean shuffleOn = true;
	int resumePosition = 0;
	public mainActivityCallback maincallback;

	public void seekTo(int i) {
		mediaPlayer.seekTo(i);
	}

	public interface mainActivityCallback {
		void update();

		void showDialog(String name, String movie);


	}

	@Override
	public void onCreate() {
		super.onCreate();
		register_setNewalbum();
		register_playNewAudio();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return iBinder;
	}

	public void setMainCallbacks(mainActivityCallback callbacks) {
		this.maincallback = callbacks;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//getting systems default ringtone


		//we have some options for service
		//start sticky means service will be explicity started and stopped
		return START_STICKY;
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		//stopping the player when service is destroyed
		if (mediaPlayer != null) {
			new StorageUtil(getApplicationContext()).storeResumePosition(mediaPlayer.getCurrentPosition());
			stopMedia();
			mediaPlayer.release();
		}

		mediaPlayer.release();
		unregisterReceiver(setNewAlbum);
		unregisterReceiver(playNewAudio);

	}

	@Override
	public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		skipToNext();

	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {
		playMedia();
		if (maincallback != null) {
			maincallback.update();
		}

	}

	@Override
	public void onSeekComplete(MediaPlayer mediaPlayer) {

	}

	@Override
	public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {

	}

	@Override
	public void onAudioFocusChange(int i) {

	}

	public class LocalBinder extends Binder {
		public MusicService getService() {
			return MusicService.this;
		}
	}


	private void initMediaPlayer() {

		mediaPlayer = new MediaPlayer();
		//Set up MediaPlayer event listeners
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnBufferingUpdateListener(this);
		mediaPlayer.setOnSeekCompleteListener(this);

		//Reset so that the MediaPlayer is not pointing to another data source
		mediaPlayer.reset();

		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			// Set the data source to the mediaFile location
			mediaPlayer.setDataSource(setProxyUrl(activeSong.getUrl()));

		} catch (IOException e) {
			e.printStackTrace();
			stopSelf();
		}
		mediaPlayer.prepareAsync();
		maincallback.showDialog(activeSong.getSongName(), activeSong.getMovieName());

	}


	private String setProxyUrl(String url) throws IOException {
		HttpProxyCacheServer proxy = application.getProxy(getApplicationContext());
		proxy.registerCacheListener(this, url);
		String proxyUrl = proxy.getProxyUrl(url);
		Log.d("proxy", "Use proxy url " + proxyUrl + " instead of original url " + url);
		return proxyUrl;

	}

	private BroadcastReceiver setNewAlbum = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			//Get the new media index form SharedPreferences
			if (shuffleOn) {
				StorageUtil storage = new StorageUtil(getApplicationContext());
				playlist = getShuffledList(storage.loadAudio());
				audioIndex = storage.loadAudioIndex();

			} else {
				StorageUtil storage = new StorageUtil(getApplicationContext());
				playlist = storage.loadAudio();
				audioIndex = storage.loadAudioIndex();
			}


		}
	};

	private void register_setNewalbum() {
		//Register playNewMedia receiver
		IntentFilter filter = new IntentFilter(MainActivity.Broadcast_NEW_ALBUM);
		registerReceiver(setNewAlbum, filter);

	}

	private ArrayList<song> getShuffledList(ArrayList<song> list) {
		ArrayList<song> shuffleList = new ArrayList<>();
		if (list != null) {

			Random r = new Random();
			List<Integer> randomsNos = new ArrayList<>();
			for (int a = 0; a < list.size(); a++) {
				int b = 0;
				do {
					b = r.nextInt(list.size());
					if (!randomsNos.contains(b)) {
						randomsNos.add(b);
					}
				} while (randomsNos.contains(b) && randomsNos.size() != list.size());

			}

			for (int values : randomsNos) {
				shuffleList.add(list.get(values));
			}

			return shuffleList;
		}
		return null;

	}

	private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			//Get the new media index form SharedPreferences
			audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
			if (audioIndex != -1 && audioIndex < playlist.size()) {
				//index is in a valid range
				song s = new StorageUtil(getApplicationContext()).loadAudio().get(audioIndex);
				for (song sg : playlist) {
					if (s.getSongName().equals(sg.getSongName())) {
						audioIndex = playlist.indexOf(sg);
						activeSong = playlist.get(audioIndex);
					}
				}

			} else {
				stopSelf();
			}

			//A PLAY_NEW_AUDIO action received
			//reset mediaPlayer to play the new Audio
			stopMedia();
			if (mediaPlayer != null) {

				mediaPlayer.reset();
			}

			initMediaPlayer();


		}
	};

	private void register_playNewAudio() {
		//Register playNewMedia receiver
		IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
		registerReceiver(playNewAudio, filter);
	}


	private void stopMedia() {
		if (mediaPlayer == null) return;
		if (mediaPlayer.isPlaying()) {

			mediaPlayer.stop();

		}
	}

	private void playMedia() {
		if (mediaPlayer != null) {
			mediaPlayer.start();
		}
	}

	public void pauseMedia() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			new StorageUtil(getApplicationContext()).storeResumePosition(mediaPlayer.getCurrentPosition());
		}
	}

	public void resumeMedia() {
		resumePosition = new StorageUtil(getApplicationContext()).getResumePosition();
		if (mediaPlayer != null) {
			if (!mediaPlayer.isPlaying()) {
				if (resumePosition != -1) {
					mediaPlayer.seekTo(resumePosition);
					mediaPlayer.start();
				}
			}
		}

	}

	public void skipToPrevious() {

		if (audioIndex <= 0) {
			//if first in playlist
			//set index to the last of audioList
			audioIndex = playlist.size() - 1;
			activeSong = playlist.get(audioIndex);
		} else {
			//get previous in playlist
			activeSong = playlist.get(--audioIndex);
		}
		new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

		stopMedia();
		//reset mediaPlayer
		mediaPlayer.reset();
		initMediaPlayer();
		Log.i(TAG, "skipToPrevious: "+audioIndex);
	}

	public void skipToNext() {
		if (playlist.size() > 0) {
			if (audioIndex == playlist.size() - 1) {
				//if last in playlist
				audioIndex = 0;
				activeSong = playlist.get(audioIndex);
			} else if (audioIndex >= playlist.size()) {
				audioIndex = 0;
				activeSong = playlist.get(audioIndex);
				//get next in playlist


			} else {
				activeSong = playlist.get(++audioIndex);
			}
		}
		new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

		stopMedia();
		//reset mediaPlayer
		mediaPlayer.reset();
		initMediaPlayer();
		Log.i(TAG, "skipToNext: "+audioIndex);
	}

	public boolean isPlaying() {
		if (mediaPlayer == null) {
			return false;
		} else if (mediaPlayer.isPlaying()) {
			return true;
		}
		return false;
	}

	public int getDuration() {
		
		
		return mediaPlayer.getDuration();
	}

	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	public song getActiveSong() {
		return activeSong;
	}

	public boolean isShuffleOn() {
		return shuffleOn;
	}

	public void setShuffleOnOff(boolean set) {
		if (set) {

			StorageUtil storage = new StorageUtil(getApplicationContext());
			playlist = getShuffledList(storage.loadAudio());
			if(mediaPlayer != null) {
				if (isPlaying() || mediaPlayer.getCurrentPosition() > 0) {
					for (song sg : playlist) {
						if (sg.getMovieName().equals(activeSong.getMovieName()) && sg.getSongName().equals(activeSong.getSongName())) {
							audioIndex = playlist.indexOf(sg);
							storage.storeAudioIndex(audioIndex);

						}
					}
				}
			}
			shuffleOn = set;

		} else {
			StorageUtil storage = new StorageUtil(getApplicationContext());
			playlist = storage.loadAudio();
			if (mediaPlayer != null) {
				if (isPlaying() || mediaPlayer.getCurrentPosition() > 0) {
					for (song sg : playlist) {
						if (sg.getMovieName().equals(activeSong.getMovieName()) && sg.getSongName().equals(activeSong.getSongName())) {
							audioIndex = playlist.indexOf(sg);
							storage.storeAudioIndex(audioIndex);
							Log.i(TAG, "setShuffleOnOff: inside set index");

						}
					}
				}
			}

			shuffleOn = set;
		}
	}


}
