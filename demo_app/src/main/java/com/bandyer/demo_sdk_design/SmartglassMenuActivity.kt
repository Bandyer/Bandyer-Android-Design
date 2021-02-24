package com.bandyer.demo_sdk_design

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.bottom_sheet.view.AudioRouteState
import com.bandyer.sdk_design.call.bottom_sheet.items.AudioRoute
import com.bandyer.sdk_design.call.bottom_sheet.items.SmartglassCallAction
import com.bandyer.sdk_design.call.smartglass.SmartglassActionItemMenu
import com.bandyer.sdk_design.call.smartglass.SmartglassesMenuLayout
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
                    micToggled = false,
                    cameraToggled = false))
            .apply {
                selectionListener = object : SmartglassesMenuLayout.OnSmartglassMenuSelectionListener {
                    override fun onSelected(item: ActionItem) {
                        Toast.makeText(applicationContext, item::class.java.simpleName, Toast.LENGTH_SHORT).show()
                        dismiss()
                        selectionListener = null
                    }

                    override fun onDismiss() = finish()
                }
            }
}