#ifndef CAMERA_SAMPLE_RENDERER_H
#define CAMERA_SAMPLE_RENDERER_H

#include <jni.h>

extern "C" {
//---------------------------------------------------------------------------
/// @brief
///   Sets up the viewport
/// @param width
///    width of viewport
/// @param height
///    height of viewport
//---------------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_com_example_cameranative_CameraSampleRenderer_surfaceChanged(JNIEnv* env,
		jobject obj, jint width, jint height);

//---------------------------------------------------------------------------
/// @brief
///   Cleans up any memory and state associated with the renderer.
//---------------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_com_example_cameranative_CameraSampleRenderer_cleanup(JNIEnv * env,
		jobject obj);

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
JNIEXPORT void JNICALL
Java_com_example_cameranative_CameraSampleRenderer_render(JNIEnv * env,
		jobject obj, jbyteArray img, jint w, jint h);

//------------------------------------------------------------------------------
/// @brief Gets the rendering frame rate
///
/// @return Rendering FPS (frames per second)
//------------------------------------------------------------------------------
JNIEXPORT jfloat JNICALL
Java_com_example_cameranative_CameraSampleRenderer_getGPUFPS(JNIEnv* env,
		jobject obj);

//---------------------------------------------------------------------------
/// @brief
///   Retrieves a pointer to the renderer's rendering buffer, will create
///   a new buffer if not yet allocated or size changes. Locks buffer so
///   don't call lock before this.
///
/// @param w Width of image to render.
/// @param h Height of image to render.
///
/// @return Pointer to buffer.
//---------------------------------------------------------------------------
uint8_t* getRenderBuffer(uint32_t w, uint32_t h);

//---------------------------------------------------------------------------
/// @brief
///   Returns the size of the render buffer for the buffer's format.
///
/// @return Size in bytes of the render buffer.
//---------------------------------------------------------------------------
uint32_t getRenderBufferSize();

//---------------------------------------------------------------------------
/// @brief
///   Unlocks render buffer for use.
///
//---------------------------------------------------------------------------
void unlockRenderBuffer();

//---------------------------------------------------------------------------
/// @brief
///   Locks render buffer to prevent processing collisions and data
///   corruption.
//---------------------------------------------------------------------------
void lockRenderBuffer();

};

#endif // CAMERA_SAMPLE_RENDERER_H
