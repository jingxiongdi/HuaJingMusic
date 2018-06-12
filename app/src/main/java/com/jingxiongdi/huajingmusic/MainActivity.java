package com.jingxiongdi.huajingmusic;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jingxiongdi.huajingmusic.adapter.MainAdapter;
import com.jingxiongdi.huajingmusic.bean.Song;
import com.jingxiongdi.huajingmusic.service.PlayService;
import com.jingxiongdi.huajingmusic.util.DBHelper;
import com.jingxiongdi.huajingmusic.util.L;
import com.jingxiongdi.huajingmusic.util.MusicConstants;
import com.jingxiongdi.huajingmusic.util.SPUtils;
import com.jingxiongdi.huajingmusic.util.ToastUtil;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

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
    private ImageView playModelBtn = null;
    private int curPlayPostion = 0;
    private int curPlayListPostion = 0;
    private TextView curTime = null;
    private TextView allTime = null;
    private AppCompatSeekBar seekBar = null;
    private static final int EXPAND_LIST = 0;
    private AtomicInteger nowTime = new AtomicInteger(0);
    private int curPlaySongDuration = 0;
    private int curPlayMode = 0;
    private Thread updateProgressThread = new Thread(){
        @Override
        public void run() {
            super.run();
            while (nowTime.intValue() < curPlaySongDuration){
                L.d("jxdddd : "+nowTime.intValue());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                nowTime.getAndAdd(1);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setProgress(nowTime.intValue());
                        curTime.setText(setTimeString(nowTime.intValue()));
                    }
                });
            }
        }
    };
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case EXPAND_LIST:
                    expandableListView.expandGroup(0,true);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

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

    private void setPlayMode(int show){
        switch (curPlayMode) {
            case 0:
                playModelBtn.setBackgroundResource(R.mipmap.all_cycle);
                if(show == 1){
                    ToastUtil.showShort(MainActivity.this,"列表循环");
                }
                break;
            case 1:
                playModelBtn.setBackgroundResource(R.mipmap.single_cycle);
                if(show == 1){
                    ToastUtil.showShort(MainActivity.this,"单曲循环");
                }
                break;

            case 2:
                playModelBtn.setBackgroundResource(R.mipmap.random_cycle);
                if(show == 1){
                    ToastUtil.showShort(MainActivity.this,"随机循环");
                }
                break;
            default:
                playModelBtn.setBackgroundResource(R.mipmap.all_cycle);
                break;
        }


    }

    private void setViews() {
        curTime = findViewById(R.id.cur_time);
        allTime = findViewById(R.id.all_the_time);
        playModelBtn = findViewById(R.id.play_model);
        curPlayMode =(int) SPUtils.get(MainActivity.this,MusicConstants.CUR_PLAY_MODE,0);
        setPlayMode(0);
        playModelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(curPlayMode < 2) {
                    curPlayMode++;
                } else if(curPlayMode >= 2){
                    curPlayMode = 0;
                }
                setPlayMode(1);
                SPUtils.put(MainActivity.this,MusicConstants.CUR_PLAY_MODE,curPlayMode);

            }
        });

        seekBar = findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
              //  L.d("jxdzz "+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
              //  L.d("jxdzz onStartTrackingTouch "+seekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
              //  L.d("jxdzz onStopTrackingTouch "+seekBar.getProgress());
                nowTime.set(seekBar.getProgress());
                playService.setPlaySection(seekBar.getProgress());
            }
        });
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
                nowTime.set(0);
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
                nowTime.set(0);
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
            nowTime.set(0);
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

    private String setTimeString(int time){
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

        return min+":"+s+"";
    }

    private void playMusic(){
        long time = songList.get(curPlayListPostion).get(curPlayPostion).getDuration()/1000;
        curPlaySongDuration = (int) time;

        allTime.setText(setTimeString(curPlaySongDuration));
        playOrPauseBtn.setBackgroundResource(R.mipmap.player_pause_bubble);
        String  path   = songList.get(curPlayListPostion).get(curPlayPostion).getFileUrl();
        playService.playMusic(path);
        mainAdapter.refreshAdapter(curPlayPostion);
        expandableListView.setSelectedChild(0,curPlayPostion,true);

        setSeekBarProgress(curPlaySongDuration);
    }

    private void setSeekBarProgress(int time){
        seekBar.setMax(time);
        seekBar.setProgress(0);
        if(!updateProgressThread.isAlive()){
            updateProgressThread.start();
        }
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
    protected void onResume() {
        handler.sendEmptyMessageDelayed(EXPAND_LIST,500);
        super.onResume();
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
