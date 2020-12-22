#include <jni.h>
#include <string>
#include <android/log.h>
/**
 * java 中的基本数据类型与c/C++的基本类型映射
 *
 * Java      JNI       C/C++类型
 * boolean   jboolean  unsigned char (无符号 8 位整型)
 * byte      jbyte     char(有符号8位整形)
 * char      jchar     char (有符号 8 位整型)
 * short     jshort    unsingned short (无符号 16 位整型)
 * int       jint      int (有符号 32 位整型)
 * long      jlong     long (有符号 64 位整型)
 * float     jfloat    float (有符号 32 位浮点型)
 * double    jdouble   double (有符号 64 位双精度型)
 *
 * JNIEnv 表示Java调用native语言的环境，是一个封装了几乎全部JNNI方法得指针
 * JNIEnv 只在创建它的线程生效，不能跨线程传递，不同线程的JNiEnv彼此独立
 * native环境中创建的线程，如果需要访问JNI,必须要调用AttachCurrentThread关联，并使用DetachedCurrentThread解除链接
 *
 * JavaVM 是虚拟机在JNI层的代表，一个进程只有一个JavaVM，所有的线程共用一个javaVM.
 *
 */
extern "C" {
int main();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_github_cmake_1application_MainActivity_testCmake(JNIEnv *env,jclass clazz) {
    std::string hello = "Hello from C++";
    __android_log_print(ANDROID_LOG_DEBUG, "DevYK", "main--->:%d", main());
}