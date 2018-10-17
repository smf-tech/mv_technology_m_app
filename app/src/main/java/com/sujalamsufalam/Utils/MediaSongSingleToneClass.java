package com.sujalamsufalam.Utils;

import android.media.MediaPlayer;

/**
 * Created by Admin on 8/11/2016.
 */
public class MediaSongSingleToneClass {
    private static MediaPlayer mp;
    private MediaSongSingleToneClass()
    {

    }
    public static MediaPlayer getInstance() {
        if (mp==null)
        {
            mp =new MediaPlayer();
        }
        return mp;
    }

}
