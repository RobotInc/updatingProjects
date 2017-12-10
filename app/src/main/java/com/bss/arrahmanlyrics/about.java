package com.bss.arrahmanlyrics;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link about.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link about#newInstance} factory method to
 * create an instance of this fragment.
 */
public class about extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	private OnFragmentInteractionListener mListener;

	public about() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment about.
	 */
	// TODO: Rename and change types and number of parameters
	public static about newInstance(String param1, String param2) {
		about fragment = new about();
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
		//View view = inflater.inflate(R.layout.fragment_about,container,false);
		String des = "We all know how magical AR Rahman's tunes are! And it's pleasure to sing along with the help of lyrics. So we made this musical app as a tribute to AR Rahman. Explore and feel the Music";
		Element versionElement = new Element();
		versionElement.setTitle("Version 2.3");
		Element telegram = new Element();
		telegram.setTitle("Chat with us in Telegram");
		telegram.setValue("https://t.me/beyonity");
		telegram.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = "https://t.me/beyonity";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
		Element Facebook = new Element();
		Facebook.setTitle("Like us on Facebook");
		Facebook.setValue("https://t.me/beyonity");
		Facebook.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = "https://www.facebook.com/beyonityss";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
		Facebook.setIconDrawable(R.drawable.about_icon_facebook);
		Element copyRights = new Element();
		copyRights.setTitle("© BSS 2017");
		telegram.setIconDrawable(R.drawable.telegram);

		View aboutPage = new AboutPage(getContext())
				.isRTL(false)
				.setImage(R.mipmap.ic_launcher)
				.setDescription(des)
				.addItem(versionElement)
				.addGroup("You can easily Reach Us by")
				.addItem(Facebook)
				.addItem(telegram)
				.addItem(copyRights)
				.create();



		return aboutPage;
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
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
}