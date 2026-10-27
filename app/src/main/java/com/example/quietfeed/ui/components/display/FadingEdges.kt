package com.example.quietfeed.ui.components.display

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.example.quietfeed.ui.theme.AppSpacing

private const val FADE_ANIMATION_MILLIS: Int = 200

@Composable
fun Modifier.fadingEdges(bottomInset: Dp, state: ScrollableState): Modifier {
    val topFade by animateFloatAsState(animationSpec = tween(FADE_ANIMATION_MILLIS), label = "topFade", targetValue = if (state.canScrollBackward) 1f else 0f)
    val bottomFade by animateFloatAsState(animationSpec = tween(FADE_ANIMATION_MILLIS), label = "bottomFade", targetValue = if (state.canScrollForward) 1f else 0f)

    return graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }.drawWithContent {
        drawContent()
        val fade = AppSpacing.fadingEdge.toPx()
        val bottom = size.height - bottomInset.toPx()
        drawRect(
            blendMode = BlendMode.DstIn,
            brush = Brush.verticalGradient(0f to Color.Black.copy(alpha = 1f - topFade), 1f to Color.Black, endY = fade, startY = 0f),
            size = Size(size.width, fade),
        )
        drawRect(
            blendMode = BlendMode.DstIn,
            brush = Brush.verticalGradient(0f to Color.Black, 1f to Color.Black.copy(alpha = 1f - bottomFade), endY = bottom, startY = bottom - fade),
            size = Size(size.width, fade),
            topLeft = Offset(0f, bottom - fade),
        )
        drawRect(
            blendMode = BlendMode.DstIn,
            color = Color.Black.copy(alpha = 1f - bottomFade),
            size = Size(size.width, size.height - bottom),
            topLeft = Offset(0f, bottom),
        )
    }
}
