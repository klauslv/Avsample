//
// Created by ming lv on 12/25/20.
//

#ifndef AVSAMPLE_AUDIOENGINE_H
#define AVSAMPLE_AUDIOENGINE_H

#include <SLES/OpenSLES.h>
#include <stdio.h>
#include <android/log.h>
#include <assert.h>
#include <SLES/OpenSLES_Android.h>

class AudioEngine {
public:
    SLObjectItf engineObj;
    SLEngineItf engine;

    SLObjectItf outputMixObj;

private:
    void createEngine() {
        //音频的播放就涉及到了OpenSLES
        //TODO 第一大步：创建引擎并获取引擎接口
        //1、1创建引擎对象：SLObjectItf engineObj
        SLresult result = slCreateEngine(&engineObj, 0, NULL, 0, NULL, NULL);
        if (SL_RESULT_SUCCESS != result) {
            return;
        }

        //1.2初始化引擎
        result = (*engineObj)->Realize(engineObj, SL_BOOLEAN_FALSE);
        if (SL_BOOLEAN_FALSE != result) {
            return;
        }

        //1.3获取引擎接口 SLEngineItf engineInterface
        result = (*engineObj)->GetInterface(engineObj, SL_IID_ENGINE, &engine);
        if (SL_RESULT_SUCCESS != result) {
            return;
        }

        //todo 第二大步设置混音器
        //2.1创建混音器：SLObjectItf outputMixObject
        result = (*engine)->CreateOutputMix(engine, &outputMixObj, 0, 0, 0);
        if (SL_RESULT_SUCCESS != result) {
            return;
        }

        //2.2初始化混音器
        result = (*outputMixObj)->Realize(outputMixObj, SL_BOOLEAN_FALSE);
        if (SL_RESULT_SUCCESS != result) {
            return;
        }
    }

    //释放引擎资源
    virtual void release() {
        if (outputMixObj) {
            (*outputMixObj)->Destroy(outputMixObj);
            outputMixObj = nullptr;
        }

        if (engineObj) {
            (*engineObj)->Destroy(engineObj);
            engineObj = nullptr;
            engine = nullptr;
        }
    }

public:
    //引擎初始化
    AudioEngine() : engineObj(nullptr), engine(nullptr), outputMixObj(nullptr) {
        createEngine();
    }

    virtual ~AudioEngine() {
        release();
    }
};

#endif //AVSAMPLE_AUDIOENGINE_H
