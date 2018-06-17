package com.jingxiongdi.huajingmusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.jingxiongdi.huajingmusic.inteface.PlayControl;
import com.jingxiongdi.huajingmusic.util.L;

public class PlayService extends Service {
    private MediaPlayer mediaPlayer = null;
    private static final String TAG = PlayService.class.getSimpleName();
    private MediaPlayer   player  =   null;
    private PlayControl playControl = null;
    public PlayService() {
        /**
         * 这个方法不能省略，否则会报没有默认的构造函数
         */
    }

    public PlayService(PlayControl p) {
        L.d(TAG,"PlayService");
       // mediaPlayer = new MediaPlayer();
        playControl = p;
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

    public void playMusic(String path) {
        try{
            if(player != null){
                player.stop();
                player.reset();
                player = null;
            }

            player = new MediaPlayer();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    playControl.playCompleteNext();
                }
            });
            player.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                    L.d("oninfojxd","i : "+i+"  i1 : "+i1);
                    return false;
                }
            });
            player.setDataSource(path);
            player.prepare();
            player.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stopPlayMuic(){
        if(player.isPlaying()){
            player.stop();
        }
    }

    public boolean isPlaying() {
        if(player!=null){
            return player.isPlaying();
        }
        return false;
    }

    public void setPlaySection(int time){
        if(player!=null){
            player.seekTo(time*1000);
        }
    }

    public void pausePlay() {
        if(player!=null&&player.isPlaying()){
            L.d("pausePlay");
             player.pause();
        }
    }

    public void startPlay() {
        if(player!=null&&!player.isPlaying()){
            L.d("startPlay");
            player.start();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player!=null){
            player.release();
            player = null;
        }
        L.d(TAG,"PlayService onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
