#ifndef ABOUT_H
#define ABOUT_H

#include <jni.h>

extern "C" {

//-----------------------------------------------------------------------------
/// @brief getVersionData
///
/// @return Software version
//-----------------------------------------------------------------------------
JNIEXPORT jstring JNICALL
Java_com_example_cameranative_About_getVersionData(JNIEnv* env, jobject obj);
};

#endif  // ABOUT_H
