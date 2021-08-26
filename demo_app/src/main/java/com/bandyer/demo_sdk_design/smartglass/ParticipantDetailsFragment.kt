package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.extensions.parseToColor
import com.bandyer.sdk_design.new_smartglass.*

class ParticipantDetailsFragment : SmartGlassParticipantDetailsFragment() {

    private val args: ParticipantDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val data = args.participantData!!

        avatar!!.setText(data.name.first().toString())
        avatar!!.setBackground(data.name.parseToColor())
        name!!.text = data.name
        with(contactStateText!!) {
            when (data.userState) {
                SmartGlassParticipantData.UserState.INVITED -> setContactState(
                    ContactStateTextView.State.INVITED
                )
                SmartGlassParticipantData.UserState.OFFLINE -> setContactState(
                    ContactStateTextView.State.LAST_SEEN,
                    data.lastSeenTime
                )
                else -> setContactState(
                    ContactStateTextView.State.ONLINE
                )
            }
        }
        when {
            data.avatarImageId != null -> avatar!!.setImage(data.avatarImageId)
            data.avatarImageUrl != null -> avatar!!.setImage(data.avatarImageUrl!!)
            else -> avatar!!.setImage(null)
        }
        contactStateDot!!.isActivated = data.userState == SmartGlassParticipantData.UserState.ONLINE

        itemAdapter!!.add(ParticipantDetailsItem(resources.getString(R.string.bandyer_smartglass_videocall)))
        itemAdapter!!.add(ParticipantDetailsItem(resources.getString(R.string.bandyer_smartglass_call)))

        return view
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = when(event) {
        SmartGlassTouchEvent.Event.SWIPE_DOWN -> {
            findNavController().popBackStack()
            true
        }
        else -> false
    }
}