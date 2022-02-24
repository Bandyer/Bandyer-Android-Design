package com.bandyer.video_android_glass_ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.core.view.WindowInsetsCompat

internal abstract class ImmersiveActivity<T : BoundService>(clazz: Class<T>): BoundServiceActivity<T>(clazz) {

    private var decorView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterImmersiveMode()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        decorView = null
    }

    private fun enterImmersiveMode() {
        supportActionBar?.hide()
        decorView = window.decorView

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return
        // ATM there is no way of doing this on api > 30
        decorView!!.apply {
            setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0)
                    hideSystemUI()
            }
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            decorView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }
}