package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.theme.Spacing

/**
 * One-time code input drawn as separate boxes. Only digits are accepted, up to [length].
 * The box where the next digit goes is outlined in red.
 */
@Composable
fun TTCodeField(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    isError: Boolean = false,
    onDone: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    BasicTextField(
        value = code,
        onValueChange = { typed -> onCodeChange(typed.filter(Char::isDigit).take(length)) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                repeat(length) { index ->
                    val digit = code.getOrNull(index)
                    val isNext = index == code.length
                    val borderColor = when {
                        isError -> colors.error
                        digit != null || isNext -> colors.primary
                        else -> colors.outline
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .background(colors.surface, MaterialTheme.shapes.medium)
                            .border(if (isNext) 2.dp else 1.dp, borderColor, MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = digit?.toString().orEmpty(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.onSurface,
                        )
                    }
                }
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun TTCodeFieldPreview() {
    TTPreviewColumn {
        TTCodeField(code = "4820", onCodeChange = {})
        TTCodeField(code = "12", onCodeChange = {}, isError = true)
    }
}