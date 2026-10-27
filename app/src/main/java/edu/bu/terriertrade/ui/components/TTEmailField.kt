package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark

/** Email field for BU addresses: email keyboard, autocorrect off, "you@bu.edu" placeholder. */
@Composable
fun TTEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "BU email",
    placeholder: String = "you@bu.edu",
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    TTTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        errorMessage = errorMessage,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = imeAction,
            autoCorrectEnabled = false,
        ),
        keyboardActions = keyboardActions,
    )
}

@PreviewLightDark
@Composable
private fun TTEmailFieldPreview() {
    TTPreviewColumn {
        TTEmailField(value = "", onValueChange = {}, modifier = Modifier.fillMaxWidth())
        TTEmailField(
            value = "priya@gmail.com",
            onValueChange = {},
            errorMessage = "Use your @bu.edu email",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}