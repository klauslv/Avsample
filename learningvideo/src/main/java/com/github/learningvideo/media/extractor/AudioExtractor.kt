package com.github.learningvideo.media.extractor

import android.media.MediaExtractor
import android.media.MediaFormat
import com.github.learningvideo.media.IExtractor
import java.nio.ByteBuffer
import java.security.cert.CertPath

/**
 * Created by lvming on 1/9/21 11:59 AM.
 * Email: lvming@guazi.com
 * Description:音频数据提取器
 */
class AudioExtractor(path: String) : IExtractor {

    private val mMediaExtractor = MMExtractor(path)

    override fun getFormat(): MediaFormat? {
        return mMediaExtractor.getAudioFormat()
    }

    override fun readBuffer(byteBuffer: ByteBuffer): Int {
        return mMediaExtractor.readBuffer(byteBuffer)
    }

    override fun getCurrentTimeStamp(): Long {
        return mMediaExtractor.getCurrentTimeStamp()
    }

    /**
     * 如视频I/P/B帧，也可通过Extractor获取
     * @return Int
     */
    override fun getSampleFlag(): Int {
        return mMediaExtractor.getSampleFlag()
    }

    override fun seek(pos: Long): Long {
        return mMediaExtractor.seek(pos)
    }

    override fun setStartPos(pos: Long) {
        return mMediaExtractor.setStartPos(pos)
    }

    override fun stop() {
        return mMediaExtractor.stop()
    }
}