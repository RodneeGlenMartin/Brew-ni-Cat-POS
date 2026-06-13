package com.example.cattasticpos.ui.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import com.example.cattasticpos.ui.theme.AlabasterPalette
import com.example.cattasticpos.ui.theme.adaptiveBodyMuted
import com.example.cattasticpos.ui.theme.alabasterSpecularBorderBrush
import com.example.cattasticpos.ui.theme.adaptiveGlassFill
import com.example.cattasticpos.ui.theme.adaptiveGlassRadius
import com.example.cattasticpos.ui.theme.neonSelectionBrush
import com.example.cattasticpos.ui.theme.specularBorderBrush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cattasticpos.ui.icons.FluentIcon
import com.example.cattasticpos.ui.icons.FluentIcons
import com.example.cattasticpos.ui.icons.PosChipIcon
import com.example.cattasticpos.ui.theme.adaptiveGlassBrush
import com.example.cattasticpos.ui.adaptive.iOSSpring
import com.example.cattasticpos.ui.adaptive.rememberPosFeedback

@Composable
fun CupertinoSection(
    modifier: Modifier = Modifier,
    header: String? = null,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cupertino = LocalCupertinoColors.current
    val darkTheme = isSystemInDarkTheme()
    val radius = adaptiveGlassRadius(darkTheme)
    val shape = RoundedCornerShape(radius)
    val borderBrush = if (darkTheme) specularBorderBrush() else alabasterSpecularBorderBrush()

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
                .clip(shape)
                .background(adaptiveGlassFill(darkTheme))
                .border(1.dp, borderBrush, shape)
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
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = iOSSpring,
        label = "adaptiveButtonScale"
    )
    val performFeedback = rememberPosFeedback()

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
                onClick = {
                    performFeedback(FeedbackEvent(BionicHaptic.Confirm, PosSound.Confirm))
                    onClick()
                }
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides contentColor,
                LocalTextStyle provides MaterialTheme.typography.labelLarge.copy(color = contentColor)
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
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val cupertino = LocalCupertinoColors.current
    val darkTheme = isSystemInDarkTheme()
    val shape = RoundedCornerShape(14.dp)
    val fill = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
    } else {
        adaptiveGlassFill(darkTheme)
    }
    val borderBrush = if (selected) {
        neonSelectionBrush(cupertino.accent)
    } else if (darkTheme) {
        specularBorderBrush()
    } else {
        alabasterSpecularBorderBrush()
    }
    val fg = if (selected) {
        if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    } else {
        adaptiveBodyMuted(darkTheme)
    }
    val performFeedback = rememberPosFeedback()

    Box(
        modifier = modifier
            .clip(shape)
            .background(fill)
            .border(1.dp, borderBrush, shape)
            .clickable(onClick = {
                performFeedback(FeedbackEvent(BionicHaptic.Selection, PosSound.Select))
                onClick()
            })
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                PosChipIcon(imageVector = icon, selected = selected)
            }
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = fg
            )
        }
    }
}

@Composable
fun SelectableOptionRow(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailing: String? = null
) {
    val darkTheme = isSystemInDarkTheme()
    val cupertino = LocalCupertinoColors.current
    val labelColor = if (isSelected) {
        if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    } else {
        adaptiveBodyMuted(darkTheme)
    }
    val optionShape = RoundedCornerShape(16.dp)
    val performFeedback = rememberPosFeedback()
    val checkScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.85f,
        animationSpec = iOSSpring,
        label = "checkScale"
    )
    val checkAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = iOSSpring,
        label = "checkAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(optionShape)
            .then(
                if (isSelected) {
                    Modifier.background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                        shape = optionShape
                    )
                } else {
                    Modifier.background(
                        brush = adaptiveGlassBrush(darkTheme),
                        shape = optionShape
                    )
                }
            )
            .border(
                width = 1.dp,
                brush = if (isSelected) neonSelectionBrush(cupertino.accent) else androidx.compose.ui.graphics.SolidColor(
                    if (darkTheme) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
                ),
                shape = optionShape
            )
            .clickable {
                performFeedback(FeedbackEvent(BionicHaptic.Selection, PosSound.Select))
                onSelect()
            }
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (leadingIcon != null) {
                PosChipIcon(imageVector = leadingIcon, selected = isSelected)
            }
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = labelColor
            )
            if (!trailing.isNullOrBlank()) {
                Text(
                    text = trailing,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else labelColor
                )
            }
            if (isSelected) {
                FluentIcon(
                    imageVector = FluentIcons.CheckmarkCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = checkScale
                            scaleY = checkScale
                            alpha = checkAlpha
                        },
                    size = 20.dp
                )
            }
        }
    }
}
