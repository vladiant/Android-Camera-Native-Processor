#ifndef CAMERA_SAMPLE_H
#define CAMERA_SAMPLE_H

#include <jni.h>

extern "C" {
   //---------------------------------------------------------------------------
   /// @brief
   ///   Destroys the renderer
   //---------------------------------------------------------------------------
   JNIEXPORT void JNICALL Java_com_example_cameranative_CameraSample_cleanup
   (
      JNIEnv * env, 
      jobject obj
   );

   //---------------------------------------------------------------------------
   /// @brief
   ///   Update the display
   /// @param img
   ///    pointer to buffer that is to be rendered
   /// @param w 
   ///    width of buffer
   /// @param h
   ///    height of buffer
   //---------------------------------------------------------------------------
   JNIEXPORT void JNICALL Java_com_example_cameranative_CameraSampleRenderer_render
   (
      JNIEnv * env, 
      jobject obj, 
      jbyteArray img, 
      jint w, 
      jint h
   );

   //------------------------------------------------------------------------------
   /// @brief Calls native process
   /// @param img Pointer to camera image
   /// @param w Width of image
   /// @param y Height of height
   //------------------------------------------------------------------------------
   JNIEXPORT void JNICALL Java_com_example_cameranative_CameraSample_update
   (
      JNIEnv*     env, 
      jobject     obj, 
      jbyteArray  img, 
      jint        w, 
      jint        h
   );

   //---------------------------------------------------------------------------
   /// @brief
   ///    Initializes the renderer
   //---------------------------------------------------------------------------
   JNIEXPORT void JNICALL Java_com_example_cameranative_CameraSample_init
   (
      JNIEnv* env, 
      jobject obj
   );

   //------------------------------------------------------------------------------
   /// @brief Gets the camera frame rate
   /// 
   /// @return Camera FPS (frames per second)
   //------------------------------------------------------------------------------
   JNIEXPORT jfloat JNICALL Java_com_example_cameranative_CameraSample_getCameraFPS
   (
      JNIEnv*  env, 
      jobject  obj
   );

   //------------------------------------------------------------------------------
   /// @brief Increments the camera frame tick count
   /// 
   //------------------------------------------------------------------------------
   JNIEXPORT void JNICALL Java_com_example_cameranative_CameraSample_cameraFrameTick
   (
      JNIEnv*  env, 
      jobject  obj
   );

   //------------------------------------------------------------------------------
   /// @brief Retrieves native processing timing from native layer.
   /// 
   /// @return Native processing timing, filtered, in ms.
   //------------------------------------------------------------------------------
   JNIEXPORT jfloat JNICALL 
      Java_com_example_cameranative_CameraSample_getNativeProcessTime
   (
      JNIEnv*  env, 
      jobject  obj
   );

};

#endif // CAMERA_SAMPLE_H
