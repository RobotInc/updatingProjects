package com.bss.arrahmanlyrics;


import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.arrahmanlyrics.adapter.SongAdapter;
import com.bss.arrahmanlyrics.adapter.albumAdapter;
import com.bss.arrahmanlyrics.models.Tracks;
import com.bss.arrahmanlyrics.models.albumModel;
import com.bss.arrahmanlyrics.models.albumsongs;
import com.bss.arrahmanlyrics.models.song;
import com.bss.arrahmanlyrics.models.songModel;
import com.bss.arrahmanlyrics.utils.CustomViewPager;
import com.bss.arrahmanlyrics.utils.DividerItemDecoration;
import com.bss.arrahmanlyrics.utils.Helper;
import com.bss.arrahmanlyrics.utils.MusicService;
import com.bss.arrahmanlyrics.utils.RecyclerItemClickListener;
import com.bss.arrahmanlyrics.utils.StorageUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import info.hoang8f.android.segmented.SegmentedGroup;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, MusicService.mainActivityCallback {

	private static final String TAG = "MainActivity";
	//firebase
	FirebaseAuth auth;
	GoogleApiClient mGoogleApiClient;
	private FirebaseAuth mFirebaseAuth;
	FirebaseUser user;
	int firebaseSignRequestCode = 5;
	DatabaseReference reference;
	CustomViewPager viewPager;
	ProgressDialog dialog;
	int count;

	//list
	RecyclerView rv1;
	RecyclerView rv2;
	albumAdapter albumadapter;
	SongAdapter songadapter;
	List<albumModel> albumList;
	List<songModel> songList;
	List<Tracks> trackList;
	List<albumsongs> albumsongsList;
	int totalSongs = 0;
	ArrayList<song> playlist = new ArrayList<>();

	//database values
	public HashMap<String, Object> values = new HashMap<>();

	//mediaconrols
	ImageButton playpause, previous, next,shuffle,fav;
	SeekBar seekBar;
	TextView currentTime, totalTime, moviename, songname;

	//musicservice
	boolean serviceBound = false;
	MusicService player;
	SegmentedGroup segmentedGroup;
	//fragments
	EnglishFragment englishFragment;
	TamilFragment tamilFragment;
	private Handler mHandler = new Handler();
	HashMap<String,Bitmap> albumcovers;

	public static final String Broadcast_PLAY_NEW_AUDIO = "com.bss.arrahmanlyrics.activites.PlayNewAudio";
	public static final String Broadcast_NEW_ALBUM = "com.bss.arrahmanlyrics.activites.PlayNewAlbum";
	public static final String Broadcast_PLAY = "com.bss.arrahmanlyrics.activites.Play";
	public static final String Broadcast_PAUSE = "com.bss.arrahmanlyrics.activites.Pause";
	public static final String Broadcast_NEXT = "com.bss.arrahmanlyrics.activites.Next";
	public static final String Broadcast_Prev = "com.bss.arrahmanlyrics.activites.Previous";
	public static final String Broadcast_Shuffle = "com.bss.arrahmanlyrics.activites.Shuffle";
	public static final String Broadcast_UnShuffle = "com.bss.arrahmanlyrics.activites.UnShuffle";
	public static final String Broadcast_EQTOGGLE = "com.bss.arrahmanlyrics.activites.eqToggle";
	public static final String Broadcast_ADDTOQUEUE = "com.bss.arrahmanlyrics.activites.addToQueue";
	public static final String Broadcast_REMOVEFROMQUERE = "com.bss.arrahmanlyrics.activites.removeFromQueue";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		auth = FirebaseAuth.getInstance();


		//signin initialize
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.default_web_client_id))
				.requestEmail()
				.build();
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
					@Override
					public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

					}
				} /* OnConnectionFailedListener */)
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
				.build();
		mFirebaseAuth = FirebaseAuth.getInstance();
		user = mFirebaseAuth.getCurrentUser();

		if (user == null) {
			signIn();
		} else {
			initUI();
			Toast.makeText(this, "signed in", Toast.LENGTH_SHORT).show();
		}


	}

	//app signin
	private void signIn() {
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, firebaseSignRequestCode);
	}


	public void signinTry() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

		builder.setTitle("Error While Connecting");
		builder.setMessage("oops Looks like network issues make sure your internet connection is on and try again... ");
		builder.setNegativeButton("Quit",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
					                    int which) {
						System.exit(1);
					}
				});
		builder.setPositiveButton("Try again",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
					                    int which) {
						signIn();
					}
				});

		builder.show();

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		signinTry();
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);

		if (requestCode == firebaseSignRequestCode) {
			Log.e("test", String.valueOf(requestCode));
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

			handleSignInResult(result);
		} else {
			signinTry();
		}
	}

	private void handleSignInResult(GoogleSignInResult result) {
		Log.d("Sign In", "handleSignInResult:" + result.isSuccess());
		if (result.isSuccess()) {
			// Signed in successfully, show authenticated UI.
			GoogleSignInAccount acct = result.getSignInAccount();
			Log.i("user Name", acct.getDisplayName());


			firebaseAuthWithGoogle(acct);


		} else {


			signinTry();
		}
	}

	private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
		Log.d("Sign in", "firebaseAuthWithGoogle:" + acct.getId());

		final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
		mFirebaseAuth.signInWithCredential(credential)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						Log.d("sign in", "signInWithCredential:onComplete:" + task.isSuccessful());
						//String pic = acct.getPhotoUrl().toString();
						//Toast.makeText(getApplicationContext(),pic,Toast.LENGTH_SHORT).show();
						// Picasso.with(getApplicationContext()).load(pic).into(profileImage);
						// If sign in fails, display a message to the user. If sign in succeeds
						// the auth state listener will be notified and logic to handle the
						// signed in user can be handled in the listener.
						if (task.isSuccessful()) {
							user = mFirebaseAuth.getCurrentUser();
							initUI();

						}

						//userEmailId.setText(user.getEmail());
						if (!task.isSuccessful()) {
							Log.w("Sign in", "signInWithCredential", task.getException());
							signinTry();
						}
						// ...
					}
				});
	}

	@Override
	public void onBackPressed() {


		final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else if (drawer.isDrawerOpen(GravityCompat.END)) {
			drawer.closeDrawer(GravityCompat.END);
		} else {

			super.onBackPressed();
		}
	}

	private void initUI() {
		dialog = new ProgressDialog(this);

		dialog.setMessage("Loading Database");


		dialog.show();

		mHandler.post(runnable);
		playpause = (ImageButton) findViewById(R.id.playpause);
		previous = (ImageButton) findViewById(R.id.previous);
		next = (ImageButton) findViewById(R.id.next);
		shuffle = (ImageButton) findViewById(R.id.shuffle);
		fav = (ImageButton) findViewById(R.id.fav_pop);

		viewPager = (CustomViewPager) findViewById(R.id.vg);
		//viewPager.setPagingEnabled(false);
		englishFragment = new EnglishFragment();
		tamilFragment = new TamilFragment();
		currentTime = (TextView) findViewById(R.id.currentTime);
		totalTime = (TextView) findViewById(R.id.totalTime);
		songname = (TextView) findViewById(R.id.songname);
		moviename = (TextView) findViewById(R.id.moviename);

		segmentedGroup = (SegmentedGroup) findViewById(R.id.segmented);
		segmentedGroup.setTintColor(getResources().getColor(R.color.amber_900));
		segmentedGroup.check(R.id.tamil);
		segmentedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup radioGroup, int i) {
				switch (i) {
					case R.id.tamil:
						viewPager.setCurrentItem(0);
						break;
					case R.id.english:
						viewPager.setCurrentItem(1);
						break;
				}
			}
		});
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


			}

			@Override
			public void onPageSelected(int position) {
				if(position == 0){
					segmentedGroup.check(R.id.tamil);
				}else if(position == 1){
					segmentedGroup.check(R.id.english);
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});


		SectionsPagerAdapter lyricsAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		lyricsAdapter.addFragment(tamilFragment, "Tamil");
		lyricsAdapter.addFragment(englishFragment, "English");

		viewPager.setAdapter(lyricsAdapter);

		playpause.setOnClickListener(this);
		previous.setOnClickListener(this);
		next.setOnClickListener(this);
		shuffle.setOnClickListener(this);

		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				if (b) {
					player.seekTo(i);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
		count = 0;

		final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ImageButton menuLeft = (ImageButton) findViewById(R.id.menuleft);
		ImageButton menuRight = (ImageButton) findViewById(R.id.menuright);


		menuLeft.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (drawer.isDrawerOpen(GravityCompat.START)) {
					drawer.closeDrawer(GravityCompat.START);
				} else {
					drawer.openDrawer(GravityCompat.START);
				}
			}
		});

		menuRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (drawer.isDrawerOpen(GravityCompat.END)) {
					drawer.closeDrawer(GravityCompat.END);
				} else {
					drawer.openDrawer(GravityCompat.END);
				}
			}
		});

		reference = FirebaseDatabase.getInstance().getReference().child("AR Rahman").child("Tamil");
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				values = (HashMap<String, Object>) dataSnapshot.getValue();
				setImagesList();
				setUpAlbumList();
				setUpSongList();
				dialog.hide();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});

	}

	private void setUpAlbumList() {
		albumList = new ArrayList<>();
		albumsongsList = new ArrayList<>();
		prepareAlbums();

	}

	private void setUpSongList() {
		StorageUtil storageUtil = new StorageUtil(getApplicationContext());
		storageUtil.clearCachedAudioPlaylist();
		songList = new ArrayList<>();
		songadapter = new SongAdapter(MainActivity.this, songList,MainActivity.this);

		rv1 = (RecyclerView) findViewById(R.id.rv1);
		rv1.setAdapter(songadapter);
		rv1.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
		rv1.setItemAnimator(new DefaultItemAnimator());
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		rv1.setLayoutManager(layoutManager);
		rv1.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				songModel song = songadapter.getItem(position);
				StorageUtil storageUtil = new StorageUtil(getApplicationContext());
				if (storageUtil.loadAudio() == null || storageUtil.loadAudio().size() < totalSongs) {
					playlist.clear();
					for (songModel songs : songList) {
						song s = new song(songs.getMovietitle(), songs.getSongTitle(), songs.getUlr());
						playlist.add(s);
					}
					storageUtil.storeAudio(playlist);
					storageUtil.storeAudioIndex(position);
					Intent setplaylist = new Intent(MainActivity.Broadcast_NEW_ALBUM);
					sendBroadcast(setplaylist);
					Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_NEW_AUDIO);
					sendBroadcast(broadcastIntent);
					closeDrawer();

				} else {
					storageUtil.storeAudioIndex(position);
					Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_NEW_AUDIO);
					sendBroadcast(broadcastIntent);
					closeDrawer();

				}

			}
		}));
		prepareSongs();

	}

	private void prepareSongs() {
		songList.clear();
		List<songModel> list = new ArrayList<>();
		SortedSet<String> trackNos = new TreeSet<>();
		for (String albums : values.keySet()) {
			HashMap<String, Object> songs = (HashMap<String, Object>) values.get(albums);

			for (String song : songs.keySet()) {
				if (!song.equals("IMAGE")) {
					HashMap<String, Object> oneSong = (HashMap<String, Object>) songs.get(song);
					songModel newSong = new songModel(albums, song, oneSong.get("Lyricist").toString(), String.valueOf(oneSong.get("Download")));
					list.add(newSong);
					trackNos.add(song);
				}

			}
		}


		for (String Track : trackNos) {
			for (songModel songNo : list) {
				if (songNo.getSongTitle().equals(Track)) {
					songList.add(songNo);
				}
			}

		}

		totalSongs = songList.size();

		songadapter.notifyDataSetChanged();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dialog.dismiss();
	}

	private void prepareAlbums() {
		albumList.clear();

		List<albumModel> list = new ArrayList<>();

		SortedSet<String> trackNos = new TreeSet<>();
		for (String album : values.keySet()) {
			list.add(new albumModel(album, ((HashMap<String, Object>) values.get(album)).size() - 1));
			trackNos.add(album);
		}
		trackList = new ArrayList<>();
		for (String tracks : trackNos) {
			for (albumModel album : list) {
				if (album.getMovietitle().equals(tracks)) {
					HashMap<String, Object> songs = (HashMap<String, Object>) values.get(album.getMovietitle());
					albumsongsList = new ArrayList<>();
					for (String song : songs.keySet()) {
						if (!song.equals("IMAGE")) {
							HashMap<String, Object> oneSong = (HashMap<String, Object>) songs.get(song);
							albumsongs albumsong = new albumsongs(song, String.valueOf(oneSong.get("Track NO")), String.valueOf(oneSong.get("Lyricist")));
							albumsongsList.add(albumsong);
						}
					}
					Tracks tracks1 = new Tracks(album.getMovietitle(), getSortedList(albumsongsList));

					trackList.add(tracks1);
					albumList.add(album);
				}
			}
		}

		albumadapter = new albumAdapter(trackList, MainActivity.this, albumList,MainActivity.this);
		rv2 = (RecyclerView) findViewById(R.id.rv2);
		rv2.setAdapter(albumadapter);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		rv2.setLayoutManager(layoutManager);




	}

	private byte[] getImage(String imageString) {
		if (imageString.equals(null)) {
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byte[] bitMapData = stream.toByteArray();
			return bitMapData;

		}
		byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
		//Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
		return decodedString;
	}

	public Bitmap getBitmap(String imageString) {
		if (imageString.equals(null)) {
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
			return bitmap;
		}
		byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
		Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
		//Bitmap resized = Bitmap.createScaledBitmap(bitmap, 65, 65, false);
		return bitmap;
	}

	public List<albumsongs> getSortedList(List<albumsongs> list) {
		SortedSet<Integer> trackNos = new TreeSet<>();
		List<albumsongs> sorted = new ArrayList<>();
		for (albumsongs songs : list) {
			trackNos.add(Integer.parseInt(songs.getTrackNo()));
		}

		for (int numbers : trackNos) {
			for (albumsongs songs : list) {
				if (numbers == Integer.parseInt(songs.getTrackNo())) {
					sorted.add(songs);
				}
			}
		}

		return sorted;
	}

	@Override
	public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
		super.onSaveInstanceState(outState, outPersistentState);
		outState.putBoolean("serviceStatus", serviceBound);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		serviceBound = savedInstanceState.getBoolean("serviceStatus");
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
			player = binder.getService();
			player.setMainCallbacks(MainActivity.this);
			serviceBound = true;

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			serviceBound = false;
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		if (!serviceBound) {
			Intent playerIntent = new Intent(this, MusicService.class);
			startService(playerIntent);
			bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
			Log.i("bounded", "service bounded");


		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i("testing", "am in stop");
		if (serviceBound) {
			unbindService(serviceConnection);
			serviceBound = false;
			//player.setCallbacks(null);
		}
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
			case R.id.playpause: {
				if (player != null) {
					if (player.isPlaying()) {
						playpause.setImageResource(R.drawable.play);
						player.pauseMedia();
					} else {
						if(player.mediaPlayer != null) {
							if (player.getCurrentPosition() > 0) player.resumeMedia();
							playpause.setImageResource(R.drawable.pause);
						}
					}
				}
				break;
			}
			case R.id.previous: {
				if (player != null) {
					if (player.isPlaying()) {
						if (new StorageUtil(getApplicationContext()).loadAudio().size() > 0) {
							player.skipToPrevious();
						}
					}
				}
				break;
			}
			case R.id.next: {
				if (player != null) {
					if (player.isPlaying()) {
						if (new StorageUtil(getApplicationContext()).loadAudio().size() > 0) {
							player.skipToNext();
						}
					}
				}
				break;
			}
			case R.id.shuffle: {
				if(player != null){
					if(player.isShuffleOn()){
						player.setShuffleOnOff(false);
						shuffle.setImageResource(R.drawable.shuffleoff);
					}else {
						player.setShuffleOnOff(true);
						shuffle.setImageResource(R.drawable.shuffle);

					}
				}
				break;
			}
		}

	}


	@Override
	public void update() {
		if (player != null) {
			if (player.isPlaying()) {
				if (dialog.isShowing()) {
					dialog.hide();
				}

				seekBar.setMax(player.getDuration());
				totalTime.setText(Helper.durationCalculator(player.getDuration()));
				playpause.setImageResource(R.drawable.pause);
				setLyrics(player.getActiveSong());

				Log.i("CalledSet", "called set details");

			} else {

				seekBar.setMax(player.getDuration());

				playpause.setImageResource(android.R.drawable.ic_media_play);
			}


		} else {

			seekBar.setProgress(player.getCurrentPosition());

			Log.i("CalledSet", "called set details");


		}

	}

	@Override
	public void showDialog(String song,String movie) {

		if(dialog != null){
			Log.i(TAG, "showDialog: loading ");

				dialog.setMessage("Loading "+Helper.FirstLetterCaps(song)+"\nFrom "+Helper.FirstLetterCaps(movie));
				dialog.show();

		}
	}

	private void setLyrics(song activeSong) {
		String movieTitle = activeSong.getMovieName();
		String songTitle = activeSong.getSongName();
		songname.setText(Helper.FirstLetterCaps(songTitle));
		moviename.setText(Helper.FirstLetterCaps(movieTitle));

		HashMap<String, Object> songs = (HashMap<String, Object>) values.get(movieTitle);
		HashMap<String, Object> songlyrics = (HashMap<String, Object>) songs.get(songTitle);
		String english1 = String.valueOf(songlyrics.get("English"));
		String english2 = String.valueOf(songlyrics.get("EnglishOne"));
		englishFragment.setLyrics(english1, english2);

		String tamil1 = String.valueOf(songlyrics.get("Others"));
		String tamil2 = String.valueOf(songlyrics.get("OthersOne"));
		tamilFragment.setLyrics(tamil1, tamil2);


	}

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			if (player != null) {
				if (player.isPlaying()) {
					int position = player.getCurrentPosition();
					seekBar.setProgress(position);
					currentTime.setText(Helper.durationCalculator(position));

				}
			}
			mHandler.postDelayed(runnable, 1000);
		}
	};

	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		private final List<Fragment> mFragmentList = new ArrayList<>();
		private final List<String> mFragmentTitleList = new ArrayList<>();

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return mFragmentList.get(position);
		}

		@Override
		public int getCount() {
			return mFragmentList.size();
		}

		public void addFragment(Fragment fragment, String title) {
			mFragmentList.add(fragment);
			mFragmentTitleList.add(title);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mFragmentTitleList.get(position);
		}
	}

	public void closeDrawer() {
		final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else if (drawer.isDrawerOpen(GravityCompat.END)) {
			drawer.closeDrawer(GravityCompat.END);
		}
	}

	public void setImagesList(){

		albumcovers = new HashMap<>();
		for(String movies : values.keySet()){
			HashMap<String,Object> movie = (HashMap<String, Object>) values.get(movies);
			albumcovers.put(movies,getBitmap(String.valueOf(movie.get("IMAGE"))));

		}

	}

	public Bitmap getImageBitmap(String movie){
		return albumcovers.get(movie);
	}
}