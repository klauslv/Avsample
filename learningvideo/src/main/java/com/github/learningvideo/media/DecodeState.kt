package com.github.learningvideo.media

/**
 * Created by lvming on 1/8/21 4:43 PM.
 * Email: lvming@guazi.com
 * Description:解码状态枚举
 */
enum class DecodeState {

    /**
     * 开始状态
     */
    START,

    /**
     * 解码中
     */
    DECODING,

    /**
     * 解码暂停
     */
    PAUSE,

    /**
     * 正在快进
     */
    SEEKING,

    /**
     * 解码完成
     */
    FINISH,

    /**
     * 解码器释放
     */
    STOP
}