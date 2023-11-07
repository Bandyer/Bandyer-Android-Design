package com.kaleyra.video_sdk.call.mapper

import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

internal object WatermarkMapper {
    fun Flow<CompanyUI.Theme>.toWatermarkInfo(companyName: Flow<String>): Flow<WatermarkInfo> = combine(companyName, this) { name, theme ->
        WatermarkInfo(logo = Logo(theme.day.logo, theme.night.logo), text = name)
    }.distinctUntilChanged()
}