package com.github.learningvideo.media.decoder

import com.github.learningvideo.media.BaseDecoder
import com.github.learningvideo.media.Frame
import com.github.learningvideo.media.IDecoderStateListener

/**
 * Created by lvming on 1/9/21 3:25 PM.
 * Email: lvming@guazi.com
 * Description: 默认解码状态监听器
 */
class DefDecodeStateListener : IDecoderStateListener {
    override fun decoderPrepare(decodeJob: BaseDecoder?) {
        TODO("Not yet implemented")
    }

    override fun decoderReady(decodeJob: BaseDecoder?) {
        TODO("Not yet implemented")
    }

    override fun decoderRunning(decodeJob: BaseDecoder?) {
        TODO("Not yet implemented")
    }

    override fun decoderPause(decodeJob: BaseDecoder?) {
        TODO("Not yet implemented")
    }

    override fun decodeOneFrame(decodeJob: BaseDecoder?, frame: Frame) {
        TODO("Not yet implemented")
    }

    override fun decoderFinish(decodeJob: BaseDecoder?) {
        TODO("Not yet implemented")
    }

    override fun decoderDestroy(decodeJob: BaseDecoder?) {
        TODO("Not yet implemented")
    }

    override fun decoderError(decodeJob: BaseDecoder?, msg: String) {
        TODO("Not yet implemented")
    }
}