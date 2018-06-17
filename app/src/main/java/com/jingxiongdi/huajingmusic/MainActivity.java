package com.jingxiongdi.huajingmusic;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
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
import com.jingxiongdi.huajingmusic.inteface.PlayControl;
import com.jingxiongdi.huajingmusic.service.PlayService;
import com.jingxiongdi.huajingmusic.util.CommonUtil;
import com.jingxiongdi.huajingmusic.util.DBHelper;
import com.jingxiongdi.huajingmusic.util.L;
import com.jingxiongdi.huajingmusic.util.MusicConstants;
import com.jingxiongdi.huajingmusic.util.SPUtils;
import com.jingxiongdi.huajingmusic.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import pub.devrel.easypermissions.EasyPermissions;

import static com.jingxiongdi.huajingmusic.util.DBHelper.UserSchema.title;

public class MainActivity extends BaseActivity implements PlayControl{
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
    private Thread updateProgressThread = null;
    private boolean isPlayerPause = false;
    private boolean firstSongRFirstPlay = true;
    private ArrayList<Song> songAll = null;
    private static final int LOVE_PUSH = 1;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case EXPAND_LIST:
                    expandableListView.expandGroup(0,true);
                    break;
                case LOVE_PUSH:
                    initNotification();
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

        handler.sendEmptyMessageDelayed(LOVE_PUSH,5000);

    }

    private void initNotification() {
        //发送通知
        NotificationCompat.Builder notifyBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        //设置可以显示多行文本
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("ccc"))
                        .setContentTitle("接住啦，这是一个爱心推送！")
                        .setContentText("锅锅爱你，每天为你打call！")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        //设置大图标
                        .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_launcher_background))
                        // 点击消失
                        .setAutoCancel(true)
                        // 设置该通知优先级
                        .setPriority(Notification.PRIORITY_MAX)
                        .setTicker("悬浮通知")
                        // 通知首次出现在通知栏，带上升动画效果的
                        .setWhen(System.currentTimeMillis())
                        // 通知产生的时间，会在通知信息里显示
                        // 向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
                        .setDefaults( Notification.DEFAULT_VIBRATE | Notification.DEFAULT_ALL | Notification.DEFAULT_SOUND );
        NotificationManager mNotifyMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = notifyBuilder.build();
        mNotifyMgr.notify( 0, notification);
    }

    private void startANewThread(){
//        if(updateProgressThread !=null) {
//            updateProgressThread = null;
//        }

        updateProgressThread = new Thread(){
            @Override
            public void run() {
                super.run();
                while (true){
                    if(!isPlayerPause && (nowTime.intValue() < curPlaySongDuration)){
                       // L.d("jxdddd : "+nowTime.intValue());
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
            }
        };

        updateProgressThread.start();
    }

    private void initData() {
        songListString.add("全部歌曲");
        new Thread(new Runnable() {
            @Override
            public void run() {
                songAll = DBHelper.getInstance(MainActivity.this).queryAllSong();
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
        playService = new PlayService(this);
        intent = new Intent(MainActivity.this,PlayService.class);
        startService(intent);

    }

    private void setPlayMode(int show){
        switch (curPlayMode) {
            case 0:
                playModelBtn.setBackgroundResource(R.mipmap.all_cycle);
                if(show == 1){
                    songList .clear();
                    songList.add(songAll);
                    mainAdapter.refreshData(curPlayPostion,songList);
                    ToastUtil.showShort(MainActivity.this,"列表循环");
                }
                break;
            case 1:
                playModelBtn.setBackgroundResource(R.mipmap.single_cycle);
                if(show == 1){
                    songList .clear();
                    songList.add(songAll);
                    mainAdapter.refreshData(curPlayPostion,songList);
                    ToastUtil.showShort(MainActivity.this,"单曲循环");
                }
                break;

            case 2:
                playModelBtn.setBackgroundResource(R.mipmap.random_cycle);
                if(show == 1){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<Song> mySongList = songList.get(0);
                            songList.clear();
                            songList.add(CommonUtil.getRomSongList(mySongList));
                            L.d("songList zzz "+songList.size());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mainAdapter.refreshData(curPlayPostion,songList);
                                    ToastUtil.showShort(MainActivity.this,"随机循环");
                                }
                            });
                        }
                    }).start();


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
                L.d("preBtn curPlayPostionzzz "+curPlayPostion);
                if(curPlayPostion == 0){
                    curPlayPostion = songList.get(0).size() - 1;
                }else{
                    curPlayPostion--;
                }
                L.d("preBtn curPlayPostionzzzzzaa "+curPlayPostion);
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
                L.d("nextBtn curPlayPostion "+curPlayPostion+"  songList.size() : "+songList.get(0).size());
                playNext();
            }
        });
    }

    private void playNext(){
        if(curPlayPostion == songList.get(0).size() - 1){
            curPlayPostion = 0;
        }else {
            curPlayPostion++;
        }
        L.d("nextBtn curPlayPostionzzz "+curPlayPostion);
        nowTime.set(0);
        playMusic();
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
        L.d("curPlayPostion : "+curPlayPostion);
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
        if(updateProgressThread==null){
            startANewThread();
        }

    }
    private int pauseSeek = 0;
    private void playOrPause(){
        if(playService.isPlaying()){
            isPlayerPause = true;
            playService.pausePlay();
            pauseSeek = seekBar.getProgress();
            playOrPauseBtn.setBackgroundResource(R.mipmap.player_play_bubble);
        }else {
            if(curPlayPostion==0&&firstSongRFirstPlay){
                /**
                 * 解决第一次进入app，点击播放暂停按钮，不播放的问题
                 */
                firstSongRFirstPlay = false;
                playMusic();
                return;
            }
            isPlayerPause = false;
            playService.startPlay();
            playService.setPlaySection(pauseSeek);
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
        MusicAppliction.getInstance().setbMainExist(true);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("PlayService","onPause");

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void playCompleteNext() {
        L.d("playCompleteNext");
        if(curPlayMode == 1){
            nowTime.set(0);
            setSeekBarProgress(curPlaySongDuration);
            playService.startPlay();
        }else{
            /**
             * 代码模拟点击事件
             *
             */
            nextBtn.performClick();
        }
    }
}
