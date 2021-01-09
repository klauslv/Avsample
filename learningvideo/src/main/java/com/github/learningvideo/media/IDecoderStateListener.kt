package com.github.learningvideo.media

/**
 * Created by lvming on 1/8/21 12:05 PM.
 * Email: lvming@guazi.com
 * Description:解码状态回调接口
 */
interface IDecoderStateListener {
    /**
     * 解码准备
     * @param decodeJob BaseDecoder?
     */
    fun decoderPrepare(decodeJob: BaseDecoder?)

    /**
     * 解码器准备就绪
     * @param decodeJob BaseDecoder?
     */
    fun decoderReady(decodeJob: BaseDecoder?)

    /**
     * 解码器正在执行
     * @param decodeJob BaseDecoder?
     */
    fun decoderRunning(decodeJob: BaseDecoder?)

    /**
     * 解码器暂停
     * @param decodeJob BaseDecoder?
     */
    fun decoderPause(decodeJob: BaseDecoder?)

    /**
     * 解码具体某一帧
     * @param decodeJob BaseDecoder?
     * @param frame Frame
     */
    fun decodeOneFrame(decodeJob: BaseDecoder?, frame: Frame)

    /**
     * 解码完成
     * @param decodeJob BaseDecoder?
     */
    fun decoderFinish(decodeJob: BaseDecoder?)

    /**
     * 销毁解码器
     * @param decodeJob BaseDecoder?
     */
    fun decoderDestroy(decodeJob: BaseDecoder?)

    /**
     * 解码器错误
     * @param decodeJob BaseDecoder?
     * @param msg String
     */
    fun decoderError(decodeJob: BaseDecoder?, msg: String)

}