package com.example.floattest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDBHandler {

    private final String TAG = "MyDBHandler";

    SQLiteOpenHelper mHelper = null;
    SQLiteDatabase mDB = null;

    //Automatically create a chatlog database when this creates a handler.
    public MyDBHandler(Context context, String name) {
        mHelper = new MySQLiteOpenHelper(context, name, null, 1);
    }

    public static MyDBHandler open(Context context, String name) {
        return new MyDBHandler(context, name);
    }
    public Cursor tabNum(String packageName){
        mDB = mHelper.getReadableDatabase();
        Cursor c = mDB.rawQuery("SELECT DISTINCT title from "+packageName,null);
        return c;
    }


    public Cursor select(String packageName)
    {
        mDB = mHelper.getReadableDatabase();
        Cursor c = mDB.query(packageName, null, null, null, null, null, null);
        return c;
    }



    public void insert(String packageName, int id, long postTime, String title, String text, String subText) {

        Log.d(TAG, "insert");

        mDB = mHelper.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put("id", id);
        value.put("postTime", postTime);
        value.put("title", title);
        value.put("text", text);
        value.put("subText", subText);

        String packNmae = packageName.replaceAll("\\.", "");

        mDB.insert(packNmae, null, value);

    }

    public void delete(String packName,String tabName)
    {
        Log.d(TAG, "delete");
        mDB = mHelper.getWritableDatabase();
        mDB.delete(packName, "title=?", new String[]{tabName});
    }

    public void close() {
        mHelper.close();
    }

    //create table
    public void createTable(String packageName){
        mDB = mHelper.getWritableDatabase();

        String sql = "CREATE TABLE IF NOT EXISTS "+packageName+"(_id integer primary key autoincrement, id integer,posttime long, title text, text text, subtext text)";
        mDB.execSQL(sql);
    }
    public void deleteTable(String packageName){
        mDB = mHelper.getWritableDatabase();

        String sql = "DROP TABLE "+packageName;
        mDB.execSQL(sql);
    }
    public void deleteAll(String packName){
        mDB = mHelper.getWritableDatabase();
        String sql ="DROP TABLE IF EXISTS "+packName.replaceAll("\\.", "");
        mDB.execSQL(sql);
    }

}