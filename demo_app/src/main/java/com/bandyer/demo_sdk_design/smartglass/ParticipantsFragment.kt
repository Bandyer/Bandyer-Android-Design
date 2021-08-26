package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.new_smartglass.ParticipantsItem
import com.bandyer.sdk_design.new_smartglass.SmartGlassParticipantData
import com.bandyer.sdk_design.new_smartglass.SmartGlassParticipantsFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import java.util.*

class ParticipantsFragment : SmartGlassParticipantsFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        itemAdapter!!.add(ParticipantsItem(SmartGlassParticipantData("Mario Rossi", "Mario Rossi", SmartGlassParticipantData.UserState.ONLINE, R.drawable.sample_image, null, Date().time)))
        itemAdapter!!.add(ParticipantsItem(SmartGlassParticipantData("Felice Trapasso", "Felice Trapasso", SmartGlassParticipantData.UserState.OFFLINE, null, "https://i.imgur.com/9I2qAlW.jpeg", Date().time)))
        itemAdapter!!.add(ParticipantsItem(SmartGlassParticipantData("Francesco Sala", "Francesco Sala", SmartGlassParticipantData.UserState.INVITED, null, null, Date().time)))

        return view
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = when(event) {
        SmartGlassTouchEvent.Event.TAP -> {
            val itemData = itemAdapter!!.getAdapterItem(currentParticipantIndex).data
            val action = ParticipantsFragmentDirections.actionParticipantsFragmentToParticipantDetailsFragment(itemData)
            findNavController().navigate(action)
            true
        }
        SmartGlassTouchEvent.Event.SWIPE_DOWN -> {
            findNavController().popBackStack()
            true
        }
        else -> false
    }
}