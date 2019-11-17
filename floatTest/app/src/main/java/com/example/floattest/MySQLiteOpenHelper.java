package com.example.floattest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    private final String TAG = "MySQLiteOpenHelper";
    private SQLiteDatabase sqLiteDatabase ;

    //sql에서는 데이터베이스만 만들어둔다. 테이블은 직접생성
    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        this.sqLiteDatabase = sqLiteDatabase;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //String sql="drop table if exists student";
        //sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }


}

