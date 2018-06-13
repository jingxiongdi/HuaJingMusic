package com.jingxiongdi.huajingmusic;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jingxiongdi.huajingmusic.bean.Song;
import com.jingxiongdi.huajingmusic.util.AudioUtils;
import com.jingxiongdi.huajingmusic.util.DBHelper;
import com.jingxiongdi.huajingmusic.util.L;
import com.jingxiongdi.huajingmusic.util.MusicConstants;
import com.jingxiongdi.huajingmusic.util.SPUtils;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class InitActivity extends BaseActivity  implements EasyPermissions.PermissionCallbacks{
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
        L.d("MusicAppliction InitActivity ");
        if(MusicAppliction.getInstance().isbMainExist()){
            handler.sendEmptyMessage(0);
        }
        if (Build.VERSION.SDK_INT >= 23) {

            if (EasyPermissions.hasPermissions(InitActivity.this, mPermissionList)) {
            } else {
                EasyPermissions.requestPermissions(this, "拍照需要的权限", 1, mPermissionList);
            }
        } else {
        }

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
                if(songs!=null&&songs.size()!=0&&(songs1 == null || songs1.size() != songs.size())){
                    L.d("dropAndRecreateTable ");
                    ArrayList<Song> songDataBase = new ArrayList<>();
                    //过滤小于60s的音乐
                    for(Song song:songs){
                   //     if(song.getDuration() > 60000)
                        {
                            songDataBase.add(song);
                        }
                    }
                    if(songDataBase.size() > 0){
                        DBHelper.getInstance(InitActivity.this).dropAndRecreateTable();
                        DBHelper.getInstance(InitActivity.this).addSongList(songDataBase);
                    }
                }

                handler.sendEmptyMessageDelayed(0,500);
            }
        }).start();
    }
    private String[] mPermissionList = new String[]{Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.i("jxd", "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.i("jxd", "onPermissionsDenied:" + requestCode + ":" + perms.size());
    }
}
