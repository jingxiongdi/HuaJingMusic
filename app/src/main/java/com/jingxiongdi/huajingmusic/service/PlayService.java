package com.jingxiongdi.huajingmusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import com.jingxiongdi.huajingmusic.util.L;

public class PlayService extends Service {
    private MediaPlayer mediaPlayer = null;
    private static final String TAG = PlayService.class.getSimpleName();

    public PlayService() {
        L.d(TAG,"PlayService");
       // mediaPlayer = new MediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        L.d(TAG,"PlayService onBind");
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.d(TAG,"PlayService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        L.d(TAG,"PlayService onCreate");
    }


    public class MyBinder extends Binder {
        /** * 获取Service的方法 * @return 返回PlayerService */
        public PlayService getService(){
            return PlayService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        L.d(TAG,"PlayService onUnbind");
        return super.onUnbind(intent);
    }

    public void stopPlayService(){
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.d(TAG,"PlayService onDestroy");
    }
}
