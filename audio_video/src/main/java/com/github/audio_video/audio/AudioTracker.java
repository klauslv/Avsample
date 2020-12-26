package com.github.audio_video.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lvming on 12/24/20 3:07 PM.
 * Email: lvming@guazi.com
 * Description:
 */
public class AudioTracker {
    private static final String TAG = "AudioTracker";

    //采样率 44100HZ ，所有的设备都支持
    private static final int SAMPLE_RATE = 44100;
    //单声道，所有的设备都支持
    private static final int CHANNEL = AudioFormat.CHANNEL_OUT_MONO;
    //位深16位，所有的设备都支持
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    //缓冲区字节大小
    private int mBufferSizeInBytes = 0;
    private Context mContext;
    /**
     * 文件名
     */
    private String mFilePath;
    //播放对象
    private AudioTrack mAudioTrack;
    private volatile Status mStatus = Status.STATUS_NO_READY;
    //单任务线程池
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private Handler mMainHandler = new Handler(Looper.getMainLooper());


    public AudioTracker(Context context) {
        mContext = context;
    }


    //创建AudioTracker
    public void createAudioTracker(String filePath) throws IllegalStateException {
        mFilePath = filePath;
        mBufferSizeInBytes = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL, AUDIO_FORMAT);
        if (mBufferSizeInBytes <= 0) {
            throw new IllegalStateException("AudioTrack is not available" + mBufferSizeInBytes);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAudioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC).build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AUDIO_FORMAT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(CHANNEL).build())
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setBufferSizeInBytes(mBufferSizeInBytes)
                    .build();

        } else {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                    CHANNEL, AUDIO_FORMAT, mBufferSizeInBytes, AudioTrack.MODE_STREAM);
        }
        mStatus = Status.STATUS_READY;
    }

    public void start() {
        if (mStatus == Status.STATUS_NO_READY || mAudioTrack == null) {
            throw new IllegalStateException("播放器尚未初始化");
        }
        if (mStatus == Status.STATUS_START) {
            throw new IllegalStateException("正在播放...");
        }
        Log.d(TAG, "==start==");
        mExecutorService.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    playAudioData();
                } catch (IOException e) {
                    e.printStackTrace();
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "播放出错", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        mStatus = Status.STATUS_START;
    }

    /**
     * 播放PCM 音频数据
     *
     * @throws IOException
     */
    private void playAudioData() throws IOException {
        InputStream dis = null;
        try {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "播放开始", Toast.LENGTH_SHORT).show();
                }
            });
            dis = new DataInputStream(new BufferedInputStream(new FileInputStream(mFilePath)));
            byte[] bytes = new byte[mBufferSizeInBytes];
            int length;

            //当前播放实例是否初始化成功，如果处于初始化成功的状态并且未播放的状态，那么就调用 play
            if (null != mAudioTrack && mAudioTrack.getState() != AudioTrack.STATE_UNINITIALIZED
                    && mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                mAudioTrack.play();
            }
            while ((length = dis.read(bytes)) != -1 && mStatus == Status.STATUS_START) {
                mAudioTrack.write(bytes, 0, length);
            }
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "播放结束", Toast.LENGTH_SHORT).show();
                }
            });
        } finally {
            if (dis != null) {
                dis.close();
            }
        }
    }

    //停止播放
    public void stop() throws IllegalStateException {
        Log.d(TAG,"==stop==");
        if (mStatus == Status.STATUS_NO_READY || mStatus == Status.STATUS_READY){
            Log.d(TAG,"播放尚未开始");
        } else{
            mStatus = Status.STATUS_STOP;
            mAudioTrack.stop();
            release();
        }
    }
    //释放
    public void release(){
        Log.d(TAG,"==release==");
        mStatus = Status.STATUS_NO_READY;
        if(mAudioTrack != null){
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    public enum Status {
        //未开始
        STATUS_NO_READY,
        //预备
        STATUS_READY,
        //播放
        STATUS_START,
        //停止
        STATUS_STOP
    }
}
