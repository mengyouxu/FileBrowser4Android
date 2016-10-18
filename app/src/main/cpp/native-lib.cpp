//
// Created by mengyouxu on 2016/10/14.
//

#include "UdpDataSource.h"
#include <jni.h>

#include <android/log.h>
#define LOG_TAG "native-lib"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

class UdpCacher *cacher = nullptr;
extern "C"{
    jstring Java_meng_FileBrowser_UdpDataSource3_stringFromJNI(JNIEnv* env, jobject obj);
    jboolean  Java_meng_FileBrowser_UdpDataSource3_nativeOpen(JNIEnv* env, jobject obj, jstring udpAddr, int port);
    jboolean  Java_meng_FileBrowser_UdpDataSource3_nativeClose(JNIEnv* env, jobject obj);
    jint Java_meng_FileBrowser_UdpDataSource3_nativeRead(JNIEnv *env, jobject obj, jbyteArray buffer, int offset, int readLength);
    jbyteArray  Java_meng_FileBrowser_UdpDataSource3_nativeByteArrayTest(JNIEnv* env, jobject obj, jbyteArray array);
}

jstring Java_meng_FileBrowser_UdpDataSource3_stringFromJNI(JNIEnv* env, jobject obj) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
jbyteArray  Java_meng_FileBrowser_UdpDataSource3_nativeByteArrayTest(JNIEnv* env, jobject obj, jbyteArray array){
    jbyte *data = env->GetByteArrayElements(array, 0);
    LOGD("L%d nativeByteArrayTest data[0] = %c", __LINE__, data[0]);

    jbyteArray byteArrayR = env->NewByteArray(10);
    env->SetByteArrayRegion(byteArrayR, 0, 10, data);

    char *td="xyzxyzxyzx";
    env->SetByteArrayRegion(array, 0, 10, (jbyte *)td);
    return byteArrayR;
}

jboolean  Java_meng_FileBrowser_UdpDataSource3_nativeOpen(JNIEnv* env, jobject obj, jstring udpAddr, jint port){
    const char *tmp = env->GetStringUTFChars(udpAddr, NULL);
    int udpPort = port;
    if (tmp == NULL) {  // Out of memory
        return false;
    }
    LOGD("setDataSource: path %s", tmp);

    std::string pathStr(tmp);
    env->ReleaseStringUTFChars(udpAddr, tmp);
    tmp = NULL;
    LOGD("L%d nativeOpen ADDR: %s:%d", __LINE__, pathStr.c_str(), udpPort);
    cacher = new UdpCacher(pathStr.c_str(), udpPort);
    cacher->cacherStart();
    return true;
}

jboolean  Java_meng_FileBrowser_UdpDataSource3_nativeClose(JNIEnv* env, jobject obj){
    if(cacher != nullptr){
        cacher->cacherStop();
    }
    return true;
}

jint Java_meng_FileBrowser_UdpDataSource3_nativeRead(JNIEnv *env, jobject obj, jbyteArray buffer, int offset, int readLength){
    // LOGD("L%d native_read", __LINE__);
    int ret_val = 0;
    char *buff = (char *)malloc(readLength);

    if(cacher != nullptr){
        ret_val = cacher->read(buff, readLength);
        if(ret_val > 0){
            env->SetByteArrayRegion(buffer, offset, ret_val, (jbyte*)buff);
        }
    }
    free(buff);
    return ret_val;
}
