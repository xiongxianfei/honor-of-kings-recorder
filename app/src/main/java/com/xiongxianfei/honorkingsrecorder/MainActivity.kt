package com.xiongxianfei.honorkingsrecorder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.xiongxianfei.honorkingsrecorder.ui.navigation.AppNavHost
import com.xiongxianfei.honorkingsrecorder.ui.theme.HonorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HonorTheme {
                AppNavHost()
            }
        }
    }
}
