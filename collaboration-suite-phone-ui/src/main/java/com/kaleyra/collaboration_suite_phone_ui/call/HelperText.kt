package com.kaleyra.collaboration_suite_phone_ui.call

import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.call.shadow

@Composable
fun HelperText(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified, textAlign: TextAlign? = null) =
    Text(text = text, color = color, textAlign = textAlign, style = LocalTextStyle.current.shadow(), fontSize = 12.sp, fontStyle = FontStyle.Italic, modifier = modifier)