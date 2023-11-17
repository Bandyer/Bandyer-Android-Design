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

package com.kaleyra.video_common_ui.theme

import com.kaleyra.video.Company
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.KaleyraVideo
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
