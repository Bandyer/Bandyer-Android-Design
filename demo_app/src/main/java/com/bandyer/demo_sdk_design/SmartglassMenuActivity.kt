package com.bandyer.demo_sdk_design

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.bottom_sheet.view.AudioRouteState
import com.bandyer.sdk_design.call.bottom_sheet.items.AudioRoute
import com.bandyer.sdk_design.call.bottom_sheet.items.SmartglassCallAction
import com.bandyer.sdk_design.call.smartglass.ui.menu.SmartglassActionItemMenu
import com.bandyer.sdk_design.call.smartglass.ui.menu.SmartglassesMenuLayout
import java.util.*

class SmartglassMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smartglasses_menu)
        showCallActions()
    }

    private fun showCallActions(): SmartglassActionItemMenu = SmartglassActionItemMenu.show(
            appCompatActivity = this,
            items = SmartglassCallAction.getSmartglassActions(
                    ctx = this,
                    micToggled = true,
                    cameraToggled = false,
                    withChat = true,
                    withWhiteboard = true,
                    withFileShare = true,
                    withScreenShare = true)).apply {
        selectionListener = object : SmartglassesMenuLayout.OnSmartglassMenuSelectionListener {
            override fun onSelected(item: ActionItem) {
                Toast.makeText(applicationContext, item::class.java.simpleName, Toast.LENGTH_SHORT).show()
                if (item !is SmartglassCallAction.SMARTGLASS_AUDIOROUTE) return
                selectionListener = null
                dismiss()
                showAudioRoutes()
            }

            override fun onDismiss() = finish()
        }
    }

    private fun showAudioRoutes(): SmartglassActionItemMenu = SmartglassActionItemMenu.show(
            appCompatActivity = this,
            items = listOf(
                    SmartglassCallAction.SMARTGLASS_AUDIOROUTE(this)
                            .apply { setCurrent(AudioRoute.LOUDSPEAKER(this@SmartglassMenuActivity, UUID.randomUUID().toString(), "Loudspeaker")) },
                    SmartglassCallAction.SMARTGLASS_AUDIOROUTE(this)
                            .apply { setCurrent(AudioRoute.BLUETOOTH(this@SmartglassMenuActivity, UUID.randomUUID().toString(), "Bluetooth", bluetoothConnectionStatus = AudioRouteState.BLUETOOTH.CONNECTED())) },
                    SmartglassCallAction.SMARTGLASS_AUDIOROUTE(this)
                            .apply { setCurrent(AudioRoute.WIRED_HEADSET(this@SmartglassMenuActivity, UUID.randomUUID().toString(), "Wired headset")) },
                    SmartglassCallAction.SMARTGLASS_AUDIOROUTE(this)
                            .apply { setCurrent(AudioRoute.MUTED(this@SmartglassMenuActivity, UUID.randomUUID().toString(), "Muted")) }

            )).apply {
        selectionListener = object : SmartglassesMenuLayout.OnSmartglassMenuSelectionListener {
            override fun onSelected(item: ActionItem) {
                Toast.makeText(applicationContext, item::class.java.simpleName, Toast.LENGTH_SHORT).show()
                dismiss()
                showAudioRoutes()
            }

            override fun onDismiss() = finish()
        }
    }
}