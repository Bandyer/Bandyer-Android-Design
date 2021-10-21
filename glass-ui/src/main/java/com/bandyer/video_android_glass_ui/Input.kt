package com.bandyer.video_android_glass_ui

import android.view.View
import kotlinx.coroutines.flow.Flow

interface Input {

    val id: String

    val enabled: Flow<Boolean>

    val state: Flow<State>

    sealed class State {

        object Idle : State()

        object Active : State()

        sealed class Closed(open val reason: String) : State() {

            companion object : Closed("The input has been closed") {
                override fun hashCode(): Int = "Closed".hashCode()
                override fun equals(other: Any?) = other !is Error && other is Closed
                override fun toString() = "Closed: $reason"
            }

            sealed class Error(reason: String) : Closed(reason)
        }
    }

    interface Audio : Input

    interface Video : Input {

        override val id: String

        override val enabled: Flow<Boolean>

        override val state: Flow<State>

        var view: Flow<View?>

        val currentQuality: Flow<Quality>

        val source: Source

        data class Quality(val resolution: Resolution, val fps: Int) {
            data class Resolution(val width: Int, val height: Int)
        }

        sealed class Source {

            object Application : Source()

            object Screen : Source()

            object Custom : Source()

            sealed class Camera : Source() {
                data class Internal(val lenses: List<Lens>, val currentLens: Flow<Lens>) : Camera() {
                    data class Lens(val name: String, val availableQualities: List<Quality>, val isRear: Boolean)
                }

                object Usb : Camera()
            }
        }
    }
}

data class Video(
    override val id: String,
    override val enabled: Flow<Boolean>,
    override val state: Flow<Input.State>,
    override var view: Flow<View?>,
    override val currentQuality: Flow<Input.Video.Quality>,
    override val source: Input.Video.Source
) : Input.Video

data class Audio(
    override val id: String,
    override val enabled: Flow<Boolean>,
    override val state: Flow<Input.State>
) : Input.Audio
