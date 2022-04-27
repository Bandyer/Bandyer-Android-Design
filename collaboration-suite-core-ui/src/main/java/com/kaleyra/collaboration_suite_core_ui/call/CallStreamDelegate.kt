package com.kaleyra.collaboration_suite_core_ui.call

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.VideoStreamView
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.plus

interface CallStreamDelegate: LifecycleOwner {

    private companion object {
        const val MY_STREAM_ID = "main"
    }

    fun setUpCallStreams(context: Context, call: Call) {
        val mainScope = MainScope() + CoroutineName("Call Scope: ${call.id}")
        setUpStreamsAndVideos(call, context, mainScope)
        updateStreamInputsOnPermissions(call, mainScope)

        call.state
            .takeWhile { it !is Call.State.Disconnected.Ended }
            .onCompletion { mainScope.cancel() }
            .launchIn(lifecycleScope)
    }

    fun publishMyStream(fragmentActivity: FragmentActivity, call: Call) {
        val me = call.participants.value.me
        if (me.streams.value.firstOrNull { it.id == MY_STREAM_ID } != null) return
        me.addStream(fragmentActivity, MY_STREAM_ID).let {
            it.audio.value = null
            it.video.value = null
        }
    }

    private fun updateStreamInputsOnPermissions(call: Call, coroutineScope: CoroutineScope) {
        val hasVideo = call.extras.preferredType.hasVideo()

        call.inputs.allowList.onEach { inputs ->
            if (inputs.isEmpty()) return@onEach

            val videoInput = inputs.lastOrNull { it is Input.Video.My } as? Input.Video.My
            val audioInput = inputs.firstOrNull { it is Input.Audio } as? Input.Audio

            videoInput?.setQuality(Input.Video.Quality.Definition.HD)

            val me = call.participants.value.me
            me.streams.value.firstOrNull { it.id == MY_STREAM_ID }?.let {
                it.audio.value = audioInput
                if (hasVideo) it.video.value = videoInput
            }

        }.launchIn(coroutineScope)
    }

    private fun setUpStreamsAndVideos(
        call: Call,
        context: Context,
        coroutineScope: CoroutineScope
    ) {
        val pJobs = mutableListOf<Job>()
        val sJobs = hashMapOf<String, List<Job>>()
        call.participants
            .map { it.others + it.me }
            .onEach onEachParticipants@{ participants ->
                pJobs.forEach {
                    it.cancel()
                    it.join()
                }
                pJobs.clear()

                sJobs.values.forEach { jobs ->
                    jobs.forEach {
                        it.cancel()
                        it.join()
                    }
                }
                sJobs.clear()

                participants.forEach { participant ->
                    pJobs += participant.streams
                        .onEach onEachStreams@{ streams ->
                            sJobs[participant.userId]?.forEach {
                                it.cancel()
                                it.join()
                            }
                            val streamsJobs = mutableListOf<Job>()
                            streams.forEach { stream ->
                                stream.open()
                                streamsJobs += stream.video.onEach { video ->
                                    if (video?.view?.value != null) return@onEach
                                    video?.view?.value = VideoStreamView(context.applicationContext)
                                }.launchIn(coroutineScope)
                            }
                            sJobs[participant.userId] = streamsJobs
                        }.launchIn(coroutineScope)
                }
            }.launchIn(coroutineScope)
    }
}