package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.theme.Spacing

/** Inner content shared by all TT buttons: optional icon + label, or a spinner while loading. */
@Composable
internal fun TTButtonContent(
    text: String,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(18.dp)
                .semantics { contentDescription = "$text, loading" },
            strokeWidth = 2.dp,
            color = LocalContentColor.current,
        )
    } else {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.sm))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}