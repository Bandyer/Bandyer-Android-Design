package com.kaleyra.collaboration_suite_phone_ui.call.compose.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
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

@Composable
fun UserFeedback(onUserFeedback: (Float, String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        var textFieldValue by remember { mutableStateOf(TextFieldValue()) }
        Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                var isEditTextFocused by remember { mutableStateOf(false) }
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
                var rating by remember { mutableStateOf(DefaultRating) }
                StartSlider(
                    value = rating,
                    onValueChange = { rating = it }
                )
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
                        .padding(all = 16.dp)
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
                        backgroundColor = if (isEditTextFocused) MaterialTheme.colors.onSurface.copy(
                            .12f
                        ) else Color.Transparent,
                        cursorColor = MaterialTheme.colors.secondary,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(.3f),
                        focusedBorderColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { },
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
fun StartSlider(value: Float, onValueChange: (Float) -> Unit) {
    Layout(
        content = {
            Slider(
                value = value,
                onValueChange = onValueChange,
                steps = 3,
                valueRange = 1f.rangeTo(5f),
                onValueChangeFinished = {},
                modifier = Modifier.alpha(0f)
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                repeat(3 + 2) { index ->
                    val scale by animateFloatAsState(targetValue = if (index <= value - 1) 1f else .75f)
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_empty_star),
                        contentDescription = null,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(3 + 2) { index ->
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_full_star),
                        contentDescription = null,
                        tint = MaterialTheme.colors.secondary,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer {
                                alpha = if (index <= value - 1) 1f else 0f
                            }
                    )
                }
            }
        }
    ) { measurables, constraints ->
        val slider = measurables[0].measure(constraints)
        val track = measurables[1].measure(constraints)
        val indicator = measurables[2].measure(constraints)

        layout(slider.width, slider.height) {
            slider.placeRelative(0, 0)
            track.placeRelative(0, slider.height / 2 - track.height / 2)
            indicator.placeRelative(0, slider.height / 2 - track.height / 2)
        }
    }

}

@Preview
@Composable
internal fun UserFeedbackPreview() = KaleyraTheme {
    UserFeedback({ _, _ -> }, {})
}
