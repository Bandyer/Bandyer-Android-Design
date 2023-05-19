package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite.Company
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Logo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

internal object WatermarkMapper {
    fun Flow<Company>.toWatermarkInfo(): Flow<WatermarkInfo> {
        return combine(this.flatMapLatest { it.name }, this.flatMapLatest { it.theme }) { name, theme ->
            WatermarkInfo(logo = Logo(theme.day.logo, theme.night.logo), text = name)
        }
    }
}