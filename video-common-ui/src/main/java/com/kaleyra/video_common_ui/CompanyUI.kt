/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_common_ui

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.kaleyra.video.Company
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
