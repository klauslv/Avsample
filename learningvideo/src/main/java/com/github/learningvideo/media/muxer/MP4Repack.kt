package com.github.learningvideo.media.muxer

import android.media.MediaCodec
import android.util.Log
import com.github.learningvideo.media.extractor.AudioExtractor
import com.github.learningvideo.media.extractor.VideoExtractor
import java.nio.ByteBuffer


/**
 * Created by lvming on 1/11/21 10:20 AM.
 * Email: lvming@guazi.com
 * Description:MP4重新打包工具
 */
class MP4Repack(path: String) {

    private val TAG = "MP4Repack"

    //初始化音视频分离器
    private val mAExtractor: AudioExtractor = AudioExtractor(path)
    private val mVExtractor: VideoExtractor = VideoExtractor(path)

    //初始化封装器
    private val mMuxer: MMuxer = MMuxer()

    /**
     * 启动重封装
     */
    fun start() {
        val audioFormat = mAExtractor.getFormat()
        val videoFormat = mVExtractor.getFormat()

        //判断是否有音频数据，没有音频数据则告诉封装器，忽略音频轨道
        if (audioFormat != null) {
            mMuxer.addAudioTrack(audioFormat)
        } else {
            mMuxer.setNoAudio()
        }

        //判断是否有视频数据，没有视频数据就告诉封装器，忽略视频轨道
        if (videoFormat != null) {
            mMuxer.addVideoTrack(videoFormat)
        } else {
            mMuxer.setNoVideo()
        }

        //启动线程
        Thread {
            val buffer = ByteBuffer.allocate(500 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()

            //音频分离和写入
            if (audioFormat != null) {
                var size = mAExtractor.readBuffer(buffer)
//                第一个为offset，一般为0
//                第二个为数据大小，就是Extractor提取的当前帧的数据大小
//                第三个为当前帧对应的时间戳，这个时间戳非常重要，影响到视频能不能正常播放，通过Extractor获取
//                第四个为当前帧类型，如视频I/P/B帧，也可通过Extractor获取
                while (size > 0) {
                    bufferInfo.set(0, size, mAExtractor.getCurrentTimeStamp(), mAExtractor.getSampleFlag())
                    mMuxer.writeAudioData(buffer, bufferInfo)
                    size = mAExtractor.readBuffer(buffer)
                }
            }
            //视频分离和写入
            if (videoFormat != null) {
                var size = mVExtractor.readBuffer(buffer)
                while (size > 0) {
                    bufferInfo.set(0, size, mVExtractor.getCurrentTimeStamp(), mVExtractor.getSampleFlag())
                    mMuxer.writeVideoData(buffer, bufferInfo)
                    size = mVExtractor.readBuffer(buffer)
                }
            }

            mAExtractor.stop()
            mVExtractor.stop()
            mMuxer.releaseAudioTrack()
            mMuxer.releaseVideoTrack()
            Log.i(TAG, "MP4重新打包完成")
        }.start()
    }
}