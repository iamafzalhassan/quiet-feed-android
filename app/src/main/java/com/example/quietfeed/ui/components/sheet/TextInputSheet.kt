package com.example.quietfeed.ui.components.sheet

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.example.quietfeed.ui.components.control.AppTextField
import com.example.quietfeed.ui.components.control.PrimaryButton
import com.example.quietfeed.ui.theme.AppSpacing

@Composable
fun TextInputSheet(
    confirmLabel: String,
    initialValue: String,
    label: String,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int = Int.MAX_VALUE,
) {
    var value by rememberSaveable(initialValue) { mutableStateOf(initialValue) }
    val canConfirm = value.isNotBlank() && value.trim() != initialValue.trim()

    AppModalSheet(modifier = modifier, onDismiss = onDismiss, title = title) { sheet ->
        AppTextField(
            capitalization = KeyboardCapitalization.Words,
            label = label,
            maxLength = maxLength,
            onDone = { if (canConfirm) sheet.closeThen { onConfirm(value.trim()) } },
            onValueChange = { text -> value = text },
            value = value,
        )
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        SheetActions(onDismiss = onDismiss, sheet = sheet) {
            PrimaryButton(modifier = Modifier.weight(1f), isEnabled = canConfirm, label = confirmLabel, onClick = { sheet.closeThen { onConfirm(value.trim()) } })
        }
    }
}
