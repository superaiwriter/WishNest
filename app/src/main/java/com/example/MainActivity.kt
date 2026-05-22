package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.WishNestAppContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WishNestViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: WishNestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = viewModel.isDarkTheme) {
                WishNestAppContainer(viewModel = viewModel)
            }
        }
    }
}
