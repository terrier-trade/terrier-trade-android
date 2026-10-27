package edu.bu.terriertrade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import edu.bu.terriertrade.ui.TerrierTradeApp
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TerrierTradeTheme {
                TerrierTradeApp()
            }
        }
    }
}