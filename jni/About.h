#ifndef ABOUT_H
#define ABOUT_H

#include <jni.h>

extern "C" {

   //-----------------------------------------------------------------------------
   /// @brief fcvGetVersion
   /// 
   /// @return FastCV version
   //-----------------------------------------------------------------------------
   JNIEXPORT jstring JNICALL Java_com_example_cameranative_About_getFastCVVersion
   (
      JNIEnv* env,
      jobject obj
   );

};

#endif // ABOUT_H
