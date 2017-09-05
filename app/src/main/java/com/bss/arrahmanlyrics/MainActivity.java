package com.bss.arrahmanlyrics;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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

	}
}
