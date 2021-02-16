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

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.bandyer.android_audiosession.model.AudioOutputDevice.EARPIECE
import com.bandyer.android_audiosession.router.LastConnectedDeviceRoutingStrategy
import com.bandyer.android_audiosession.session.AudioCallSession
import com.bandyer.android_audiosession.session.AudioCallSessionListener
import com.bandyer.android_audiosession.session.AudioCallSessionState
import com.bandyer.android_audiosession.session.audioCallSessionOptions
import com.bandyer.sdk_design.bottom_sheet.BandyerBottomSheet
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.bottom_sheet.view.AudioRouteState
import com.bandyer.sdk_design.call.bottom_sheet.OnAudioRouteBottomSheetListener
import com.bandyer.sdk_design.call.bottom_sheet.items.AudioRoute
import com.bandyer.sdk_design.call.bottom_sheet.items.CallAction
import com.bandyer.sdk_design.call.bottom_sheet.items.CallAction.CAMERA
import com.bandyer.sdk_design.call.bottom_sheet.items.CallAction.Items.getActions
import com.bandyer.sdk_design.call.widgets.BandyerCallActionWidget
import com.bandyer.sdk_design.call.widgets.BandyerCallInfoWidget
import com.bandyer.sdk_design.extensions.getScreenSize
import java.util.*

class CallActivity : AppCompatActivity(), OnAudioRouteBottomSheetListener, BandyerCallActionWidget.OnClickListener {

    private val TAG = "CallActivity"

    private var callActionWidget: BandyerCallActionWidget<ActionItem, BandyerBottomSheet>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val main = LayoutInflater.from(this).inflate(R.layout.activity_call, null)
        val viewGroup = window.decorView as ViewGroup
        viewGroup.addView(main)

        initializeAudioSession()
        initializeUI(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (callActionWidget != null) callActionWidget?.saveInstanceState(outState)
    }

    @SuppressLint("NewApi")
    private fun initializeAudioSession() {
        if (AudioCallSession.getInstance().audioSessionState != AudioCallSessionState.UNINITIALIZED) return
        AudioCallSession.getInstance().start(this.applicationContext, audioCallSessionOptions {
            discoveryOptions = {
                earpiece = true
                loudspeaker = true
                wiredHeadset = true
                bluetoothHeadsetDiscoveryOptions = {
                    discoverNearbyHeadsets = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                }
                routingStrategy = LastConnectedDeviceRoutingStrategy(applicationContext, EARPIECE())
            }
        }, object : AudioCallSessionListener {

            override fun onOutputDeviceConnecting(currentAudioOutputDevice: AudioOutputDevice?, connectingAudioOutputDevice: AudioOutputDevice?, availableOutputs: List<AudioOutputDevice>) {
                connectingAudioOutputDevice ?: return
                onAudioOutputsChanged(availableOutputs)
                currentAudioOutputDevice?.let {
                    callActionWidget?.selectAudioRoute(getAudioRoute(connectingAudioOutputDevice))
                }
                Log.e(TAG, "${Thread.currentThread()} onOutputDeviceConnecting: $connectingAudioOutputDevice ${connectingAudioOutputDevice.name} currentDevice: $currentAudioOutputDevice ${currentAudioOutputDevice?.name}")
            }

            override fun onOutputDeviceConnected(oldAudioOutputDevice: AudioOutputDevice?, connectedAudioOutputDevice: AudioOutputDevice?, availableOutputs: List<AudioOutputDevice>, userSelected: Boolean) {
                connectedAudioOutputDevice ?: return
                onAudioOutputsChanged(availableOutputs)
                callActionWidget?.selectAudioRoute(getAudioRoute(connectedAudioOutputDevice))
                Log.e(TAG, "${Thread.currentThread()} onOutputDeviceConnected: $connectedAudioOutputDevice ${connectedAudioOutputDevice.name} oldDevice: $oldAudioOutputDevice ${oldAudioOutputDevice?.name}")
                if (!userSelected) return
//                window.decorView.postDelayed({
//                    callActionWidget!!.collapse()
//                }, resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
            }

            override fun onOutputDeviceAttached(currentAudioOutputDevice: AudioOutputDevice?, attachedAudioOutputDevice: AudioOutputDevice, availableOutputs: List<AudioOutputDevice>) {
                onAudioOutputsChanged(availableOutputs)
                currentAudioOutputDevice ?: return
                callActionWidget?.selectAudioRoute(getAudioRoute(currentAudioOutputDevice))
                Log.e(TAG, "${Thread.currentThread()} onOutputDeviceAttached: $attachedAudioOutputDevice ${attachedAudioOutputDevice.name} currentDevice: $currentAudioOutputDevice ${currentAudioOutputDevice.name}")
            }

            override fun onOutputDeviceUpdated(currentAudioOutputDevice: AudioOutputDevice?, updatedAudioOutputDevice: AudioOutputDevice, availableOutputs: List<AudioOutputDevice>) {
                onAudioOutputsChanged(availableOutputs)
                callActionWidget?.updateAudioRoute(getAudioRoute(updatedAudioOutputDevice))
                currentAudioOutputDevice ?: return
                callActionWidget?.selectAudioRoute(getAudioRoute(currentAudioOutputDevice))
                Log.e(TAG, "${Thread.currentThread()} onOutputDeviceUpdated: $updatedAudioOutputDevice ${updatedAudioOutputDevice.name} currentDevice: $currentAudioOutputDevice ${currentAudioOutputDevice.name}")
            }

            override fun onOutputDeviceDetached(currentAudioOutputDevice: AudioOutputDevice?, detachedAudioOutputDevice: AudioOutputDevice, availableOutputs: List<AudioOutputDevice>) {
                Log.e(TAG, "${Thread.currentThread()} onOutputDeviceDetached: $detachedAudioOutputDevice ${detachedAudioOutputDevice.name} currentDevice: $currentAudioOutputDevice ${currentAudioOutputDevice?.name}")
                onAudioOutputsChanged(availableOutputs)
                currentAudioOutputDevice ?: return
                callActionWidget?.selectAudioRoute(getAudioRoute(currentAudioOutputDevice))
            }
        })

    }

    private fun onAudioOutputsChanged(availableOutputs: List<AudioOutputDevice>) {
        callActionWidget!!.setAudioRouteItems(availableOutputs.map { getAudioRoute(it) }.plus(getMutedAudioRoute()))
    }

    private fun initializeUI(savedInstanceState: Bundle?) {
        initializeCallInfoWidget()
        initializeBottomSheetLayout(savedInstanceState)
    }

    private fun initializeBottomSheetLayout(savedInstanceState: Bundle?) {
        callActionWidget = BandyerCallActionWidget(this, findViewById(R.id.coordinator_layout), getActions(this, true, false, true, true, true,true))

        callActionWidget!!.onAudioRoutesRequest = this
        callActionWidget!!.onClickListener = this
        callActionWidget!!.restoreInstanceState(savedInstanceState)

        callActionWidget!!.showCallControls(true)
        AudioCallSession.getInstance().currentAudioOutputDevice?.let {
            callActionWidget?.selectAudioRoute(getAudioRoute(it))
        }
    }

    private fun initializeCallInfoWidget() {
        val callInfoWidget = findViewById<BandyerCallInfoWidget>(R.id.call_info)
        callInfoWidget.setTitle("Bob Martin, John Doe, Mark Smith, Julie Randall")
        callInfoWidget.setSubtitle("Dialing...")
        callInfoWidget.setRecordingText("Recording in progress...")
        callInfoWidget.setRecording(true)
    }

    private fun getAudioRoute(audioOutputDevice: AudioOutputDevice): AudioRoute {
        val isActive = AudioCallSession.getInstance().currentAudioOutputDevice == audioOutputDevice
        return when (audioOutputDevice) {
            is AudioOutputDevice.BLUETOOTH -> AudioRoute.BLUETOOTH(this,
                    identifier = audioOutputDevice.identifier,
                    name = audioOutputDevice.name ?: "",
                    batteryLevel = audioOutputDevice.batteryLevel,
                    bluetoothConnectionStatus = AudioRouteState.BLUETOOTH.valueOf(audioOutputDevice.bluetoothConnectionStatus.name))
            is AudioOutputDevice.NONE -> AudioRoute.MUTED(this, audioOutputDevice.identifier, audioOutputDevice.name, isActive)
            is EARPIECE -> AudioRoute.EARPIECE(this, audioOutputDevice.identifier, audioOutputDevice.name, isActive)
            is AudioOutputDevice.LOUDSPEAKER -> AudioRoute.LOUDSPEAKER(this, audioOutputDevice.identifier, audioOutputDevice.name, isActive)
            is AudioOutputDevice.WIRED_HEADSET -> AudioRoute.WIRED_HEADSET(this, audioOutputDevice.identifier, audioOutputDevice.name, isActive)
        }
    }

    override fun onAudioRoutesRequested(): List<AudioRoute> {
        val routes = mutableListOf<AudioRoute>()
        for (device in AudioCallSession.getInstance().getAvailableAudioOutputDevices) {
            val isActive = AudioCallSession.getInstance().currentAudioOutputDevice == device
            when (device) {
                is AudioOutputDevice.BLUETOOTH -> routes.add(AudioRoute.BLUETOOTH(this,
                        device.identifier,
                        device.name,
                        device.batteryLevel,
                        AudioRouteState.BLUETOOTH.valueOf(device.bluetoothConnectionStatus.name)))
                is AudioOutputDevice.WIRED_HEADSET -> routes.add(AudioRoute.WIRED_HEADSET(this, device.identifier, device.name, isActive))
                is AudioOutputDevice.LOUDSPEAKER -> routes.add(AudioRoute.LOUDSPEAKER(this, device.identifier, device.name, isActive))
                is EARPIECE -> routes.add(AudioRoute.EARPIECE(this, device.identifier, device.name, isActive))
                is AudioOutputDevice.NONE -> {
                    // nothing to add
                }
            }
        }
        routes.add(getMutedAudioRoute())
        return routes
    }

    override fun onAudioRouteClicked(item: AudioRoute, position: Int): Boolean {
        AudioCallSession.getInstance().currentAudioOutputDevice?.let {
            callActionWidget!!.selectAudioRoute(getAudioRoute(it))
        }

        val device = when (item) {
            is AudioRoute.BLUETOOTH -> item.toAudioOutputDevice()
            is AudioRoute.MUTED -> AudioOutputDevice.NONE() // mute stream audio
            is AudioRoute.EARPIECE -> EARPIECE()
            is AudioRoute.LOUDSPEAKER -> AudioOutputDevice.LOUDSPEAKER()
            is AudioRoute.WIRED_HEADSET -> AudioOutputDevice.WIRED_HEADSET()
        }.apply { this.name = item.name }

        if (item::class.java == (AudioCallSession.getInstance().currentAudioOutputDevice!!)::class.java) {
            callActionWidget!!.selectAudioRoute(item)
            return true
        }

        AudioCallSession.getInstance().changeAudioOutputDevice(device)
        return true
    }

    private fun getMutedAudioRoute() = AudioRoute.MUTED(this, UUID.randomUUID().toString(), resources.getString(R.string.bandyer_call_action_audio_route_muted), AudioCallSession.getInstance().currentAudioOutputDevice is AudioOutputDevice.NONE)

    override fun onCallActionClicked(item: CallAction, position: Int): Boolean {
        if (item is CAMERA) {
            // if permissions toggle
            item.toggle()
            return true
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioCallSession.getInstance().dispose()
    }


    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            enterPictureInPictureMode(PictureInPictureParams.Builder().setAspectRatio(Rational(getScreenSize().x, getScreenSize().y)).build())
        }
    }
}

private fun AudioRoute.BLUETOOTH.toAudioOutputDevice(): AudioOutputDevice.BLUETOOTH = AudioOutputDevice.BLUETOOTH(this.identifier).apply {
    this.address = this.identifier
    this.batteryLevel = this@toAudioOutputDevice.batteryLevel
    this.bluetoothConnectionStatus = AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.valueOf(this.bluetoothConnectionStatus.name)
}