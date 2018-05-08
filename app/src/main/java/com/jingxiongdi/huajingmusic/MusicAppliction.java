package com.jingxiongdi.huajingmusic;

import android.app.Application;

import com.jingxiongdi.huajingmusic.util.L;

/**
 * Created by jingxiongdi on 2018/5/5.
 */

public class MusicAppliction extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        L.d("MusicAppliction onCreate");
    }
}
