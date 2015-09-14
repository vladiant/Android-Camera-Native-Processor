APP_ABI := armeabi-v7a
NDK_TOOLCHAIN_VERSION := 4.9
APP_OPTIM := release
APP_STL := gnustl_static
APP_CPPFLAGS := -frtti -fexceptions -pthread -Wall -std=c++11
APP_CFLAGS := -pthread -Wall

APP_CFLAGS += -march=armv7-a -mfloat-abi=softfp -mfpu=neon
APP_CPPFLAGS += -march=armv7-a -mfloat-abi=softfp -mfpu=neon