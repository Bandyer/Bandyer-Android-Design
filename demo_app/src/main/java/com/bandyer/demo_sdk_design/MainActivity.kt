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
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bandyer.demo_sdk_design.databinding.ActivityMainBinding
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.call.bottom_sheet.items.CallAction
import com.bandyer.sdk_design.call.dialogs.BandyerSnapshotDialog
import com.bandyer.sdk_design.smartglass.call.menu.SmartGlassActionItemMenu
import com.bandyer.sdk_design.smartglass.call.menu.SmartGlassMenuLayout
import com.bandyer.sdk_design.smartglass.call.menu.items.getSmartglassActions
import com.bandyer.sdk_design.smartglass.call.menu.utils.MotionEventInterceptor
import com.bandyer.sdk_design.whiteboard.dialog.BandyerWhiteboardTextEditorDialog
import com.bandyer.sdk_design.whiteboard.dialog.BandyerWhiteboardTextEditorDialog.BandyerWhiteboardTextEditorWidgetListener
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    companion object {
        const val tag = "MainAcitivy"
    }

    var mText: String? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))
        initializeListeners()
    }

    private fun initializeListeners() {
        binding.btnChat.setOnClickListener { startActivity(Intent(this, ChatActivity::class.java)) }

        binding.btnCall.setOnClickListener { startActivity(Intent(this, CallActivity::class.java)) }

        binding.btnSmartglassesMenu.setOnClickListener { showSmartGlassAction() }

        binding.btnWhiteboard.setOnClickListener { WhiteBoardDialog().show(this@MainActivity) }

        binding.btnRinging.setOnClickListener { startActivity(Intent(this, RingingActivity::class.java)) }

        binding.btnSwitchNightMode.setOnClickListener {
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

        binding.btnWhiteboardEditor.setOnClickListener {
            BandyerWhiteboardTextEditorDialog().show(this@MainActivity, mText, object : BandyerWhiteboardTextEditorWidgetListener {
                override fun onTextEditConfirmed(newText: String) {
                    mText = newText
                }
            })
        }

        binding.btnSnapshotPreview.setOnClickListener {
            BandyerSnapshotDialog().show(this@MainActivity)
        }

        binding.btnLivePointer.setOnClickListener { startActivity(Intent(this, PointerActivity::class.java)) }

        binding.btnBluetoothAudioroute.setOnClickListener { startActivity(Intent(this, BluetoothAudioRouteActivity::class.java)) }

        binding.btnFileShare.setOnClickListener { startActivity(Intent(this, FileShareActivity::class.java)) }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    private fun showSmartGlassAction(): SmartGlassActionItemMenu = SmartGlassActionItemMenu.show(
            appCompatActivity = this,
            items = CallAction.getSmartglassActions(
                    ctx = this,
                    micToggled = false,
                    cameraToggled = false))
            .apply {
                selectionListener = object : SmartGlassMenuLayout.OnSmartglassMenuSelectionListener {
                    override fun onSelected(item: ActionItem) {
                        Toast.makeText(applicationContext, item::class.java.simpleName, Toast.LENGTH_SHORT).show()
                        dismiss()
                        selectionListener = null
                    }

                    override fun onDismiss() = Unit
                }
                motionEventInterceptor = object : MotionEventInterceptor {
                    override fun onMotionEventIntercepted(event: MotionEvent?) {
                        Log.d(tag, "$event")
                    }
                }
            }
}