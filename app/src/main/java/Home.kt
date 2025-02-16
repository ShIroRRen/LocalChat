package work.niggergo.localchat

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jeziellago.compose.markdowntext.MarkdownText
import work.niggergo.localchat.uicfg.LocalChatTheme
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun cpAssets(context: Context, assetPath: String, targetPath: String) {
  val targetPathFile = File(context.filesDir, targetPath)
  if (!targetPathFile.exists()) {
    targetPathFile.mkdirs()
    cpAssetsR(context, assetPath, targetPathFile)
  }
}

fun cpAssetsR(context: Context, assetPath: String, targetPath: File) {
  val assetManager = context.assets
  val files = assetManager.list(assetPath) ?: return
  for (fileName in files) {
    val assetFilePath = "$assetPath/$fileName"
    val targetFile = File(targetPath, fileName)
    val subFiles = assetManager.list(assetFilePath)
    if (subFiles?.isNotEmpty() == true) {
      targetFile.mkdirs()
      cpAssetsR(context, assetFilePath, targetFile)
    } else {
      cp2File(assetManager.open(assetFilePath), targetFile)
    }
  }
}

fun cp2File(ins: InputStream, out: File) {
  val outs = FileOutputStream(out)
  val buffer = ByteArray(1024)
  var length: Int
  while (ins.read(buffer).also { length = it } > 0) outs.write(buffer, 0, length)
  ins.close()
  outs.close()
}

open class Home : ComponentActivity() {
  @Composable
  private fun messageItem(message: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
      MarkdownText(markdown = message, modifier = Modifier.padding(8.dp), isTextSelectable = true)
      HorizontalDivider(color = Color.Gray, thickness = 1.dp)
    }
  }
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    cpAssets(this, "model", "model")
    Jni().Init(File(this.filesDir, "model/config.json").absolutePath)
    setContent {
      var systemTxt by remember { mutableStateOf(TextFieldValue()) }
      var inputTxt by remember { mutableStateOf(TextFieldValue()) }
      var messages by remember { mutableStateOf(listOf<String>()) }
      var canDo = true
      LocalChatTheme {
        Surface(
          modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
          Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            Row(
              modifier = Modifier.padding(top = 10.dp, start = 30.dp).height(50.dp).fillMaxWidth(),
              horizontalArrangement = Arrangement.Start,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Image(
                painter = painterResource(id = R.mipmap.icon),
                contentDescription = "图标",
                modifier = Modifier.height(40.dp).width(40.dp)
              )
              Text(
                modifier = Modifier.padding(start = 20.dp),
                text = stringResource(id = R.string.app_name),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }) {
            Column(
              modifier = Modifier.fillMaxSize().padding(it)
            ) {
              OutlinedTextField(
                value = systemTxt,
                onValueChange = { systemTxt = it },
                modifier = Modifier.weight(2f).padding(8.dp).fillMaxWidth(),
                label = { Text("提示词") },
              )
              LazyColumn(
                modifier = Modifier.weight(8f).fillMaxWidth().padding(8.dp)
              ) {
                items(messages) { message ->
                  messageItem(message = message)
                }
              }
              Spacer(modifier = Modifier.weight(0.01f))
              Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedTextField(
                  value = inputTxt,
                  onValueChange = { inputTxt = it },
                  modifier = Modifier.weight(1f).padding(8.dp),
                  label = { Text("输入消息") })
                Button(
                  onClick = {
                    if (inputTxt.text.isNotEmpty()) {
                      if (canDo) {
                        canDo = false
                        messages = messages + ("Q: " + inputTxt.text)
                        val temp = inputTxt.text
                        inputTxt = TextFieldValue()
                        Thread {
                          if (inputTxt.text == "/reset") {
                            Jni().Reset()
                          } else {
                            messages =
                              messages + ("A: " + Jni().HistoryChat(systemTxt.text, temp).replace("<think>", "\n```\n")
                                .replace("</think>", "\n```\n"))
                            Jni().Done()
                            messages = messages.filter { it.isNotBlank() }
                          }
                          canDo = true
                        }.start()
                      }
                    }
                  }, modifier = Modifier.padding(start = 8.dp)
                ) {
                  Text("→")
                }
              }
            }
          }
        }
      }
    }
  }
}