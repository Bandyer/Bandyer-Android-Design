package com.bandyer.demo_sdk_design

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.call.bottom_sheet.items.CallAction
import com.bandyer.sdk_design.smartglass.call.menu.items.getSmartglassActions
import com.bandyer.sdk_design.smartglass.call.menu.SmartGlassActionItemMenu
import com.bandyer.sdk_design.smartglass.call.menu.SmartGlassMenuLayout

class SmartglassMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smartglasses_menu)
        showCallActions()
    }

    private fun showCallActions(): SmartGlassActionItemMenu = SmartGlassActionItemMenu.show(
            appCompatActivity = this,
            items = CallAction.getSmartglassActions(
                    ctx = this,
                    micToggled = true,
                    cameraToggled = true))
            .apply {
                selectionListener = object : SmartGlassMenuLayout.OnSmartglassMenuSelectionListener {
                    override fun onSelected(item: ActionItem) {
                        Toast.makeText(applicationContext, item::class.java.simpleName, Toast.LENGTH_SHORT).show()
                        dismiss()
                        selectionListener = null
                    }

                    override fun onDismiss() = finish()
                }
            }
}