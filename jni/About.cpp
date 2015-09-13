#include <stdlib.h>
#include <android/log.h>
#include "About.h"

#define LOG_TAG    "CameraNativeSample"
#define DPRINTF(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

JNIEXPORT jstring JNICALL Java_com_example_cameranative_About_getVersionData(
		JNIEnv* env, jobject obj) {
	char sVersion[] = "C code model";
	DPRINTF("Software version %s", sVersion);
	return env->NewStringUTF(sVersion);
}

