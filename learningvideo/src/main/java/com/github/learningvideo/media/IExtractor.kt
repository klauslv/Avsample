package com.github.learningvideo.media

import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * Created by lvming on 1/8/21 4:26 PM.
 * Email: lvming@guazi.com
 * Description:音视频分离器定义
 */
interface IExtractor {

    /**
     * 获取媒体数据格式
     * @return MediaFormat?
     */
    fun getFormat(): MediaFormat?

    /**
     * 读取音视频数据
     * @param byteBuffer ByteBuffer
     * @return Int
     */
    fun readBuffer(byteBuffer: ByteBuffer): Int

    /**
     * 获取当前帧时间
     * @return Long
     */
    fun getCurrentTimeStamp(): Long

    fun getSampleFlag(): Int

    /**
     * seek到指定位置，并返回实际帧的时间戳
     * @param pos Long
     * @return Long
     */
    fun seek(pos: Long): Long

    fun setStartPos(pos: Long)

    /**
     * 停止读取数据
     */
    fun stop()
}