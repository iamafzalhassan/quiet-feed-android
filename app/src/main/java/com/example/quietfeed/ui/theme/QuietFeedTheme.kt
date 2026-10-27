package com.example.quietfeed.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private const val SELECTION_ALPHA: Float = 0.24f

private val QuietFeedColorScheme: ColorScheme = darkColorScheme(
    background = AppColors.surfaceBase,
    error = AppColors.danger,
    errorContainer = AppColors.dangerSurface,
    inverseOnSurface = AppColors.textPrimary,
    inverseSurface = AppColors.surfaceInverse,
    onBackground = AppColors.textPrimary,
    onError = AppColors.primaryOn,
    onErrorContainer = AppColors.textPrimary,
    onPrimary = AppColors.primaryOn,
    onPrimaryContainer = AppColors.primaryOn,
    onSecondary = AppColors.primaryOn,
    onSurface = AppColors.textPrimary,
    onSurfaceVariant = AppColors.textSecondary,
    outline = AppColors.divider,
    outlineVariant = AppColors.divider,
    primary = AppColors.primary,
    primaryContainer = AppColors.primary,
    scrim = AppColors.surfaceBase,
    secondary = AppColors.primary,
    surface = AppColors.surfaceCard,
    surfaceContainer = AppColors.surfaceCard,
    surfaceContainerHigh = AppColors.surfaceCard,
    surfaceContainerHighest = AppColors.surfaceField,
    surfaceContainerLow = AppColors.surfaceBase,
    surfaceContainerLowest = AppColors.surfaceBase,
    surfaceVariant = AppColors.surfaceField,
    tertiary = AppColors.primary,
)

private val QuietFeedTypography: Typography = Typography(
    bodyLarge = AppTextStyles.body,
    bodyMedium = AppTextStyles.listSecondary,
    bodySmall = AppTextStyles.errorHint,
    labelLarge = AppTextStyles.button,
    labelSmall = AppTextStyles.fieldLabel,
    titleLarge = AppTextStyles.screenTitle,
    titleMedium = AppTextStyles.sectionHeading,
    titleSmall = AppTextStyles.listPrimary,
)

@Composable
fun QuietFeedTheme(content: @Composable () -> Unit) {
    val selectionColors = TextSelectionColors(backgroundColor = AppColors.primary.copy(alpha = SELECTION_ALPHA), handleColor = AppColors.primary)

    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
        MaterialTheme(colorScheme = QuietFeedColorScheme, typography = QuietFeedTypography, content = content)
    }
}
