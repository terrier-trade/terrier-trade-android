package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark

/**
 * Password field with a Show/Hide toggle.
 * The screen owns the password text; only the purely visual show/hide state lives here.
 */
@Composable
fun TTPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Password",
    errorMessage: String? = null,
    helperText: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    TTTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        errorMessage = errorMessage,
        helperText = helperText,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
            autoCorrectEnabled = false,
        ),
        keyboardActions = keyboardActions,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TTPasswordVisibilityToggle(visible = visible, onToggle = { visible = !visible })
        },
    )
}


@PreviewLightDark
@Composable
private fun TTPasswordFieldPreview() {
    TTPreviewColumn {
        TTPasswordField(value = "secret123", onValueChange = {}, modifier = Modifier.fillMaxWidth())
        TTPasswordField(
            value = "abc",
            onValueChange = {},
            errorMessage = "At least 8 characters",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}