package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun CallScreen() {
    val navigationBarsInsets = WindowInsets.navigationBars
    val navigationBarsInsetsPadding = WindowInsets.navigationBars.asPaddingValues()
    val sheetState = rememberBottomSheetScaffoldState(
        initialValue = BottomSheetValue.Collapsed
    )
    val scope = rememberCoroutineScope()
    BottomSheetScaffold(
        modifier = Modifier,
        sheetState = sheetState,
        sheetBackgroundColor = Color.Green,
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
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(navigationBarsInsetsPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bottom sheet",
                    fontSize = 60.sp
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(Color.Cyan),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                scope.launch {
                    if (sheetState.isCollapsed) {
                        sheetState.expand()
                    } else {
                        sheetState.collapse()
                    }
                }
            }) {
                Text(text = "Fraction: ${sheetState.currentValue}")
            }
        }
    }
}

@Preview
@Composable
fun CallScreenPreview() {
    CallScreen()
}