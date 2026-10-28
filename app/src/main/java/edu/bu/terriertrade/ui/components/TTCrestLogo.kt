package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.R

/**
 * The Terrier Trade crest on a white circle.
 * Always white behind the crest, in dark mode too, because it's the brand mark.
 */
@Composable
fun TTCrestLogo(
    modifier: Modifier = Modifier,
    size: Dp = 112.dp,
    contentDescription: String? = "Terrier Trade crest",
) {
    Box(
        modifier = modifier
            .size(size)
            .background(Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_crest),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .padding(size / 10),
        )
    }
}

@PreviewLightDark
@Composable
private fun TTCrestLogoPreview() {
    TTPreviewColumn {
        TTCrestLogo()
        TTCrestLogo(size = 48.dp)
    }
}