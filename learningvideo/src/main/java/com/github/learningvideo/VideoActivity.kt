package com.github.learningvideo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import com.github.learningvideo.media.decoder.AudioDecoder
import com.github.learningvideo.media.decoder.DefDecodeStateListener
import com.github.learningvideo.media.decoder.VideoDecoder
import com.github.learningvideo.media.muxer.MP4Repack
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import kotlinx.android.synthetic.main.activity_video.*
import java.util.concurrent.Executors

/**
 * 简单播放器
 */
class VideoActivity : AppCompatActivity() {
    val path = Environment.getExternalStorageDirectory().absolutePath + "/mvtest.mp4"
    lateinit var videoDecoder: VideoDecoder
    lateinit var audioDecoder: AudioDecoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        requestPermission()
        initPlayer()
    }

    private fun requestPermission() {
        val permissions = Permission.Group.STORAGE
//        AndPermission.with(this)
//                .runtime()
//                .permission(permissions)
//                .onGranted{}
//                .onDenied{
//                    Toast.makeText(this, "请打开权限，否则无法获取本地文件", Toast.LENGTH_SHORT).show()
//                }.start()
    }

    private fun initPlayer() {
        //创建线程池
        val threadPool = Executors.newFixedThreadPool(10)

        //创建视频解码器
        videoDecoder = VideoDecoder(path, sfv, null)
        videoDecoder.setStateListener(DefDecodeStateListener())
        threadPool.execute(videoDecoder)

        //创建音频解码器
        audioDecoder = AudioDecoder(path)
        audioDecoder.setStateListener(DefDecodeStateListener())
        threadPool.execute(audioDecoder)

        //开启播放
        videoDecoder.resume()
        audioDecoder.resume()
    }

    fun clickRepack(view: View) {
        repack()
    }

    private fun repack() {
        val repack = MP4Repack(path)
        repack.start()
    }

    override fun onDestroy() {
        videoDecoder.stop()
        audioDecoder.stop()
        super.onDestroy()
    }
}