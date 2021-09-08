package com.bandyer.demo_sdk_design.smartglass

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.extensions.parseToColor
import com.bandyer.sdk_design.new_smartglass.*

class ParticipantDetailsFragment : SmartGlassParticipantDetailsFragment(), TiltController.TiltListener {

    private val activity by lazy { requireActivity() as SmartGlassActivity }
    private val args: ParticipantDetailsFragmentArgs by navArgs()

    private var tiltController: TiltController? = null
    private var actionIndex = 0

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

        val data = args.participantData!!

        avatar!!.setText(data.name.first().toString())
        avatar!!.setBackground(data.name.parseToColor())
        name!!.text = data.name
        with(contactStateText!!) {
            when (data.userState) {
                BandyerParticipantData.UserState.INVITED -> setContactState(
                    BandyerContactStateTextView.State.INVITED
                )
                BandyerParticipantData.UserState.OFFLINE -> setContactState(
                    BandyerContactStateTextView.State.LAST_SEEN,
                    data.lastSeenTime
                )
                else -> setContactState(
                    BandyerContactStateTextView.State.ONLINE
                )
            }
        }
        when {
            data.avatarImageId != null -> avatar!!.setImage(data.avatarImageId)
            data.avatarImageUrl != null -> avatar!!.setImage(data.avatarImageUrl!!)
            else -> avatar!!.setImage(null)
        }
        contactStateDot!!.isActivated = data.userState == BandyerParticipantData.UserState.ONLINE

        itemAdapter!!.add(BandyerParticipantDetailsItem(resources.getString(R.string.bandyer_smartglass_videocall)))
        itemAdapter!!.add(BandyerParticipantDetailsItem(resources.getString(R.string.bandyer_smartglass_call)))

        bottomActionBar!!.setSwipeOnClickListener {
            rvActions!!.smoothScrollToNext(actionIndex)
        }

        bottomActionBar!!.setSwipeDownOnClickListener {
            findNavController().popBackStack()
        }

        // add scroll listener
        rvActions!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager
                val foundView = snapHelper!!.findSnapView(layoutManager) ?: return
                actionIndex = layoutManager!!.getPosition(foundView)
            }
        })

        return view
    }

    override fun onTilt(x: Float, y: Float) = rvActions!!.scrollBy((x * 40).toInt(), 0)

    override fun onResume() {
        super.onResume()
        activity.setStatusBarColor(ResourcesCompat.getColor(resources, R.color.bandyer_smartglass_background_color, null))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.requestAllSensors()
    }

    override fun onStop() {
        super.onStop()
        activity.setStatusBarColor(null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.releaseAllSensors()
    }

    override fun onSmartGlassTouchEvent(event: BandyerSmartGlassTouchEvent): Boolean = when(event.type) {
        BandyerSmartGlassTouchEvent.Type.SWIPE_FORWARD -> {
            if(event.source == BandyerSmartGlassTouchEvent.Source.KEY) {
                rvActions!!.smoothScrollToNext(actionIndex)
                true
            } else false
        }
        BandyerSmartGlassTouchEvent.Type.SWIPE_BACKWARD -> {
            if(event.source == BandyerSmartGlassTouchEvent.Source.KEY) {
                rvActions!!.smoothScrollToPrevious(actionIndex)
                true
            } else false
        }
        BandyerSmartGlassTouchEvent.Type.SWIPE_DOWN -> {
            findNavController().popBackStack()
            true
        }
        else -> super.onSmartGlassTouchEvent(event)
    }
}