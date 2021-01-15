package com.github.learningvideo.opengl.drawer

import android.graphics.Shader
import android.graphics.SurfaceTexture
import android.opengl.ETC1Util
import android.opengl.GLES11Ext
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Created by lvming on 1/15/21 10:21 AM.
 * Email: lvming@guazi.com
 * Description:视频渲染器
 */
class VideoDrawer : IDrawer {

    /**
     * 顶点坐标
     */
    private val mVertexCoors = floatArrayOf(
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
    )

    /**
     * 纹理坐标
     */
    private val mTextureCoors = floatArrayOf(
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    )

    private var mTextureId: Int = -1

    //OpenGL程序ID
    private var mProgram: Int = -1

    //顶点坐标接收者
    private var mVertexPosHandler: Int = -1

    //纹理坐标接收者
    private var mTexturePosHandler: Int = -1

    //纹理接收者
    private var mTextureHandler: Int = -1

    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mTextureBuffer: FloatBuffer

    //视频的渲染需要通过SurfaceTexture来更新画面
    private var mSurfaceTexture: SurfaceTexture? = null

    private var mSftCb: ((SurfaceTexture) -> Unit)? = null

    init {
        //步骤1：初始化顶点坐标
        initPos()
    }

    private fun initPos() {
        val bb = ByteBuffer.allocateDirect(mVertexCoors.size * 4)
        bb.order(ByteOrder.nativeOrder())
        //将坐标数据转化为floatBuffer，用于传入给OpenGLES程序
        mVertexBuffer = bb.asFloatBuffer()
        mVertexBuffer.put(mVertexCoors)
        mVertexBuffer.position(0)

        val cc = ByteBuffer.allocateDirect(mTextureCoors.size * 4)
        cc.order(ByteOrder.nativeOrder())
        mTextureBuffer = cc.asFloatBuffer()
        mTextureBuffer.put(mTextureCoors)
        mTextureBuffer.position(0)
    }

    override fun setTextureID(id: Int) {
        mTextureId = id
        mSurfaceTexture = SurfaceTexture(id)
        mSftCb?.invoke(mSurfaceTexture!!)
    }

    override fun getSurfaceTexture(cb: (st: SurfaceTexture) -> Unit) {
        mSftCb = cb
    }
    override fun draw() {
        if (mTextureId != -1) {
            //步骤2：创建、编译、并启动OPenGL找色器
            createGLPrg()
            //步骤3、激活并绑定纹理单元
            activateTexture()
            //步骤4、绑定图片到纹理单元
            updateTexture()
            //步骤5、开始渲染绘制
            doDraw()
        }
    }

    /**
     * 创建、编译、并启动OPenGL着色器
     */
    private fun createGLPrg() {
        if (mProgram == -1) {
            val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, getVertexShader())
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader())

            //创建OPenGL Es 程序，注意：需要在OpenGL渲染线程中创建，否则无法渲染
            mProgram = GLES20.glCreateProgram()
            //将顶点着色器加入到程序
            GLES20.glAttachShader(mProgram, vertexShader)
            //将片元你着色器加入到程序中
            GLES20.glAttachShader(mProgram, fragmentShader)
            //链接到着色器程序
            GLES20.glLinkProgram(mProgram)

            mVertexPosHandler = GLES20.glGetAttribLocation(mProgram, "aPosition")
            mTextureHandler = GLES20.glGetUniformLocation(mProgram, "uTexture")
            mTexturePosHandler = GLES20.glGetAttribLocation(mProgram, "aCoordinate")
        }
        //使用OPenGL程序
        GLES20.glUseProgram(mProgram)
    }

    /**
     * 激活并绑定纹理单元
     */
    private fun activateTexture() {
        //激活指定的纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        //绑定纹理ID到纹理单元
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId)
        //将激活的纹理单元传递到着色器里面
        GLES20.glUniform1i(mTextureHandler, 0)
        //配置边缘过度参数
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
    }


    private fun updateTexture() {
        mSurfaceTexture?.updateTexImage()
    }

    private fun doDraw() {
        //启用顶点的句柄
        GLES20.glEnableVertexAttribArray(mVertexPosHandler)
        GLES20.glEnableVertexAttribArray(mTexturePosHandler)
        //设置折色器参数，第二个参数表示一个顶点包含的数据数量，这里为xy,所以为2
        GLES20.glVertexAttribPointer(mVertexPosHandler, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer)
        GLES20.glVertexAttribPointer(mTexturePosHandler, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer)
        //开始绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }


    /**
     * 根据类型加载对应的着色器
     * @param type Int 着色器类型 顶点还是片元
     * @param shaderCode String 着色器GLSL重点饿一段代码
     * @return Int 对应点着色器
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        //根据type创建顶点着色器或者片元着色器
        val shader = GLES20.glCreateShader(type)
        //将资源加入到着色器，bi能够编译
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        return shader
    }

    /**
     * 获取GLSL中顶点着色器的属性
     * @return String
     */
    private fun getVertexShader(): String {
        return "attribute vec4 aPosition;" +
                "attribute vec2 aCoordinate;" +
                "varying vec2 vCoordinate;" +
                "void main() {" +
                "    gl_Position = aPosition;" +
                "    vCoordinate = aCoordinate;" +
                "}"
    }

    private fun getFragmentShader(): String {
        //一定要加换行"\n"，否则会和下一行的precision混在一起，导致编译出错
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;" +
                "varying vec2 vCoordinate;" +
                "uniform samplerExternalOES uTexture;" +
                "void main() {" +
                "  gl_FragColor=texture2D(uTexture, vCoordinate);" +
                "}"
    }

    override fun setVideoSize(videW: Int, videH: Int) {

    }

    override fun setWorldSize(worldW: Int, worldH: Int) {

    }

    override fun setAlpha(alpha: Float) {

    }


    override fun release() {
        GLES20.glDisableVertexAttribArray(mVertexPosHandler)
        GLES20.glDisableVertexAttribArray(mTexturePosHandler)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDeleteTextures(1, intArrayOf(mTextureId), 0)
        GLES20.glDeleteProgram(mProgram)
    }
}