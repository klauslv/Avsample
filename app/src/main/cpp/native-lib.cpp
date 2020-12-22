#include <jni.h>
#include <string>
#include <android/log.h>
#include <pthread.h>

#define TAG "native-lib"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__);
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__);
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__);
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
extern "C"
JNIEXPORT jstring JNICALL
Java_com_github_avsample_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_github_avsample_MainActivity_test1(JNIEnv *env, jobject thiz,
                                            jboolean b,
                                            jbyte b1,
                                            jchar c,
                                            jshort s,
                                            jlong l,
                                            jfloat f,
                                            jdouble d,
                                            jstring name,
                                            jint age,
                                            jintArray i,
                                            jobjectArray strs,
                                            jbooleanArray array) {

    //1、接收Java传递过来的Boolean值
    unsigned char b_boolean = b;
    LOGD("boolean-> %d", b_boolean);

    //2、接收Java传递过来的byte值
    char c_byte = b1;
    LOGD("c_byte-> %d", c_byte);

    //3、接收Java传递过来的char值
    unsigned short c_char = c;
    LOGD("c_byte-> %d", c_char);

    //4、接收Java传递过来的short值
    short s_short = s;
    LOGD("s_short-> %d", s_short);

    //5、接收Java传递过来的long值
    long l_long = l;
    LOGD("l_long-> %d", l_long);

    //6、接收Java传递过来的float值
    float f_float = f;
    LOGD("f_float-> %f", f_float);

    //7、接收Java传递过来的double值
    double d_double = d;
    LOGD("d_double-> %f", d_double);


    //8、接收Java传递过来的double值
    const char *name_str = env->GetStringUTFChars(name, 0);
    LOGD("name_str-> %s", name_str);

    //9、接收Java传递过来的int值
    int age_java = age;
    LOGD("age_java-> %d", age_java);

    //10、打印Java传递过来的int[]
    jint *intArray = env->GetIntArrayElements(i, NULL);
    //拿到数组长度
    jsize intArraySize = env->GetArrayLength(i);
    for (int i = 0; i < intArraySize; ++i) {
        LOGD("intarray->%d", intArray[i]);
    }

    //释放数组
    env->ReleaseIntArrayElements(i, intArray, 0);

    //11、打印Java传递过来的String[]
    jsize stringLength = env->GetArrayLength(strs);
    for (int i = 0; i < stringLength; ++i) {
        jobject jobject1 = env->GetObjectArrayElement(strs, i);
        //强转为JNI String
        jstring jstringData = static_cast<jstring >(jobject1);

        const char *itemStr = env->GetStringUTFChars(jstringData, NULL);

        LOGD("String[%d]:%s", i, itemStr);

        //回收String[]
        env->ReleaseStringUTFChars(jstringData, itemStr);
    }

    //12、打印Java传递过来的Object对象
    jsize booleanLength = env->GetArrayLength(array);
    jboolean *bArray = env->GetBooleanArrayElements(array, NULL);
    for (int i = 0; i < booleanLength; ++i) {
        bool b = bArray[i];
        jboolean b2 = bArray[i];
        LOGD("boolean:%d", b)
        LOGD("jboolean:%d", b2);
    }
    //回收
    env->ReleaseBooleanArrayElements(array, bArray, 0);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_github_avsample_MainActivity_getPerson(JNIEnv *env, jobject thiz) {
    //1、拿到JAVA类的全路径
    const char *person_java = "com/github/avsample/Person";
    const char *method = "<init>";//Java 构造方法得标识

    //2、找到需要处理的java对象class
    jclass j_person_class = env->FindClass(person_java);
    LOGD("Person: %s", "111111111");

    //3、拿到空构造方法
    jmethodID person_constructor = env->GetMethodID(j_person_class, method, "()V");
    LOGD("Person: %s", "22222222");

    //4、创建对象
    jobject person_obj = env->NewObject(j_person_class, person_constructor);
    LOGD("Person: %s", "3333333333");

    //5、拿到setName方法得签名，并拿到对应的setName方法
    const char *nameSig = "(Ljava/lang/String;)V";
    jmethodID nameMethodId = env->GetMethodID(j_person_class, "setName", nameSig);
    LOGD("Person: %s", "4444444");

    //6.拿到setAge方法得签名，并且拿到setAge方法
    jmethodID ageMethodId = env->GetMethodID(j_person_class, "setAge", "(I)V");
    LOGD("Person: %s", "5555555");

    //7、调用Java对象函数
    const char *name = "clear";
    jstring newStringName = env->NewStringUTF(name);
    env->CallVoidMethod(person_obj, nameMethodId, newStringName);
    env->CallVoidMethod(person_obj, ageMethodId, 28);
    LOGD("Person: %s", "66666666");

    jmethodID jtoString = env->GetMethodID(j_person_class, "toString", "()Ljava/lang/String;");
    jobject obj_string = env->CallObjectMethod(person_obj, jtoString);
    jstring perStr = static_cast<jstring >(obj_string);
    const char *itemStr2 = env->GetStringUTFChars(perStr, NULL);
    LOGD("Person: %s", itemStr2);
    LOGD("Person: %s", "7777777");
    return person_obj;

}

extern "C" //支持C语言
JNIEXPORT void JNICALL //告诉虚拟机这是jni函数
Java_com_github_avsample_MainActivity_dynamicRegister(JNIEnv *env, jobject thiz, jstring name) {
    const char *j_name = env->GetStringUTFChars(name, NULL);
    LOGD("动态注册：%s", j_name);

    env->ReleaseStringUTFChars(name, j_name);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_github_avsample_MainActivity_dynamicRegister2(JNIEnv *env, jobject thiz, jstring name) {
    const char *j_name = env->GetStringUTFChars(name, NULL);
    LOGD("动态注册：%s", j_name);

    jclass clazz = env->GetObjectClass(thiz);//拿到当前类的class
    jmethodID mid = env->GetMethodID(clazz, "testException", "()V");
    env->CallVoidMethod(thiz, mid);//执行跑出一个异常
    jthrowable exc = env->ExceptionOccurred();//检查是否发生异常
    if (exc) {
        env->ExceptionDescribe();//打印异常信息
        env->ExceptionClear();//清掉发生的异常
        jclass newExcCls = env->FindClass("java/lang/IllegalArgumentException");
        env->ThrowNew(newExcCls, "JNI 中发生了一个异常信息");//返回一个新的异常到JAVA

    }
    //释放
    env->ReleaseStringUTFChars(name, j_name);
}

jclass personClass;
extern "C"
JNIEXPORT void JNICALL
Java_com_github_avsample_MainActivity_test4(JNIEnv *env, jobject thiz) {
    LOGD("测试局部引用");

    if (personClass == NULL) {
        const char *person_class = "com/github/avsample/Person";
        jclass jclass1 = env->FindClass(person_class);

        //提升全局解决不能重复使用的问题
        personClass = static_cast<jclass >(env->NewGlobalRef(jclass1));
        LOGD("personClass == null 执行了。")
    }

    //Java Person 构造方法实例化
    const char *sig = "()V";
    const char *method = "<init>";//java构造方法
    jmethodID  init = env->GetMethodID(personClass,method,sig);

    //创建出来
    env->NewObject(personClass,init);

    env->DeleteWeakGlobalRef(personClass);
    personClass = NULL;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_github_avsample_MainActivity_nativeCount(JNIEnv *env, jobject thiz) {
    jclass jclass1 = env->GetObjectClass(thiz);
    jfieldID  filedId = env->GetFieldID(jclass1,"count", "I");

    if(env->MonitorEnter(thiz) != JNI_OK){
        LOGE("%s:MonitorEnter() failed",__FUNCTION__);
    }

    int val = env->GetIntField(thiz,filedId);
    val++;
    LOGI("count = %d",val);
    env->SetIntField(thiz,filedId,val);
    if (env->ExceptionOccurred()) {
        LOGE("ExceptionOccurred()...");
        if (env->MonitorExit(thiz) != JNI_OK) {
            LOGE("%s: MonitorExit() failed", __FUNCTION__);
        };
    }

    if (env->MonitorExit(thiz) != JNI_OK) {
        LOGE("%s: MonitorExit() failed", __FUNCTION__);
    };
}
JavaVM *jvm;
jobject  instance;
void * customThread(void * pVoid){
    //调用的话，一定需要JNIEnv *env
    //JNIEnv *env 无法跨越线程，只有javaVm才能跨越线程


    JNIEnv * env = NULL;
    int result = jvm->AttachCurrentThread(&env,0);//把native的线程附加到JVM
    if(result != 0){
        return 0;
    }
    jclass mainActivityClass = env->GetObjectClass(instance);

    //拿到MainActivity的UpdateUi
    const char *sig = "()V";
    jmethodID meth= env->GetMethodID(mainActivityClass,"updateUI",sig);
    env->CallVoidMethod(instance,meth);

    //解除 附加到JVM的native线程
    jvm->DetachCurrentThread();
    return 0;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_github_avsample_MainActivity_testThread(JNIEnv *env, jobject thiz) {
    instance = env->NewGlobalRef(thiz);//全局的不会被释放，可以在线程里面调用

    //如果是非全局的，函数结束就被释放了
    pthread_t pthreadID;
    pthread_create(&pthreadID,0,customThread,instance);
    pthread_join(pthreadID,0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_github_avsample_MainActivity_unThread(JNIEnv *env, jobject thiz) {
    if(NULL != instance){
        env->DeleteGlobalRef(instance);
        instance = NULL;
    }
}