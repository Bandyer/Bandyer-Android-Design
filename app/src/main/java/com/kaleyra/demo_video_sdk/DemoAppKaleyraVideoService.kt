package com.kaleyra.demo_video_sdk

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.kaleyra.app_configuration.model.UserDetailsProviderMode.CUSTOM
import com.kaleyra.app_configuration.utils.MediaStorageUtils.getUriFromString
import com.kaleyra.app_utilities.storage.ConfigurationPrefsManager
import com.kaleyra.app_utilities.storage.LoginManager
import com.kaleyra.demo_video_sdk.storage.DefaultConfigurationManager
import com.kaleyra.demo_video_sdk.ui.custom_views.mapToCallUIActions
import com.kaleyra.video_common_ui.ChatUI
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.KaleyraVideoService
import com.kaleyra.video_common_ui.model.UserDetails
import com.kaleyra.video_common_ui.model.UserDetailsProvider
import com.kaleyra.video_sdk.extensions.configure
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class DemoAppKaleyraVideoService : KaleyraVideoService() {

    companion object {

        fun configure(context: Context) {
            val configuration = context.configuration()
            if (!KaleyraVideo.isConfigured) {
                KaleyraVideo.configure(configuration)
                KaleyraVideo.userDetailsProvider = customUserDetailsProvider(context)
                KaleyraVideo.theme =
                    CompanyUI.Theme(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                        defaultStyle = CompanyUI.Theme.DefaultStyle.System,
                        day = CompanyUI.Theme.Style(colors = CompanyUI.Theme.Colors(secondary = Color(0xFF0087E2))),
                        night = CompanyUI.Theme.Style(colors = CompanyUI.Theme.Colors(secondary = Color.Yellow))
                    )
                KaleyraVideo.conference.call.onEach {
                    val callConfiguration = DefaultConfigurationManager.getDefaultCallConfiguration()
                    it.actions.value = callConfiguration.actions.mapToCallUIActions()
                    it.withFeedback = callConfiguration.options.feedbackEnabled
                    it.backCameraAsDefault = callConfiguration.options.backCameraAsDefault
                }.launchIn(MainScope())
            }
        }

        internal fun customUserDetailsProvider(context: Context): UserDetailsProvider? {
            val appConfiguration = ConfigurationPrefsManager.getConfiguration(context)
            if (appConfiguration.userDetailsProviderMode != CUSTOM) return null
            return { userIds: List<String> ->
                Result.success(userIds.map {
                    UserDetails(
                        userId = it,
                        name = appConfiguration.customUserDetailsName ?: it,
                        image = getUriFromString(appConfiguration.customUserDetailsImageUrl) ?: Uri.EMPTY
                    )
                })
            }
        }
        fun connect(context: Context) {
            val loggedUserId = LoginManager.getLoggedUser(context)
            if (!LoginManager.isUserLogged(context)) return
            KaleyraVideo.connect(loggedUserId) { requestToken(loggedUserId) }
        }
    }

    override suspend fun onRequestKaleyraVideoConfigure(): Unit = configure(applicationContext)
}
