package com.github.learningvideo.media

import android.media.MediaCodec
import java.nio.ByteBuffer

/**
 * Created by lvming on 1/8/21 5:07 PM.
 * Email: lvming@guazi.com
 * Description:一帧数据
 */
class Frame {

    var buffer: ByteBuffer? = null

    var bufferInfo = MediaCodec.BufferInfo()
        private set

    fun setBufferInfo(info: MediaCodec.BufferInfo) {
        bufferInfo.set(info.offset, info.size, info.presentationTimeUs, info.flags)
    }
}