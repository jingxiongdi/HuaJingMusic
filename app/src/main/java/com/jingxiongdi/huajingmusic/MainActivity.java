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
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

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

    private PlayService playService = null;
    private Intent intent = null;
    private ImageView preBtn = null;
    private ImageView playOrPauseBtn = null;
    private ImageView nextBtn = null;
    private int curPlayPostion = 0;
    private int curPlayListPostion = 0;
    private TextView curTime = null;
    private TextView allTime = null;
    private AppCompatSeekBar seekBar = null;

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
        startService(intent);

    }


    private void setViews() {
        curTime = findViewById(R.id.cur_time);
        allTime = findViewById(R.id.all_the_time);
        seekBar = findViewById(R.id.seekbar);

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

        preBtn =  findViewById(R.id.back_left);
        playOrPauseBtn = findViewById(R.id.play_or_pause);
        nextBtn = findViewById(R.id.next_right);

        preBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(curPlayPostion == 0){
                    curPlayPostion = songList.size() - 1;
                }
                curPlayPostion--;
                playMusic();
            }
        });

        playOrPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playOrPause();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(curPlayPostion == songList.size() - 1){
                    curPlayPostion = 0;
                }
                curPlayPostion++;
                playMusic();
            }
        });
    }

    ExpandableListView.OnChildClickListener childClick = new ExpandableListView.OnChildClickListener() {

        @Override
        public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {

            L.d("childClick : i : "+i+" i1 : "+i1);
            //play
            curPlayListPostion = i;
            curPlayPostion = i1;
            playMusic();
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

    private void playMusic(){
        long time = songList.get(curPlayListPostion).get(curPlayPostion).getDuration()/1000;
        String min = "";
        String s = "";
        if(time/60 <10) {
            min = "0"+time/60;
        }else {
            min = ""+time/60;
        }

        if(time%60<10){
            s = "0"+time%60;
        }
        else {
            s = ""+time%60;
        }
        allTime.setText(min+":"+s+"");
        playOrPauseBtn.setBackgroundResource(R.mipmap.player_pause_bubble);
        String  path   = songList.get(curPlayListPostion).get(curPlayPostion).getFileUrl();
        playService.playMusic(path);
        mainAdapter.refreshAdapter(curPlayPostion);
        expandableListView.setSelectedChild(0,curPlayPostion,true);
    }

    private void playOrPause(){
        if(playService.isPlaying()){
            playService.pausePlay();
            playOrPauseBtn.setBackgroundResource(R.mipmap.player_play_bubble);
        }else {
            playService.startPlay();
            playOrPauseBtn.setBackgroundResource(R.mipmap.player_pause_bubble);
        }
    }

    private void showExitDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("提示")
                .setMessage("确认退出花景音乐吗？")
                .setPositiveButton( "是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        playService.stopPlayMuic();
                        stopService(intent);
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

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
