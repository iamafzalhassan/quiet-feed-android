package com.example.quietfeed.ui.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.quietfeed.R
import com.example.quietfeed.ui.theme.AppColors
import com.example.quietfeed.ui.theme.AppSpacing
import com.example.quietfeed.ui.theme.AppTextStyles

@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    backLabel: String = stringResource(R.string.action_back),
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backIcon: ImageVector = Icons.AutoMirrored.Outlined.ArrowBack,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.surfaceBase)
            .statusBarsPadding()
            .heightIn(min = AppSpacing.topBarHeight)
            .padding(end = AppSpacing.xs, start = if (onBack == null) AppSpacing.screenPadding else AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(contentDescription = backLabel, imageVector = backIcon, tint = AppColors.primary)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTextStyles.screenTitle, text = title)
            if (subtitle != null) {
                Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTextStyles.listSecondary, text = subtitle)
            }
        }
        actions()
    }
}
