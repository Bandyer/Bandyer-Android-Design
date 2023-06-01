package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import android.util.Rational
import com.kaleyra.collaboration_suite.phonebox.StreamView
import com.kaleyra.collaboration_suite.phonebox.VideoStreamView
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

object CallUiStateMapper {

    fun Flow<CallUiState>.toPipAspectRatio(): Flow<Rational> =
        this.map { it.featuredStreams }
            .map { it.value.firstOrNull() }
            .filterNotNull()
            .distinctUntilChanged()
            .map { it.video?.view?.value as? VideoStreamView }
            .filterNotNull()
            .flatMapLatest { it.state }
            .filterIsInstance<StreamView.State.Rendering>()
            .flatMapLatest { it.definition }
            .map {
                if (it.width > it.height) Rational(16, 9)
                else Rational(9, 16)
            }

}