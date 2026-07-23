package com.example.cattasticpos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.cattasticpos.ui.icons.FluentIcon
import com.example.cattasticpos.ui.icons.FluentIcons
import com.example.cattasticpos.ui.icons.PosIconSize
import com.example.cattasticpos.ui.theme.adaptiveBodyMuted
import com.example.cattasticpos.ui.theme.adaptiveGlassBrush
import com.example.cattasticpos.ui.theme.adaptiveGlassContentColor

@Composable
fun GlassSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClose: (() -> Unit)? = null
) {
    val darkTheme = isSystemInDarkTheme()
    val shape = RoundedCornerShape(12.dp)
    val textColor = adaptiveGlassContentColor(darkTheme)
    val placeholderColor = adaptiveBodyMuted(darkTheme)
    val borderColor = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .then(if (compact) Modifier.height(40.dp) else Modifier.fillMaxWidth())
            .clip(shape)
            .background(adaptiveGlassBrush(darkTheme))
            .border(1.dp, borderColor, shape),
        placeholder = {
            Text(
                placeholder,
                color = placeholderColor,
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            FluentIcon(
                imageVector = FluentIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                size = if (compact) PosIconSize.Small else PosIconSize.Medium
            )
        },
        trailingIcon = if (onClose != null) {
            {
                IconButton(onClick = onClose, modifier = Modifier.size(if (compact) 32.dp else 40.dp)) {
                    FluentIcon(
                        imageVector = FluentIcons.Close,
                        contentDescription = "Close search",
                        tint = MaterialTheme.colorScheme.primary,
                        size = if (compact) PosIconSize.Small else PosIconSize.Medium
                    )
                }
            }
        } else {
            null
        },
        textStyle = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions.Default
    )
}
