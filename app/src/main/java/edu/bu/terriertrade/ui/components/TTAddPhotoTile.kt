package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Dashed tile that opens the photo picker. */
@Composable
fun TTAddPhotoTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Add photo",
    size: Dp = 88.dp,
) {
    val color = MaterialTheme.colorScheme.primary
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = modifier
            .size(size)
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    color = color,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                )
            }
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.AddAPhoto, contentDescription = null, tint = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@PreviewLightDark
@Composable
private fun TTAddPhotoTilePreview() {
    TTPreviewColumn {
        TTAddPhotoTile(onClick = {})
    }
}
