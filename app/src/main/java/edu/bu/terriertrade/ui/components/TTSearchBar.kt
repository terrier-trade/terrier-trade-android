package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark

/**
 * White pill search field. Shows a clear (×) button while typing.
 * When [onFilterClick] is set and the field is empty, shows a filter button instead.
 */
@Composable
fun TTSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search books, tutoring, tickets",
    onSearch: () -> Unit = {},
    onFilterClick: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                }
            }
        } else if (onFilterClick != null) {
            {
                IconButton(
                    onClick = onFilterClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = colors.primaryContainer,
                        contentColor = colors.onPrimaryContainer,
                    ),
                ) {
                    Icon(Icons.Outlined.Tune, contentDescription = "Filters")
                }
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        shape = CircleShape,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = colors.primary,
            unfocusedPlaceholderColor = colors.onSurfaceVariant,
            focusedPlaceholderColor = colors.onSurfaceVariant,
        ),
    )
}

@PreviewLightDark
@Composable
private fun TTSearchBarPreview() {
    TTPreviewColumn {
        TTSearchBar(query = "", onQueryChange = {})
        TTSearchBar(query = "", onQueryChange = {}, onFilterClick = {})
        TTSearchBar(query = "calculus", onQueryChange = {}, onFilterClick = {})
    }
}