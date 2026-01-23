package com.spongycode.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.spongycode.debukker.preferences.initializePreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializePreferences(this)
        setContent {
            App()
        }
    }
}