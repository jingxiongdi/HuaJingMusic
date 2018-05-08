package com.jingxiongdi.huajingmusic.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jingxiongdi.huajingmusic.bean.Song;

import java.util.ArrayList;

/**
 * Created by jingxiongdi on 2018/5/5.
 */

public class DBHelper {

    private static DBConnection helper;
    private static DBHelper dbHelper;

    public static DBHelper getInstance(Context context)
    {
        if(dbHelper == null){
            dbHelper = new DBHelper(context);
        }
        return dbHelper;
    }

    public DBHelper(Context context){
        super();
        getDB(context);
    }

    private static DBConnection getDB(Context context){

        if(helper == null){
            helper = new DBConnection(context);
        }
        return helper;
    }



    public void addSongList(ArrayList<Song> prolist)
    {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        for(int i = 0;i<prolist.size();i++)
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put(UserSchema.name,prolist.get(i).getFileName());
            contentValues.put(UserSchema.album,prolist.get(i).getAlbum());
            contentValues.put(UserSchema.duration, prolist.get(i).getDuration());
            contentValues.put(UserSchema.fileUrl, prolist.get(i).getFileUrl());
            contentValues.put(UserSchema.title,prolist.get(i).getTitle());
            contentValues.put(UserSchema.singer,prolist.get(i).getSinger());
            contentValues.put(UserSchema.size, prolist.get(i).getSize());
            contentValues.put(UserSchema.type, prolist.get(i).getType());
            contentValues.put(UserSchema.year,prolist.get(i).getYear());
            db.insert(UserSchema.TABLE_NAME, null, contentValues);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void addSong(Song pro)
    {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(UserSchema.name,pro.getFileName());
        contentValues.put(UserSchema.album,pro.getAlbum());
        contentValues.put(UserSchema.duration, pro.getDuration());
        contentValues.put(UserSchema.fileUrl, pro.getFileUrl());
        contentValues.put(UserSchema.title,pro.getTitle());
        contentValues.put(UserSchema.singer,pro.getSinger());
        contentValues.put(UserSchema.size, pro.getSize());
        contentValues.put(UserSchema.type, pro.getType());
        contentValues.put(UserSchema.year,pro.getYear());
        db.insert(UserSchema.TABLE_NAME, null, contentValues);
        db.close();

    }


    public void dropAndRecreateTable() {
        SQLiteDatabase db = null;
        try {

            db = helper.getWritableDatabase();
            String sql = "DROP TABLE " + UserSchema.TABLE_NAME;
            db.execSQL(sql);

            String create_sql = "CREATE TABLE song_info(id integer primary key autoincrement,name varchar(200),title varchar(100),"
                    + "duration integer,singer varchar(100),album varchar(100),year varchar(100)," +
                    "type varchar(100),size varchar(100),fileUrl varchar(100))";
            db.execSQL(create_sql);
        } catch (Exception ex) {

        }
        finally {
            if(db!=null){
                db.close();
            }
        }

    }

    public void DropDatabase(Context ctx, String DATABASE_NAME) {
        try {
            ctx.deleteDatabase(DATABASE_NAME);
        } catch (Exception ex) {

        }
    }

    public void update(ContentValues values, String where, String[] whereArgs) {

        SQLiteDatabase db = helper.getWritableDatabase();
        db.update(UserSchema.TABLE_NAME, values, where, whereArgs);
        db.close();
    }

    public void delete(String where, String[] whereArgs) {

        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(UserSchema.TABLE_NAME, where, whereArgs);
        db.close();
    }

    public void delDB(String DBName) {

        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DROP Database " + DBName);
        db.close();
    }




//    public ArrayList<String> queryUrlList() {
//        ArrayList<String> channelList = new ArrayList<>();
//        try {
//            final SQLiteDatabase db = helper.getReadableDatabase();
//            Cursor c = null;
//           /* c = db.query(UserSchema.TABLE_NAME, null, where, whereArgs, null,
//                    null, orderBy, limit);*/
//            c = db.query(UserSchema.TABLE_NAME, new String[] {UserSchema.channel_id, UserSchema.channel_name,UserSchema.url,UserSchema.group_name },
//                    null, null, null, null, null);
//            ArrayList<ChannelInfo> lst = new ArrayList<ChannelInfo>();
//
//            while (c != null && c.moveToNext()) {
//                channelList.add(c.getString(c.getColumnIndex("url")));
//            }
//            if (c != null)c.close();
//            return channelList;
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//
//        return null;
//
//    }

    public ArrayList<Song> queryAllSong() {

        try {
            final SQLiteDatabase db = helper.getReadableDatabase();
            Cursor c = null;
           /* c = db.query(UserSchema.TABLE_NAME, null, where, whereArgs, null,
                    null, orderBy, limit);*/
            String sql = "select * from song_info";
            L.d(sql);
            c = db.rawQuery(sql,null);
            ArrayList<Song> lst = new ArrayList<Song>();

            while (c != null && c.moveToNext()) {
                Song item = new Song();
                item.setAlbum(c.getString(c.getColumnIndex(UserSchema.album)));
                item.setDuration(c.getInt(c.getColumnIndex(UserSchema.duration)));
                item.setFileName(c.getString(c.getColumnIndex(UserSchema.name)));
                item.setFileUrl(c.getString(c.getColumnIndex(UserSchema.fileUrl)));

                item.setTitle(c.getString(c.getColumnIndex(UserSchema.title)));
                item.setType(c.getString(c.getColumnIndex(UserSchema.type)));
                item.setSinger(c.getString(c.getColumnIndex(UserSchema.singer)));
                item.setSize(c.getString(c.getColumnIndex(UserSchema.size)));
                item.setYear(c.getString(c.getColumnIndex(UserSchema.year)));
                lst.add(item);
            }
            if (c != null)c.close();
            return lst;
        } catch (Exception e) {
            // TODO: handle exception
        }

        return null;

    }

    public interface UserSchema {
        String TABLE_NAME = "song_info";
        String id = "id";
        String name = "name";
        String title = "title";
        String duration = "duration";
        String singer = "singer";
        String album = "album";
        String year = "year";
        String type = "type";
        String size = "size";
        String fileUrl = "fileUrl";
    }

    public static class DBConnection extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "songs.db";
        private static final int DATABASE_VERSION = 1;

        private DBConnection(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            String create_sql = "CREATE TABLE song_info(id integer primary key autoincrement,name varchar(200),title varchar(100),"
                    + "duration integer,singer varchar(100),album varchar(100),year varchar(100)," +
                    "type varchar(100),size varchar(100),fileUrl varchar(100))";
            db.execSQL(create_sql);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            String sql=" DROP TABLE IF EXISTS "+UserSchema.TABLE_NAME;
            db.execSQL(sql);
            onCreate(db);
        }

    }

}
