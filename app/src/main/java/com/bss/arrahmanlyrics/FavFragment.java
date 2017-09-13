package com.bss.arrahmanlyrics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bss.arrahmanlyrics.adapter.FavoriteSongAdapter;
import com.bss.arrahmanlyrics.models.song;
import com.bss.arrahmanlyrics.models.songModel;
import com.bss.arrahmanlyrics.utils.DividerItemDecoration;
import com.bss.arrahmanlyrics.utils.RecyclerItemClickListener;
import com.bss.arrahmanlyrics.utils.StorageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FavFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FavFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    List<songModel> favoriteSongList;
    RecyclerView rv3;
    FavoriteSongAdapter adapter;
    HashMap<String,Object> values = new HashMap<>();
    HashMap<String,ArrayList<String>> favoritesMab = new HashMap<>();
    ArrayList<song> playlist = new ArrayList<>();
    public FavFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavFragment newInstance(String param1, String param2) {
        FavFragment fragment = new FavFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fav,container,false);
        values = ((MainActivity)getActivity()).values;
        rv3 = (RecyclerView) view.findViewById(R.id.rv3);
        favoriteSongList = new ArrayList<>();
        adapter = new FavoriteSongAdapter(getContext(),favoriteSongList,((MainActivity)getActivity()));
        rv3.setAdapter(adapter);
        rv3.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        rv3.setItemAnimator(new DefaultItemAnimator());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        rv3.setLayoutManager(layoutManager);
        rv3.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                songModel song = adapter.getItem(position);
                StorageUtil storageUtil = new StorageUtil(getContext());
                playlist.clear();
                for (songModel songs : favoriteSongList) {
                    song s = new song(songs.getMovietitle(), songs.getSongTitle(), songs.getUlr());
                    playlist.add(s);
                }
                storageUtil.storeAudio(playlist);
                storageUtil.storeAudioIndex(position);
                Intent setplaylist = new Intent(MainActivity.Broadcast_NEW_ALBUM);
                getActivity().sendBroadcast(setplaylist);
                Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_NEW_AUDIO);
                getActivity().sendBroadcast(broadcastIntent);


            }


        }));
        prepareFavorite();
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void prepareFavorite() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        favoritesMab = new HashMap<>();
        favoriteSongList.clear();
        DatabaseReference favref = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child("Fav Songs");
        favref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HashMap<String, Object> movies = (HashMap<String, Object>) dataSnapshot.getValue();
                    Log.i("Fav", "onDataChange: on data change");
                    ArrayList<String> Favsonglist = new ArrayList<String>();
                    favoritesMab.clear();
                    if (movies != null) {
                        for (String movie : movies.keySet()) {

                            HashMap<String, Object> songs = (HashMap<String, Object>) movies.get(movie);

                            Favsonglist.clear();
                            for (String song : songs.keySet()) {

                                Favsonglist.add(song);
                            }

                            favoritesMab.put(movie, (ArrayList<String>) Favsonglist.clone());
                        }
                        prepareFavList(favoritesMab);
                    }
                }else {
                    if(favoriteSongList != null && favoritesMab !=null && adapter!= null) {
                        favoriteSongList.clear();
                        favoritesMab.clear();
                        adapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @SuppressLint("ResourceType")
    private void prepareFavList(HashMap<String, ArrayList<String>> favoritesMab) {
        favoriteSongList.clear();
        if (favoritesMab != null) {
            for (String movies : favoritesMab.keySet()) {

                HashMap<String, Object> movieMap = (HashMap<String, Object>) values.get(movies);
                ArrayList<String> favoriteSongs = favoritesMab.get(movies);

                if (movieMap == null) {
                    return;
                }
                for (String song : favoriteSongs) {
                    HashMap<String, Object> songMap = (HashMap<String, Object>) movieMap.get(song);

                    songModel songModel = new songModel(movies, song, String.valueOf(songMap.get("Lyricist")), String.valueOf(songMap.get("Download")), Integer.parseInt(String.valueOf(songMap.get("Year"))));
                    favoriteSongList.add(songModel);
                }
            }
        }

        adapter.notifyDataSetChanged();


    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public HashMap<String, ArrayList<String>> getFavoritesMab(){
        return favoritesMab;
    }
}
