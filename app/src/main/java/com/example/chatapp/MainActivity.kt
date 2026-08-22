package com.example.chatapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chatapp.data.AppDatabase
import com.example.chatapp.data.ChatMessage
import com.example.chatapp.data.UserPreferences
import com.example.chatapp.ui.ChatBubble
import com.example.chatapp.ui.Header
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = UserPreferences(this)

        val savedUriString = prefs.getLogoUri()
        val initialUri = savedUriString?.let {
            Uri.parse(it)
        }

        val db = AppDatabase.getDatabase(this)
        val chatDao = db.chatDao()

        setContent {

            var userLogo by remember {
                mutableStateOf(initialUri)
            }

            var messageText by remember {
                mutableStateOf("")
            }

            var selectedImageUri by remember {
                mutableStateOf<Uri?>(null)
            }

            val messages by chatDao
                .getAllMessages()
                .collectAsState(initial = emptyList())

            val scope = rememberCoroutineScope()

            /*
             * Gallery picker
             */
            val imagePicker =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri: Uri? ->

                    uri?.let {

                        try {
                            contentResolver.takePersistableUriPermission(
                                it,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (_: SecurityException) {
                            // কিছু provider persistable permission দেয় না
                        }

                        selectedImageUri = it
                    }
                }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                /*
                 * Header / Logo
                 */
                Header(
                    context = this@MainActivity
                ) { newUri ->
                    userLogo = newUri
                }

                /*
                 * Chat list
                 */
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {

                    items(
                        items = messages,
                        key = { it.id }
                    ) { item ->

                        ChatBubble(
                            message = item.messageText,
                            imageUri = item.imageUri?.let {
                                Uri.parse(it)
                            },
                            userLogoUri = userLogo
                        )
                    }
                }

                /*
                 * Message input
                 */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    /*
                     * Image button
                     */
                    IconButton(
                        onClick = {
                            imagePicker.launch(
                                arrayOf("image/*")
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Select Image",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    /*
                     * Text box
                     */
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = {
                            messageText = it
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text("Message...")
                        },
                        singleLine = true
                    )

                    /*
                     * Send button
                     */
                    IconButton(
                        enabled = messageText.isNotBlank() ||
                                selectedImageUri != null,

                        onClick = {

                            val text = messageText.trim()

                            val image = selectedImageUri?.toString()

                            /*
                             * Database-এ message save
                             */
                            scope.launch {

                                chatDao.insertMessage(
                                    ChatMessage(
                                        messageText = text,
                                        imageUri = image
                                    )
                                )
                            }

                            /*
                             * Input clear
                             */
                            messageText = ""
                            selectedImageUri = null
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        }
    }
}
