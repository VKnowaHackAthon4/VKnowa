package com.sogouime.hackathon4.vknowa.util;

/**
 * Created by zhusong on 2017-08-20.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.InterpolatorRes;
import android.support.v4.app.NavUtils;

import com.sogouime.hackathon4.vknowa.entity.LexerWords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    // 清空表
    public void ClearTables()
    {
        int n = db.delete("hwd_table1", null, null);
        n = db.delete("hwd_table2", null, null);
        n = db.delete("hwd_table3", null, null);
    }

    // 获取总文件数
    public int GetTotalFiles()
    {
        Cursor cursor = db.rawQuery("select count(*)from hwd_table2",null);
        cursor.moveToFirst();
        return (int)(cursor.getLong(0));
    }

    // 读取IDN
    public HashMap<String, Integer> LoadIDNFromDB()
    {
        HashMap<String, Integer> idn = new HashMap<String, Integer>();
        String sql = "select * from hwd_table1";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            String word = cursor.getString(cursor.getColumnIndex("word"));
            int num = cursor.getInt(cursor.getColumnIndex("num"));
            idn.put(word, num);
        }
        return idn;
    }
    // 读取IDF
    public HashMap<String, Float> LoadIDFFromDB()
    {
        HashMap<String, Float> idf = new HashMap<String, Float>();
        String sql = "select * from hwd_table1";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            String word = cursor.getString(cursor.getColumnIndex("word"));
            float idffre = cursor.getFloat(cursor.getColumnIndex("idffre"));
            idf.put(word, idffre);
        }
        cursor.close();
        return idf;
    }
    // 写入IDN
    public boolean SaveIDNToDB(HashMap<String, Integer> idn, HashMap<String, Float> idf)
    {
        Iterator iter = idn.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String word = entry.getKey().toString();
            int num = Integer.parseInt(entry.getValue().toString());
            float idffre = idf.get(word);
            String sql = String.format("REPLACE INTO hwd_table1 values('%s', %d, %f)", word, num, idffre);
            db.execSQL(sql);
        }
        return true;
    }

    // 读取TF
    public HashMap<String, HashMap<String, Float>> LoadTFFromDB()
    {
        HashMap<String, HashMap<String, Float>> allTf = new HashMap<String, HashMap<String, Float>>();
        String sql = "select * from hwd_table2";
        HashMap<Integer, String> ifs = new HashMap<Integer, String>();
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            int index = cursor.getInt(cursor.getColumnIndex("id"));
            String filename = cursor.getString(cursor.getColumnIndex("filename"));
            ifs.put(index, filename);
        }

        Iterator iter = ifs.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String filename = entry.getValue().toString();
            int index = Integer.parseInt(entry.getKey().toString());
            HashMap<String, Float> tf = new HashMap<String, Float>();
            sql = String.format("select * from hwd_table3 where id=%d", index);
            cursor = db.rawQuery(sql, null);
            while ( cursor.moveToNext() ) {
                String word = cursor.getString(cursor.getColumnIndex("word"));
                float tffre = cursor.getFloat(cursor.getColumnIndex("tffre"));
                tf.put(word, tffre);
            }
            allTf.put(filename, tf);
        }
        return allTf;
    }
    // 保存ALL_TF到数据库中
    public boolean SaveALLTFToDB(HashMap<String, HashMap<String, Float>> allTf)
    {
        return true;
    }
    // 保存TF到数据库中
    public boolean SaveTFToDB(String filename, HashMap<String, Float> Tf)
    {
        String sql = String.format("SELECT id FROM hwd_table2 WHERE filename='%s'", filename);
        Cursor cursor = db.rawQuery(sql, null);
        int index = -1;
        if (cursor.moveToNext()){
            index = cursor.getInt(0);
        }
        ContentValues cv = new ContentValues();
        if (index == -1){
            cv.put("filename", filename);
            db.insertOrThrow("hwd_table2", null, cv);
            cursor = db.rawQuery(sql, null);
            if (!cursor.moveToNext()){
                return false;
            }
            index = cursor.getInt(0);
        }

        Iterator iter = Tf.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String word = entry.getKey().toString();
            float tffre = Float.parseFloat(entry.getValue().toString());
            sql = String.format("REPLACE INTO hwd_table1 values(%d, '%s', %f)", index, word, tffre);
            db.execSQL(sql);
        }
        return true;
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
