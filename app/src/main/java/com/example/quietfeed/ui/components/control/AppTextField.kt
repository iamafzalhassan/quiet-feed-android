package com.example.quietfeed.ui.components.control

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.example.quietfeed.ui.theme.AppColors
import com.example.quietfeed.ui.theme.AppSpacing
import com.example.quietfeed.ui.theme.AppTextStyles

@Composable
fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    maxLength: Int = Int.MAX_VALUE,
    onDone: (() -> Unit)? = null,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    FieldFrame(modifier = modifier, isFocused = isFocused, label = label, onTap = { focusRequester.requestFocus() }) {
        BasicTextField(
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onFocusChanged { isFocused = it.isFocused },
            cursorBrush = SolidColor(AppColors.primary),
            enabled = isEnabled,
            keyboardActions = KeyboardActions(onDone = { if (onDone == null) focusManager.clearFocus() else onDone() }),
            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, capitalization = capitalization, imeAction = ImeAction.Done, keyboardType = keyboardType),
            onValueChange = { text -> onValueChange(text.take(maxLength)) },
            singleLine = true,
            textStyle = AppTextStyles.fieldValue.copy(color = if (isEnabled) AppColors.textPrimary else AppColors.textDisabled),
            value = value,
        )
    }
}

@Composable
private fun FieldFrame(label: String, onTap: () -> Unit, modifier: Modifier = Modifier, isFocused: Boolean = false, content: @Composable () -> Unit) {
    val currentOnTap by rememberUpdatedState(onTap)
    val shape = RoundedCornerShape(AppSpacing.radiusField)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AppSpacing.controlHeight)
            .clip(shape)
            .background(color = AppColors.surfaceField, shape = shape)
            .pointerInput(Unit) { detectTapGestures { currentOnTap() } }
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = AppTextStyles.fieldLabel.copy(color = if (isFocused) AppColors.primary else AppColors.textSecondary),
                text = label,
            )
            content()
        }
    }
}
