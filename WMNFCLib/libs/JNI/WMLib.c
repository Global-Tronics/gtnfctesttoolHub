//
// Created by ah_m_ on 2023-11-09.
//

#include <jni.h>
#include "../../cpp back/JNIEXPORT.h"
#include "WMLib.h"



#include <stdio.h>
#include <jni.h>
#include <jni.h>
#include <jni.h>


//JNIEXPORT jstring


//JNIEXPORT void JNICALL

JNIEXPORT void JNICALL

JNICALL
Java_com_example_myapp_MainActivity_getMessage(JNIEnv *env, jobject instance) {
    return (*env)->NewStringUTF(env, "Hello from native code!");
}




