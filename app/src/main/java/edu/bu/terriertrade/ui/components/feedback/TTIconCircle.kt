package edu.bu.terriertrade.ui.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.preview.TTPreviewColumn

/** A big icon in a tinted circle, for the top of confirmation and empty screens. Decorative only. */
@Composable
fun TTIconCircle(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(containerColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(size * 0.46f),
        )
    }
}

@PreviewLightDark
@Composable
private fun TTIconCirclePreview() {
    TTPreviewColumn {
        TTIconCircle(icon = Icons.Outlined.Email)
    }
}