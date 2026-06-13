package com.example.cattasticpos.ui.adaptive

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

fun Modifier.posPressable(
    onClick: () -> Unit,
    enabled: Boolean = true,
    feedback: FeedbackEvent = FeedbackEvent(BionicHaptic.Selection, PosSound.Tap),
    scalePressed: Float = 0.96f
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) scalePressed else 1f,
        animationSpec = iOSSpring,
        label = "posPressScale"
    )
    val performFeedback = rememberPosFeedback()
    scale(scale)
        .semantics { role = Role.Button }
        .clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                performFeedback(feedback)
                onClick()
            }
        )
}

@Composable
fun rememberPressScale(interactionSource: MutableInteractionSource, pressedScale: Float = 0.96f): Float {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = iOSSpring,
        label = "pressScale"
    )
    return scale
}
