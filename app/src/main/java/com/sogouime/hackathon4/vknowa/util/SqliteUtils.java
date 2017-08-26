package com.sogouime.hackathon4.vknowa.util;

/**
 * Created by zhusong on 2017-08-20.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NavUtils;

import com.sogouime.hackathon4.vknowa.entity.LexerWords;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import static java.sql.Types.NULL;

public class SqliteUtils {

    private static volatile SqliteUtils instance;

    private DbHelper                    dbHelper;
    private SQLiteDatabase              db;

    private SqliteUtils(Context context) {
        dbHelper = new DbHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public static SqliteUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (SqliteUtils.class) {
                if (instance == null) {
                    instance = new SqliteUtils(context);
                }
            }
        }
        return instance;
    }

    public static SqliteUtils getInstance()
    {
        if ( instance != null )
        {
            return instance;
        }
        else
        {
            return null;
        }
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    //逻辑接口
    public boolean InsertItem(String parsedText, String filePath, String name, HashSet<LexerWords> lexerWords){
        String sql = String.format("select id from origin_data where file_path='%s'", filePath);

        Cursor cursor = db.rawQuery(sql, null);
        int a = -1;
        if (cursor.moveToNext()){
            a = cursor.getInt(0);
        }
        ContentValues contentValues = new ContentValues();
        if (a == -1){
            contentValues.put("id", NULL);
            contentValues.put("file_path", filePath);
            contentValues.put("parsed_text", parsedText);
            contentValues.put("name", name );
            contentValues.put("timestamp", 1);
            db.insertOrThrow("origin_data", null, contentValues);

            cursor = db.rawQuery(sql, null);
            if (!cursor.moveToNext()){
                return false;
            }
            a = cursor.getInt(0);
        }

        Iterator<LexerWords> it = lexerWords.iterator();
        while (it.hasNext()){
            LexerWords curWord = it.next();
            ContentValues contentValues1 = new ContentValues();
            contentValues1.put("weight", curWord.GetWeight());
            contentValues1.put("tag", curWord.GetTag());
            contentValues1.put("ntag", curWord.GetNTag());
            contentValues1.put("word", curWord.GetWord());
            contentValues1.put("fileindex", a);
            db.insertOrThrow("lexer_words", null, contentValues1);
        }

        return  true;
    }

    public ArrayList<Integer> Search(String word, int nTag){
        ArrayList<Integer> arrRet = new ArrayList<Integer>();
        //db.beginTransaction();
        String Sql = String.format("select * from lexer_words where word='%s' AND ntag=%d", word, nTag);
       // Sql = "select * from origin_data";
        Cursor cursor = db.rawQuery(Sql, new String[]{});
        while (cursor.moveToNext()){
            arrRet.add(cursor.getInt(4));
        }
       // db.endTransaction();
        return  arrRet;
    }

    public String GetOriginFilePathByIndex(int index){
        String strRet = null;
      //  db.beginTransaction();
        String sql = String.format("select file_path from origin_data where id=%d", index);
        Cursor cursor = db.rawQuery(sql, new String[]{});
        if (cursor.moveToNext()){
            strRet = cursor.getString(0);
        }
        return  strRet;
    }


}
