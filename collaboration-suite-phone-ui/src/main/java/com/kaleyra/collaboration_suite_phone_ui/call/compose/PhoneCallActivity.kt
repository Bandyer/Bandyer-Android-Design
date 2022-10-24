@file:OptIn(ExperimentalMaterialApi::class)

package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import kotlinx.coroutines.launch

class PhoneCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme(setDefaultFontFamily = true) {
                CallScreen()
            }
        }
    }
}

@Composable
fun CallScreen() {
    val sheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.Collapsed
    )
    val isCollapsed by remember(sheetState) {
        derivedStateOf { sheetState.targetValue == BottomSheetValue.Collapsed && sheetState.progress.fraction == 1f }
    }
    val alpha by animateFloatAsState(if (isCollapsed) 0f else 1f)
    val navigationBottomInsets =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    BottomSheetScaffold(sheetState = sheetState,
        sheetPeekHeight = 48.dp + navigationBottomInsets,
        sheetHalfExpandedHeight = 150.dp + navigationBottomInsets,
        anchor = { Anchor() },
        sheetBackgroundColor = MaterialTheme.colors.surface.copy(alpha = alpha),
        sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        backgroundColor = Color.Black,
        contentColor = Color.White,
        sheetContent = { BottomSheetContent(sheetState) }
    ) { sheetPadding -> ScreenContent(sheetState, sheetPadding) }
}

@Composable
fun BottomSheetContent(sheetState: BottomSheetState) {
    val collapsed by remember(sheetState) {
        derivedStateOf {
            sheetState.targetValue == BottomSheetValue.Collapsed
        }
    }
    val contentColor = LocalContentColor.current
    val color by remember(sheetState) {
        derivedStateOf {
            if (sheetState.targetValue == BottomSheetValue.Collapsed && sheetState.progress.fraction == 1f) Color.White
            else contentColor.copy(alpha = 0.8f)
        }
    }
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Line(
            collapsed = collapsed,
            onClickLabel = "clickLabel",
            color = color,
            onClick = {
                if (sheetState.targetValue == BottomSheetValue.Collapsed) {
                    scope.launch {
                        sheetState.halfExpand()
                    }
                }
            })
    }
}

@Composable
fun ScreenContent(sheetState: BottomSheetState, sheetPadding: WindowInsets) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(sheetPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Current value: ${sheetState.currentValue}")
        Text(text = "Target value: ${sheetState.targetValue}")
        Text(text = "Direction: ${sheetState.direction}")
        Text(text = "Fraction: ${sheetState.progress.fraction}")
        Text(text = "Offset: ${sheetState.offset.value}")
        Text(text = "Overflow: ${sheetState.overflow.value}")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Anchor() {
    Box {
        CompositionLocalProvider(
            LocalOverScrollConfiguration provides null
        ) {
            LazyRow(reverseLayout = true) {
                items(8) {
                    Text(
                        text = "Anchor",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(56.dp)
                            .background(Color.Red)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CallScreenPreview() {
    CallScreen()
}