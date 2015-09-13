#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <stdlib.h>
#include <android/log.h>
#include <time.h>

#include "CameraSample.h"
#include "CameraSampleRenderer.h"
#include "FPSCounter.h"

#define LOG_TAG    "CameraSample.cpp"
#define DPRINTF(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define IPRINTF(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define EPRINTF(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

inline uint8_t clip(int32_t val) {
	return val > 255 ? 255 : (val < 0 ? 0 : val);
}

inline uint8_t yuv2r(int32_t y, int32_t u, int32_t v) {
	int32_t r = (256 * y + 359 * (v - 128) + 128) >> 8;
	return clip(r);
}

inline uint8_t yuv2g(int32_t y, int32_t u, int32_t v) {
	int32_t g = (256 * y - 88 * (u - 128) - 183 * (v - 128) + 128) >> 8;
	return clip(g);
}

inline uint8_t yuv2b(int32_t y, int32_t u, int32_t v) {
	int32_t b = (256 * y + 454 * (u - 128) + 128) >> 8;
	return clip(b);
}

void convertYUV420toRGB565(const uint8_t* inputY, const uint8_t* inputUV,
		int imageWidth, int imageHeight, int inputYImageBpln,
		int inputUVImageBpln, int outputImageBpln, uint8_t* outputRGB565) {

	uint8_t* inputYData = (uint8_t*) inputY;
	uint8_t* inputUVData = (uint8_t*) inputUV;
	uint8_t* outputData = (uint8_t*) outputRGB565;

	for (int row = 0; row < imageHeight; row += 2) {

		uint8_t* pInputYRowUp = &inputYData[(row + 0) * inputYImageBpln];
		uint8_t* pInputYRowDown = &inputYData[(row + 1) * inputYImageBpln];

		uint8_t* pInputUVRow = &inputUVData[(row / 2) * inputUVImageBpln];

		uint8_t* pOutputRowUp = &outputData[(row + 0) * outputImageBpln];
		uint8_t* pOutputRowDown = &outputData[(row + 1) * outputImageBpln];

		int col = 0;

		for (; col < imageWidth / 2; col++) {

			uint16_t r = 0, g = 0, b = 0, g1 = 0;
			uint16_t y = 0, u = 0, v = 0;

			// UV data for the four pixels.
			u = *pInputUVRow++;
			v = *pInputUVRow++;

			// Top left pixel.
			y = *pInputYRowUp++;

			r = yuv2r(y, u, v);
			g = yuv2g(y, u, v);
			b = yuv2b(y, u, v);

			g1 = g >> 5;
			g = (g & 0x3c) << 3;
			r >>= 3;
			b &= 0xf8;
			*pOutputRowUp++ = (uint8_t) (g | r);
			*pOutputRowUp++ = (uint8_t) (b | g1);

			// Top right pixel.
			y = *pInputYRowUp++;

			r = yuv2r(y, u, v);
			g = yuv2g(y, u, v);
			b = yuv2b(y, u, v);

			g1 = g >> 5;
			g = (g & 0x3c) << 3;
			r >>= 3;
			b &= 0xf8;
			*pOutputRowUp++ = (uint8_t) (g | r);
			*pOutputRowUp++ = (uint8_t) (b | g1);

			// Bottom left pixel.
			y = *pInputYRowDown++;

			r = yuv2r(y, u, v);
			g = yuv2g(y, u, v);
			b = yuv2b(y, u, v);

			g1 = g >> 5;
			g = (g & 0x3c) << 3;
			r >>= 3;
			b &= 0xf8;
			*pOutputRowDown++ = (uint8_t) (g | r);
			*pOutputRowDown++ = (uint8_t) (b | g1);

			// Bottom right pixel.
			y = *pInputYRowDown++;

			r = yuv2r(y, u, v);
			g = yuv2g(y, u, v);
			b = yuv2b(y, u, v);

			g1 = g >> 5;
			g = (g & 0x3c) << 3;
			r >>= 3;
			b &= 0xf8;
			*pOutputRowDown++ = (uint8_t) (g | r);
			*pOutputRowDown++ = (uint8_t) (b | g1);
		} // for (int col ...

	} // for (int row ...

	return;
}

//------------------------------------------------------------------------------
/// @brief
///    Contains state information of the instance of the application.
//------------------------------------------------------------------------------
struct State {
	//---------------------------------------------------------------------------
	/// Constructor for State object sets variables to default values.
	//---------------------------------------------------------------------------
	State() {
		timeFilteredMs = 5;
	}

	/// Camera preview FPS counter
	FPSCounter camFPSCounter;

	/// Filtered timing value for sample processing.
	float timeFilteredMs;

};

/// Application' state structure to hold global state for sample app.
static State state;

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
uint64_t getTimeMicroSeconds() {
	struct timespec t;
	t.tv_sec = t.tv_nsec = 0;

	clock_gettime(CLOCK_REALTIME, &t);
	return (uint64_t) t.tv_sec * 1000000ULL + t.tv_nsec / 1000L;
}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_com_example_cameranative_CameraSample_cleanup(JNIEnv * env, jobject obj) {
	DPRINTF("%s\n", __FUNCTION__);

	// TODO: Deinitialize camera sample processing here.
}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
JNIEXPORT void
JNICALL Java_com_example_cameranative_CameraSample_update(JNIEnv* env,
		jobject obj, jbyteArray img, jint w, jint h) {
	jbyte* jimgData = NULL;
	jboolean isCopy = 0;
	uint8_t* renderBuffer;
	uint64_t time;
	float timeMs;

	// Get data from JNI
	jimgData = env->GetByteArrayElements(img, &isCopy);

	renderBuffer = getRenderBuffer(w, h);

	lockRenderBuffer();

	time = getTimeMicroSeconds();

	uint8_t* pJimgData = (uint8_t*) jimgData;

	// Copy the image first in our own buffer to avoid corruption during
	// rendering. Not that we can still have corruption in image while we do
	// copy but we can't help that.

	convertYUV420toRGB565(pJimgData, &pJimgData[w * h], w, h, w, w, w * 2,
			(uint8_t*) renderBuffer);

	// TODO: Perform sample processing here.

	timeMs = (getTimeMicroSeconds() - time) / 1000.f;
	state.timeFilteredMs = ((state.timeFilteredMs * (29.f / 30.f))
			+ (float) (timeMs / 30.f));

	unlockRenderBuffer();

	// Let JNI know we don't need data anymore. this is important!
	env->ReleaseByteArrayElements(img, jimgData, JNI_ABORT);
}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
JNIEXPORT void JNICALL Java_com_example_cameranative_CameraSample_init(
		JNIEnv* env, jobject obj) {

	// TODO: Initialize camera sample processing here.

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
JNIEXPORT void JNICALL Java_com_example_cameranative_CameraSample_cameraFrameTick(
		JNIEnv* env, jobject obj) {
	state.camFPSCounter.FrameTick();
}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
JNIEXPORT jfloat JNICALL
Java_com_example_cameranative_CameraSample_getNativeProcessTime(JNIEnv* env,
		jobject obj) {
	return (jfloat) state.timeFilteredMs;
}

