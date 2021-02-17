/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.demo_sdk_design

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bandyer.sdk_design.call.dialogs.BandyerSnapshotDialog
import com.bandyer.sdk_design.whiteboard.dialog.BandyerWhiteboardTextEditorDialog
import com.bandyer.sdk_design.whiteboard.dialog.BandyerWhiteboardTextEditorDialog.BandyerWhiteboardTextEditorWidgetListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton


class MainActivity : AppCompatActivity() {

    var mText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val main = LayoutInflater.from(this).inflate(R.layout.activity_main, null)
        val viewGroup = window.decorView as ViewGroup
        viewGroup.addView(main)

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))
        setActionBarTopMargin()
        initializeListeners()
    }

    private fun initializeListeners() {
        findViewById<MaterialButton>(R.id.btn_chat).setOnClickListener { startActivity(Intent(this, ChatActivity::class.java)) }

        findViewById<MaterialButton>(R.id.btn_call).setOnClickListener { startActivity(Intent(this, CallActivity::class.java)) }

        findViewById<MaterialButton>(R.id.btn_whiteboard).setOnClickListener { startActivity(Intent(this, WhiteboardActivity::class.java)) }

        findViewById<MaterialButton>(R.id.btn_ringing).setOnClickListener { startActivity(Intent(this, RingingActivity::class.java)) }

        findViewById<MaterialButton>(R.id.btn_switch_night_mode).setOnClickListener {
            val isNightTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (isNightTheme) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    window.setWindowAnimations(R.style.Bandyer_ThemeTransitionAnimation)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    window.setWindowAnimations(R.style.Bandyer_ThemeTransitionAnimation)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }

        findViewById<MaterialButton>(R.id.btn_whiteboard_editor).setOnClickListener {
            BandyerWhiteboardTextEditorDialog().show(this@MainActivity, mText, object : BandyerWhiteboardTextEditorWidgetListener {
                override fun onTextEditConfirmed(newText: String) {
                    mText = newText
                }
            })
        }

        findViewById<MaterialButton>(R.id.btn_snapshot_preview).setOnClickListener {
            BandyerSnapshotDialog().show(this@MainActivity)
        }

        findViewById<MaterialButton>(R.id.btn_live_pointer).setOnClickListener { startActivity(Intent(this, PointerActivity::class.java)) }

        findViewById<MaterialButton>(R.id.btn_bluetooth_audioroute).setOnClickListener { startActivity(Intent(this, BluetoothAudioRouteActivity::class.java)) }
    }

    private fun setActionBarTopMargin() {
        (findViewById<AppBarLayout>(R.id.app_bar_layout).layoutParams as ViewGroup.MarginLayoutParams).topMargin = getStatusBarHeight()
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

}