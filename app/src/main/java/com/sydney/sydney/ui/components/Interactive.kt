package com.sydney.sydney.ui.components

import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/*
 * Interaction kit. The motion rules come from Emil Kowalski's design engineering notes:
 *  - Press feedback is a quick scale to 0.97, never a bounce.
 *  - Ease-out curves only. Ease-in feels sluggish.
 *  - UI motion stays under 300 ms, and exits are faster than entrances.
 *  - Things used all day (list taps, nav) get the lightest touch. Delight is for rare moments.
 *  - Only transform and opacity animate, so nothing triggers layout.
 *  - If the user turned animations off in system settings, nothing here moves.
 * The ideas for the press, heart and skeleton patterns were picked from 21st.dev examples
 * (Press Depth, Heart Favorite, Card Skeleton) and rebuilt natively, without the 3D or gradients.
 */

/** A stronger ease-out than the built-in one: fast start, long gentle settle. */
val EaseOutStrong = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

private const val PRESS_IN_MS = 120
private const val PRESS_OUT_MS = 180
private const val ENTER_MS = 260
private const val STAGGER_MS = 40L
private const val MAX_STAGGERED_ITEMS = 6

/** False when the user set animations to off in system settings (or a battery saver did). */
@Composable
fun rememberMotionEnabled(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
}

/** Scales down slightly while pressed, so the interface visibly "hears" the touch. */
fun Modifier.pressScale(source: InteractionSource, pressedScale: Float = 0.97f): Modifier = composed {
    val motion = rememberMotionEnabled()
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && motion) pressedScale else 1f,
        animationSpec = tween(if (pressed) PRESS_IN_MS else PRESS_OUT_MS, easing = EaseOutStrong),
        label = "pressScale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** Fades and lifts an item into place. Plays once per item, staggered for the first few. */
fun Modifier.entrance(key: Any, index: Int): Modifier = composed {
    val motion = rememberMotionEnabled()
    var played by rememberSaveable(key) { mutableStateOf(!motion) }
    val progress = remember { Animatable(if (played) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!played) {
            delay(index.coerceAtMost(MAX_STAGGERED_ITEMS) * STAGGER_MS)
            progress.animateTo(1f, tween(ENTER_MS, easing = EaseOutStrong))
            played = true
        }
    }
    graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 12.dp.toPx()
    }
}

// ---- Surfaces ---------------------------------------------------------------------------------

@Composable
private fun hairline(): BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

/** A flat card: paper surface, one-pixel hairline, no shadow. */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    container: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = container,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = hairline(),
    ) { Column(content = content) }
}

/** A tappable [SurfaceCard] that presses in slightly. Reports itself as a button to screen readers. */
@Composable
fun PressableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Surface(
        modifier = modifier
            .pressScale(source)
            .clip(MaterialTheme.shapes.medium)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = hairline(),
    ) { Column(content = content) }
}

// ---- Buttons ----------------------------------------------------------------------------------

private val ButtonShape = RoundedCornerShape(8.dp)

/** Solid ink button. The one main action on a screen. */
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 48.dp)
            .pressScale(source),
        enabled = enabled,
        shape = ButtonShape,
        interactionSource = source,
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        content = content,
    )
}

/** Outlined button for the second action. */
@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 48.dp)
            .pressScale(source),
        enabled = enabled,
        shape = ButtonShape,
        interactionSource = source,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        content = content,
    )
}

/** Text-only action in the accent colour. Links, cancel, sign out. */
@Composable
fun QuietButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    TextButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 48.dp)
            .pressScale(source, pressedScale = 0.98f),
        enabled = enabled,
        shape = ButtonShape,
        interactionSource = source,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary),
        content = content,
    )
}

// ---- Heart ------------------------------------------------------------------------------------

/**
 * Save toggle. Saving pops the heart with a small spring so the action feels acknowledged;
 * unsaving is a plain colour fade, because undoing something should not celebrate.
 */
@Composable
fun HeartToggle(
    saved: Boolean,
    onToggle: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val motion = rememberMotionEnabled()
    val pop = remember { Animatable(1f) }
    var first by remember { mutableStateOf(true) }
    LaunchedEffect(saved) {
        if (first) {
            first = false
        } else if (saved && motion) {
            pop.snapTo(0.72f)
            pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium))
        }
    }
    val tint by animateColorAsState(
        if (saved) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
        tween(160, easing = EaseOutStrong),
        label = "heartTint",
    )
    val source = remember { MutableInteractionSource() }
    IconButton(onClick = onToggle, modifier = modifier.pressScale(source, 0.9f), interactionSource = source) {
        Icon(
            imageVector = if (saved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.graphicsLayer {
                scaleX = pop.value
                scaleY = pop.value
            },
        )
    }
}

// ---- Loading ----------------------------------------------------------------------------------

/** A grey block that gently pulses. Opacity only, so it is cheap and respects reduced motion. */
@Composable
fun Skeleton(modifier: Modifier = Modifier, shape: Shape = MaterialTheme.shapes.small) {
    val motion = rememberMotionEnabled()
    val alpha = if (motion) {
        val transition = rememberInfiniteTransition(label = "skeleton")
        transition.animateFloat(
            initialValue = 0.45f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "skeletonAlpha",
        ).value
    } else {
        0.7f
    }
    Box(
        modifier
            .graphicsLayer { this.alpha = alpha }
            .clip(shape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
    )
}

/** Placeholder with the same footprint as a hostel card, so the list does not jump when it loads. */
@Composable
fun HostelCardSkeleton(modifier: Modifier = Modifier) {
    SurfaceCard(modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Skeleton(Modifier.size(96.dp), MaterialTheme.shapes.small)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Skeleton(Modifier.fillMaxWidth(0.7f).height(16.dp))
                Skeleton(Modifier.fillMaxWidth(0.45f).height(12.dp))
                Skeleton(Modifier.fillMaxWidth(0.3f).height(14.dp))
            }
        }
    }
}

@Composable
fun HostelListSkeleton(modifier: Modifier = Modifier, count: Int = 4) {
    Column(
        modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(count) { HostelCardSkeleton() }
    }
}
