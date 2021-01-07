package com.github.audio_video.video;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

/**
 * Created by lvming on 12/25/20 7:40 PM.
 * Email: lvming@guazi.com
 * Description:
 */
public class YUVPlay extends GLSurfaceView implements SurfaceHolder.Callback {

    private Context context;
    private String TAG = getClass().getSimpleName();
    private String yuv420Path;
    private Object surface;

    public YUVPlay(Context context) {
        super(context);
    }

    public YUVPlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context.getApplicationContext();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        super.surfaceCreated(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        super.surfaceDestroyed(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//        super.surfaceChanged(holder, format, w, h);
    }

    public native void nativeGlesPlay(String yuv420Path, Object surface);
    public native void nativeWindowPlay(String yuv420Path, Object surface);

    public  void showMessage(final String message){
        Log.d(TAG,message);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,message,Toast.LENGTH_LONG).show();
            }
        });
    }

    //使用gles播放
    public void glesPlay(final String yuv420Path, final Object surface) {
        this.yuv420Path = yuv420Path;
        this.surface = surface;

        Thread thread = new Thread(playRunnable);
        thread.start();
    }

    private Runnable playRunnable = new Runnable() {
        @Override
        public void run() {
            nativeGlesPlay(yuv420Path, surface);
        }
    };

    public native void onDestroy();

}
