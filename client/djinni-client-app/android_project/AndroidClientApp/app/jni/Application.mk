# Android makefile for helloworld shared lib, jni wrapper around libhelloworld C API
 
APP_ABI := all
APP_OPTIM := release
APP_PLATFORM := android-14
# GCC 4.9 Toolchain :[DEPRECATED] https://android.googlesource.com/platform/ndk/+/master/docs/ClangMigration.md
NDK_TOOLCHAIN_VERSION = 4.9
# GNU libc++ is the only Android STL which supports C++11 features
APP_STL := c++_static
APP_BUILD_SCRIPT := jni/Android.mk
APP_MODULES := libclientapp_jni
