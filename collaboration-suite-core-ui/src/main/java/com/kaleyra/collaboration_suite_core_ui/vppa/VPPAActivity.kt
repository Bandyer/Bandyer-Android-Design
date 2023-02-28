package com.kaleyra.collaboration_suite_core_ui.vppa

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.kaleyra.collaboration_suite_core_ui.databinding.KaleyraActivityVppaBinding

internal class VPPAActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "com.kaleyra.collaboration_suite_core_ui.vppa.EXTRA_TITLE"
        const val EXTRA_MESSAGE = "com.kaleyra.collaboration_suite_core_ui.vppa.EXTRA_MESSAGE"
        const val EXTRA_ACCEPT_TEXT = "com.kaleyra.collaboration_suite_core_ui.vppa.EXTRA_ACCEPT_TEXT"
        const val EXTRA_DECLINE_TEXT = "com.kaleyra.collaboration_suite_core_ui.vppa.EXTRA_DECLINE_TEXT"
        const val EXTRA_ID = "com.kaleyra.collaboration_suite_core_ui.vppa.EXTRA_ID"

        private var instance: VPPAActivity? = null

        fun show(context: Context, intentWithExtras: Intent) {
            val intent = instance?.intent ?: Intent(context, VPPAActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            intent.putExtras(intentWithExtras)
            context.startActivity(intent)
        }

        fun close() {
            instance?.finishAndRemoveTask()
        }
    }

    private lateinit var binding: KaleyraActivityVppaBinding

    private var termsAndConditionId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        binding = KaleyraActivityVppaBinding.inflate(layoutInflater)
        setUp(intent)
        setContentView(binding.root)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setUp(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        sendBroadcastAndFinish(this, VPPABroadcastReceiver.ACTION_CANCEL, termsAndConditionId)
    }

    private fun setUp(intent: Intent) {
        val extras = intent.extras ?: kotlin.run {
            finishAndRemoveTask()
            return
        }
        val title = extras.getString(EXTRA_TITLE, "")
        val message = extras.getString(EXTRA_MESSAGE, "")
        val acceptText = extras.getString(EXTRA_ACCEPT_TEXT, "")
        val declineText = extras.getString(EXTRA_DECLINE_TEXT, "")
        val id = extras.getString(EXTRA_ID, "").also {
            termsAndConditionId = it
        }

        with(binding) {
            kaleyraTitle.text = title
            kaleyraMessage.text = message
        }

        with(binding.kaleyraAcceptButton) {
            text = acceptText
            setOnClickListener {
                sendBroadcastAndFinish(context, VPPABroadcastReceiver.ACTION_ACCEPT_TERM, id)
            }
        }

        with(binding.kaleyraDeclineButton) {
            text = declineText
            setOnClickListener {
                sendBroadcastAndFinish(context, VPPABroadcastReceiver.ACTION_DECLINE_TERM, id)
            }
        }
    }

    private fun sendBroadcastAndFinish(context: Context, action: String, id: String) {
        val packageName = context.applicationContext.packageName
        context.sendBroadcast(Intent(context, VPPABroadcastReceiver::class.java).apply {
            this.`package` = packageName
            this.action = action
            putExtra(VPPABroadcastReceiver.EXTRA_ID, id)
        })
        finishAndRemoveTask()
    }
}