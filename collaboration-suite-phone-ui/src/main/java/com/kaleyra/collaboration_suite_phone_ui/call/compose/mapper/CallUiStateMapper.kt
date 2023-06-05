package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import android.util.Rational
import com.kaleyra.collaboration_suite.phonebox.VideoStreamView
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.utility.MathUtils.findGreatestCommonDivisor
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
                val gcd = findGreatestCommonDivisor(it.width, it.height)
                if (gcd != 0) Rational(it.width / gcd, it.height / gcd)
                else Rational(1,1)
            }

}