package com.jingxiongdi.huajingmusic;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jingxiongdi.huajingmusic.bean.Song;
import com.jingxiongdi.huajingmusic.util.AudioUtils;
import com.jingxiongdi.huajingmusic.util.DBHelper;
import com.jingxiongdi.huajingmusic.util.L;
import com.jingxiongdi.huajingmusic.util.MusicConstants;
import com.jingxiongdi.huajingmusic.util.SPUtils;

import java.util.ArrayList;

public class InitActivity extends BaseActivity {
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            InitActivity.this.finish();
            startActivity(new Intent(InitActivity.this,MainActivity.class));
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);


        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Song> songs =  AudioUtils.getAllSongs(InitActivity.this);
                L.d("songs : "+songs.size());
//                if(SPUtils.contains(InitActivity.this, MusicConstants.ALL_MUSIC_IN_PHONE)){
//                    SPUtils.remove(InitActivity.this,MusicConstants.ALL_MUSIC_IN_PHONE);
//                }
//                SPUtils.put(InitActivity.this,MusicConstants.ALL_MUSIC_IN_PHONE,songs);
                ArrayList<Song> songs1 =  DBHelper.getInstance(InitActivity.this).queryAllSong();
                if(songs1 == null || songs1.size() != songs.size()){
                    L.d("dropAndRecreateTable ");
                    DBHelper.getInstance(InitActivity.this).dropAndRecreateTable();
                    DBHelper.getInstance(InitActivity.this).addSongList(songs);
                }

                handler.sendEmptyMessageDelayed(0,500);
            }
        }).start();
    }
}
