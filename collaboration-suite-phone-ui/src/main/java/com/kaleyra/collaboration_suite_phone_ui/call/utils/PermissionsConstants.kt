package com.kaleyra.collaboration_suite_phone_ui.call.utils

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

internal const val RecordAudioPermission = Manifest.permission.RECORD_AUDIO
internal const val CameraPermission = Manifest.permission.CAMERA
@RequiresApi(Build.VERSION_CODES.S)
internal const val BluetoothScanPermission = Manifest.permission.BLUETOOTH_SCAN
@RequiresApi(Build.VERSION_CODES.S)
internal const val BluetoothConnectPermission = Manifest.permission.BLUETOOTH_CONNECT
