package com.example.calculandoosilencio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.calculandoosilencio.ui.theme.CalculandoOSilencioTheme
import com.example.calculandoosilencio.ui.theme.TransactionScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculandoOSilencioTheme {
                TransactionScreen()
            }
        }
    }
}