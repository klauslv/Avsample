package com.github.learningvideo.media.decoder

import android.media.*
import com.github.learningvideo.media.BaseDecoder
import com.github.learningvideo.media.IExtractor
import com.github.learningvideo.media.extractor.AudioExtractor
import java.lang.Exception
import java.nio.ByteBuffer

/**
 * Created by lvming on 1/9/21 2:29 PM.
 * Email: lvming@guazi.com
 * Description: 音频解码器
 */
class AudioDecoder(path: String) : BaseDecoder(path) {

    /**
     * 采样率
     */
    private var mSampleRate = -1;

    /**
     * 声音通道数量
     */
    private var mChannels = 1

    /**
     * PCM 采样位数
     */
    private var mPCMEncodeBit = AudioFormat.ENCODING_PCM_16BIT

    /**
     * 音频播放器
     */
    private var mAudioTrack: AudioTrack? = null

    /**
     * 音频数据缓存
     */
    private var mAudioOutTempBuf: ShortArray? = null


    override fun doneDecode() {
        mAudioTrack?.stop()
        mAudioTrack?.release()
    }

    override fun render(outputBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        if (mAudioOutTempBuf!!.size < bufferInfo.size / 2) {
            mAudioOutTempBuf = ShortArray(bufferInfo.size / 2)
        }

        outputBuffer.position(0)
        outputBuffer.asShortBuffer().get(mAudioOutTempBuf, 0, bufferInfo.size / 2)
        mAudioTrack!!.write(mAudioOutTempBuf!!, 0, bufferInfo.size / 2)
    }

    override fun check(): Boolean {
        return true
    }

    override fun initExtractor(mFilePath: String): IExtractor? {
        return AudioExtractor(mFilePath)
    }

    override fun initSpecParams(format: MediaFormat) {
        try {
            mChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            mPCMEncodeBit = if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                format.getInteger(MediaFormat.KEY_PCM_ENCODING)
            } else {
                //如果没有这个参数，默认为16位采样
                AudioFormat.ENCODING_PCM_16BIT
            }
        } catch (e: Exception) {

        }
    }

    override fun initRender(): Boolean {
        val channel = if (mChannels == 1) {
            //单声道
            AudioFormat.CHANNEL_OUT_MONO
        } else {
            //双声道
            AudioFormat.CHANNEL_OUT_STEREO
        }

        //获取最小的缓冲区
//        根据通道数量配置单声道和双声道
//        根据采样率、通道数、采样位数计算获取最小缓冲区
        val minBufferSize = AudioTrack.getMinBufferSize(mSampleRate, channel, mPCMEncodeBit)

        mAudioOutTempBuf = ShortArray(minBufferSize / 2)

//        音频播放需要获取采样率，通道数，采样位数等
        mAudioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,//播放类型，音乐
                mSampleRate,//采样率
                channel,//通道
                mPCMEncodeBit,//采样位数
                minBufferSize,//缓冲区大小
                AudioTrack.MODE_STREAM//播放模式：数据流动态写入，另一种是一次性写入
        )
//        有一点注意的点是，需要把解码数据由ByteBuffer类型转换为ShortBuffer，这时Short数据类型的长度要减半
        mAudioTrack!!.play()
        return true
    }

    /**
     * 初始化解码器
     * @param codec MediaCodec
     * @param format MediaFormat
     * @return Boolean
     */
    override fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
        //音频不需要surface,直接传null
        codec.configure(format, null, null, 0)
        return true
    }

    override fun stop() {
        TODO("Not yet implemented")
    }
}