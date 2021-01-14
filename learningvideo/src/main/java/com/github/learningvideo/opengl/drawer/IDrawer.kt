package com.github.learningvideo.opengl.drawer

import android.graphics.SurfaceTexture
import android.view.Surface
import java.nio.channels.FileLock

/**
 * Created by lvming on 1/11/21 2:58 PM.
 * Email: lvming@guazi.com
 * Description: 渲染器
 */
interface IDrawer {

    fun setVideoSize(videW: Int, videH: Int)
    fun setWorldSize(worldW: Int, worldH: Int)
    fun setAlpha(alpha: Float)
    fun draw()
    fun setTextureID(id: Int)
    fun getSurfaceTexture(cb: (st: SurfaceTexture) -> Unit) {}
    fun release()
}