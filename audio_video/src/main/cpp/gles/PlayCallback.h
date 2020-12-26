//
// Created by ming lv on 12/26/20.
//

#ifndef AVSAMPLE_PLAYCALLBACK_H
#define AVSAMPLE_PLAYCALLBACK_H

#include <jni.h>

class PlayCallback {
public:
    PlayCallback(JavaVM *javaVM, JNIEnv *env, jobject job);
    ~PlayCallback();

    void onSucceed(const char*);
    void onError(const char*);
    void toJavaMessage(const char *message);

private:
    JavaVM *javaVm = 0;
    JNIEnv *env = 0;
    jobject instance;

    jmethodID jmd_showMessage;

};


#endif //AVSAMPLE_PLAYCALLBACK_H
