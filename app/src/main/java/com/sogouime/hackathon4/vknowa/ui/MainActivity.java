package com.sogouime.hackathon4.vknowa.ui;

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
import com.sogouime.hackathon4.vknowa.util.LogUtils;
import com.sogouime.hackathon4.vknowa.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private Button mHttpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StringBuilder urlDeepi = new StringBuilder("http://api.ai.sogou.com/nlp/lexer");
        urlDeepi.append("?text=");

        try {
            //!< 测试HTTP
            String testStr = "我把钥匙放在右边抽屉的柜子里了";
            urlDeepi.append((testStr != null ? URLEncoder.encode(testStr, "UTF-8") : ""));
            //urlDeepi.append(StringUtils.utf8Encode(testStr));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("This method requires UTF-8 encoding support", e);
        }

        Http2Utils.doGetAsyn(urlDeepi.toString(), new Http2Utils.CallBack() {
            @Override
            public void onRequestComplete(String result) {
                LogUtils.d(result);
            }
        });

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
