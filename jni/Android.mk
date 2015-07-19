LOCAL_PATH:= $(call my-dir)

##########################################################
# FastCV - start.
##########################################################

include $(CLEAR_VARS)
LOCAL_MODULE := fastcv
LOCAL_SRC_FILES := fastcv/lib/libfastcv.a
LOCAL_STRIP_MODULE := false
include $(PREBUILT_STATIC_LIBRARY)

##########################################################
# FastCV - end.
##########################################################



##########################################################
# Native Processing - start.
##########################################################

include $(CLEAR_VARS)
LOCAL_PRELINK_MODULE:= false

# This variable determines the OpenGL ES API version to use:
# If set to true, OpenGL ES 1.1 is used, otherwise OpenGL ES 2.0.

USE_OPENGL_ES_1_1 := false

# Set OpenGL ES version-specific settings.

ifeq ($(USE_OPENGL_ES_1_1), true)
    OPENGLES_LIB  := -lGLESv1_CM
    OPENGLES_DEF  := -DUSE_OPENGL_ES_1_1
else
    OPENGLES_LIB  := -lGLESv2
    OPENGLES_DEF  := -DUSE_OPENGL_ES_2_0
endif

# An optional set of compiler flags that will be passed when building
# C ***AND*** C++ source files.
#
# NOTE: flag "-Wno-write-strings" removes warning about deprecated conversion
#       from string constant to 'char*'

LOCAL_CFLAGS := -Wno-write-strings $(OPENGLES_DEF)

# The list of additional linker flags to be used when building your
# module. This is useful to pass the name of specific system libraries
# with the "-l" prefix.

LOCAL_LDLIBS := -llog $(OPENGLES_LIB)
LOCAL_LDFLAGS:= -Wl,--no-fix-cortex-a8

LOCAL_MODULE    := libcameranativesample
LOCAL_CFLAGS    := -Werror -Wall
LOCAL_C_INCLUDES := 
LOCAL_SRC_FILES := \
    About.cpp \
    CameraSample.cpp \
    FPSCounter.cpp \
    CameraRendererRGB565GL2.cpp \
    CameraUtil.cpp \
    CameraSampleRenderer.cpp

LOCAL_STATIC_LIBRARIES := fastcv
LOCAL_SHARED_LIBRARIES := liblog libGLESv2
LOCAL_C_INCLUDES += $(LOCAL_PATH)/fastcv/inc

include $(BUILD_SHARED_LIBRARY)

##########################################################
# Native Processing - end.
##########################################################
