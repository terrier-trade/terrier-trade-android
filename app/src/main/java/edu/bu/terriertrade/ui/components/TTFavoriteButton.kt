package edu.bu.terriertrade.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark

/**
 * Heart button: red filled when favorited, grey outline when not.
 * Set [onPhoto] to put it on a white circle so it stands out on a picture.
 */
@Composable
fun TTFavoriteButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onPhoto: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    IconButton(
        onClick = onToggle,
        modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (onPhoto) colors.surface else Color.Transparent,
            contentColor = if (isFavorite) colors.primary else colors.onSurfaceVariant,
        ),
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
        )
    }
}

@PreviewLightDark
@Composable
private fun TTFavoriteButtonPreview() {
    TTPreviewColumn {
        TTFavoriteButton(isFavorite = false, onToggle = {})
        TTFavoriteButton(isFavorite = true, onToggle = {})
        TTFavoriteButton(isFavorite = true, onToggle = {}, onPhoto = true)
    }
}