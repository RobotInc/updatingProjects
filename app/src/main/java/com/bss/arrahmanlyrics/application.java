package com.bss.arrahmanlyrics;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.bss.arrahmanlyrics.utils.mediaCache;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by mohan on 5/21/17.
 */

public class application extends android.app.Application {
    private static Context mContext;
    private HttpProxyCacheServer proxy;

    @Override
    public void onCreate() {
        super.onCreate();
        // Required initialization logic here!
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mContext = this;


    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public static Context getContext()
    {
        return mContext;
    }

    public static HttpProxyCacheServer getProxy(Context context) {
        application app = (application) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this)
                .cacheDirectory(mediaCache.getVideoCacheDir(this))
                .build();
    }


}