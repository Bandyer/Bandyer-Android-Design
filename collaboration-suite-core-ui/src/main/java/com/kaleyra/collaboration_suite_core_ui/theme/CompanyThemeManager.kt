package com.kaleyra.collaboration_suite_core_ui.theme

import com.kaleyra.collaboration_suite.Company
import com.kaleyra.collaboration_suite_core_ui.CompanyUI
import com.kaleyra.collaboration_suite_core_ui.KaleyraVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

object CompanyThemeManager {

    val Company.combinedTheme: Flow<CompanyUI.Theme>
        get() = KaleyraVideo.theme?.let { flowOf(it) } ?: kotlin.run { theme.mapToUI() }

    private fun Flow<Company.Theme>.mapToUI(): Flow<CompanyUI.Theme> =
        map { theme ->
            val defaultTheme = CompanyUI.Theme()
            CompanyUI.Theme(day = defaultTheme.day.copy(logo = theme.day.logo), night = defaultTheme.night.copy(logo = theme.night.logo))
        }

}
