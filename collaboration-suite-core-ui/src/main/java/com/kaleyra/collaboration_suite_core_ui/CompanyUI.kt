package com.kaleyra.collaboration_suite_core_ui

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.kaleyra.collaboration_suite.Company
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class CompanyUI(company: Company): Company by company {

    data class Theme(
        val fontFamily: FontFamily = KaleyraFontFamily.default,
        val defaultStyle: DefaultStyle = DefaultStyle.System,
        override val day: Style = Style(),
        override val night: Style = Style()
    ) : Company.Theme {

        sealed class DefaultStyle {
            object Day: DefaultStyle()

            object Night: DefaultStyle()

            object System: DefaultStyle()
        }

        data class Style(override val logo: Uri? = null, val colors: Colors? = null) : Company.Theme.Style

        data class Colors(val secondary: Color)
    }
}

internal class NoOpCompany(
    override val name: SharedFlow<String> = MutableSharedFlow(),
    override val id: SharedFlow<String> = MutableSharedFlow(),
    override val theme: SharedFlow<Company.Theme> = MutableSharedFlow()
) : Company
