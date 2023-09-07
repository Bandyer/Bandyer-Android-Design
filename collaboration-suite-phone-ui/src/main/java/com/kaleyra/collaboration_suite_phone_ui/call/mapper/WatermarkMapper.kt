package com.kaleyra.collaboration_suite_phone_ui.call.mapper

import com.kaleyra.collaboration_suite_core_ui.CompanyUI
import com.kaleyra.collaboration_suite_phone_ui.call.streams.Logo
import com.kaleyra.collaboration_suite_phone_ui.call.streams.WatermarkInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

internal object WatermarkMapper {
    fun Flow<CompanyUI.Theme>.toWatermarkInfo(companyName: Flow<String>): Flow<WatermarkInfo> = combine(companyName, this) { name, theme ->
        WatermarkInfo(logo = Logo(theme.day.logo, theme.night.logo), text = name)
    }.distinctUntilChanged()
}