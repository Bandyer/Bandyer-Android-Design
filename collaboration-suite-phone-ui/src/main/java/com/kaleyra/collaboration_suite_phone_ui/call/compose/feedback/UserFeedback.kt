package com.kaleyra.collaboration_suite_phone_ui.call.compose.feedback

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kaleyra.collaboration_suite_core_ui.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.IconButton

private const val DefaultRating = 5f
private const val SliderLevels = 5

@Composable
internal fun UserFeedback(onUserFeedback: (Float, String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        var textFieldValue by remember { mutableStateOf(TextFieldValue()) }
        var isEditTextFocused by remember { mutableStateOf(false) }
        var sliderValue by remember { mutableStateOf(DefaultRating) }

        Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                IconButton(
                    icon = painterResource(id = R.drawable.ic_kaleyra_close),
                    iconDescription = stringResource(id = R.string.kaleyra_close),
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                )

                AnimatedVisibility(visible = !isEditTextFocused) {
                    Text(
                        text = stringResource(id = R.string.kaleyra_feedback_evaluate_call),
                        color = MaterialTheme.colors.onSurface.copy(alpha = .8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                StarSlider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    levels = SliderLevels,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = ratingTextFor(sliderValue),
                    fontSize = 16.sp,
                    modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 28.dp))

                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    placeholder = {
                        if (!isEditTextFocused) {
                            Text(
                                text = stringResource(id = R.string.kaleyra_feedback_leave_a_comment),
                                fontSize = 14.sp,
                                color = MaterialTheme.colors.onSurface.copy(.5f)
                            )
                        }
                    },
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 24.dp)
                        .fillMaxWidth()
                        .onFocusChanged {
                            isEditTextFocused = it.hasFocus
                        }
                        .animateContentSize(),
                    maxLines = 4,
                    minLines = if (isEditTextFocused) 4 else 1,
                    textStyle = TextStyle(
                        fontSize = 14.sp
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = if (isEditTextFocused) MaterialTheme.colors.onSurface.copy(.12f) else Color.Transparent,
                        cursorColor = MaterialTheme.colors.secondary,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(.3f),
                        focusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onUserFeedback(sliderValue, textFieldValue.text) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.kaleyra_feedback_vote),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ratingTextFor(sliderValue: Float): String {
    val stringId by remember(sliderValue) {
        derivedStateOf {
            when {
                sliderValue < 2f -> R.string.kaleyra_feedback_bad
                sliderValue < 3f -> R.string.kaleyra_feedback_poor
                sliderValue < 4f -> R.string.kaleyra_feedback_neutral
                sliderValue < 5f -> R.string.kaleyra_feedback_good
                else -> R.string.kaleyra_feedback_excellent
            }
        }
    }
    return stringResource(id = stringId)
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun UserFeedbackPreview() = KaleyraTheme {
    UserFeedback({ _, _ -> }, {})
}
