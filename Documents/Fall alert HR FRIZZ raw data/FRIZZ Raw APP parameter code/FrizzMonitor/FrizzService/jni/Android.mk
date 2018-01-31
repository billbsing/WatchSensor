LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := FrizzManager
LOCAL_SRC_FILES := FrizzManager.cpp
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS := -llog -llog -lm -landroid
LOCAL_SHARED_LIBRARIES += \
    libandroid_runtime \
	libnativehelper \
	libcutils \
	libutils \
	liblog \
	libhardware \
	
include $(BUILD_SHARED_LIBRARY)
