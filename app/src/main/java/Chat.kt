package work.niggergo.localchat

import java.io.Serializable

class Chat : Serializable {
    external fun Init(modelDir: String?): Boolean
    external fun Submit(input: String?): String?
    external fun HistoryChat(input: String): String
    external fun Response(): ByteArray?
    external fun Done()
    external fun Reset()

    companion object {
        init {
            System.loadLibrary("llm")
        }
    }
}
