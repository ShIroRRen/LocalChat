package work.niggergo.localchat

import java.io.Serializable

class Jni : Serializable {
  external fun Init(modelDir: String): Boolean
  external fun HistoryChat(system: String, input: String): String
  external fun Done()
  external fun Reset()
  
  companion object {
    init {
      System.loadLibrary("nga-chat")
    }
  }
}
