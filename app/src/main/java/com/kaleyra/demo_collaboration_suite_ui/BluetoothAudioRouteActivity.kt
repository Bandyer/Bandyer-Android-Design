/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.demo_collaboration_suite_ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.items.AdapterActionItem
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view.AudioRouteState
import com.kaleyra.collaboration_suite_phone_ui.call.bottom_sheet.items.AudioRoute
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

class BluetoothAudioRouteActivity : AppCompatActivity() {

    var fastItemAdapter = ItemAdapter<AdapterActionItem>()
    var fastAdapter = FastAdapter.with(fastItemAdapter)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_audio_route)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.kaleyra_bluetooth_items)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = fastAdapter

        val routes = initDummyDevices()

        fastItemAdapter.set(routes.map { AdapterActionItem(it) })
    }

    private fun initDummyDevices(): List<AudioRoute> {
        val routes = mutableListOf<AudioRoute>()

        routes.add(AudioRoute.BLUETOOTH(this,
                "dummy1",
                "Bluetooth Earphones",
                50,
                AudioRouteState.BLUETOOTH.valueOf(AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.PLAYING_AUDIO.name)))

        routes.add(AudioRoute.BLUETOOTH(this,
                "dummy2",
                "Bluetooth Earphones",
                50,
                AudioRouteState.BLUETOOTH.valueOf(AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.CONNECTED.name)))

        routes.add(AudioRoute.BLUETOOTH(this,
                "dummy3",
                "Bluetooth Earphones",
                null,
                AudioRouteState.BLUETOOTH.valueOf(AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.DISCONNECTED.name)))

        routes.add(AudioRoute.BLUETOOTH(this,
                "dummy4",
                "Bluetooth Earphones",
                50,
                AudioRouteState.BLUETOOTH.valueOf(AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.DEACTIVATING.name)))

        routes.add(AudioRoute.BLUETOOTH(this,
                "dummy5",
                "Bluetooth Earphones",
                null,
                AudioRouteState.BLUETOOTH.valueOf(AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.FAILED.name)))

        routes.add(AudioRoute.BLUETOOTH(this,
                "dummy6",
                "Bluetooth Earphones",
                null,
                AudioRouteState.BLUETOOTH.valueOf(AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.CONNECTING.name)))

        routes.add(AudioRoute.BLUETOOTH(this,
                "dummy7",
                "Bluetooth Earphones",
                null,
                AudioRouteState.BLUETOOTH.valueOf(AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.AVAILABLE.name)))

        routes.add(AudioRoute.BLUETOOTH(this,
                "dummy8",
                "Bluetooth Earphones",
                null,
                AudioRouteState.BLUETOOTH.valueOf(AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.ACTIVATING.name)))

        routes.add(AudioRoute.BLUETOOTH(this,
                "dummy9",
                "Bluetooth Earphones",
                50,
                AudioRouteState.BLUETOOTH.valueOf(AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.ACTIVE.name)))

        routes.add(AudioRoute.BLUETOOTH(this,
                "dummy10",
                "Bluetooth Earphones",
                50,
                AudioRouteState.BLUETOOTH.valueOf(AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.CONNECTING_AUDIO.name)))

        return routes
    }
}