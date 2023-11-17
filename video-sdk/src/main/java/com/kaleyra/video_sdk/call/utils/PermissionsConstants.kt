/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.utils

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

internal const val RecordAudioPermission = Manifest.permission.RECORD_AUDIO
internal const val CameraPermission = Manifest.permission.CAMERA
@RequiresApi(Build.VERSION_CODES.S)
internal const val BluetoothScanPermission = Manifest.permission.BLUETOOTH_SCAN
@RequiresApi(Build.VERSION_CODES.S)
internal const val BluetoothConnectPermission = Manifest.permission.BLUETOOTH_CONNECT
