package com.example.quietfeed.feature.channels.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.example.quietfeed.R
import com.example.quietfeed.domain.channel.AddChannelError
import com.example.quietfeed.domain.channel.Channel
import com.example.quietfeed.ui.components.control.AppTextField
import com.example.quietfeed.ui.components.control.PrimaryButton
import com.example.quietfeed.ui.components.sheet.AppModalSheet
import com.example.quietfeed.ui.components.sheet.SheetActions
import com.example.quietfeed.ui.theme.AppSpacing
import com.example.quietfeed.ui.theme.AppTextStyles

@Composable
fun AddChannelSheet(
    isAdding: Boolean,
    addedChannelName: String?,
    onClosed: () -> Unit,
    onInputChange: () -> Unit,
    onAdd: (input: String, name: String) -> Unit,
    addError: AddChannelError?,
    modifier: Modifier = Modifier,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    val canAdd = input.isNotBlank() && name.isNotBlank() && !isAdding

    AppModalSheet(modifier = modifier, onDismiss = onClosed, title = stringResource(R.string.add_channel_title)) { sheet ->
        LaunchedEffect(addedChannelName) {
            if (addedChannelName != null) {
                sheet.closeThen(onClosed)
            }
        }
        AppTextField(
            capitalization = KeyboardCapitalization.Words,
            isEnabled = !isAdding,
            label = stringResource(R.string.field_name),
            maxLength = Channel.MAX_NAME_LENGTH,
            onValueChange = { value -> name = value },
            value = name,
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        AppTextField(
            capitalization = KeyboardCapitalization.None,
            isEnabled = !isAdding,
            keyboardType = KeyboardType.Uri,
            label = stringResource(R.string.field_channel),
            onDone = { if (canAdd) onAdd(input.trim(), name.trim()) },
            onValueChange = { value ->
                input = value
                onInputChange()
            },
            value = input,
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = if (addError == null) AppTextStyles.listSecondary else AppTextStyles.errorHint,
            text = stringResource(addError?.messageRes ?: R.string.add_channel_hint),
        )
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        SheetActions(onDismiss = onClosed, sheet = sheet) {
            PrimaryButton(modifier = Modifier.weight(1f), isEnabled = input.isNotBlank() && name.isNotBlank(), isLoading = isAdding, label = stringResource(R.string.action_add), onClick = { onAdd(input.trim(), name.trim()) })
        }
    }
}

private val AddChannelError.messageRes: Int
    get() = when (this) {
        AddChannelError.ALREADY_ADDED -> R.string.add_error_already_added
        AddChannelError.FAILED -> R.string.add_error_failed
        AddChannelError.INVALID_INPUT -> R.string.add_error_invalid_input
        AddChannelError.NOT_FOUND -> R.string.add_error_not_found
        AddChannelError.OFFLINE -> R.string.add_error_offline
    }
