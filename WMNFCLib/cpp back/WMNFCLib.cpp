#include <jni.h>
#include <string>
#include "aesmodded.h"
#define SUCCESS									0x00

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_myapp_MainActivity_getNativeString(JNIEnv* env, jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

uint8_t defaultSystemKey[16] = { 0x03, 0xE3, 0xF1, 0x77, 0x01, 0x00, 0xC3, 0x0B,
                                 0x27, 0x68, 0x49, 0x20, 0x00, 0xFF, 0xE5, 0x0C };

uint8_t defaultSystemInitVectorKey[16] = { 0x08, 0x42, 0x00, 0x6F, 0x8E, 0x03, 0x33, 0xBC,
                                           0xF6, 0x91, 0x05, 0x43, 0x00, 0xEC ,   0x0A , 0x00 };




uint16_t EncryptBuffer(uint8_t* inputbuffer, uint8_t* WritingBuf, uint8_t aesDefaultKey[], uint8_t aesInitVector[], uint16_t datasize)
{
    AES_ctx ctx;
    memcpy(ctx.RoundKey, aesDefaultKey, 0x10);
    memcpy(ctx.Iv, aesInitVector, 0x10);
    AES_init_ctx_iv(&ctx, aesDefaultKey, aesInitVector);
    AES_CBC_encrypt_buffer(&ctx, inputbuffer, datasize);
    memcpy(WritingBuf, inputbuffer, datasize);

    //	AES_CBC_decrypt_buffer(&ctx, inputbuffer, 64);

    return SUCCESS;
}


uint16_t DecryptBuffer(uint8_t* inputbuffer, uint8_t* WritingBuf, uint8_t aesDefaultKey[], uint8_t aesInitVector[], uint16_t datasize)
{
    AES_ctx ctx;
    memcpy(ctx.RoundKey, aesDefaultKey, 0x10);
    memcpy(ctx.Iv, aesInitVector, 0x10);
    AES_init_ctx_iv(&ctx, aesDefaultKey, aesInitVector);
    //AES_CBC_encrypt_buffer(&ctx, inputbuffer, datasize);

    AES_CBC_decrypt_buffer(&ctx, inputbuffer, datasize);
    memcpy(WritingBuf, inputbuffer, datasize);

    return SUCCESS;
}



extern "C"
JNIEXPORT void JNICALL
Java_gt_nfc_wmlib_GTNFCWaterMeterClass_Java_1com_1example_1myapp_1MainActivity_1getNativeString(
        JNIEnv *env, jobject thiz) {
    // TODO: implement Java_com_example_myapp_MainActivity_getNativeString()

}