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
import com.bandyer.video_android_core_ui.extensions.StringExtensions.parseToColor
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.bandyer.video_android_glass_ui.BandyerGlassTouchEvent
import com.bandyer.video_android_glass_ui.chat.menu.BandyerChatMenuItem
import com.bandyer.video_android_glass_ui.chat.menu.BandyerGlassChatMenuFragment
import com.bandyer.video_android_glass_ui.contact.BandyerContactData
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToNext

class ChatMenuFragment : BandyerGlassChatMenuFragment(), TiltController.TiltListener {

    private val activity by lazy { requireActivity() as SmartGlassActivity }
    private val args: ChatMenuFragmentArgs by navArgs()

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

        val data = args.contactData!!

        avatar!!.setText(data.name.first().toString())
        avatar!!.setBackground(data.name.parseToColor())
        name!!.text = data.name
        with(contactStateText!!) {
            when (data.userState) {
                BandyerContactData.UserState.INVITED -> setContactState(
                    com.bandyer.video_android_glass_ui.contact.BandyerContactStateTextView.State.INVITED
                )
                BandyerContactData.UserState.OFFLINE -> setContactState(
                    com.bandyer.video_android_glass_ui.contact.BandyerContactStateTextView.State.LAST_SEEN,
                    data.lastSeenTime
                )
                else                                 -> setContactState(
                    com.bandyer.video_android_glass_ui.contact.BandyerContactStateTextView.State.ONLINE
                )
            }
        }
        when {
            data.avatarImageId != null  -> avatar!!.setImage(data.avatarImageId)
            data.avatarImageUrl != null -> avatar!!.setImage(data.avatarImageUrl!!)
            else                        -> avatar!!.setImage(null)
        }
        contactStateDot!!.isActivated = data.userState == BandyerContactData.UserState.ONLINE

        itemAdapter!!.add(BandyerChatMenuItem(resources.getString(R.string.bandyer_glass_videocall)))
        itemAdapter!!.add(BandyerChatMenuItem(resources.getString(R.string.bandyer_glass_call)))

        bottomActionBar!!.setSwipeOnClickListener {
            rvActions!!.horizontalSmoothScrollToNext(actionIndex)
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

    override fun onTilt(x: Float, y: Float, z: Float) = rvActions!!.scrollBy((x * resources.displayMetrics.densityDpi / 5).toInt(), 0)

    override fun onResume() {
        super.onResume()
        activity.setStatusBarColor(ResourcesCompat.getColor(resources, R.color.bandyer_glass_background_color, null))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.requestAllSensors()
    }

    override fun onStop() {
        super.onStop()
        activity.setStatusBarColor(null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.releaseAllSensors()
    }

    override fun onSmartGlassTouchEvent(event: BandyerGlassTouchEvent): Boolean = when (event.type) {
        BandyerGlassTouchEvent.Type.SWIPE_FORWARD  -> {
            if (event.source == BandyerGlassTouchEvent.Source.KEY) {
                rvActions!!.horizontalSmoothScrollToNext(actionIndex)
                true
            } else false
        }
        BandyerGlassTouchEvent.Type.SWIPE_BACKWARD -> {
            if (event.source == BandyerGlassTouchEvent.Source.KEY) {
                rvActions!!.horizontalSmoothScrollToPrevious(actionIndex)
                true
            } else false
        }
        BandyerGlassTouchEvent.Type.SWIPE_DOWN     -> {
            findNavController().popBackStack()
            true
        }
        else                                                                               -> super.onSmartGlassTouchEvent(event)
    }
}