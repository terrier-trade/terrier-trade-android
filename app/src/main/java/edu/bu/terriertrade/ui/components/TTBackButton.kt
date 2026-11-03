package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.theme.TTTheme

/** OnRed: sits on a red header. Outlined: sits on the plain background. */
enum class TTBackButtonStyle { OnRed, Outlined }

/** Round back arrow, 48dp touch target. */
@Composable
fun TTBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TTBackButtonStyle = TTBackButtonStyle.Outlined,
    contentDescription: String = "Back",
) {
    val colors = MaterialTheme.colorScheme
    val isOnRed = style == TTBackButtonStyle.OnRed
    IconButton(
        onClick = onClick,
        modifier = if (isOnRed) modifier else modifier.border(1.dp, colors.outline, CircleShape),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isOnRed) TTTheme.colors.onHeader.copy(alpha = 0.15f) else Color.Transparent,
            contentColor = if (isOnRed) TTTheme.colors.onHeader else colors.onBackground,
        ),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = contentDescription)
    }
}

@PreviewLightDark
@Composable
private fun TTBackButtonPreview() {
    TTPreviewColumn {
        TTBackButton(onClick = {})
    }
}