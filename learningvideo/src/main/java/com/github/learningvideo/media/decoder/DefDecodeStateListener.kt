package com.github.learningvideo.media.decoder

import android.util.Log
import com.github.learningvideo.media.BaseDecoder
import com.github.learningvideo.media.Frame
import com.github.learningvideo.media.IDecoderStateListener

/**
 * Created by lvming on 1/9/21 3:25 PM.
 * Email: lvming@guazi.com
 * Description: 默认解码状态监听器
 */
class DefDecodeStateListener : IDecoderStateListener {
    private val TAG = "DefDecodeStateListener";
    override fun decoderPrepare(decodeJob: BaseDecoder?) {
        Log.d(TAG, "decoderPrepare")
    }

    override fun decoderReady(decodeJob: BaseDecoder?) {
        Log.d(TAG, "decoderReady")
    }

    override fun decoderRunning(decodeJob: BaseDecoder?) {
        Log.d(TAG, "decoderRunning")
    }

    override fun decoderPause(decodeJob: BaseDecoder?) {
        Log.d(TAG, "decoderPause")
    }

    override fun decodeOneFrame(decodeJob: BaseDecoder?, frame: Frame) {
        Log.d(TAG, "decodeOneFrame")
    }

    override fun decoderFinish(decodeJob: BaseDecoder?) {
        Log.d(TAG, "decoderFinish")
    }

    override fun decoderDestroy(decodeJob: BaseDecoder?) {
        Log.d(TAG, "decoderDestroy")
    }

    override fun decoderError(decodeJob: BaseDecoder?, msg: String) {
        Log.d(TAG, "decoderError")
    }
}