//
// Created by ming lv on 12/26/20.
//

#include "PlayCallback.h"

PlayCallback::~PlayCallback() {

}

PlayCallback::PlayCallback(JavaVM *javaVM, JNIEnv *env, jobject job) {
    this->javaVm = javaVM;
    this->env = env;
    this->instance = env->NewGlobalRef(job);
}

void PlayCallback::onSucceed(const char *message) {
    toJavaMessage(message);
}

void PlayCallback::onError(const char *message) {
    toJavaMessage(message);
}

void PlayCallback::toJavaMessage(const char *message) {
    jclass videoPlayClass = this->env->GetObjectClass(instance);
    this->jmd_showMessage = this->env->GetMethodID(videoPlayClass,"showMessage","(Ljava/lang/String;)V");

    jstring string = env->NewStringUTF(message);
    this->env->CallVoidMethod(instance,jmd_showMessage,string);
}


