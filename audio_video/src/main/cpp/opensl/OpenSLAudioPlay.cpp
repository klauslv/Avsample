//
// Created by ming lv on 12/25/20.
//
#include <pthread.h>
#include <cstring>
#include "OpenSLAudioPlay.h"

#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,"DevYK",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"DevYK",FORMAT,##__VA_ARGS__);

void playerCallback(SLAndroidSimpleBufferQueueItf bq, void *context);

OpenSLAudioPlay::OpenSLAudioPlay(int sampleRate, int sampleFormat, int channels)
        : mAudioEngine(new AudioEngine()), mPlayerObj(nullptr), mPlayer(nullptr),
          mBufferQueue(nullptr), mEffectSend(nullptr), mVolume(nullptr),
          mSampleRate((SLmilliHertz) sampleRate * 1000), mSampleFormat(sampleFormat),
          mChannels(channels), mBufSize(0), mIndex(0) {
    mMutex = PTHREAD_MUTEX_INITIALIZER;
    mBuffers[0] = nullptr;
    mBuffers[1] = nullptr;
}

OpenSLAudioPlay::~OpenSLAudioPlay() {

}

bool OpenSLAudioPlay::init() {
    SLresult result;

    //TODO 第三大步 创建播放器
    //3.1配置输入声音信息
    //创建buffer缓冲类型的队列 2个队列
    SLDataLocator_AndroidSimpleBufferQueue locBufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    //pcm数据格式
    // SL_DATAFORMAT_PCM：数据格式为pcm格式
    //2:双声道
    //SL_SAMPLINGRATE_44_1：采样率为44100（44.1赫兹 应用最广的，兼容性最好的）
    //SL_PCMSAMPLEFORMAT_FIXED_16：采样格式为16bit （16位）(2个字节)
    //SL_PCMSAMPLEFORMAT_FIXED_16：数据大小为16bit （16位）（2个字节）
    //SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT：左右声道（双声道）  （双声道 立体声的效果）
    //SL_BYTEORDER_LITTLEENDIAN：小端模式
    SLDataFormat_PCM formatPcm = {SL_DATAFORMAT_PCM, (SLuint32) mChannels, mSampleRate,
                                  (SLuint32) mSampleFormat,
                                  (SLuint32) mSampleFormat,
                                  mChannels == 2 ? 0 : SL_SPEAKER_FRONT_CENTER,
                                  SL_BYTEORDER_LITTLEENDIAN};
    if (mSampleRate) {
        formatPcm.samplesPerSec = mSampleRate;
    }

    //数据源 将上述配置信息放到这个数据源中
    SLDataSource audioSrc = {&locBufq, &formatPcm};

    //3.2配置音轨(输出)
    //设置混音器
    SLDataLocator_OutputMix locatorOutputMix = {SL_DATALOCATOR_OUTPUTMIX,
                                                mAudioEngine->outputMixObj};
    SLDataSink audioSink = {&locatorOutputMix, nullptr};

    /**
     * 创建audio player
     */
    //需要的接口 操作队列的接口
    const SLInterfaceID ids[3] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME, SL_IID_EFFECTSEND};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

    //3.3创建播放器
    result = (*mAudioEngine->engine)->CreateAudioPlayer(mAudioEngine->engine, &mPlayerObj,
                                                        &audioSrc, &audioSink, mSampleRate ? 2 : 3,
                                                        ids, req);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("CreateAudioPlayer failed:%d", result);
        return false;
    }

    //3.4初始化播放器：mPlayerObj
    result = (*mPlayerObj)->Realize(mPlayerObj, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("mPlayerObj Realize failed: %d", result);
        return false;
    }

    //3.5获取播放器接口：
    result = (*mPlayerObj)->GetInterface(mPlayerObj, SL_IID_PLAY, &mPlayer);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("mPlayerObj GetInterface failed:%d", result);
        return false;
    }

    //TODO 第四大步：设置播放回调函数
    //4.1获取播放器队列接口：SLAndroidSimpleBufferQueueItf mBufferQueue
    result = (*mPlayerObj)->GetInterface(mPlayerObj, SL_IID_BUFFERQUEUE, &mBufferQueue);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("mPlayerObj GetInterface failed:%d", result);
        return false;
    }
    //4.2设置回调 void playerCallback(SLAndroidSimpleBufferQueueItf bq, void *context)
    result = (*mBufferQueue)->RegisterCallback(mBufferQueue, playerCallback, this);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("mPlayerObj GetInterface failed:%d", result)
        return false;
    }

    mEffectSend = nullptr;
    if (mSampleRate == 0) {
        result = (*mPlayerObj)->GetInterface(mPlayerObj, SL_IID_EFFECTSEND, &mEffectSend);
        if (result != SL_RESULT_SUCCESS) {
            LOGE("mPlayerObj GetInterface failed:%d", result);
            return false;
        }
    }

    result = (*mPlayerObj)->GetInterface(mPlayerObj, SL_IID_VOLUME, &mVolume);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("mPlayerObj GetInterface failed:%d", result);
        return false;
    }

    //TODO 第五大步：设置播放器状态为播放状态
    result = (*mPlayer)->SetPlayState(mPlayer, SL_PLAYSTATE_PLAYING);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("mPlayerObj SetInterface failed:%d", result);
        return false;
    }

    return true;
}

//一帧音频播放完毕后就会回调这个函数
void playerCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    OpenSLAudioPlay *player = (OpenSLAudioPlay *) context;
    pthread_mutex_unlock(&player->mMutex);
}

void OpenSLAudioPlay::enqueueSample(void *data, size_t length) {
    //必须等待一帧音频播放完毕后才可以Enqueue 第二帧音频
    pthread_mutex_lock(&mMutex);
    if (mBufSize < length) {
        mBufSize = length;
        if (mBuffers[0]) {
            delete[] mBuffers[0];
        }
        if (mBuffers[1]) {
            delete[] mBuffers[1];
        }
        mBuffers[0] = new uint8_t[mBufSize];
        mBuffers[1] = new uint8_t[mBufSize];
    }
    memcpy(mBuffers[mIndex], data, length);
    //第六步：手动激活回调函数
    (*mBufferQueue)->Enqueue(mBufferQueue, mBuffers[mIndex], length);
    mIndex = 1 - mIndex;
}

/**
 * 释放资源
 */
void OpenSLAudioPlay::release() {
    pthread_mutex_unlock(&mMutex);
    if (mPlayerObj) {
        (*mPlayerObj)->Destroy(mPlayerObj);
        mPlayerObj = nullptr;
        mPlayer = nullptr;
        mBufferQueue = nullptr;
        mEffectSend = nullptr;
        mVolume = nullptr;
    }

    if (mAudioEngine) {
        delete mAudioEngine;
        mAudioEngine = nullptr;
    }

    if (mBuffers[0]) {
        delete[] mBuffers[0];
        mBuffers[0] = nullptr;
    }

    if (mBuffers[1]) {
        delete[] mBuffers[1];
        mBuffers[1] = nullptr;
    }
    pthread_mutex_unlock(&mMutex);
    pthread_mutex_destroy(&mMutex);
}


