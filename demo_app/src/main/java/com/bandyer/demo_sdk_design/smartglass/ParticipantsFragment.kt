package com.bandyer.demo_sdk_design.smartglass

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.new_smartglass.*
import java.util.*

class ParticipantsFragment : SmartGlassParticipantsFragment(), TiltController.TiltListener {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    private var tiltController: TiltController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tiltController =
                TiltController(
                    requireContext(),
                    this
                )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        if(Build.MODEL == resources.getString(R.string.bandyer_smartglass_realwear_model_name))
            bottomActionBar!!.setSwipeText(resources.getString(R.string.bandyer_smartglass_right_left))

        itemAdapter!!.add(ParticipantItem(SmartGlassParticipantData("Mario Rossi", "Mario Rossi", SmartGlassParticipantData.UserState.ONLINE, R.drawable.sample_image, null, Date().time)))
        itemAdapter!!.add(ParticipantItem(SmartGlassParticipantData("Felice Trapasso", "Felice Trapasso", SmartGlassParticipantData.UserState.OFFLINE, null, "https://i.imgur.com/9I2qAlW.jpeg", Date().time)))
        itemAdapter!!.add(ParticipantItem(SmartGlassParticipantData("Francesco Sala", "Francesco Sala", SmartGlassParticipantData.UserState.INVITED, null, null, Date().time)))

        bottomActionBar!!.setTapOnClickListener {
            val itemData = itemAdapter!!.getAdapterItem(currentParticipantIndex).data
            val action = ParticipantsFragmentDirections.actionParticipantsFragmentToParticipantDetailsFragment(itemData)
            findNavController().navigate(action)
        }

        bottomActionBar!!.setSwipeOnClickListener {
            rvParticipants!!.smoothScrollToNext(currentParticipantIndex)
        }

        bottomActionBar!!.setSwipeDownOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onTilt(x: Float, y: Float) = rvParticipants!!.scrollBy((x * 40).toInt(), 0)

    override fun onResume() {
        super.onResume()
        activity.showStatusBarCenteredTitle()
        activity.setStatusBarColor(ResourcesCompat.getColor(resources, R.color.bandyer_smartglass_background_color, null))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.requestAllSensors()
    }

    override fun onStop() {
        super.onStop()
        activity.hideStatusBarCenteredTitle()
        activity.setStatusBarColor(null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.releaseAllSensors()
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