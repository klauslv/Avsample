package com.github.audio_video.video;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.github.audio_video.R;

import androidx.appcompat.app.AppCompatActivity;

public class VideoPlayActivity extends AppCompatActivity {

    private YUVPlay mYUVPlay;

    private final String PATH = Environment.getExternalStorageDirectory() + "/yuvtest.yuv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        mYUVPlay = findViewById(R.id.surface);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mYUVPlay.onDestroy();
        mYUVPlay = null;
    }

    /**
     * OPenGL ES
     * @param view
     */
    public void gles_play(View view) {
        mYUVPlay.glesPlay(PATH, mYUVPlay.getHolder().getSurface());
    }

    /**
     * nativeWindow
     * @param view
     */
    public void native_window_play(View view) {
        mYUVPlay.nativeWindowPlay(PATH, mYUVPlay.getHolder().getSurface());
    }
}