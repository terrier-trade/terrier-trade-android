package edu.bu.terriertrade.ui.components.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import edu.bu.terriertrade.ui.preview.TTPreviewColumn

/**
 * Whole-dollar price input. Only digits get through, up to maxDigits.
 * The value stays a String so the field can be empty while typing.
 */
@Composable
fun TTPriceField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Price",
    errorMessage: String? = null,
    helperText: String? = "Whole dollars. Enter 0 to give it away free.",
    maxDigits: Int = 4,
) {
    TTTextField(
        value = value,
        onValueChange = { typed -> onValueChange(typed.filter(Char::isDigit).take(maxDigits)) },
        label = label,
        placeholder = "40",
        errorMessage = errorMessage,
        helperText = helperText,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        trailingIcon = { Text("USD") },
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
private fun TTPriceFieldPreview() {
    TTPreviewColumn {
        TTPriceField(value = "40", onValueChange = {}, modifier = Modifier.fillMaxWidth())
        TTPriceField(value = "", onValueChange = {}, errorMessage = "Enter a price", modifier = Modifier.fillMaxWidth())
    }
}
