package com.sogouime.hackathon4.vknowa.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.sogouime.hackathon4.vknowa.R;
import com.sogouime.hackathon4.vknowa.util.Http2Utils;
import com.sogouime.hackathon4.vknowa.util.JSONUtils;
import com.sogouime.hackathon4.vknowa.util.LogUtils;
import com.sogouime.hackathon4.vknowa.util.SqliteUtils;
import com.sogouime.hackathon4.vknowa.util.StringUtils;

public class MainActivity extends AppCompatActivity {

    private Button mHttpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

/*        try {
            Http2Utils.doPostAsyn(urlDeepi.toString(), param.toString(), new Http2Utils.CallBack() {
                @Override
                public void onRequestComplete(String result) {
                    LogUtils.d(result);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //!< 网络测试Button
        Button mHttpBtn = (Button)findViewById(R.id.button2);
        mHttpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread()
                {
                    public void run()
                    {
                        //SQLiteDatabase dataBase = /*SqliteUtils.getInstance(getApplicationContext())*/SQLiteDatabase.openOrCreateDatabase("/data/data/com.sogouime.hackthon4.vknowa/databases/message.db", null);  ;

                        SQLiteDatabase dataBase = SqliteUtils.getInstance(getApplicationContext()).getDb();
                        String sqlCreate="create table voiceinfo(_id integer primary key autoincrement,sname text,snumber text)";//执行SQL语句
                        dataBase.execSQL(sqlCreate);

/*                        ContentValues cValue = new ContentValues();
                        cValue.put("sname", "xiaoming");
                        cValue.put("snumber", "01005");
                        dataBase.insert("stu_table", null, cValue);*/

                        String sqlInsert="insert into voiceinfo(sname,snumber) values('xiaoming','01006')";
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
                        }

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

                            String getParam = /*"我把钥匙放在右边抽屉的柜子里了"*/"今天下午我想去游泳";
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
        });
    // Example of a call to a native method
    TextView tv = (TextView) findViewById(R.id.sample_text);
    //tv.setText(stringFromJNI());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
