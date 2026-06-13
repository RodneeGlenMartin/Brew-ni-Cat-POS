package com.example.cattasticpos.ui.adaptive

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
    Snap,
    Success,
    Error,
    Add
}
