package com.example.cattasticpos.ui.adaptive

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

val iOSSpringSpec = spring<Float>(
    dampingRatio = 0.82f,
    stiffness = 380f
)

/** @see iOSSpringSpec */
val iOSSpring = iOSSpringSpec

val iOSSpringSize = spring<IntSize>(
    dampingRatio = 0.82f,
    stiffness = 380f
)

val iOSSpringDp = spring<Dp>(
    dampingRatio = 0.82f,
    stiffness = 380f
)

val iOSNavigationSpring = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow
)

enum class BionicHaptic {
    Light,
    Selection,
    Confirm,
    Snap
}

@Composable
fun rememberBionicHaptic(): (BionicHaptic) -> Unit {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    return remember(haptic, view) {
        { type ->
            when (type) {
                BionicHaptic.Light -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                BionicHaptic.Selection -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                BionicHaptic.Confirm -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                BionicHaptic.Snap -> view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }
    }
}
