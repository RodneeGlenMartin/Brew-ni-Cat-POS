package com.example.cattasticpos.ui.components.unstyled

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.cattasticpos.ui.adaptive.AdaptiveButton
import com.example.cattasticpos.ui.adaptive.CupertinoSegmentChip

/**
 * Unstyled foundation wrappers routed through adaptive Cupertino primitives.
 */
@Composable
fun PosButtonIconLabel(
    icon: @Composable () -> Unit,
    label: String,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    iconSpacing: androidx.compose.ui.unit.Dp = 8.dp
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(iconSpacing))
        Text(
            text = label,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PosPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable () -> Unit
) {
    AdaptiveButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        containerColor = containerColor,
        contentColor = contentColor,
        contentPadding = contentPadding,
        content = {
            CompositionLocalProvider(
                LocalContentColor provides contentColor,
                LocalTextStyle provides MaterialTheme.typography.labelLarge.copy(color = contentColor)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    content()
                }
            }
        }
    )
}

@Composable
fun PosFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    CupertinoSegmentChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        icon = icon
    )
}

@Composable
fun PosFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AdaptiveButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 56.dp, minHeight = 56.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        contentPadding = PaddingValues(16.dp)
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onPrimary,
            LocalTextStyle provides MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
        }
    }
}

@Composable
fun PosOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    CupertinoFormTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        singleLine = singleLine,
        keyboardType = keyboardType,
        visualTransformation = visualTransformation
    )
}

@Composable
private fun CupertinoFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier,
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            inner()
        }
    )
}
