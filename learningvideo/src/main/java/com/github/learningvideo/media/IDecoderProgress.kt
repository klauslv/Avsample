package com.github.learningvideo.media

/**
 * Created by lvming on 1/8/21 12:04 PM.
 * Email: lvming@guazi.com
 * Description:解码进度
 */
interface IDecoderProgress {

    /**
     * 视频宽高回调
     * @param width Int
     * @param height Int
     * @param rotationAngle Int
     */
    fun videoSizeChange(width: Int, height: Int, rotationAngle: Int)

    /**
     * 视频播放进度回调
     * @param pos Long
     */

    fun videoProgressChange(pos: Long)

}