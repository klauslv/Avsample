//
// Created by ming lv on 12/26/20.
//

#include "video_play.h"
#include<jni.h>
#include "gles_play.h"
#include "PlayCallback.h"

const JavaVM *javaVm = 0;
Gles_play *gles_play = 0;

int JNI_OnLoad(JavaVM *javaVm, void *pVoid) {
    ::javaVm = javaVm;
    return JNI_VERSION_1_6;/// 坑，这里记得一定要返回，和异步线程指针函数一样（记得返回）
}

extern "C"
JNIEXPORT void JNICALL
Java_com_github_audio_1video_video_YUVPlay_nativeGlesPlay(JNIEnv *env, jobject instance,
                                                          jstring yuv420_path, jobject surface) {
    const char *yuv420path = env->GetStringUTFChars(yuv420_path, 0);

    LOGD("11111111");
    PlayCallback *playCallback = new PlayCallback(const_cast<JavaVM *>(javaVm), env, instance);
    LOGD("2222222");
    gles_play = new Gles_play(env, instance, playCallback, yuv420path, surface);
    LOGD("33333333");
    //这里prepare内部会开启一个子线程，由于开启会造成堆栈溢出，固取消了JNI中开启
//    gles_play->prepare();
    gles_play->start();
    LOGD("44444");
    env->ReleaseStringUTFChars(yuv420_path, yuv420path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_github_audio_1video_video_YUVPlay_nativeWindowPlay(JNIEnv *env, jobject thiz,
                                                            jstring yuv420_path, jobject surface) {
    const char *yuv420path = env->GetStringUTFChars(yuv420_path, 0);
    //todo
    env->ReleaseStringUTFChars(yuv420_path, yuv420path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_github_audio_1video_video_YUVPlay_onDestroy(JNIEnv *env, jobject thiz) {
    if (gles_play) {
        gles_play->release();
    }
}