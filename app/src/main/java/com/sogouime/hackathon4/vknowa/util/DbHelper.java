package com.sogouime.hackathon4.vknowa.util;

/**
 * Created by zhusong on 2017-08-20.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.sogouime.hackathon4.vknowa.constant.DbConstants;

public class DbHelper extends SQLiteOpenHelper {
    public DbHelper(Context context) {
        super(context, DbConstants.DB_NAME, null, DbConstants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            /*
            db.execSQL(DbConstants.CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL.toString());
            db.execSQL(DbConstants.CREATE_IMAGE_SDCARD_CACHE_TABLE_INDEX_SQL.toString());

            db.execSQL(DbConstants.CREATE_HTTP_CACHE_TABLE_SQL.toString());
            db.execSQL(DbConstants.CREATE_HTTP_CACHE_TABLE_INDEX_SQL.toString());
            db.execSQL(DbConstants.CREATE_HTTP_CACHE_TABLE_UNIQUE_INDEX.toString());
            */
            db.execSQL(DbConstants.CREATE_ORIGIN_DATA_TABLE_SQL.toString());
            db.execSQL(DbConstants.CREATE_LEXER_WORDS_TABEL_SQL.toString());
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
