package org.openedx.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry

expect val navEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
expect val navExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
expect val navPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
expect val navPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
