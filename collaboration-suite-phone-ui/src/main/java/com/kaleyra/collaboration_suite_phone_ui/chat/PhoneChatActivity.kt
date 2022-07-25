package com.kaleyra.collaboration_suite_phone_ui.chat

import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutWidget
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatUnreadMessagesWidget
import com.kaleyra.collaboration_suite_phone_ui.extensions.getAttributeResourceId

class PhoneChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_call)
        setContent {
            MdcTheme {
                ChatScreen(onBackPressed = { onBackPressed() })
            }
        }
    }
}

@Composable
fun ChatScreen(modifier: Modifier = Modifier, onBackPressed: () -> Unit) {
    Scaffold(
        topBar = { ChatTopAppBar(navigationIcon = { NavigationIcon(onBackPressed = onBackPressed) }) },
        modifier = modifier
    ) {
        ConstraintLayout(Modifier.fillMaxSize()) {
            val (fab, input) = createRefs()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(fab) {
                        bottom.linkTo(input.top, margin = 16.dp)
                        end.linkTo(parent.end, margin = 16.dp)
                    },
                horizontalArrangement = Arrangement.End,
            ) {
                AndroidView(
                    factory = { KaleyraChatUnreadMessagesWidget(it) },
                )
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .constrainAs(input) {
                    bottom.linkTo(parent.bottom)
                }) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth(),
                    factory = {
                        val themeResId =
                            it.theme.getAttributeResourceId(R.attr.kaleyra_chatInputWidgetStyle)
                        KaleyraChatInputLayoutWidget(ContextThemeWrapper(it, themeResId))
                    }
                )
            }
        }
    }
}

@Composable
fun ChatTopAppBar(
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)
) {
    TopAppBar(
        modifier = modifier
    ) {
        Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.high,
                content = navigationIcon
            )
        }

        AndroidView(
            factory = {
                val themeResId = it.theme.getAttributeResourceId(R.attr.kaleyra_chatInfoWidgetStyle)
                KaleyraChatInfoWidget(ContextThemeWrapper(it, themeResId))
            },
            update = {

            }
        )
    }
}

@Composable
fun NavigationIcon(modifier: Modifier = Modifier, onBackPressed: () -> Unit) {
    IconButton(modifier = modifier, onClick = onBackPressed) {
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = stringResource(id = R.string.kaleyra_back)
        )
    }
}
