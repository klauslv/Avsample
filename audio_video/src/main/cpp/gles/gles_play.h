//
// Created by ming lv on 12/26/20.
//

#ifndef AVSAMPLE_GLES_PLAY_H
#define AVSAMPLE_GLES_PLAY_H

//使用EGL需要添加头文件
#include <EGL/egl.h>
#include<EGL/eglext.h>

//使用OpenGL ES 2.0需要添加的头文件
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <jni.h>
#include <string.h>
#include <android/log.h>
#include <string>
#include <android/native_window_jni.h>
#include "PlayCallback.h"

#define LOGD(...) __android_log_print(ANDROID_LOG_WARN,"klaus",__VA_ARGS__)


class Gles_play {
public:
    Gles_play(JNIEnv *env, jobject thiz, PlayCallback *playCallback, const char *data_source,
              jobject pJobject);

    ~Gles_play();

    void playYUV(jobject surface);

    void prepare();

    void start();

    void release();

private:
    PlayCallback *playCallback = 0;

    void showMessage(JNIEnv *,const char*,bool);

    JNIEnv *env = 0;
    jobject thiz;
    pthread_t pid_prepare;
    char *data_source = 0;
    jobject surface;
    bool isPlay;
};


#endif //AVSAMPLE_GLES_PLAY_H
