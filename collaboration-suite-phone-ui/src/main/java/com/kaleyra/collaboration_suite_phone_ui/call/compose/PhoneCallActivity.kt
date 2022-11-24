package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import com.google.android.material.composethemeadapter.MdcTheme

class PhoneCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CompositionLocalProvider(LocalBackPressedDispatcher provides onBackPressedDispatcher) {
                MdcTheme(setDefaultFontFamily = true) {
                    CallScreen()
                }
            }
        }
    }
}
