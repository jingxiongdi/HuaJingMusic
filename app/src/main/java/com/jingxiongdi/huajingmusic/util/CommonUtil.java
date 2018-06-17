package com.jingxiongdi.huajingmusic.util;

import com.jingxiongdi.huajingmusic.bean.Song;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by jingxiongdi on 2018/6/17.
 */

public class CommonUtil {

    public static ArrayList<Song> getRomSongList(ArrayList<Song> list){
        /**
         * ,rand.nextInt(100);中的100是随机数的上限,产生的随机数为0-100的整数,不包括100。
         */
       // L.d("newList size "+list.size());
        ArrayList<Song> newList = new ArrayList<>();
        Random a = new Random();//这里不要设置参数，否则每次获得的随机数是一样的
        int i;
        for(i = 0;i<list.size();i++){
            int c = a.nextInt(list.size());
            L.d("i "+i+" c "+c);
            if(newList.contains(list.get(c))){
               // L.d("****chongfu "+list.get(c));
                i = i - 1;
            }else{
                newList.add(list.get(c));
            //    L.d("歌曲"+i+" "+newList.get(i).getFileName());
            }

        }
     //   L.d("newList size aaaa "+newList.size());
        return newList;
    }
}
