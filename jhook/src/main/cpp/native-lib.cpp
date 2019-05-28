#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include "art_method.h"

#define  LOG_TAG    "JXposed"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

size_t methsize;

extern "C" JNIEXPORT jint
JNICALL
Java_com_democpp_MainActivity_fork(
        JNIEnv *env,
        jobject /* this */) {
    pid_t pid;
    pid = fork();
    return pid;
}

extern "C" JNIEXPORT void
JNICALL
Java_com_iwcode_jhook_HookUtil_replaceNativeArt(JNIEnv* env, jclass clazz,jobject src, jobject new_,jobject invoker) {

    void* mSrc=(void*)env->FromReflectedMethod(src);
    void* mNew_= env->FromReflectedMethod(new_);
//    void* mInvoker=env->FromReflectedMethod(invoker);
    art::mirror::ArtMethod*  mInvoker =
            (art::mirror::ArtMethod*)env->FromReflectedMethod(invoker);

    memcpy(mInvoker, mSrc, methsize);
    mInvoker->access_flags_ = mInvoker->access_flags_ | 0x0002;
    memcpy(mSrc, mNew_, methsize);
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }

    size_t firMid = (size_t) env->GetMethodID(env->FindClass("com/iwcode/jhook/Test"), "f1", "()V");
    size_t secMid = (size_t) env->GetMethodID(env->FindClass("com/iwcode/jhook/Test"), "f2", "()V");

    methsize = secMid - firMid;

    return JNI_VERSION_1_4;
}
