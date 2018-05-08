package com.jingxiongdi.huajingmusic;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;

import com.jingxiongdi.huajingmusic.adapter.MainAdapter;
import com.jingxiongdi.huajingmusic.bean.Song;
import com.jingxiongdi.huajingmusic.util.DBHelper;
import com.jingxiongdi.huajingmusic.util.MusicConstants;
import com.jingxiongdi.huajingmusic.util.SPUtils;
import com.jingxiongdi.huajingmusic.util.ToastUtil;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    private ExpandableListView expandableListView = null;
    private ArrayList<String> songListString = new ArrayList<>();
    private ArrayList<ArrayList<Song>> songList = new ArrayList<>();
    private MainAdapter mainAdapter = null;

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

    }

    private void setViews() {
        Toolbar toolbar = findViewById(R.id.id_toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });
        expandableListView = findViewById(R.id.expanded_list);
    }


}
