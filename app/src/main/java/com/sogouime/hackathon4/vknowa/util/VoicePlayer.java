package com.sogouime.hackathon4.vknowa.util;

/**
 * Created by liugao on 2017/8/26.
 */
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;


public class VoicePlayer {

    public static void PlayVoice(String p_uri)
    {
        if ( p_uri.isEmpty() )
        {
            return;
        }
        MediaPlayer mediaPlayer = new MediaPlayer();
        try
        {
            mediaPlayer.setDataSource(p_uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
