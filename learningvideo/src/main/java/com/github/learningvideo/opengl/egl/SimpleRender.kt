package com.github.learningvideo.opengl.egl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.github.learningvideo.opengl.drawer.IDrawer
import com.github.learningvideo.opengl.drawer.TriangleDrawer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 简单的OPENGl渲染器
 * 三个重写的接口在一个线程中，
 */
class SimpleRender : GLSurfaceView.Renderer {

    private val drawers = mutableListOf<IDrawer>()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //清屏，清屏颜色为黑色
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        //开启混合，即半透明
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        //创建了一个纹理ID，并设置给Drawer,如下
        val textureIds = OpenGLTools.createTextureIds(drawers.size)
        for ((idx, drawer) in drawers.withIndex()) {
            drawer.setTextureID(textureIds[idx])
        }
    }

    /**
     * 调用glViewPort,设置了OpenGL绘制的区域宽高和位置
     * 绘制区域是OPenGL在GLSurafaceView中的绘制区域，一般都是全部铺满
     * @param gl GL10
     * @param width Int
     * @param height Int
     */
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        for (drawer in drawers) {
            drawer.setWorldSize(width, height)
        }

    }

    /**
     * 真正实现绘制的地方，该接口会不停的回调，数显绘制区域。
     * @param gl GL10
     */
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        drawers.forEach {
            it.draw()
        }
    }

    fun addDrawer(drawer: IDrawer) {
        drawers.add(drawer)
    }

}
