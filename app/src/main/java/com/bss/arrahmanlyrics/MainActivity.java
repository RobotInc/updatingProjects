package com.bss.arrahmanlyrics;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bss.arrahmanlyrics.adapter.SongAdapter;
import com.bss.arrahmanlyrics.adapter.albumAdapter;
import com.bss.arrahmanlyrics.adapter.albumAdapter2;
import com.bss.arrahmanlyrics.models.Tracks;
import com.bss.arrahmanlyrics.models.albumModel;
import com.bss.arrahmanlyrics.models.albumsongs;
import com.bss.arrahmanlyrics.models.songModel;
import com.bss.arrahmanlyrics.utils.DividerItemDecoration;
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

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

	private static final String TAG = "MainActivity";
	//firebase
	FirebaseAuth auth;
	GoogleApiClient mGoogleApiClient;
	private FirebaseAuth mFirebaseAuth;
	FirebaseUser user;
	int firebaseSignRequestCode = 5;
	DatabaseReference reference;

	ProgressDialog dialog;

	//list
	RecyclerView rv1;
	RecyclerView rv2;
	albumAdapter albumadapter;
	SongAdapter songadapter;
	List<albumModel> albumList;
	List<songModel> songList;
	List<Tracks> trackList;
	List<albumsongs> albumsongsList;

	//database values
	public HashMap<String, Object> values = new HashMap<>();


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

		if(user == null){
			signIn();
		}else {
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

	private void initUI(){
		dialog = new ProgressDialog(this);
		dialog.setMessage("Loading Database");
		dialog.show();
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

		songList = new ArrayList<>();
		songadapter = new SongAdapter(MainActivity.this,songList);
		rv1 = (RecyclerView) findViewById(R.id.rv1);
		rv1.setAdapter(songadapter);
		rv1.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
		rv1.setItemAnimator(new DefaultItemAnimator());
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		rv1.setLayoutManager(layoutManager);
		prepareSongs();

	}

	private void prepareSongs() {
		songList.clear();
		List<songModel> list = new ArrayList<>();
		SortedSet<String> trackNos = new TreeSet<>();
		for (String albums : values.keySet()) {
			HashMap<String, Object> songs = (HashMap<String, Object>) values.get(albums);
			byte[] image = getImage(String.valueOf(songs.get("IMAGE")));
			for (String song : songs.keySet()) {
				if (!song.equals("IMAGE")) {
					HashMap<String, Object> oneSong = (HashMap<String, Object>) songs.get(song);
					songModel newSong = new songModel(albums, song, oneSong.get("Lyricist").toString(), image, String.valueOf(oneSong.get("Download")));
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
			list.add(new albumModel(album, ((HashMap<String, Object>) values.get(album)).size() - 1,
					getImage(String.valueOf(((HashMap<String, Object>) values.get(album)).get("IMAGE"))), getBitmap(String.valueOf(((HashMap<String, Object>) values.get(album)).get("IMAGE")))));
			trackNos.add(album);
		}
		trackList = new ArrayList<>();
		for(String tracks : trackNos){
			for(albumModel album : list){
				if(album.getMovietitle().equals(tracks)){
					HashMap<String,Object> songs = (HashMap<String, Object>) values.get(album.getMovietitle());
					albumsongsList = new ArrayList<>();
					for(String song : songs.keySet()){
						if(!song.equals("IMAGE")) {
							HashMap<String, Object> oneSong = (HashMap<String, Object>) songs.get(song);
							albumsongs albumsong = new albumsongs(song, String.valueOf(oneSong.get("Track NO")), String.valueOf(oneSong.get("Lyricist")));
							albumsongsList.add(albumsong);
						}
					}
					Tracks tracks1 = new Tracks(album.getMovietitle(),getSortedList(albumsongsList));

					trackList.add(tracks1);
					albumList.add(album);
				}
			}
		}

		albumadapter = new albumAdapter(trackList,MainActivity.this,albumList);
		rv2 = (RecyclerView) findViewById(R.id.rv2);
		rv2.setAdapter(albumadapter);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		rv2.setLayoutManager(layoutManager);
		albumadapter.notifyDataSetChanged();


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
		return bitmap;
	}

	public List<albumsongs> getSortedList(List<albumsongs> list){
		SortedSet<Integer> trackNos = new TreeSet<>();
		List<albumsongs> sorted = new ArrayList<>();
		for(albumsongs songs : list){
			trackNos.add(Integer.parseInt(songs.getTrackNo()));
		}

		for(int numbers : trackNos){
			for(albumsongs songs : list){
				if(numbers == Integer.parseInt(songs.getTrackNo())){
					sorted.add(songs);
				}
			}
		}

		return sorted;
	}
}