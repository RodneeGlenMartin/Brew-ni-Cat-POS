@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cattasticpos.ui.adaptive

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cattasticpos.ui.theme.AlabasterPalette
import com.example.cattasticpos.ui.theme.AdaptiveGlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = topBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = contentWindowInsets,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    largeTitle: Boolean = true,
    hazeState: dev.chrisbanes.haze.HazeState? = null
) {
    val cupertino = LocalCupertinoColors.current
    val glassModifier = if (hazeState != null) {
        Modifier.liquidGlassChild(state = hazeState).then(modifier)
    } else {
        modifier
    }
    val colors = TopAppBarDefaults.largeTopAppBarColors(
        containerColor = if (hazeState != null) Color.Transparent else MaterialTheme.colorScheme.background,
        scrolledContainerColor = if (hazeState != null) Color.Transparent else MaterialTheme.colorScheme.surface,
        navigationIconContentColor = cupertino.accent,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        actionIconContentColor = cupertino.accent
    )

    if (largeTitle && scrollBehavior != null) {
        LargeTopAppBar(
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = navigationIcon,
            actions = actions,
            scrollBehavior = scrollBehavior,
            colors = colors,
            modifier = glassModifier
        )
    } else {
        androidx.compose.material3.TopAppBar(
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (hazeState != null) Color.Transparent else MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = cupertino.accent,
                actionIconContentColor = cupertino.accent
            ),
            modifier = glassModifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberAdaptiveTopBarScrollBehavior(): TopAppBarScrollBehavior {
    return TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = androidx.compose.material3.rememberTopAppBarState()
    )
}

fun Modifier.adaptiveNestedScroll(scrollBehavior: TopAppBarScrollBehavior?): Modifier {
    return if (scrollBehavior != null) {
        nestedScroll(scrollBehavior.nestedScrollConnection)
    } else {
        this
    }
}

@Composable
fun AdaptiveSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(
        hostState = hostState,
        snackbar = { data -> AdaptiveGlassSnackbar(data) }
    )
}

@Composable
private fun AdaptiveGlassSnackbar(data: SnackbarData) {
    val darkTheme = isSystemInDarkTheme()
    val textColor = if (darkTheme) Color.White else AlabasterPalette.Heading

    AdaptiveGlassCard(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = data.visuals.message,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AdaptiveFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = LocalCupertinoColors.current.accent,
        contentColor = LocalCupertinoColors.current.onAccent,
        content = content
    )
}
