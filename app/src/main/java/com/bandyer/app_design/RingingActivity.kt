package com.bandyer.app_design

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bandyer.video_android_phone_ui.bottom_sheet.BandyerBottomSheet
import com.bandyer.video_android_phone_ui.bottom_sheet.items.ActionItem
import com.bandyer.video_android_phone_ui.call.bottom_sheet.items.CallAction
import com.bandyer.video_android_phone_ui.call.widgets.BandyerCallActionWidget

class RingingActivity : AppCompatActivity() {

    private var callActionWidget: BandyerCallActionWidget<ActionItem, BandyerBottomSheet>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ringing)
        initializeBottomSheetLayout()
    }

    private fun initializeBottomSheetLayout() {
        callActionWidget = BandyerCallActionWidget(this, findViewById(R.id.coordinator_layout), CallAction.getActions(this, cameraToggled = false, micToggled = true, withChat = true, withWhiteboard = true, withFileShare = true, withScreenShare = true))
        callActionWidget!!.showRingingControls()
    }
}