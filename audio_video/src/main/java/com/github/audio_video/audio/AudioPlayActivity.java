package com.github.audio_video.audio;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.github.audio_video.R;
import com.github.audio_video.Utils;

import androidx.appcompat.app.AppCompatActivity;

public class AudioPlayActivity extends AppCompatActivity {

    private AudioTracker mAudioTracker;
    private final String PATH = Environment.getExternalStorageDirectory() + "/test.pcm";
    private AudioRecordDemo mAudioRecordDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_play);
        mAudioTracker = new AudioTracker(this);

        mAudioRecordDemo = new AudioRecordDemo();

        mAudioRecordDemo.setOnAudioFrameCapturedListener(new AudioRecordDemo.OnAudioFrameCapturedListener() {
            @Override
            public void onAudioFrameCaptured(byte[] audioData) {
                //写入音频数据到PCM文件中
                Utils.writePCM(audioData);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAudioTracker.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioTracker.release();
    }

    /**
     * 使用AudioRecord采集PCM音频
     * @param view
     */
    public void playRecord(View view) {
        mAudioRecordDemo.startCapture();
    }

    /**
     * 停止采集
     * @param view
     */
    public void stopRecord(View view) {
        mAudioRecordDemo.stopCapture();
    }

    /**
     * 使用Audiotrack播放PCM音频文件
     * @param view
     */
    public void playPCM(View view) {
        mAudioTracker.createAudioTracker(PATH);
        mAudioTracker.start();
    }

    public void OpenSL_Play_PCM(View view) {
        nativePlayPcm(PATH);
    }

    public void OpenSL_Stop_PCM(View view) {
        nativeStopPcm();
    }

    private static native void nativePlayPcm(String pcmPath);

    private static native void nativeStopPcm();
}