#include <stdlib.h>
#include <android/log.h>
#include <fastcv.h>
#include "About.h"

#define LOG_TAG    "CameraNativeSample"
#define DPRINTF(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

JNIEXPORT jstring JNICALL Java_com_example_cameranative_About_getFastCVVersion(
		JNIEnv* env, jobject obj) {
	char sVersion[32];
	fcvGetVersion(sVersion, 32);
	DPRINTF("FastCV version %s", sVersion);
	return env->NewStringUTF(sVersion);
}

