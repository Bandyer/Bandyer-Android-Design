package com.kaleyra.video_sdk.termsandconditions.screen

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.termsandconditions.model.TermsAndConditionsUiState
import com.kaleyra.video_sdk.termsandconditions.viewmodel.TermsAndConditionsViewModel
import com.kaleyra.video_sdk.theme.TermsAndConditionsTheme

internal const val TermsProgressIndicatorTag = "TermsProgressIndicatorTag"

@Composable
internal fun TermsAndConditionsScreen(
    viewModel: TermsAndConditionsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = TermsAndConditionsViewModel.provideFactory(::requestConfiguration)
    ),
    title: String,
    message: String,
    acceptText: String,
    declineText: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TermsAndConditionsScreen(
        uiState = uiState,
        title = title,
        message = message,
        acceptText = acceptText,
        declineText = declineText,
        onAccept = onAccept,
        onDecline = remember(onDecline) {
            {
                onDecline()
                viewModel.decline()
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun TermsAndConditionsScreen(
    uiState: TermsAndConditionsUiState,
    title: String,
    message: String,
    acceptText: String,
    declineText: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.findActivity()

    if (uiState.isDeclined || uiState.isConnected) {
        LaunchedEffect(Unit) {
            activity.finishAndRemoveTask()
        }
    }

    Surface(modifier) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
        ) {
            val (titleRef, messageRef, buttonsRef) = createRefs()

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(titleRef) {
                        top.linkTo(parent.top)
                    }
            )
            Text(
                text = message,
                fontSize = 14.sp,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .constrainAs(messageRef) {
                        top.linkTo(titleRef.bottom, margin = 24.dp)
                        bottom.linkTo(buttonsRef.top)
                        height = Dimension.fillToConstraints
                    }
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(buttonsRef) {
                        top.linkTo(messageRef.bottom, margin = 24.dp)
                        bottom.linkTo(parent.bottom)
                    }
            ) {
                TextButton(
                    onClick = onDecline,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.onSurface),
                    content = { Text(text = declineText, fontSize = 14.sp) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                var loading by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        loading = true
                        onAccept()
                    },
                    modifier = Modifier.animateContentSize()
                ) {
                    if (!loading) {
                        Text(text = acceptText, fontSize = 14.sp)
                    } else {
                        CircularProgressIndicator(
                            color = MaterialTheme.colors.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(18.dp)
                                .testTag(TermsProgressIndicatorTag)
                        )
                    }
                }
            }
        }
    }

}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun TermsAndConditionsScreenPreview() {
    TermsAndConditionsTheme {
        TermsAndConditionsScreen(
            uiState = TermsAndConditionsUiState(),
            "Terms and Conditions",
            "Terms message ",
            "Accept",
            "Decline",
            {},
            {}
        )
    }
}