cmake_minimum_required(VERSION 3.4.1)

file(GLOB clientapp_sources
    ../../../deps/djinni/support-lib/jni/*.cpp
    ../../../generated-src/jni/*.cpp
    ../../../src/cpp/*.cpp
)

add_library(clientapp SHARED ${clientapp_sources})

# include directories for header files
include_directories(
    ../../../deps/djinni/support-lib/
    ../../../deps/djinni/support-lib/jni/
    ../../../generated-src/cpp/
    ../../../generated-src/jni/
    ../../../src/cpp/
)

target_link_libraries(clientapp)