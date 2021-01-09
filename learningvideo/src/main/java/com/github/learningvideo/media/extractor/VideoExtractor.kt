package com.github.learningvideo.media.extractor

import android.media.MediaExtractor
import android.media.MediaFormat
import com.github.learningvideo.media.IExtractor
import java.nio.ByteBuffer
import java.security.cert.CertPath

/**
 * Created by lvming on 1/9/21 12:06 PM.
 * Email: lvming@guazi.com
 * Description:视频数据提取器
 */
class VideoExtractor(path: String) : IExtractor {
    private val mMediaExtractor = MMExtractor(path)

    override fun getFormat(): MediaFormat? {
        return mMediaExtractor.getVideoFormat()
    }

    override fun readBuffer(byteBuffer: ByteBuffer): Int {
        return mMediaExtractor.readBuffer(byteBuffer)
    }

    override fun getCurrentTimeStamp(): Long {
        return mMediaExtractor.getCurrentTimeStamp()
    }

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
