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

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bandyer.video_android_phone_ui.call.widgets.LivePointerView
import com.google.android.material.appbar.MaterialToolbar
import java.util.*

class PointerActivity : AppCompatActivity() {

    private var livePointerView: LivePointerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pointer)

        initializeActionBar()

        livePointerView = findViewById(R.id.pointerView)
        livePointerView?.updateLabelText("Requester")
        updatePointerViewPosition()
    }

    override fun onResume() {
        super.onResume()
        updatePointerViewPosition()
    }

    override fun onPause() {
        super.onPause()
        livePointerView?.removeCallbacks(null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if(item.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun initializeActionBar() {
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = resources.getString(R.string.live_pointer)
    }

    private fun updatePointerViewPosition() {
        val random = Random()
        val randomX = random.nextInt(101)
        val randomY = random.nextInt(101)
        livePointerView?.updateLivePointerPosition(randomX.toFloat(), randomY.toFloat())
        livePointerView?.postDelayed({ updatePointerViewPosition() }, 1000)
    }
}