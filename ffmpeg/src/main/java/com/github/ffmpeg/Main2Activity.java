package com.github.ffmpeg;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class Main2Activity extends AppCompatActivity {
//
//    static {
//        System.loadLibrary("ffmpeg_lib");
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        TextView textView = findViewById(R.id.sample_text);
//        textView.setText(getFFmpegVersion());
    }

    /**
     * @return 返回当前
     */
//    public native static String getFFmpegVersion();
}