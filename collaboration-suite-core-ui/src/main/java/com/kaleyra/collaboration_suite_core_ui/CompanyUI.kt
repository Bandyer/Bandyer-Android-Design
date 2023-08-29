package com.kaleyra.collaboration_suite_core_ui

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.kaleyra.collaboration_suite.Company
import com.kaleyra.collaboration_suite_core_ui.theme.kaleyra_theme_dark_primary
import com.kaleyra.collaboration_suite_core_ui.theme.kaleyra_theme_dark_secondary
import com.kaleyra.collaboration_suite_core_ui.theme.kaleyra_theme_light_primary
import com.kaleyra.collaboration_suite_core_ui.theme.kaleyra_theme_light_secondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class CompanyUI(
    private val company: Company?,
    private val scope: CoroutineScope?
) : Company by company ?: NoOpCompany() {

    private val mutableTheme: MutableSharedFlow<Theme> = MutableSharedFlow(replay = 1)

    override val theme: SharedFlow<Theme> = mutableTheme

    private var companyThemeJob: Job? = null

    fun setTheme(theme: Theme) {
        companyThemeJob?.cancel()
        scope?.launch { this@CompanyUI.mutableTheme.emit(theme) }
    }

    init {
        companyThemeJob = scope?.launch {
            company?.theme?.collect {
                val theme = Theme(day = Theme().day.copy(logo = it.day.logo), night = Theme().night.copy(logo = it.night.logo))
                mutableTheme.emit(theme)
            }
        }
    }
}

internal class NoOpCompany(
    override val name: SharedFlow<String> = MutableSharedFlow(),
    override val id: SharedFlow<String> = MutableSharedFlow(),
    override val theme: SharedFlow<Company.Theme> = MutableSharedFlow()
) : Company

data class Theme(
    val fontFamily: FontFamily = KaleyraFontFamily.fontFamily,
    val defaultStyle: DefaultStyle = DefaultStyle.System,
    override val day: Style = Style(colors = Colors(primary = kaleyra_theme_light_primary, secondary = kaleyra_theme_light_secondary)),
    override val night: Style = Style(colors =  Colors(primary = kaleyra_theme_dark_primary, secondary = kaleyra_theme_dark_secondary))
) : Company.Theme {

    sealed class DefaultStyle {
        object Day: DefaultStyle()

        object Night: DefaultStyle()

        object System: DefaultStyle()
    }

    data class Style(override val logo: Uri = Uri.EMPTY, val colors: Colors) : Company.Theme.Style

    data class Colors(val primary: Color, val secondary: Color)
}

