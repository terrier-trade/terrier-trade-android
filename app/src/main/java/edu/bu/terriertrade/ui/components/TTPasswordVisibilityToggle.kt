package edu.bu.terriertrade.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark

/** "Show" / "Hide" toggle for password fields. Stateless: the caller owns [visible]. */
@Composable
fun TTPasswordVisibilityToggle(
    visible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TTTextButton(
        text = if (visible) "Hide" else "Show",
        onClick = onToggle,
        modifier = modifier.semantics {
            contentDescription = if (visible) "Hide password" else "Show password"
        },
    )
}

@PreviewLightDark
@Composable
private fun TTPasswordVisibilityTogglePreview() {
    TTPreviewColumn {
        TTPasswordVisibilityToggle(visible = false, onToggle = {})
        TTPasswordVisibilityToggle(visible = true, onToggle = {})
    }
}