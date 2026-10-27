package com.example.quietfeed.ui.components.display

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.example.quietfeed.ui.theme.AppColors
import com.example.quietfeed.ui.theme.AppSpacing
import com.example.quietfeed.ui.theme.AppTextStyles

@Composable
fun AppListTile(
    title: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    titleMaxLines: Int = 1,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val shape = RoundedCornerShape(AppSpacing.radiusCard)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AppSpacing.controlHeight)
            .clip(shape)
            .background(color = if (isSelected) AppColors.surfaceSunken else AppColors.surfaceCard, shape = shape)
            .border(border = BorderStroke(AppSpacing.hairline, if (isSelected) AppColors.primary else AppColors.divider), shape = shape)
            .then(if (onClick == null && onLongClick == null) Modifier else Modifier.combinedClickable(onClick = onClick ?: {}, onLongClick = onLongClick, role = Role.Button))
            .padding(AppSpacing.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(maxLines = titleMaxLines, overflow = TextOverflow.Ellipsis, style = AppTextStyles.listPrimary, text = title)
            if (subtitle != null) {
                Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTextStyles.listMeta, text = subtitle)
            }
        }
        trailing()
    }
}
