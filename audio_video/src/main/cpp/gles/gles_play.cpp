//
// Created by ming lv on 12/26/20.
//
#include <pthread.h>
#include "gles_play.h"

ANativeWindow *nativeWindow = 0;
EGLDisplay display;
EGLSurface windowSurface;
EGLContext context;
int width = 640;
int height = 272;
//静态初始化互斥锁
pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

/**
 * 播放YUV线程
 * @param pVoid
 * @return
 */
void *readYUVThread(void *pVoid) {
    Gles_play *gles_play = static_cast<Gles_play *>(pVoid);
    gles_play->start();
    return 0;
}

GLint initShader(const char *source, int type);

GLint initShader(const char *source, GLint type) {
    GLint sh = glCreateShader(type);
    if (sh == 0) {
        LOGD("glCreateShader %d failed", type);
        return 0;
    }
    //加载shader
    glShaderSource(sh, 1, &source, 0);
    //编译shader
    glCompileShader(sh);

    GLint status;
    glGetShaderiv(sh, GL_COMPILE_STATUS, &status);
    if (status == 0) {
        LOGD("glCompileShader %d failed", type);
        LOGD("source %s", source);
        return 0;
    }
    LOGD("glCompileShader %d sources", type);
    return sh;
}

//顶点着色器，每个顶点执行一次，可以并行执行
#define GET_STR(x) #x
static const char *vertexShader = GET_STR(
//输入顶点坐标,会在程序指定将数据输入到该字段
        attribute
        vec4 aPosition;
        //输入的纹理坐标，会在程序指定将数据输入到该字段
        attribute
        vec2 aTextCood;
        //输出的纹理坐标
        varying
        vec2 vTextCord;
        void main() {
            //这里其实是将上下翻转过来（因为安卓图片会自动上下翻转，所以转回来）
            vTextCoord = vec2(aTextCoord.x, 1.0 - aTextCoord.y);
            //直接把传入的坐标值作为传入渲染管线。gl_Position是OpenGL内置的
            gl_Position = aPosition;
        }
);
//图元被光栅化为多少片段，就被调用多少次
static const char *fragYUV420P = GET_STR(
        precision
        mediump float;
        varying
        vec2 vTextCoord;
        //输入的yuv三个纹理
        uniform
        sampler2D yTexture;//采样器
        uniform
        sampler2D uTexture;//采样器
        uniform
        sampler2D vTexture;//采样器
        void main() {
            vec3 yuv;
            vec3 rgb;
            //分别取yuv各个分量的采样纹理（r表示？）
            //
            yuv.x = texture2D(yTexture, vTextCoord).g;
            yuv.y = texture2D(uTexture, vTextCoord).g - 0.5;
            yuv.z = texture2D(vTexture, vTextCoord).g - 0.5;
            rgb = mat3(
                    1.0, 1.0, 1.0,
                    0.0, -0.39465, 2.03211,
                    1.13983, -0.5806, 0.0
            ) * yuv;
            //gl_FragColor是OpenGL内置的
            gl_FragColor = vec4(rgb, 1.0);
        }
);


Gles_play::Gles_play(JNIEnv *env, jobject thiz, PlayCallback *playCallback, const char *source,
                     jobject pJobject) {
    this->thiz = thiz;
    this->playCallback = playCallback;
    this->env = env;
    this->surface = surface;
    //这里有坑，这里赋值之后不能给其它地方使用，因为被释放了，变成悬空指针
    //this->data_source=source;
    // [strlen(data_source)] 这段代码有坑：因为（hello  而在C++中是 hello\n），所以需要加1
    this->data_source = new char[strlen(source) + 1];
    strcpy(this->data_source, source);

}

Gles_play::~Gles_play() {
    if (playCallback) {
        delete playCallback;
        playCallback = 0;
    }
}

/**
 * 准备开启一个线程
 */
void Gles_play::prepare() {
    pthread_create(&pid_prepare, 0, readYUVThread, this);

}

void Gles_play::start() {
    this->playYUV(this->surface);
}

void Gles_play::release() {

}

void Gles_play::playYUV(jobject surface) {
    //加锁
    pthread_mutex_lock(&mutex);
    //先判断资源是否释放，避免播放异常
    release();

    showMessage(env, "start", true);

    //开始播放
    isPlay = true;

    //1、获取原始窗口
    nativeWindow = ANativeWindow_fromSurface(env, surface);

    //获取display
    display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (display == EGL_NO_DISPLAY) {
        LOGD("egl display failed");
        showMessage(env, "egl display failed", false);
        return;
    }
    //2.初始化egl，后两个参数为主次版本号
    if (EGL_TRUE != eglInitialize(display, 0, 0)) {
        LOGD("eglInitialize failed");
        showMessage(env, "eglInitialize failed", false);
        return;
    }

    //3.1 surface配置，可以理解为窗口
    EGLConfig eglConfig;
    EGLint configNum;
    EGLint configSpec[] = {
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_NONE
    };

    if (EGL_TRUE != eglChooseConfig(display, configSpec, &eglConfig, 1, &configNum)) {
        LOGD("eglChooseConfig failed");
        showMessage(env, "eglChooseConfig failed", false);
        return;
    }

    //3.2创建surface(egl和NativeWindow进行关联，最后一个参数为属性新年喜，0表示默认版本)
    windowSurface = eglCreateWindowSurface(display, eglConfig, nativeWindow, 0);
    if (windowSurface == EGL_NO_SURFACE) {
        LOGD("eglCreateWindowSurface failed");
        showMessage(env, "eglCreateWindowSurface failed", false);
        return;
    }


    //4、创建关联上下文
    const EGLint ctxAttr[] = {
            EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE
    };
    //EGL_NO_CONTEXT表示不需要多个设备共享上下文
    context = eglCreateContext(display, eglConfig, EGL_NO_CONTEXT, ctxAttr);
    if (context == EGL_NO_CONTEXT) {
        LOGD("eglCreateContext failed");
        showMessage(env, "eglMakeCount failed", false);
        return;
    }

    GLint vsh = initShader(vertexShader, GL_VERTEX_SHADER);
    GLint fsh = initShader(fragYUV420P, GL_FRAGMENT_SHADER);

    //创建渲染程序
    GLint program = glCreateProgram();
    if (program == 0) {
        LOGD("glCreateProgram failed");
        showMessage(env,"glCreateProgram failed",false);
        return;
    }

    //想渲染程序中加入着色器

}


void Gles_play::showMessage(JNIEnv *env, const char *message, bool b) {
    if (this->playCallback) {
        if (b) {
            this->playCallback->onSucceed(message);
        } else {
            this->playCallback->onError(message);
        }
    }

}
