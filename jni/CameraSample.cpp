#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <stdlib.h>
#include <android/log.h>
#include <time.h>
#include <fastcv.h>

#include "CameraSample.h"
#include "CameraSampleRenderer.h"
#include "FPSCounter.h"

#define LOG_TAG    "CameraSample.cpp"
#define DPRINTF(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define IPRINTF(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define EPRINTF(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

/// Constant maximum number of corners to detect.
static const uint32_t MAX_CORNERS_TO_DETECT = 43000; // ~480*800 / 9

//------------------------------------------------------------------------------
/// @brief
///    Contains state information of the instance of the application.
//------------------------------------------------------------------------------
struct State {
	//---------------------------------------------------------------------------
	/// Constructor for State object sets variables to default values.
	//---------------------------------------------------------------------------
	State() {
		numCorners = 0;

		timeFilteredMs = 5;
	}

	/// Camera preview FPS counter
	FPSCounter camFPSCounter;

	/// Number of corners detected in last frame.
	uint32_t numCorners;

	/// Storage for corners detected in last frame.
	uint32_t FASTCV_ALIGN128( corners[MAX_CORNERS_TO_DETECT*2] );

	/// Filtered timing value for FastCV processing.
	float timeFilteredMs;

};

/// Application' state structure to hold global state for sample app.
static State state;


//------------------------------------------------------------------------------
/// @brief Performs scaling, blurring and corner detection
/// 
/// @param data Pointer to image data to perform processing
/// @param w Width of data
/// @param y Height of data
//------------------------------------------------------------------------------
void updateCorners(uint8_t* data, uint32_t w, uint32_t h);

//------------------------------------------------------------------------------
/// @brief Returns current time in microseconds
/// 
/// @return Time in microseconds
//------------------------------------------------------------------------------
uint64_t getTimeMicroSeconds();

//==============================================================================
// Function Definitions
//==============================================================================

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
void updateCorners(uint8_t* data, uint32_t w, uint32_t h) {

	//reset the number of corners detected
	state.numCorners = 0;

	//
	// Apply fast corner detection.
	// Find w*h / 9 corners, with threshold set by user and border ==7
	//
	fcvCornerFast9u8(data, w, h, 0,
			20, 7, state.corners, MAX_CORNERS_TO_DETECT,
			&state.numCorners);

}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
uint64_t getTimeMicroSeconds() {
	struct timespec t;
	t.tv_sec = t.tv_nsec = 0;

	clock_gettime(CLOCK_REALTIME, &t);
	return (uint64_t) t.tv_sec * 1000000ULL + t.tv_nsec / 1000L;
}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_com_example_cameranative_CameraSample_cleanup
(
   JNIEnv * env, 
   jobject obj
)
{
   DPRINTF("%s\n", __FUNCTION__);


   fcvCleanUp();
}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
JNIEXPORT void 
   JNICALL Java_com_example_cameranative_CameraSample_update
(
   JNIEnv*     env, 
   jobject     obj, 
   jbyteArray  img, 
   jint        w, 
   jint        h
)
{ 
   jbyte*            jimgData = NULL;
   jboolean          isCopy = 0;
   uint8_t*          renderBuffer;
   uint64_t          time;
   float             timeMs;

   // Get data from JNI 
   jimgData = env->GetByteArrayElements( img, &isCopy );
  
   renderBuffer = getRenderBuffer( w, h );  

   lockRenderBuffer();
   
   time = getTimeMicroSeconds();

   // jimgData might not be 128 bit aligned.
   // fcvColorYUV420toRGB565u8() and other fcv functionality inside 
   // updateCorners() require 128 bit memory aligned. In case of jimgData 
   // is not 128 bit aligned, it will allocate memory that is 128 bit 
   // aligned and copies jimgData to the aligned memory.

   uint8_t*  pJimgData = (uint8_t*)jimgData;    

   // Copy the image first in our own buffer to avoid corruption during 
   // rendering. Not that we can still have corruption in image while we do 
   // copy but we can't help that. 
   
   // if viewfinder is disabled, simply set to gray
   fcvColorYUV420toRGB565u8(
	 pJimgData,
	 w,
	 h,
	 (uint32_t*)renderBuffer );

   // Perform FastCV Corner processing
   updateCorners( (uint8_t*)pJimgData, w, h );

   timeMs = ( getTimeMicroSeconds() - time ) / 1000.f;
   state.timeFilteredMs = 
      ((state.timeFilteredMs*(29.f/30.f)) + (float)(timeMs/30.f));

   // Have renderer draw corners on render buffer.
   drawCorners( state.corners, state.numCorners );

   unlockRenderBuffer();
   
   // Let JNI know we don't need data anymore. this is important!
   env->ReleaseByteArrayElements( img, jimgData, JNI_ABORT );
}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
JNIEXPORT void JNICALL Java_com_example_cameranative_CameraSample_init
(
   JNIEnv* env, 
   jobject obj
)
{
   
   fcvSetOperationMode( (fcvOperationMode) FASTCV_OP_PERFORMANCE );

return;
}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
JNIEXPORT jfloat JNICALL Java_com_example_cameranative_CameraSample_getCameraFPS(
	JNIEnv* env, jobject obj) {
return state.camFPSCounter.GetFilteredFPS();
}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
JNIEXPORT void JNICALL Java_com_example_cameranative_CameraSample_cameraFrameTick
(
	JNIEnv* env,
	jobject obj
)
{
state.camFPSCounter.FrameTick();
}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
JNIEXPORT jfloat JNICALL
Java_com_example_cameranative_CameraSample_getNativeProcessTime
(
	JNIEnv* env,
	jobject obj
)
{
return (jfloat) state.timeFilteredMs;
}

