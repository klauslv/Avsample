package com.github.learningvideo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
    }

    private fun requestPermission() {
        val permissions = Permission.Group.STORAGE
//        AndPermission.with(this)
//                .runtime()
//                .permission(permissions)
//                .onGranted {
//                }
//                .onDenied {
//                    Toast.makeText(this, "请打开权限，否则无法获取本地文件", Toast.LENGTH_SHORT).show()
//                }
//                .start()
    }

    fun clickSimplePlayer(view: View) {
        startActivity(Intent(this, VideoActivity::class.java))
    }

    fun clickSimpleTriangle(view: View) {
        val intent = Intent(this, SimpleRenderActivity::class.java)
        intent.putExtra("type", 0)
        startActivity(intent)
    }

    fun clickSimpleTexture(view: View) {
        val intent = Intent(this, SimpleRenderActivity::class.java)
        intent.putExtra("type", 1)
        startActivity(intent)
    }

    fun clickOpenGLPlayer(view: View) {
        startActivity(Intent(this, OpenGLPlayerActivity::class.java))
    }

    fun clickMultiOpenGLPlayer(view: View) {
        startActivity(Intent(this, MultiOpenGLPlayerActivity::class.java))
    }

    fun clickEGLPlayer(view: View) {}
    fun clickSoulPlayer(view: View) {}
    fun clickEncoder(view: View) {}
    fun clickFFmpegInfo(view: View) {}
    fun clickFFmpegGLPlayer(view: View) {}
    fun clickFFmpegRepack(view: View) {}
    fun clickFFmpegEncode(view: View) {}
}