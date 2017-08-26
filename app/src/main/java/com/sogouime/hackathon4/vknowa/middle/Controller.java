package com.sogouime.hackathon4.vknowa.middle;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sogouime.hackathon4.vknowa.util.Http2Utils;
import com.sogouime.hackathon4.vknowa.util.JSONUtils;
import com.sogouime.hackathon4.vknowa.util.LogUtils;
import com.sogouime.hackathon4.vknowa.util.SqliteUtils;
import com.sogouime.hackathon4.vknowa.util.StringUtils;

/**
 * Created by zhusong on 2017-08-26.
 */

public class Controller {

    public static void TransVoiceInfo(String voiceText, String voiceFilePath) {
        new Thread()
        {
            public void run()
            {
                //SQLiteDatabase dataBase = /*SqliteUtils.getInstance(getApplicationContext())*/SQLiteDatabase.openOrCreateDatabase("/data/data/com.sogouime.hackthon4.vknowa/databases/message.db", null);  ;

/*                SQLiteDatabase dataBase = SqliteUtils.getInstance(getApplicationContext()).getDb();
                String sqlCreate="create table voiceinfo(_id integer primary key autoincrement,sname text,snumber text)";//执行SQL语句
                dataBase.execSQL(sqlCreate);*/

/*                        ContentValues cValue = new ContentValues();
                        cValue.put("sname", "xiaoming");
                        cValue.put("snumber", "01005");
                        dataBase.insert("stu_table", null, cValue);*/

/*                String sqlInsert="insert into voiceinfo(sname,snumber) values('xiaoming','01006')";
                dataBase.execSQL(sqlInsert);

                String sqlInsert2="insert into voiceinfo(sname,snumber) values('zhusong','01007')";
                dataBase.execSQL(sqlInsert2);

                String sqlInsert3="insert into voiceinfo(sname,snumber) values('zhaiyiqiao','01008')";
                dataBase.execSQL(sqlInsert3);

                Cursor cursor = dataBase.query("voiceinfo", new String[]{"_id","sname","snumber"}, "_id=?", new String[]{"3"}, null, null, null);
                while(cursor.moveToNext()){
                    String name = cursor.getString(cursor.getColumnIndex("sname"));
                    String number = cursor.getString(cursor.getColumnIndex("snumber"));
                    System.out.println("query------->" + "姓名："+name+" "+"学号："+number);
                }*/

/*                        String whereClause = "id=?";
                        String[] whereArgs = {String.valueOf(2)};
                        dataBase.delete("stu_table",whereClause,whereArgs);*/

/*                        String sqlDeleteItem = "delete from voiceinfo where _id = 1";
                        dataBase.execSQL(sqlDeleteItem);*/

/*                        String sqlDelete ="DROP TABLE voiceinfo";
                        dataBase.execSQL(sqlDelete);*/

/*                        ContentValues values = new ContentValues();
                        values.put("snumber","101003");
                        String whereClause = "id=?";
                        String[] whereArgs={String.valuesOf(1)};
                        dataBase.update("usertable", values, whereClause, whereArgs);*/

/*                        String sqlUpdate = "update voiceinfo set snumber = 654321 where id = 1";
                        dataBase.execSQL(sqlUpdate);*/

                try
                {
                    StringBuilder urlLexerGet = new StringBuilder("http://api.ai.sogou.com/nlp/lexer");
                    urlLexerGet.append("?text=");

                    String voiceFilePathDummy = voiceFilePath;
                    String getParam = /*"我把钥匙放在右边抽屉的柜子里了"*//*"今天下午我想去游泳"*/voiceText;
                    urlLexerGet.append(StringUtils.utf8Encode(getParam));
                    StringBuilder response = new StringBuilder(Http2Utils.doGet(urlLexerGet.toString()));
                    if (!response.toString().isEmpty()) {
                        //!< status
                        int defaultStatus = -1;
                        int statusValue = JSONUtils.getInt(response.toString(), "status", defaultStatus);

                        //!< statusText
                        String defaultStatusText = "";
                        String statusTextValue = JSONUtils.getString(response.toString(), "statusText", defaultStatusText);

                        //!< count
                        int defaultCount = 0;
                        int countValue = JSONUtils.getInt(response.toString(), "count", defaultCount);

                        //!< text
                        String defaultText = "";
                        String textValue = JSONUtils.getString(response.toString(), "text", defaultText);

                        //!< items
                        String defaultItems[] = {};
                        String returnItems[] = JSONUtils.getStringArray(response.toString(), "items", defaultItems);

                        for(int index = 0; index < returnItems.length; ++index) {
                            String returnValue = returnItems[index];
                            LogUtils.d(returnValue);
                        }
                    }

/*                          StringBuilder urlSemanticPost = new StringBuilder("http://api.ai.sogou.com/nlp/semantic");
                            StringBuilder postParam = new StringBuilder("text=");
                            postParam.append(StringUtils.utf8Encode(getParam));
                            Http2Utils.doPost(urlSemanticPost.toString(), postParam.toString());*/
                } catch (Exception e)
                {
                    e.printStackTrace();
                }

            };
        }.start();
    }
}
