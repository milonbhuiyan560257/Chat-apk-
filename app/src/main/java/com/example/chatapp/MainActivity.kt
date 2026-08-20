package com.example.chatapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.chatapp.data.AppDatabase
import com.example.chatapp.data.UserPreferences
import com.example.chatapp.ui.ChatBubble
import com.example.chatapp.ui.Header

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = UserPreferences(this)
        val savedUriString = prefs.getLogoUri()
        val initialUri = savedUriString?.let { Uri.parse(it) }

        val db = AppDatabase.getDatabase(this)
        val chatDao = db.chatDao()

        setContent {
            var userLogo by remember { mutableStateOf(initialUri) }
            val messages by chatDao.getAllMessages().collectAsState(initial = emptyList())

            Column(modifier = Modifier.fillMaxSize()) {
                Header(context = this@MainActivity) { newUri ->
                    userLogo = newUri
                }
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(messages) { item ->
                        ChatBubble(
                            message = item.messageText,
                            userLogoUri = userLogo
                        )
                    }
                }
            }
        }
    }
}
