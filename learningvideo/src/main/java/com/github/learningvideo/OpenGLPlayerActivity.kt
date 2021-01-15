package com.github.learningvideo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Surface
import com.github.learningvideo.media.decoder.AudioDecoder
import com.github.learningvideo.media.decoder.VideoDecoder
import com.github.learningvideo.opengl.drawer.IDrawer
import com.github.learningvideo.opengl.drawer.VideoDrawer
import com.github.learningvideo.opengl.egl.SimpleRender
import kotlinx.android.synthetic.main.activity_open_g_l_player.*
import java.util.concurrent.Executors

class OpenGLPlayerActivity : AppCompatActivity() {
    val path = Environment.getExternalStorageDirectory().absolutePath + "/mvtest.mp4"
    lateinit var drawer: IDrawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_g_l_player)

        initRender()
    }

    private fun initRender() {
        drawer = VideoDrawer()
        drawer.getSurfaceTexture {
            //使用SurfaceTexture初始化一个surface，并传递给MediaCodec使用
            initPlayer(Surface(it))
        }

        gl_surface.setEGLContextClientVersion(2)
        val render = SimpleRender()
        render.addDrawer(drawer)
        gl_surface.setRenderer(render)
    }

    private fun initPlayer(surface: Surface) {
        val threadPool = Executors.newFixedThreadPool(10)

        val videoDecoder = VideoDecoder(path, null, surface)
        threadPool.execute(videoDecoder)

        val audioDecoder = AudioDecoder(path)
        threadPool.execute(audioDecoder)

        videoDecoder.resume()
        audioDecoder.resume()
    }
}