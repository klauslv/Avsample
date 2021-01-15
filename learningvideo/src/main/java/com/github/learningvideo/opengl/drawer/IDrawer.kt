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

    //设置视频的原始宽高
    fun setVideoSize(videW: Int, videH: Int)

    //设置OPenGL窗口的宽高
    fun setWorldSize(worldW: Int, worldH: Int)

    fun setAlpha(alpha: Float)
    fun draw()
    fun setTextureID(id: Int)

    //新增接口，用于提供SurfaceTexture
    fun getSurfaceTexture(cb: (st: SurfaceTexture) -> Unit) {}
    fun release()
}