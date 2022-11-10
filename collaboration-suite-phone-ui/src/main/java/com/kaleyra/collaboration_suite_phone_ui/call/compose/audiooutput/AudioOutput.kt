package com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.model.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.SubMenuLayout
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

@Composable
internal fun AudioOutput(
    items: ImmutableList<AudioDevice>,
    onItemClick: (AudioDevice) -> Unit,
    onCloseClick: () -> Unit
) {
    SubMenuLayout(
        title = stringResource(id = R.string.kaleyra_audio_route_title),
        onCloseClick = onCloseClick
    ) {
        LazyColumn {
            items(items = items.value, key = { it.id }) {
                val title = titleFor(it)
                AudioItem(
                    title = title,
                    subtitle = subtitleFor(it),
                    icon = painterFor(it),
                    selected = it.isPlaying,
                    modifier = Modifier
                        .clickable(
                            onClickLabel = title,
                            role = Role.Button,
                            onClick = { onItemClick(it) }
                        )
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun titleFor(device: AudioDevice): String =
    when (device) {
        is AudioDevice.LoudSpeaker -> stringResource(R.string.kaleyra_call_action_audio_route_loudspeaker)
        is AudioDevice.EarPiece -> stringResource(R.string.kaleyra_call_action_audio_route_earpiece)
        is AudioDevice.WiredHeadset -> stringResource(R.string.kaleyra_call_action_audio_route_wired_headset)
        is AudioDevice.Muted -> stringResource(R.string.kaleyra_call_action_audio_route_muted)
        is AudioDevice.Bluetooth -> device.name
            ?: stringResource(R.string.kaleyra_call_action_audio_route_bluetooth)
    }

@Composable
private fun subtitleFor(device: AudioDevice): String? =
    when (device) {
        is AudioDevice.Bluetooth -> {
            val connectionState = device.connectionState
            val batteryLevel = device.batteryLevel

            val deviceState = when {
                connectionState == BluetoothDeviceState.Disconnected -> stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_disconnected)
                connectionState == BluetoothDeviceState.Failed -> stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_failed)
                connectionState == BluetoothDeviceState.Available -> stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_available)
                connectionState.isConnected() -> stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_connected)
                connectionState == BluetoothDeviceState.Deactivating -> stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_deactivating)
                else -> ""
            }

            val battery = if (batteryLevel != null) stringResource(
                R.string.kaleyra_bluetooth_battery_info,
                stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_battery_level),
                batteryLevel
            ) else ""

            val connectingState = when {
                connectionState.isConnecting() && deviceState.isNotBlank() -> stringResource(
                    R.string.kaleyra_bluetooth_connecting_status_info,
                    stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_activating)
                )
                connectionState.isConnecting() -> stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_activating)
                else -> ""
            }

            stringResource(R.string.kaleyra_bluetooth_info, deviceState, battery, connectingState)
        }
        else -> null
    }

@Composable
private fun painterFor(device: AudioDevice): Painter = painterResource(
    id = when (device) {
        is AudioDevice.LoudSpeaker -> R.drawable.ic_kaleyra_loud_speaker
        is AudioDevice.EarPiece -> R.drawable.ic_kaleyra_earpiece
        is AudioDevice.WiredHeadset -> R.drawable.ic_kaleyra_wired_headset
        is AudioDevice.Muted -> R.drawable.ic_kaleyra_muted
        is AudioDevice.Bluetooth -> R.drawable.ic_kaleyra_bluetooth_headset
    }
)

@Composable
internal fun AudioItem(
    title: String,
    subtitle: String?,
    icon: Painter,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colors.secondary else LocalContentColor.current
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = if (selected) MaterialTheme.colors.secondary else LocalContentColor.current,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = LocalContentColor.current.copy(alpha = .5f)
                )
            }
        }
    }
}

@Preview
@Composable
internal fun AudioRoutePreview() {
    KaleyraTheme {
        AudioOutput(
            items = mockAudioDevices,
            onItemClick = { },
            onCloseClick = { }
        )
    }
}