package com.github.learningvideo.media.extractor

import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import java.nio.ByteBuffer


/**
 * Created by lvming on 1/9/21 11:15 AM.
 * Email: lvming@guazi.com
 * Description:音视频分离器
 */
class MMExtractor(path: String?) {

    /**
     * 音视频分离器
     */
    private var mExtractor: MediaExtractor? = null

    /**
     * 音频通道索引
     */
    private var mAudioTrack = -1

    /**
     * 视频通道索引
     */
    private var mVideoTrack = -1

    /**
     * 当前帧时间戳
     */
    private var mCurSampleTime: Long = 0

    /**
     * 当前帧标志
     */
    private var mCurSampleFlag: Int = 0

    /**
     * 开始解码时间点
     */
    private var mStartPos: Long = 0

    init {
        //初始化
//        新建，然后设置音视频文件路径
        mExtractor = MediaExtractor()
        mExtractor?.setDataSource(path!!)
    }

    /**
     * 获取视频格式参数
     * @return MediaFormat?
     */
    fun getVideoFormat(): MediaFormat? {
        //2.1获取视频多媒体格式
//        遍历视频文件中所有的通道，一般是音频和视频两个通道；
//        然后获取对应通道的编码格式，判断是否包含"video/"或者"audio/"开头的编码格式；
//        最后通过获取的索引，返回对应的音视频多媒体格式信息。
        for (i in 0 until mExtractor!!.trackCount) {
            val mediaFormat = mExtractor!!.getTrackFormat(0)
            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith("video/")) {
                mVideoTrack = i
                break
            }
        }
        return if (mVideoTrack >= 0)
            mExtractor!!.getTrackFormat(mVideoTrack)
        else null
    }

    /**
     * 获取音频格式参数
     * @return MediaFormat?
     */
    fun getAudioFormat(): MediaFormat? {
        //2.2获取音频多媒体格式
        for (i in 0 until mExtractor!!.trackCount) {
            val mediaFormat = mExtractor!!.getTrackFormat(i)
            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith("audio/")) {
                mAudioTrack = i
                break
            }
        }

        return if (mAudioTrack >= 0)
            mExtractor!!.getTrackFormat(mAudioTrack)
        else null
    }

    /**
     * 读取视频数据
     * @param byteBuffer ByteBuffer
     * @return Int
     */
    fun readBuffer(byteBuffer: ByteBuffer): Int {
        //3、提取数据
        byteBuffer.clear()
        selectSourceTrack()
//        readBuffer(byteBuffer: ByteBuffer)中的参数就是解码器传进来的，用于存放待解码数据的缓冲区。
//        将返回读取到的音视频数据流的大小，小于0表示数据已经读完
        var readSampleCount = mExtractor!!.readSampleData(byteBuffer, 0)
        if (readSampleCount < 0) {
            return -1
        }
//        进入下一帧：先记录当前帧的时间戳，然后调用advance进入下一帧，这时读取指针将自动移动到下一帧开头。
        //记录当前帧的时间戳
        mCurSampleTime = mExtractor!!.sampleTime
        //进入下一帧
        mExtractor!!.advance()
        return readSampleCount

    }

    /**
     * 选择通道
     */
    private fun selectSourceTrack() {
//        根据当前选择的通道（同时只选择一个音/视频通道），调用mExtractor!!.selectTrack(mAudioTrack)将通道切换正确。
        if (mVideoTrack >= 0) {
            mExtractor!!.selectTrack(mVideoTrack)
        } else if (mAudioTrack >= 0) {
            mExtractor!!.selectTrack(mAudioTrack)
        }
    }

    /**
     * seek到指定位置，并返回实际帧的时间戳
     *
     * seek(pos: Long)方法，主要用于跳播，快速将数据定位到指定的播放位置，但是，由于视频中，除了I帧以外，
     * PB帧都需要依赖其他的帧进行解码，所以，通常只能seek到I帧，但是I帧通常和指定的播放位置有一定误差，
     * 因此需要指定seek靠近哪个关键帧，有以下三种类型：
     * SEEK_TO_PREVIOUS_SYNC：跳播位置的上一个关键帧
     * SEEK_TO_NEXT_SYNC：跳播位置的下一个关键帧
     * SEEK_TO_CLOSEST_SYNC：距离跳播位置的最近的关键帧
     * @param pos Long
     * @return Long
     */
    fun seek(pos: Long): Long {
        mExtractor!!.seekTo(pos, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        return mExtractor!!.sampleTime
    }

    /**
     * 停止读取数据
     */
    fun stop() {
        //释放提取器
//        客户端退出解码的时候，需要调用stop释放提取器相关资源
        mExtractor?.release()
        mExtractor = null
    }

    fun getVideoTrack(): Int {
        return mVideoTrack
    }

    fun getAudioTrack(): Int {
        return mAudioTrack
    }

    fun setStartPos(pos: Long) {
        mStartPos = pos
    }

    /**
     * 获取当前帧时间
     * @return Long
     */
    fun getCurrentTimeStamp(): Long {
        return mCurSampleTime
    }

    fun getSampleFlag(): Int {
        return mCurSampleFlag
    }
}