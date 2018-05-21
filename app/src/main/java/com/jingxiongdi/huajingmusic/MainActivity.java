package com.jingxiongdi.huajingmusic;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.jingxiongdi.huajingmusic.adapter.MainAdapter;
import com.jingxiongdi.huajingmusic.bean.Song;
import com.jingxiongdi.huajingmusic.service.PlayService;
import com.jingxiongdi.huajingmusic.util.DBHelper;
import com.jingxiongdi.huajingmusic.util.L;
import com.jingxiongdi.huajingmusic.util.ToastUtil;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    private ExpandableListView expandableListView = null;
    private ArrayList<String> songListString = new ArrayList<>();
    private ArrayList<ArrayList<Song>> songList = new ArrayList<>();
    private MainAdapter mainAdapter = null;
    private MediaPlayer   player  =   null;
    private PlayService playService = null;
    private Intent intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setViews();

        initData();
    }

    private void initData() {
        songListString.add("全部歌曲");
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Song> songAll = DBHelper.getInstance(MainActivity.this).queryAllSong();
                songList = new ArrayList<>();
                songList.add(songAll);
                if(songAll == null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showShort(MainActivity.this,"没有获取到音乐文件");
                        }
                    });
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainAdapter = new MainAdapter(MainActivity.this,songList,songListString);
                        expandableListView.setAdapter(mainAdapter);
                    }
                });

            }
        }).start();
      //  songList =(ArrayList<ArrayList<Song>>) SPUtils.get(MainActivity.this, MusicConstants.ALL_MUSIC_IN_PHONE,null);
        playService = new PlayService();
        intent = new Intent(MainActivity.this,PlayService.class);
        //startService(intent);
        bindService(intent, serviceConnection,  Context.BIND_AUTO_CREATE);

    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            L.d("PlayService","PlayService onServiceConnected");
            PlayService.MyBinder binder = (PlayService.MyBinder)service;
            binder.getService();// PlayService
        }
        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            L.d("PlayService","PlayService onServiceDisconnected");
        }
    };


    private void setViews() {
        Toolbar toolbar = findViewById(R.id.id_toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               showExitDialog();
            }
        });
        expandableListView = findViewById(R.id.expanded_list);
        expandableListView.setOnGroupClickListener(listOnitemClick);
        expandableListView.setOnChildClickListener(childClick);
    }

    ExpandableListView.OnChildClickListener childClick = new ExpandableListView.OnChildClickListener() {

        @Override
        public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {

            L.d("childClick : i : "+i+" i1 : "+i1);
            //play
            try{
                if(player != null){
                    player.stop();
                    player.reset();
                    player = null;
                }

                player = new MediaPlayer();
                String  path   = songList.get(i).get(i1).getFileUrl();
                player.setDataSource(path);
                player.prepare();
                player.start();
            }catch (Exception e){
                e.printStackTrace();
            }

            mainAdapter.refreshAdapter(i1);
            expandableListView.setSelectedChild(0,i1,true);



            return false;
        }
    };

    ExpandableListView.OnGroupClickListener listOnitemClick = new ExpandableListView.OnGroupClickListener() {

        @Override
        public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
            L.d("listOnitemClick : "+i);
            return false;
        }
    };

    private void showExitDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("提示")
                .setMessage("确认退出花景音乐吗？")
                .setPositiveButton( "是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.finish();
                        System.exit(0);
                    }
                })
                .setNegativeButton("否", null)
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if(keyCode == KeyEvent.KEYCODE_BACK){
                showExitDialog();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        Log.d("PlayService","onPause");
        unbindService(serviceConnection);
        //stopService(intent);
        playService.stopPlayService();
        Log.d("PlayService","onPause222");
        if(player.isPlaying()){
            player.stop();
        }


        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(player.isPlaying()){
            player.stop();
        }
        super.onDestroy();
    }
}
