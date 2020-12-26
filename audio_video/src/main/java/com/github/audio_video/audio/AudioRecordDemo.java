package com.github.audio_video.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by lvming on 12/24/20 4:20 PM.
 * Email: lvming@guazi.com
 * Description:
 */
public class AudioRecordDemo {
    private static final String TAG = "AudioRecordDemo";

    //默认录远音
    public static final int DEFAULT_SOURCE = MediaRecorder.AudioSource.MIC;

    /**
     * 采样率 使用常用的 44100
     */
    private static final int DEFAULT_SAMPLE_RATE = 44100;
    /**
     * 通道
     */
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 采样格式为 16 bit
     */
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    //是否开始采集
    private boolean mIsCaptureStarted = false;
    private int mMinBufferSize = 0;
    private AudioRecord mAudioRecord;

    private volatile boolean mIsLoopExit = false;
    private Thread mCaptureThread;

    private OnAudioFrameCapturedListener mOnAudioFrameCapturedListener;

    public interface OnAudioFrameCapturedListener {
        void onAudioFrameCaptured(byte[] audioData);
    }

    public void setOnAudioFrameCapturedListener(OnAudioFrameCapturedListener onAudioFrameCapturedListener) {
        mOnAudioFrameCapturedListener = onAudioFrameCapturedListener;
    }

    public boolean startCapture() {
        return startCapture(DEFAULT_SOURCE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
    }

    /**
     * 开始采集
     *
     * @param audioSource
     * @param sampleRateInHz
     * @param channelConfig
     * @param audioFormat
     * @return
     */
    public boolean startCapture(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        //已经开始采集，就停止采集
        if (mIsCaptureStarted) {
            Log.e(TAG, "Capture already started !");
            return false;
        }
        mMinBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (mMinBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invaid paramter");
            return false;
        }
        Log.d(TAG, "getMinBufferSize = " + mMinBufferSize + " bytes !");

        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, mMinBufferSize);
        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioRecord initialize fail !");
            return false;
        }
        mAudioRecord.startRecording();

        mIsLoopExit = false;
        mCaptureThread = new Thread(new AudioCaptureRunnable());
        mCaptureThread.start();
        mIsCaptureStarted = true;

        Log.d(TAG, "Start capture success");
        return true;
    }

    public void stopCapture() {
        if (!mIsCaptureStarted) {
            return;
        }
        mIsLoopExit = true;

        try {
            mCaptureThread.interrupted();
            mCaptureThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
        }
        mAudioRecord.release();
        mIsCaptureStarted = false;
        mOnAudioFrameCapturedListener = null;
        Log.d(TAG, "Stop audio capture success");
    }

    private class AudioCaptureRunnable implements Runnable {
        @Override
        public void run() {

            while (!mIsLoopExit) {
                byte[] buffer = new byte[mMinBufferSize];

                int ret = mAudioRecord.read(buffer, 0, mMinBufferSize);
                if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(TAG, "Error ERROR_INVALID_OPERATION");
                } else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "ERRor");
                } else {
                    if (mOnAudioFrameCapturedListener != null) {
                        mOnAudioFrameCapturedListener.onAudioFrameCaptured(buffer);
                    }
                    Log.d(TAG, "ok,Captured" + ret + "bytes");
                }
            }
        }
    }
}
