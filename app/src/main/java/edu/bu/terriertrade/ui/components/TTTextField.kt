package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark

/**
 * Base text field for the app. Stateless: the screen owns [value] and updates it in [onValueChange].
 * Shows [errorMessage] in red under the field if set, otherwise [helperText].
 */
@Composable
fun TTTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    errorMessage: String? = null,
    helperText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val supporting = errorMessage ?: helperText

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        placeholder = if (placeholder != null) { { Text(placeholder) } } else null,
        supportingText = if (supporting != null) { { Text(supporting) } } else null,
        isError = errorMessage != null,
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        shape = MaterialTheme.shapes.medium,
    )
}

@PreviewLightDark
@Composable
private fun TTTextFieldPreview() {
    TTPreviewColumn {
        TTTextField(
            value = "",
            onValueChange = {},
            label = "Title",
            placeholder = "e.g. Calculus textbook",
            modifier = Modifier.fillMaxWidth(),
        )
        TTTextField(
            value = "abc",
            onValueChange = {},
            label = "Price",
            errorMessage = "Enter a valid price",
            modifier = Modifier.fillMaxWidth(),
        )
        TTTextField(
            value = "Lightly used, no highlighting.",
            onValueChange = {},
            label = "Description",
            helperText = "Condition, edition, pickup spot",
            singleLine = false,
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}