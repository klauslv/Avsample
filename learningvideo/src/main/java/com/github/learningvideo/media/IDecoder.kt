package com.github.learningvideo.media

import android.media.MediaFormat

/**
 *
 * Created by lvming on 1/8/21 11:43 AM.
 * Email: lvming@guazi.com
 * Description:解码器定义
 * 把整个解码流程抽象为一个解码基类
 *
 * 为什么要继承runnable，这里使用的是同步解码模式，需要不断循环压入和拉取数据，是一个耗时操作，因此，
 * 我们将解码器定义为一个runnable，最后放到线程池中执行
 */
interface IDecoder : Runnable {
    /**
     * 暂停解码
     */
    fun pause()

    /**
     * 继续解码
     */
    fun resume()

    /**
     * 停止解码
     */
    fun stop()

    /**
     * 是否正在解码
     */
    fun isDecoding(): Boolean

    /**
     *
     * @param pos Long 毫秒
     * @return Long 毫秒
     */
    fun seekTo(pos: Long): Long

    /**
     * 跳转到指定位置，并播放
     * 并返回实际帧的时间
     * @param pos Long 毫秒
     * @return Long 毫秒
     */
    fun seekAndPlay(pos: Long): Long

    /**
     * 是否正在快进
     * @return Boolean
     */
    fun isSeeking(): Boolean

    /**
     * 设置尺寸监听器
     * @param l IDecoderProgress
     */
    fun setSizeListener(l: IDecoderProgress)

    /**
     * 设置状态监听器
     * @param l IDecoderStateListener
     */
    fun setStateListener(l: IDecoderStateListener)

    /**
     * 获取视频宽
     * @return int
     */
    fun getWidth(): Int

    /**
     * 获取视频长度
     * @return Int
     */
    fun getHeight(): Int

    /**
     * 获取当前帧时间，单位ms
     * @return Long
     */
    fun getCurTimeStamp(): Long

    /**
     * 获取视频长度
     * @return Long
     */
    fun getDuration(): Long

    /**
     * 获取视频旋转角度
     * @return Int
     */
    fun getRotationAngle(): Int

    /**
     * 获取音视频对应的参数格式
     * @return MediaFormat
     */
    fun getMediaFormat(): MediaFormat?

    /**
     * 获取音视频对应的媒体轨道
     * @return Int
     */
    fun getTrack(): Int

    /**
     * 获取解码文件的路径
     * @return String
     */
    fun getFilePath(): String

    /**
     * 无需音视频同步
     * @return IDecoder
     */
    fun withoutSync(): IDecoder

    /**
     * 是否停止解码
     * @return Boolean
     */
    fun isStop(): Boolean
}