package com.github.learningvideo.media

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import java.io.File
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by lvming on 1/8/21 4:16 PM.
 * Email: lvming@guazi.com
 * Description:解码器基类
 */
abstract class BaseDecoder(private val mFilePath: String) : IDecoder {
    private val TAG = "BaseDecoder"
    //------------线程相关----------------------

    /**
     * 解码器是否正在运行
     */
    private var mIsRunning = true

    /**
     * 线程等待锁
     */
    private var mLock = Object()

    /**
     * 是否可以进入解码
     */
    private var mReadyForDecoder = false

    //---------------状态相关--------------------------

    /**
     * 音频解码器
     */
    private var mCodec: MediaCodec? = null

    /**
     * 音视频数据读取器，kotlin属性默认不是null，如果可以为null需要后面加？
     */
    private var mExtractor: IExtractor? = null

    /**
     * 解码输入缓存区
     */
    private var mInputBuffers: Array<ByteBuffer>? = null;

    /**
     * 解码输出缓存区
     */
    private var mOutputBuffers: Array<ByteBuffer>? = null

    /**
     * 解码数据信息
     */
    private var mBufferInfo = MediaCodec.BufferInfo()

    private var mState = DecodeState.STOP

    protected var mStateListener: IDecoderStateListener? = null

    /**
     * 流数据是否结束
     */
    private var mIsEOS = false

    protected var mVideoWidth = 0

    protected var mVideoHeight = 0

    private var mDuration: Long = 0
    private var mStartPos: Long = 0
    private var mEndPos: Long = 0

    /**
     * 开始解码时间，用于音视频同步
     */
    private var mStartTimeForSync = -1L

    //是否需要音视频渲染同步
    private var mSyncRender = true

    final override fun run() {
        if (mState == DecodeState.STOP) {
            mState = DecodeState.START
        }

        //回调状态
        mStateListener?.decoderPrepare(this)

        //【解码步骤：1.初始化，并启动解码器】
        if (!init()) return

        Log.i(TAG, "开始解码")

        while (mIsRunning) {
            if (mState != DecodeState.START &&
                    mState != DecodeState.DECODING &&
                    mState != DecodeState.SEEKING) {
                Log.i(TAG, "进入等待：$mState")

                waitDecode()

                //--------------同步时间矫正------------------
                //恢复同步的起始时间，即去除等待流失的时间
                mStartTimeForSync = System.currentTimeMillis() - getCurTimeStamp()
            }

            if (!mIsRunning ||
                    mState == DecodeState.STOP) {
                mIsRunning = false
                break
            }

            if (mStartTimeForSync == -1L) {
                mStartTimeForSync = System.currentTimeMillis()
            }

            //如果数据没有解码完毕，将数据推入解码器解码
            if (!mIsEOS) {
                //解码步骤：2、将数据压入解码器输入缓冲
                mIsEOS = pushBufferFromDecoder()
            }

            //解码步骤3、将解码好的数据从缓冲区拉取出来
            val index = pullBufferFromDecoder()
            if (index >= 0) {
                //解码步骤4、渲染
                render(mOutputBuffers!![index], mBufferInfo)
                //解码步骤5、释放输出缓冲
                //第二个参数，是个boolean，命名为render，这个参数在视频解码时，用于决定是否要将这一帧数据显示出来
                mCodec!!.releaseOutputBuffer(index, true)
                if (mState == DecodeState.START) {
                    mState = DecodeState.PAUSE
                }
            }

            //解码步骤6、判断是否解码完成
            //当接收到这个标志后，解码器就知道所有数据已经接收完毕，在所有数据解码完成以后，会在最后一帧数据加上结束标记信息，
            if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                mState = DecodeState.FINISH
                mStateListener?.decoderFinish(this)
            }
        }
        doneDecode()
        //解码步骤7、释放解码器
        //在while循环结束后，释放掉所有的资源。至此，一次解码结束
        release()
    }

    /**
     * 释放资源
     */
    private fun release() {
        try {
            mState = DecodeState.STOP
            mIsEOS = false
            mExtractor?.stop()
            mCodec?.stop()
            mCodec?.release()
            mStateListener?.decoderDestroy(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 结束解码
     */
    abstract fun doneDecode()

    /**
     * 调用了一个虚函数render，也就是将渲染交给子类
     * @param byteBuffer ByteBuffer
     * @param mBufferInfo BufferInfo
     */
    abstract fun render(byteBuffer: ByteBuffer, mBufferInfo: MediaCodec.BufferInfo)

    /**
     * 将解码好的数据从缓冲区拉出来
     * @return Int
     */
    private fun pullBufferFromDecoder(): Int {
        //查询是否有解码完成的数据，index>=0时表示数据有效，并且index为缓冲区索引
//        调用dequeueOutputBuffer方法查询是否有解码完成的可用数据，其中mBufferInfo用于获取数据帧信息，
//        第二参数是等待时间，这里等待1000ms，填入-1是无限等待
        var index = mCodec!!.dequeueOutputBuffer(mBufferInfo, 1000)
//        判断index类型
        when (index) {
            //输出格式改变了
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
            }
            //没有可用的数据等会再来
            MediaCodec.INFO_TRY_AGAIN_LATER -> {
            }
            //输入缓冲改变了
            MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                mOutputBuffers = mCodec!!.outputBuffers
            }
            //大于等于0：有可用数据，index就是输出缓冲索引
            else -> {
                return index
            }
        }
        return -1;
    }

    /**
     * 将数据压入解码器
     * @return Boolean
     */
    private fun pushBufferFromDecoder(): Boolean {
        //查询是否有可用的输入缓冲，返回缓冲索引。其中参数2000为等待2000ms，如果填入-1则无限等待
        var inputBufferIndex = mCodec!!.dequeueInputBuffer(1000)
        var isEndOfStream = false
        if (inputBufferIndex >= 0) {
//            通过缓冲索引 inputBufferIndex 获取可用的缓冲区，并使用Extractor提取待解码数据，填充到缓冲区中。
            val inputBuffer = mInputBuffers!![inputBufferIndex]
            val sampleSize = mExtractor!!.readBuffer(inputBuffer)

//            如果SampleSize返回-1，说明没有更多的数据了
            if (sampleSize < 0) {
                //如果数据已经取完，压入数据结束标志：MediaCodec.BUFFER_FLAG_END_OF_STREAM
                mCodec!!.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                isEndOfStream = true
            } else {
//                调用queueInputBuffer将数据压入解码器
                mCodec!!.queueInputBuffer(inputBufferIndex, 0, sampleSize, mExtractor!!.getCurrentTimeStamp(), 0)
            }
        }
        return isEndOfStream
    }


    private fun init(): Boolean {
        //1、检查参数是否完整
        if (mFilePath.isEmpty() || !File(mFilePath).exists()) {
            Log.w(TAG, "文件路径为空")
            mStateListener?.decoderError(this, "文件路径为空")
            return false
        }
        //调用虚函数，检查子类参数是否完整
        if (!check()) return false

        //2、初始化数据提取器
        mExtractor = initExtractor(mFilePath)
        if (mExtractor == null || mExtractor!!.getFormat() == null) {
            return false
        }

        //3.初始化参数
        if (!initParams()) return false

        //4.初始化渲染器
        if (!initRender()) return false

        //初始化解码器
        if (!initCodec()) return false

        return true
    }


    private fun initParams(): Boolean {
        try {
            val format = mExtractor!!.getFormat()!!
            mDuration = format.getLong(MediaFormat.KEY_DURATION) / 1000
            if (mEndPos == 0L) mEndPos = mDuration
            initSpecParams(mExtractor!!.getFormat()!!)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun initCodec(): Boolean {

        try {
            //1.根据音视频编码格式初始化解码器
            //首先，通过Extractor获取到音视频数据的编码信息MediaFormat；
            //然后，查询MediaFormat中的编码类型（如video/avc，即H264；audio/mp4a-latm，即AAC）；
            val type = mExtractor!!.getFormat()!!.getString(MediaFormat.KEY_MIME)!!
            //最后，调用createDecoderByType创建解码器。
            mCodec = MediaCodec.createDecoderByType(type)
            //2.配置接麦器
            if (!configCodec(mCodec!!, mExtractor!!.getFormat()!!)) {
                waitDecode()
            }
            //3.启动解码器
            mCodec!!.start()

            //4.获取解码器缓冲区
            mInputBuffers = mCodec?.inputBuffers
            mOutputBuffers = mCodec?.outputBuffers
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * 解码线程进入等待
     */
    private fun waitDecode() {
        try {
            if (mState == DecodeState.PAUSE) {
                mStateListener?.decoderPause(this)
            }
            synchronized(mLock) {
                mLock.wait()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 通知解码线程继续运行
     */
    protected fun notifyDecoder() {
        synchronized(mLock) {
            mLock.notifyAll()
        }
        if (mState == DecodeState.DECODING) {
            mStateListener?.decoderRunning(this)
        }
    }

    /**
     * 检查子类参数
     * @return Boolean
     */
    abstract fun check(): Boolean

    /**
     * 初始化数据提取器
     * @param mFilePath String
     * @return IExtractor?
     */
    abstract fun initExtractor(mFilePath: String): IExtractor?

    /**
     * 初始化子类自己特有的参数
     * @param format MediaFormat
     */
    abstract fun initSpecParams(format: MediaFormat)

    /**
     * 初始化渲染器
     * @return Boolean
     */
    abstract fun initRender(): Boolean

    /**
     * 配置解码器
     */
    abstract fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean

    /**
     * 暂停解码
     */
    override fun pause() {
        mState = DecodeState.PAUSE
    }

    /**
     * 继续解码
     */
    override fun resume() {
        mState = DecodeState.DECODING
        notifyDecoder()
    }

    override fun seekTo(pos: Long): Long {
        return 0
    }

    override fun seekAndPlay(pos: Long): Long {
        return 0
    }

    override fun isDecoding(): Boolean {
        return mState == DecodeState.DECODING
    }

    override fun isSeeking(): Boolean {
        return mState == DecodeState.SEEKING
    }

    override fun isStop(): Boolean {
        return mState == DecodeState.STOP
    }

    override fun setSizeListener(l: IDecoderProgress) {
        TODO("Not yet implemented")
    }

    override fun setStateListener(l: IDecoderStateListener) {
        TODO("Not yet implemented")
    }

    override fun getWidth(): Int {
        return mVideoWidth
    }

    override fun getHeight(): Int {
        return mVideoHeight
    }

    override fun getDuration(): Long {
        return mDuration
    }

    override fun getCurTimeStamp(): Long {
        return mBufferInfo.presentationTimeUs / 1000
    }

    override fun getRotationAngle(): Int {
        return 0
    }

    override fun getMediaFormat(): MediaFormat? {
        return mExtractor?.getFormat()
    }

    override fun getTrack(): Int {
        return 0
    }

    override fun getFilePath(): String {
        return mFilePath
    }

    override fun withoutSync(): IDecoder {
        mSyncRender = false
        return this
    }
}