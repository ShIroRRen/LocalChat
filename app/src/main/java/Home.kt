package zip.latestfile.localchat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent.getIntent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jeziellago.compose.markdowntext.MarkdownText
import zip.latestfile.localchat.ui.theme.LocalChatTheme
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.*

open class Init : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}


fun copyAssetFolderToFilesDirIfNotExists(context: Context, assetFolderName: String, targetFolderName: String) {
    val targetFolder = File(context.filesDir, targetFolderName)
    if (!targetFolder.exists()) {
        targetFolder.mkdirs()
        copyAssetRecursively(context, assetFolderName, targetFolder)
    }
}

fun copyAssetRecursively(context: Context, assetFolderPath: String, targetFolder: File) {
    val assetManager = context.assets
    val files = assetManager.list(assetFolderPath) ?: return
    for (fileName in files) {
        val assetFilePath = "$assetFolderPath/$fileName"
        val targetFile = File(targetFolder, fileName)
        val subFiles = assetManager.list(assetFilePath)
        if (subFiles?.isNotEmpty() == true) {
            targetFile.mkdirs()
            copyAssetRecursively(context, assetFilePath, targetFile)
        } else {
            val inputStream: InputStream = assetManager.open(assetFilePath)
            copyStreamToFile(inputStream, targetFile)
        }
    }
}

fun copyStreamToFile(inputStream: InputStream, outFile: File) {
    val outputStream = FileOutputStream(outFile)
    val buffer = ByteArray(1024)
    var length: Int
    while (inputStream.read(buffer).also { length = it } > 0) {
        outputStream.write(buffer, 0, length)
    }
    inputStream.close()
    outputStream.close()
}

open class Home : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        copyAssetFolderToFilesDirIfNotExists(this, "model", "model")
        Chat().Init(File(this.filesDir, "model/config.json").absolutePath)
        setContent {
            server.body(this, this@Home)
        }
    }
}

abstract class server {

    companion object {
        @Composable
        fun MessageItem(message: String) {
            Column(modifier = Modifier.fillMaxWidth()) {
                MarkdownText(markdown = message, modifier = Modifier.padding(8.dp), isTextSelectable = true)
                Divider(color = Color.Gray, thickness = 1.dp)
            }
        }

        @Preview
        @SuppressLint("ComposableNaming")
        @Composable
        fun body(context: Context, activity: Activity) {
            var inputText by remember { mutableStateOf(TextFieldValue()) }
            var messages by remember { mutableStateOf(listOf<String>()) }
            var canDo = true
            LocalChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                        Row(
                            modifier = Modifier
                                .padding(top = 10.dp, start = 30.dp)
                                .height(50.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.mipmap.icon),
                                contentDescription = "图标",
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(40.dp)
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(it)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                items(messages) { message ->
                                    MessageItem(message = message)
                                }
                            }
                            Spacer(modifier = Modifier.weight(0.01f))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { it -> inputText = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp),
                                    label = { Text("输入消息") }
                                )
                                Button(
                                    onClick = {
                                        if (inputText.text.isNotEmpty()) {
                                            if (canDo) {
                                                canDo = false
                                                messages = messages + ("Q: " + inputText.text)
                                                val temp = inputText.text
                                                inputText = TextFieldValue()
                                                Thread {
                                                    if (inputText.text == "/reset") {
                                                        Chat().Reset()
                                                    } else {
                                                        messages =
                                                            messages + ("A: " + Chat().HistoryChat(temp))
                                                        Chat().Done()
                                                        messages = messages.filter { it.isNotBlank() }
                                                    }
                                                    canDo = true
                                                }.start()
                                            }
                                        }
                                    },
                                    modifier = Modifier.padding(start = 8.dp)
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
