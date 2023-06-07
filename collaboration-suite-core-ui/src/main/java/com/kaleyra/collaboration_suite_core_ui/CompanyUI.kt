package com.kaleyra.collaboration_suite_core_ui

import android.net.Uri
import com.kaleyra.collaboration_suite.Company
import com.kaleyra.collaboration_suite_core_ui.Theme.Style
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
                val theme = Theme(Style(it.day.logo), Style(it.night.logo))
                mutableTheme.emit(theme)
            }
        }
    }
}

internal class NoOpCompany(
    override val name: SharedFlow<String> = MutableSharedFlow(),
    override val theme: SharedFlow<Company.Theme> = MutableSharedFlow()
) : Company

data class Theme(override val day: Style, override val night: Style) : Company.Theme {
    data class Style(override val logo: Uri = Uri.EMPTY) : Company.Theme.Style
}

