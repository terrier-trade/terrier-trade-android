package edu.bu.terriertrade.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** How far TTAppHeader's rounded bottom corners reach down (its corner radius). */
val TTAppHeaderCornerOverlap: Dp = 24.dp

/**
 * Pulls a tab's content up behind the app frame's red header by [overlap], keeping its bottom where it was.
 * The frame draws its header on top of the content, so a red block at the very top of the content
 * fills in the header's rounded corners, and the header and that block read as one red shape.
 * Browse uses this to show its search bar "inside" the header, like the proposal.
 */
fun Modifier.joinAppHeader(overlap: Dp = TTAppHeaderCornerOverlap): Modifier = layout { measurable, constraints ->
    val shift = overlap.roundToPx()
    val taller = if (constraints.hasBoundedHeight) {
        constraints.copy(minHeight = constraints.minHeight + shift, maxHeight = constraints.maxHeight + shift)
    } else {
        constraints
    }
    val placeable = measurable.measure(taller)
    layout(placeable.width, placeable.height - shift) {
        placeable.place(0, -shift)
    }
}
