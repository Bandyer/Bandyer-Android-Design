package com.kaleyra.collaboration_suite_core_ui

import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isDND
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isSilent
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.disableAudioRouting
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.enableAudioRouting
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import com.kaleyra.collaboration_suite_utils.logging.PriorityLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.take

/**
 * Phone box UI
 *
 * @property phoneBox The PhoneBox delegate
 * @property callActivityClazz The call activity Class<*>
 * @property logger The PriorityLogger
 * @constructor
 */
class PhoneBoxUI(
    private val phoneBox: PhoneBox,
    private val callActivityClazz: Class<*>,
    private val logger: PriorityLogger? = null,
) : PhoneBox by phoneBox {

    private val callScope = CoroutineScope(Dispatchers.IO)

    private var mappedCalls: List<CallUI> = listOf()

    /**
     * @suppress
     */
     
    override val call: SharedFlow<CallUI> = phoneBox.call.map { getOrCreateCallUI(it) }.shareIn(callScope, SharingStarted.Eagerly, replay = 1)

    /**
     * @suppress
     */
    override val callHistory: SharedFlow<List<CallUI>> = phoneBox.callHistory.map { it.map { getOrCreateCallUI(it) } }.shareIn(callScope, SharingStarted.Eagerly, replay = 1)


    /**
     * WithUI flag, set to true to automatically show the call ui on a new call, false otherwise
     */
    var withUI = true

    init {
        listenToCalls()
    }

    /**
     * @suppress
     */
    override fun connect() = phoneBox.connect()

    /**
     * @suppress
     */
    override fun disconnect() = phoneBox.disconnect()

    internal fun dispose() {
        disconnect()
        disableAudioRouting(logger)
        callScope.cancel()
    }

    /**
     * Call
     *
     * @param users to be called
     * @param options creation options
     */
    fun call(users: List<User>, options: (PhoneBox.CreationOptions.() -> Unit)? = null): CallUI = create(users, options).apply { connect() }

    /**
     * Join an url
     *
     * @param url to join
     */
    fun join(url: String): CallUI = create(url).apply { connect() }

    /**
     * @suppress
     */
    override fun create(url: String) = synchronized(this) { createCallUI(phoneBox.create(url)) }

    /**
     * @suppress
     */
    override fun create(users: List<User>, conf: (PhoneBox.CreationOptions.() -> Unit)?) = synchronized(this) { createCallUI(phoneBox.create(users, conf)) }

    /**
     * Show the call ui
     * @param call The call object that should be shown.
     */
    fun show(call: CallUI) {
        if (!canShowCallActivity(ContextRetainer.context, call)) return
        UIProvider.showCall(callActivityClazz)
    }

    private fun listenToCalls() {
        var serviceJob: Job? = null
        call.onEach {
            if (it.state.value is Call.State.Disconnected.Ended || !withUI) return@onEach
            serviceJob?.cancel()
            serviceJob = callService(it, callScope)
            it.enableAudioRouting(withCallSounds = true, logger = logger, coroutineScope = callScope)
            if (it.isLink) showOnAppResumed(it) else show(it)
        }.launchIn(callScope)
    }

    private fun callService(call: CallUI, scope: CoroutineScope): Job = with(ContextRetainer.context) {
        call.state
            .onEach { state ->
                when {
                    state is Call.State.Disconnected.Ended -> stopService(Intent(this, CallService::class.java))
                    state is Call.State.Disconnected
                            || (state is Call.State.Connecting && call.participants.value.let { it.creator() == it.me })
                            || state is Call.State.Connected && call.isLink
                    -> {
                        val intent = Intent(this, CallService::class.java)
                        intent.putExtra(CallService.CALL_ACTIVITY_CLASS, callActivityClazz)
                        startService(intent)
                    }
                    else -> Unit
                }
            }
            .onCompletion { stopService(Intent(this@with, CallService::class.java)) }
            .launchIn(scope)
    }

    private fun canShowCallActivity(context: Context, call: CallUI): Boolean {
        val participants = call.participants.value
        val creator = participants.creator()
        val isOutgoing = creator == participants.me
        return AppLifecycle.isInForeground.value &&
                (!context.isDND() || (context.isDND() && isOutgoing)) &&
                (!context.isSilent() || (context.isSilent() && (isOutgoing || call.isLink)))
    }

    private fun showOnAppResumed(call: CallUI): Unit = let { AppLifecycle.isInForeground.dropWhile { !it }.take(1).onEach { show(call) }.launchIn(callScope) }

    private fun getOrCreateCallUI(call: Call): CallUI = synchronized(this) { mappedCalls.firstOrNull { it.id == call.id } ?: createCallUI(call) }

    private fun createCallUI(call: Call): CallUI = CallUI(call).apply { mappedCalls = mappedCalls + this }
}