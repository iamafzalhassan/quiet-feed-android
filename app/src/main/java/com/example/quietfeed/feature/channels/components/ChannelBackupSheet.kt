package com.example.quietfeed.feature.channels.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.quietfeed.R
import com.example.quietfeed.ui.components.display.AppListTile
import com.example.quietfeed.ui.components.sheet.AppModalSheet
import com.example.quietfeed.ui.theme.AppSpacing

@Composable
fun ChannelBackupSheet(onDismiss: () -> Unit, onExport: () -> Unit, onImport: () -> Unit, modifier: Modifier = Modifier) {
    AppModalSheet(modifier = modifier, onDismiss = onDismiss, title = stringResource(R.string.backup_title)) { sheet ->
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            AppListTile(onClick = { sheet.closeThen(onExport) }, subtitle = stringResource(R.string.backup_export_subtitle), title = stringResource(R.string.backup_export_title))
            AppListTile(onClick = { sheet.closeThen(onImport) }, subtitle = stringResource(R.string.backup_import_subtitle), title = stringResource(R.string.backup_import_title))
        }
    }
}
