#include <android/asset_manager_jni.h>
#include <android/bitmap.h>

#include <jni.h>
#include <string>
#include <vector>
#include <sstream>
#include <thread>

#include "llm.hpp"

using namespace MNN::Transformer;

static std::vector<std::pair<std::string, std::string>> history;
static std::unique_ptr<Llm> llm(nullptr);
static std::stringstream response_buffer;

extern "C" {

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    history.push_back(std::make_pair("system", "你是用户的助手。"));
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved) {
}

JNIEXPORT jboolean JNICALL Java_work_niggergo_localchat_Chat_Init(JNIEnv* env, jobject thiz, jstring modelDir) {
    const char* model_dir = env->GetStringUTFChars(modelDir, 0);
    if (!llm.get()) {
        llm.reset(Llm::createLLM(model_dir));
        llm->load();
    }
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_work_niggergo_localchat_Chat_Ready(JNIEnv* env, jobject thiz) {
    if (llm.get()) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

JNIEXPORT jstring JNICALL Java_work_niggergo_localchat_Chat_Submit(JNIEnv* env, jobject thiz, jstring inputStr) {
    if (!llm.get()) {
        return env->NewStringUTF("Failed, Chat is not ready!");
    }
    const char* input_str = env->GetStringUTFChars(inputStr, 0);
    auto chat = [&](std::string str) {
        llm->response(str, &response_buffer, "<eop>");
    };
    std::thread chat_thread(chat, input_str);
    chat_thread.detach();
    jstring result = env->NewStringUTF("Submit success!");
    return result;
}

JNIEXPORT jstring JNICALL Java_work_niggergo_localchat_Chat_HistoryChat(JNIEnv* env, jobject thiz, jstring input) {
    if (!llm.get()) {
        return env->NewStringUTF("没有初始化，问什么问呐？");
    }
    const char* input_str = env->GetStringUTFChars(input, 0);
    history.emplace_back(std::make_pair("user", input_str));
    std::ostringstream lineOs;
    llm->response(history, &lineOs, nullptr, 1);
    auto line = lineOs.str();
    while (!llm->stoped()) {
        llm->generate(1);
        line = lineOs.str();
    }
    history.emplace_back(std::make_pair("assistant", line));
    jstring result = env->NewStringUTF(line.c_str());
    return result;
}

JNIEXPORT jbyteArray JNICALL Java_work_niggergo_localchat_Chat_Response(JNIEnv* env, jobject thiz) {
    auto len = response_buffer.str().size();
    jbyteArray res = env->NewByteArray(len);
    env->SetByteArrayRegion(res, 0, len, (const jbyte*)response_buffer.str().c_str());
    return res;
}

JNIEXPORT void JNICALL Java_work_niggergo_localchat_Chat_Done(JNIEnv* env, jobject thiz) {
    response_buffer.str("");
}

JNIEXPORT void JNICALL Java_work_niggergo_localchat_Chat_Reset(JNIEnv* env, jobject thiz) {
    llm->reset();
}

} // extern "C"