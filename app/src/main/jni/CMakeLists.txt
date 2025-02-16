cmake_minimum_required(VERSION 3.5)
project(mnn-llm)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Oz")

option(LLM_SUPPORT_VISION "Llm model support vision input." OFF)
option(DUMP_PROFILE_INFO "Dump profile info when chat." OFF)

if (DUMP_PROFILE_INFO)
    add_definitions(-DDUMP_PROFILE_INFO)
endif()

if (CMAKE_CXX_COMPILER_ID STREQUAL "Clang")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -stdlib=libc++")
endif()

# compile MNN first
set(MNN_LOW_MEMORY ON CACHE BOOL "Open MNN_LOW_MEMORY" FORCE)
set(MNN_SUPPORT_TRANSFORMER_FUSE ON CACHE BOOL "Open MNN_SUPPORT_TRANSFORMER_FUSE" FORCE)
if (CMAKE_ANDROID_ARCH_ABI STREQUAL "arm64-v8a")
    set(MNN_ARM82 ON CACHE BOOL "Open MNN_ARM82" FORCE)
endif()
if (LLM_SUPPORT_VISION)
    add_definitions(-DLLM_SUPPORT_VISION)
    set(MNN_BUILD_OPENCV ON CACHE BOOL "Open MNN_BUILD_OPENCV" FORCE)
    set(MNN_IMGCODECS ON CACHE BOOL "Open MNN_IMGCODECS" FORCE)
endif()

add_subdirectory(${CMAKE_CURRENT_LIST_DIR}/mnn-llm/MNN)

# include dir
include_directories(${CMAKE_CURRENT_LIST_DIR}/mnn-llm/include/
                    ${CMAKE_CURRENT_LIST_DIR}/mnn-llm/MNN/include/
                    ${CMAKE_CURRENT_LIST_DIR}/mnn-llm/MNN/tools/cv/include/
                    ${CMAKE_CURRENT_LIST_DIR}/mnn-llm/MNN/3rd_party/
                    ${CMAKE_CURRENT_LIST_DIR}/mnn-llm/MNN/tools/
                    )

# source files
FILE(GLOB SRCS ${CMAKE_CURRENT_LIST_DIR}/mnn-llm/src/*.cpp)

# jni file
list(APPEND SRCS "${CMAKE_CURRENT_LIST_DIR}/llm_mnn_jni.cpp")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
# compile dynamic so, support Linux/Mac
add_library(nga-chat SHARED ${SRCS})
set_target_properties(nga-chat PROPERTIES WINDOWS_EXPORT_ALL_SYMBOLS TRUE)

target_link_libraries(nga-chat MNN MNN_Express)
if (MNN_OPENCL)
    target_link_libraries(nga-chat MNN_CL)
endif()
if (LLM_SUPPORT_VISION)
    target_link_libraries(nga-chat MNNOpenCV)
endif()

set(LLM_LIBS "")
list(APPEND LLM_LIBS $<TARGET_FILE:MNN>)
list(APPEND LLM_LIBS $<TARGET_FILE:MNN_Express>)
if (MNN_OPENCL)
    list(APPEND LLM_LIBS $<TARGET_FILE:MNN_CL>)
endif()
add_custom_command(TARGET nga-chat POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy
            ${LLM_LIBS}
            ${CMAKE_CURRENT_LIST_DIR}/../../../libs/${CMAKE_ANDROID_ARCH_ABI})