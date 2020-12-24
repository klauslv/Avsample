#1、指定cmake的最小版本
cmake_minimum_required(VERSION 3.4.1)

#找到包含所有的CPP文件
file(GLOB allCpp *.cpp)

#打印当前cmakeLists的路径
message("当前cmake的路径：${CMAKE_SOURCE_DIR} \n cpu架构：${CMAKE_ANDROID_ARCH_ABI}")

#加入cpp原文件
add_library(
        #库的名字
        ffmpeg_lib
        #可以共享
        SHARED
        #native-lib.cpp替换${allCpp}批量导入文件
        ${allCpp})

# 只能找系统的
find_library(
        log-lib
        log)

#引入FFmpeg的头文件
include_directories(${CMAKE_SOURCE_DIR}/include)

# 引入FFmpeg的库文件，设置内部的方式引入，指定库的目录是-L 指定具体的库是-l
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/${CMAKE_ANDROID_ARCH_ABI}")


message("当前的log路径在哪里啊 >>>>>>>>> ${log-lib}")

#开始链接🔝的库
target_link_libraries(
        ffmpeg_lib
        # 先把有依赖的库，先依赖进来
        avformat avcodec avfilter avutil swresample swscale

        ${log-lib}
)