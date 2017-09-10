package com.bss.arrahmanlyrics.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.bss.arrahmanlyrics.MainActivity;
import com.bss.arrahmanlyrics.R;
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
	private MediaSessionManager mediaSessionManager;
	private MediaSessionCompat mediaSession;
	private MediaControllerCompat.TransportControls transportControls;
	public void seekTo(int i) {
		mediaPlayer.seekTo(i);
	}
	private static final int NOTIFICATION_ID = 101;
	NotificationCompat.Builder notificationBuilder;

	public static final String ACTION_PLAY = "com.bss.arrahmanlyrics.ACTION_PLAY";
	public static final String ACTION_PAUSE = "com.bss.arrahmanlyrics.ACTION_PAUSE";
	public static final String ACTION_PREVIOUS = "com.bss.arrahmanlyrics.ACTION_PREVIOUS";
	public static final String ACTION_NEXT = "com.bss.arrahmanlyrics.ACTION_NEXT";
	public static final String ACTION_STOP = "com.bss.arrahmanlyrics.ACTION_STOP";
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
		if (mediaSessionManager == null) {
			try {
				initMediaSession();
				//initMediaPlayer();
			} catch (RemoteException e) {
				e.printStackTrace();
				stopSelf();
			}
			//buildNotification(PlaybackStatus.PAUSED);
		}
		handleIncomingActions(intent);
		//we have some options for service
		//start sticky means service will be explicity started and stopped
		return START_STICKY;
	}

	private void initMediaSession() throws RemoteException {
		if (mediaSessionManager != null) return; //mediaSessionManager exists

		mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
		// Create a new MediaSession
		mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
		//Get MediaSessions transport controls
		transportControls = mediaSession.getController().getTransportControls();
		//set MediaSession -> ready to receive media commands
		mediaSession.setActive(true);
		//indicate that the MediaSession handles transport control commands
		// through its MediaSessionCompat.Callback.
		mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

		//Set mediaSession's MetaData
		//updateMetaData();

		// Attach Callback to receive MediaSession updates
		mediaSession.setCallback(new MediaSessionCompat.Callback() {
			// Implement callbacks
			@Override
			public void onPlay() {
				super.onPlay();
				if (mediaPlayer == null) return;
				resumeMedia();
				buildNotification(PlaybackStatus.PLAYING);

				if (maincallback != null) {
					maincallback.update();
				}

			}

			@Override
			public void onPause() {
				super.onPause();
				if (mediaPlayer == null) return;
				pauseMedia();
				buildNotification(PlaybackStatus.PAUSED);


				if (maincallback != null) {
					maincallback.update();
				}

			}

			@Override
			public void onSkipToNext() {
				super.onSkipToNext();
				if (mediaPlayer == null) return;
				skipToNext();
				updateMetaData();
				buildNotification(PlaybackStatus.PLAYING);

			}

			@Override
			public void onSkipToPrevious() {
				super.onSkipToPrevious();
				if (mediaPlayer == null) return;
				skipToPrevious();
				updateMetaData();
				buildNotification(PlaybackStatus.PLAYING);

			}

			@Override
			public void onStop() {
				super.onStop();
				if (mediaPlayer == null) return;
				removeNotification();
				//Stop the service
				stopSelf();

				if (maincallback != null) {
					maincallback.update();
				}

			}

			@Override
			public void onSeekTo(long position) {
				super.onSeekTo(position);
			}
		});
	}
	private void updateMetaData() {
		Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_launcher); //replace with medias albumArt
		// Update the current metadata
		mediaSession.setMetadata(new MediaMetadataCompat.Builder()
				.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
				.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeSong.getMovieName())
				.putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeSong.getSongName())
				.build());
	}


	private void buildNotification(PlaybackStatus playbackStatus) {

		int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
		PendingIntent play_pauseAction = null;

		//Build a new notification according to the current state of the MediaPlayer
		if (playbackStatus == PlaybackStatus.PLAYING) {
			notificationAction = android.R.drawable.ic_media_pause;
			//create the pause action
			play_pauseAction = playbackAction(1);
		} else if (playbackStatus == PlaybackStatus.PAUSED) {
			notificationAction = android.R.drawable.ic_media_play;
			//create the play action
			play_pauseAction = playbackAction(0);
		}

		Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_launcher); //replace with your own image

		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendInt = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Create a new Notification
		notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
				.setShowWhen(false)
				.setAutoCancel(true)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setCategory(Intent.CATEGORY_APP_MUSIC)
				.setPriority(Notification.PRIORITY_DEFAULT)
				// Set the Notification style
				.setStyle(new NotificationCompat.MediaStyle()
						// Attach our MediaSession token
						.setMediaSession(mediaSession.getSessionToken())
						// Show our playback controls in the compact notification view.
						.setShowActionsInCompactView(0, 1, 2))

				// Set the Notification color
				.setColor(getResources().getColor(R.color.colorPrimary))
				// Set the large and small icons
				.setLargeIcon(largeIcon)
				.setSmallIcon(android.R.drawable.stat_sys_headset)
				// Set Notification content information
				.setContentIntent(pendInt)
				.setContentTitle(activeSong.getMovieName())
				.setContentInfo(activeSong.getSongName())
				// Add playback actions
				.addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
				.addAction(notificationAction, "pause", play_pauseAction)
				.addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));
		if (playbackStatus == PlaybackStatus.PLAYING) {
			notificationBuilder.setOngoing(true);
		} else if (playbackStatus == PlaybackStatus.PAUSED) {
			notificationBuilder.setOngoing(false);
		}
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());

	}

	private void removeNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
		stopForeground(true);
		//notificationManager.cancelAll();
	}

	private PendingIntent playbackAction(int actionNumber) {
		Intent playbackAction = new Intent(this, MusicService.class);
		switch (actionNumber) {
			case 0:
				// Play
				playbackAction.setAction(ACTION_PLAY);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			case 1:
				// Pause
				playbackAction.setAction(ACTION_PAUSE);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			case 2:
				// Next track
				playbackAction.setAction(ACTION_NEXT);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			case 3:
				// Previous track
				playbackAction.setAction(ACTION_PREVIOUS);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			default:
				break;
		}
		return null;
	}

	private void handleIncomingActions(Intent playbackAction) {
		if (playbackAction == null || playbackAction.getAction() == null) return;

		String actionString = playbackAction.getAction();
		if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
			transportControls.play();
		} else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
			transportControls.pause();
		} else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
			transportControls.skipToNext();
		} else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
			transportControls.skipToPrevious();
		} else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
			transportControls.stop();
		}
	}

	@Override
	public void onDestroy() {

		//stopping the player when service is destroyed
		if (mediaPlayer != null) {
			new StorageUtil(getApplicationContext()).storeResumePosition(mediaPlayer.getCurrentPosition());
			pauseMedia();
			stopMedia();
			mediaPlayer.release();
		}

		mediaPlayer.release();
		unregisterReceiver(setNewAlbum);
		unregisterReceiver(playNewAudio);
		removeNotification();
		super.onDestroy();

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
			updateMetaData();
			buildNotification(PlaybackStatus.PLAYING);
		}
	}

	public void pauseMedia() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			updateMetaData();
			buildNotification(PlaybackStatus.PAUSED);

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
					buildNotification(PlaybackStatus.PLAYING);

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
		updateMetaData();
		buildNotification(PlaybackStatus.PLAYING);
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
