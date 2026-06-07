package com.example.cattasticpos.ui.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import com.example.cattasticpos.ui.theme.ObsidianPalette
import com.example.cattasticpos.ui.theme.neonSelectionBrush
import com.example.cattasticpos.ui.theme.specularBorderBrush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CupertinoSection(
    modifier: Modifier = Modifier,
    header: String? = null,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cupertino = LocalCupertinoColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        header?.let {
            Text(
                text = it.uppercase(),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
                color = cupertino.secondaryLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ObsidianPalette.GlassRadius))
                .background(ObsidianPalette.GlassFill)
                .border(1.dp, specularBorderBrush(), RoundedCornerShape(ObsidianPalette.GlassRadius))
        ) {
            content()
        }
        footer?.let {
            Text(
                text = it,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp),
                color = cupertino.secondaryLabel,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun CupertinoFormRow(
    label: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    trailing: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit
) {
    val cupertino = LocalCupertinoColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = label,
                color = cupertino.label,
                fontSize = 16.sp,
                modifier = Modifier.weight(0.42f)
            )
            Box(modifier = Modifier.weight(0.58f)) {
                content()
            }
            trailing()
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp),
                color = cupertino.separator.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
fun AdaptiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = LocalCupertinoColors.current.accent,
    contentColor: Color = LocalCupertinoColors.current.onAccent,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale = if (pressed) 0.96f else 1f

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) containerColor else containerColor.copy(alpha = 0.45f))
            .semantics { role = Role.Button }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides contentColor
            ) {
                content()
            }
        }
    }
}

@Composable
fun CupertinoSegmentChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val cupertino = LocalCupertinoColors.current
    val shape = RoundedCornerShape(14.dp)
    val fill = if (selected) cupertino.accent.copy(alpha = 0.1f) else ObsidianPalette.GlassFill
    val borderBrush = if (selected) neonSelectionBrush(cupertino.accent) else specularBorderBrush()
    val fg = if (selected) Color.White else ObsidianPalette.BodyMuted

    Box(
        modifier = modifier
            .clip(shape)
            .background(fill)
            .border(1.dp, borderBrush, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = fg
        )
    }
}
