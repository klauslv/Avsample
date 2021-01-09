package com.github.learningvideo.media.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.github.learningvideo.media.BaseDecoder
import com.github.learningvideo.media.IExtractor
import com.github.learningvideo.media.extractor.VideoExtractor
import java.nio.ByteBuffer

/**
 * Created by lvming on 1/9/21 2:18 PM.
 * Email: lvming@guazi.com
 * Description: 视频解码器
 */
class VideoDecoder(path: String, sfv: SurfaceView?, surface: Surface?) : BaseDecoder(path) {

    private val TAG = "VideoDecoder"

    private val mSurfaceView = sfv
    private var mSurface = surface

    override fun doneDecode() {

    }

    override fun render(byteBuffer: ByteBuffer, mBufferInfo: MediaCodec.BufferInfo) {

    }

    /**
     * SurfaceView应该是大家比较熟悉的View了，最常使用的就是用来做MediaPlayer的显示。当然也可以绘制图片、动画等。
     * Surface应该不是很常用了，这里为了支持后续使用OpenGL来渲染视频，所以预先做了支持。
     * @return Boolean
     */
    override fun check(): Boolean {
        if (mSurfaceView == null && mSurface == null) {
            Log.w(TAG, "SurfaceView和Surface都为空，至少需要一个不为空")
            mStateListener?.decoderError(this, "显示器为空")
            return false
        }
        return true
    }

    /**
     * 生成数据提取器
     * @param mFilePath String
     * @return IExtractor?
     */
    override fun initExtractor(mFilePath: String): IExtractor? {
        return VideoExtractor(mFilePath)
    }

    override fun initSpecParams(format: MediaFormat) {
    }

    override fun initRender(): Boolean {
        return true
    }

    override fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
        if (mSurface != null) {
            //配置解码器
            codec.configure(format, mSurface, null, 0)
            notifyDecoder()
        } else if (mSurfaceView?.holder?.surface != null) {
            mSurface = mSurfaceView?.holder?.surface
            configCodec(codec, format)
        } else {
//            SurfaceView的创建是有一个时间过程的，并非马上可以使用，需要通过CallBack来监听它的状态
            mSurfaceView?.holder?.addCallback(object : SurfaceHolder.Callback2 {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    mSurface = holder.surface
                    configCodec(codec, format)
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {

                }

                override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
                }
            })
            return false
        }
        return true
    }

    override fun stop() {
    }
}