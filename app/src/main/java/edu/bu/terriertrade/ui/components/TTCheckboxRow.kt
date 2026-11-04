package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.theme.Spacing

/**
 * Checkbox with a sentence next to it. The whole row is tappable, not just the box.
 * With [linkText] and [onLinkClick], the end of the sentence is a red underlined link,
 * like "I agree to the Terms & community rules".
 */
@Composable
fun TTCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    linkText: String? = null,
    onLinkClick: (() -> Unit)? = null,
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val label = buildAnnotatedString {
        append(text)
        if (linkText != null && onLinkClick != null) {
            append(" ")
            withLink(
                LinkAnnotation.Clickable(
                    tag = "link",
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor,
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ),
                    linkInteractionListener = { onLinkClick() },
                ),
            ) { append(linkText) }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Checkbox(checked = checked, onCheckedChange = null)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        TTErrorText(message = errorMessage)
    }
}

@PreviewLightDark
@Composable
private fun TTCheckboxRowPreview() {
    TTPreviewColumn {
        TTCheckboxRow(checked = true, onCheckedChange = {}, text = "I'll meet in public places on campus.")
        TTCheckboxRow(
            checked = false,
            onCheckedChange = {},
            text = "I agree to the",
            linkText = "Terms & community rules",
            onLinkClick = {},
            errorMessage = "Agree to the community rules to continue",
        )
    }
}
