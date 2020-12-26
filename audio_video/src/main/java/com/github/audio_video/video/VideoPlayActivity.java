package com.github.audio_video.video;

import android.os.Bundle;
import android.view.View;

import com.github.audio_video.R;

import androidx.appcompat.app.AppCompatActivity;

public class VideoPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void gles_play(View view) {
    }

    public void native_window_play(View view) {
    }
}