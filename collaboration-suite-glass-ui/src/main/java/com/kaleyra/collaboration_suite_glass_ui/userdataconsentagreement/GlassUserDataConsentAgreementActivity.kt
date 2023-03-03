package com.kaleyra.collaboration_suite_glass_ui.userdataconsentagreement

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.turnScreenOn
import com.kaleyra.collaboration_suite_glass_ui.GlassBaseActivity
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraActivityUserDataConsentAgreementGlassBinding
import com.kaleyra.collaboration_suite_glass_ui.status_bar_views.StatusBarView
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ActivityExtensions.enableImmersiveMode
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GlassUserDataConsentAgreementActivity : GlassBaseActivity() {

    private lateinit var binding: KaleyraActivityUserDataConsentAgreementGlassBinding

    private val viewModel: UserDataConsentAgreementViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = KaleyraActivityUserDataConsentAgreementGlassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (DeviceUtils.isSmartGlass) enableImmersiveMode()
        turnScreenOn()

        repeatOnStarted {
            viewModel
                .battery
                .onEach {
                    with(binding.kaleyraStatusBar) {
                        setBatteryChargingState(it.state == BatteryInfo.State.CHARGING)
                        setBatteryCharge(it.percentage)
                    }
                }
                .launchIn(this)

            viewModel
                .wifi
                .onEach {
                    binding.kaleyraStatusBar.setWiFiSignalState(
                        when {
                            it.state == WiFiInfo.State.DISABLED                                     -> StatusBarView.WiFiSignalState.DISABLED
                            it.level == WiFiInfo.Level.NO_SIGNAL || it.level == WiFiInfo.Level.POOR -> StatusBarView.WiFiSignalState.LOW
                            it.level == WiFiInfo.Level.FAIR || it.level == WiFiInfo.Level.GOOD      -> StatusBarView.WiFiSignalState.MODERATE
                            else                                                                    -> StatusBarView.WiFiSignalState.FULL
                        }
                    )
                }
                .launchIn(this)
        }
    }

    override fun onDestinationChanged(destinationId: Int) = Unit

}

//internal class VPPAActivity : AppCompatActivity() {
//
//    companion object {
//        const val EXTRA_TITLE = "com.kaleyra.collaboration_suite_core_ui.vppa.EXTRA_TITLE"
//        const val EXTRA_MESSAGE = "com.kaleyra.collaboration_suite_core_ui.vppa.EXTRA_MESSAGE"
//        const val EXTRA_ACCEPT_TEXT = "com.kaleyra.collaboration_suite_core_ui.vppa.EXTRA_ACCEPT_TEXT"
//        const val EXTRA_DECLINE_TEXT = "com.kaleyra.collaboration_suite_core_ui.vppa.EXTRA_DECLINE_TEXT"
//        const val EXTRA_ID = "com.kaleyra.collaboration_suite_core_ui.vppa.EXTRA_ID"
//
//        private var instance: VPPAActivity? = null
//
//        fun show(context: Context, intentWithExtras: Intent) {
//            val intent = instance?.intent ?: Intent(context, VPPAActivity::class.java).apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            }
//            intent.putExtras(intentWithExtras)
//            context.startActivity(intent)
//        }
//
//        fun close() {
//            instance?.finishAndRemoveTask()
//        }
//    }
//
//    private lateinit var binding: KaleyraActivityVppaBinding
//
//    private var termsAndConditionId: String = ""
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        instance = this
//        binding = KaleyraActivityVppaBinding.inflate(layoutInflater)
//        setUp(intent)
//        setContentView(binding.root)
//    }
//
//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//        setUp(intent)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        instance = null
//    }
//
//    override fun onBackPressed() {
//        super.onBackPressed()
//        sendBroadcastAndFinish(this, VPPABroadcastReceiver.ACTION_CANCEL, termsAndConditionId)
//    }
//
//    private fun setUp(intent: Intent) {
//        val extras = intent.extras ?: kotlin.run {
//            finishAndRemoveTask()
//            return
//        }
//        val title = extras.getString(EXTRA_TITLE, "")
//        val message = extras.getString(EXTRA_MESSAGE, "")
//        val acceptText = extras.getString(EXTRA_ACCEPT_TEXT, "")
//        val declineText = extras.getString(EXTRA_DECLINE_TEXT, "")
//        val id = extras.getString(EXTRA_ID, "").also {
//            termsAndConditionId = it
//        }
//
//        with(binding) {
//            kaleyraTitle.text = title
//            kaleyraMessage.text = message
//        }
//
//        with(binding.kaleyraAcceptButton) {
//            text = acceptText
//            setOnClickListener {
//                sendBroadcastAndFinish(context, VPPABroadcastReceiver.ACTION_ACCEPT_TERM, id)
//            }
//        }
//
//        with(binding.kaleyraDeclineButton) {
//            text = declineText
//            setOnClickListener {
//                sendBroadcastAndFinish(context, VPPABroadcastReceiver.ACTION_DECLINE_TERM, id)
//            }
//        }
//    }
//
//    private fun sendBroadcastAndFinish(context: Context, action: String, id: String) {
//        val packageName = context.applicationContext.packageName
//        context.sendBroadcast(Intent(context, VPPABroadcastReceiver::class.java).apply {
//            this.`package` = packageName
//            this.action = action
//            putExtra(VPPABroadcastReceiver.EXTRA_ID, id)
//        })
//        finishAndRemoveTask()
//    }
//}
