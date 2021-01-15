package com.github.learningvideo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.Surface
import com.github.learningvideo.media.decoder.AudioDecoder
import com.github.learningvideo.media.decoder.VideoDecoder
import com.github.learningvideo.opengl.SimpleRender
import com.github.learningvideo.opengl.drawer.VideoDrawer
import kotlinx.android.synthetic.main.activity_multi_open_g_l_player.*
import kotlinx.android.synthetic.main.activity_open_g_l_player.*
import java.util.concurrent.Executors

class MultiOpenGLPlayerActivity : AppCompatActivity() {
    private val path = Environment.getExternalStorageDirectory().absolutePath + "/mvtest.mp4"
    private val path2 = Environment.getExternalStorageDirectory().absolutePath + "/mvtest2.mp4"

    private val render = SimpleRender()

    private val threadPool = Executors.newFixedThreadPool(10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_open_g_l_player)
        initFirstVideo()
        initSecondVideo()
        initRender()
    }

    private fun initFirstVideo() {
        val drawer = VideoDrawer()
        drawer.setVideoSize(1920, 1080)
        drawer.getSurfaceTexture {
            initPlayer(path, Surface(it), true)
        }
        render.addDrawer(drawer)

    }


    private fun initSecondVideo() {
        val drawer = VideoDrawer()
        //设置半透明的值
        drawer.setAlpha(0.5f)
        drawer.setVideoSize(1920, 1080)
        drawer.getSurfaceTexture {
            initPlayer(path2, Surface(it), false)
        }
        render.addDrawer(drawer)

        //设置绘制器，用于触摸移动
        gl_surface1.addDrawer(drawer)

        // 设置缩放系数
        Handler().postDelayed({
            drawer.scale(0.5f, 0.5f)
        }, 1000)
    }

    private fun initPlayer(path: String, surface: Surface, withSound: Boolean) {
        val videoDecoder = VideoDecoder(path, null, surface)
        threadPool.execute(videoDecoder)
        videoDecoder.resume()

        if (withSound) {
            val audioDecoder = AudioDecoder(path)
            threadPool.execute(audioDecoder)
            audioDecoder.resume()
        }
    }

    private fun initRender() {
        gl_surface1.setEGLContextClientVersion(2)
        gl_surface1.setRenderer(render)
    }

}