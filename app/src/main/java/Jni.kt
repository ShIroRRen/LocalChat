package work.niggergo.localchat

import dalvik.annotation.optimization.FastNative

object Jni {
	@JvmStatic
	@FastNative
	external fun init(modelDir: String): Boolean

	@JvmStatic
	@FastNative
	external fun historyChat(system: String, input: String): String

	@JvmStatic
	@FastNative
	external fun done()

	@JvmStatic
	@FastNative
	external fun reset()

	init {
		System.loadLibrary("nga-chat")
	}
}
