package com.jingxiongdi.huajingmusic.adapter;


import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jingxiongdi.huajingmusic.R;
import com.jingxiongdi.huajingmusic.bean.Song;
import com.jingxiongdi.huajingmusic.util.DensityUtils;

import java.util.ArrayList;

/**
 * Created by jingxiongdi on 2018/5/6.
 */

public class MainAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private ArrayList<ArrayList<Song>> songList;
    private ArrayList<String> songListString;
    private LayoutInflater mInflater;

    private int curPlayPos = 0;

    public MainAdapter(Context context, ArrayList<ArrayList<Song>> songs,ArrayList<String> songString){
        mContext = context;
        songList = songs;
        songListString = songString;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getGroupCount() {
        return songListString.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return songList.get(i).size();
    }

    @Override
    public Object getGroup(int i) {
        return songListString.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return songList.get(i).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean b, View convertView, ViewGroup viewGroup) {
        TextView myText = null;
        if (convertView != null) {
            myText = (TextView)convertView;
            myText.setText(groupPosition+1+" "+songListString.get(groupPosition));
        } else {
            myText = createView(groupPosition+1+" "+songListString.get(groupPosition));
        }
        return myText;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean b, View convertView, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            if (viewHolder != null)
            {
                convertView = mInflater.inflate(R.layout.list_child_item, null);
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.playImage = (ImageView) convertView.findViewById(R.id.play_img);
                convertView.setTag(viewHolder);
            }
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.name.setText(childPosition+1+": "+songList.get(groupPosition).get(childPosition).getFileName());
        if(curPlayPos == childPosition){
            viewHolder.playImage.setBackgroundResource(R.mipmap.mini_play_button);
        }
        else {
            viewHolder.playImage.setBackgroundResource(0);
        }

        return convertView;
    }

    public void refreshAdapter(int p){
        curPlayPos = p;
        notifyDataSetChanged();
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        //点击事件不生效
        return true;
    }

    private TextView createView(String content) {
        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, (int) DensityUtils.px2dp(mContext,250f));
        TextView myText = new TextView(mContext);
        myText.setTextSize(22);
        myText.setLayoutParams(layoutParams);
        myText.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        myText.setPadding((int) DensityUtils.px2dp(mContext,250f), 0, 0, 0);
        myText.setText(content);
        return myText;
    }

//    private TextView createChidView(String content) {
//        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
//                ViewGroup.LayoutParams.FILL_PARENT, (int) DensityUtils.px2dp(mContext,250f));
//        TextView myText = new TextView(mContext);
//        myText.setTextSize(16);
//        myText.setLayoutParams(layoutParams);
//        myText.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
//        myText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
//        myText.setMaxLines(2);
//        myText.setPadding((int) DensityUtils.px2dp(mContext,350f), 0, 0, 0);
//        myText.setText(content);
//        return myText;
//    }

    private static class ViewHolder
    {
        private TextView name;
        private ImageView playImage;
    }
}
