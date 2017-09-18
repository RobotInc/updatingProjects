package com.bss.arrahmanlyrics;


import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.arrahmanlyrics.adapter.ExpandableListAdapter;
import com.bss.arrahmanlyrics.adapter.FavoriteSongAdapter;
import com.bss.arrahmanlyrics.adapter.SongAdapter;
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
import com.bss.arrahmanlyrics.utils.albumbitmaps;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import info.hoang8f.android.segmented.SegmentedGroup;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, MusicService.mainActivityCallback, SearchView.OnQueryTextListener {

	private static final String TAG = "MainActivity";
	//firebase
	FirebaseAuth auth;
	GoogleApiClient mGoogleApiClient;
	private FirebaseAuth mFirebaseAuth;
	FirebaseUser user;
	int firebaseSignRequestCode = 5;
	DatabaseReference reference;

	//viewpager
	CustomViewPager viewPager;
	CustomViewPager viewPager2;


	//list
	RecyclerView rv1, rv3;
	SlidingUpPanelLayout favoritePanel;
	SongAdapter songadapter;
	List<albumModel> albumList;
	List<songModel> songList;
	List<albumsongs> albumsongsList;
	List<songModel> filteredSongList;
	ArrayList<song> playlist = new ArrayList<>();

	SearchView songsearch;
	SearchView albumsearch;


	//database values
	public HashMap<String, Object> values = new HashMap<>();
	private HashMap<String, List<albumsongs>> _listDataChild;
	HashMap<String, ArrayList<String>> favoritesMab;
	HashMap<String, Bitmap> albumcovers;

	//mediaconrols
	ImageButton playpause, previous, next, shuffle, fav;
	SeekBar seekBar;
	TextView currentTime, totalTime, moviename, songname;

	//musicservice
	boolean serviceBound = false;
	MusicService player;


	//fragments
	SegmentedGroup segmentedGroup;
	EnglishFragment englishFragment;
	TamilFragment tamilFragment;
	private Handler mHandler = new Handler();


	ExpandableListAdapter adapter;
	BottomNavigationView bottomMenu;
	FavFragment favFragment;
	about aboutFragment;
	apps appsFragment;

	//variables
	ImageView up;
	int totalSongs = 0;
	Point p;
	ProgressDialog dialog;
	int count;


	boolean ascending = true, ascendingyear = true;
	public static final String Broadcast_PLAY_NEW_AUDIO = "com.bss.arrahmanlyrics.activites.PlayNewAudio";
	public static final String Broadcast_NEW_ALBUM = "com.bss.arrahmanlyrics.activites.PlayNewAlbum";

	private InterstitialAd mInterstitialAd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Fabric.with(this, new Crashlytics());
		MobileAds.initialize(this, "ca-app-pub-7987343674758455~2523296928");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
			Toast.makeText(this, "signed in as " + user.getEmail(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume: on resume called");
		if (serviceBound) {
			if (player != null) {
				player.setMainCallbacks(MainActivity.this);
				update();
			}
		}
		Log.i(TAG, "onResume: on resume over");
	}

	/*@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 121:
				if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
					//TODO
					Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(this, "we need premission to manage media player while receiving phone calls, please grand premission", Toast.LENGTH_LONG).show();

				}
				break;

			default:
				break;
		}
	}*/

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
		} else if (favoritePanel != null &&
				(favoritePanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || favoritePanel.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
			favoritePanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

		} else {

			super.onBackPressed();
		}
	}

	private void initUI() {
		mInterstitialAd = new InterstitialAd(this);
		mInterstitialAd.setAdUnitId("ca-app-pub-7987343674758455/6284132866");
		mInterstitialAd.loadAd(new AdRequest.Builder().build());
		mInterstitialAd.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				// Code to be executed when an ad finishes loading.
				Log.i("Ads Interstitial", "onAdLoaded");
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				// Code to be executed when an ad request fails.
				Log.i("Ads Interstitial", "onAdFailedToLoad" + errorCode);
			}

			@Override
			public void onAdOpened() {
				// Code to be executed when an ad opens an overlay that
				// covers the screen.
				Log.i("Ads Interstitial", "onAdOpened");
			}

			@Override
			public void onAdLeftApplication() {
				// Code to be executed when the user has left the app.
				Log.i("Ads Interstitial", "onAdLeftApplication");
			}

			@Override
			public void onAdClosed() {
				// Code to be executed when when the user is about to return
				// to the app after tapping on an ad.
				Log.i("Ads Interstitial", "onAdClosed");
			}
		});
		up = (ImageView) findViewById(R.id.favup);
		dialog = new ProgressDialog(this);
		dialog.setMessage("Loading Database");
		dialog.show();

		mHandler.post(runnable);
		playpause = (ImageButton) findViewById(R.id.playpause);
		previous = (ImageButton) findViewById(R.id.previous);
		next = (ImageButton) findViewById(R.id.next);
		shuffle = (ImageButton) findViewById(R.id.shuffle);
		fav = (ImageButton) findViewById(R.id.fav_pop);
		fav.setOnClickListener(this);

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
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			@Override
			public void onPageSelected(int position) {
				if (position == 0) {
					segmentedGroup.check(R.id.tamil);
				} else if (position == 1) {
					segmentedGroup.check(R.id.english);
				}
			}
			@Override
			public void onPageScrollStateChanged(int state) {}
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
					if (player != null) {
						if (player.mediaPlayer != null) {
							player.seekTo(i);
						}
					}

				}
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
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
		reference.keepSynced(true);
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				values = (HashMap<String, Object>) dataSnapshot.getValue();
				if (values != null) {
					setImagesList();
					setUpAlbumList();
					setUpSongList();
					setUpFavorites();
					dialog.hide();
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});

		setNavigation();
		favoritePanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		favoritePanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
			@Override
			public void onPanelSlide(View panel, float slideOffset) {

			}

			@Override
			public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
				if (favoritePanel != null &&
						(favoritePanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || favoritePanel.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
					if (mInterstitialAd.isLoaded()) {
						mInterstitialAd.show();
					} else {
						Log.d("TAG", "The interstitial wasn't loaded yet.");
					}
					up.setImageResource(R.drawable.down);
				} else if (favoritePanel != null &&
						(favoritePanel.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED)) {
					up.setImageResource(R.drawable.up);
				}
			}
		});
		favoritePanel.setAnchorPoint(0.7f);
		/*int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
		if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
			Toast.makeText(this, "Please grant \"read call state premission\" for smooth audio playback", Toast.LENGTH_LONG).show();
			ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_PHONE_STATE}, 121);
		}*/

	}

	private void setNavigation() {
		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		NavigationView navigationView2 = (NavigationView) findViewById(R.id.nav_view2);
		View view = navigationView.getHeaderView(0);
		View view2 = navigationView2.getHeaderView(0);
		//alpha = (ImageButton) view.findViewById(R.id.alphasong);
		//alpha.setOnClickListener(this);
		//year = (ImageButton) view.findViewById(R.id.numsong);
		//year.setOnClickListener(this);
		songsearch = (SearchView) view.findViewById(R.id.songsearch);
		songsearch.setOnQueryTextListener(this);
		songsearch.setQueryHint("name,movie,year & lyricist");


		albumsearch = (SearchView) view2.findViewById(R.id.albumsearch);

		albumsearch.setQueryHint("name,movie,year & lyricist");

		albumsearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String s) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				if(albumList != null && _listDataChild !=null) {
					if (s.isEmpty()) {
						adapter.setall(albumList, _listDataChild);
						return false;
					}
					if (s.length() < 1) {
						adapter.setall(albumList, _listDataChild);
						return false;
					}
					adapter.filterData(albumList, _listDataChild, s);
					return true;
				}
				return false;
			}
		});

	}

	private void setUpAlbumList() {
		albumList = new ArrayList<>();
		albumsongsList = new ArrayList<>();

		prepareAlbums();

	}

	private void setUpFavorites() {

		favFragment = new FavFragment();
		aboutFragment = new about();
		appsFragment = new apps();

		viewPager2 = (CustomViewPager) findViewById(R.id.rvg);
		favPageAdapter favPageAdapter = new favPageAdapter(getSupportFragmentManager());
		favPageAdapter.addFragment(favFragment, "Favorite");
		favPageAdapter.addFragment(aboutFragment, "About");
		favPageAdapter.addFragment(appsFragment, "Apps");


		viewPager2.setAdapter(favPageAdapter);
		viewPager2.setPagingEnabled(false);

		bottomMenu = (BottomNavigationView) findViewById(R.id.navigation);

		bottomMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				if (item.getItemId() == R.id.navigation_home) {
					viewPager2.setCurrentItem(0);
				} else if (item.getItemId() == R.id.navigation_notifications) {
					viewPager2.setCurrentItem(1);
				} else if (item.getItemId() == R.id.navigation_dashboard) {
					viewPager2.setCurrentItem(2);
				}
				updateNavigationBarState(item.getItemId());

				return true;
			}
		});
	}

	private void updateNavigationBarState(int actionId) {
		Menu menu = bottomMenu.getMenu();

		for (int i = 0, size = menu.size(); i < size; i++) {
			MenuItem item = menu.getItem(i);
			item.setChecked(item.getItemId() == actionId);
		}
	}
	private void setUpSongList() {
		StorageUtil storageUtil = new StorageUtil(getApplicationContext());
		storageUtil.clearCachedAudioPlaylist();
		songList = new ArrayList<>();
		songadapter = new SongAdapter(MainActivity.this, songList, MainActivity.this);

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

				if (storageUtil.loadAudio() == null || totalSongs > storageUtil.loadAudio().size()) {
					for (songModel songs : songList) {
						song s = new song(songs.getMovietitle(), songs.getSongTitle(), songs.getUlr());
						playlist.add(s);
					}
					int index = 0;
					for (song s : playlist) {
						if (s.getSongName().equals(song.getSongTitle()) && s.getMovieName().equals(song.getMovietitle())) {
							index = playlist.indexOf(s);
						}
					}
					storageUtil.storeAudio(playlist);
					storageUtil.storeAudioIndex(index);
					Intent setplaylist = new Intent(MainActivity.Broadcast_NEW_ALBUM);
					sendBroadcast(setplaylist);
					Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_NEW_AUDIO);
					sendBroadcast(broadcastIntent);
					closeDrawer();

				} else {
					int index = 0;
					Log.i(TAG, "onItemClick: " + song.getSongTitle() + " " + song.getMovietitle());
					ArrayList<song> array = new StorageUtil(getApplicationContext()).loadAudio();
					for (song s : array) {
						if (s.getSongName().equals(song.getSongTitle()) && s.getMovieName().equals(song.getMovietitle())) {
							Log.i(TAG, "onItemClick: " + s.getSongName() + " " + s.getMovieName());
							index = array.indexOf(s);
							Log.i(TAG, "onItemClick: " + s.getSongName() + " " + s.getMovieName() + " " + index);
						}
					}
					storageUtil.storeAudioIndex(index);
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
		filteredSongList = new ArrayList<>();
		List<songModel> list = new ArrayList<>();
		SortedSet<String> trackNos = new TreeSet<>();

		for (String albums : values.keySet()) {
			HashMap<String, Object> songs = (HashMap<String, Object>) values.get(albums);

			for (String song : songs.keySet()) {
				if (!song.equals("IMAGE")) {
					HashMap<String, Object> oneSong = (HashMap<String, Object>) songs.get(song);
					Log.i(TAG, "prepareSongs: " + albums + " " + song);
					songModel newSong = new songModel(albums, song, oneSong.get("Lyricist").toString(), String.valueOf(oneSong.get("Download")), Integer.parseInt(String.valueOf(oneSong.get("Year"))));
					list.add(newSong);
					trackNos.add(song);
				}

			}
		}
		ArrayList<songModel> dummy = new ArrayList();
		for (String Track : trackNos) {
			for (songModel songNo : list) {
				if (songNo.getSongTitle().equals(Track)) {
					dummy.add(songNo);
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
		filteredSongList = songList;
		songadapter.notifyDataSetChanged();

	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy: on destroy called");
		if (serviceBound) {
			unbindService(serviceConnection);
			serviceBound = false;
			player.setMainCallbacks(null);
		}

		NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
		Log.i(TAG, "onDestroy: am in destory");
		if (dialog != null) {
			dialog.dismiss();
		}
		super.onDestroy();


	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);

			return true;
		} else if (drawer.isDrawerOpen(GravityCompat.END)) {
			drawer.closeDrawer(GravityCompat.END);
			return true;
		} else if (favoritePanel != null &&
				(favoritePanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || favoritePanel.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
			favoritePanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
			return true;

		}else if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			this.moveTaskToBack(true);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void prepareAlbums() {
		albumList.clear();

		_listDataChild = new HashMap<>();

		List<albumModel> list = new ArrayList<>();

		SortedSet<String> trackNos = new TreeSet<>();
		for (String album : values.keySet()) {
			list.add(new albumModel(album, ((HashMap<String, Object>) values.get(album)).size() - 1));
			trackNos.add(album);
		}

		for (String tracks : trackNos) {
			for (albumModel album : list) {
				if (album.getMovietitle().equals(tracks)) {
					HashMap<String, Object> songs = (HashMap<String, Object>) values.get(album.getMovietitle());
					albumsongsList = new ArrayList<>();
					for (String song : songs.keySet()) {
						if (!song.equals("IMAGE")) {
							HashMap<String, Object> oneSong = (HashMap<String, Object>) songs.get(song);
							albumsongs albumsong = new albumsongs(song, String.valueOf(oneSong.get("Track NO")), String.valueOf(oneSong.get("Lyricist")), String.valueOf(oneSong.get("Download")), String.valueOf(oneSong.get("Year")));
							albumsongsList.add(albumsong);
						}
					}
					albumList.add(album);
					_listDataChild.put(album.getMovietitle(), getSortedList(albumsongsList));

				}
			}
		}

		for (String name : _listDataChild.keySet()) {
			Log.i(TAG, "prepareAlbums: " + name + " " + _listDataChild.get(name).size());
		}
		adapter = new ExpandableListAdapter(this, albumList, _listDataChild, MainActivity.this);
		final ExpandableListView albumview = (ExpandableListView) findViewById(R.id.rv2);
		albumview.setAdapter(adapter);
		albumview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
				List<albumModel> model = adapter.get_listDataHeader();
				HashMap<String, List<albumsongs>> map = adapter.get_listDataChild();


				List<albumsongs> songlistalbums = map.get(model.get(i).getMovietitle());
				StorageUtil storageUtil = new StorageUtil(getApplicationContext());

				playlist.clear();
				for (albumsongs songs : songlistalbums) {
					song s = new song(model.get(i).getMovietitle(), songs.getSongName(), songs.getUlr());
					playlist.add(s);
				}
				storageUtil.storeAudio(playlist);
				storageUtil.storeAudioIndex(i1);
				Intent setplaylist = new Intent(MainActivity.Broadcast_NEW_ALBUM);
				sendBroadcast(setplaylist);
				Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_NEW_AUDIO);
				sendBroadcast(broadcastIntent);
				closeDrawer();

				return false;
			}
		});
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



	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
			player = binder.getService();
			player.setMainCallbacks(MainActivity.this);
			update();
			serviceBound = true;

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			serviceBound = false;
		}
	};

	@Override
	protected void onStart() {
		Log.i(TAG, "onStart: on start called");
		super.onStart();
		if (!serviceBound) {
			Intent playerIntent = new Intent(MainActivity.this, MusicService.class);
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
			if (player != null) {
				player.setMainCallbacks(null);
			}
		}
		Log.i("testing", "finished");

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
						if (player.mediaPlayer != null) {
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
				if (player != null) {
					if (player.isShuffleOn()) {
						player.setShuffleOnOff(false);
						shuffle.setImageResource(R.drawable.shuffleoff);
					} else {
						player.setShuffleOnOff(true);
						shuffle.setImageResource(R.drawable.shuffle);

					}
				}
				break;
			}
			case R.id.fav_pop: {

				if (player != null) {
					if (player.mediaPlayer != null) {
						song s = player.getActiveSong();
						if (checkFavoriteItem()) {
							removeFavorite(s);

						} else {
							addFavorite(s);

						}
					}
				}


				break;

			}

		}

	}

	@Override
	public void update() {
		if (player != null && player.mediaPlayer != null) {
			if (player.isPlaying()) {
				if (dialog.isShowing()) {
					dialog.hide();
				}

				seekBar.setMax(player.getDuration());
				totalTime.setText(Helper.durationCalculator(player.getDuration()));
				playpause.setImageResource(R.drawable.pause);
				setLyrics(player.getActiveSong());
				if (checkFavoriteItem()) {
					fav.setImageResource(R.drawable.favon);
				} else {
					fav.setImageResource(R.drawable.heart);
				}
				Log.i("CalledSet", "called set details");

			} else {

				if (player.mediaPlayer != null) {
					seekBar.setMax(player.getDuration());
					playpause.setImageResource(R.drawable.play);
				}


				//playpause.setImageResource(android.R.drawable.ic_media_play);
			}


		}

	}


	@Override
	public void showDialog(String song, String movie) {

		if (dialog != null) {
			Log.i(TAG, "showDialog: loading ");

			dialog.setMessage("Loading " + Helper.FirstLetterCaps(song) + "\nFrom " + Helper.FirstLetterCaps(movie));
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

	@Override
	public boolean onQueryTextSubmit(String s) {
		return false;

	}

	@Override
	public boolean onQueryTextChange(String s) {
		if(songList != null) {
			if (s.length() < 1) {
				songadapter.setFilter(songList);
				return false;
			}
			if (s.isEmpty()) {
				songadapter.setFilter(songList);
				return false;
			}
			filteredSongList = new ArrayList<>();
			filteredSongList = filterSongs(songList, s);

			songadapter.setFilter(filteredSongList);
			return true;
		}
		return false;
	}


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

	public class favPageAdapter extends FragmentPagerAdapter {
		private final List<Fragment> mFragmentList = new ArrayList<>();
		private final List<String> mFragmentTitleList = new ArrayList<>();

		public favPageAdapter(FragmentManager fm) {
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

	public void setImagesList() {

		albumcovers = new HashMap<>();
		for (String movies : values.keySet()) {
			HashMap<String, Object> movie = (HashMap<String, Object>) values.get(movies);
			albumcovers.put(movies, getBitmap(String.valueOf(movie.get("IMAGE"))));

		}
		albumbitmaps.setCovers(albumcovers);

	}

	public Bitmap getImageBitmap(String movie) {
		return albumcovers.get(movie);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		int[] location = new int[2];
		ImageButton button = (ImageButton) findViewById(R.id.fav_pop);

		// Get the x, y location and store it in the location[] array
		// location[0] = x, location[1] = y.
		button.getLocationOnScreen(location);

		//Initialize the Point with x, and y positions
		p = new Point();
		p.x = location[0];
		p.y = location[1];
	}

	public boolean checkFavoriteItem() {
		if (player != null) {
			if (player.mediaPlayer != null) {
				song s = player.getActiveSong();
				favoritesMab = favFragment.getFavoritesMab();
				if (favoritesMab != null) {
					Log.i(TAG, "checkFavoriteItem: " + s.getMovieName() + " " + s.getSongName());
					Log.i(TAG, "checkFavoriteItem: " + favoritesMab);

					if (s != null) {
						if (favoritesMab.containsKey(s.getMovieName())) {
							for (String name : favoritesMab.get(s.getMovieName())) {
								if (name.equals(s.getSongName())) {
									return true;
								}
							}
						}
					}

				}
			}
		}


		return false;

	}

	public void addFavorite(song s) {
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		HashMap<String, Object> map = new HashMap<>();
		map.put(s.getSongName(), s.getSongName());
		DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
		userRef.child(user.getUid()).child("Fav Songs").child(s.getMovieName()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if (task.isSuccessful()) {
					Toast.makeText(MainActivity.this, "Successfully Added to Favorites", Toast.LENGTH_SHORT).show();
					fav.setImageResource(R.drawable.favon);
				}
			}
		});


	}

	public void removeFavorite(song s) {

		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

		DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
		userRef.child(user.getUid()).child("Fav Songs").child(s.getMovieName()).child(s.getSongName()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if (task.isSuccessful()) {
					Toast.makeText(MainActivity.this, "Successfully removed from Favorites", Toast.LENGTH_SHORT).show();
					fav.setImageResource(R.drawable.heart);
				}
			}
		});

	}

	public List<songModel> filterSongs(List<songModel> listsongs, String query) {
		query = query.toLowerCase().trim();
		final List<songModel> filteralbumlist = new ArrayList<>();
		for (songModel songs : listsongs) {
			final String text1 = songs.getSongTitle().toLowerCase();
			final String text2 = songs.getMovietitle().toLowerCase();
			final String text3 = songs.getLyricistName().toLowerCase();
			final String text4 = String.valueOf(songs.getYear());
			if (text1.contains(query) || text2.contains(query) || text3.contains(query) || text4.contains(query)) {
				filteralbumlist.add(songs);
			}
		}
		return filteralbumlist;
	}

	private void logUser() {
		// TODO: Use the current user's information
		// You can call any combination of these three methods
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user != null) {
			Crashlytics.setUserIdentifier(user.getUid());
			Crashlytics.setUserEmail(user.getEmail());
			Crashlytics.setUserName(user.getDisplayName());
		}
	}


	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */


}
