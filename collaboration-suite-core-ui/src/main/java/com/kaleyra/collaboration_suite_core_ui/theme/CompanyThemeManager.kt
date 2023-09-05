package com.kaleyra.collaboration_suite_core_ui.theme

import com.kaleyra.collaboration_suite.Company
import com.kaleyra.collaboration_suite_core_ui.CompanyUI
import com.kaleyra.collaboration_suite_core_ui.KaleyraVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object CompanyThemeManager {

    val Company.combinedTheme: Flow<CompanyUI.Theme>
        get() = theme.mapToUI(uiTheme = KaleyraVideo.theme)

    private fun Flow<Company.Theme>.mapToUI(uiTheme: CompanyUI.Theme?): Flow<CompanyUI.Theme> =
        map { theme ->
            // TODO add remote colors fallback
            uiTheme?.copy(
                day = CompanyUI.Theme.Style(logo = uiTheme.day.logo ?: theme.day.logo, colors = uiTheme.day.colors),
                night = CompanyUI.Theme.Style(logo = uiTheme.night.logo ?: theme.night.logo, colors = uiTheme.night.colors)
            ) ?: // TODO add remote colors
            CompanyUI.Theme(day = CompanyUI.Theme.Style(logo = theme.day.logo), night = CompanyUI.Theme.Style(logo = theme.night.logo))
        }

}
