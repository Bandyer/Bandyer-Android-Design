package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.new_smartglass.ParticipantItem
import com.bandyer.sdk_design.new_smartglass.SmartGlassParticipantData
import com.bandyer.sdk_design.new_smartglass.SmartGlassParticipantsFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import java.util.*

class ParticipantsFragment : SmartGlassParticipantsFragment() {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        itemAdapter!!.add(ParticipantItem(SmartGlassParticipantData("Mario Rossi", "Mario Rossi", SmartGlassParticipantData.UserState.ONLINE, R.drawable.sample_image, null, Date().time)))
        itemAdapter!!.add(ParticipantItem(SmartGlassParticipantData("Felice Trapasso", "Felice Trapasso", SmartGlassParticipantData.UserState.OFFLINE, null, "https://i.imgur.com/9I2qAlW.jpeg", Date().time)))
        itemAdapter!!.add(ParticipantItem(SmartGlassParticipantData("Francesco Sala", "Francesco Sala", SmartGlassParticipantData.UserState.INVITED, null, null, Date().time)))

        bottomActionBar!!.setTapOnClickListener {
            val itemData = itemAdapter!!.getAdapterItem(currentParticipantIndex).data
            val action = ParticipantsFragmentDirections.actionParticipantsFragmentToParticipantDetailsFragment(itemData)
            findNavController().navigate(action)
        }

        bottomActionBar!!.setSwipeDownOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        activity.showStatusBarCenteredTitle()
        activity.setStatusBarColor(ResourcesCompat.getColor(resources, R.color.bandyer_smartglass_background_color, null))
    }

    override fun onStop() {
        super.onStop()
        activity.hideStatusBarCenteredTitle()
        activity.setStatusBarColor(null)
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent): Boolean = when(event.type) {
        SmartGlassTouchEvent.Type.TAP -> {
            val itemData = itemAdapter!!.getAdapterItem(currentParticipantIndex).data
            val action = ParticipantsFragmentDirections.actionParticipantsFragmentToParticipantDetailsFragment(itemData)
            findNavController().navigate(action)
            true
        }
        SmartGlassTouchEvent.Type.SWIPE_DOWN -> {
            findNavController().popBackStack()
            true
        }
        else -> super.onSmartGlassTouchEvent(event)
    }
}