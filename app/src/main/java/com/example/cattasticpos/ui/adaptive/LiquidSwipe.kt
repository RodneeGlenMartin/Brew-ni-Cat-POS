package com.example.cattasticpos.ui.adaptive

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

/** Fast, fluid edge-to-edge horizontal swipe — no fade, matched in/out travel. */
val liquidSwipeSpring = spring<IntOffset>(
    dampingRatio = 0.92f,
    stiffness = 720f
)

fun <T> AnimatedContentTransitionScope<T>.liquidSwipeTransition(forward: Boolean): ContentTransform {
    val spec = liquidSwipeSpring
    return if (forward) {
        slideInHorizontally(spec) { fullWidth -> fullWidth } togetherWith
            slideOutHorizontally(spec) { fullWidth -> -fullWidth }
    } else {
        slideInHorizontally(spec) { fullWidth -> -fullWidth } togetherWith
            slideOutHorizontally(spec) { fullWidth -> fullWidth }
    }
}
