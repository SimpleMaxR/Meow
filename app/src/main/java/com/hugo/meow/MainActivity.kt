package com.hugo.meow

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hugo.imagepreviewer.utils.AppDatabase
import com.hugo.meow.data.NetworkService
import com.hugo.meow.ui.MeowApp
import com.hugo.meow.ui.MeowViewModel
import com.hugo.meow.ui.theme.MeowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.getDatabase(this)
        val networkService = NetworkService(database, applicationContext)
        val meowViewModel = MeowViewModel(networkService, database)
        setContent {
            MeowTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    MeowApp(meowViewModel)
                }
            }
        }
    }
}