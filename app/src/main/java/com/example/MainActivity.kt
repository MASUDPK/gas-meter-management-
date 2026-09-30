package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GasScreen
import com.example.ui.GasViewModel
import com.example.ui.theme.JamilaBhavanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JamilaBhavanTheme {
                val viewModel: GasViewModel = viewModel()
                GasScreen(viewModel = viewModel)
            }
        }
    }
}
