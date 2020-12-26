//
// Created by ming lv on 12/25/20.
//
#include <jni.h>
#include <pthread.h>
#include "OpenSLAudioPlay.h"

/**
 * 播放pcmFile
 */
FILE *pcmFile = 0;

/**
 * 定义一个opensl播放器
 */
OpenSLAudioPlay *slAudioPlay = nullptr;

/**
 * 是否正在播放
 */
bool isPlaying = false;

void *playThreadFunc(void *arg);

void *playThreadFunc(void *arg) {
    const int bufferSize = 2048;
    short buffer[bufferSize];
    while (isPlaying && !feof(pcmFile)) {
        fread(buffer, 1, bufferSize, pcmFile);
        slAudioPlay->enqueueSample(buffer, bufferSize);
    }
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_github_audio_1video_audio_AudioPlayActivity_nativePlayPcm(JNIEnv *env, jclass clazz,
                                                                   jstring pcm_path) {

    //将JAva传过来的字符串类型转换成C中的类型
    const char *_pcmPath = env->GetStringUTFChars(pcm_path, NULL);

    //如果已经实例化，就释放资源
    if (slAudioPlay) {
        slAudioPlay->release();
        delete slAudioPlay;
        slAudioPlay = nullptr;
    }

    //实例化 OpenSLAudioPlay
    slAudioPlay = new OpenSLAudioPlay(44100, SAMPLE_FORMAT_16, 1);
    slAudioPlay->init();
    pcmFile = fopen(_pcmPath, "r");
    isPlaying = true;
    pthread_t playThread;
    pthread_create(&playThread, nullptr, playThreadFunc, 0);

    env->ReleaseStringUTFChars(pcm_path, _pcmPath);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_github_audio_1video_audio_AudioPlayActivity_nativeStopPcm(JNIEnv *env, jclass clazz) {
    isPlaying = false;
    if (slAudioPlay) {
        slAudioPlay->release();
        delete slAudioPlay;
        slAudioPlay = nullptr;
    }

    if (pcmFile) {
        fclose(pcmFile);
        pcmFile = nullptr;
    }
}