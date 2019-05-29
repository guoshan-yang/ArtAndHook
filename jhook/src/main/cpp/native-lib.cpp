#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include "art_method.h"

#define  LOG_TAG    "JXposed"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

static size_t methsize;
static int supperOffset;


extern "C" JNIEXPORT void
JNICALL
Java_com_yanggs_jhook_HookUtil_replaceNativeArt(JNIEnv* env, jclass clazz,jobject src, jobject new_,jobject invoker) {

//    void* mSrc=(void*)env->FromReflectedMethod(src);
//    void* mNew_= env->FromReflectedMethod(new_);
//    void* mInvoker=env->FromReflectedMethod(invoker);
    art::mirror::ArtMethod*  mSrc = (art::mirror::ArtMethod*)env->FromReflectedMethod(src);
    art::mirror::ArtMethod*  mNew_ = (art::mirror::ArtMethod*)env->FromReflectedMethod(new_);
    art::mirror::ArtMethod*  mInvoker = (art::mirror::ArtMethod*)env->FromReflectedMethod(invoker);

    memcpy(mInvoker, mSrc, methsize);
    mInvoker->access_flags_ = mInvoker->access_flags_ | 0x0002;
    memcpy(mSrc, mNew_, methsize);
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }

    size_t firMid = (size_t) env->GetMethodID(env->FindClass("com/yanggs/jhook/Test"), "f1", "()V");
    size_t secMid = (size_t) env->GetMethodID(env->FindClass("com/yanggs/jhook/Test"), "f2", "()V");

    methsize = secMid - firMid;

    return JNI_VERSION_1_4;
}


extern "C" JNIEXPORT void
JNICALL
Java_com_yanggs_jhook_HookUtil_computeSupperCls(JNIEnv* env, jclass clazz,jobject fld, jobject test) {

    art::mirror::ArtField* field=(art::mirror::ArtField*)env->FromReflectedField(fld);
    art::mirror::ArtField* demo=(art::mirror::ArtField*)env->FromReflectedField(test);

    uint32_t *dCls=(uint32_t *)field->declaring_class_;
    uint32_t *hCls=(uint32_t *)demo->declaring_class_;
    for(int i=0;i<50;++i){
        if(*(dCls+i)==NULL&&*(hCls+i)==(uint32_t)dCls){
            supperOffset=i;
            return;
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_yanggs_jhook_HookUtil_setSupperCls(JNIEnv *env, jclass type, jobject flag) {

    art::mirror::ArtField* field=(art::mirror::ArtField*)env->FromReflectedField(flag);
    size_t *dCls=(size_t *)field->declaring_class_;
    *(dCls + supperOffset) = NULL;
}