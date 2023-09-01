package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import android.util.Rational
import com.kaleyra.collaboration_suite.conference.VideoStreamView
import com.kaleyra.collaboration_suite_core_ui.utils.MathUtils
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
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
            .flatMapLatest { it.videoSize }
            .map {
                val gcd = MathUtils.findGreatestCommonDivisor(it.width, it.height)
                if (gcd != 0) Rational(it.width / gcd, it.height / gcd)
                else Rational.NaN
            }
            .distinctUntilChanged()

}