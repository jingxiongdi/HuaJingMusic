package com.jingxiongdi.huajingmusic;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.jingxiongdi.huajingmusic.util.L;

import java.util.List;

/**
 * Created by jingxiongdi on 2018/5/5.
 */

public class MusicAppliction extends Application {
    private boolean bMainExist = false;
    private static MusicAppliction instance ;

    public static MusicAppliction getInstance(){
        if (instance == null){
            instance = new MusicAppliction();
        }
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        L.d("MusicAppliction onCreate ");
    }


    public boolean isbMainExist() {
        return bMainExist;
    }

    public void setbMainExist(boolean bMainExist) {
        this.bMainExist = bMainExist;
    }
}
