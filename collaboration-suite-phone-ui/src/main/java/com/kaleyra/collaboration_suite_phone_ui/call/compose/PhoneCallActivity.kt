package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.os.Bundle
import android.util.Log
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.composethemeadapter.MdcTheme
import kotlinx.coroutines.launch

class PhoneCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme {
                CallScreen()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CallScreen() {
    val navigationBarsInsets = WindowInsets.navigationBars
    val navigationBarsInsetsPadding = WindowInsets.navigationBars.asPaddingValues()
    val sheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.Collapsed
    )
    BottomSheetScaffold(
        modifier = Modifier,
        sheetState = sheetState,
        sheetPeekHeight = 60.dp,
//        sheetHalfExpandedHeight = 150.dp,
        anchor = {
            Box {
                CompositionLocalProvider(
                    LocalOverScrollConfiguration provides null
                ) {
                    LazyRow(reverseLayout = true) {
                        items(8) {
                            Text(
                                text = "Anchor", modifier = Modifier
                                    .padding(8.dp)
                                    .size(56.dp)
                                    .background(Color.Red)
                            )
                        }
                    }
                }
            }
        },
        sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        backgroundColor = Color.Black,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Line(sheetState)
            }
        }
    ) { sheetPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(sheetPadding),
            contentAlignment = Alignment.Center
        ) {
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Line(bottomSheetState: BottomSheetState) {
    val isExpanded by remember(bottomSheetState) {
        derivedStateOf {
            val progress = bottomSheetState.progress
            when {
                progress.fraction == 1f && bottomSheetState.isExpanded -> 1f
                progress.from == BottomSheetValue.HalfExpanded && progress.to == BottomSheetValue.Expanded -> progress.fraction
                progress.from == BottomSheetValue.Expanded && progress.to == BottomSheetValue.HalfExpanded -> 1 - progress.fraction
                else -> 0f
            }
        }
    }
    Log.e("PhoneCallActivity", "KLR-fraction: ${bottomSheetState.progress.from}")
    val transitionLabel = "LineAnimation"
    val transition = updateTransition(targetState = isExpanded, label = transitionLabel)
    val width by transition.animateDp(label = transitionLabel) { state ->
        28.dp - (24 * state).dp
    }
    Spacer(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .size(width, 4.dp)
            .background(
                color = LocalContentColor.current.copy(alpha = 0.8f),
                shape = CircleShape
            )
    )
}


@Preview
@Composable
fun CallScreenPreview() {
    CallScreen()
}