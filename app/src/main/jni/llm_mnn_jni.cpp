#include <sstream>
#include <string>
#include <thread>
#include <vector>

#include <jni.h>

#include <android/asset_manager_jni.h>
#include <android/bitmap.h>

#include "llm.hpp"

using namespace std;
using namespace MNN::Transformer;

vector<pair<string, string>> static history;
unique_ptr<Llm> static llm(nullptr);
// static stringstream response_buffer;

extern "C" {

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	history.push_back(make_pair("system", "你是用户的助手。"));
	return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved) {}

JNIEXPORT jboolean JNICALL Java_work_niggergo_localchat_Jni_init(JNIEnv* env, jobject thiz,
																 jstring config) {
	if (!llm.get()) {
		char const* config_str = env->GetStringUTFChars(config, 0);
		llm.reset(Llm::createLLM(config_str));
		env->ReleaseStringUTFChars(config, config_str);
	}
	llm->load();
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_work_niggergo_localchat_Jni_ready(JNIEnv* env, jobject thiz) {
	if (llm.get()) return JNI_TRUE;
	return JNI_FALSE;
}

JNIEXPORT jstring JNICALL Java_work_niggergo_localchat_Jni_historyChat(JNIEnv* env, jobject thiz,
																	   jstring system, jstring input) {
	if (!llm.get()) return env->NewStringUTF("没有初始化，问什么问呐？");
	char const* system_str = env->GetStringUTFChars(system, 0);
	if (!string(system_str).empty() && !history.empty() && history[0].first == "system")
		history[0].second = system_str;
	env->ReleaseStringUTFChars(system, system_str);
	char const* input_str = env->GetStringUTFChars(input, 0);
	history.emplace_back(make_pair("user", input_str));
	env->ReleaseStringUTFChars(input, input_str);
	ostringstream lineOs;
	llm->response(history, &lineOs, nullptr, 1);
	auto line = lineOs.str();
	while (!llm->stoped()) {
		llm->generate(1);
		line = lineOs.str();
	}
	history.emplace_back(make_pair("assistant", line));
	jstring result = env->NewStringUTF(line.c_str());
	return result;
}

JNIEXPORT void JNICALL Java_work_niggergo_localchat_Jni_done(JNIEnv* env, jobject thiz) {
	// response_buffer.str("");
}

JNIEXPORT void JNICALL Java_work_niggergo_localchat_Jni_reset(JNIEnv* env, jobject thiz) { llm->reset(); }
}