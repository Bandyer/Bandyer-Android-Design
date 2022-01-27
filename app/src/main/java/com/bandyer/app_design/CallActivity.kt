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

package com.bandyer.app_design

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.util.Rational
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.bandyer.android_audiosession.model.AudioOutputDevice.EARPIECE
import com.bandyer.android_audiosession.router.LastConnectedDeviceRoutingStrategy
import com.bandyer.android_audiosession.session.AudioCallSession
import com.bandyer.android_audiosession.session.AudioCallSessionListener
import com.bandyer.android_audiosession.session.AudioCallSessionState
import com.bandyer.android_audiosession.session.audioCallSessionOptions
import com.bandyer.video_android_core_ui.extensions.ContextExtensions.getScreenSize
import com.bandyer.video_android_phone_ui.bottom_sheet.BandyerBottomSheet
import com.bandyer.video_android_phone_ui.bottom_sheet.items.ActionItem
import com.bandyer.video_android_phone_ui.bottom_sheet.view.AudioRouteState
import com.bandyer.video_android_phone_ui.bottom_sheet.view.BottomSheetLayoutType
import com.bandyer.video_android_phone_ui.buttons.BandyerActionButton
import com.bandyer.video_android_phone_ui.buttons.BandyerHideableButton
import com.bandyer.video_android_phone_ui.call.bottom_sheet.OnAudioRouteBottomSheetListener
import com.bandyer.video_android_phone_ui.call.bottom_sheet.items.AudioRoute
import com.bandyer.video_android_phone_ui.call.bottom_sheet.items.CallAction
import com.bandyer.video_android_phone_ui.call.bottom_sheet.items.CallAction.Items.getActions
import com.bandyer.video_android_phone_ui.call.widgets.BandyerCallActionWidget
import com.bandyer.video_android_phone_ui.call.widgets.BandyerCallInfoWidget
import com.bandyer.video_android_phone_ui.call.widgets.BandyerCallUserInfoWidget
import com.bandyer.video_android_phone_ui.screensharing.AppViewOverlay
import com.bandyer.video_android_phone_ui.screensharing.StatusBarOverlayView
import com.bandyer.video_android_phone_ui.screensharing.dialog.BandyerScreenSharePickerDialog
import com.bandyer.video_android_phone_ui.views.ViewOverlayAttacher
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import java.util.*

class CallActivity : AppCompatActivity(), OnAudioRouteBottomSheetListener, BandyerCallActionWidget.OnClickListener {

    private val TAG = "CallActivity"

    private var callActionWidget: BandyerCallActionWidget<ActionItem, BandyerBottomSheet>? = null

    private var appViewOverlay: AppViewOverlay? = null
    private val genericActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

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
                window.decorView.postDelayed({
                    callActionWidget!!.collapse()
                }, resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
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

    override fun onResume() {
        super.onResume()
        initializeBottomSheetLayout(null)
    }

    private fun onAudioOutputsChanged(availableOutputs: List<AudioOutputDevice>) {
        callActionWidget!!.setAudioRouteItems(availableOutputs.map { getAudioRoute(it) }.plus(getMutedAudioRoute()))
    }

    private fun initializeUI(savedInstanceState: Bundle?) {
        initializeBottomSheetLayout(savedInstanceState)
        initializeCallInfoWidget()
        initializeUserInfoWidget()
//        Picasso.get().load(R.drawable.sample_image).into(findViewById<ImageView>(R.id.image))
    }

    private fun initializeBottomSheetLayout(savedInstanceState: Bundle?) {
        callActionWidget = callActionWidget ?: BandyerCallActionWidget<ActionItem, BandyerBottomSheet>(this, findViewById(
            R.id.coordinator_layout
        ), getActions(this, true, false, true, true, true, true)).apply {
            onAudioRoutesRequest = this@CallActivity
            onClickListener = this@CallActivity
            savedInstanceState?.let { restoreInstanceState(it) }
        }
        callActionWidget!!.setAnchoredView(findViewById(R.id.fullscreen_info), Gravity.TOP or Gravity.CENTER_HORIZONTAL)
        callActionWidget!!.showCallControls(true, false, false, bottomSheetLayoutType = BottomSheetLayoutType.GRID(4, BottomSheetLayoutType.Orientation.VERTICAL))
        AudioCallSession.getInstance().currentAudioOutputDevice?.let {
            callActionWidget?.selectAudioRoute(getAudioRoute(it))
        }
    }

    private fun initializeCallInfoWidget() = with(findViewById<BandyerCallInfoWidget>(R.id.call_info)) {
        setTitle("Bob Martin, John Doe, Mark Smith, Julie Randall")
        setSubtitle("Dialing...")
        setRecordingText("Recording in progress...")
        setRecording(true)

        setOnClickListener {
            when {
                callActionWidget!!.isCollapsed() -> callActionWidget!!.anchor()
                callActionWidget!!.isAnchored() -> callActionWidget!!.expand()
                callActionWidget!!.isExpanded() -> callActionWidget!!.collapse()
            }
        }
    }

    private fun initializeUserInfoWidget() = with(findViewById<BandyerCallUserInfoWidget>(R.id.call_user_info)) {
        this.text = "Juan Carlos"
        val fullScreenInfo = this@CallActivity.findViewById<BandyerHideableButton>(R.id.fullscreen_info)
        fullscreenActionButton!!.setOnClickListener {
            setFullscreenStyle(!fullscreenActionButton!!.isActivated)
            fullScreenInfo.visibility = if (fullscreenActionButton!!.isActivated) View.VISIBLE else View.GONE
        }
    }

    private fun getAudioRoute(audioOutputDevice: AudioOutputDevice): AudioRoute {
        val isActive = AudioCallSession.getInstance().currentAudioOutputDevice == audioOutputDevice
        return when (audioOutputDevice) {
            is AudioOutputDevice.BLUETOOTH -> AudioRoute.BLUETOOTH(
                this,
                identifier = audioOutputDevice.identifier,
                name = audioOutputDevice.name ?: "",
                batteryLevel = audioOutputDevice.batteryLevel,
                bluetoothConnectionStatus = AudioRouteState.BLUETOOTH.valueOf(audioOutputDevice.bluetoothConnectionStatus.name)
            )
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
                is AudioOutputDevice.BLUETOOTH -> routes.add(
                    AudioRoute.BLUETOOTH(
                        this,
                        device.identifier,
                        device.name,
                        device.batteryLevel,
                        AudioRouteState.BLUETOOTH.valueOf(device.bluetoothConnectionStatus.name)
                    )
                )
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

    private fun getMutedAudioRoute() = AudioRoute.MUTED(this, UUID.randomUUID().toString(), resources.getString(
        R.string.bandyer_call_action_audio_route_muted
    ), AudioCallSession.getInstance().currentAudioOutputDevice is AudioOutputDevice.NONE)

    override fun onCallActionClicked(item: CallAction, position: Int): Boolean {
        return when (item) {
            is CallAction.SCREEN_SHARE -> {
                if (item.toggled) {
                    appViewOverlay?.hide()
                } else {
                    BandyerScreenSharePickerDialog().show(this@CallActivity) {
                        Snackbar.make(callActionWidget!!.coordinatorLayout, it.name, Snackbar.LENGTH_SHORT).show()

                        val desiredType: ViewOverlayAttacher.OverlayType = when (it) {
                            BandyerScreenSharePickerDialog.SharingOption.WHOLE_DEVICE -> ViewOverlayAttacher.OverlayType.GLOBAL.also { getOverlayPermission() }
                            else -> ViewOverlayAttacher.OverlayType.CURRENT_APPLICATION
                        }
                        appViewOverlay = AppViewOverlay(StatusBarOverlayView(this@CallActivity), desiredType)
                        appViewOverlay!!.show(this@CallActivity)
                    }
                }
                item.toggle()
                true
            }
            is CallAction.CAMERA -> {
                // if permissions toggle
                item.toggle()
                true
            }
            is CallAction.SWITCH_CAMERA -> {
                (item.itemView as? BandyerActionButton)?.isEnabled = false
                false
            }
            else -> false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioCallSession.getInstance().dispose()
    }

    override fun onBackPressed() {
        if (!enterPip()) moveTaskToBack(true)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPip()
    }

    @SuppressLint("NewApi")
    private fun enterPip(): Boolean {
        val canGoInPiP: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                && hasPermission(AppOpsManager.OPSTR_PICTURE_IN_PICTURE)
        if (!canGoInPiP) return false
        return enterPictureInPictureMode(PictureInPictureParams.Builder().setAspectRatio(Rational(getScreenSize().x, getScreenSize().y)).build())
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) callActionWidget!!.hide()
        else callActionWidget!!.show()
    }

    private fun getOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) return
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        genericActivityResultLauncher.launch(intent)
    }

    @Suppress("DEPRECATION")
    private fun hasPermission(permission: String) = with(getSystemService(APP_OPS_SERVICE) as AppOpsManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            unsafeCheckOpNoThrow(permission, Process.myUid(), packageName) == AppOpsManager.MODE_ALLOWED
        else
            checkOpNoThrow(permission, Process.myUid(), packageName) == AppOpsManager.MODE_ALLOWED
    }
}

private fun AudioRoute.BLUETOOTH.toAudioOutputDevice(): AudioOutputDevice.BLUETOOTH = AudioOutputDevice.BLUETOOTH(this.identifier).apply {
    this.address = this.identifier
    this.batteryLevel = this@toAudioOutputDevice.batteryLevel
    this.bluetoothConnectionStatus = AudioOutputDevice.BLUETOOTH.BluetoothConnectionStatus.valueOf(this.bluetoothConnectionStatus.name)
}