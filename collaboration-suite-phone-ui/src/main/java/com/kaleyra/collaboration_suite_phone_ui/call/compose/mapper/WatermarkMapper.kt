package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite_core_ui.Theme
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Logo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

internal object WatermarkMapper {
    fun Flow<Theme>.toWatermarkInfo(companyName: Flow<String>): Flow<WatermarkInfo> = combine(companyName, this) { name, theme ->
        WatermarkInfo(logo = Logo(theme.day.logo, theme.night.logo), text = name)
    }.distinctUntilChanged()
}