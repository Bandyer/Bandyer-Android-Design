package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.extensions.parseToColor
import com.bandyer.sdk_design.new_smartglass.ContactStateTextView
import com.bandyer.sdk_design.new_smartglass.ParticipantDetailsItem
import com.bandyer.sdk_design.new_smartglass.SmartGlassParticipantDetailsFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent

class ParticipantDetailsFragment : SmartGlassParticipantDetailsFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        avatar!!.setText("S")
        avatar!!.setBackground("Sara Bernini".parseToColor())
        name!!.text = "Sara Bernini"
        contactStateText!!.setContactState(ContactStateTextView.State.INVITED)

        itemAdapter!!.add(ParticipantDetailsItem(resources.getString(R.string.bandyer_smartglass_videocall)))
        itemAdapter!!.add(ParticipantDetailsItem(resources.getString(R.string.bandyer_smartglass_call)))

        return view
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = false
}